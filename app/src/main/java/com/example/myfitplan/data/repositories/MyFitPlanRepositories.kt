package com.example.myfitplan.data.repositories

import com.example.myfitplan.data.database.Exercise
import com.example.myfitplan.data.database.ExerciseDAO
import com.example.myfitplan.data.database.ExerciseInsideDay
import com.example.myfitplan.data.database.ExerciseInsideDayDAO
import com.example.myfitplan.data.database.ExerciseInsideDayWithExercise
import com.example.myfitplan.data.database.FastingSession
import com.example.myfitplan.data.database.FastingSessionDAO
import com.example.myfitplan.data.database.Food
import com.example.myfitplan.data.database.FoodDAO
import com.example.myfitplan.data.database.FoodInsideMeal
import com.example.myfitplan.data.database.FoodInsideMealDAO
import com.example.myfitplan.data.database.FoodInsideMealWithFood
import com.example.myfitplan.data.database.MealType
import com.example.myfitplan.data.database.StepCounter
import com.example.myfitplan.data.database.StepCounterDAO
import com.example.myfitplan.data.database.User
import com.example.myfitplan.data.database.UserDAO
import kotlinx.coroutines.flow.Flow

class MyFitPlanRepositories(
    private val userDAO: UserDAO,
    private val foodDAO: FoodDAO,
    private val foodInsideMealDAO: FoodInsideMealDAO,
    private val exerciseDAO: ExerciseDAO,
    private val exerciseInsideDayDAO: ExerciseInsideDayDAO,
    private val stepCounterDAO: StepCounterDAO,
    val fastingSessionDAO: FastingSessionDAO // <-- aggiungi il DAO del digiuno!
) {

    /* Users */
    val users: Flow<List<User>> = userDAO.getAllUsers()
    suspend fun insertUser(user: User) = userDAO.insert(user)
    suspend fun updateUser(user: User) = userDAO.upsert(user)
    suspend fun login(email: String, password: String) = userDAO.login(email, password)
    suspend fun setProfilePicUrl(email: String, pictureUrl: String) = userDAO.setProfilePicUrl(email, pictureUrl)

    /* Food */
    val foods: Flow<List<Food>> = foodDAO.getAllFoods()
    suspend fun upsertFood(food: Food) = foodDAO.upsert(food)
    suspend fun getFood(name: String, email: String): Food = foodDAO.getFood(name, email)
    suspend fun deleteFood(name: String, email: String) {
        foodDAO.deleteFood(name, email)
        foodInsideMealDAO.removeFoodInsideAllMeals(name, email)
    }

    /* FoodInsideMeal */
    val foodInsideAllMeals: Flow<List<FoodInsideMealWithFood>> = foodInsideMealDAO.getFoodInsideAllMeals()
    fun getFoodsInsideMeal(date: String, mealType: MealType, email: String): Flow<List<FoodInsideMealWithFood>> =
        foodInsideMealDAO.getFoodInsideMeal(date, mealType, email)
    suspend fun upsertFoodInsideMeal(item: FoodInsideMeal) = foodInsideMealDAO.upsert(item)
    suspend fun deleteFoodInsideMeal(date: String, mealType: MealType, foodName: String, email: String) =
        foodInsideMealDAO.removeFoodInsideMeal(date, mealType, foodName, email)

    /* Exercises */
    val exercises: Flow<List<Exercise>> = exerciseDAO.getAllExercises()
    suspend fun upsertExercise(exercise: Exercise) = exerciseDAO.upsert(exercise)
    suspend fun getExercise(name: String, email: String): Exercise = exerciseDAO.getExercise(name, email)
    suspend fun deleteExercise(name: String, email: String) {
        exerciseDAO.deleteExercise(name, email)
        exerciseInsideDayDAO.removeExerciseInsideAllDays(name, email)
    }

    /* ExerciseInsideDay */
    val exercisesInsideAllDays: Flow<List<ExerciseInsideDayWithExercise>> =
        exerciseInsideDayDAO.getExercisesInsideAllDays()
    fun getExercisesInsideDay(date: String, email: String): Flow<List<ExerciseInsideDay>> =
        exerciseInsideDayDAO.getExercisesInsideDay(date, email)
    suspend fun upsertExerciseInsideDay(item: ExerciseInsideDay) = exerciseInsideDayDAO.upsert(item)
    suspend fun deleteExerciseInsideDay(item: ExerciseInsideDay) =
        exerciseInsideDayDAO.removeExerciseInsideDay(item.exerciseName, item.date, item.emailEID)

    fun getSteps(email: String, date: String) = stepCounterDAO.getSteps(email, date)
    suspend fun upsertSteps(stepCounter: StepCounter) = stepCounterDAO.upsert(stepCounter)

    suspend fun saveFastingSession(session: FastingSession) = fastingSessionDAO.insertSession(session)
    suspend fun getAllFastingSessions() = fastingSessionDAO.getAllSessions()
    suspend fun clearFastingSessions() = fastingSessionDAO.deleteAllSessions()
    suspend fun saveFastingSessionFifo(session: FastingSession) {
        val all = fastingSessionDAO.getAllSessions()
        if (all.size >= 5) {
            fastingSessionDAO.deleteOldestSession()
        }
        fastingSessionDAO.insertSession(session)
    }
}