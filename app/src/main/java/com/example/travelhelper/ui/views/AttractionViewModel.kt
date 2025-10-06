package com.example.travelhelper.ui.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelhelper.data.db.AtractionDao
import com.example.travelhelper.data.db.AttractionsRepository
import com.example.travelhelper.data.network.OSMPlace
import com.example.travelhelper.data.network.OSMRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AttractionViewModel(private val dao: AtractionDao): ViewModel() {

    private val repository = AttractionsRepository(dao)
    fun addAttractionToDb(attraction: OSMPlace){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addAttractionToDB(attraction)
        }
    }

}