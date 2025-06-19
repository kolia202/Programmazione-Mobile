package com.example.myfitplan.ui.screens.signUp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myfitplan.data.database.*
import com.example.myfitplan.ui.MyFitPlanRoute
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignUpScreen(navController: NavController) {
    val viewModel: SignUpViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.success) {
        if (state.success) {
            navController.navigate(MyFitPlanRoute.Theme) {
                popUpTo(MyFitPlanRoute.SignUp) { inclusive = true }
            }
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = state.email,
                onValueChange = { viewModel.onFieldChange { old -> old.copy(email = it) } },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.username,
                onValueChange = { viewModel.onFieldChange { old -> old.copy(username = it) } },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.password,
                onValueChange = { viewModel.onFieldChange { old -> old.copy(password = it) } },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.confirmPassword,
                onValueChange = { viewModel.onFieldChange { old -> old.copy(confirmPassword = it) } },
                label = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth()
            )
            GenderDropdown(state.gender) { value -> viewModel.onFieldChange { old -> old.copy(gender = value) } }
            OutlinedTextField(
                value = state.age,
                onValueChange = { viewModel.onFieldChange { old -> old.copy(age = it) } },
                label = { Text("Age") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.weight,
                onValueChange = { viewModel.onFieldChange { old -> old.copy(weight = it) } },
                label = { Text("Weight (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.height,
                onValueChange = { viewModel.onFieldChange { old -> old.copy(height = it) } },
                label = { Text("Height (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            ActivityDropdown(state.activity) { value -> viewModel.onFieldChange { old -> old.copy(activity = value) } }
            GoalDropdown(state.goal) { value -> viewModel.onFieldChange { old -> old.copy(goal = value) } }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { navController.navigate(MyFitPlanRoute.Login) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { viewModel.signUp() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Register")
                }
            }

            state.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun GenderDropdown(selected: GenderType, onSelect: (GenderType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selected.string,
            onValueChange = {},
            readOnly = true,
            label = { Text("Gender") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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
}

@Composable
fun ActivityDropdown(selected: ActivityType, onSelect: (ActivityType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selected.string,
            onValueChange = {},
            readOnly = true,
            label = { Text("Activity Level") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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
}

@Composable
fun GoalDropdown(selected: GoalType, onSelect: (GoalType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selected.string,
            onValueChange = {},
            readOnly = true,
            label = { Text("Goal") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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
}