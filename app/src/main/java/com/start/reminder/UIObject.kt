package com.start.reminder

class UIObject(private var action: Boolean, private var notificationCode: Int) {

    fun getAction(): Boolean {
        return action
    }

    fun getNotificationCode(): Int {
        return notificationCode
    }

}