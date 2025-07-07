package com.example.myfitplan.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myfitplan.data.database.MealType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    navController: NavController,
    viewModel: HomeViewModel
) {
    LaunchedEffect(Unit) { viewModel.loadSummaryHistory() }
    val summaryList by viewModel.summaryHistory.collectAsState()
    val colors = MaterialTheme.colorScheme

    var showOnlyWithMeals by remember { mutableStateOf(false) }
    var fromDate by remember { mutableStateOf<String?>(null) }
    var toDate by remember { mutableStateOf<String?>(null) }

    val allDates = remember(summaryList) { summaryList.map { it.formattedDate } }

    val filteredList = summaryList.filter { day ->
        val inDateRange = (fromDate == null || allDates.indexOf(day.formattedDate) >= allDates.indexOf(fromDate))
                && (toDate == null || allDates.indexOf(day.formattedDate) <= allDates.indexOf(toDate))
        val hasMeals = !showOnlyWithMeals || MealType.entries.any { (day.meals[it]?.isNotEmpty() == true) }
        inDateRange && hasMeals
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History Summary") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(padding)
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp, horizontal = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Switch(
                        checked = showOnlyWithMeals,
                        onCheckedChange = { showOnlyWithMeals = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.primary,
                            uncheckedThumbColor = colors.secondary,
                            checkedTrackColor = colors.primary.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.scale(0.95f)
                    )
                    Text(
                        "Only days with meals",
                        color = colors.primary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Text(
                    text = "From",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onBackground,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                DateFilterDropdown(
                    label = "From",
                    dates = allDates,
                    selected = fromDate,
                    onSelect = { fromDate = it }
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "To",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onBackground,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                DateFilterDropdown(
                    label = "To",
                    dates = allDates,
                    selected = toDate,
                    onSelect = { toDate = it }
                )
            }

            HorizontalDivider(thickness = 1.dp, color = colors.secondary.copy(alpha = 0.12f))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 6.dp)
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList) { item ->
                    ExpandableSummaryDayCard(item)
                }
            }
        }
    }
}

@Composable
fun ExpandableSummaryDayCard(item: DailySummary) {
    val colors = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(22.dp))
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.formattedDate,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.primary
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = colors.primary
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(
                Modifier.fillMaxWidth(),
                Arrangement.SpaceBetween
            ) {
                MacroItem(label = "Kcal", value = item.summary.calories, color = colors.primary)
                MacroItem(label = "Carbs", value = item.summary.carbs, color = Color(0xFFfcb900))
                MacroItem(label = "Protein", value = item.summary.protein, color = Color(0xFF00c853))
                MacroItem(label = "Fat", value = item.summary.fat, color = Color(0xFFff5252))
            }
            AnimatedVisibility(visible = expanded) {
                Column {
                    Divider(
                        color = colors.secondary.copy(alpha = 0.15f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                    MealType.entries.forEach { mealType ->
                        val foods = item.meals[mealType].orEmpty()
                        if (foods.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(
                                            color = colors.primary.copy(alpha = 0.13f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getMealIcon(mealType),
                                        contentDescription = mealType.string,
                                        tint = colors.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = mealType.string,
                                    color = colors.primary,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 42.dp)
                            ) {
                                foods.forEach { f ->
                                    Text(
                                        text = "- ${f.food.name} (${f.foodInsideMeal.quantity} ${f.food.unit.string}): " +
                                                "${(f.food.kcalPerc * f.foodInsideMeal.quantity).toInt()} kcal",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.onSurface,
                                        modifier = Modifier.padding(bottom = 1.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                        }
                    }
                    if (MealType.entries.all { item.meals[it].isNullOrEmpty() }) {
                        Text(
                            text = "No foods added on this day",
                            color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MacroItem(label: String, value: Int, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.widthIn(min = 50.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = color
        )
        Text(
            text = "$value",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = color
        )
    }
}

@Composable
fun DateFilterDropdown(
    label: String,
    dates: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier
                .height(38.dp)
                .fillMaxWidth(0.92f)
                .defaultMinSize(minWidth = 180.dp)
        ) {
            Text("${selected ?: "All"}", maxLines = 1)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("All") },
                onClick = {
                    onSelect(null)
                    expanded = false
                }
            )
            dates.forEach { date ->
                DropdownMenuItem(
                    text = { Text(date) },
                    onClick = {
                        onSelect(date)
                        expanded = false
                    }
                )
            }
        }
    }
}