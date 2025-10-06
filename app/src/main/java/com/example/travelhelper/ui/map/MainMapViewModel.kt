package com.example.travelhelper.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelhelper.data.db.AtractionDao
import com.example.travelhelper.data.db.AttractionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainMapViewModel(private val dao: AtractionDao) : ViewModel() {

    private val _selectedAttraction = MutableStateFlow<AttractionEntity?>(null)
    val selectedAttraction: StateFlow<AttractionEntity?> = _selectedAttraction.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val isCurrentPlaceLiked: StateFlow<Boolean> = _selectedAttraction.flatMapLatest { attraction ->
        attraction?.id?.let { dao.isPlaceLiked(it) } ?: flowOf(false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun selectAttraction(attraction: AttractionEntity?) {
        _selectedAttraction.value = attraction
    }

    fun onLikeClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentAttraction = _selectedAttraction.value ?: return@launch
            val currentlyLiked = isCurrentPlaceLiked.value
            dao.setLikedStatus(attractionId = currentAttraction.id, isLiked = !currentlyLiked)
        }
    }

    suspend fun getAllAttractionsFromDb(): List<AttractionEntity> {
        return withContext(Dispatchers.IO) {
            dao.getAllAttractions()
        }
    }
}
