package com.example.myfitplan.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import kotlinx.serialization.Serializable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myfitplan.ui.screens.home.HomeScreen
import com.example.myfitplan.ui.screens.theme.ThemeScreen
import com.example.myfitplan.ui.screens.theme.ThemeViewModel
import com.example.myfitplan.ui.screens.login.LoginScreen
import com.example.myfitplan.ui.screens.signUp.SignUpScreen
import org.koin.androidx.compose.koinViewModel

sealed interface MyFitPlanRoute {
    @Serializable data object Login : MyFitPlanRoute
    @Serializable data object SignUp : MyFitPlanRoute
    @Serializable data object Theme : MyFitPlanRoute
    @Serializable data object Home : MyFitPlanRoute
}

@Composable
fun MyFitPlanNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = MyFitPlanRoute.Home,
        modifier = modifier
    ) {
        composable<MyFitPlanRoute.Login> {
            LoginScreen(navController)
        }
        composable<MyFitPlanRoute.SignUp> {
            SignUpScreen(navController)
        }
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
    }
}