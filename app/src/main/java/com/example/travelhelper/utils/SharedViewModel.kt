package com.example.travelhelper.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel: ViewModel() {

    private val _dialogResult = MutableLiveData<Array<Double>>()
    val dialogResult: LiveData<Array<Double>> = _dialogResult

    fun setDialogResult(result: Array<Double>) {
        _dialogResult.value = result
    }

}