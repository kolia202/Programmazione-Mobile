package com.example.myfitplan.ui.screens.food

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myfitplan.data.database.Food
import com.example.myfitplan.data.database.FoodUnit
import com.example.myfitplan.ui.MyFitPlanRoute
import com.example.myfitplan.ui.composables.NavBar
import com.example.myfitplan.ui.composables.NavBarItem
import com.example.myfitplan.ui.composables.TopBar
import org.koin.androidx.compose.koinViewModel

@Composable
fun FoodScreen(
    userEmail: String,
    navController: NavController,
    viewModel: FoodViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var selectedNav by remember { mutableStateOf(NavBarItem.Ristoranti) }
    var expandedUnit by remember { mutableStateOf(false) }
    var expandedCategory by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme

    LaunchedEffect(userEmail) { viewModel.init(userEmail) }

    Scaffold(
        topBar = {
            TopBar(
                onProfileClick = { navController.navigate(MyFitPlanRoute.Profile) },
                onPieChartClick = { navController.navigate(MyFitPlanRoute.Badge) }
            )
        },
        bottomBar = {
                NavBar(
                    selected = selectedNav,
                    onItemSelected = { selectedNav = it },
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
                text = "Foods",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 28.sp,
                    color = colors.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 2.dp),
                textAlign = TextAlign.Center
            )
            FilterChipsRow(
                categories = state.categories,
                selected = state.selectedCategory,
                onCategorySelected = viewModel::selectCategory
            )
            Spacer(Modifier.height(14.dp))

            if (state.selectedCategory == "Add") {
                Column(
                    Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Add new food",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = colors.primary,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = viewModel::onNameChange,
                        label = { Text("Food name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Category:", fontWeight = FontWeight.Medium, modifier = Modifier.padding(end = 6.dp))
                        Box {
                            Button(
                                onClick = { expandedCategory = true },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                            ) {
                                Text(state.category, color = colors.onPrimary)
                            }
                            DropdownMenu(
                                expanded = expandedCategory,
                                onDismissRequest = { expandedCategory = false }
                            ) {
                                listOf("Breakfast", "Lunch", "Dinner", "Snack").forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = {
                                            viewModel.onCategoryChange(cat)
                                            expandedCategory = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        value = state.kcal,
                        onValueChange = { viewModel.onKcalChange(it.filter { ch -> ch.isDigit() || ch == '.' }) },
                        label = { Text("Kcal per 100g/ml") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.carbs,
                        onValueChange = { viewModel.onCarbsChange(it.filter { ch -> ch.isDigit() || ch == '.' }) },
                        label = { Text("Carbs per 100g/ml") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.protein,
                        onValueChange = { viewModel.onProteinChange(it.filter { ch -> ch.isDigit() || ch == '.' }) },
                        label = { Text("Proteins per 100g/ml") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.fat,
                        onValueChange = { viewModel.onFatChange(it.filter { ch -> ch.isDigit() || ch == '.' }) },
                        label = { Text("Fat per 100g/ml") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Unit:", fontWeight = FontWeight.Medium)
                        Box {
                            Button(
                                onClick = { expandedUnit = true },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                            ) {
                                Text(state.unit.string, color = colors.onPrimary)
                            }
                            DropdownMenu(
                                expanded = expandedUnit,
                                onDismissRequest = { expandedUnit = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Grams") },
                                    onClick = {
                                        viewModel.onUnitChange(FoodUnit.GRAMS)
                                        expandedUnit = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Milliliters") },
                                    onClick = {
                                        viewModel.onUnitChange(FoodUnit.MILLILITERS)
                                        expandedUnit = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.saveFood() },
                        enabled = state.name.isNotBlank() && state.kcal.isNotBlank(),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Add food", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    }
                    if (state.success) {
                        AlertDialog(
                            onDismissRequest = { viewModel.resetSuccess() },
                            title = { Text("Success") },
                            text = { Text("Food added!") },
                            confirmButton = {
                                TextButton(onClick = { viewModel.resetSuccess() }) { Text("OK") }
                            }
                        )
                    }
                    if (state.error != null) {
                        Text(state.error!!, color = colors.error)
                    }
                }
            } else {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = viewModel::onSearch,
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                    label = { Text("Search food") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                if (state.favorites.any { it.description == state.selectedCategory }) {
                    Text(
                        "Favorites",
                        fontWeight = FontWeight.Bold,
                        color = colors.primary,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(top = 2.dp, bottom = 4.dp)
                    )
                    FoodCardList(
                        foods = state.favorites.filter { it.description == state.selectedCategory },
                        onToggleFavorite = viewModel::toggleFavorite,
                        onDeleteFood = viewModel::deleteFood
                    )
                    Spacer(Modifier.height(8.dp))
                }
                Text(
                    "All foods",
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(top = 10.dp, bottom = 4.dp)
                )
                FoodCardList(
                    foods = state.filteredFoods,
                    onToggleFavorite = viewModel::toggleFavorite,
                    onDeleteFood = viewModel::deleteFood
                )
            }
            Spacer(Modifier.height(20.dp))
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
fun FoodCardList(
    foods: List<Food>,
    onToggleFavorite: (Food) -> Unit,
    onDeleteFood: (Food) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    if (foods.isEmpty()) {
        Text(
            "No foods found.",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        return
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        foods.forEach { food ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colors.surface
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Column(
                        Modifier.weight(1f)
                    ) {
                        Text(
                            food.name,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary,
                            fontSize = 18.sp
                        )
                        Text(
                            food.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.secondary,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "${food.kcalPerc} kcal • ${food.carbsPerc}g carbs • ${food.proteinPerc}g protein • ${food.fatPerc}g fat",
                            color = colors.onSurface,
                            fontSize = 15.sp,
                            lineHeight = 17.sp
                        )
                        Text(
                            "Unit: ${food.unit.string}",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                    IconButton(onClick = { onToggleFavorite(food) }) {
                        Icon(
                            imageVector = if (food.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Toggle favorite",
                            tint = if (food.isFavorite) colors.primary else colors.onSurfaceVariant,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    IconButton(onClick = { onDeleteFood(food) }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete food",
                            tint = colors.error,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}