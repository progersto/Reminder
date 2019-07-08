package com.start.reminder

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

class DataController: ViewModel() {

    private val liveData = MutableLiveData<ValueLiveData>()
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

    fun getLifeData(): MutableLiveData<ValueLiveData> {
        return liveData
    }

    fun getLifeDataS(): MutableLiveData<String> {
        return liveDataS
    }

    fun setValueInLifeData(newValue: ValueLiveData) {
        liveData.postValue(newValue)
    }

    fun setValueInLifeDataS(newValue: String) {
        liveDataS.postValue(newValue)
    }
}


