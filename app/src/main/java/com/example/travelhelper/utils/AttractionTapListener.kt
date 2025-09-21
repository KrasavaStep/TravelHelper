package com.example.travelhelper.utils

import com.example.travelhelper.views.MainMapFragment
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.MapObject
import com.yandex.mapkit.map.MapObjectTapListener
import java.lang.ref.WeakReference

class AttractionTapListener(fragment: MainMapFragment): MapObjectTapListener {

    private val weakFragment = WeakReference(fragment)

    override fun onMapObjectTap(mapObject: MapObject, point: Point): Boolean {
        val fragment = weakFragment.get() ?: return false



        return true
    }
}