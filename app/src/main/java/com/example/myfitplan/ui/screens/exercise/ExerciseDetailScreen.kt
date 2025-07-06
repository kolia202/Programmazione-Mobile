package com.example.myfitplan.ui.screens.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myfitplan.data.database.Exercise
import com.example.myfitplan.ui.MyFitPlanRoute
import com.example.myfitplan.ui.composables.TopBarBadge
import org.w3c.dom.Text

@Composable
fun ExerciseDetailScreen(
    navController: NavController,
    exercise: Exercise
){
    val colors = MaterialTheme.colorScheme

    Scaffold (
        topBar = {
            TopBarBadge(
                onHomeClick = { navController.popBackStack() },
                onSettingsClick = { navController.navigate(MyFitPlanRoute.Profile) }
            )
        }
    ){
        padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(colors.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(
                text = exercise.name,
                style= MaterialTheme.typography.headlineLarge,
                color = colors.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = exercise.description,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface
            )
        }
    }
}