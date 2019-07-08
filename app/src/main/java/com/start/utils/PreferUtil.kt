package com.start.utils

import android.content.Context
import android.preference.PreferenceManager
import android.util.Log

var TIME_KEY = "time"
var ALARM_CANCEL_KEY = "cancelAlarm"
var TIME_FROM_KEY = "time_from"
var TIME_TO_KEY = "time_to"
var SET_ICON_KEY = "set_icon"

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

fun saveTimeFrom(context: Context, value: String) {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    val editor = prefs.edit()
    editor.putString(TIME_FROM_KEY, value)
    editor.apply()
}

fun restoreTimeFrom(context: Context): String {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    return prefs.getString(TIME_FROM_KEY, "09:00")!!
}

fun saveTimeTo(context: Context, value: String) {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    val editor = prefs.edit()
    editor.putString(TIME_TO_KEY, value)
    editor.apply()
}

fun restoreTimeTo(context: Context): String {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    return prefs.getString(TIME_TO_KEY, "21:00")!!
}

fun canceledAlarm(context: Context, value: Boolean) {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    val editor = prefs.edit()
    editor.putBoolean(ALARM_CANCEL_KEY, value)
    editor.apply()
}

fun isCancelAlarm(context: Context): Boolean {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    val alarmUp = prefs.getBoolean(ALARM_CANCEL_KEY, false)
    if (alarmUp) {
        Log.d("Package__", "Alarm is already active")
    } else {
        Log.d("Package__", "Alarm is not active")
    }
    return alarmUp
}

fun setIcon(context: Context, value: Boolean) {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    val editor = prefs.edit()
    editor.putBoolean(SET_ICON_KEY, value)
    editor.apply()
}

fun isSetIcon(context: Context): Boolean {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    return prefs.getBoolean(SET_ICON_KEY, false)
}
