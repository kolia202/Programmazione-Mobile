package com.example.myfitplan.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        User::class,
        Food::class,
        FoodInsideMeal::class,
        Exercise::class,
        ExerciseInsideDay::class,
        StepCounter::class,
        FastingSession::class,
        Badge::class,
        BadgeUser::class
    ],
    version = 6,
    exportSchema = false
)
abstract class MyFitPlanDatabase : RoomDatabase() {
    abstract fun userDAO(): UserDAO
    abstract fun foodDAO(): FoodDAO
    abstract fun foodInsideMealDAO(): FoodInsideMealDAO
    abstract fun exerciseDAO(): ExerciseDAO
    abstract fun exerciseInsideDayDAO(): ExerciseInsideDayDAO
    abstract fun stepCounterDAO(): StepCounterDAO
    abstract fun fastingSessionDAO(): FastingSessionDAO
    abstract fun badgeDAO(): BadgeDAO
    abstract fun badgeUserDAO(): BadgeUserDAO
}