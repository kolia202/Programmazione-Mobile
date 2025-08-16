package com.example.myfitplan.ui.screens.badge

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myfitplan.data.database.Badge
import com.example.myfitplan.ui.MyFitPlanRoute
import com.example.myfitplan.ui.composables.NavBar
import com.example.myfitplan.ui.composables.NavBarItem
import com.example.myfitplan.ui.composables.TopBarBadge
import com.example.myfitplan.ui.screens.home.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun BadgeScreen(
    badgeViewModel: BadgeViewModel,
    navController: NavController
) {
    val colors = MaterialTheme.colorScheme
    var selectedTab by remember { mutableStateOf(NavBarItem.Home) }

    val firstBadge by badgeViewModel.firstBadge.collectAsState()
    val earnedFirst by badgeViewModel.earnedFirst.collectAsState()

    val mealBadge by badgeViewModel.mealBadge.collectAsState()
    val earnedMeal by badgeViewModel.earnedMeal.collectAsState()

    val workoutBadge by badgeViewModel.workoutBadge.collectAsState()
    val earnedWorkout by badgeViewModel.earnedWorkout.collectAsState()

    val tenKmBadge by badgeViewModel.tenKmBadge.collectAsState()
    val earnedTenKm by badgeViewModel.earnedTenKm.collectAsState()

    val items = listOf(
        BadgeGridItem(
            badge = firstBadge,
            earned = earnedFirst != null,
            date = earnedFirst?.badgeUser?.dataAchieved,
            howTo = "Unlock “${englishTitleFor(firstBadge)}” by logging in for the first time after signing up."
        ),
        BadgeGridItem(
            badge = mealBadge,
            earned = earnedMeal != null,
            date = earnedMeal?.badgeUser?.dataAchieved,
            howTo = "Add at least 1 food to each category: Breakfast, Lunch, Dinner, and Snack."
        ),
        BadgeGridItem(
            badge = workoutBadge,
            earned = earnedWorkout != null,
            date = earnedWorkout?.badgeUser?.dataAchieved,
            howTo = "Complete your first workout (timer finished)."
        ),
        BadgeGridItem(
            badge = tenKmBadge,
            earned = earnedTenKm != null,
            date = earnedTenKm?.badgeUser?.dataAchieved,
            howTo = "Complete a route of at least 10 km using navigation to the destination."
        )
    )

    var infoDialog by remember { mutableStateOf<BadgeGridItem?>(null) }

    Scaffold(
        topBar = {
            TopBarBadge(
                onProfileClick = { navController.navigate(MyFitPlanRoute.Profile) },
                onHomeClick = { navController.navigate(MyFitPlanRoute.Home) }
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(padding)
        ) {
            val gridState = rememberLazyGridState()
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = gridState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 48.dp)
            ) {

                item(span = { GridItemSpan(2) }) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Badges & Charts",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.primary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                }

                items(items) { it ->
                    BadgeGridCard(
                        item = it,
                        onClick = { infoDialog = it }
                    )
                }

                item(span = { GridItemSpan(2) }) {
                    Spacer(Modifier.height(12.dp))
                    WeeklyCaloriesChartSection()
                    Spacer(Modifier.height(96.dp))
                }
            }
        }
    }

    infoDialog?.let { item ->
        InfoDialog(
            title = englishTitleFor(item.badge),
            text = item.howTo,
            onDismiss = { infoDialog = null }
        )
    }
}

private data class BadgeGridItem(
    val badge: Badge,
    val earned: Boolean,
    val date: String?,
    val howTo: String
)

private fun badgeKey(b: Badge): String {
    val t = b.title.trim().lowercase()
    val i = b.icon.trim().lowercase()
    return when {

        "dumbbell" in i || "workout" in t || "allenamento" in t -> "first_workout"

        "restaurant" in i || "pasti" in t || "meals" in t -> "all_meals"

        "route" in i || ("10" in t && "km" in t) -> "ten_km"

        "trophy" in i || "🏆" in i || "login" in t || "accesso" in t -> "first_login"
        else -> "unknown"
    }
}

private fun englishTitleFor(badge: Badge): String = when (badgeKey(badge)) {
    "first_login"   -> "First Login"
    "all_meals"     -> "All Meals"
    "first_workout" -> "First Workout"
    "ten_km"        -> "10 km"
    else            -> badge.title
}

private fun englishDescriptionFor(badge: Badge): String = when (badgeKey(badge)) {
    "first_login"   -> "You logged in for the first time!"
    "all_meals"     -> "Add at least one food to Breakfast, Lunch, Dinner, and Snack."
    "first_workout" -> "You completed your first workout!"
    "ten_km"        -> "Complete a route of at least 10 km for the first time."
    else            -> badge.description
}

@Composable
private fun iconFor(badge: Badge) = when (badgeKey(badge)) {
    "first_login"   -> Icons.Rounded.EmojiEvents
    "all_meals"     -> Icons.Rounded.Restaurant
    "first_workout" -> Icons.Rounded.FitnessCenter
    "ten_km"        -> Icons.Rounded.DirectionsRun
    else            -> Icons.Rounded.EmojiEvents
}

