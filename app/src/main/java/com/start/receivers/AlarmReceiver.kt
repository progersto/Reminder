package com.start.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.Intent
import android.media.AudioManager

import android.media.MediaPlayer
import android.os.*
import android.util.Log
import android.widget.Toast
import com.start.reminder.BestService
import com.start.reminder.MainViewModel
import com.start.reminder.UIObject
import com.start.utils.*
import java.io.IOException

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, javaClass.name)
        wakeLock.acquire()

        if (!checkTime(context)) {
            cancelAlarm(context)
            MainViewModel.getInstance().setValueInLifeData(
                    UIObject(false, BestService.OTHER_NOTIFICATIONS_CODE)
            )
            return
        }

        Toast.makeText(context, "Посмотрите уведомления !!!!!!!!!!", Toast.LENGTH_LONG).show()

        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            when (audioManager.ringerMode) {
                AudioManager.RINGER_MODE_NORMAL -> {
                    context.melody()
                    context.vibrate(longArrayOf(0, 400, 200, 500))
                }
                AudioManager.RINGER_MODE_SILENT -> {
                }
                AudioManager.RINGER_MODE_VIBRATE -> {
                    context.vibrate(longArrayOf(0, 400, 200, 500))
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        wakeLock.release()
    }


    companion object {
        fun setAlarm(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                    (1000 * 60 * restoreTime(context)).toLong(),
                    pendingIntent
            )
            Log.d("Package__", "setAlarm " + restoreTime(context))
        }

        fun cancelAlarm(context: Context) {
            val intent = Intent(context, AlarmReceiver::class.java)
//            val sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val sender = PendingIntent.getBroadcast(context, 0, intent, 0)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(sender)
            setAlarm(context, false)
            Log.d("Package__", "cancelAlarm")
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

    private fun Context.melody() {
        val mediaPlayer = MediaPlayer()
        val descriptor = applicationContext.assets.openFd("win" + ".mp3")
        mediaPlayer.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
        descriptor.close()
        mediaPlayer.prepare()
        mediaPlayer.start()
    }
}