package com.start.reminder

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import com.start.model.User


class DataController {

    private val liveData = MutableLiveData<ValueliveData>()
    val liveDataS = MutableLiveData<String>()
    val liveDataUser = Transformations.switchMap(liveDataS) { action -> getUser(action) }


    companion object {

        @Volatile
        private var INSTANCE: DataController? = null

        fun getInstance(): DataController {
            if (INSTANCE == null) {
                synchronized(DataController::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = DataController()
                    }
                }
            }
            return INSTANCE as DataController
        }
    }

    fun getLifeData(): MutableLiveData<ValueliveData> {
        return liveData
    }

    fun setValueInLifeData(newValue: ValueliveData) {
        liveData.postValue(newValue)
    }

    private fun getUser(action: String): MutableLiveData<User> {
        val mutableLiveData = MutableLiveData<User>()
        mutableLiveData.value = User(action, action)
        return mutableLiveData
    }

}


