package com.example.myfitplan.ui.screens.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfitplan.data.database.Food
import com.example.myfitplan.data.database.FoodUnit
import com.example.myfitplan.data.repositories.MyFitPlanRepositories
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FoodUiState(
    val allFoods: List<Food> = emptyList(),
    val filteredFoods: List<Food> = emptyList(),
    val favorites: List<Food> = emptyList(),
    val categories: List<String> = listOf("Add", "Breakfast", "Lunch", "Dinner", "Snack"),
    val selectedCategory: String = "Add",
    val searchQuery: String = "",
    val name: String = "",
    val category: String = "Breakfast",
    val kcal: String = "",
    val carbs: String = "",
    val protein: String = "",
    val fat: String = "",
    val unit: FoodUnit = FoodUnit.GRAMS,
    val isFavorite: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)

class FoodViewModel(
    private val repo: MyFitPlanRepositories
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoodUiState())
    val uiState: StateFlow<FoodUiState> = _uiState

    private var userEmail: String = ""

    fun init(email: String) {
        if (userEmail != email) {
            userEmail = email
            observeFoods()
        }
    }

    private fun observeFoods() {
        viewModelScope.launch {
            repo.foods.collect { foodsAll ->
                val foods = foodsAll.filter { it.email == userEmail }
                val favs = foods.filter { it.isFavorite }
                updateState(foods, favs)
            }
        }
    }

    private fun updateState(foods: List<Food>, favs: List<Food>) {
        val filtered = filterFoods(foods)
        _uiState.update {
            it.copy(
                allFoods = foods,
                favorites = favs,
                filteredFoods = filtered
            )
        }
    }

    // Campi aggiunta
    fun onNameChange(name: String) { _uiState.update { it.copy(name = name) } }
    fun onCategoryChange(category: String) { _uiState.update { it.copy(category = category) } }
    fun onKcalChange(kcal: String) { _uiState.update { it.copy(kcal = kcal) } }
    fun onCarbsChange(carbs: String) { _uiState.update { it.copy(carbs = carbs) } }
    fun onProteinChange(protein: String) { _uiState.update { it.copy(protein = protein) } }
    fun onFatChange(fat: String) { _uiState.update { it.copy(fat = fat) } }
    fun onUnitChange(unit: FoodUnit) { _uiState.update { it.copy(unit = unit) } }

    fun selectCategory(cat: String) {
        _uiState.update { it.copy(selectedCategory = cat, searchQuery = "") }
        updateState(_uiState.value.allFoods, _uiState.value.favorites)
    }

    fun onSearch(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        updateState(_uiState.value.allFoods, _uiState.value.favorites)
    }

    private fun filterFoods(foods: List<Food>): List<Food> {
        val cat = _uiState.value.selectedCategory
        val query = _uiState.value.searchQuery.trim().lowercase()
        return when (cat) {
            "Add" -> emptyList()
            else -> foods.filter {
                it.description == cat &&
                        (query.isBlank() || it.name.lowercase().contains(query))
            }
        }
    }

    fun saveFood() {
        viewModelScope.launch {
            val state = _uiState.value
            try {
                val food = Food(
                    email = userEmail,
                    name = state.name.trim(),
                    description = state.category,
                    kcalPerc = state.kcal.toFloatOrNull() ?: 0f,
                    carbsPerc = state.carbs.toFloatOrNull() ?: 0f,
                    proteinPerc = state.protein.toFloatOrNull() ?: 0f,
                    fatPerc = state.fat.toFloatOrNull() ?: 0f,
                    unit = state.unit,
                    isFavorite = false
                )
                repo.upsertFood(food)
                _uiState.update { it.copy(
                    name = "", category = "Breakfast", kcal = "", carbs = "",
                    protein = "", fat = "", unit = FoodUnit.GRAMS,
                    success = true, error = null
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Errore: ${e.localizedMessage}") }
            }
        }
    }

    fun toggleFavorite(food: Food) {
        viewModelScope.launch {
            repo.upsertFood(food.copy(isFavorite = !food.isFavorite))
        }
    }

    fun resetSuccess() {
        _uiState.update { it.copy(success = false) }
    }

    fun deleteFood(food: Food) {
        viewModelScope.launch {
            repo.deleteFood(food.name, food.email)
        }
    }
}