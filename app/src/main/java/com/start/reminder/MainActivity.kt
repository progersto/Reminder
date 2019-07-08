package com.start.reminder

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.arch.lifecycle.Observer
import android.content.ComponentName
import android.content.Intent
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.text.format.DateFormat
import android.util.Log
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.start.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val isNotificationServiceEnabled: Boolean
        get() {
            val pkgName = packageName
            val flat = Settings.Secure.getString(contentResolver,
                    ENABLED_NOTIFICATION_LISTENERS)
            if (!TextUtils.isEmpty(flat)) {
                val names = flat.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (name in names) {
                    val cn = ComponentName.unflattenFromString(name)
                    if (cn != null) {
                        if (TextUtils.equals(pkgName, cn.packageName)) {
                            return true
                        }
                    }
                }
            }
            return false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkbox_icon.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                setIcon(this, true)
            }else{
                setIcon(this, false)
            }
        }

        if (restoreTime(this) == 0) {
            saveTime(this, Integer.parseInt(time!!.text.toString()))
        } else {
            time!!.text = restoreTime(this).toString()
        }

        if (isCancelAlarm(this)) {
            repeatBtn!!.text = "Остановить уведомления"
        } else {
            repeatBtn!!.text = "Нет уведомлений"
        }

        seekBar!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                var progr = progress
                if (progress == 0) {
                    progr = 1
                }
                time!!.text = progr.toString()
                saveTime(seekBar.context, progr)
                if (isCancelAlarm(seekBar.context)) {
                    Alarm.cancelAlarm(this@MainActivity)
                    Alarm.setAlarm(this@MainActivity)
                    Log.d("Package__", "Alarm is reinstalled")
                } else
                    Log.d("Package__", "set time")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        repeatBtn!!.setOnClickListener { v ->
            if (repeatBtn!!.text.toString() == "Нет уведомлений") {
                Toast.makeText(v.context, "Нет уведомлений", Toast.LENGTH_SHORT).show()
            } else {
                Alarm.cancelAlarm(v.context)
                repeatBtn!!.text = "Нет уведомлений"
                changeInterceptedNotificationImage(NotificationListenerExampleService.OTHER_NOTIFICATIONS_CODE)
            }
        }
        permissoinBtn!!.setOnClickListener { startActivity(Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS)) }

        if (!isNotificationServiceEnabled) {
            permissoinBtn!!.text = "нет разрешений"
            buildNotificationServiceAlertDialog().show()
        } else {
            permissoinBtn!!.text = "есть разрешения"
        }

        val liveData = DataController.getInstance().getLifeData()
        liveData.observe(this, Observer { value ->
            Log.d("Package__", "liveData Observe")
            setTextBtn(value!!.getAction())//stop notification text
            changeInterceptedNotificationImage(value.getNotificationCode())
        })
        val liveDataS = DataController.getInstance().getLifeDataS()
        liveDataS.observe(this, Observer { s -> setTextPermission(s!!) })

        if (DataController.getInstance().getLifeData().value == null) {
            DataController.getInstance().setValueInLifeData(ValueliveData(
                    false, NotificationListenerExampleService.OTHER_NOTIFICATIONS_CODE)
            )
            Alarm.cancelAlarm(this)
        }

        fromTime!!.setOnClickListener { configureTimePicker(fromTV, true) }
        toTime!!.setOnClickListener { configureTimePicker(toTV, false) }

        fromTV.text = restoreTimeFrom(this)
        toTV.text = restoreTimeTo(this)

        checkbox_icon.setOnClickListener {
            restoreTimeFrom(this)
            restoreTimeTo(this)
        }
    }//onCreate

    private fun configureTimePicker(text: TextView, fromOrTo: Boolean) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val picker = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minut ->

            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minut)
            calendar.set(Calendar.SECOND, 0)

            val mn = if (minut < 10) "0$minut" else minut.toString()
            val hr = if (hourOfDay < 10) "0$hourOfDay" else hourOfDay.toString()
            val hourString = "$hr:$mn"

            if (fromOrTo) {
                saveTimeFrom(this, hourString)
            } else {
                saveTimeTo(this, hourString)
            }

            text.text = hourString
        }, hour, minute, DateFormat.is24HourFormat(this))
        picker.show()
    }

    private fun changeInterceptedNotificationImage(notificationCode: Int) {
        when (notificationCode) {
            NotificationListenerExampleService.VIBER_CODE -> intercepted_notification_logo.setImageResource(R.drawable.viber)
            NotificationListenerExampleService.WHATSAPP_CODE -> intercepted_notification_logo.setImageResource(R.drawable.whatsapp_logo)
            NotificationListenerExampleService.TELEGRAM_CODE -> intercepted_notification_logo.setImageResource(R.drawable.telegram)
            NotificationListenerExampleService.CALL_CODE -> intercepted_notification_logo.setImageResource(R.drawable.call)
            NotificationListenerExampleService.OTHER_NOTIFICATIONS_CODE -> intercepted_notification_logo.setImageResource(R.drawable.notif_icon_2_no)
        }
    }

    private fun setTextBtn(send: Boolean) {
        if (send) {
            repeatBtn!!.text = "Остановить уведомления"
        } else {
            repeatBtn!!.text = "Нет уведомлений"
        }
    }

    private fun setTextPermission(s: String) {
        when (s) {
            "onBind" -> permissoinBtn!!.text = "есть разрешения"
            "onUnbind" -> permissoinBtn!!.text = "нет разрешений"
        }
    }

    private fun buildNotificationServiceAlertDialog(): AlertDialog {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(R.string.notification_listener_service)
        alertDialogBuilder.setMessage(R.string.notification_listener_service_explanation)
        alertDialogBuilder.setPositiveButton(R.string.yes
        ) { _, _ -> startActivity(Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
        alertDialogBuilder.setNegativeButton(R.string.no) { _, _ -> Log.d("Package__", "NegativeButton") }
        return alertDialogBuilder.create()
    }

    companion object {

        private val ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners"
        private val ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"
    }
}


