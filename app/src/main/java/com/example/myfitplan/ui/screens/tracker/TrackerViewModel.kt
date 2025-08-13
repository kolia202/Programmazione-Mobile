package com.example.myfitplan.ui.screens.tracker

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfitplan.data.database.Badge
import com.example.myfitplan.data.database.BadgeDAO
import com.example.myfitplan.data.database.BadgeUser
import com.example.myfitplan.data.database.BadgeUserDAO
import com.example.myfitplan.data.repositories.DatastoreRepository
import com.example.myfitplan.data.repositories.MyFitPlanRepositories
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.*

data class TrackerUiState(
    val current: Location? = null,
    val snappedCurrent: Location? = null,
    val startPoint: Location? = null,
    val destination: Location? = null,
    val routePath: List<Location> = emptyList(),
    val cumulativeMeters: List<Double> = emptyList(),
    val isRouting: Boolean = false,
    val routingError: String? = null,
    val totalDistanceMeters: Double? = null,
    val totalDurationSeconds: Double? = null,
    val distanceRemaining: Double? = null,
    val durationRemaining: Double? = null,
    val etaSeconds: Double? = null,
    val navigating: Boolean = false,
    val followMode: Boolean = true,
    val lastProgressMeters: Double = 0.0
)

class TrackerViewModel(
    app: Application,
    private val repos: MyFitPlanRepositories,
    private val ds: DatastoreRepository,
    private val badgeDao: BadgeDAO,
    private val badgeUserDao: BadgeUserDAO
) : AndroidViewModel(app) {

    private val appCtx = app.applicationContext
    private val fused by lazy { LocationServices.getFusedLocationProviderClient(appCtx) }

    private val _state = MutableStateFlow(TrackerUiState())
    val state: StateFlow<TrackerUiState> = _state.asStateFlow()

    private var updatesActive = false
    private var tenKmAwardTriggered = false // evita chiamate ripetute mentre sei a fine percorso

    init { primeLastKnown() }

    fun toggleFollow() = _state.update { it.copy(followMode = !it.followMode) }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(appCtx, Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(appCtx, Manifest.permission.ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    private fun primeLastKnown() {
        if (!hasLocationPermission()) return
        fused.lastLocation.addOnSuccessListener { it?.let { loc -> _state.update { s -> s.copy(current = loc) } } }
    }

    fun setDestination(lat: Double, lng: Double) {
        val dest = Location("dest").apply { latitude = lat; longitude = lng; time = System.currentTimeMillis() }
        _state.update {
            it.copy(
                destination = dest,
                routingError = null,
                // reset trigger badge quando imposti una nuova destinazione
                lastProgressMeters = 0.0
            )
        }
        tenKmAwardTriggered = false
    }

    fun clearRouting() {
        _state.update {
            it.copy(
                startPoint = null, destination = null, routePath = emptyList(),
                cumulativeMeters = emptyList(),
                isRouting = false, routingError = null,
                totalDistanceMeters = null, totalDurationSeconds = null,
                distanceRemaining = null, durationRemaining = null, etaSeconds = null,
                navigating = false, snappedCurrent = null,
                lastProgressMeters = 0.0
            )
        }
        tenKmAwardTriggered = false
        updatesActive = false
    }

    fun startRoutingFromCurrent(navigationMode: Boolean = true) {
        viewModelScope.launch {
            val current = getCurrentOrLastKnown()
            val dest = _state.value.destination
            if (current == null || dest == null) {
                _state.update { it.copy(routingError = "Punto corrente o destinazione non disponibili.") }
                return@launch
            }
            _state.update { it.copy(current = current, startPoint = current, isRouting = true, routingError = null) }
            val ok = fetchRoute(current, dest)
            _state.update { it.copy(navigating = navigationMode && ok) }
            if (navigationMode && ok) startContinuousUpdates()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startContinuousUpdates() {
        if (!hasLocationPermission() || updatesActive) return
        updatesActive = true
        locationFlow().onEach { loc ->
            _state.update { it.copy(current = loc) }
            updateProgress(loc)
        }.launchIn(viewModelScope)
    }

    @SuppressLint("MissingPermission")
    private fun locationFlow() = callbackFlow<Location> {
        if (!hasLocationPermission()) { close(); return@callbackFlow }
        val req = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 1200L)
            .setMinUpdateIntervalMillis(700L)
            .setMinUpdateDistanceMeters(1.5f)
            .build()
        val cb = object : LocationCallback() {
            override fun onLocationResult(res: LocationResult) {
                res.lastLocation?.let { trySend(it).isSuccess }
            }
        }
        fused.requestLocationUpdates(req, cb, android.os.Looper.getMainLooper())
        awaitClose { fused.removeLocationUpdates(cb) }
    }.takeWhile { updatesActive }

    private fun updateProgress(current: Location) {
        val s = _state.value
        if (s.routePath.size < 2 || s.totalDistanceMeters == null || s.totalDurationSeconds == null) return

        val proj = projectOnPolyline(current, s.routePath)
        val snapped = proj?.toLocation(current) ?: return

        val i = proj.segIndex
        val metersFromStart = s.cumulativeMeters[i] + s.routePath[i].distanceTo(snapped)
        val monotoneMeters = max(metersFromStart, s.lastProgressMeters)

        val total = s.totalDistanceMeters
        val remaining = (total - monotoneMeters).coerceAtLeast(0.0)

        // stima durata rimanente: proporzionale alla media OSRM
        val avgSpeed = (s.totalDurationSeconds / s.totalDistanceMeters).coerceAtLeast(1.0 / 3.0) // clamp
        val remSeconds = remaining * avgSpeed

        _state.update {
            it.copy(
                snappedCurrent = snapped,
                distanceRemaining = remaining,
                durationRemaining = remSeconds,
                etaSeconds = remSeconds,
                lastProgressMeters = monotoneMeters
            )
        }

        // Ricalcolo se fuori rotta
        val offRoute = current.distanceTo(snapped) > 35f
        if (offRoute && !s.isRouting && s.destination != null) {
            viewModelScope.launch { fetchRoute(snapped, s.destination) }
        }

        // --- Badge "10 km": assegna quando percorso completato e total >= 10_000 m
        if (!tenKmAwardTriggered && remaining <= 15.0 && total >= 10_000.0) {
            tenKmAwardTriggered = true
            viewModelScope.launch { awardTenKmBadgeIfNeeded() }
        }
    }

    private suspend fun fetchRoute(start: Location, dest: Location): Boolean {
        _state.update { it.copy(isRouting = true, routingError = null, routePath = emptyList(), lastProgressMeters = 0.0) }
        val url = "https://router.project-osrm.org/route/v1/foot/${start.longitude},${start.latitude};${dest.longitude},${dest.latitude}" +
                "?overview=full&geometries=geojson&steps=true"
        return try {
            val (path, totalDist, totalDur) = withContext(Dispatchers.IO) {
                val json = JSONObject(URL(url).readText())
                val route0 = json.getJSONArray("routes").getJSONObject(0)
                val coords = route0.getJSONObject("geometry").getJSONArray("coordinates")
                val path = buildList {
                    for (i in 0 until coords.length()) {
                        val c = coords.getJSONArray(i)
                        add(Location("route").apply {
                            longitude = c.getDouble(0)
                            latitude  = c.getDouble(1)
                            time = System.currentTimeMillis()
                        })
                    }
                }
                Triple(path, route0.optDouble("distance"), route0.optDouble("duration"))
            }

            val cumulative = buildCumulative(path)

            _state.update {
                it.copy(
                    routePath = path,
                    cumulativeMeters = cumulative,
                    totalDistanceMeters = totalDist,
                    totalDurationSeconds = totalDur,
                    isRouting = false,
                    routingError = null,
                    lastProgressMeters = 0.0,
                    distanceRemaining = totalDist,
                    durationRemaining = totalDur,
                    etaSeconds = totalDur
                )
            }
            tenKmAwardTriggered = false
            true
        } catch (e: Exception) {
            _state.update { it.copy(isRouting = false, routingError = "Impossibile calcolare il percorso: ${e.message}") }
            false
        }
    }

    private fun buildCumulative(path: List<Location>): List<Double> {
        val out = ArrayList<Double>(path.size).apply { add(0.0) }
        var acc = 0.0
        for (i in 0 until path.lastIndex) {
            acc += path[i].distanceTo(path[i+1]).toDouble()
            out.add(acc)
        }
        return out
    }

    private data class Projection(val lat: Double, val lng: Double, val segIndex: Int) {
        fun toLocation(template: Location): Location =
            Location("snapped").apply {
                latitude = lat; longitude = lng
                speed = template.speed; accuracy = template.accuracy; time = template.time
            }
    }

    private fun projectOnPolyline(p: Location, path: List<Location>): Projection? {
        if (path.size < 2) return null
        val lat0 = p.latitude

        fun toXY(lat: Double, lng: Double): Pair<Double, Double> {
            val R = 6371000.0
            val x = Math.toRadians(lng) * R * cos(Math.toRadians(lat0))
            val y = Math.toRadians(lat) * R
            return x to y
        }
        fun toLatLng(x: Double, y: Double): Pair<Double, Double> {
            val R = 6371000.0
            val lat = Math.toDegrees(y / R)
            val lng = Math.toDegrees(x / (R * cos(Math.toRadians(lat0))))
            return lat to lng
        }

        val (Px, Py) = toXY(p.latitude, p.longitude)
        var bestD2 = Double.POSITIVE_INFINITY
        var best: Projection? = null

        for (i in 0 until path.lastIndex) {
            val a = path[i]; val b = path[i+1]
            val (Ax, Ay) = toXY(a.latitude, a.longitude)
            val (Bx, By) = toXY(b.latitude, b.longitude)
            val ABx = Bx - Ax; val ABy = By - Ay
            val len2 = ABx*ABx + ABy*ABy
            if (len2 == 0.0) continue
            val t = (((Px - Ax)*ABx + (Py - Ay)*ABy) / len2).coerceIn(0.0, 1.0)
            val X = Ax + t*ABx; val Y = Ay + t*ABy
            val d2 = (Px - X)*(Px - X) + (Py - Y)*(Py - Y)
            if (d2 < bestD2) {
                bestD2 = d2
                val (lat, lng) = toLatLng(X, Y)
                best = Projection(lat, lng, i)
            }
        }
        return best
    }

    @SuppressLint("MissingPermission")
    private suspend fun getCurrentOrLastKnown(): Location? {
        if (!hasLocationPermission()) return null
        return getCurrentLocationCompat() ?: runCatching { fused.lastLocation.await() }.getOrNull()
    }

    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocationCompat(): Location? = kotlinx.coroutines.suspendCancellableCoroutine { cont ->
        if (!hasLocationPermission()) { cont.resume(null, null); return@suspendCancellableCoroutine }
        val cts = CancellationTokenSource()
        fused.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
            .addOnSuccessListener { cont.resume(it, null) }
            .addOnFailureListener { cont.resume(null, null) }
        cont.invokeOnCancellation { cts.cancel() }
    }

    // ------- Assegnazione badge "10 km"
    private suspend fun awardTenKmBadgeIfNeeded() {
        val title = "10 km"
        val desc = "Completa un percorso di almeno 10 km per la prima volta."
        val icon = "route"

        val badge = badgeDao.getByTitle(title) ?: run {
            val newId = (badgeDao.getMaxBadgeId() ?: 0) + 1
            val created = Badge(id = newId, title = title, description = desc, icon = icon)
            badgeDao.upsert(created)
            created
        }

        val email = ds.user.first()?.email ?: return
        val already = badgeUserDao.userHasBadge(email, badge.id) == 1
        if (!already) {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(java.util.Date())
            badgeUserDao.upsert(BadgeUser(email = email, badgeId = badge.id, dataAchieved = date))
        }
    }
}

private suspend fun com.google.android.gms.tasks.Task<Location>.await(): Location? =
    kotlinx.coroutines.suspendCancellableCoroutine { cont ->
        addOnSuccessListener { cont.resume(it, null) }
        addOnFailureListener { cont.resume(null, null) }
    }