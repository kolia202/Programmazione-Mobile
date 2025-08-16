package com.example.myfitplan.ui.screens.badge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfitplan.data.database.Badge
import com.example.myfitplan.data.database.BadgeDAO
import com.example.myfitplan.data.database.BadgeUser
import com.example.myfitplan.data.database.BadgeUserDAO
import com.example.myfitplan.data.database.BadgeWithUserData
import com.example.myfitplan.data.database.MealType
import com.example.myfitplan.data.repositories.MyFitPlanRepositories
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BadgeViewModel(
    private val badgeDao: BadgeDAO,
    private val badgeUserDao: BadgeUserDAO,
    private val repo: MyFitPlanRepositories,
    private val userEmail: String
) : ViewModel() {

    private val FIRST_TITLE = "Primo Accesso"

    private val MEAL_TITLE = "Tutti i pasti"
    private val MEAL_DESC = "Add at least one food to Breakfast, Lunch, Dinner and Snack."
    private val MEAL_ICON = "restaurant"

    private val WORKOUT_TITLE = "Primo Workout"
    private val WORKOUT_DESC = "You completed your first workout!"
    private val WORKOUT_ICON = "dumbbell"

    private val TENKM_TITLE = "10 km"
    private val TENKM_DESC = "Complete a route of at least 10 km for the first time."
    private val TENKM_ICON = "route"

    init {
        viewModelScope.launch {
            ensureMealBadgeSeeded()
            ensureWorkoutBadgeSeeded()
            ensureTenKmBadgeSeeded()
        }
        observeFoodsForMealBadge()
    }

    val allBadges: StateFlow<List<Badge>> =
        badgeDao.getAllBadges().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val userBadges: StateFlow<List<BadgeWithUserData>> =
        badgeUserDao.getUserBadgeWithInfo(userEmail)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val firstBadge: StateFlow<Badge> =
        allBadges.map { list ->
            list.find { it.title.equals(FIRST_TITLE, true) }
                ?: Badge(1, FIRST_TITLE, "You logged in for the first time!", "trophy")
        }.stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            Badge(1, FIRST_TITLE, "You logged in for the first time!", "trophy")
        )

    val earnedFirst: StateFlow<BadgeWithUserData?> =
        userBadges.map { list -> list.find { it.badge.title.equals(FIRST_TITLE, true) } }
            .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val mealBadge: StateFlow<Badge> =
        allBadges.map { list ->
            list.find { it.title.equals(MEAL_TITLE, true) }
                ?: Badge(2, MEAL_TITLE, MEAL_DESC, MEAL_ICON)
        }.stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            Badge(2, MEAL_TITLE, MEAL_DESC, MEAL_ICON)
        )

    val earnedMeal: StateFlow<BadgeWithUserData?> =
        userBadges.map { list -> list.find { it.badge.title.equals(MEAL_TITLE, true) } }
            .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val workoutBadge: StateFlow<Badge> =
        allBadges.map { list ->
            list.find { it.title.equals(WORKOUT_TITLE, true) }
                ?: Badge(3, WORKOUT_TITLE, WORKOUT_DESC, WORKOUT_ICON)
        }.stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            Badge(3, WORKOUT_TITLE, WORKOUT_DESC, WORKOUT_ICON)
        )

    val earnedWorkout: StateFlow<BadgeWithUserData?> =
        userBadges.map { list -> list.find { it.badge.title.equals(WORKOUT_TITLE, true) } }
            .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val tenKmBadge: StateFlow<Badge> =
        allBadges.map { list ->
            list.find { it.title.equals(TENKM_TITLE, true) }
                ?: Badge(4, TENKM_TITLE, TENKM_DESC, TENKM_ICON)
        }.stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            Badge(4, TENKM_TITLE, TENKM_DESC, TENKM_ICON)
        )

    val earnedTenKm: StateFlow<BadgeWithUserData?> =
        userBadges.map { list -> list.find { it.badge.title.equals(TENKM_TITLE, true) } }
            .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun addBadge(badgeId: Int, date: String) {
        viewModelScope.launch {
            badgeUserDao.upsert(BadgeUser(email = userEmail, badgeId = badgeId, dataAchieved = date))
        }
    }

    fun removeBadge(badgeId: Int) {
        viewModelScope.launch {
            badgeUserDao.deleteuserBadge(userEmail, badgeId)
        }
    }

    private suspend fun ensureMealBadgeSeeded(): Badge {
        badgeDao.getByTitle(MEAL_TITLE)?.let { return it }
        val id = (badgeDao.getMaxBadgeId() ?: 0) + 1
        return Badge(id, MEAL_TITLE, MEAL_DESC, MEAL_ICON).also { badgeDao.upsert(it) }
    }

    private suspend fun ensureWorkoutBadgeSeeded(): Badge {
        badgeDao.getByTitle(WORKOUT_TITLE)?.let { return it }
        val id = (badgeDao.getMaxBadgeId() ?: 0) + 1
        return Badge(id, WORKOUT_TITLE, WORKOUT_DESC, WORKOUT_ICON).also { badgeDao.upsert(it) }
    }

    private suspend fun ensureTenKmBadgeSeeded(): Badge {
        badgeDao.getByTitle(TENKM_TITLE)?.let { return it }
        val id = (badgeDao.getMaxBadgeId() ?: 0) + 1
        return Badge(id, TENKM_TITLE, TENKM_DESC, TENKM_ICON).also { badgeDao.upsert(it) }
    }

    private fun observeFoodsForMealBadge() {
        viewModelScope.launch {
            repo.foods.collect { foodsAll ->
                val foods = foodsAll.filter { it.email == userEmail }
                if (foods.isEmpty()) return@collect

                val needed = setOf(
                    MealType.BREAKFAST.string,
                    MealType.LUNCH.string,
                    MealType.DINNER.string,
                    MealType.SNACK.string
                )
                val userCats = foods.map { it.description }.toSet()
                if (needed.all { it in userCats }) {
                    val badge = ensureMealBadgeSeeded()
                    val hasIt = badgeUserDao.userHasBadge(userEmail, badge.id) == 1
                    if (!hasIt) {
                        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        badgeUserDao.upsert(BadgeUser(userEmail, badge.id, today))
                    }
                }
            }
        }
    }
}