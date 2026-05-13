package com.gastro.nameet.ui.screens

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.gastro.nameet.model.Place
import com.gastro.nameet.model.LoadingSource
import com.gastro.nameet.viewmodel.MapsViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import com.gastro.nameet.util.Constants
import com.gastro.nameet.util.RouteConstants
import com.gastro.nameet.ui.theme.PrimaryOrange
import com.gastro.nameet.model.LatLng as ModelLatLng
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MapsScreen(
    navController: NavController,
    viewModel: MapsViewModel = hiltViewModel()
) {
    val state by viewModel.mapState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val loadingPill by viewModel.loadingPill.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Iloilo City Center
    val iloilo = LatLng(Constants.ILOILO_LAT, Constants.ILOILO_LNG)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(iloilo, Constants.DEFAULT_ZOOM)
    }

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showMapTypeMenu by remember { mutableStateOf(false) }

    // Map type from state
    val mapType = when (state.mapTypeIndex) {
        0 -> MapType.NORMAL
        1 -> MapType.SATELLITE
        2 -> MapType.TERRAIN
        3 -> MapType.HYBRID
        else -> MapType.NORMAL
    }
    val mapTypeLabels = listOf("Standard", "Satellite", "Terrain", "Hybrid")

    // Location permissions
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    val hasLocationPermission = locationPermissionsState.permissions.any { it.status.isGranted }

    // Sync permission state to ViewModel
    LaunchedEffect(hasLocationPermission) {
        viewModel.setLocationPermissionGranted(hasLocationPermission)
    }

    // Update bottom sheet visibility when a place is selected
    LaunchedEffect(state.selectedPlace) {
        if (state.selectedPlace != null) {
            showBottomSheet = true
        }
    }

    // When search results change, zoom the camera to fit all markers
    LaunchedEffect(state.places) {
        if (searchQuery.isNotBlank() && state.places.isNotEmpty()) {
            if (state.places.size == 1) {
                val p = state.places.first()
                scope.launch {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(p.latitude, p.longitude), 15f
                        )
                    )
                }
            } else if (state.places.size > 1) {
                val boundsBuilder = LatLngBounds.builder()
                state.places.forEach { p ->
                    boundsBuilder.include(LatLng(p.latitude, p.longitude))
                }
                scope.launch {
                    try {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100)
                        )
                    } catch (e: Exception) {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(iloilo, Constants.DEFAULT_ZOOM)
                        )
                    }
                }
            }
        } else if (searchQuery.isBlank()) {
            scope.launch {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(iloilo, Constants.DEFAULT_ZOOM)
                )
            }
        }
    }

    // React to user location updates
    LaunchedEffect(state.userLocation) {
        state.userLocation?.let { loc ->
            scope.launch {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(loc.latitude, loc.longitude), 16f
                    )
                )
            }
        }
    }

    // Show error messages
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = hasLocationPermission,
                    mapType = mapType,
                    minZoomPreference = 5f
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false
                ),
                onMapClick = {
                    viewModel.clearPolylines()
                }
            ) {
                state.places.forEach { place ->
                    Marker(
                        state = MarkerState(position = LatLng(place.latitude, place.longitude)),
                        title = place.name,
                        snippet = place.address.ifBlank { place.category },
                        onClick = {
                            viewModel.selectPlace(place.id)
                            true
                        }
                    )
                }

                // Show user location marker if we have it but the map's blue dot is disabled
                if (!hasLocationPermission && state.userLocation != null) {
                    val loc = state.userLocation!!
                    Marker(
                        state = MarkerState(position = LatLng(loc.latitude, loc.longitude)),
                        title = "You are here"
                    )
                }

                state.polylines.forEach { polylineData ->
                    Polyline(
                        points = polylineData.points.map { LatLng(it.latitude, it.longitude) },
                        color = Color(android.graphics.Color.parseColor(polylineData.color)),
                        width = polylineData.width
                    )
                }
            }

            // Search Bar and Delicacies Capsules
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter)
            ) {
                // Search bar with inline loading
                Box(modifier = Modifier.fillMaxWidth()) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp)),
                        placeholder = { Text("Search food in Iloilo...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Search box loading indicator
                                if (state.isLoading && state.loadingSource == LoadingSource.SEARCH_BOX) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = null)
                                    }
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        ),
                        singleLine = true
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Delicacy pills with inline loading
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(viewModel.delicacies) { delicacy ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilterChip(
                                selected = searchQuery == delicacy,
                                onClick = { viewModel.onPillClick(delicacy) },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(delicacy)
                                        // Pill loading indicator
                                        if (state.isLoading && loadingPill == delicacy) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(14.dp),
                                                strokeWidth = 1.5.dp,
                                                color = if (searchQuery == delicacy) Color.White
                                                       else MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                                ),
                                border = null,
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                    }
                }

                // Route info box overlay
                val currentRouteInfo = state.routeInfo
                if (currentRouteInfo != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, tint = PrimaryOrange)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Route to ${currentRouteInfo.destinationName}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Text("Distance: ${String.format("%.1f", currentRouteInfo.distanceKm)} km", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { viewModel.clearPolylines() }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear Route")
                            }
                        }
                    }
                }
            }

            // Right-side action buttons column (Camera reset, Map type, Locate)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Reset search and camera button
                SmallFloatingActionButton(
                    onClick = {
                        viewModel.resetSearch()
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(iloilo, Constants.DEFAULT_ZOOM)
                            )
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset Map")
                }

                // Map type button
                Box {
                    SmallFloatingActionButton(
                        onClick = { showMapTypeMenu = !showMapTypeMenu },
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        Icon(Icons.Default.Layers, contentDescription = "Map Type")
                    }
                    DropdownMenu(
                        expanded = showMapTypeMenu,
                        onDismissRequest = { showMapTypeMenu = false }
                    ) {
                        mapTypeLabels.forEachIndexed { index, label ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (state.mapTypeIndex == index) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                        Text(label)
                                    }
                                },
                                onClick = {
                                    viewModel.setMapType(index)
                                    showMapTypeMenu = false
                                }
                            )
                        }
                    }
                }

                // Locate me button
                FloatingActionButton(
                    onClick = {
                        if (hasLocationPermission) {
                            viewModel.fetchCurrentLocation()
                        } else {
                            locationPermissionsState.launchMultiplePermissionRequest()
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Locate Me")
                }
            }

            // Centered locating spinner
            if (state.isLocating) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        shadowElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = PrimaryOrange)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Locating...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        // Bottom sheet
        if (showBottomSheet && state.selectedPlace != null) {
            val place = state.selectedPlace!!
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                    // Bug 3 fix: clear selected place on dismiss
                    viewModel.clearSelectedPlace()
                },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                PlaceDetailsBottomSheetContent(
                    place = place,
                    isBookmarked = state.isSelectedPlaceBookmarked,
                    onBookmarkClick = { viewModel.toggleBookmark(place.id) },
                    onLocateClick = {
                        viewModel.drawRouteToPlace(place)
                        showBottomSheet = false
                        viewModel.clearSelectedPlace()
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(place.latitude, place.longitude),
                                    15f
                                )
                            )
                        }
                    },
                    onFullDetailsClick = {
                        // Bug 4 fix: save the place to DB before navigating
                        viewModel.savePlaceForDetails(place)
                        showBottomSheet = false
                        viewModel.clearSelectedPlace()
                        navController.navigate("${RouteConstants.PLACE_DETAILS}/${place.id}")
                    }
                )
            }
        }
    }
}

@Composable
fun PlaceDetailsBottomSheetContent(
    place: Place,
    isBookmarked: Boolean,
    onBookmarkClick: () -> Unit,
    onLocateClick: () -> Unit,
    onFullDetailsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = place.category,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onBookmarkClick) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = if (isBookmarked) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Address row
        if (place.address.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = place.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Rating row
        if (place.rating > 0f) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFB84D),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = String.format("%.1f", place.rating),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (place.imageUrl.isNotBlank()) {
            AsyncImage(
                model = place.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (place.description.isNotBlank()) {
            Text(
                text = place.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onLocateClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Icon(Icons.Default.Directions, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Locate")
            }

            OutlinedButton(
                onClick = onFullDetailsClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Text("View Details")
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
