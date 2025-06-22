package com.example.myfitplan.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfitplan.data.database.MealType
import com.example.myfitplan.data.database.User
import com.example.myfitplan.data.database.FoodInsideMealWithFood
import com.example.myfitplan.data.repositories.MyFitPlanRepositories
import com.example.myfitplan.data.repositories.DatastoreRepository
import com.example.myfitplan.utilities.DateUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

data class MacroSummary(
    val calories: Int = 0,
    val carbs: Int = 0,
    val protein: Int = 0,
    val fat: Int = 0
)

data class HomeUiState(
    val currentDate: String = "",
    val user: User? = null,
    val meals: Map<MealType, List<FoodInsideMealWithFood>> = emptyMap(),
    val summary: MacroSummary = MacroSummary()
)

class HomeViewModel(
    private val repositories: MyFitPlanRepositories,
    private val datastoreRepository: DatastoreRepository
) : ViewModel() {

    private val today = DateUtils.getToday()
    private val _uiState = MutableStateFlow(
        HomeUiState(
            currentDate = DateUtils.formattedDate(today)
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _giornoX = MutableStateFlow(1)
    val giornoX: StateFlow<Int> = _giornoX.asStateFlow()

    init {
        viewModelScope.launch {
            val startDateMillis = datastoreRepository.getOrSetStartDateMillis(today.time)
            val diff = ((today.time - startDateMillis) / (1000 * 60 * 60 * 24)).toInt() + 1
            _giornoX.value = diff
        }
        setDate(today)
        observeUser()
    }

    private fun observeUser() {
        viewModelScope.launch {
            datastoreRepository.user.collect { user ->
                _uiState.update { it.copy(user = user) }
                user?.let { fetchMealsForAllTypes(_uiState.value.currentDate) }
            }
        }
    }

    private fun setDate(date: Date) {
        _uiState.update { it.copy(currentDate = DateUtils.formattedDate(date)) }
        fetchMealsForAllTypes(DateUtils.formattedDate(date))
    }

    private fun fetchMealsForAllTypes(date: String) {
        val user = _uiState.value.user ?: return
        MealType.entries.forEach { mealType ->
            repositories.getFoodsInsideMeal(date, mealType, user.email)
                .onEach { foods ->
                    val mealsMap = _uiState.value.meals.toMutableMap()
                    mealsMap[mealType] = foods
                    _uiState.update {
                        it.copy(
                            meals = mealsMap,
                            summary = computeMacroSummary(mealsMap, user)
                        )
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    private fun computeMacroSummary(
        mealsMap: Map<MealType, List<FoodInsideMealWithFood>>,
        user: User
    ): MacroSummary {
        var calories = 0f
        var carbs = 0f
        var protein = 0f
        var fat = 0f

        mealsMap.values.flatten().forEach { f ->
            val qty = f.foodInsideMeal.quantity
            calories += f.food.kcalPerc * qty
            carbs += f.food.carbsPerc * qty
            protein += f.food.proteinPerc * qty
            fat += f.food.fatPerc * qty
        }
        return MacroSummary(
            calories = calories.toInt(),
            carbs = carbs.toInt(),
            protein = protein.toInt(),
            fat = fat.toInt()
        )
    }

    fun getSelectedDate(): String = DateUtils.formattedDate(DateUtils.getToday())
}