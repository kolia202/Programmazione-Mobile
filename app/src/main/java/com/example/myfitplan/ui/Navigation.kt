package com.example.myfitplan.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import kotlinx.serialization.Serializable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myfitplan.ui.screens.home.HomeScreen
import com.example.myfitplan.ui.screens.theme.ThemeScreen
import com.example.myfitplan.ui.screens.theme.ThemeViewModel
import com.example.myfitplan.ui.screens.login.LoginScreen
import org.koin.androidx.compose.koinViewModel

sealed interface MyFitPlanRoute {
    @Serializable data object Home : MyFitPlanRoute
    @Serializable data object Theme : MyFitPlanRoute
    @Serializable data object Login : MyFitPlanRoute
     {

    }
}

@Composable
fun MyFitPlanNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = MyFitPlanRoute.Home,
    ) {
        composable<MyFitPlanRoute.Home> {
            HomeScreen(navController)
        }
        composable<MyFitPlanRoute.Theme> {
            val viewModel: ThemeViewModel = koinViewModel()
            val themeState by viewModel.state.collectAsStateWithLifecycle()
            ThemeScreen(
                state = themeState,
                onThemeSelected = viewModel::changeTheme
            )
        }
        composable<MyFitPlanRoute.Login>{
            LoginScreen(navController)
        }
    }
}