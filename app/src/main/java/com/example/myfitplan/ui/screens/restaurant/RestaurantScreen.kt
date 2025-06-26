package com.example.myfitplan.ui.screens.restaurant

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myfitplan.ui.MyFitPlanRoute
import com.example.myfitplan.utilities.rememberPermission
import org.koin.androidx.compose.koinViewModel

@Composable
fun RestaurantScreen(
    navController: NavController,
    viewModel: RestaurantViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val locationPermission = rememberPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    val radius by viewModel.radius.collectAsState()
    var showPermissionDialog by remember { mutableStateOf(false) }

    // Check permission on enter
    LaunchedEffect(locationPermission.status) {
        if (locationPermission.status.isDenied) {
            navController.navigate(MyFitPlanRoute.Profile)
        }
    }

    // UI
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 24.dp)
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Fastfood,
                contentDescription = "Ristoranti",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Ristoranti nei dintorni",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp
                ),
            )
            Spacer(Modifier.height(16.dp))

            Text(
                text = "Scegli il raggio di ricerca:",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Slider(
                    value = radius.toFloat(),
                    onValueChange = { viewModel.setRadius(it.toInt()) },
                    valueRange = 1f..10f,
                    steps = 9,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(12.dp))
                Text("$radius km", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    // Intent to Google Maps con ricerca "ristoranti" e raggio (circa)
                    val gmmIntentUri = Uri.parse("geo:0,0?q=ristoranti&radius=${radius * 1000}")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    if (mapIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(mapIntent)
                    } else {
                        Toast.makeText(context, "Maps non disponibile", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Cerca ristoranti", fontSize = 18.sp)
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Tocca un ristorante su Maps per vedere la scheda completa e prenotare.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}