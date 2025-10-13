package com.example.travelhelper.ui.liked_places

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelhelper.data.data_model.AttractionModel
import com.example.travelhelper.data.data_model.RouteModel
import com.example.travelhelper.data.db.AttractionsRepository
import com.example.travelhelper.data.network.overpass_api.OSMPlace
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LikedPlacesViewModel(
    private val attractionRepository: AttractionsRepository
) : ViewModel() {

    private val _likedAttractionState = MutableStateFlow<LikedAttractionsUiState>(LikedAttractionsUiState.Empty("Нет избранных мест"))
    val likedAttractionState: StateFlow<LikedAttractionsUiState> = _likedAttractionState.asStateFlow()

    private val _routesState = MutableStateFlow<RoutesUiState>(RoutesUiState.Empty("Нет избранных мест"))
    val routesState: StateFlow<RoutesUiState> = _routesState.asStateFlow()


    fun getLikedAttractions() {
        viewModelScope.launch {
            val attractionList = attractionRepository.getLikedAttractionsData()
            _likedAttractionState.value = LikedAttractionsUiState.Success(attractionList)

            if (attractionList.isEmpty()) {
                _likedAttractionState.value = LikedAttractionsUiState.Empty("Нет избранных мест")
            }

        }
    }

    fun getRoutes() {
        viewModelScope.launch {
            val routesList = attractionRepository.getRoutesData()
            _routesState.value = RoutesUiState.Success(routesList)

            if (routesList.isEmpty()) {
                _routesState.value = RoutesUiState.Empty("Нет избранных мест")
            }

        }
    }

    sealed class LikedAttractionsUiState {
        data class Success(val attractions: List<AttractionModel>) : LikedAttractionsUiState()
        data class Empty(val emptyAlert: String) : LikedAttractionsUiState()
    }

    sealed class RoutesUiState {
        data class Success(val routes: List<RouteModel>) : RoutesUiState()
        data class Empty(val emptyAlert: String) : RoutesUiState()
    }

}