package com.example.myfitplan.ui.screens.profile

import android.annotation.SuppressLint
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myfitplan.ui.MyFitPlanRoute
import com.example.myfitplan.utilities.LocationService
import com.example.myfitplan.utilities.rememberCamera
import com.example.myfitplan.utilities.rememberPermission
import com.example.myfitplan.utilities.saveImageToStorage
import com.example.myfitplan.ui.composables.TopBarProfile

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ProfileScreen(
    navController: androidx.navigation.NavHostController,
    profileViewModel: ProfileViewModel,
    locationService: LocationService
) {
    val userState by remember { derivedStateOf { profileViewModel.loggedUser } }
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme

    LaunchedEffect(Unit) {
        profileViewModel.refreshUser()
    }

    val cameraLauncher = rememberCamera { uri ->
        profileViewModel.setProfilePicUrl(userState.user?.email ?: "", uri.toString())
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                saveImageToStorage(it, context.contentResolver)
                profileViewModel.setProfilePicUrl(userState.user?.email ?: "", it.toString())
            }
        }
    )
    val cameraPermission = rememberPermission(android.Manifest.permission.CAMERA) { status ->
        if (status.isGranted) {
            cameraLauncher.takePicture()
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }
    fun launchCamera() = if (cameraPermission.status.isGranted) {
        cameraLauncher.takePicture()
    } else {
        cameraPermission.launchPermissionRequest()
    }

    Scaffold(
        topBar = {
            TopBarProfile(
                onHomeClick = { navController.navigate(MyFitPlanRoute.Home) },
                onSettingsClick = { navController.navigate(MyFitPlanRoute.Settings) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(paddingValues)
                .padding(horizontal = 22.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            Box(contentAlignment = Alignment.BottomEnd) {
                ProfileImage(
                    profileImage = userState.user?.pictureUrl?.toUri(),
                    onClick = { showDialog = true }
                )
                IconButton(
                    onClick = { showDialog = true },
                    modifier = Modifier
                        .offset(x = (-6).dp, y = (-6).dp)
                        .size(40.dp)
                        .background(colors.primary, CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Change profile picture",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = userState.user?.username ?: "Username",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.primary,
                modifier = Modifier.padding(top = 6.dp)
            )
            Text(
                text = userState.user?.email ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 14.dp)
            )

            Card(
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    Modifier
                        .padding(vertical = 14.dp, horizontal = 18.dp)
                        .fillMaxWidth()
                ) {
                    UserInfoRow("Height", "${userState.user?.height ?: "-"} cm")
                    DividerLine()
                    UserInfoRow("Weight", "${userState.user?.weight ?: "-"} kg")
                    DividerLine()
                    UserInfoRow("Gender", userState.user?.gender?.string ?: "-")
                    DividerLine()
                    UserInfoRow("Age", userState.user?.age?.toString() ?: "-")
                    DividerLine()
                    UserInfoRow("Activity level", userState.user?.activityLevel?.string ?: "-")
                    DividerLine()
                    UserInfoRow("Goal", userState.user?.goal?.string ?: "-")
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                OutlinedButton(
                    onClick = { profileViewModel.logout(navController) },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, colors.error),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text(
                        text = "Logout",
                        color = colors.error,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = { navController.navigate(MyFitPlanRoute.EditProfile) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Edit profile",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Change profile picture", textAlign = TextAlign.Center) },
                    text = { Text("Choose how to change your profile picture", textAlign = TextAlign.Center) },
                    confirmButton = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = {
                                    showDialog = false
                                    galleryLauncher.launch("image/*")
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Gallery", textAlign = TextAlign.Center)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    showDialog = false
                                    launchCamera()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Camera", textAlign = TextAlign.Center)
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showDialog = false
                                profileViewModel.removeProfilePic(userState.user?.email ?: "")
                            }
                        ) {
                            Text("Remove photo", color = colors.error)
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(26.dp))
        }
    }
}

@Composable
fun ProfileImage(profileImage: Uri?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(128.dp)
            .clip(CircleShape)
            .clickable { onClick() }
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), CircleShape)
            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
    ) {
        if (profileImage != null && profileImage.path != null && profileImage.toString().isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(profileImage)
                    .crossfade(true)
                    .build(),
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(128.dp)
                    .clip(CircleShape)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile picture",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(110.dp)
                    .align(Alignment.Center)
            )
        }
    }
}

@Composable
fun UserInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 17.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun DividerLine() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 0.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f)
    )
}