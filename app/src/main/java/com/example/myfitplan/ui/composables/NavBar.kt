package com.example.myfitplan.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myfitplan.ui.MyFitPlanRoute

enum class NavBarItem(val icon: @Composable () -> Unit) {
    Home({ Icon(Icons.Default.Home, contentDescription = "Home", modifier = Modifier.size(32.dp)) }),
    Digiuno({ Icon(Icons.Default.Timer, contentDescription = "Digiuno", modifier = Modifier.size(32.dp)) }),
    Esercizi({ Icon(Icons.Default.FitnessCenter, contentDescription = "Esercizi", modifier = Modifier.size(32.dp)) }),
    Ristoranti({ Icon(Icons.Default.Fastfood, contentDescription = "Ristoranti", modifier = Modifier.size(32.dp)) })
}

@Composable
fun NavBar(
    selected: NavBarItem,
    onItemSelected: (NavBarItem) -> Unit,
    navController: NavController
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        color = colors.surfaceVariant,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavBarItem.entries.forEachIndexed { index, item ->
                IconButton(
                    onClick = {
                        onItemSelected(item)
                        if (index == 0) {
                            navController.navigate(MyFitPlanRoute.Home)
                        }
                        if (index == 3) {
                            navController.navigate(MyFitPlanRoute.Restaurant)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    item.icon.invoke().apply {
                        Icon(
                            imageVector = when (item) {
                                NavBarItem.Home -> Icons.Default.Home
                                NavBarItem.Digiuno -> Icons.Default.Timer
                                NavBarItem.Esercizi -> Icons.Default.FitnessCenter
                                NavBarItem.Ristoranti -> Icons.Default.Fastfood
                            },
                            contentDescription = null,
                            tint = colors.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}