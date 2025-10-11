package com.example.travelhelper.utils

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object LocationLiveData {
    private val _location = MutableLiveData<Location>()
    val location: LiveData<Location> = _location

    fun updateLocation(newLocation: Location) {
        _location.postValue(newLocation)
    }

}