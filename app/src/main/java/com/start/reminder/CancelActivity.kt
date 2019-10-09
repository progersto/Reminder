package com.start.reminder

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.start.receivers.AlarmReceiver

class CancelActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        finish()
    }

    override fun onResume() {
        super.onResume()
        Log.d("Package__", "CancelActivity")
        Handler().post {
            Log.d("Package__", "CancelActivity")
            AlarmReceiver.cancelAlarm(this)
        }
    }
}