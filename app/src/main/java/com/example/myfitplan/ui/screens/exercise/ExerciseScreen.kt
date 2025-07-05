package com.example.myfitplan.ui.screens.exercise

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myfitplan.data.database.Exercise
import com.example.myfitplan.ui.MyFitPlanRoute
import com.example.myfitplan.ui.composables.NavBar
import com.example.myfitplan.ui.composables.NavBarItem
import com.example.myfitplan.ui.composables.TopBar
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ExerciseScreen(
    navController: NavController,
    userEmail: String
) {
    val viewModel: ExerciseViewModel = koinViewModel { parametersOf(userEmail) }
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(NavBarItem.Esercizi) }

    Scaffold(
        topBar = {
            TopBar(
                onProfileClick = { navController.navigate(MyFitPlanRoute.Profile) },
                onPieChartClick = { navController.navigate(MyFitPlanRoute.Badge) }
            )
        },
        bottomBar = {
            NavBar(
                selected = selectedTab,
                onItemSelected = { selectedTab = it },
                navController = navController
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp)
        ) {
            // Categorie (Tab)
            ScrollableTabRow(
                selectedTabIndex = state.categories.indexOf(state.selectedCategory)
            ) {
                state.categories.forEachIndexed { i, cat ->
                    Tab(
                        selected = state.selectedCategory == cat,
                        onClick = { viewModel.selectCategory(cat) },
                        text = { Text(cat) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Barra ricerca
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearch,
                label = { Text("Cerca esercizio") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // Preferiti
            if (state.favorites.isNotEmpty()) {
                Text("Preferiti", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                ExerciseList(
                    exercises = state.favorites,
                    onToggleFavorite = viewModel::toggleFavorite,
                    onExerciseClick = { exercise ->
                        navController.navigate("exerciseDetail/${exercise.name}")
                    }
                )
            }

            Spacer(Modifier.height(8.dp))

            // Lista esercizi filtrata
            Text("Esercizi", fontWeight = FontWeight.Bold)
            ExerciseList(
                exercises = state.filteredExercises,
                onToggleFavorite = viewModel::toggleFavorite,
                onExerciseClick = { exercise ->
                    navController.navigate("exerciseDetail/${exercise.name}")
                }
            )
        }
    }
}

@Composable
fun ExerciseList(
    exercises: List<Exercise>,
    onToggleFavorite: (Exercise) -> Unit,
    onExerciseClick: (Exercise) -> Unit
) {
    Column {
        exercises.forEach { ex ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clickable { onExerciseClick(ex) },
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(ex.name, fontWeight = FontWeight.Bold)
                        Text(ex.category, style = MaterialTheme.typography.bodySmall)
                        Text(ex.description, maxLines = 2)
                    }
                    IconButton(onClick = { onToggleFavorite(ex) }) {
                        Icon(
                            imageVector = if (ex.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Aggiungi ai preferiti",
                            tint = if (ex.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}