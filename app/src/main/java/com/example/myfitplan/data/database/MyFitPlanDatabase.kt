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
    ],
    version = 1,
    exportSchema = false
)
abstract class MyFitPlanDatabase : RoomDatabase() {
    abstract fun userDAO(): UserDAO
    abstract fun foodDAO(): FoodDAO
    abstract fun foodInsideMealDAO(): FoodInsideMealDAO
    abstract fun exerciseDAO(): ExerciseDAO
    abstract fun exerciseInsideDayDAO(): ExerciseInsideDayDAO
}