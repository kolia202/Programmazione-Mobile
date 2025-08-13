package com.example.myfitplan.ui.screens.badge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfitplan.data.database.Badge
import com.example.myfitplan.data.database.BadgeDAO
import com.example.myfitplan.data.database.BadgeUser
import com.example.myfitplan.data.database.BadgeUserDAO
import com.example.myfitplan.data.database.BadgeWithUserData
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BadgeViewModel(
    private val badgeDao: BadgeDAO,
    private val badgeUserDao: BadgeUserDAO,
    private val userEmail: String
) : ViewModel() {

    private val FIRST_TITLE = "Primo Accesso"

    // Tutti i badge e quelli dell'utente
    val allBadges: StateFlow<List<Badge>> =
        badgeDao.getAllBadges().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val userBadges: StateFlow<List<BadgeWithUserData>> =
        badgeUserDao.getUserBadgeWithInfo(userEmail)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Badge "Primo Accesso" con fallback locale (se non presente a DB)
    val firstBadge: StateFlow<Badge> =
        allBadges.map { list ->
            list.find { it.title.equals(FIRST_TITLE, true) }
                ?: Badge(
                    id = 1,
                    title = FIRST_TITLE,
                    description = "Hai effettuato il tuo primo accesso!",
                    icon = "trophy"
                )
        }.stateIn(
            viewModelScope, SharingStarted.Lazily,
            Badge(1, FIRST_TITLE, "Hai effettuato il tuo primo accesso!", "trophy")
        )

    val earnedFirst: StateFlow<BadgeWithUserData?> =
        userBadges.map { list -> list.find { it.badge.title.equals(FIRST_TITLE, true) } }
            .stateIn(viewModelScope, SharingStarted.Lazily, null)

    // CRUD badge utente (senza notifiche)
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
}