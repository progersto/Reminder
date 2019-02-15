package com.start.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import android.media.AudioManager
import android.media.MediaPlayer
import android.os.PowerManager
import android.util.Log
import android.widget.Toast
import com.start.utils.canceledAlarm
import com.start.utils.restoreTime
import java.io.IOException

class Alarm : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, javaClass.name)
        wakeLock.acquire()

        Log.d("Package__", "onReceive alarm")
        Toast.makeText(context, "Alarm !!!!!!!!!!", Toast.LENGTH_LONG).show() // For example
        val mediaPlayer = MediaPlayer()
        try {
            val afd = context.assets.openFd("win" + ".mp3")
            mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mediaPlayer.prepare()
            mediaPlayer.start()
            Log.d("Package__", "onReceive play")
        } catch (e: IOException) {
            Log.e("Package__", "error ${e.message}")
            e.printStackTrace()
        }
        wakeLock.release()
    }

    companion object {

        fun setAlarm(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, Alarm::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), (1000 * 60 * restoreTime(context)).toLong(), pendingIntent) // Millisec * Second * Minute
            canceledAlarm(context, true)
            Log.d("Package__", "setAlarm " + restoreTime(context))
        }

        fun cancelAlarm(context: Context) {
            val intent = Intent(context, Alarm::class.java)
            val sender = PendingIntent.getBroadcast(context, 0, intent, 0)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(sender)
            canceledAlarm(context, false)
            Log.d("Package__", "cancelAlarm")
        }
    }


}