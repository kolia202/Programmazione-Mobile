package com.example.myfitplan.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myfitplan.data.models.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val colors = MaterialTheme.colorScheme

    val error = state.passwordChangeError

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(
                    "Settings",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        color = colors.primary
                    )
                )
            },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(padding)
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            Text(
                "App Theme",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = colors.primary
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                Modifier.selectableGroup().fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Theme.entries.forEach { theme ->
                    Row(
                        Modifier
                            .selectable(
                                selected = (theme == state.theme),
                                onClick = { viewModel.onThemeSelected(theme) },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (theme == state.theme),
                            onClick = null
                        )
                        Text(theme.toString(), modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
            Text(
                "Change Password",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = colors.primary
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            var oldPassVisible by remember { mutableStateOf(false) }
            var newPassVisible by remember { mutableStateOf(false) }
            var confirmPassVisible by remember { mutableStateOf(false) }

            OutlinedTextField(
                value = state.currentPassword,
                onValueChange = viewModel::onCurrentPasswordChange,
                label = { Text("Current Password") },
                singleLine = true,
                isError = error?.contains("current", true) == true,
                visualTransformation = if (oldPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { oldPassVisible = !oldPassVisible }) {
                        Icon(
                            if (oldPassVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (oldPassVisible) "Hide password" else "Show password"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            if (error?.contains("current", true) == true) {
                Text(
                    error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.Start).padding(start = 4.dp, top = 2.dp)
                )
            }

            OutlinedTextField(
                value = state.newPassword,
                onValueChange = viewModel::onNewPasswordChange,
                label = { Text("New Password") },
                singleLine = true,
                isError = error?.contains("New password", true) == true || error?.contains("character", true) == true,
                visualTransformation = if (newPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { newPassVisible = !newPassVisible }) {
                        Icon(
                            if (newPassVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (newPassVisible) "Hide password" else "Show password"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            if (
                error?.contains("New password", true) == true ||
                error?.contains("character", true) == true
            ) {
                Text(
                    error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.Start).padding(start = 4.dp, top = 2.dp)
                )
            }

            OutlinedTextField(
                value = state.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label = { Text("Confirm New Password") },
                singleLine = true,
                isError = error?.contains("match", true) == true,
                visualTransformation = if (confirmPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmPassVisible = !confirmPassVisible }) {
                        Icon(
                            if (confirmPassVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (confirmPassVisible) "Hide password" else "Show password"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            if (error?.contains("match", true) == true) {
                Text(
                    error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.Start).padding(start = 4.dp, top = 2.dp)
                )
            }

            if (
                error?.contains("required", true) == true ||
                error?.contains("occurred", true) == true
            ) {
                Text(
                    error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp)
                )
            }

            Button(
                onClick = { viewModel.changePassword() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 2.dp)
                    .height(54.dp),
                enabled = !state.loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary
                )
            ) {
                Text(
                    "Change Password",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            if (state.passwordChangeSuccess) {
                Text(
                    text = "Password changed successfully",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 6.dp)
                )
            }
        }
    }
}