package com.example.myfitplan.ui.screens.badge

import androidx.compose.material3.Badge
import com.example.myfitplan.data.database.BadgeDAO

suspend fun initializeWelcomeBadgeIfNeeded(badgeDAO: BadgeDAO){
    val existingBadge = try {
        badgeDAO.getBadgeId(1)
    }catch (e:Exception) {
        null
    }

    if (existingBadge == null){
        badgeDAO.upsert(
            com.example.myfitplan.data.database.Badge(
                id = 1,
                title = "Benvenuto",
                description = "Ti sei registrato!",
                icon = "badge_star.png"
            )
        )
    }
}