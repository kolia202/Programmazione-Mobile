package com.example.myfitplan.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import kotlinx.serialization.Serializable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myfitplan.ui.screens.badge.BadgeScreen
import com.example.myfitplan.ui.screens.badge.BadgeViewModel
import com.example.myfitplan.ui.screens.editProfile.EditProfileScreen
import com.example.myfitplan.ui.screens.home.HomeScreen
import com.example.myfitplan.ui.screens.theme.ThemeScreen
import com.example.myfitplan.ui.screens.theme.ThemeViewModel
import com.example.myfitplan.ui.screens.login.LoginScreen
import com.example.myfitplan.ui.screens.profile.ProfileScreen
import com.example.myfitplan.ui.screens.profile.ProfileViewModel
import com.example.myfitplan.ui.screens.settings.SettingsScreen
import com.example.myfitplan.ui.screens.settings.SettingsViewModel
import com.example.myfitplan.ui.screens.signUp.SignUpScreen
import com.example.myfitplan.utilities.LocationService
import org.koin.androidx.compose.koinViewModel

sealed interface MyFitPlanRoute {
    @Serializable data object Login : MyFitPlanRoute
    @Serializable data object SignUp : MyFitPlanRoute
    @Serializable data object Theme : MyFitPlanRoute
    @Serializable data object Home : MyFitPlanRoute
    @Serializable data object Profile : MyFitPlanRoute
    @Serializable data object EditProfile : MyFitPlanRoute
    @Serializable data object Badge: MyFitPlanRoute
    @Serializable data object Settings : MyFitPlanRoute
}

@Composable
fun MyFitPlanNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = MyFitPlanRoute.Login,
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
        composable<MyFitPlanRoute.EditProfile>{
            EditProfileScreen(navController)
        }
        composable<MyFitPlanRoute.Profile> {
            val viewModel: ProfileViewModel = koinViewModel()
            val context = LocalContext.current
            val locationService = remember { LocationService(context) }
            ProfileScreen(
                navController = navController,
                profileViewModel = viewModel,
                locationService = locationService
            )
        }
        composable<MyFitPlanRoute.Theme> {
            val viewModel: ThemeViewModel = koinViewModel()
            val themeState by viewModel.state.collectAsStateWithLifecycle()
            ThemeScreen(
                state = themeState,
                onThemeSelected = viewModel::changeTheme
            )
        }
        composable<MyFitPlanRoute.Settings> {
            val vm: SettingsViewModel = koinViewModel()
            SettingsScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }
        composable<MyFitPlanRoute.Badge>{
            val badgeViewModel: BadgeViewModel = koinViewModel()

            BadgeScreen(badgeViewModel)
        }
    }
}