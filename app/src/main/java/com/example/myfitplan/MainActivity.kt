package com.example.myfitplan

import android.Manifest
import android.provider.Settings
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.myfitplan.data.models.Theme
import com.example.myfitplan.ui.MyFitPlanNavGraph
import com.example.myfitplan.ui.screens.theme.ThemeViewModel
import com.example.myfitplan.ui.theme.MyFitPlanTheme
import com.example.myfitplan.utilities.LocationService
import com.example.myfitplan.utilities.StartMonitoringResult
import com.example.myfitplan.utilities.rememberPermission
import com.example.myfitplan.utilities.PermissionStatus
import org.koin.androidx.viewmodel.ext.android.getViewModel

class MainActivity : ComponentActivity() {
    private lateinit var locationService: LocationService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationService = LocationService(this)

        enableEdgeToEdge()
        setContent {
            val themeViewModel = getViewModel<ThemeViewModel>()
            val themeState by themeViewModel.state.collectAsStateWithLifecycle()

            MyFitPlanTheme(
                darkTheme = when (themeState.theme) {
                    Theme.Light -> false
                    Theme.Dark -> true
                    Theme.System -> isSystemInDarkTheme()
                }
            ) {
                val snackbarHostState = remember { SnackbarHostState() }
                val navController = rememberNavController()
                val context = this@MainActivity

                var showLocationDisabled by remember { mutableStateOf(false) }
                var showPermissionDenied by remember { mutableStateOf(false) }
                var showPermissionPermanentlyDenied by remember { mutableStateOf(false) }

                val locationPermission =
                    rememberPermission(Manifest.permission.ACCESS_COARSE_LOCATION) { status ->
                        when (status) {
                            PermissionStatus.Granted -> {
                                val result = locationService.requestCurrentLocation()
                                showLocationDisabled = result == StartMonitoringResult.GPSDisabled
                            }

                            PermissionStatus.Denied -> showPermissionDenied = true
                            PermissionStatus.PermanentlyDenied -> showPermissionPermanentlyDenied = true

                            else -> {}
                        }
                    }

                fun requestLocationAccess() {
                    if (locationPermission.status.isGranted) {
                        val result = locationService.requestCurrentLocation()
                        showLocationDisabled = result == StartMonitoringResult.GPSDisabled
                    } else {
                        locationPermission.launchPermissionRequest()
                    }
                }

                LaunchedEffect(Unit) {
                    requestLocationAccess()
                }

                Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
                    MyFitPlanNavGraph(
                        navController = navController,
                        modifier = Modifier.padding(padding)
                    )

                    if (showLocationDisabled) {
                        AlertDialog(
                            title = { Text("Location Disabled") },
                            text = { Text("Enable location services to use this feature.") },
                            onDismissRequest = { showLocationDisabled = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    locationService.openLocationSettings()
                                    showLocationDisabled = false
                                }) {
                                    Text("Open Settings")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showLocationDisabled = false }) {
                                    Text("Dismiss")
                                }
                            }
                        )
                    }

                    if (showPermissionDenied) {
                        AlertDialog(
                            title = { Text("Permission Denied") },
                            text = { Text("We need location permission to continue.") },
                            onDismissRequest = { showPermissionDenied = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    locationPermission.launchPermissionRequest()
                                    showPermissionDenied = false
                                }) {
                                    Text("Try Again")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showPermissionDenied = false }) {
                                    Text("Dismiss")
                                }
                            }
                        )
                    }

                    if (showPermissionPermanentlyDenied) {
                        LaunchedEffect(snackbarHostState) {
                            val result = snackbarHostState.showSnackbar(
                                message = "Permission permanently denied.",
                                actionLabel = "Settings",
                                duration = SnackbarDuration.Long
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            }
                            showPermissionPermanentlyDenied = false
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        locationService.pauseLocationRequest()
    }

    override fun onResume() {
        super.onResume()
        locationService.resumeLocationRequest()
    }
}