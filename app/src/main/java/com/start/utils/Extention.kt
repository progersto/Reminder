package com.start.utils

import android.content.Context
import android.util.Log
import java.util.*

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

fun checkTime(context: Context): Boolean {
    val timeF = restoreTimeFrom(context)
    val timeFrom = compareDate(timeF)
    val timeT = restoreTimeTo(context)
    val timeTo = compareDate(timeT)
    if (!timeFrom && timeTo) {
        Log.d("Package__", "checkTime = true")
    } else {
        Log.d("Package__", "checkTime = false")
    }
    return !timeFrom && timeTo
}