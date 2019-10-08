package com.start.reminder

import android.app.*
import android.arch.lifecycle.Observer
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.start.receivers.AlarmReceiver
import com.start.receivers.MyRees
import com.start.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var cont: Context

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

        disable_vibro_checkbox_icon.isChecked = isVibro(this)
        disable_vibro_checkbox_icon.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                setVibro(this, true)
                initDisableVibro(disable_vibroTV.text.toString())
            } else {
                setVibro(this, false)
            }
        }

        checkbox_icon.isChecked = isSetIcon(this)
        checkbox_icon.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) setIcon(this, true) else setIcon(this, false)
        }

        if (restoreTime(this) == 0) {
            saveTime(this, Integer.parseInt(time!!.text.toString()))
        } else {
            time!!.text = restoreTime(this).toString()
        }

        if (isAlarm(this)) {
            repeatBtn!!.text = "Остановить уведомления"
        } else {
            repeatBtn!!.text = "Нет уведомлений"
        }

        seekBar.progress = time.text.toString().toInt()
        seekBar!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                var progr = progress
                if (progress == 0) {
                    progr = 1
                }
                time!!.text = progr.toString()
                saveTime(seekBar.context, progr)
                if (isAlarm(seekBar.context)) {
                    reInstallTimer()
                } else
                    Log.d("Package__", "set time")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        repeatBtn!!.setOnClickListener { v ->
            if (repeatBtn!!.text.toString() == "Нет уведомлений") {
                Toast.makeText(v.context, "Нет уведомлений", Toast.LENGTH_SHORT).show()
            } else {
                cancel(v)
            }
        }
        permissoinBtn!!.setOnClickListener {
            cancel(it)
            startActivity(Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

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
                    false, NotificationService.OTHER_NOTIFICATIONS_CODE)
            )
            AlarmReceiver.cancelAlarm(this)
        }

        fromTime.setOnClickListener { configureTimePicker(fromTV, "from") }
        toTime.setOnClickListener { configureTimePicker(toTV, "to") }
        disable_vibro_time_btn.setOnClickListener { configureTimePicker(disable_vibroTV, "vibro") }

        fromTV.text = restoreTimeFrom(this)
        toTV.text = restoreTimeTo(this)
        disable_vibroTV.text = restoreTimeVibro(this)

        checkbox_icon.setOnClickListener {
            restoreTimeFrom(this)
            restoreTimeTo(this)
        }
    }//onCreate

    private fun cancel(v: View) {
        AlarmReceiver.cancelAlarm(v.context)
        repeatBtn!!.text = "Нет уведомлений"
        changeInterceptedNotificationImage(NotificationService.OTHER_NOTIFICATIONS_CODE)
    }

    private fun reInstallTimer() {
        AlarmReceiver.cancelAlarm(this@MainActivity)
        AlarmReceiver.setAlarm(this@MainActivity)
        setAlarm(this, true)
        Log.d("Package__", "AlarmReceiver is reinstalled")
    }

    private fun configureTimePicker(text: TextView, savedTime: String) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val picker = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minut ->
            if (view.isShown) {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minut)
                calendar.set(Calendar.SECOND, 0)

                val mn = if (minut < 10) "0$minut" else minut.toString()
                val hr = if (hourOfDay < 10) "0$hourOfDay" else hourOfDay.toString()
                val hourString = "$hr:$mn"

                when (savedTime) {
                    "from" -> {
                        saveTimeFrom(this, hourString)
                        if (!checkTime(this)) {
                            AlarmReceiver.cancelAlarm(this@MainActivity)
                            DataController.getInstance().setValueInLifeData(
                                    ValueliveData(false, NotificationService.OTHER_NOTIFICATIONS_CODE)
                            )
                        }
                    }
                    "to" -> {
                        saveTimeTo(this, hourString)
                        if (!checkTime(this)) {
                            AlarmReceiver.cancelAlarm(this@MainActivity)
                            DataController.getInstance().setValueInLifeData(
                                    ValueliveData(false, NotificationService.OTHER_NOTIFICATIONS_CODE)
                            )
                        }
                    }
                    "vibro" -> saveTimeVibro(this, hourString)
                }

                text.text = hourString
            }
        }, hour, minute, DateFormat.is24HourFormat(this))
        picker.show()
    }

    private fun changeInterceptedNotificationImage(notificationCode: Int) {
        when (notificationCode) {
            NotificationService.VIBER_CODE -> intercepted_notification_logo.setImageResource(R.drawable.viber)
            NotificationService.WHATSAPP_CODE -> intercepted_notification_logo.setImageResource(R.drawable.whatsapp_logo)
            NotificationService.TELEGRAM_CODE -> intercepted_notification_logo.setImageResource(R.drawable.telegram)
            NotificationService.CALL_CODE -> intercepted_notification_logo.setImageResource(R.drawable.call)
            NotificationService.OTHER_NOTIFICATIONS_CODE -> intercepted_notification_logo.setImageResource(R.drawable.notif_icon_2_no)
        }
    }

    private fun setTextBtn(send: Boolean) {
        if (send) {
            repeatBtn!!.text = "Стоп"
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

    private fun initDisableVibro(timeString: String) {
        val timeInMillis = MyRees.time(timeString)
        val intent = Intent(this, MyRees::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        val data = Date(timeInMillis)
        Log.d("Package__", "$data")
    }

    companion object {
        private val ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners"
        private val ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"
    }
}


