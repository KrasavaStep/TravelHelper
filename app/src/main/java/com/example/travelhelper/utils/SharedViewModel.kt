package com.example.travelhelper.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.travelhelper.data.data_model.AttractionModel

class SharedViewModel: ViewModel() {

    private val _dialogResult = MutableLiveData<Array<Double>>()
    val dialogResult: LiveData<Array<Double>> = _dialogResult

    private val _selectedAttraction = MutableLiveData<AttractionModel>()
    val selectedAttraction: LiveData<AttractionModel> = _selectedAttraction

    fun setDialogResult(result: Array<Double>) {
        _dialogResult.value = result
    }

    fun selectAttraction(attraction: AttractionModel) {
        _selectedAttraction.value = attraction
    }

}