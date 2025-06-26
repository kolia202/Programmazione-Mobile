package com.example.myfitplan.ui.screens.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.myfitplan.data.database.User
import com.example.myfitplan.data.repositories.MyFitPlanRepositories
import com.example.myfitplan.data.repositories.DatastoreRepository
import com.example.myfitplan.ui.MyFitPlanRoute
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class UserState(val user: User?)

class ProfileViewModel(
    private val repositories: MyFitPlanRepositories,
    private val datastoreRepository: DatastoreRepository
) : ViewModel() {

    var loggedUser by mutableStateOf(UserState(null))
        private set

    fun setProfilePicUrl(email: String, profilePicUrl: String): Job = viewModelScope.launch {
        repositories.setProfilePicUrl(email, profilePicUrl)
        if (loggedUser.user != null) {
            val userCopy = loggedUser.user!!.copy(pictureUrl = profilePicUrl)
            datastoreRepository.saveUser(userCopy)
            loggedUser = UserState(userCopy)
        }
    }

    fun logout(navController: NavHostController): Job = viewModelScope.launch {
        datastoreRepository.removeUser()
        loggedUser = UserState(null)
        navController.navigate(MyFitPlanRoute.Login)
    }

    fun removeProfilePic(email: String): Job = viewModelScope.launch {
        repositories.setProfilePicUrl(email, "")
        if (loggedUser.user != null) {
            val userCopy = loggedUser.user!!.copy(pictureUrl = null)
            datastoreRepository.saveUser(userCopy)
            loggedUser = UserState(userCopy)
        }
    }

    fun refreshUser() = viewModelScope.launch {
        val user = datastoreRepository.user.first()
        loggedUser = UserState(user)
    }

    init {
        viewModelScope.launch {
            val user = datastoreRepository.user.first()
            loggedUser = UserState(user)
        }
    }
}