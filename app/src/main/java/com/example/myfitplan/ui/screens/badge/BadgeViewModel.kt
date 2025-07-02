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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BadgeViewModel(
    private val badgeDao: BadgeDAO,
    private val badgeUserDao: BadgeUserDAO,
    private val userEmail: String
) :ViewModel(){
    //lista dei badge
    val allBadges: StateFlow<List<Badge>> = badgeDao.getAllBadges().stateIn(viewModelScope,
        SharingStarted.Lazily, emptyList())

    //badge ottenuti dall'utente
    val userBadges: StateFlow<List<BadgeWithUserData>> = badgeUserDao.getUserBadgeWithInfo(userEmail)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())



    // assegnazione di un badge
    fun addBadge(badgeId: Int, date: String){
        viewModelScope.launch {
            badgeUserDao.upsert(
                BadgeUser(email = userEmail, badgeId = badgeId, dataAchieved = date)
            )
        }
    }

    //rimozione di un badge
    fun removeBadge(badgeId: Int){
        viewModelScope.launch{
            badgeUserDao.deleteuserBadge(userEmail,badgeId)
        }
    }
}

