package com.start.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.Intent

import android.media.MediaPlayer
import android.os.*
import android.util.Log
import android.widget.Toast
import com.start.utils.*
import java.io.IOException
import java.util.*

class Alarm : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, javaClass.name)
        wakeLock.acquire()

        Toast.makeText(context, "Alarm !!!!!!!!!!", Toast.LENGTH_LONG).show()
        val mediaPlayer = MediaPlayer()
        try {
            context.vibrate(longArrayOf(100, 200))

            val descriptor = context.assets.openFd("win" + ".mp3")
            mediaPlayer.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
            descriptor.close()
            mediaPlayer.prepare()
            mediaPlayer.start()

        } catch (e: IOException) {
            Log.e("Package__", "error ${e.message}")
            e.printStackTrace()
        }
        wakeLock.release()
    }

    companion object {

        fun setAlarm(context: Context) {
            val timeF = restoreTimeFrom(context)
            val timeFrom = compareDate(timeF)
            val timeT = restoreTimeTo(context)
            val timeTo = compareDate(timeT)

            if (!timeFrom && timeTo){
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(context, Alarm::class.java)
                val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
                alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                        (1000 * 60 * restoreTime(context)).toLong(),
                        pendingIntent
                )
                Log.d("Package__", "setAlarm " + restoreTime(context))
            }
        }

        fun cancelAlarm(context: Context) {
            val intent = Intent(context, Alarm::class.java)
            val sender = PendingIntent.getBroadcast(context, 0, intent, 0)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(sender)
            canceledAlarm(context, false)
            Log.d("Package__", "cancelAlarm")
        }

        private fun compareDate(reference: String): Boolean {
            val now = Calendar.getInstance()
            val hourNow = now.get(Calendar.HOUR_OF_DAY)
            val minuteNow = now.get(Calendar.MINUTE)

            val dateNow = Calendar.getInstance()
            dateNow.set(Calendar.HOUR_OF_DAY, hourNow)
            dateNow.set(Calendar.MINUTE, minuteNow)
            dateNow.set(Calendar.SECOND, 0)

            val parts = reference.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val date2 = Calendar.getInstance()
            date2.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]))
            date2.set(Calendar.MINUTE, Integer.parseInt(parts[1]))
            date2.set(Calendar.SECOND, 0)

            return dateNow.before(date2) //текущей дата находится до сравниваемая даты
        }
    }

    private fun Context.vibrate(pattern: LongArray) {
        val vibrator = applicationContext.getSystemService(VIBRATOR_SERVICE) as Vibrator? ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }
}