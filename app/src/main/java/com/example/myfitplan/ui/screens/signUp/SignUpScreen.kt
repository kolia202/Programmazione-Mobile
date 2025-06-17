package com.example.myfitplan.ui.screens.signUp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
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
import com.example.myfitplan.data.repositories.DatastoreRepository
import com.example.myfitplan.data.repositories.MyFitPlanRepositories
import com.example.myfitplan.ui.MyFitPlanRoute
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun SignUpScreen(navController: NavController) {
    val repo: MyFitPlanRepositories = koinInject()
    val datastore: DatastoreRepository = koinInject()
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf(GenderType.OTHER) }
    var age by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var activity by remember { mutableStateOf(ActivityType.SEDENTARY) }
    var goal by remember { mutableStateOf(GoalType.MAINTAIN_WEIGHT) }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("Confirm Password") }, modifier = Modifier.fillMaxWidth())

            GenderDropdown(gender) { gender = it }
            OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Age") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("Weight (kg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = height, onValueChange = { height = it }, label = { Text("Height (cm)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

            ActivityDropdown(activity) { activity = it }
            GoalDropdown(goal) { goal = it }

            Button(onClick = {
                if (password != confirmPassword) {
                    error = "Passwords do not match"
                } else {
                    coroutineScope.launch {
                        val bmr = calculateBMR(gender, weight.toFloat(), height.toFloat(), age.toInt())
                        val dailyCalories = (bmr * activity.k * goal.k).toInt()
                        val user = User(email, password, username, null, height.toFloat(), weight.toFloat(), gender, age.toInt(), activity, goal, bmr, dailyCalories, DietType.STANDARD)
                        repo.insertUser(user)
                        datastore.saveUser(user)
                        navController.navigate(MyFitPlanRoute.Theme) {
                            popUpTo(MyFitPlanRoute.SignUp) { inclusive = true }
                        }
                    }
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Sign Up")
            }

            error?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }
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
            GenderType.values().forEach {
                DropdownMenuItem(text = { Text(it.string) }, onClick = {
                    onSelect(it)
                    expanded = false
                })
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
            ActivityType.values().forEach {
                DropdownMenuItem(text = { Text(it.string) }, onClick = {
                    onSelect(it)
                    expanded = false
                })
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
            GoalType.values().forEach {
                DropdownMenuItem(text = { Text(it.string) }, onClick = {
                    onSelect(it)
                    expanded = false
                })
            }
        }
    }
}

fun calculateBMR(gender: GenderType, weight: Float, height: Float, age: Int): Int {
    return (10 * weight + 6.25 * height - 5 * age + gender.k).toInt()
}