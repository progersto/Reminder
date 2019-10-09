package com.start.reminder

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

class MainViewModel: ViewModel() {

    private val uiObj = MutableLiveData<UIObject>()
    private val permission = MutableLiveData<String>()

    companion object {

        @Volatile
        private var INSTANCE: MainViewModel? = null

        fun getInstance(): MainViewModel {
            if (INSTANCE == null) {
                synchronized(MainViewModel::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = MainViewModel()
                    }
                }
            }
            return INSTANCE as MainViewModel
        }
    }

    fun getLifeData(): MutableLiveData<UIObject> {
        return uiObj
    }

    fun checkPermission(): MutableLiveData<String> {
        return permission
    }

    fun setValueInLifeData(newValue: UIObject) {
        uiObj.postValue(newValue)
    }

    fun setValueInLifeDataS(newValue: String) {
        permission.postValue(newValue)
    }
}


