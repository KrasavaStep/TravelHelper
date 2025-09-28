package com.example.travelhelper.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AppBarViewModel : ViewModel() {
    private val _query = MutableLiveData<String>()
    val query: LiveData<String> = _query
}