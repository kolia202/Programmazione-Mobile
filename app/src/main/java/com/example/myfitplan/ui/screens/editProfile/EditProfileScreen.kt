package com.example.myfitplan.ui.screens.editProfile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.navigation.NavController
import com.example.myfitplan.data.database.*
import com.example.myfitplan.ui.screens.profile.ProfileViewModel
import com.example.myfitplan.utilities.rememberCamera
import com.example.myfitplan.utilities.rememberPermission
import com.example.myfitplan.utilities.saveImageToStorage
import org.koin.androidx.compose.koinViewModel

@Composable
fun EditProfileScreen(
    navController: NavController,
    editProfileViewModel: EditProfileViewModel = koinViewModel(),
    profileViewModel: ProfileViewModel = koinViewModel() // importante: VM condiviso per immagine
) {
    val editState = editProfileViewModel.editState
    val colors = MaterialTheme.colorScheme

    val context = LocalContext.current

    // Stato utente per la foto
    val userState by remember { derivedStateOf { profileViewModel.loggedUser } }

    // Dialog gestione foto
    var showDialog by remember { mutableStateOf(false) }

    // --- CAMERA/GALLERY HANDLERS ---
    val cameraLauncher = rememberCamera { uri ->
        profileViewModel.setProfilePicUrl(userState.user?.email ?: "", uri.toString())
        Toast.makeText(context, "Profile photo updated!", Toast.LENGTH_SHORT).show()
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                saveImageToStorage(it, context.contentResolver)
                profileViewModel.setProfilePicUrl(userState.user?.email ?: "", it.toString())
                Toast.makeText(context, "Profile photo updated!", Toast.LENGTH_SHORT).show()
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
    // --- END CAMERA/GALLERY HANDLERS ---

    // Error handling campi
    var heightError by remember { mutableStateOf(false) }
    var weightError by remember { mutableStateOf(false) }
    var ageError by remember { mutableStateOf(false) }

    fun validate(): Boolean {
        heightError = editState.height.toFloatOrNull() == null
        weightError = editState.weight.toFloatOrNull() == null
        ageError = editState.age.toIntOrNull() == null
        return !heightError && !weightError && !ageError
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(34.dp))

            Text(
                text = "Edit Profile",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = colors.primary
                ),
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // --- FOTO PROFILO CAMBIABILE ---
            Box(contentAlignment = Alignment.BottomEnd) {
                // Foto profilo
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

            Spacer(modifier = Modifier.height(20.dp))

            // --- INPUT FIELDS, flat senza card ---
            OutlinedTextField(
                value = editState.height,
                onValueChange = { editProfileViewModel.onValueChange { copy(height = it) } },
                label = { Text("Height (cm)") },
                leadingIcon = { Icon(Icons.Default.Height, null) },
                isError = heightError,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            )
            if (heightError) ErrorText("Invalid height")

            OutlinedTextField(
                value = editState.weight,
                onValueChange = { editProfileViewModel.onValueChange { copy(weight = it) } },
                label = { Text("Weight (kg)") },
                leadingIcon = { Icon(Icons.Default.FitnessCenter, null) },
                isError = weightError,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            )
            if (weightError) ErrorText("Invalid weight")

            OutlinedTextField(
                value = editState.age,
                onValueChange = { editProfileViewModel.onValueChange { copy(age = it) } },
                label = { Text("Age") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                isError = ageError,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            )
            if (ageError) ErrorText("Invalid age")

            GenderDropdown(
                selected = editState.gender,
                onSelect = { editProfileViewModel.onValueChange { copy(gender = it) } }
            )
            ActivityDropdown(
                selected = editState.activity,
                onSelect = { editProfileViewModel.onValueChange { copy(activity = it) } }
            )
            GoalDropdown(
                selected = editState.goal,
                onSelect = { editProfileViewModel.onValueChange { copy(goal = it) } }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // --- BOTTONI ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = {
                        if (validate()) {
                            editProfileViewModel.saveProfile {
                                navController.popBackStack()
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Save")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // --- DIALOG FOTO ---
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Change profile picture", textAlign = androidx.compose.ui.text.style.TextAlign.Center) },
                text = { Text("Choose how to change your profile picture", textAlign = androidx.compose.ui.text.style.TextAlign.Center) },
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
                            Text("Gallery", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                showDialog = false
                                launchCamera()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Camera", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
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
    }
}

// --- ProfileImage RIUTILIZZATA da ProfileScreen, la puoi estrarre come composable comune se vuoi ---
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

// --- Resto dei dropdown e errorText come prima ---
@Composable
fun GenderDropdown(selected: GenderType, onSelect: (GenderType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = selected.string,
        onValueChange = {},
        readOnly = true,
        label = { Text("Gender") },
        leadingIcon = { Icon(Icons.Default.Person, null) },
        shape = RoundedCornerShape(14.dp),
        trailingIcon = {
            IconButton(onClick = { expanded = !expanded }) {
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable { expanded = true }
    )
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.fillMaxWidth(0.9f)
    ) {
        GenderType.entries.forEach {
            DropdownMenuItem(
                text = { Text(it.string) },
                onClick = {
                    onSelect(it)
                    expanded = false
                }
            )
        }
    }
}

@Composable
fun ActivityDropdown(selected: ActivityType, onSelect: (ActivityType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = selected.string,
        onValueChange = {},
        readOnly = true,
        label = { Text("Activity Level") },
        leadingIcon = { Icon(Icons.Default.FitnessCenter, null) },
        shape = RoundedCornerShape(14.dp),
        trailingIcon = {
            IconButton(onClick = { expanded = !expanded }) {
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable { expanded = true }
    )
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.fillMaxWidth(0.9f)
    ) {
        ActivityType.entries.forEach {
            DropdownMenuItem(
                text = { Text(it.string) },
                onClick = {
                    onSelect(it)
                    expanded = false
                }
            )
        }
    }
}

@Composable
fun GoalDropdown(selected: GoalType, onSelect: (GoalType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = selected.string,
        onValueChange = {},
        readOnly = true,
        label = { Text("Goal") },
        leadingIcon = { Icon(Icons.Default.Star, null) },
        shape = RoundedCornerShape(14.dp),
        trailingIcon = {
            IconButton(onClick = { expanded = !expanded }) {
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable { expanded = true }
    )
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.fillMaxWidth(0.9f)
    ) {
        GoalType.entries.forEach {
            DropdownMenuItem(
                text = { Text(it.string) },
                onClick = {
                    onSelect(it)
                    expanded = false
                }
            )
        }
    }
}

@Composable
fun ErrorText(text: String) {
    Text(
        text,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier
            .padding(start = 4.dp, top = 1.dp, bottom = 2.dp)
    )
}