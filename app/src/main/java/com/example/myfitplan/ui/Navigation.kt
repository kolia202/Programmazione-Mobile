package com.example.myfitplan.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import kotlinx.serialization.Serializable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myfitplan.data.database.Exercise
import com.example.myfitplan.data.repositories.DatastoreRepository
import com.example.myfitplan.data.repositories.MyFitPlanRepositories
import com.example.myfitplan.dataStore
import com.example.myfitplan.ui.screens.badge.BadgeScreen
import com.example.myfitplan.ui.screens.badge.BadgeViewModel
import com.example.myfitplan.ui.screens.editProfile.EditProfileScreen
import com.example.myfitplan.ui.screens.exercise.ExerciseDetailScreen
import com.example.myfitplan.ui.screens.exercise.ExerciseScreen
import com.example.myfitplan.ui.screens.food.FoodScreen
import com.example.myfitplan.ui.screens.food.ManageMealScreen
import com.example.myfitplan.ui.screens.home.HomeScreen
import com.example.myfitplan.ui.screens.theme.ThemeScreen
import com.example.myfitplan.ui.screens.theme.ThemeViewModel
import com.example.myfitplan.ui.screens.login.LoginScreen
import com.example.myfitplan.ui.screens.profile.ProfileScreen
import com.example.myfitplan.ui.screens.profile.ProfileViewModel
import com.example.myfitplan.ui.screens.settings.SettingsScreen
import com.example.myfitplan.ui.screens.settings.SettingsViewModel
import com.example.myfitplan.ui.screens.signUp.SignUpScreen
import com.example.myfitplan.ui.screens.timer.TimerScreen
import com.example.myfitplan.ui.screens.timer.TimerViewModel
import com.example.myfitplan.utilities.LocationService
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.getKoin
import org.koin.core.parameter.parametersOf
import com.example.myfitplan.data.database.MealType
import com.example.myfitplan.ui.screens.home.HomeViewModel
import com.example.myfitplan.ui.screens.home.SummaryScreen
import com.example.myfitplan.utilities.DateUtils.getTodayDate
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

sealed interface MyFitPlanRoute {
    @Serializable data object Login : MyFitPlanRoute
    @Serializable data object SignUp : MyFitPlanRoute
    @Serializable data object Theme : MyFitPlanRoute
    @Serializable data object Home : MyFitPlanRoute
    @Serializable data object Profile : MyFitPlanRoute
    @Serializable data object EditProfile : MyFitPlanRoute
    @Serializable data object Badge: MyFitPlanRoute
    @Serializable data object Settings : MyFitPlanRoute
    @Serializable data object FastingTimer : MyFitPlanRoute
    @Serializable data object Exercise : MyFitPlanRoute
    @Serializable data object Food : MyFitPlanRoute
    @Serializable data object Summary : MyFitPlanRoute
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
        composable<MyFitPlanRoute.EditProfile> {
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
            val viewModel: SettingsViewModel = koinViewModel()
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable<MyFitPlanRoute.Badge> {
            val context = LocalContext.current
            val datastoreRepository = remember { DatastoreRepository(context.dataStore) }
            val userEmail by datastoreRepository.getUserEmail().collectAsState(initial = "")

            if (userEmail.isNotBlank()) {
                val badgeViewModel: BadgeViewModel = koinViewModel() { parametersOf(userEmail) }
                BadgeScreen(badgeViewModel,navController)
            }
        }
        composable<MyFitPlanRoute.FastingTimer> {
            val timerViewModel: TimerViewModel = koinViewModel()
            TimerScreen(
                navController = navController,
                viewModel = timerViewModel
            )
        }
        composable<MyFitPlanRoute.Exercise> {
            val context = LocalContext.current
            val repo = getKoin().get<MyFitPlanRepositories>()
            val datastore = remember { DatastoreRepository(context.dataStore) }
            val userEmail by datastore.getUserEmail().collectAsState(initial = "")
            if (userEmail.isNotBlank()) {
                ExerciseScreen(navController, userEmail)
            }
        }
        composable<MyFitPlanRoute.Food> {
            val context = LocalContext.current
            val datastore = remember { DatastoreRepository(context.dataStore) }
            val userEmail by datastore.getUserEmail().collectAsState(initial = "")
            if (userEmail.isNotBlank()) {
                FoodScreen(
                    userEmail = userEmail,
                    navController = navController
                )
            }
        }
        composable("exercise_detail"){
            val exercise = navController
                .previousBackStackEntry
                ?.savedStateHandle
                ?.get<Exercise>("exercise_detail")

            if (exercise != null){
                ExerciseDetailScreen(
                    navController = navController,
                    exercise=exercise
                )
            }
        }
        composable("manage_meal/{mealType}") { backStackEntry ->
            val mealType = MealType.valueOf(backStackEntry.arguments?.getString("mealType") ?: "BREAKFAST")
            val context = LocalContext.current
            val datastore = remember { DatastoreRepository(context.dataStore) }
            val userEmail by datastore.getUserEmail().collectAsState(initial = "")
            if (userEmail.isNotBlank()) {
                ManageMealScreen(
                    mealType = mealType,
                    date = getTodayDate(),
                    userEmail = userEmail,
                    navController = navController
                )
            }
        }
        composable<MyFitPlanRoute.Summary> {
            val viewModel: HomeViewModel = koinViewModel()
            SummaryScreen(navController = navController, viewModel = viewModel)
        }
    }
}