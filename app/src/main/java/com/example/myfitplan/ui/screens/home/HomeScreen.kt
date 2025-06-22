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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myfitplan.R
import com.example.myfitplan.data.database.MealType
import com.example.myfitplan.data.database.FoodInsideMealWithFood
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val dayX by viewModel.giornoX.collectAsState(1)

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Today",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 30.sp,
                    color = MaterialTheme.colorScheme.primary
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )
            Text(
                text = viewModel.getSelectedDate(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary,
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
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 20.sp,
                )
                Text(
                    text = "Summary",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 20.sp,
                )
            }

            HomeSummaryCard(
                user = state.user,
                summary = state.summary
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
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 20.sp,
                )
                Text(
                    text = "Other",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary,
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
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


@Composable
fun MealCard(
    mealType: MealType,
    foods: List<FoodInsideMealWithFood>,
    onAddFoodClick: () -> Unit
) {
    val kcal = foods.sumOf { (it.food.kcalPerc * it.foodInsideMeal.quantity).toInt() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getMealIcon(mealType),
                    contentDescription = mealType.string,
                    tint = MaterialTheme.colorScheme.primary,
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
                    color = Color.Black
                )
                Text(
                    text = "${kcal} kcal",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            IconButton(
                onClick = { onAddFoodClick() },
                modifier = Modifier
                    .size(25.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add food",
                    tint = Color.White,
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                    Text("$kcalEaten", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text("Eaten", fontSize = 14.sp, color = Color.Black)
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
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 8.dp,
                        trackColor = Color.LightGray,
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$kcalLeft", fontWeight = FontWeight.Bold, fontSize = 23.sp, color = Color.Black)
                        Text("Left", fontSize = 13.sp, color = Color.Black)
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("$kcalBurned", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text("Burned", fontSize = 14.sp, color = Color.Black)
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
    val progress = (value.toFloat() / target).coerceIn(0f, 1f)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 2.dp)
        )
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .width(70.dp)
                .height(8.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = Color.LightGray,
        )
        Text(
            text = "$value / $target g",
            style = MaterialTheme.typography.labelLarge,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}