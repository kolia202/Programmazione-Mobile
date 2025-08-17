package com.example.myfitplan.ui.screens.signUp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfitplan.data.database.*
import com.example.myfitplan.data.repositories.MyFitPlanRepositories
import com.example.myfitplan.data.repositories.DatastoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class SignUpUiState(
    val email: String = "",
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val gender: GenderType = GenderType.OTHER,
    val age: String = "",
    val weight: String = "",
    val height: String = "",
    val activity: ActivityType = ActivityType.SEDENTARY,
    val goal: GoalType = GoalType.MAINTAIN_WEIGHT,
    val error: String? = null,
    val success: Boolean = false
)

class SignUpViewModel(
    private val repo: MyFitPlanRepositories,
    private val datastore: DatastoreRepository,
    private val badgeUserDAO: BadgeUserDAO,
    private val badgeDao: BadgeDAO
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState

    fun onFieldChange(field: (SignUpUiState) -> SignUpUiState) {
        _uiState.value = field(_uiState.value)
    }

    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isPasswordSecure(password: String): Boolean {
        val regex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*()_+\\-={}:;'<>,.?/~`]).{8,}\$")
        return regex.matches(password)
    }

    fun signUp() {
        val state = _uiState.value
        if (state.email.isBlank() || state.username.isBlank() || state.password.isBlank() || state.confirmPassword.isBlank() ||
            state.age.isBlank() || state.weight.isBlank() || state.height.isBlank()
        ) {
            _uiState.value = state.copy(error = "All fields are required")
            return
        }
        if (!isEmailValid(state.email)) {
            _uiState.value = state.copy(error = "Invalid email format")
            return
        }
        if (!isPasswordSecure(state.password)) {
            _uiState.value = state.copy(error = "Password must be at least 8 characters and include upper, lower, number and special character")
            return
        }
        if (state.password != state.confirmPassword) {
            _uiState.value = state.copy(error = "Passwords do not match")
            return
        }
        viewModelScope.launch {
            try {
                val existingUsers = repo.users.first()
                val alreadyRegistered = existingUsers.any { it.email == state.email }
                if (alreadyRegistered) {
                    _uiState.value = state.copy(error = "Email already registered")
                    return@launch
                }
                val bmr = calculateBMR(state.gender, state.weight.toFloat(), state.height.toFloat(), state.age.toInt())
                val dailyCalories = (bmr * state.activity.k * state.goal.k).toInt()
                val user = User(
                    email = state.email,
                    password = state.password,
                    username = state.username,
                    pictureUrl = null,
                    height = state.height.toFloat(),
                    weight = state.weight.toFloat(),
                    gender = state.gender,
                    age = state.age.toInt(),
                    activityLevel = state.activity,
                    goal = state.goal,
                    bmr = bmr,
                    dailyCalories = dailyCalories,
                    diet = DietType.STANDARD
                )
                repo.insertUser(user)
                datastore.saveUser(user)
                _uiState.value = state.copy(success = true, error = null)
            } catch (e: Exception) {
                _uiState.value = state.copy(error = "Registration failed: ${e.message}")
            }
        }
    }

    private fun calculateBMR(gender: GenderType, weight: Float, height: Float, age: Int): Int {
        return (10 * weight + 6.25 * height - 5 * age + gender.k).toInt()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}