@Composable
private fun BadgeGridCard(
    item: BadgeGridItem,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val cardAspectRatio = 1.05f

    val containerColor =
        if (item.earned) colors.primaryContainer else colors.surfaceVariant
    val contentColor =
        if (item.earned) colors.onPrimaryContainer else colors.onSurface
    val descriptionColor =
        if (item.earned) colors.onPrimaryContainer.copy(alpha = 0.85f) else colors.onSurfaceVariant
    val dateColor =
        if (item.earned) colors.onPrimaryContainer.copy(alpha = 0.75f) else colors.onSurfaceVariant

    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (item.earned) 6.dp else 1.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(cardAspectRatio)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        if (item.earned) colors.primary else colors.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconFor(item.badge),
                    contentDescription = englishTitleFor(item.badge),
                    tint = if (item.earned) colors.onPrimary else colors.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                englishTitleFor(item.badge),
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                englishDescriptionFor(item.badge),
                style = MaterialTheme.typography.bodySmall,
                color = descriptionColor,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.weight(1f))

        }
    }
}

@Composable
private fun InfoDialog(title: String, text: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } }
    )
}

@Composable
private fun WeeklyCaloriesChartSection(
    homeVM: HomeViewModel = koinViewModel()
) {
    LaunchedEffect(Unit) { homeVM.loadSummaryHistory() }

    val colors = MaterialTheme.colorScheme
    val history by homeVM.summaryHistory.collectAsState()
    val uiState by homeVM.uiState.collectAsState()

    val kcalTarget = uiState.user?.dailyCalories ?: 2000

    val allDays = remember(history, uiState.summary, kcalTarget) {
        val past = history.map {
            DayKcal(
                rawLabel = it.formattedDate,
                target = kcalTarget,
                eaten = it.summary.calories
            )
        }
        val today = DayKcal(
            rawLabel = homeVM.getSelectedDate(),
            target = kcalTarget,
            eaten = uiState.summary.calories,
            isToday = true
        )
        past + today
    }

    val last7 = allDays.takeLast(minOf(7, allDays.size))
    if (last7.isEmpty()) return

    val startIndex = (allDays.size - last7.size) + 1
    val labeled = remember(last7, startIndex) {
        last7.mapIndexed { i, d -> d.copy(displayLabel = "D${startIndex + i}") }
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Calories Trend (last 7 days)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.primary
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Logged days: ${labeled.size}/7 • Target: $kcalTarget kcal",
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            WeeklyCaloriesChart(
                data = labeled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            )
        }
    }
}

private data class DayKcal(
    val rawLabel: String,
    val target: Int,
    val eaten: Int,
    val isToday: Boolean = false,
    val displayLabel: String = ""
)

@Composable
private fun WeeklyCaloriesChart(
    data: List<DayKcal>,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val maxY = remember(data) { data.maxOf { maxOf(it.target, it.eaten) }.coerceAtLeast(1) }
    val step = remember(maxY) { niceStep(maxY) }

    val density = LocalDensity.current
    val yAxisWidthPx = with(density) { 40.dp.toPx() }
    val labelPaddingPx = with(density) { 40.dp.toPx() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            val chartHeight = size.height - labelPaddingPx
            val chartWidth = size.width - yAxisWidthPx
            val barGroupWidth = chartWidth / data.size
            val barWidth = barGroupWidth / 3

            var yTick = 0
            while (yTick <= maxY) {
                val y = chartHeight - (yTick / maxY.toFloat()) * chartHeight
                drawLine(
                    color = colors.outlineVariant,
                    start = Offset(yAxisWidthPx, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        yTick.toString(),
                        0f,
                        y + 4f,
                        android.graphics.Paint().apply {
                            color = colors.onSurfaceVariant.toArgb()
                            textSize = 28f
                        }
                    )
                }
                yTick += step
            }

            data.forEachIndexed { index, day ->
                val xBase = yAxisWidthPx + index * barGroupWidth + barWidth / 2

                val eatenHeight = (day.eaten / maxY.toFloat()) * chartHeight
                drawRect(
                    color = colors.primary,
                    topLeft = Offset(xBase, chartHeight - eatenHeight),
                    size = Size(barWidth, eatenHeight)
                )

                if (day.isToday) {
                    drawRect(
                        color = colors.tertiary,
                        topLeft = Offset(xBase, chartHeight - eatenHeight),
                        size = Size(barWidth, eatenHeight),
                        style = Stroke(width = 4f)
                    )
                }

                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        day.eaten.toString(),
                        xBase,
                        chartHeight - eatenHeight - with(density) { 6.dp.toPx() },
                        android.graphics.Paint().apply {
                            color = colors.onSurface.toArgb()
                            textSize = 26f
                            textAlign = android.graphics.Paint.Align.LEFT
                        }
                    )
                }

                val targetHeight = (day.target / maxY.toFloat()) * chartHeight
                val xTarget = xBase + barWidth + with(density) { 4.dp.toPx() }
                drawRect(
                    color = colors.secondary,
                    topLeft = Offset(xTarget, chartHeight - targetHeight),
                    size = Size(barWidth, targetHeight)
                )

                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        day.target.toString(),
                        xTarget,
                        chartHeight - targetHeight - with(density) { 6.dp.toPx() },
                        android.graphics.Paint().apply {
                            color = colors.onSurface.toArgb()
                            textSize = 26f
                            textAlign = android.graphics.Paint.Align.LEFT
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 40.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            data.forEach { day ->
                Text(
                    text = day.displayLabel + if (day.isToday) " (today)" else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (day.isToday) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            LegendDot("Consumed", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.width(24.dp))
            LegendDot("Target", MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.onSurface)
        }
    }
}

private fun niceStep(max: Int): Int = when {
    max <= 800 -> 100
    max <= 1600 -> 200
    max <= 2400 -> 300
    max <= 3200 -> 400
    else -> 500
}

@Composable
private fun LegendDot(
    text: String,
    color: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, color = textColor)
    }
}