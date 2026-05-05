package com.gastro.nameet.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.gastro.nameet.ui.components.BottomNavBar
import com.gastro.nameet.viewmodel.MapsViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.gastro.nameet.util.RouteConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsScreen(
    navController: NavController,
    viewModel: MapsViewModel = hiltViewModel()
) {
    val state by viewModel.mapState.collectAsState()
    
    // Default camera position (Philippines)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(12.8797, 121.7740), 6f)
    }

    Scaffold(
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = false), // Should request permission
                uiSettings = MapUiSettings(zoomControlsEnabled = true)
            ) {
                state.places.forEach { place ->
                    Marker(
                        state = MarkerState(position = LatLng(place.latitude, place.longitude)),
                        title = place.name,
                        snippet = place.category,
                        onClick = {
                            viewModel.selectPlace(place.id)
                            false
                        },
                        onInfoWindowClick = {
                            navController.navigate("${RouteConstants.PLACE_DETAILS}/${place.id}")
                        }
                    )
                }

                state.polylines.forEach { polylineData ->
                    Polyline(
                        points = polylineData.points.map { LatLng(it.latitude, it.longitude) },
                        color = androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(polylineData.color)),
                        width = polylineData.width
                    )
                }
            }

            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}
