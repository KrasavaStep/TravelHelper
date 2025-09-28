package com.example.travelhelper.ui.map

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yandex.mapkit.geometry.Point

class MainMapViewModel : ViewModel() {
    val placemarksData = MutableLiveData<List<Point>>()

    fun loadPlacemarks() {
        val points = listOf(
            Point(52.4221751,31.0167343),
            Point(52.4218218,31.0156507)
        )
        placemarksData.value = points
    }
}