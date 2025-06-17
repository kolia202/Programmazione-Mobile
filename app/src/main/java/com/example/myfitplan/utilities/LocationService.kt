package com.example.myfitplan.utilities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*

enum class MonitoringStatus { Monitoring, Paused, NotMonitoring }
enum class StartMonitoringResult { Started, GPSDisabled, PermissionDenied }
data class Coordinates(val latitude: Double, val longitude: Double)

class LocationService(private val ctx: Context) {
    var monitoringStatus by mutableStateOf(MonitoringStatus.NotMonitoring)
        private set
    var coordinates: Coordinates? by mutableStateOf(null)
        private set

    private val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(ctx)

    // VERSIONE COMPATIBILE (NON Builder)
    @Suppress("DEPRECATION")
    private val locationRequest = LocationRequest.create().apply {
        interval = 10000
        fastestInterval = 5000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            result.locations.lastOrNull()?.let {
                coordinates = Coordinates(it.latitude, it.longitude)
            }
            endLocationRequest()
        }
    }

    fun openLocationSettings() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (intent.resolveActivity(ctx.packageManager) != null) {
            ctx.startActivity(intent)
        }
    }

    fun requestCurrentLocation(): StartMonitoringResult {
        val locationManager = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!isLocationEnabled) return StartMonitoringResult.GPSDisabled

        val permissionGranted = ContextCompat.checkSelfPermission(
            ctx, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!permissionGranted) return StartMonitoringResult.PermissionDenied

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        monitoringStatus = MonitoringStatus.Monitoring
        return StartMonitoringResult.Started
    }

    fun endLocationRequest() {
        if (monitoringStatus == MonitoringStatus.NotMonitoring) return
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        monitoringStatus = MonitoringStatus.NotMonitoring
    }

    fun pauseLocationRequest() {
        if (monitoringStatus != MonitoringStatus.Monitoring) return
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        monitoringStatus = MonitoringStatus.Paused
    }

    fun resumeLocationRequest() {
        if (monitoringStatus != MonitoringStatus.Paused) return
        requestCurrentLocation()
    }
}