package com.start.reminder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.start.utils.isCanselAlarm

class NotificationListenerExampleService : NotificationListenerService() {
    private val notifId = 4

    companion object {
        const val PATH_SERVICE = "com.start.reminder.NotificationListenerExampleService"

        const val CALL_CODE = 1
        const val WHATSAPP_CODE = 2
        const val TELEGRAM_CODE = 4
        const val OTHER_NOTIFICATIONS_CODE = 6 // We ignore all notification with code == 4
    }

    private object ApplicationPackageNames {
        const val WHATSAPP_PACK_NAME = "com.whatsapp"
        const val TELEGRAM_PACK_NAME = "org.telegram.messenger"
        const val TELEGRAM_X_PACK_NAME = "org.thunderdog.challegram"
        const val CALL_PACK_NAME = "com.android.phone"
//        const val CALL_PACK_NAME = "com.android.incallui"
//        const val CALL_8_PACK_NAME = "com.google.android.dialer"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0)

        val builder = Notification.Builder(applicationContext)
        builder.setContentTitle("Reminder ON")
        builder.setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.notif_icon_2_small_1))
        builder.setSmallIcon(R.drawable.notif_icon_2)
        builder.setWhen(System.currentTimeMillis())
        builder.setContentIntent(pendingIntent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("id")
        }
        startForeground(notifId, builder.build())
        Log.d("Package__", "OnCreate")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "name"
            val description = "descr"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("id", name, importance)
            channel.description = description
            val notificationManager = getSystemService<NotificationManager>(NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(channel)
        }
    }

    private fun removeNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        notificationManager?.cancel(notifId)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val i = super.onStartCommand(intent, flags, startId)
        Log.d("Package__", "onStartCommand $i")
        return i
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d("Package__", "onBind ")
        DataController.getInstance().setValueInLifeDataS("onBind")
        return super.onBind(intent)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val notificationCode = matchNotificationCode(sbn, 0)

        if (notificationCode != OTHER_NOTIFICATIONS_CODE) {
            sendBroadcastInMain(notificationCode, true)
            if (!isCanselAlarm(this)) {//if alarm already started
                Alarm.setAlarm(this)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        val notificationCode = matchNotificationCode(sbn, 1)
        if (notificationCode != OTHER_NOTIFICATIONS_CODE) {
            Alarm.cancelAlarm(this)
            sendBroadcastInMain(OTHER_NOTIFICATIONS_CODE, false)
        }
    }

    private fun sendBroadcastInMain(notificationCode: Int, send: Boolean) {
        DataController.getInstance().setValueInLifeData(ValueliveData(send, notificationCode))
    }

    private fun matchNotificationCode(sbn: StatusBarNotification, action: Int): Int {
        val packageName = sbn.packageName
        if (action == 1) {
            Log.d("Package__", "Removed = $packageName")
        } else {
            Log.d("Package__", "Posted = $packageName")
        }

        return when (packageName) {
            ApplicationPackageNames.WHATSAPP_PACK_NAME -> WHATSAPP_CODE
            ApplicationPackageNames.TELEGRAM_PACK_NAME,
            ApplicationPackageNames.TELEGRAM_X_PACK_NAME -> TELEGRAM_CODE
//            ApplicationPackageNames.CALL_8_PACK_NAME,
            ApplicationPackageNames.CALL_PACK_NAME -> CALL_CODE
            else -> OTHER_NOTIFICATIONS_CODE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Package__", "onDestroy")
        removeNotification()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("Package__", "onListenerConnected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d("Package__", "onListenerDisconnected")
    }

    override fun onRebind(intent: Intent) {
        super.onRebind(intent)
        Log.d("Package__", "onRebind")
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.d("Package__", "onUnbind")
        DataController.getInstance().setValueInLifeDataS("onUnbind")
        return super.onUnbind(intent)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.d("Package__", "onLowMemory")
    }

}
