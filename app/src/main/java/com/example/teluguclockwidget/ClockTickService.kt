package com.example.teluguclockwidget

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder

class ClockTickService : Service() {
    private val receiver = TimeTickReceiver()

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter(Intent.ACTION_TIME_TICK)
        registerReceiver(receiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
