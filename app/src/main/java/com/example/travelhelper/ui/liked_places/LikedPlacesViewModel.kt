package com.example.travelhelper.ui.liked_places

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LikedPlacesViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is liked_places Fragment"
    }
    val text: LiveData<String> = _text
}