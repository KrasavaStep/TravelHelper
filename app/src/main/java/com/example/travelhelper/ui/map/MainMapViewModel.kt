package com.example.travelhelper.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelhelper.data.data_model.AttractionModel
import com.example.travelhelper.data.data_model.RouteModel
import com.example.travelhelper.data.db.AttractionsRepository
import com.example.travelhelper.data.network.overpass_api.OSMPlace
import com.example.travelhelper.data.network.overpass_api.OSMRepository
import com.example.travelhelper.data.network.routes_api.LatLng
import com.example.travelhelper.data.network.routes_api.Route
import com.example.travelhelper.data.network.routes_api.RoutesManager
import com.example.travelhelper.data.network.routes_api.RoutesRequestWithIntermediates
import com.example.travelhelper.utils.BorderData
import com.example.travelhelper.utils.GsonParser
import com.yandex.mapkit.geometry.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.Dispatcher

class MainMapViewModel(
    private val repository: OSMRepository,
    private val routesManager: RoutesManager,
    private val attractionRepository: AttractionsRepository,
    private val gsonParser: GsonParser
) : ViewModel() {

    private val _uiState = MutableStateFlow<AttractionsUiState>(AttractionsUiState.Loading(true))
    val uiState: StateFlow<AttractionsUiState> = _uiState.asStateFlow()

    private val _customPointState = MutableStateFlow<CustomPointUiState>(CustomPointUiState.Error("пусто"))
    val customPointState: StateFlow<CustomPointUiState> = _customPointState.asStateFlow()

    private val _routeState = MutableStateFlow<RouteUiState>(RouteUiState.Loading(true))
    val routeState: StateFlow<RouteUiState> = _routeState.asStateFlow()

    private val _borderLiveData = MutableLiveData<List<BorderData>>()
    val borderLiveData: LiveData<List<BorderData>> = _borderLiveData

    private val _searchState = MutableStateFlow<List<AttractionModel>>(emptyList())
    val searchState: StateFlow<List<AttractionModel>> = _searchState.asStateFlow()

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
            } finally {
                _uiState.value = AttractionsUiState.Loading(false)
            }
        }
    }

    fun calculateRouteResponse(origin: LatLng, destination: LatLng, intermediates: List<LatLng>? = null) {
        viewModelScope.launch {
            try {
                val route = if (intermediates.isNullOrEmpty()) {
                    routesManager.calculateRoute(origin = origin, destination = destination)
                } else {
                    routesManager.calculateCustomRoute(origin = origin, destination = destination, intermediates = intermediates!!)
                }

                if (route != null) {
                    _routeState.value = RouteUiState.Success(route)
                } else {
                    _routeState.value = RouteUiState.Error(Exception("Маршрут не найден"))
                }

            } catch (e: Exception) {
                _routeState.value = RouteUiState.Error(e)
            } finally {
                _routeState.value = RouteUiState.Loading(false)
            }

        }
    }

    fun getCustomPoints(routeId: Int) {
        viewModelScope.launch {
            _customPointState.value = CustomPointUiState.Success(attractionRepository.getCustomPoints(routeId))
        }
    }

    fun searchAttractions(query: String) {
        viewModelScope.launch {
            if (query.isNotBlank() && query.length >= 2) {
                _searchState.value = attractionRepository.searchAttractions(query)
            } else {
                _searchState.value = emptyList()
            }
        }
    }

    fun decodePolyline(route: Route): List<Point> {
        return routesManager.decodePolyline(route.polyline.encodedPolyline)
            .map { Point(it.latitude, it.longitude) }
    }

    fun getRouteInfo(route: Route): List<String> {
        val routeDuration = route.duration.substring(0, route.duration.length - 1)
        val duration = routesManager.formatDuration(routeDuration.toInt())
        val distance = routesManager.formatDistance(route.distanceMeters)
        return listOf(duration, distance)
    }

    fun getBelarusBorder() {
        gsonParser.parseBorderWithGson()?.let {
            _borderLiveData.value = it
        }
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

    sealed class CustomPointUiState {
        data class Success(val attractions: List<AttractionModel>) : CustomPointUiState()
        data class Error(val exception: String) : CustomPointUiState()
    }
}
