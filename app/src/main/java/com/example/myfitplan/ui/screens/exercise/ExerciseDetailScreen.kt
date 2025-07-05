package com.example.myfitplan.ui.screens.exercise

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myfitplan.data.database.Exercise
import com.example.myfitplan.data.repositories.MyFitPlanRepositories
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    exerciseName: String,
    userEmail: String,
    navController: NavController,
    repo: MyFitPlanRepositories
) {
    var exercise by remember { mutableStateOf<Exercise?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(exerciseName) {
        exercise = repo.getExercise(exerciseName, userEmail)
    }

    exercise?.let { ex ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(ex.name) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.Favorite, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Column(Modifier.padding(padding).padding(18.dp)) {
                Text(ex.category, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                Text("Descrizione:", fontWeight = FontWeight.Bold)
                Text(ex.description)
                Spacer(Modifier.height(8.dp))
                Text("Calorie bruciate: ${ex.kcalBurned}")
                IconButton(onClick = {
                    scope.launch { repo.upsertExercise(ex.copy(isFavorite = !ex.isFavorite)) }
                }) {
                    Icon(
                        imageVector = if (ex.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Aggiungi ai preferiti"
                    )
                }
            }
        }
    }
}