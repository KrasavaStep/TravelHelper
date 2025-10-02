package com.example.travelhelper.ui.map

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelhelper.data.network.OSMPlace
import com.example.travelhelper.data.network.OSMRepository
import com.yandex.mapkit.geometry.Point
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainMapViewModel : ViewModel() {
    /*val placemarksData = MutableLiveData<List<Point>>()

    fun loadPlacemarks() {
        val points = listOf(
            Point(52.4221751,31.0167343),
            Point(52.4218218,31.0156507)
        )
        placemarksData.value = points
    }*/

    private val repository = OSMRepository()

    private val _uiState = MutableStateFlow<AttractionsUiState>(AttractionsUiState.Loading(true))
    val uiState: StateFlow<AttractionsUiState> = _uiState

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

    sealed class AttractionsUiState {
        data class Success(val attractions: List<OSMPlace>): AttractionsUiState()
        data class Error(val exception: Throwable): AttractionsUiState()
        data class Loading(val isLoading: Boolean): AttractionsUiState()
    }
}