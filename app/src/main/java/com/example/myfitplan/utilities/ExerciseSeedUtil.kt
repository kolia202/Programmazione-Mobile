package com.example.myfitplan.utilities

import com.example.myfitplan.data.database.Exercise
import com.example.myfitplan.data.repositories.MyFitPlanRepositories

private fun buildDefaultExercises(userEmail: String) = listOf(
    Exercise(userEmail, "Squat",
        "Stand shoulder-width apart, brace your core, and sit back until thighs are parallel to the floor, then drive up through the heels. Targets quads, glutes, and core; keep chest up and knees tracking over toes.",
        90f, false, "Legs"),
    Exercise(userEmail, "Lunges",
        "Step forward and lower until both knees are about 90 degrees, then push back to standing and alternate legs. Emphasizes quads and glutes; keep torso upright and front knee over mid-foot.",
        80f, false, "Legs"),
    Exercise(userEmail, "Leg Press",
        "Set feet mid-platform hip-width apart, unlock the sled, and lower until knees reach about 90 degrees, then press without locking out hard. Focus on full control and even pressure through heels.",
        120f, false, "Legs"),
    Exercise(userEmail, "Bench Press",
        "Lie on a flat bench, grip slightly wider than shoulders, lower the bar to mid-chest with elbows ~45 degrees, then press to full lockout. Keep feet planted, ribs down, and shoulder blades retracted.",
        110f, false, "Chest"),
    Exercise(userEmail, "Dumbbell Flyes",
        "On a flat bench with slight elbow bend, open arms in a wide arc until you feel a stretch across the chest, then squeeze back to the top. Move slowly and avoid over-stretching the shoulders.",
        65f, false, "Chest"),
    Exercise(userEmail, "Push Up",
        "Hands under shoulders, body in a straight line; lower chest near the floor and press back up. Keep core tight and avoid flaring elbows excessively.",
        60f, false, "Chest"),
    Exercise(userEmail, "Shoulder Press",
        "Seated or standing, press dumbbells overhead from shoulder height to full lockout, then lower under control. Keep ribs down and avoid leaning back.",
        70f, false, "Shoulders"),
    Exercise(userEmail, "Lateral Raises",
        "With slight elbow bend, raise dumbbells to shoulder height and pause briefly, then lower slowly. Lead with elbows and avoid shrugging to keep tension on lateral delts.",
        40f, false, "Shoulders"),
    Exercise(userEmail, "Arnold Press",
        "Start with palms facing you at chest level, rotate the wrists outward while pressing overhead, then reverse on the way down. Trains all heads of the deltoids with added rotation.",
        75f, false, "Shoulders"),
    Exercise(userEmail, "Pull Ups",
        "Overhand grip slightly wider than shoulders; pull chest toward the bar and control the descent. Keep legs quiet, drive elbows down, and aim for full range of motion.",
        100f, false, "Back"),
    Exercise(userEmail, "Barbell Row",
        "Hinge at the hips with a flat back, pull the bar to the lower ribs, and lower under control. Keep core braced and avoid jerking the weight.",
        90f, false, "Back"),
    Exercise(userEmail, "Lat Pulldown",
        "Grip the bar wide, pull it to the upper chest while keeping the torso tall, then return with control. Drive elbows down and keep shoulders away from ears.",
        85f, false, "Back"),
    Exercise(userEmail, "Barbell Curl",
        "Stand tall with elbows close to the sides; curl the bar to shoulder height and lower slowly. Avoid swinging and keep wrists neutral.",
        50f, false, "Biceps"),
    Exercise(userEmail, "Dumbbell Curl",
        "Alternate arms; curl with a slight supination of the wrist as you lift, then lower under control. Keep elbows pinned and torso still.",
        45f, false, "Biceps"),
    Exercise(userEmail, "Hammer Curl",
        "Hold dumbbells with a neutral grip and curl to shoulder height, emphasizing the brachialis and forearms. Control the descent and avoid shrugging.",
        48f, false, "Biceps"),
    Exercise(userEmail, "Triceps Pushdown",
        "Using a cable, keep elbows tucked at your sides and extend the forearms until lockout, then return with control. Focus on moving only at the elbow.",
        55f, false, "Triceps"),
    Exercise(userEmail, "Overhead Extension",
        "With a dumbbell held overhead, bend elbows to lower behind the head, then extend to full lockout. Keep ribs down and upper arms steady.",
        50f, false, "Triceps"),
    Exercise(userEmail, "Dips",
        "Support on parallel bars, lower until elbows are about 90 degrees, then press up. Lean slightly forward to hit chest more or stay upright to bias triceps.",
        70f, false, "Triceps"),
    Exercise(userEmail, "Crunches",
        "Lie on your back with knees bent; curl the ribcage toward the pelvis and pause, then lower slowly. Keep lower back lightly pressed to the floor.",
        35f, false, "Abs"),
    Exercise(userEmail, "Plank",
        "Forearms on the floor, body in a straight line from head to heels; brace the core and hold while breathing steadily. Avoid sagging hips or elevated hips.",
        30f, false, "Abs"),
    Exercise(userEmail, "Leg Raises",
        "Hanging or lying, raise straight legs until hips flex fully, then lower without arching the lower back. Keep core engaged and movement controlled.",
        40f, false, "Abs"),
    Exercise(userEmail, "Jump Rope",
        "Maintain tall posture with relaxed shoulders; make small, quick hops on the mid-foot while turning the rope from the wrists. Keep jumps low and rhythmic.",
        120f, false, "Other"),
    Exercise(userEmail, "Burpees",
        "Squat down to place hands, kick to a plank, perform an optional push-up, jump feet back under, and explode upward. Keep the core tight and land softly.",
        140f, false, "Other"),
    Exercise(userEmail, "Mountain Climbers",
        "From a plank, drive knees alternately toward the chest at a steady pace while keeping hips level. Maintain a strong shoulder position and neutral spine.",
        100f, false, "Other"),
    Exercise(userEmail, "Deadlift",
        "Set the bar over mid-foot, hinge at the hips, brace, and stand up by pushing the floor away, then return the bar with control. Keep a neutral spine and the bar close to the body.",
        150f, false, "Back"),
    Exercise(userEmail, "Hip Thrust",
        "Upper back on a bench with bar or weight over hips; drive through heels to full hip extension and squeeze glutes, then lower under control. Keep ribs down and shins vertical at the top.",
        90f, false, "Legs"),
    Exercise(userEmail, "Russian Twist",
        "Seated with chest tall, lean back about 45 degrees and rotate the torso side to side, touching the floor lightly each rep. Keep hips steady and avoid rounding the lower back.",
        30f, false, "Abs"),
)

suspend fun seedDefaultExercises(repo: MyFitPlanRepositories, userEmail: String) {
    buildDefaultExercises(userEmail).forEach { repo.upsertExercise(it) }
}

suspend fun syncDefaultExerciseDescriptions(repo: MyFitPlanRepositories, userEmail: String) {
    val defaults = buildDefaultExercises(userEmail)
    for (def in defaults) {
        val existing = runCatching { repo.getExercise(def.name, userEmail) }.getOrNull()
        if (existing != null) {
            repo.upsertExercise(
                existing.copy(
                    description = def.description,
                    category = def.category
                )
            )
        } else {
            repo.upsertExercise(def)
        }
    }
}
