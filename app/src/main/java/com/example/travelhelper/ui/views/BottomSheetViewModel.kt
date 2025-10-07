package com.example.travelhelper.ui.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelhelper.data.db.AtractionDao
import com.example.travelhelper.data.db.AttractionsRepository
import com.example.travelhelper.data.network.OSMPlace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BottomSheetViewModel(private val repository: AttractionsRepository): ViewModel() {

    fun addLikedAttractionToDb(attraction: OSMPlace){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addAttractionToDB(attraction, isLiked = true)
        }
    }

}