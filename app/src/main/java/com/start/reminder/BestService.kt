package com.start.reminder

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.start.receivers.AlarmReceiver
import com.start.reminder.BestService.PackageNames.CALL_PACK_NAME
import com.start.reminder.BestService.PackageNames.TELEGRAM_PACK_NAME
import com.start.reminder.BestService.PackageNames.TELEGRAM_X_PACK_NAME
import com.start.reminder.BestService.PackageNames.VIBER_PACK_NAME
import com.start.reminder.BestService.PackageNames.WHATSAPP_PACK_NAME
import com.start.utils.*

class BestService : AccessibilityService() {

    private val notifId = 4


    companion object {
        const val CALL_CODE = 1
        const val WHATSAPP_CODE = 2
        const val VIBER_CODE = 3
        const val TELEGRAM_CODE = 4
        const val OTHER_NOTIFICATIONS_CODE = 6
        var handler: Handler? = null
    }

    private object PackageNames {
        const val VIBER_PACK_NAME = "com.viber.voip"
        const val WHATSAPP_PACK_NAME = "com.whatsapp"
        const val TELEGRAM_PACK_NAME = "org.telegram.messenger"
        const val TELEGRAM_X_PACK_NAME = "org.thunderdog.challegram"
        const val CALL_PACK_NAME = "com.android.phone"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0)

        val builder = Notification.Builder(applicationContext)
        builder.setContentTitle("Reminder")
        builder.setContentText("Включен")
        builder.setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.notif_icon_2_small_1))
        builder.setSmallIcon(R.drawable.notif_icon_2)
        builder.setWhen(System.currentTimeMillis())
        builder.setContentIntent(pendingIntent)
        if (isSetIcon(this)) {
            builder.setPriority(Notification.PRIORITY_MIN)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("id")
        }
        startForeground(notifId, builder.build())

        handler = Handler()

        Log.d("Package__", "OnCreate")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "name"
            val description = "descr"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("id", name, importance)
            channel.description = description
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(channel)
        }
    }

    private fun removeNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        notificationManager?.cancel(notifId)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            Log.d("Package__", "onAccessibilityEvent ")
            val text = event.text.map { it.toString() }.toString()

            when (event.packageName) {
                VIBER_PACK_NAME,
                WHATSAPP_PACK_NAME,
                TELEGRAM_PACK_NAME,
                TELEGRAM_X_PACK_NAME,
                CALL_PACK_NAME -> {
                    Log.d("Package__", "text.length = ${text.length}")
                    if (text.length > 2) {
                        Log.d("Package__", "text = $text, packageName = ${event.packageName}")
                        startReminder(event)
                    }
                }
                else -> Log.d("Package__", "text = $text, packageName = ${event.packageName}")
            }
        }
    }

    private fun startReminder(accessibilityEvent: AccessibilityEvent) {
        val notificationCode = matchNotificationCode(accessibilityEvent, 0)

        if (MainViewModel.getInstance().getLifeData().value == null) {
            AlarmReceiver.cancelAlarm(this)
            sendBroadcastInMain(OTHER_NOTIFICATIONS_CODE, false)
        }

        if (notificationCode != OTHER_NOTIFICATIONS_CODE && !isAlarm(this) && checkTime(this)) {
            handler?.also {
                if (!it.hasMessages(0)) {
                    Log.d("Package__", "handler init ")
                    setAlarm(this, true)
                    it.postDelayed({
                        Log.d("Package__", "set in handler ")
                        sendBroadcastInMain(notificationCode, true)
                        AlarmReceiver.setAlarm(this)
                    }, (1000 * 60 * restoreTime(baseContext)).toLong())
                }
            }
        }
    }

    override fun onServiceConnected() {
        Log.d("Package__", "onServiceConnected ")
        MainViewModel.getInstance().setValueInLifeDataS("onBind")
//        Toast.makeText(applicationContext, "START", Toast.LENGTH_LONG).show()
        val events = listOf(AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED)

//        val flags = listOf(AccessibilityServiceInfo.DEFAULT,
//                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS,
//                AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS,
//                AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE)

        val info = AccessibilityServiceInfo()
        info.eventTypes = events.reduce { reduced, value -> reduced or value }
//        info.flags = flags.reduce { reduced, value -> reduced or value }
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        info.packageNames = arrayOf( VIBER_PACK_NAME, WHATSAPP_PACK_NAME, TELEGRAM_PACK_NAME,
                TELEGRAM_X_PACK_NAME, CALL_PACK_NAME)
        info.notificationTimeout = 100

        serviceInfo = info

    }

    override fun onInterrupt() {
        Log.d("SSSSSS", "onInterrupt")
    }


    private fun sendBroadcastInMain(notificationCode: Int, send: Boolean) {
        MainViewModel.getInstance().setValueInLifeData(UIObject(send, notificationCode))
    }

    private fun matchNotificationCode(sbn: AccessibilityEvent, action: Int): Int {
        val packageName = sbn.packageName
        if (action == 1) {
            Log.d("Package__", "Removed = $packageName")
        } else {
            Log.d("Package__", "Posted = $packageName")
        }

        return when (packageName) {
            VIBER_PACK_NAME -> VIBER_CODE
            WHATSAPP_PACK_NAME -> WHATSAPP_CODE
            TELEGRAM_PACK_NAME,
            TELEGRAM_X_PACK_NAME -> TELEGRAM_CODE
            CALL_PACK_NAME -> CALL_CODE
            else -> OTHER_NOTIFICATIONS_CODE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Package__", "onDestroy")
        removeNotification()
    }

    override fun onRebind(intent: Intent) {
        super.onRebind(intent)
        Log.d("Package__", "onRebind")
    }


    override fun onUnbind(intent: Intent): Boolean {
        Log.d("Package__", "onUnbind")

        AlarmReceiver.cancelAlarm(this)
        sendBroadcastInMain(OTHER_NOTIFICATIONS_CODE, false)

        MainViewModel.getInstance().setValueInLifeDataS("onUnbind")
        return super.onUnbind(intent)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.d("Package__", "onLowMemory")
    }

}
