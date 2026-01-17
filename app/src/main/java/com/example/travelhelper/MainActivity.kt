package com.example.travelhelper

import com.example.travelhelper.ui.map.SearchAdapter
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.travelhelper.databinding.ActivityMainBinding
import com.example.travelhelper.utils.AppBarViewModel
import com.example.travelhelper.utils.SharedViewModel
import com.example.travelhelper.utils.Utils.saveToPrefs
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val locationPermissionRequestCode = 1000
    private val appBarViewModel: AppBarViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by viewModels()
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val searchViewModel by viewModel<SearchViewModel>(named("searchViewModel"))
    private lateinit var searchView: SearchView
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var searchResultsRecycler: RecyclerView

    interface MenuConfig {
        fun shouldShowMenuItems(menu: Menu): Boolean
    }

    private var currentMenuConfig: MenuConfig? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_main_map, R.id.nav_liked_places, R.id.nav_settings, R.id.nav_about_app),
            drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        setupSearchRecyclerView()

        lifecycleScope.launch {
            searchViewModel.searchResults.collect { attractions ->
                searchAdapter.submitList(attractions)
                if (::searchView.isInitialized && searchView.query.isNotEmpty() && attractions.isNotEmpty()) {
                    searchResultsRecycler.visibility = View.VISIBLE
                } else {
                    searchResultsRecycler.visibility = View.GONE
                }
            }
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        getCurrentLocation()
    }

    private fun setupSearchRecyclerView() {
        searchResultsRecycler = binding.appBarMain.searchResultsRecycler
        searchAdapter = SearchAdapter { attraction ->
            // Вместо Toast отправляем результат в SharedViewModel
            sharedViewModel.selectAttraction(attraction)
            searchResultsRecycler.visibility = View.GONE
            searchView.onActionViewCollapsed()
        }
        searchResultsRecycler.adapter = searchAdapter
        searchResultsRecycler.layoutManager = LinearLayoutManager(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_activity, menu)
        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem?.actionView as SearchView
        searchView.queryHint = "Поиск по достопримечательностям..."

        searchItem.setOnActionExpandListener(object : android.view.MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: android.view.MenuItem): Boolean = true
            override fun onMenuItemActionCollapse(item: android.view.MenuItem): Boolean {
                searchResultsRecycler.visibility = View.GONE
                return true
            }
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchViewModel.performSearch(query.orEmpty())
                searchView.clearFocus()
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                searchViewModel.performSearch(newText.orEmpty())
                return true
            }
        })

        updateMenuVisibility(menu)
        return true
    }

    private fun updateMenuVisibility(menu: Menu) {
        currentMenuConfig?.shouldShowMenuItems(menu)?.let { shouldShow ->
            val searchItem = menu.findItem(R.id.action_search)
            searchItem?.isVisible = shouldShow
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

    private fun getCurrentLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                        if (task.isSuccessful && task.result != null) {
                            val location = task.result
                            applicationContext.saveToPrefs("lat", location.latitude.toFloat())
                            applicationContext.saveToPrefs("lon", location.longitude.toFloat())
                        }
                    }
                }
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun checkPermissions(): Boolean = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            locationPermissionRequestCode
        )
    }
}
