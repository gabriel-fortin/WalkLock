package com.example.g14.walklock

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import android.os.Binder
import android.os.IBinder

class WakeService : Service() {
    private val tag = WakeService::class.simpleName

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private val binder = LocalBinder()

    private lateinit var notificationManager: NotificationManager
    private lateinit var sensorManager: SensorManager

//    private val wakeLock: PowerManager.WakeLock


    override fun onCreate() {
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        super.onCreate()
    }

    override fun onBind(p0: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
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
