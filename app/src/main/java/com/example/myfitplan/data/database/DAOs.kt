package com.example.myfitplan.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDAO {
    @Query("SELECT * FROM User")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM User WHERE email = :email AND password = :password")
    suspend fun login(email: String, password: String): User

    @Query("UPDATE User SET pictureUrl = :pictureUrl WHERE email = :email")
    suspend fun setProfilePicUrl(email: String, pictureUrl: String)

    @Upsert
    suspend fun upsert(user: User)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: User)
}

@Dao
interface FoodDAO {
    @Query("SELECT * FROM Food")
    fun getAllFoods(): Flow<List<Food>>

    @Upsert
    suspend fun upsert(food: Food)

    @Query("SELECT * FROM Food WHERE name = :name AND email = :email")
    suspend fun getFood(name: String, email: String): Food

    @Query("DELETE FROM Food WHERE name = :name AND email = :email")
    suspend fun deleteFood(name: String, email: String)
}

@Dao
interface FoodInsideMealDAO {
    @Transaction
    @Query(
        "SELECT fim.emailFIM, fim.foodName, fim.date, fim.mealType, fim.quantity," +
                " f.email, f.name, f.description, f.kcalPerc, f.carbsPerc, f.proteinPerc, f.fatPerc, f.unit, f.isFavorite " +
                "FROM FoodInsideMeal AS fim " +
                "INNER JOIN Food AS f " +
                "ON fim.foodName = f.name AND fim.emailFIM = f.email " +
                "WHERE fim.date = :date AND fim.mealType = :mealType AND fim.emailFIM = :email"
    )
    fun getFoodInsideMeal(
        date: String,
        mealType: MealType,
        email: String
    ): Flow<List<FoodInsideMealWithFood>>

    @Transaction
    @Query(
        "SELECT fim.*, f.* FROM FoodInsideMeal AS fim " +
                "INNER JOIN Food AS f " +
                "ON fim.foodName = f.name AND fim.emailFIM = f.email"
    )
    fun getFoodInsideAllMeals(): Flow<List<FoodInsideMealWithFood>>

    @Upsert
    suspend fun upsert(foodInsideMeal: FoodInsideMeal)

    @Query("DELETE FROM FoodInsideMeal WHERE date = :date AND mealType = :mealType AND foodName = :foodName AND emailFIM = :email")
    suspend fun removeFoodInsideMeal(date: String, mealType: MealType, foodName: String, email: String)

    @Query("DELETE FROM FoodInsideMeal WHERE foodName = :foodName AND emailFIM = :email")
    suspend fun removeFoodInsideAllMeals(foodName: String, email: String)
}

@Dao
interface ExerciseDAO {
    @Query("SELECT * FROM Exercise")
    fun getAllExercises(): Flow<List<Exercise>>

    // ➜ NUOVO: flow limitata all'utente corrente
    @Query("SELECT * FROM Exercise WHERE email = :email")
    fun getExercisesForUser(email: String): Flow<List<Exercise>>

    @Upsert
    suspend fun upsert(exercise: Exercise)

    @Query("SELECT * FROM Exercise WHERE name = :name AND email = :email")
    suspend fun getExercise(name: String, email: String): Exercise

    @Query("DELETE FROM Exercise WHERE name = :name AND email = :email")
    suspend fun deleteExercise(name: String, email: String)
}

@Dao
interface ExerciseInsideDayDAO {
    @Query("SELECT * FROM ExerciseInsideDay WHERE date = :date AND emailEID = :email")
    fun getExercisesInsideDay(date: String, email: String): Flow<List<ExerciseInsideDay>>

    @Transaction
    @Query(
        "SELECT eid.emailEID, eid.exerciseName, eid.date, eid.duration, " +
                "e.email, e.name, e.description, e.kcalBurned, e.isFavorite, e.category " +
                "FROM ExerciseInsideDay AS eid " +
                "INNER JOIN Exercise AS e " +
                "ON eid.exerciseName = e.name AND eid.emailEID = e.email"
    )
    fun getExercisesInsideAllDays(): Flow<List<ExerciseInsideDayWithExercise>>

    @Upsert
    suspend fun upsert(exerciseInsideDay: ExerciseInsideDay)

    @Query("DELETE FROM ExerciseInsideDay WHERE exerciseName = :exerciseName AND date = :date AND emailEID = :email")
    suspend fun removeExerciseInsideDay(exerciseName: String, date: String, email: String)

    @Query("DELETE FROM ExerciseInsideDay WHERE exerciseName = :exerciseName AND emailEID = :email")
    suspend fun removeExerciseInsideAllDays(exerciseName: String, email: String)
}

@Dao
interface BadgeDAO {
    @Query("SELECT * FROM Badge")
    fun getAllBadges(): Flow<List<Badge>>

    @Upsert
    suspend fun upsert(badge: Badge)

    @Query("DELETE FROM Badge WHERE id = :id")
    suspend fun deletebadge(id: Int)

    @Query("SELECT * FROM Badge WHERE id = :id")
    suspend fun getBadgeId(id: Int): Badge

    @Query("SELECT * FROM Badge WHERE title = :title LIMIT 1")
    suspend fun getByTitle(title: String): Badge?

    @Query("SELECT MAX(id) FROM Badge")
    suspend fun getMaxBadgeId(): Int?
}

@Dao
interface BadgeUserDAO {
    // seleziona id dei badge ottenuti dall'utente
    @Query("SELECT * FROM BadgeUser WHERE email = :email")
    fun getUserBadge(email: String): Flow<List<BadgeUser>>

    // selezione informazioni Badge
    @Transaction
    @Query(
        "SELECT bu.email AS badgeUser_email, bu.badgeId AS badgeUser_badgeId, " +
                "bu.dataAchieved AS badgeUser_dataAchieved, b.id AS badge_id, b.title AS badge_title, " +
                "b.description AS badge_description, b.icon AS badge_icon " +
                "FROM BadgeUser as bu INNER JOIN Badge AS b ON bu.badgeId = b.id " +
                "WHERE bu.email = :email"
    )
    fun getUserBadgeWithInfo(email: String): Flow<List<BadgeWithUserData>>

    // inserimento di un badge ottenuto
    @Upsert
    suspend fun upsert(badgeUser: BadgeUser)

    // rimozione di un badge
    @Query("DELETE FROM BadgeUser WHERE email = :email AND badgeId = :badgeId")
    suspend fun deleteuserBadge(email: String, badgeId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM BadgeUser WHERE email = :email AND badgeId = :badgeId)")
    suspend fun userHasBadge(email: String, badgeId: Int): Int
}

@Dao
interface FastingSessionDAO {
    @Insert
    suspend fun insertSession(session: FastingSession)

    @Query("SELECT * FROM fasting_sessions ORDER BY startTime DESC")
    suspend fun getAllSessions(): List<FastingSession>

    @Query("DELETE FROM fasting_sessions")
    suspend fun deleteAllSessions()

    @Query("DELETE FROM fasting_sessions WHERE id = (SELECT id FROM fasting_sessions ORDER BY startTime ASC LIMIT 1)")
    suspend fun deleteOldestSession()
}

@Dao
interface RouteDAO {
    @Insert
    suspend fun insertRoute(route: Route): Long

    @Insert
    suspend fun insertPoints(points: List<RoutePoint>)

    @Query("SELECT * FROM Route WHERE email = :email ORDER BY id DESC")
    fun getRoutes(email: String): Flow<List<Route>>

    @Query("SELECT * FROM RoutePoint WHERE routeId = :routeId ORDER BY seq ASC")
    suspend fun getRoutePoints(routeId: Long): List<RoutePoint>
}