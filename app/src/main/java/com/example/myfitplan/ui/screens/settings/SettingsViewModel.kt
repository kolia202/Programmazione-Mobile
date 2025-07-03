package com.example.myfitplan.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfitplan.data.models.Theme
import com.example.myfitplan.data.repositories.DatastoreRepository
import com.example.myfitplan.data.repositories.MyFitPlanRepositories
import com.example.myfitplan.data.repositories.ThemeRepository
import com.example.myfitplan.data.database.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val theme: Theme = Theme.System,
    val email: String = "",
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val passwordChangeSuccess: Boolean = false,
    val passwordChangeError: String? = null,
    val loading: Boolean = false
)

class SettingsViewModel(
    private val themeRepo: ThemeRepository,
    private val userRepo: MyFitPlanRepositories,
    private val datastore: DatastoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    private var loggedUser: User? = null

    init {
        viewModelScope.launch {
            themeRepo.theme.collect { t ->
                _uiState.update { it.copy(theme = t) }
            }
        }
        viewModelScope.launch {
            datastore.user.collect { user ->
                loggedUser = user
                _uiState.update { it.copy(email = user?.email ?: "") }
            }
        }
    }

    fun onThemeSelected(theme: Theme) {
        viewModelScope.launch { themeRepo.setTheme(theme) }
        _uiState.update { it.copy(theme = theme) }
    }

    fun onCurrentPasswordChange(value: String) {
        _uiState.update { it.copy(currentPassword = value, passwordChangeError = null) }
    }
    fun onNewPasswordChange(value: String) {
        _uiState.update { it.copy(newPassword = value, passwordChangeError = null) }
    }
    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, passwordChangeError = null) }
    }

    private fun isPasswordSecure(password: String): Boolean {
        val regex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*()_+\\-={}:;'<>,.?/~`]).{8,}\$")
        return regex.matches(password)
    }

    fun changePassword() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, passwordChangeError = null, passwordChangeSuccess = false) }
            val user = loggedUser
            when {
                state.currentPassword.isBlank() || state.newPassword.isBlank() || state.confirmPassword.isBlank() -> {
                    _uiState.update { it.copy(passwordChangeError = "All fields are required", loading = false) }
                }
                user == null || user.password != state.currentPassword -> {
                    _uiState.update { it.copy(passwordChangeError = "Current password is incorrect", loading = false) }
                }
                state.newPassword == state.currentPassword -> {
                    _uiState.update { it.copy(passwordChangeError = "New password cannot be the same as the current password", loading = false) }
                }
                !isPasswordSecure(state.newPassword) -> {
                    _uiState.update { it.copy(passwordChangeError = "New password must have at least 8 characters, one uppercase, one lowercase, one number and one symbol", loading = false) }
                }
                state.newPassword != state.confirmPassword -> {
                    _uiState.update { it.copy(passwordChangeError = "Passwords do not match", loading = false) }
                }
                else -> {
                    try {
                        val updated = user.copy(password = state.newPassword)
                        userRepo.updateUser(updated)
                        datastore.saveUser(updated)
                        loggedUser = updated
                        _uiState.update {
                            it.copy(
                                passwordChangeSuccess = true,
                                currentPassword = "",
                                newPassword = "",
                                confirmPassword = "",
                                loading = false
                            )
                        }
                    } catch (e: Exception) {
                        _uiState.update { it.copy(passwordChangeError = "An error occurred while changing the password", loading = false) }
                    }
                }
            }
        }
    }

    fun clearPasswordChangeStatus() {
        _uiState.update { it.copy(passwordChangeSuccess = false, passwordChangeError = null) }
    }
}