package com.example.myfitplan.ui.screens.timer

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.CalendarContract
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myfitplan.data.database.Exercise
import com.example.myfitplan.data.database.ExerciseInsideDay
import com.example.myfitplan.data.database.FastingSession
import com.example.myfitplan.data.repositories.MyFitPlanRepositories
import com.example.myfitplan.ui.MyFitPlanRoute
import com.example.myfitplan.ui.composables.NavBar
import com.example.myfitplan.ui.composables.NavBarItem
import com.example.myfitplan.ui.composables.TopBar
import com.example.myfitplan.utilities.rememberPermission
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

@Composable
fun TimerScreen(
    navController: NavController,
    viewModel: TimerViewModel,
    repo: MyFitPlanRepositories,
    userEmail: String
) {
    val state by viewModel.uiState.collectAsState()
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val allExercises by repo.exercises.collectAsState(initial = emptyList())
    var selectedExercises by remember { mutableStateOf<List<Exercise>>(emptyList()) }

    var selectedTab by remember { mutableStateOf(NavBarItem.Digiuno) }
    var selectedHours by remember { mutableStateOf(0) }
    var selectedMinutes by remember { mutableStateOf(0) }
    var selectedSeconds by remember { mutableStateOf(0) }
    val canChangeDuration = !state.isRunning && state.elapsedMillis == 0L
    val totalDurationZero = (selectedHours == 0 && selectedMinutes == 0 && selectedSeconds == 0)

    LaunchedEffect(state.durationMillis, state.elapsedMillis, state.isRunning) {
        if ((state.durationMillis == 0L && state.elapsedMillis == 0L && !state.isRunning) ||
            (state.elapsedMillis == state.durationMillis && state.durationMillis > 0 && !state.isRunning)
        ) {
            selectedHours = 0
            selectedMinutes = 0
            selectedSeconds = 0
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notificationPermission = rememberPermission(Manifest.permission.POST_NOTIFICATIONS)
        LaunchedEffect(Unit) { notificationPermission.launchPermissionRequest() }
    }

    Scaffold(
        topBar = {
            TopBar(
                onProfileClick = { navController.navigate(MyFitPlanRoute.Profile) },
                onPieChartClick = { navController.navigate(MyFitPlanRoute.Badge) }
            )
        },
        bottomBar = {
            NavBar(
                selected = selectedTab,
                onItemSelected = {
                    selectedTab = it
                    when (it) {
                        NavBarItem.Home -> navController.navigate(MyFitPlanRoute.Home)
                        NavBarItem.Digiuno -> navController.navigate(MyFitPlanRoute.FastingTimer)
                        NavBarItem.Esercizi -> navController.navigate(MyFitPlanRoute.Exercise)
                        NavBarItem.Ristoranti -> navController.navigate(MyFitPlanRoute.Food)
                    }
                },
                navController = navController
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            Text(
                "Workout Timer",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = colors.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                textAlign = TextAlign.Center
            )
            Text(
                if (!state.isRunning && state.elapsedMillis == 0L)
                    "Set your workout duration"
                else
                    "Timer in progress",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.secondary,
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colors.surface
                )
            ) {
                Column(
                    Modifier.padding(vertical = 28.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = formatSessionDuration(
                            max(
                                0L,
                                state.durationMillis - state.elapsedMillis
                            )
                        ),
                        fontSize = 48.sp,
                        color = colors.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                    )
                    LinearProgressIndicator(
                        progress = {
                            if (state.durationMillis > 0)
                                min(
                                    1f,
                                    state.elapsedMillis.toFloat() / state.durationMillis
                                )
                            else 0f
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .padding(vertical = 8.dp)
                            .progressSemantics(),
                        color = colors.primary,
                        trackColor = colors.secondaryContainer,
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { viewModel.startTimer() },
                            enabled = !state.isRunning && !totalDurationZero,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = colors.onPrimary
                            )
                        ) { Text("Start") }
                        Button(
                            onClick = { viewModel.pauseTimer() },
                            enabled = state.isRunning,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.secondaryContainer,
                                contentColor = colors.primary
                            )
                        ) { Text("Pause") }
                        Button(
                            onClick = {
                                selectedHours = 0
                                selectedMinutes = 0
                                selectedSeconds = 0
                                viewModel.resetTimer()
                            },
                            enabled = state.elapsedMillis > 0L || state.durationMillis > 0L,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.secondaryContainer,
                                contentColor = colors.primary
                            )
                        ) { Text("Reset") }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Set duration",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = colors.primary
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    NumberStepper(
                        label = "Hours",
                        value = selectedHours,
                        min = 0,
                        max = 23,
                        enabled = canChangeDuration,
                        onValueChange = {
                            selectedHours = it
                            viewModel.setDuration(selectedHours, selectedMinutes, selectedSeconds)
                        },
                        colors = colors
                    )
                    Spacer(Modifier.width(10.dp))
                    NumberStepper(
                        label = "Minutes",
                        value = selectedMinutes,
                        min = 0,
                        max = 59,
                        enabled = canChangeDuration,
                        onValueChange = {
                            selectedMinutes = it
                            viewModel.setDuration(selectedHours, selectedMinutes, selectedSeconds)
                        },
                        colors = colors
                    )
                    Spacer(Modifier.width(10.dp))
                    NumberStepper(
                        label = "Seconds",
                        value = selectedSeconds,
                        min = 0,
                        max = 59,
                        enabled = canChangeDuration,
                        onValueChange = {
                            selectedSeconds = it
                            viewModel.setDuration(selectedHours, selectedMinutes, selectedSeconds)
                        },
                        colors = colors
                    )
                }
            }

            val favorites = allExercises.filter { it.email == userEmail && it.isFavorite }

            TomorrowWorkoutCard(
                favorites = favorites,
                selected = selectedExercises,
                onAdd = { ex -> selectedExercises = selectedExercises + ex },
                onRemove = { ex -> selectedExercises = selectedExercises - ex },
                onClear = { selectedExercises = emptyList() },
                onExport = {
                    val intent = createCalendarIntent(selectedExercises)
                    context.startActivity(intent)
                }
            )

            Spacer(Modifier.height(24.dp))

            Text(
                "Recent Workouts",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = colors.primary
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            if (state.sessionHistory.isEmpty()) {
                Text(
                    "No workouts saved.",
                    color = Color.Gray,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            } else {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp)
                ) {
                    state.sessionHistory.forEachIndexed { idx, session ->
                        WorkoutHistoryRow(session, idx)
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun TomorrowWorkoutCard(
    favorites: List<Exercise>,
    selected: List<Exercise>,
    onAdd: (Exercise) -> Unit,
    onRemove: (Exercise) -> Unit,
    onClear: () -> Unit,
    onExport: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var showDialog by remember { mutableStateOf(false) }
    val available = favorites.filter { it !in selected }

    Card(
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Tomorrow's Workout",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = colors.primary
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            if (selected.isEmpty()) {
                Text(
                    "No exercises selected.",
                    color = colors.onSurfaceVariant,
                    fontSize = 15.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                // Lista esercizi scelti
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                ) {
                    selected.forEach { ex ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                ex.name,
                                fontWeight = FontWeight.Medium,
                                color = colors.primary,
                                modifier = Modifier.weight(1f),
                                fontSize = 16.sp
                            )
                            IconButton(
                                onClick = { onRemove(ex) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Remove",
                                    tint = colors.error
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { showDialog = true },
                    shape = RoundedCornerShape(13.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(Modifier.width(4.dp))
                    Text("Add")
                }
                TextButton(
                    onClick = { onClear() },
                    enabled = selected.isNotEmpty()
                ) {
                    Text("Clear all", color = colors.error, fontSize = 14.sp)
                }
                Button(
                    onClick = onExport,
                    enabled = selected.isNotEmpty(),
                    modifier = Modifier.height(40.dp),
                    shape = RoundedCornerShape(13.dp)
                ) {
                    Text("Export to Calendar", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text("Add favorite exercise", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            },
            text = {
                if (available.isEmpty()) {
                    Text("All favorites already added.", textAlign = TextAlign.Center)
                } else {
                    Column {
                        available.forEach { ex ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onAdd(ex)
                                        showDialog = false
                                    }
                                    .padding(vertical = 10.dp, horizontal = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    ex.name,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.primary,
                                    fontSize = 16.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = colors.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Close") }
            }
        )
    }
}

fun createCalendarIntent(exercises: List<Exercise>): Intent {
    val calendarIntent = Intent(Intent.ACTION_INSERT).apply {
        data = CalendarContract.Events.CONTENT_URI
        putExtra(CalendarContract.Events.TITLE, "Workout Plan")
        putExtra(
            CalendarContract.Events.DESCRIPTION,
            exercises.joinToString("\n") { it.name }
        )
        // Data di domani, ora 18:00
        val calendar = Calendar.getInstance().apply { add(Calendar.DATE, 1); set(Calendar.HOUR_OF_DAY, 18) }
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, calendar.timeInMillis)
        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, calendar.timeInMillis + 60 * 60 * 1000) // 1 ora
    }
    return calendarIntent
}

@Composable
fun NumberStepper(
    label: String,
    value: Int,
    min: Int,
    max: Int,
    enabled: Boolean,
    onValueChange: (Int) -> Unit,
    colors: ColorScheme
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(70.dp)
    ) {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = colors.primary)
        Spacer(Modifier.height(6.dp))
        OutlinedButton(
            onClick = { if (enabled && value > min) onValueChange(value - 1) },
            enabled = enabled && value > min,
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.size(36.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = colors.secondaryContainer,
                contentColor = colors.primary
            )
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease $label")
        }
        Text(
            text = value.toString().padStart(2, '0'),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
            modifier = Modifier.padding(vertical = 2.dp)
        )
        OutlinedButton(
            onClick = { if (enabled && value < max) onValueChange(value + 1) },
            enabled = enabled && value < max,
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.size(36.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = "Increase $label")
        }
    }
}

@Composable
fun WorkoutHistoryRow(session: FastingSession, index: Int) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val start = sdf.format(Date(session.startTime))
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Workout #${index + 1}",
                fontWeight = FontWeight.Bold,
                color = colors.primary,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                "Start: $start",
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = colors.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    formatSessionDuration(session.durationMillis),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = colors.primary
                )
            }
        }
    }
}

fun formatSessionDuration(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return "%02dh  %02dm  %02ds".format(hours, minutes, seconds)
}