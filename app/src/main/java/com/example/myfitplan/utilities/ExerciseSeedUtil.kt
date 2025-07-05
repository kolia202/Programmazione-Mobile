package com.example.myfitplan.utilities

import com.example.myfitplan.data.database.Exercise
import com.example.myfitplan.data.repositories.MyFitPlanRepositories

suspend fun seedDefaultExercises(repo: MyFitPlanRepositories, userEmail: String) {
    val defaultExercises = listOf(
        Exercise(userEmail, "Squat", "Classic bodyweight or barbell squat.", 90f, false, "Legs"),
        Exercise(userEmail, "Lunges", "Alternating forward lunges.", 80f, false, "Legs"),
        Exercise(userEmail, "Leg Press", "Gym machine for leg press.", 120f, false, "Legs"),
        Exercise(userEmail, "Bench Press", "Barbell bench press on flat bench.", 110f, false, "Chest"),
        Exercise(userEmail, "Dumbbell Flyes", "Dumbbell flyes on flat bench.", 65f, false, "Chest"),
        Exercise(userEmail, "Push Up", "Classic push-ups.", 60f, false, "Chest"),
        Exercise(userEmail, "Shoulder Press", "Overhead dumbbell press.", 70f, false, "Shoulders"),
        Exercise(userEmail, "Lateral Raises", "Side lateral raises with dumbbells.", 40f, false, "Shoulders"),
        Exercise(userEmail, "Arnold Press", "Arnold dumbbell press variation.", 75f, false, "Shoulders"),
        Exercise(userEmail, "Pull Ups", "Wide grip pull ups.", 100f, false, "Back"),
        Exercise(userEmail, "Barbell Row", "Barbell bent-over row.", 90f, false, "Back"),
        Exercise(userEmail, "Lat Pulldown", "Wide grip lat pulldown machine.", 85f, false, "Back"),
        Exercise(userEmail, "Barbell Curl", "Standing barbell curl.", 50f, false, "Biceps"),
        Exercise(userEmail, "Dumbbell Curl", "Alternating dumbbell curl.", 45f, false, "Biceps"),
        Exercise(userEmail, "Hammer Curl", "Hammer curl with dumbbells.", 48f, false, "Biceps"),
        Exercise(userEmail, "Triceps Pushdown", "Cable machine pushdowns.", 55f, false, "Triceps"),
        Exercise(userEmail, "Overhead Extension", "Dumbbell overhead triceps extension.", 50f, false, "Triceps"),
        Exercise(userEmail, "Dips", "Triceps dips on parallel bars.", 70f, false, "Triceps"),
        Exercise(userEmail, "Crunches", "Standard floor crunches.", 35f, false, "Abs"),
        Exercise(userEmail, "Plank", "Forearm plank hold.", 30f, false, "Abs"),
        Exercise(userEmail, "Leg Raises", "Hanging or lying leg raises.", 40f, false, "Abs"),
        Exercise(userEmail, "Jump Rope", "Jumping rope for cardio.", 120f, false, "Other"),
        Exercise(userEmail, "Burpees", "Full body burpees.", 140f, false, "Other"),
        Exercise(userEmail, "Mountain Climbers", "Mountain climber cardio exercise.", 100f, false, "Other"),
        Exercise(userEmail, "Deadlift", "Classic barbell deadlift.", 150f, false, "Back"),
        Exercise(userEmail, "Hip Thrust", "Barbell hip thrust for glutes.", 90f, false, "Legs"),
        Exercise(userEmail, "Russian Twist", "Oblique abs exercise.", 30f, false, "Abs"),
    )
    defaultExercises.forEach { repo.upsertExercise(it) }
}