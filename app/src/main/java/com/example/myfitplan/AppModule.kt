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
import com.example.myfitplan.ui.screens.badge.BadgeViewModel
import com.example.myfitplan.ui.screens.editProfile.EditProfileViewModel
import com.example.myfitplan.ui.screens.home.HomeViewModel
import com.example.myfitplan.ui.screens.login.LoginViewModel
import com.example.myfitplan.ui.screens.profile.ProfileViewModel
import com.example.myfitplan.ui.screens.settings.SettingsViewModel
import com.example.myfitplan.ui.screens.signUp.SignUpViewModel
import com.example.myfitplan.ui.screens.theme.ThemeViewModel
import com.example.myfitplan.utilities.StepSensorManager
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
        get<MyFitPlanDatabase>().exerciseInsideDayDAO(),
        get<MyFitPlanDatabase>().stepCounterDAO()
    ) }
    single { get<MyFitPlanDatabase>().badgeDAO() }
    single { get<MyFitPlanDatabase>().badgeUserDAO()}
    viewModel { (userEmail: String) ->
        BadgeViewModel(get(), get(), userEmail)
    }

    single { DatastoreRepository(get()) }
    single { StepSensorManager(get()) }
    viewModel { ThemeViewModel(get()) }
    viewModel { LoginViewModel(get(), get()) }
    viewModel { SignUpViewModel(get(), get()) }
    viewModel { HomeViewModel(get(), get(), get()) }
    viewModel { ProfileViewModel(get(), get()) }
    viewModel { EditProfileViewModel(get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get()) }
    single { androidContext().dataStore }
}