package com.example.myfitplan.ui.screens.badge

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
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

    val firstBadge by badgeViewModel.firstBadge.collectAsState()
    val earnedFirst by badgeViewModel.earnedFirst.collectAsState()

    var showHowTo by remember { mutableStateOf(false) }

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
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(10.dp))
            Text(
                "Badge e Grafici",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.primary
            )
            Spacer(Modifier.height(14.dp))

            if (earnedFirst != null) {
                EarnedCard(
                    title = earnedFirst!!.badge.title,
                    description = earnedFirst!!.badge.description,
                    date = earnedFirst!!.badgeUser.dataAchieved,
                    onClick = { showHowTo = true }
                )
            } else {
                LockedCard(
                    title = firstBadge.title,
                    description = firstBadge.description,
                    onClick = { showHowTo = true }
                )
            }

            if (showHowTo) {
                AlertDialog(
                    onDismissRequest = { showHowTo = false },
                    title = { Text("Come si ottiene") },
                    text = { Text("Sblocchi “${firstBadge.title}” effettuando il tuo primo login dopo la registrazione.") },
                    confirmButton = {
                        TextButton(onClick = { showHowTo = false }) { Text("Ok") }
                    }
                )
            }
        }
    }
}

@Composable
private fun EarnedCard(
    title: String,
    description: String,
    date: String,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = colors.surface),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 170.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(colors.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.EmojiEvents,
                    contentDescription = title,
                    tint = colors.onPrimaryContainer,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(description, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, maxLines = 2)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun LockedCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = colors.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 170.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(colors.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.EmojiEvents,
                    contentDescription = title,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(description, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, maxLines = 2)
        }
    }
}