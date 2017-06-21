package com.example.g14.walklock

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View

import kotlinx.android.synthetic.main.activity_main.*

const val COMMAND = "command"
const val START = "start"
const val STOP = "stop"
const val DURATION = "duration in seconds"

class MainActivity : AppCompatActivity() {
    private val tag = MainActivity::class.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startButton.setOnClickListener(this::start)
        stopButton.setOnClickListener(this::stop)
        plusFiveButton.setOnClickListener { editMinutes(5) }
        minusFiveButton.setOnClickListener { editMinutes(-5) }
    }

    fun start(v: View) {
        val minutes = Integer.parseInt(minutesEditText.text.toString())
        val seconds = Integer.parseInt(secondsEditText.text.toString())
        val duration = 60 * minutes + seconds

        val serviceIntent = Intent(this, WakeService::class.java).apply {
            putExtra(COMMAND, START)
            putExtra(DURATION, duration)
        }

        Log.i(tag, "starting service from activity")
        startService(serviceIntent)
    }

    fun stop(v: View) {
        val serviceIntent = Intent(this, WakeService::class.java).apply {
            putExtra(COMMAND, STOP)
        }

        startService(serviceIntent)
    }

    fun editMinutes(diff: Int) {
        val minutes: Int = Integer.parseInt(minutesEditText.text.toString())
        val newValue = Integer.toString(minutes + diff)
        with (minutesEditText.text) {
            replace(0, this.length, newValue)
        }
    }
}


