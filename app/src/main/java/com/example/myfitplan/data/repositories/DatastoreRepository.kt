package com.example.myfitplan.data.repositories

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.myfitplan.data.database.ActivityType
import com.example.myfitplan.data.database.DietType
import com.example.myfitplan.data.database.GenderType
import com.example.myfitplan.data.database.GoalType
import com.example.myfitplan.data.database.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DatastoreRepository(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val EMAIL_KEY = stringPreferencesKey("email")
        private val PASSWORD_KEY = stringPreferencesKey("password")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val PICTURE_URL_KEY = stringPreferencesKey("pictureUrl")
        private val HEIGHT_KEY = stringPreferencesKey("height")
        private val WEIGHT_KEY = stringPreferencesKey("weight")
        private val GENDER_KEY = stringPreferencesKey("gender")
        private val AGE_KEY = stringPreferencesKey("age")
        private val ACTIVITY_KEY = stringPreferencesKey("activityLevel")
        private val GOAL_KEY = stringPreferencesKey("goal")
        private val BMR_KEY = stringPreferencesKey("bmr")
        private val DAILY_CALORIES_KEY = stringPreferencesKey("dailyCalories")
        private val DIET_TYPE_KEY = stringPreferencesKey("diet")
        private val B1_KEY = stringPreferencesKey("b1")
        private val B2_KEY = stringPreferencesKey("b2")
        private val B3_KEY = stringPreferencesKey("b3")
        private val B4_KEY = stringPreferencesKey("b4")
        private val B5_KEY = stringPreferencesKey("b5")
        private val B6_KEY = stringPreferencesKey("b6")
        private val SELECTED_DATE_MILLIS_KEY = stringPreferencesKey("selectedDateMillis")
        private val STEP_GOAL_KEY = stringPreferencesKey("stepGoal")
    }

    val user: Flow<User?> = dataStore.data.map { prefs ->
        prefs[EMAIL_KEY]?.let {
            try {
                User(
                    email = it,
                    password = prefs[PASSWORD_KEY] ?: "",
                    username = prefs[USERNAME_KEY] ?: "",
                    pictureUrl = prefs[PICTURE_URL_KEY]?.takeIf { url -> url.isNotBlank() },
                    height = prefs[HEIGHT_KEY]?.toFloat() ?: 0f,
                    weight = prefs[WEIGHT_KEY]?.toFloat() ?: 0f,
                    gender = GenderType.valueOf(prefs[GENDER_KEY] ?: "OTHER"),
                    age = prefs[AGE_KEY]?.toInt() ?: 0,
                    activityLevel = ActivityType.valueOf(prefs[ACTIVITY_KEY] ?: "SEDENTARY"),
                    goal = GoalType.valueOf(prefs[GOAL_KEY] ?: "MAINTAIN_WEIGHT"),
                    bmr = prefs[BMR_KEY]?.toInt() ?: 0,
                    dailyCalories = prefs[DAILY_CALORIES_KEY]?.toInt() ?: 0,
                    diet = DietType.valueOf(prefs[DIET_TYPE_KEY] ?: "STANDARD"),
                    b1 = prefs[B1_KEY]?.toBoolean() ?: false,
                    b2 = prefs[B2_KEY]?.toBoolean() ?: false,
                    b3 = prefs[B3_KEY]?.toBoolean() ?: false,
                    b4 = prefs[B4_KEY]?.toBoolean() ?: false,
                    b5 = prefs[B5_KEY]?.toBoolean() ?: false,
                    b6 = prefs[B6_KEY]?.toBoolean() ?: false
                )
            } catch (e: Exception) {
                Log.e("DatastoreRepository", "Error parsing user prefs", e)
                null
            }
        }
    }

    suspend fun saveUser(user: User) = dataStore.edit { prefs ->
        prefs[EMAIL_KEY] = user.email
        prefs[PASSWORD_KEY] = user.password
        prefs[USERNAME_KEY] = user.username
        prefs[PICTURE_URL_KEY] = user.pictureUrl.orEmpty()
        prefs[HEIGHT_KEY] = user.height.toString()
        prefs[WEIGHT_KEY] = user.weight.toString()
        prefs[GENDER_KEY] = user.gender.name
        prefs[AGE_KEY] = user.age.toString()
        prefs[ACTIVITY_KEY] = user.activityLevel.name
        prefs[GOAL_KEY] = user.goal.name
        prefs[BMR_KEY] = user.bmr.toString()
        prefs[DAILY_CALORIES_KEY] = user.dailyCalories.toString()
        prefs[DIET_TYPE_KEY] = user.diet.name
        prefs[B1_KEY] = user.b1.toString()
        prefs[B2_KEY] = user.b2.toString()
        prefs[B3_KEY] = user.b3.toString()
        prefs[B4_KEY] = user.b4.toString()
        prefs[B5_KEY] = user.b5.toString()
        prefs[B6_KEY] = user.b6.toString()
    }

    suspend fun removeUser() = dataStore.edit { prefs ->
        prefs.clear()
    }

    suspend fun getOrSetStartDateMillis(todayMillis: Long): Long {
        val key = stringPreferencesKey("first_open_date_millis")
        val prefs = dataStore.data.first()
        val current = prefs[key]
        if (current != null) return current.toLong()
        dataStore.edit { it[key] = todayMillis.toString() }
        return todayMillis
    }

    val stepGoal: Flow<Int> = dataStore.data.map { prefs -> prefs[STEP_GOAL_KEY]?.toIntOrNull() ?: 1000 }
    suspend fun setStepGoal(goal: Int) = dataStore.edit { it[STEP_GOAL_KEY] = goal.toString() }
}