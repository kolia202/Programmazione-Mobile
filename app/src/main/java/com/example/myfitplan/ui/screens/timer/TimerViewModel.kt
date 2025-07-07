package com.example.myfitplan.ui.screens.timer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfitplan.R
import com.example.myfitplan.data.database.Exercise
import com.example.myfitplan.data.database.FastingSession
import com.example.myfitplan.data.repositories.DatastoreRepository
import com.example.myfitplan.data.repositories.MyFitPlanRepositories
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TimerUiState(
    val isRunning: Boolean = false,
    val elapsedMillis: Long = 0L,
    val durationMillis: Long = 0L,
    val sessionHistory: List<FastingSession> = emptyList(),
    val showDialog: Boolean = false
)

class TimerViewModel(
    private val repo: MyFitPlanRepositories,
    private val datastore: DatastoreRepository,
    app: Application
) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState

    private var startTime: Long = 0L
    private var timerJobLaunched = false

    init {
        viewModelScope.launch {
            loadTimerState()
            loadHistory()
        }
    }

    fun setDuration(hours: Int, minutes: Int, seconds: Int) {
        if (_uiState.value.isRunning || _uiState.value.elapsedMillis > 0L) return
        val durationMillis = (hours * 60 * 60 * 1000L) + (minutes * 60 * 1000L) + (seconds * 1000L)
        _uiState.value = _uiState.value.copy(durationMillis = durationMillis)
        saveTimerState()
    }

    fun startTimer() {
        if (_uiState.value.isRunning) return
        startTime = System.currentTimeMillis() - _uiState.value.elapsedMillis
        _uiState.value = _uiState.value.copy(isRunning = true)
        saveTimerState()
        if (!timerJobLaunched) {
            timerJobLaunched = true
            viewModelScope.launch {
                while (_uiState.value.isRunning) {
                    val elapsed = System.currentTimeMillis() - startTime
                    if (elapsed >= _uiState.value.durationMillis) {
                        stopTimer()
                        showNotification()
                        break
                    } else {
                        _uiState.value = _uiState.value.copy(elapsedMillis = elapsed)
                    }
                    saveTimerState()
                    delay(1000L)
                }
                timerJobLaunched = false
            }
        }
    }

    fun pauseTimer() {
        _uiState.value = _uiState.value.copy(isRunning = false)
        saveTimerState()
    }

    fun resetTimer() {
        _uiState.value = _uiState.value.copy(isRunning = false, elapsedMillis = 0L, durationMillis = 0L)
        saveTimerState()
    }

    fun stopTimer() {
        val now = System.currentTimeMillis()
        val elapsed = now - startTime
        val session = FastingSession(
            startTime = startTime,
            endTime = now,
            durationMillis = elapsed
        )
        viewModelScope.launch {
            repo.saveFastingSessionFifo(session)
            loadHistory()
        }
        _uiState.value = _uiState.value.copy(
            isRunning = false,
            elapsedMillis = _uiState.value.durationMillis,
            showDialog = true
        )
        saveTimerState()
        clearTimerState()
    }

    private suspend fun loadHistory() {
        val history = repo.getAllFastingSessions()
        _uiState.value = _uiState.value.copy(sessionHistory = history)
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(showDialog = false)
    }

    private fun showNotification() {
        val context = getApplication<Application>()
        val channelId = "workout_timer"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Workout Timer",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Workout completed!")
            .setContentText("Congratulations, you have finished your workout timer.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
        manager.notify(2222, notification)
    }

    private fun saveTimerState() {
        viewModelScope.launch {
            datastore.saveLong("workout_timer_start", startTime)
            datastore.saveLong("workout_timer_elapsed", _uiState.value.elapsedMillis)
            datastore.saveLong("workout_timer_duration", _uiState.value.durationMillis)
            datastore.saveBoolean("workout_timer_running", _uiState.value.isRunning)
        }
    }

    private suspend fun loadTimerState() {
        startTime = datastore.getLong("workout_timer_start") ?: 0L
        val elapsed = datastore.getLong("workout_timer_elapsed") ?: 0L
        val duration = datastore.getLong("workout_timer_duration") ?: 0L
        val isRunning = datastore.getBoolean("workout_timer_running") ?: false
        _uiState.value = _uiState.value.copy(
            elapsedMillis = elapsed,
            durationMillis = duration,
            isRunning = isRunning
        )
        if (isRunning) startTimer()
    }

    private fun clearTimerState() {
        viewModelScope.launch {
            datastore.remove("workout_timer_start")
            datastore.remove("workout_timer_elapsed")
            datastore.remove("workout_timer_duration")
            datastore.remove("workout_timer_running")
        }
    }

    var selectedExercises by mutableStateOf<List<Exercise>>(emptyList())

    fun addExerciseToPlan(ex: Exercise) {
        selectedExercises = selectedExercises + ex
    }
    fun removeExerciseFromPlan(ex: Exercise) {
        selectedExercises = selectedExercises - ex
    }
    fun clearPlan() {
        selectedExercises = emptyList()
    }
}