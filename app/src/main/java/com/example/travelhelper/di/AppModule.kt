package com.example.travelhelper.di

import com.example.travelhelper.ui.map.MainMapViewModel
import com.example.travelhelper.ui.views.BottomSheetViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {

    // ViewModel for Detail View
    viewModel(named("bottomSheetViewModel")) { BottomSheetViewModel(get()) }
    viewModel(named("mainMapViewModel")) { MainMapViewModel(get()) }

}