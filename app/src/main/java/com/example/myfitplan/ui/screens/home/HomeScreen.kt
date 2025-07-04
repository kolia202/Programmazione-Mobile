package com.example.myfitplan.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.EmojiFoodBeverage
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myfitplan.data.database.MealType
import com.example.myfitplan.data.database.FoodInsideMealWithFood
import com.example.myfitplan.ui.MyFitPlanRoute
import com.example.myfitplan.ui.composables.NavBar
import com.example.myfitplan.ui.composables.NavBarItem
import com.example.myfitplan.ui.composables.TopBar
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val dayX by viewModel.giornoX.collectAsState(1)
    var showEditGoalDialog by remember { mutableStateOf(false) }
    var newGoalText by remember { mutableStateOf(state.stepGoal.toString()) }
    val colors = MaterialTheme.colorScheme

    var selectedTab by remember { mutableStateOf(NavBarItem.Home) }

    Scaffold(
        topBar = { TopBar(
            onProfileClick = { navController.navigate(MyFitPlanRoute.Profile) },
            onPieChartClick = { navController.navigate(MyFitPlanRoute.Badge) }
        ) },
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
                .background(colors.background)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Today",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 30.sp,
                    color = colors.primary
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )
            Text(
                text = viewModel.getSelectedDate(),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.secondary,
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Day $dayX",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.primary,
                    fontSize = 20.sp,
                )
                Text(
                    text = "Summary",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.primary,
                    fontSize = 20.sp,
                )
            }

            HomeSummaryCard(
                user = state.user,
                summary = state.summary,
                kcalBurned = state.stepKcal
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Diet",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.primary,
                    fontSize = 20.sp,
                )
                Text(
                    text = "Other",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.primary,
                    fontSize = 20.sp,
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            MealType.entries.forEach { mealType ->
                val foods = state.meals[mealType].orEmpty()
                MealCard(
                    mealType = mealType,
                    foods = foods,
                    onAddFoodClick = {
                        // da implementare
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Activity",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.primary,
                    fontSize = 20.sp,
                )
                Text(
                    text = "Other",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.primary,
                    fontSize = 20.sp,
                )
            }

            StepCardCustom(
                steps = state.steps,
                km = state.stepKm,
                kcal = state.stepKcal,
                progress = (state.steps.toFloat() / state.stepGoal).coerceIn(0f, 1f),
                goal = state.stepGoal,
                onEditGoal = {
                    newGoalText = state.stepGoal.toString()
                    showEditGoalDialog = true
                }
            )

            if (showEditGoalDialog) {
                AlertDialog(
                    onDismissRequest = { showEditGoalDialog = false },
                    title = { Text("Edit Goal Steps", color = colors.onSurface) },
                    text = {
                        OutlinedTextField(
                            value = newGoalText,
                            onValueChange = { newGoalText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Goal steps", color = colors.onSurfaceVariant) }
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                newGoalText.toIntOrNull()?.let {
                                    viewModel.setStepGoal(it)
                                    showEditGoalDialog = false
                                }
                            }
                        ) { Text("Save") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEditGoalDialog = false }) { Text("Cancel") }
                    },
                    containerColor = colors.surface,
                    titleContentColor = colors.onSurface,
                    textContentColor = colors.onSurface,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun StepCardCustom(
    steps: Int,
    km: Float,
    kcal: Int,
    progress: Float,
    goal: Int,
    onEditGoal: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                "$steps steps",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = colors.onSurface,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    String.format("%.2f km", km),
                    fontSize = 16.sp,
                    color = colors.onSurfaceVariant
                )
                Text(
                    "  |  ",
                    fontSize = 16.sp,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Text(
                    "$kcal Kcal",
                    fontSize = 16.sp,
                    color = colors.onSurfaceVariant
                )
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(25.dp)
                    .padding(vertical = 8.dp),
                color = colors.primary,
                trackColor = colors.secondaryContainer,
            )

            Text(
                "Goal: $goal steps",
                fontSize = 14.sp,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Button(
                onClick = onEditGoal,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary
                )
            ) {
                Text("Edit")
            }
        }
    }
}

