package com.example.travelhelper // Убедитесь, что имя вашего пакета верное

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelhelper.data.data_model.AttractionModel
import com.example.travelhelper.data.db.AttractionsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class SearchViewModel(
    private val repository: AttractionsRepository
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<AttractionModel>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    fun performSearch(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _searchResults.value = emptyList()
            } else {

                // 1. Получаем данные из БД в виде List<AttractionEntity>
                val attractionsFromDb = repository.getAllAttractionsData()

                // 2. Фильтруем этот список по имени
                val filteredEntities = attractionsFromDb.filter { entity ->
                    entity.name.contains(query, ignoreCase = true)
                }

                // 3. Преобразуем (map) отфильтрованный список List<AttractionEntity>
                //    в нужный нам List<AttractionModel>
                val resultsAsModels = filteredEntities.map { entity ->
                    AttractionModel(
                        id = entity.id, // Не забудьте перенести все поля
                        name = entity.name,
                        category = entity.category,
                        latitude = entity.latitude,
                        longitude = entity.longitude,
                        type = entity.type,
                        description = entity.description,
                        wikipedia = entity.wikipedia,
                        wikidata = entity.wikidata,
                        website = entity.website,
                        openingHours = entity.openingHours,
                        isFee = entity.isFee,
                        isLiked = entity.isLiked
                    )
                }

                // 4. Присваиваем уже преобразованный список. Ошибки больше нет.
                _searchResults.value = resultsAsModels
            }
        }
    }
}
