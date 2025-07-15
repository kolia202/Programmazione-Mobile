package com.example.myfitplan.ui.screens.badge

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myfitplan.R
import com.example.myfitplan.data.database.BadgeWithUserData
import com.example.myfitplan.ui.MyFitPlanRoute
import com.example.myfitplan.ui.composables.NavBar
import com.example.myfitplan.ui.composables.NavBarItem
import com.example.myfitplan.ui.composables.TopBar
import com.example.myfitplan.ui.composables.TopBarBadge

@Composable
fun BadgeScreen(badgeViewModel: BadgeViewModel, navController: NavController) {

    val userBadges = badgeViewModel.userBadges.collectAsState().value
    val colors = MaterialTheme.colorScheme

    var selectedTab by remember { mutableStateOf(NavBarItem.Home) }

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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Your Badges",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (userBadges.isEmpty()) {
                Text(
                    text = "Nessun Badge sbloccato",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.secondary
                )
            } else {
                userBadges.forEach { badgeWithUserData ->
                    BadgeCard(badgeWithUserData)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}



@Composable
fun BadgeCard(badgeWithUserData: BadgeWithUserData) {
    val colors = MaterialTheme.colorScheme
    val badge = badgeWithUserData.badge

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = CardDefaults.shape,
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = getImageResource(badge.icon)),
                contentDescription = badge.title,
                modifier = Modifier.size(64.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = badge.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface
                )
                Text(
                    text = badge.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
                Text(
                    text = "Ottenuto il: ${badgeWithUserData.badgeUser.dataAchieved}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}

fun getImageResource(iconName: String): Int {
    return when (iconName) {
        "badge_star" -> R.drawable.badge_star
        "badge_cup" -> R.drawable.badge_cup
        else -> R.drawable.ic_default_badge
    }
}
