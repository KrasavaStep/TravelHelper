package com.example.travelhelper.di

import com.example.travelhelper.ui.map.MainMapViewModel
import com.example.travelhelper.ui.bottom_sheet_view.BottomSheetViewModel
import com.example.travelhelper.ui.liked_places.LikedPlacesViewModel
import com.example.travelhelper.utils.SharedViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {

    // ViewModel for Detail View
    viewModel(named("bottomSheetViewModel")) { BottomSheetViewModel(get()) }
    viewModel(named("mainMapViewModel")) { MainMapViewModel(get(), get(), get()) }
    viewModel(named("sharedViewModel")) { SharedViewModel() }
    viewModel(named("likedViewModel")) { LikedPlacesViewModel(get()) }

}