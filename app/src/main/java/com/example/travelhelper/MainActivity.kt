package com.example.travelhelper

// region Импорты
import com.example.travelhelper.ui.SearchAdapter
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
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
import com.example.travelhelper.utils.Utils.saveToPrefs
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named
// endregion

class MainActivity : AppCompatActivity() {

    // region Существующие переменные
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val locationPermissionRequestCode = 1000
    private val appBarViewModel: AppBarViewModel by viewModels()
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    // endregion

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

        // 1. Вызываем настройку для RecyclerView
        setupSearchRecyclerView()

        // 2. Подписываемся на результаты поиска из ViewModel
        lifecycleScope.launch {
            searchViewModel.searchResults.collect { attractions ->
                // Передаем новый список в адаптер
                searchAdapter.submitList(attractions)

                // Управляем видимостью списка: показываем, только если есть текст и результаты
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
        // Находим RecyclerView из разметки app_bar_main.xml по его ID
        searchResultsRecycler = binding.appBarMain.searchResultsRecycler

        // Создаем адаптер. Клик по элементу пока просто показывает Toast.
        searchAdapter = SearchAdapter { attraction ->
            Toast.makeText(this, "Нажатие на: ${attraction.name}", Toast.LENGTH_SHORT).show()
        }

        // Присваиваем адаптер и LayoutManager нашему RecyclerView
        searchResultsRecycler.adapter = searchAdapter
        searchResultsRecycler.layoutManager = LinearLayoutManager(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_activity, menu)
        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView
        searchView.queryHint = "Поиск по достопримечательностям..."

        // Слушатель для управления видимостью списка при закрытии поиска (крестик или кнопка "назад")
        searchItem.setOnActionExpandListener(object : android.view.MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: android.view.MenuItem): Boolean = true
            override fun onMenuItemActionCollapse(item: android.view.MenuItem): Boolean {
                searchResultsRecycler.visibility = View.GONE // Прячем список
                return true
            }
        })

        // Слушатель для обработки ввода текста
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            // Вызывается при нажатии "Enter" на клавиатуре
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchViewModel.performSearch(query.orEmpty())
                searchView.clearFocus() // Прячем клавиатуру
                return true
            }

            // Вызывается при каждом изменении текста в строке поиска
            override fun onQueryTextChange(newText: String?): Boolean {
                searchViewModel.performSearch(newText.orEmpty())
                return true
            }
        })

        updateMenuVisibility(menu)
        return true
    }

    // region Существующие методы
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

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun getCurrentLocation() {
        Log.d("ROUTE_EX", "getCurrentLoc ")
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestLocationPermissions()
                    return
                }
                try {
                    fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                        if (task.isSuccessful && task.result != null) {
                            val location = task.result
                            Log.d("ROUTE_EX", "loc " + location.latitude.toString() + location.longitude.toString())
                            applicationContext.saveToPrefs("lat", location.latitude.toFloat())
                            applicationContext.saveToPrefs("lon", location.longitude.toFloat())
                        } else {
                            Log.w("ROUTE_EX", "Не удалось получить последнее известное местоположение.", task.exception)
                            Toast.makeText(this, "Не удалось определить ваше местоположение", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: SecurityException) {
                    Log.e("ROUTE_EX", "SecurityException при получении местоположения.", e)
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

    private fun checkPermissions(): Boolean = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun requestLocationPermissions() {
        Log.d("ROUTE_EX", "requestPermission")
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            locationPermissionRequestCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Toast.makeText(this, "Разрешение на геолокацию не было предоставлено", Toast.LENGTH_SHORT).show()
            }
        }
    }
    // endregion
}
