package com.example.myfitplan.ui.screens.editProfile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfitplan.data.database.*
import com.example.myfitplan.data.repositories.MyFitPlanRepositories
import com.example.myfitplan.data.repositories.DatastoreRepository
import com.example.myfitplan.ui.screens.profile.UserState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val repositories: MyFitPlanRepositories,
    private val datastoreRepository: DatastoreRepository
) : ViewModel() {
    var loggedUser by mutableStateOf(UserState(null))
        private set

    var editState by mutableStateOf(EditProfileState())
        private set

    init {
        viewModelScope.launch {
            val user = datastoreRepository.user.first()
            loggedUser = UserState(user)
            user?.let {
                editState = editState.copy(
                    height = it.height.toString(),
                    weight = it.weight.toString(),
                    gender = it.gender,
                    age = it.age.toString(),
                    activity = it.activityLevel,
                    goal = it.goal
                )
            }
        }
    }

    fun onValueChange(update: EditProfileState.() -> EditProfileState) {
        editState = editState.update()
    }

    fun saveProfile(onSaved: () -> Unit) = viewModelScope.launch {
        val user = loggedUser.user ?: return@launch
        val heightF = editState.height.toFloatOrNull() ?: user.height
        val weightF = editState.weight.toFloatOrNull() ?: user.weight
        val ageI = editState.age.toIntOrNull() ?: user.age

        val tempBMR = calculateBMR(editState.gender, weightF, heightF, ageI)
        val dailyCalories = calculateDailyCalories(tempBMR, editState.activity.k, editState.goal.k)

        val userCopy = user.copy(
            height = heightF,
            weight = weightF,
            gender = editState.gender,
            age = ageI,
            activityLevel = editState.activity,
            goal = editState.goal,
            bmr = tempBMR,
            dailyCalories = dailyCalories,
            pictureUrl = user.pictureUrl
        )
        repositories.updateUser(userCopy)
        datastoreRepository.saveUser(userCopy)
        loggedUser = UserState(userCopy)
        onSaved()
    }

    private fun calculateBMR(gender: GenderType, weight: Float, height: Float, age: Int): Int {
        return (10 * weight + 6.25 * height - 5 * age + gender.k).toInt()
    }

    private fun calculateDailyCalories(bmr: Int, activityK: Float, goalK: Float): Int {
        return (bmr * activityK * goalK).toInt()
    }
}

data class EditProfileState(
    val height: String = "",
    val weight: String = "",
    val gender: GenderType = GenderType.OTHER,
    val age: String = "",
    val activity: ActivityType = ActivityType.SEDENTARY,
    val goal: GoalType = GoalType.MAINTAIN_WEIGHT
)