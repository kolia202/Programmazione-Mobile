package com.example.myfitplan.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfitplan.data.database.Badge
import com.example.myfitplan.data.database.BadgeDAO
import com.example.myfitplan.data.database.BadgeUser
import com.example.myfitplan.data.database.BadgeUserDAO
import com.example.myfitplan.data.database.User
import com.example.myfitplan.data.repositories.DatastoreRepository
import com.example.myfitplan.data.repositories.MyFitPlanRepositories
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LoginState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class LoginViewModel(
    private val repo: MyFitPlanRepositories,
    private val datastore: DatastoreRepository,
    private val badgeDao: BadgeDAO,
    private val badgeUserDao: BadgeUserDAO
) : ViewModel() {

    companion object {
        private const val FIRST_ACCESS_TITLE = "Primo Accesso"
        private const val FIRST_ACCESS_DESCRIPTION = "Hai effettuato il tuo primo accesso!"
        private const val FIRST_ACCESS_ICON_KEY = "primo_accesso"
        private val DATE_FMT = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    }

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = LoginState(isLoading = true)
            try {
                val user: User = repo.login(email, password)
                datastore.saveUser(user)
                // assegna/crea badge "Primo Accesso" (senza alcuna notifica)
                grantFirstAccessBadge(user.email)
                _state.value = LoginState(success = true)
            } catch (e: Exception) {
                _state.value = LoginState(error = "Invalid credentials or user does not exist")
            }
        }
    }

    private suspend fun grantFirstAccessBadge(email: String) = withContext(Dispatchers.IO) {
        // 1) Cerca il badge per titolo; se non esiste, crealo (id = maxId+1)
        val existing = runCatching { badgeDao.getByTitle(FIRST_ACCESS_TITLE) }.getOrNull()
        val badge = existing ?: run {
            val newId = ((badgeDao.getMaxBadgeId() ?: 0) + 1)
            val created = Badge(
                id = newId,
                title = FIRST_ACCESS_TITLE,
                description = FIRST_ACCESS_DESCRIPTION,
                icon = FIRST_ACCESS_ICON_KEY
            )
            badgeDao.upsert(created)
            created
        }

        // 2) Se l’utente non lo ha, assegnalo
        val hasIt = runCatching { badgeUserDao.userHasBadge(email, badge.id) }.getOrNull() == 1
        if (!hasIt) {
            badgeUserDao.upsert(
                BadgeUser(
                    email = email,
                    badgeId = badge.id,
                    dataAchieved = DATE_FMT.format(Date())
                )
            )
        }
    }
}