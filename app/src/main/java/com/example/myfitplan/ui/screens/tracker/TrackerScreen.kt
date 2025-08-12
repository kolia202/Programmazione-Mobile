package com.example.myfitplan.ui.screens.tracker

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.PersonPinCircle
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.myfitplan.ui.MyFitPlanRoute
import com.example.myfitplan.ui.composables.NavBar
import com.example.myfitplan.ui.composables.NavBarItem
import com.example.myfitplan.ui.composables.TopBar
import org.koin.androidx.compose.koinViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.math.roundToInt

@Composable
fun TrackerScreen(
    navController: NavController,
    vm: TrackerViewModel = koinViewModel()
) {
    val s by vm.state.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme
    var selectedTab by remember { mutableStateOf(NavBarItem.Esercizi) }
    val isDark = isSystemInDarkTheme()

    val bottomPanelHeight = 140.dp // un filo più alto per mostrare stats

    Scaffold(
        topBar = {
            TopBar(
                onProfileClick = { navController.navigate(MyFitPlanRoute.Profile) },
                onPieChartClick = { navController.navigate(MyFitPlanRoute.Badge) }
            )
        },
        bottomBar = {
            NavBar(selected = selectedTab, onItemSelected = { selectedTab = it }, navController = navController)
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(colors.background)
        ) {
            var lastLat by remember { mutableStateOf<Double?>(null) }
            var lastLng by remember { mutableStateOf<Double?>(null) }
            val mapRef = remember { mutableStateOf<MapView?>(null) }

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osm", 0))
                    MapView(ctx).apply {
                        setMultiTouchControls(true)
                        setTilesScaledToDpi(true)
                        controller.setZoom(17.0)
                        zoomController.setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT)

                        val light = XYTileSource(
                            "CartoPositron", 0, 20, 256, ".png",
                            arrayOf(
                                "https://a.basemaps.cartocdn.com/light_all/",
                                "https://b.basemaps.cartocdn.com/light_all/",
                                "https://c.basemaps.cartocdn.com/light_all/"
                            )
                        )
                        val dark = XYTileSource(
                            "CartoDarkMatter", 0, 20, 256, ".png",
                            arrayOf(
                                "https://a.basemaps.cartocdn.com/dark_all/",
                                "https://b.basemaps.cartocdn.com/dark_all/",
                                "https://c.basemaps.cartocdn.com/dark_all/"
                            )
                        )
                        setTileSource(if (isDark) dark else light)

                        overlays.add(RotationGestureOverlay(this).apply { isEnabled = true })
                        overlays.add(CompassOverlay(ctx, this).apply { enableCompass() })

                        val myLoc = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this).apply {
                            enableMyLocation()
                            // NB: il follow reale lo gestiamo noi con s.followMode
                            disableFollowLocation()
                        }
                        overlays.add(myLoc)

                        overlays.add(MapEventsOverlay(object : MapEventsReceiver {
                            override fun singleTapConfirmedHelper(p: GeoPoint?) = false
                            override fun longPressHelper(p: GeoPoint?): Boolean {
                                p ?: return false
                                vm.setDestination(p.latitude, p.longitude)
                                controller.setCenter(p)
                                return true
                            }
                        }))
                        mapRef.value = this
                    }
                },
                update = { map ->
                    val primaryColor = colors.primary.toArgb()

                    val routeShadow = map.overlays.filterIsInstance<Polyline>().find { it.title == "route-shadow" }
                        ?: Polyline().also {
                            it.title = "route-shadow"
                            it.outlinePaint.strokeWidth = 16f
                            it.outlinePaint.alpha = 70
                            map.overlays.add(0, it)
                        }
                    val routePrimary = map.overlays.filterIsInstance<Polyline>().find { it.title == "route-primary" }
                        ?: Polyline().also {
                            it.title = "route-primary"
                            it.outlinePaint.strokeWidth = 10f
                            it.outlinePaint.isAntiAlias = true
                            it.outlinePaint.color = primaryColor
                            map.overlays.add(it)
                        }

                    val pts = s.routePath.map { GeoPoint(it.latitude, it.longitude) }
                    routeShadow.setPoints(pts)
                    routePrimary.setPoints(pts)

                    // marker: me (snappato se disponibile)
                    val meMarker = map.overlays.filterIsInstance<Marker>().find { it.title == "Tu sei qui" }
                        ?: Marker(map).apply {
                            title = "Tu sei qui"
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            map.overlays.add(this)
                        }
                    (s.snappedCurrent ?: s.current)?.let { curr ->
                        val gp = GeoPoint(curr.latitude, curr.longitude)
                        meMarker.position = gp
                        if (s.followMode && (lastLat != curr.latitude || lastLng != curr.longitude)) {
                            map.controller.animateTo(gp)
                            lastLat = curr.latitude; lastLng = curr.longitude
                        }
                    }

                    val destMarker = map.overlays.filterIsInstance<Marker>().find { it.title == "Destinazione" }
                        ?: Marker(map).apply {
                            title = "Destinazione"
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            map.overlays.add(this)
                        }
                    if (s.destination != null) {
                        destMarker.position = GeoPoint(s.destination!!.latitude, s.destination!!.longitude)
                        destMarker.isEnabled = true
                    } else destMarker.isEnabled = false

                    map.invalidate()
                }
            )

            /* Toggle FOLLOW (in alto a destra, sotto la TopBar) */
            AssistChip(
                onClick = { vm.toggleFollow() },
                label = { Text(if (s.followMode) "Follow ON" else "Follow OFF") },
                leadingIcon = { Icon(Icons.Default.CenterFocusStrong, contentDescription = null) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 12.dp, end = 16.dp)
            )

            /* Pannello zoom verticale (spostato più in alto) */
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp, bottom = 16.dp + bottomPanelHeight + 64.dp),
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                color = colors.surface
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(48.dp)) {
                    IconButton(onClick = { mapRef.value?.controller?.zoomIn() }) {
                        Icon(Icons.Default.Add, contentDescription = "Zoom in")
                    }
                    Divider()
                    IconButton(onClick = { mapRef.value?.controller?.zoomOut() }) {
                        Icon(Icons.Default.Remove, contentDescription = "Zoom out")
                    }
                }
            }

            /* FAB “torna su di me” */
            FloatingActionButton(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 16.dp, top = 56.dp), // sotto la topbar
                onClick = {
                    val map = mapRef.value ?: return@FloatingActionButton
                    val loc = (s.snappedCurrent ?: s.current) ?: return@FloatingActionButton
                    if (!s.followMode) vm.toggleFollow()
                    map.controller.animateTo(GeoPoint(loc.latitude, loc.longitude))
                },
                containerColor = colors.primary
            ) { Icon(Icons.Default.PersonPinCircle, contentDescription = "Centrati su di me") }

            /* Bottom card: include stats OSRM + residue live */
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .heightIn(min = bottomPanelHeight)
                    .wrapContentWidth(),
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 6.dp,
                color = colors.surface
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        when {
                            s.destination == null -> "Long press sulla mappa per impostare la destinazione"
                            s.isRouting -> "Calcolo percorso…"
                            else -> "Destinazione impostata"
                        }
                    )
                    Spacer(Modifier.height(8.dp))

                    // STATISTICHE
                    if (s.totalDistanceMeters != null && s.totalDurationSeconds != null) {
                        val totKm = s.totalDistanceMeters!! / 1000.0
                        val totMin = (s.totalDurationSeconds!! / 60.0).roundToInt()
                        Text("Totale OSRM: %.2f km • %d min".format(totKm, totMin), style = MaterialTheme.typography.bodyMedium)
                    }
                    if (s.distanceRemaining != null && s.etaSeconds != null) {
                        val remKm = s.distanceRemaining!! / 1000.0
                        val remMin = (s.etaSeconds!! / 60.0).roundToInt()
                        Text("Rimanenti: %.2f km • ~%d min".format(remKm, remMin), style = MaterialTheme.typography.bodyMedium)
                    }

                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { vm.startRoutingFromCurrent(navigationMode = true) },
                            enabled = s.destination != null && !s.isRouting
                        ) { Text(if (s.isRouting) "Calcolo..." else "Ricalcola") }

                        OutlinedButton(
                            onClick = { vm.clearRouting() },
                            enabled = s.destination != null || s.routePath.isNotEmpty()
                        ) { Text("Cancella") }
                    }

                    if (s.routingError != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(s.routingError ?: "", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}