package com.example.travelhelper.ui.bottom_sheet_view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelhelper.data.data_model.AttractionModel
import com.example.travelhelper.data.db.AttractionEntity
import com.example.travelhelper.data.db.AttractionsRepository
import com.example.travelhelper.data.network.overpass_api.OSMPlace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BottomSheetViewModel(private val repository: AttractionsRepository): ViewModel() {

    fun addLikedAttractionToDb(attraction: OSMPlace){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addAttractionToDB(convertToAttractionModel(attraction, isLiked = true))
        }
    }

    private fun convertToAttractionModel(attraction: OSMPlace, isLiked: Boolean): AttractionModel {
        return AttractionModel(
            name = attraction.name,
            category = attraction.category,
            latitude = attraction.latitude,
            longitude = attraction.longitude,
            type = attraction.type,
            description = attraction.description,
            wikipedia = attraction.wikipedia,
            wikidata = attraction.wikidata,
            website = attraction.website,
            openingHours = attraction.openingHours,
            isFee = attraction.isFee,
            isLiked = isLiked
            )
    }

}