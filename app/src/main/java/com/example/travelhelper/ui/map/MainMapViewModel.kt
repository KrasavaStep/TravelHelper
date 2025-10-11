package com.example.travelhelper.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelhelper.data.network.overpass_api.OSMPlace
import com.example.travelhelper.data.network.overpass_api.OSMRepository
import com.example.travelhelper.data.network.routes_api.LatLng
import com.example.travelhelper.data.network.routes_api.Route
import com.example.travelhelper.data.network.routes_api.RoutesManager
import com.yandex.mapkit.geometry.Point
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainMapViewModel(
    private val repository: OSMRepository,
    private val routesManager: RoutesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<AttractionsUiState>(AttractionsUiState.Loading(true))
    val uiState: StateFlow<AttractionsUiState> = _uiState

    private val _routeState = MutableStateFlow<RouteUiState>(RouteUiState.Loading(true))
    val routeState: StateFlow<RouteUiState> = _routeState

    fun loadAttractions(cityName: String) {
        viewModelScope.launch {
            try {
                val attractionsList = repository.getAttractions(cityName)
                _uiState.value = AttractionsUiState.Success(attractionsList)

                if (attractionsList.isEmpty()) {
                    val ex = Exception("Не найдено достопримечательностей в городе $cityName")
                    _uiState.value = AttractionsUiState.Error(ex)
                }
            } catch (e: Exception) {
                val ex = Exception("Ошибка загрузки: ${e.message}")
                _uiState.value = AttractionsUiState.Error(ex)
                _uiState.value = AttractionsUiState.Loading(false)
            } finally {
                _uiState.value = AttractionsUiState.Loading(false)
            }
        }
    }

    fun calculateRoute(origin: LatLng, destination: LatLng) {
        viewModelScope.launch {
            val route = routesManager.calculateRoute(origin, destination)

            if (route.isSuccess) {
                val route = route.getOrNull()
                if (route != null) {
                    _routeState.value = RouteUiState.Success(route)
                } else {
                    val ex = Exception("Нет данных о мрашруте")
                    _routeState.value = RouteUiState.Error(ex)
                }
            } else {
                route.onFailure { it -> _routeState.value = RouteUiState.Error(it) }
            }
        }
    }

    fun decodePolyline(route: Route): List<Point> {

        return routesManager.decodePolyline(route.polyline.encodedPolyline)
            .map { Point(it.latitude, it.longitude) }
    }

    fun getRouteInfo(route: Route): List<String> {
        val duration = routesManager.formatDuration(route.duration.toInt())
        val distance = routesManager.formatDistance(route.distanceMeters)
        return listOf(duration, distance)
    }


    sealed class RouteUiState {
        data class Success(val route: Route) : RouteUiState()
        data class Error(val exception: Throwable) : RouteUiState()
        data class Loading(val isLoading: Boolean) : RouteUiState()
    }

    sealed class AttractionsUiState {
        data class Success(val attractions: List<OSMPlace>) : AttractionsUiState()
        data class Error(val exception: Throwable) : AttractionsUiState()
        data class Loading(val isLoading: Boolean) : AttractionsUiState()
    }
}