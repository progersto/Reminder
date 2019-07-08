package com.start.reminder

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

class DataController: ViewModel() {

    private val liveData = MutableLiveData<ValueliveData>()
    private val liveDataS = MutableLiveData<String>()

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

    fun getLifeDataS(): MutableLiveData<String> {
        return liveDataS
    }

    fun setValueInLifeData(newValue: ValueliveData) {
        liveData.postValue(newValue)
    }

    fun setValueInLifeDataS(newValue: String) {
        liveDataS.postValue(newValue)
    }
}


