package com.example.myfitplan.utilities

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

enum class PermissionStatus {
    Unknown,
    Granted,
    Denied,
    PermanentlyDenied;

    val isGranted get() = this == Granted
    val isDenied get() = this == Denied || this == PermanentlyDenied
}

interface PermissionHandler {
    val permission: String
    val status: PermissionStatus
    fun launchPermissionRequest()
}

@Composable
fun rememberPermission(
    permission: String,
    onResult: (status: PermissionStatus) -> Unit = {}
): PermissionHandler {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
        ?: error("rememberPermission must be used in a ComponentActivity context")

    var status by remember { mutableStateOf(PermissionStatus.Unknown) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        status = when {
            isGranted -> PermissionStatus.Granted
            activity.shouldShowRequestPermissionRationale(permission) -> PermissionStatus.Denied
            else -> PermissionStatus.PermanentlyDenied
        }
        onResult(status)
    }

    return remember {
        object : PermissionHandler {
            override val permission: String = permission
            override val status: PermissionStatus get() = status
            override fun launchPermissionRequest() {
                permissionLauncher.launch(permission)
            }
        }
    }
}