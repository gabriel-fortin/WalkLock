package com.example.g14.walklock

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val tag = MainActivity::class.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startButton.setOnClickListener(this::start)
        stopButton.setOnClickListener(this::stop)
    }

    fun start(v: View) {
        val minutes = Integer.parseInt(minutesEditText.text.toString())
        val seconds = Integer.parseInt(secondsEditText.text.toString())
        val duration = 60 * minutes + seconds
    }

    fun stop(v: View) {
    }
}


