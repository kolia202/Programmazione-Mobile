package com.example.myfitplan.data.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey val email: String,
    val password: String,
    val username: String,
    val pictureUrl: String?,
    val height: Float,
    val weight: Float,
    val gender: GenderType,
    val age: Int,
    val activityLevel: ActivityType,
    val goal: GoalType,
    val bmr: Int,
    val dailyCalories: Int,
    val diet: DietType,
    var b1: Boolean = false,
    var b2: Boolean = false,
    var b3: Boolean = false,
    var b4: Boolean = false,
    var b5: Boolean = false,
    var b6: Boolean = false
)

enum class GenderType(val string: String, val k: Int) {
    MALE("Male", 5),
    FEMALE("Female", -161),
    OTHER("Other", -78)
}

enum class ActivityType(val string: String, val description: String, val k: Float) {
    SEDENTARY("Sedentary", "Little or no exercise", 1.2f),
    LIGHT("Light", "Light exercise (1-3 days/week)", 1.375f),
    MODERATE("Moderate", "Moderate exercise (3-5 days/week)", 1.55f),
    ACTIVE("Active", "Hard exercise (6-7 days/week)", 1.725f)
}

enum class GoalType(val string: String, val k: Float) {
    LOSE_WEIGHT("Lose Weight", 0.8f),
    MAINTAIN_WEIGHT("Maintain Weight", 1f),
    GAIN_WEIGHT("Gain Weight", 1.2f)
}

enum class DietType(val string: String, val carbsPerc: Float, val proteinPerc: Float, val fatPerc: Float) {
    STANDARD("Standard", 0.50f, 0.20f, 0.25f),
    BALANCED("Balanced", 0.50f, 0.25f, 0.25f),
    LOW_FAT("Low Fat", 0.60f, 0.25f, 0.15f),
    HIGH_PROTEIN("High Protein", 0.25f, 0.40f, 0.35f)
}

@Entity(primaryKeys = ["email", "name"])
data class Food(
    val email: String,
    val name: String,
    val description: String,
    val kcalPerc: Float,
    val carbsPerc: Float,
    val proteinPerc: Float,
    val fatPerc: Float,
    val unit: FoodUnit,
    val isFavorite: Boolean
)

enum class FoodUnit(val string: String) {
    GRAMS("g"),
    MILLILITERS("ml")
}

@Entity(primaryKeys = ["emailFIM", "foodName", "date", "mealType"])
data class FoodInsideMeal(
    val emailFIM: String,
    val foodName: String,
    val date: String,
    val mealType: MealType,
    val quantity: Float
)

enum class MealType(val string: String) {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner"),
    SNACK("Snack")
}

@Entity(primaryKeys = ["email", "name"])
data class Exercise(
    val email: String,
    val name: String,
    val description: String,
    val kcalBurned: Float,
    val isFavorite: Boolean
)

@Entity(primaryKeys = ["emailEID", "exerciseName", "date"])
data class ExerciseInsideDay(
    val emailEID: String,
    val exerciseName: String,
    val date: String,
    val duration: Int
)

data class FoodInsideMealWithFood(
    @Embedded val foodInsideMeal: FoodInsideMeal,
    @Embedded val food: Food
)

data class ExerciseInsideDayWithExercise(
    @Embedded val exerciseInsideDay: ExerciseInsideDay,
    @Embedded val exercise: Exercise
)
//visualizzazione di badge legati all'utente
data class BadgeWithUserData(
    @Embedded val badgeUser: BadgeUser,
    @Embedded val badge: Badge
)

@Entity(primaryKeys = ["email", "date"])
data class StepCounter(
    val email: String,
    val date: String,
    val steps: Int,
    val goal: Int = 1000
)
//tabella di memorizzazione dei Badge
@Entity(primaryKeys = ["id"])
data class Badge(
    val id : Int,
    val title: String,
    val description: String,
    val icon: String
)
//tabella che lega badge e Email
@Entity(
    primaryKeys = ["email" , "badgeId"],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["email"],
            childColumns = ["email"],
            onDelete = ForeignKey.CASCADE
        ),
    ForeignKey(
        entity = Badge::class,
        parentColumns = ["id"],
        childColumns = ["badgeId"],
        onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BadgeUser(
    val email: String,
    val badgeId: Int,
    val dataAchieved: String
)

@Entity(tableName = "fasting_sessions")
data class FastingSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long,
    val endTime: Long,
    val durationMillis: Long
)
