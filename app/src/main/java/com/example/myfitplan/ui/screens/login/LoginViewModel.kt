package com.example.myfitplan.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfitplan.data.database.User
import com.example.myfitplan.data.repositories.DatastoreRepository
import com.example.myfitplan.data.repositories.MyFitPlanRepositories
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LoginState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class LoginViewModel(
    private val repo: MyFitPlanRepositories,
    private val datastore: DatastoreRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = LoginState(isLoading = true)
            try {
                val user: User = repo.login(email, password)
                datastore.saveUser(user)
                _state.value = LoginState(success = true)
            } catch (e: Exception) {
                _state.value = LoginState(error = "Login fallito: ${e.message}")
            }
        }
    }
}