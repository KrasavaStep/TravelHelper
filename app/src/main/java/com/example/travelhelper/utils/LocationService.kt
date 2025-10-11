package com.example.travelhelper.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.travelhelper.utils.Utils.saveToPrefs
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null

    companion object {
        const val ACTION_START = "START_LOCATION_TRACKING"
        const val ACTION_STOP = "STOP_LOCATION_TRACKING"
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startLocationTracking()
            ACTION_STOP -> stopLocationTracking()
        }
        return START_STICKY
    }

    @SuppressLint("ForegroundServiceType")
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun startLocationTracking() {
        // Создаем уведомление для foreground service
        startForeground(1, createNotification())

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000
        ).apply {
            setMinUpdateIntervalMillis(5000)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.also { location ->
                    // Отправляем местоположение через Broadcast
//                    val broadcastIntent = Intent("LOCATION_UPDATE")
//                    broadcastIntent.putExtra("latitude", location.latitude)
//                    broadcastIntent.putExtra("longitude", location.longitude)
//                    sendBroadcast(broadcastIntent)
                    val latitude = location.latitude
                    val longitude = location.longitude
                    applicationContext.saveToPrefs("lat", latitude.toFloat())
                    applicationContext.saveToPrefs("lon", longitude.toFloat())

                    // Или обновляем через LiveData/Flow
                    //LocationLiveData.updateLocation(location)
                }
            }
        }

        if (checkPermissions()) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
        }
    }

    private fun createNotification(): Notification {
        val channelId = "location_channel"
        val channel = NotificationChannel(
            channelId,
            "Location Tracking",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Отслеживание местоположения")
            .setContentText("Активное отслеживание местоположения")
            .build()
    }

    private fun stopLocationTracking() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        stopSelf()
    }

    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onBind(intent: Intent?): IBinder? = null
}