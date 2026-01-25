package com.example.travelhelper.ui.bottom_sheet_view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelhelper.data.data_model.AttractionModel
import com.example.travelhelper.data.db.AttractionsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BottomSheetViewModel(private val repository: AttractionsRepository): ViewModel() {

    fun addLikedAttractionToDb(attraction: AttractionModel){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addAttractionToDB(attraction = attraction.copy(isLiked = true))
        }
    }

    fun removeLikedAttractionFromDb(attraction: AttractionModel) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAttractionFromDB(attraction)
        }
    }
}
