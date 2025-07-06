package com.example.myfitplan.ui.screens.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val colors = MaterialTheme.colorScheme

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
                .background(colors.background)
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Exercises",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 28.sp,
                    color = colors.primary
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )


            FilterChipsRow(
                categories = state.categories,
                selected = state.selectedCategory,
                onCategorySelected = viewModel::selectCategory
            )

            Spacer(Modifier.height(14.dp))


            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearch,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                label = { Text("Search exercise") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
            )

            Spacer(Modifier.height(18.dp))


            if (state.favorites.isNotEmpty()) {
                Text(
                    "Favorites",
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(top = 2.dp, bottom = 4.dp)
                )
                FavoritesList(
                    exercises = state.favorites,
                    onToggleFavorite = viewModel::toggleFavorite
                )
                Spacer(Modifier.height(10.dp))
            }

            Text(
                "All Exercises",
                fontWeight = FontWeight.Bold,
                color = colors.primary,
                fontSize = 18.sp,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 10.dp, bottom = 4.dp)
            )
            ExerciseList(
                exercises = state.filteredExercises,
                onToggleFavorite = viewModel::toggleFavorite,
                onExerciseClick = {exercise ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("exercise_detail",exercise)
                    navController.navigate("exercise_detail")
                }
            )

            Spacer(Modifier.height(22.dp))
        }
    }
}

@Composable
fun FilterChipsRow(
    categories: List<String>,
    selected: String,
    onCategorySelected: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories.size) { idx ->
            val cat = categories[idx]
            FilterChip(
                selected = selected == cat,
                onClick = { onCategorySelected(cat) },
                label = {
                    Text(
                        cat,
                        color = if (selected == cat) colors.onPrimary else colors.primary,
                        fontWeight = FontWeight.Medium
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.primary,
                    containerColor = colors.secondaryContainer,
                    labelColor = if (selected == cat) colors.onPrimary else colors.primary,
                    selectedLabelColor = colors.onPrimary
                ),
                modifier = Modifier.height(34.dp)
            )
        }
    }
}

@Composable
fun FavoritesList(
    exercises: List<Exercise>,
    onToggleFavorite: (Exercise) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        exercises.forEach { ex ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(3.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colors.surface
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            ex.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = colors.primary
                        )
                        Text(
                            ex.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.secondary,
                        )
                        Text(
                            ex.description,
                            maxLines = 2,
                            color = colors.onSurface,
                            fontSize = 14.sp
                        )
                    }
                    IconButton(onClick = { onToggleFavorite(ex) }) {
                        Icon(
                            imageVector = if (ex.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Remove from favorites",
                            tint = if (ex.isFavorite) colors.primary else colors.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseList(
    exercises: List<Exercise>,
    onToggleFavorite: (Exercise) -> Unit,
    onExerciseClick: (Exercise) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    if (exercises.isEmpty()) {
        Text(
            "No exercises found.",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurfaceVariant,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        return
    }

    // LazyColumn per lunga lista (nested scroll ok con verticalScroll esterno)
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        exercises.forEach { ex ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .clickable { onExerciseClick(ex) },
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(3.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colors.surface
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            ex.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = colors.primary
                        )
                        Text(
                            ex.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.secondary,
                        )
                        Text(
                            ex.description,
                            maxLines = 2,
                            color = colors.onSurface,
                            fontSize = 14.sp
                        )
                    }
                    IconButton(onClick = { onToggleFavorite(ex) }) {
                        Icon(
                            imageVector = if (ex.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Add to favorites",
                            tint = if (ex.isFavorite) colors.primary else colors.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}