@Composable
fun MealCard(
    mealType: MealType,
    foods: List<FoodInsideMealWithFood>,
    onAddFoodClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val kcal = foods.sumOf { (it.food.kcalPerc * it.foodInsideMeal.quantity).toInt() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(colors.primary.copy(alpha = 0.08f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getMealIcon(mealType),
                    contentDescription = mealType.string,
                    tint = colors.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = mealType.string,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    text = "${kcal} kcal",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurface,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            IconButton(
                onClick = { onAddFoodClick() },
                modifier = Modifier
                    .size(25.dp)
                    .background(colors.primary, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add food",
                    tint = colors.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

fun getMealIcon(mealType: MealType): ImageVector {
    return when (mealType) {
        MealType.BREAKFAST -> Icons.Filled.FreeBreakfast
        MealType.LUNCH     -> Icons.Filled.LunchDining
        MealType.DINNER    -> Icons.Filled.DinnerDining
        MealType.SNACK     -> Icons.Filled.EmojiFoodBeverage
    }
}

@Composable
fun HomeSummaryCard(
    user: com.example.myfitplan.data.database.User?,
    summary: MacroSummary,
    kcalBurned: Int = 0
) {
    val colors = MaterialTheme.colorScheme
    val kcalTarget = user?.dailyCalories ?: 2000
    val kcalEaten = summary.calories
    val kcalLeft = (kcalTarget - kcalEaten).coerceAtLeast(0)
    val progress = (kcalEaten.toFloat() / kcalTarget).coerceIn(0f, 1f)

    val carbsTarget = ((user?.dailyCalories ?: 0) * (user?.diet?.carbsPerc ?: 0.5f) / 4).toInt().coerceAtLeast(1)
    val proteinTarget = ((user?.dailyCalories ?: 0) * (user?.diet?.proteinPerc ?: 0.2f) / 4).toInt().coerceAtLeast(1)
    val fatTarget = ((user?.dailyCalories ?: 0) * (user?.diet?.fatPerc ?: 0.25f) / 9).toInt().coerceAtLeast(1)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .heightIn(min = 200.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("$kcalEaten", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.onSurface)
                    Text("Eaten", fontSize = 14.sp, color = colors.onSurface)
                }

                Box(
                    modifier = Modifier
                        .weight(1.3f)
                        .height(92.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(84.dp),
                        color = colors.primary,
                        strokeWidth = 8.dp,
                        trackColor = colors.secondaryContainer,
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$kcalLeft", fontWeight = FontWeight.Bold, fontSize = 23.sp, color = colors.onSurface)
                        Text("Left", fontSize = 13.sp, color = colors.onSurface)
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("$kcalBurned", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.onSurface)
                    Text("Burned", fontSize = 14.sp, color = colors.onSurface)
                }
            }

            Spacer(Modifier.height(18.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MacroProgressBar(
                    label = "Carbs",
                    value = summary.carbs,
                    target = carbsTarget
                )
                MacroProgressBar(
                    label = "Proteins",
                    value = summary.protein,
                    target = proteinTarget
                )
                MacroProgressBar(
                    label = "Fats",
                    value = summary.fat,
                    target = fatTarget
                )
            }
        }
    }
}

@Composable
fun MacroProgressBar(label: String, value: Int, target: Int) {
    val colors = MaterialTheme.colorScheme
    val progress = (value.toFloat() / target).coerceIn(0f, 1f)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = colors.onSurface,
            modifier = Modifier.padding(bottom = 2.dp)
        )
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .width(70.dp)
                .height(8.dp),
            color = colors.primary,
            trackColor = colors.secondaryContainer,
        )
        Text(
            text = "$value / $target g",
            style = MaterialTheme.typography.labelLarge,
            color = colors.onSurface,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}