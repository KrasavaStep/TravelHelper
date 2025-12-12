package com.example.travelhelper

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.activity.viewModels
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import com.example.travelhelper.databinding.ActivityMainBinding
import com.example.travelhelper.utils.AppBarViewModel
import com.yandex.mapkit.MapKitFactory
import kotlin.getValue
import androidx.core.view.size
import androidx.core.view.get
import com.example.travelhelper.utils.LocationService
import com.example.travelhelper.utils.Utils.saveToPrefs
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.FusedOrientationProviderClient
import com.google.android.gms.location.LocationServices
import okhttp3.internal.http2.Settings

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private val locationPermissionRequestCode = 1000

    private val appBarViewModel: AppBarViewModel by viewModels()

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    interface MenuConfig {
        fun shouldShowMenuItems(menu: Menu): Boolean
    }

    private var currentMenuConfig: MenuConfig? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        supportFragmentManager.addOnBackStackChangedListener {
            val fragment = supportFragmentManager.findFragmentById(R.id.map_fragment)

        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_main_map, R.id.nav_liked_places, R.id.nav_settings, R.id.nav_about_app
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        getCurrentLocation()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
       /* menuInflater.inflate(R.menu.main_activity, menu)
        val menuItem = menu.findItem(R.id.action_search)
        val searchView = menuItem.actionView as SearchView
        updateMenuVisibility(menu)
        searchView.setOnQueryTextListener( object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false;
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false;
            }
        })
        return true*/
        return false
    }

    private fun updateMenuVisibility(menu: Menu) {
        currentMenuConfig?.shouldShowMenuItems(menu)?.let { shouldShow ->
            for (i in 0 until menu.size) {
                menu[i].isVisible = shouldShow
            }
        }
    }

    fun setMenuConfig(config: MenuConfig?) {
        currentMenuConfig = config
        invalidateOptionsMenu()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        //stopLocationService()
        super.onDestroy()
    }

    /*private fun startLocationService() {
        val intent = Intent(this, LocationService::class.java).apply {
            action = LocationService.ACTION_START
        }

        startForegroundService(intent)
    }*/

    /*private fun stopLocationService() {
        val intent = Intent(this, LocationService::class.java).apply {
            action = LocationService.ACTION_STOP
        }
        startService(intent)
    }*/

    private fun getCurrentLocation() {
        Log.d("ROUTE_EX",  "getCurrentLoc ")
        if (checkPermissions()) {
            Log.d("ROUTE_EX",  "if checkPerm = true ")
            if (isLocationEnabled()) {
                Log.d("ROUTE_EX",  "if isLocEnabl = true  ")
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestLocationPermissions()
                    return
                }
                try {
                    fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                        // Проверяем, что задача выполнилась успешно И что результат (location) не null
                        if (task.isSuccessful && task.result != null) {
                            val location = task.result
                            // Теперь мы внутри блока, где location гарантированно не null
                            Log.d("ROUTE_EX",  "loc " + location.latitude.toString() + location.longitude.toString())
                            applicationContext.saveToPrefs("lat", location.latitude.toFloat())
                            applicationContext.saveToPrefs("lon", location.longitude.toFloat())
                        } else {
                            // Этот блок выполнится, если местоположение получить не удалось
                            Log.w("ROUTE_EX", "Не удалось получить последнее известное местоположение.", task.exception)
                            Toast.makeText(this, "Не удалось определить ваше местоположение", Toast.LENGTH_SHORT).show()
                        }
                    }

                } catch (e: Exception) {

                }


            } else {
                Toast.makeText(this, "Включите GPS", Toast.LENGTH_LONG).show()
                val intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }

        } else {
            requestLocationPermissions()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun checkPermissions(): Boolean = ActivityCompat.checkSelfPermission(
        this,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
        this,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED


    private fun requestLocationPermissions() {
        Log.d("ROUTE_EX",  "requestPermission")
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            locationPermissionRequestCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)

        if (requestCode == locationPermissionRequestCode) {
            Log.d("ROUTE_EX",  "requestPermission + 1")
            getCurrentLocation()
        }

    }


}