package com.start.receivers

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.util.Log
import com.start.utils.restoreTimeVibro
import java.util.*

class MyRees : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        createNewAlarm(context)
        disableVibro(context)
    }

    companion object {
        fun time(timeString: String): Long {
            val parts = timeString.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            val current = Calendar.getInstance()
            if (Integer.parseInt(parts[0])<= current.get(Calendar.HOUR_OF_DAY) &&
                    Integer.parseInt(parts[1])<= current.get(Calendar.MINUTE)){
                current.add(Calendar.DATE, 1)
            }
            current.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]))
            current.set(Calendar.MINUTE, Integer.parseInt(parts[1]))
            current.set(Calendar.SECOND, 0)

            return current.timeInMillis
        }
    }

    private fun disableVibro(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        when (audioManager.ringerMode) {
            AudioManager.RINGER_MODE_VIBRATE,
            AudioManager.RINGER_MODE_SILENT -> {
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
            }
        }
    }

    private fun createNewAlarm(context: Context) {
        val timeInMillis = time(restoreTimeVibro(context))

        val intentAlarm = Intent(context, MyRees::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 100, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        val data = Date(timeInMillis)
        Log.d("Package__", data.toString())
    }
}
