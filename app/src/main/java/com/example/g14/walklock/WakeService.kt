package com.example.g14.walklock

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.*
import android.util.Log
import java.util.*

const val NOTIF_ID = 5637 // notification id for foreground service

class WakeService : Service() {
    private val tag = WakeService::class.simpleName

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private val binder = LocalBinder()

    private lateinit var foregroundNotification: ForegroundNotification
    private lateinit var keepingSensorsBusy: KeepingSensorsBusy
    private lateinit var powerManager: PowerManager

    private val timer: Timer = Timer()


    lateinit var wakeLock: PowerManager.WakeLock


    override fun onCreate() {
        foregroundNotification = ForegroundNotification()
        keepingSensorsBusy = KeepingSensorsBusy()
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager

        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "wake lock TAG")
                .apply { setReferenceCounted(false) }

        super.onCreate()
    }

    override fun onBind(p0: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            Log.e(tag, "intent is null; fix this!!!")
            return START_STICKY
        }

        val command = intent.getStringExtra(COMMAND)
        Log.i(tag, "start command: $command")
        when (command) {
            START -> startCounter(intent.getIntExtra(DURATION, -1))
            STOP -> stopCounter()
            null -> Log.e(tag, "command missing (us null)")
            else -> Log.e(tag, "unrecognized command: $command")
        }

        // TODO: do I need to call super.onStartCommand?
        return super.onStartCommand(intent, flags, startId)

//        return START_STICKY
    }

    fun startCounter(duration: Int) {
        if (duration <= 0) {
            Log.e(tag, "invalid duration; should be >0")
            return
        }

        foregroundNotification.start(Date().time + (duration * 1000))
        keepingSensorsBusy.start()
        timer.after(duration, this::stopCounter)
        wakeLock.acquire(duration * 1000L)
    }

    fun stopCounter() {
        foregroundNotification.stop()
        keepingSensorsBusy.stop()
        stopSelf()
        timer.cancelCurrentShedule()
        wakeLock.release()
    }

    inner class ForegroundNotification {
        fun start(timestamp: Long) {
            val notifIntent = Intent(this@WakeService, MainActivity::class.java)
            val pendingIntent = PendingIntent
                    .getActivity(this@WakeService, /*req code:*/ 0, notifIntent, /*flags:*/ 0)

            val notification: Notification = with(Notification.Builder(this@WakeService)) {
                setContentText("sensors are being used")
                setContentTitle("WORKING! (or… walking?)")
                setSmallIcon(R.drawable.nogi)
                setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.nogi))
//            setLargeIcon(resources.getDrawable(R.drawable.nogi, null).)

                setContentIntent(pendingIntent)

                if (Build.VERSION.SDK_INT >= 24) {
                    setUsesChronometer(true)
                    setChronometerCountDown(true)  // requires API 24
                } else {
                    setUsesChronometer(false)
                }
                setWhen(timestamp)
            }.build()

            Log.i(tag, "starting foreground")
            this@WakeService.startForeground(NOTIF_ID, notification)
        }

        fun stop() {
            val removeStatusBar = true
            Log.i(tag, "stopping foreground")
            this@WakeService.stopForeground(removeStatusBar)
        }
    }

    inner class KeepingSensorsBusy {
        private val sensorManager: SensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        private val listener = object : SensorEventListener {
            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
            override fun onSensorChanged(p0: SensorEvent?) {}
        }

        fun start() {
            val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
            sensorManager.registerListener(listener, gyroscope, SensorManager.SENSOR_DELAY_NORMAL)
        }

        fun stop() {
            sensorManager.unregisterListener(listener)
        }
    }

    inner class Timer {
        lateinit var action: () -> Unit
        val postableAction = Runnable { action() }
        val handler = Handler(Looper.getMainLooper())

        fun after(seconds: Int, performAction: () -> Unit) {
            cancelCurrentShedule()
            action = performAction
            handler.postDelayed(postableAction, seconds * 1000L)
        }

        fun cancelCurrentShedule() {
            handler.removeCallbacks(postableAction)
        }
    }

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    inner class LocalBinder : Binder() {
        internal val service: WakeService
            get() = this@WakeService
    }


}
