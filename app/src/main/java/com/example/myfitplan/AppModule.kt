package com.example.myfitplan

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.example.myfitplan.data.database.MyFitPlanDatabase
import com.example.myfitplan.data.repositories.DatastoreRepository
import com.example.myfitplan.data.repositories.MyFitPlanRepositories
import com.example.myfitplan.data.repositories.ThemeRepository
import com.example.myfitplan.ui.screens.theme.ThemeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val Context.dataStore: DataStore<Preferences> by preferencesDataStore("theme")

val appModule = module {
    single {
        Room.databaseBuilder(
            get(),
            MyFitPlanDatabase::class.java,
            "myfitplan"
        ).fallbackToDestructiveMigration().build()
    }
    single { get<Context>().dataStore }
    single { ThemeRepository(get()) }
    single { MyFitPlanRepositories(
        get<MyFitPlanDatabase>().userDAO(),
        get<MyFitPlanDatabase>().foodDAO(),
        get<MyFitPlanDatabase>().foodInsideMealDAO(),
        get<MyFitPlanDatabase>().exerciseDAO(),
        get<MyFitPlanDatabase>().exerciseInsideDayDAO())
    }
    single { DatastoreRepository(get()) }
    viewModel { ThemeViewModel(get()) }
    single { androidContext().dataStore }
}