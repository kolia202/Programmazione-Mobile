package com.example.myfitplan.ui.screens.badge

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@Composable
fun BadgeScreen(
    badgeViewModel: BadgeViewModel,
    navController: NavController
) {
    val colors = MaterialTheme.colorScheme
    var selectedTab by remember { mutableStateOf(NavBarItem.Home) }

    // --- Stato badge dal VM ---
    val firstBadge by badgeViewModel.firstBadge.collectAsState()
    val earnedFirst by badgeViewModel.earnedFirst.collectAsState()

    val mealBadge by badgeViewModel.mealBadge.collectAsState()
    val earnedMeal by badgeViewModel.earnedMeal.collectAsState()

    val workoutBadge by badgeViewModel.workoutBadge.collectAsState()
    val earnedWorkout by badgeViewModel.earnedWorkout.collectAsState()

    val tenKmBadge by badgeViewModel.tenKmBadge.collectAsState()
    val earnedTenKm by badgeViewModel.earnedTenKm.collectAsState()

    // Lista per la griglia (2 per riga)
    val items = listOf(
        BadgeGridItem(
            badge = firstBadge,
            earned = earnedFirst != null,
            date = earnedFirst?.badgeUser?.dataAchieved,
            howTo = "Sblocchi “${firstBadge.title}” effettuando il tuo primo login dopo la registrazione."
        ),
        BadgeGridItem(
            badge = mealBadge,
            earned = earnedMeal != null,
            date = earnedMeal?.badgeUser?.dataAchieved,
            howTo = "Aggiungi almeno 1 cibo in ciascuna categoria: Breakfast, Lunch, Dinner e Snack."
        ),
        BadgeGridItem(
            badge = workoutBadge,
            earned = earnedWorkout != null,
            date = earnedWorkout?.badgeUser?.dataAchieved,
            howTo = "Completa il tuo primo workout (timer completato)."
        ),
        BadgeGridItem(
            badge = tenKmBadge,
            earned = earnedTenKm != null,
            date = earnedTenKm?.badgeUser?.dataAchieved,
            howTo = "Completa un percorso di almeno 10 km con la navigazione fino alla destinazione."
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(colors.background)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Badge e Grafici",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))

            val gridState = rememberLazyGridState()
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = gridState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 96.dp) // spazio per la bottom bar
            ) {
                items(items) { it ->
                    BadgeGridCard(
                        item = it,
                        onClick = { infoDialog = it }
                    )
                }
            }
        }
    }

    infoDialog?.let { item ->
        InfoDialog(
            title = item.badge.title,
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

@Composable
private fun BadgeGridCard(
    item: BadgeGridItem,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val cardAspectRatio = 1.05f // stessa dimensione per ogni cella

    // Colori distinti
    val containerColor =
        if (item.earned) colors.primaryContainer else colors.surfaceVariant
    val contentColor =
        if (item.earned) colors.onPrimaryContainer else colors.onSurface
    val descriptionColor =
        if (item.earned) colors.onPrimaryContainer.copy(alpha = 0.85f) else colors.onSurfaceVariant
    val dateColor =
        if (item.earned) colors.onPrimaryContainer.copy(alpha = 0.75f) else colors.onSurfaceVariant
    val borderColor =
        if (item.earned) colors.primary else colors.outline

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
            // Cerchio icona
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        if (item.earned) colors.primary else colors.surface
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconFor(item.badge),
                    contentDescription = item.badge.title,
                    tint = if (item.earned) colors.onPrimary else colors.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                item.badge.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                item.badge.description,
                style = MaterialTheme.typography.bodySmall,
                color = descriptionColor,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.weight(1f)) // allinea la sezione finale

            if (item.earned && item.date != null) {
                Text(
                    "Data: ${item.date}",
                    style = MaterialTheme.typography.labelSmall,
                    color = dateColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun InfoDialog(title: String, text: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Ok") } }
    )
}

// Icône diverse in base al campo 'icon' del badge
@Composable
private fun iconFor(badge: Badge) = when (badge.icon.lowercase()) {
    "trophy" -> Icons.Rounded.EmojiEvents
    "restaurant" -> Icons.Rounded.Restaurant
    "dumbbell" -> Icons.Rounded.FitnessCenter
    "route" -> Icons.Rounded.DirectionsRun
    else -> Icons.Rounded.EmojiEvents
}
