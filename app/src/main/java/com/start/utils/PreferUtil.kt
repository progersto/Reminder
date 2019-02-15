package com.start.utils

import android.content.Context
import android.preference.PreferenceManager
import android.util.Log

var TIME_KEY = "time"
var ALARM_CANCEL_KEY = "cancelAlarm"


fun saveTime(context: Context, value: Int) {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    val editor = prefs.edit()
    editor.putInt(TIME_KEY, value)
    editor.apply()
}

fun restoreTime(context: Context): Int {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    return prefs.getInt(TIME_KEY, 0)
}

fun canceledAlarm(context: Context, value: Boolean) {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    val editor = prefs.edit()
    editor.putBoolean(ALARM_CANCEL_KEY, value)
    editor.apply()
}

fun isCanselAlarm(context: Context): Boolean {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    val alarmUp = prefs.getBoolean(ALARM_CANCEL_KEY, false)
    if (alarmUp) {
        Log.d("Package__", "Alarm is already active")
    } else {
        Log.d("Package__", "Alarm is not active")
    }
    return alarmUp
}