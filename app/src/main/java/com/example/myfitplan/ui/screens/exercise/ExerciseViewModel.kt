package com.example.myfitplan.ui.screens.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfitplan.data.database.Exercise
import com.example.myfitplan.data.repositories.MyFitPlanRepositories
import com.example.myfitplan.utilities.seedDefaultExercises
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ExerciseUiState(
    val allExercises: List<Exercise> = emptyList(),
    val filteredExercises: List<Exercise> = emptyList(),
    val favorites: List<Exercise> = emptyList(),
    val categories: List<String> = listOf("All", "Legs", "Chest", "Shoulders", "Back", "Biceps", "Triceps", "Abs", "Other"),
    val selectedCategory: String = "All",
    val searchQuery: String = ""
)

class ExerciseViewModel(
    private val repo: MyFitPlanRepositories,
    private val userEmail: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExerciseUiState())
    val uiState: StateFlow<ExerciseUiState> = _uiState

    init {
        viewModelScope.launch {
            val myFlow = repo.getExercisesForUser(userEmail)

            val mineFirst = myFlow.first()
            if (mineFirst.isEmpty()) {
                seedDefaultExercises(repo, userEmail)
            }

            myFlow.collect { mine ->
                updateState(mine)
            }
        }
    }

    private fun updateState(mine: List<Exercise>) {
        val fav = mine.filter { it.isFavorite }
        val filtered = filterAndSearch(mine)
        _uiState.update {
            it.copy(
                allExercises = mine,
                favorites = fav,
                filteredExercises = filtered
            )
        }
    }

    fun selectCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
        updateState(_uiState.value.allExercises)
    }

    fun onSearch(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        updateState(_uiState.value.allExercises)
    }

    private fun filterAndSearch(source: List<Exercise>): List<Exercise> {
        val cat = _uiState.value.selectedCategory
        val query = _uiState.value.searchQuery.trim().lowercase()
        return source.filter {
            (cat == "All" || it.category == cat) &&
                    (query.isBlank() ||
                            it.name.lowercase().contains(query) ||
                            it.description.lowercase().contains(query))
        }
    }

    fun toggleFavorite(exercise: Exercise) {
        viewModelScope.launch {
            repo.upsertExercise(exercise.copy(isFavorite = !exercise.isFavorite))
        }
    }
}