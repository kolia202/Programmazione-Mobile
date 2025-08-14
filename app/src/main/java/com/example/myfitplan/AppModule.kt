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
import com.example.myfitplan.ui.screens.exercise.ExerciseViewModel
import com.example.myfitplan.ui.screens.food.FoodViewModel
import com.example.myfitplan.ui.screens.home.HomeViewModel
import com.example.myfitplan.ui.screens.login.LoginViewModel
import com.example.myfitplan.ui.screens.profile.ProfileViewModel
import com.example.myfitplan.ui.screens.settings.SettingsViewModel
import com.example.myfitplan.ui.screens.signUp.SignUpViewModel
import com.example.myfitplan.ui.screens.theme.ThemeViewModel
import com.example.myfitplan.ui.screens.timer.TimerViewModel
import com.example.myfitplan.ui.screens.tracker.TrackerViewModel
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
        )
            .fallbackToDestructiveMigration()
            .build()
    }


    single { androidContext().dataStore }
    single { ThemeRepository(get()) }
    single { DatastoreRepository(get()) }


    single { get<MyFitPlanDatabase>().userDAO() }
    single { get<MyFitPlanDatabase>().foodDAO() }
    single { get<MyFitPlanDatabase>().foodInsideMealDAO() }
    single { get<MyFitPlanDatabase>().exerciseDAO() }
    single { get<MyFitPlanDatabase>().exerciseInsideDayDAO() }
    single { get<MyFitPlanDatabase>().routeDAO() }
    single { get<MyFitPlanDatabase>().fastingSessionDAO() }
    single { get<MyFitPlanDatabase>().badgeDAO() }
    single { get<MyFitPlanDatabase>().badgeUserDAO() }


    single {
        MyFitPlanRepositories(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }


    viewModel { ThemeViewModel(get()) }
    viewModel { LoginViewModel(get(), get(), get(), get()) }
    viewModel { SignUpViewModel(get(), get()) }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { ProfileViewModel(get(), get()) }
    viewModel { EditProfileViewModel(get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get()) }
    viewModel { (userEmail: String) -> ExerciseViewModel(get(), userEmail) }
    viewModel { FoodViewModel(get()) }
    viewModel { (userEmail: String) ->
        BadgeViewModel(
            badgeDao = get(),
            badgeUserDao = get(),
            repo = get(),
            userEmail = userEmail
        )
    }

   viewModel {
        TimerViewModel(
            repo = get(),
            datastore = get(),
            badgeDao = get(),
            badgeUserDao = get(),
            app = get()
        )
    }

    viewModel {
        TrackerViewModel(
            app = get(),
            repos = get(),
            ds = get(),
            badgeDao = get(),
            badgeUserDao = get()
        )
    }
}