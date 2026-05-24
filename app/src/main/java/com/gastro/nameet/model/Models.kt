package com.gastro.nameet.model

import kotlinx.serialization.Serializable

@Serializable
data class Place(
    val id: Int,
    val name: String,
    val description: String = "",
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String = "",
    val rating: Float = 0f,
    val category: String = "", // breakfast, lunch, dinner, dessert, snacks, soup
    val phone: String = "",
    val address: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class Food(
    val id: Int,
    val placeId: Int,
    val name: String,
    val description: String = "",
    val imageUrl: String = "",
    val category: String = ""
)

@Serializable
data class Bookmark(
    val id: Int,
    val placeId: Int,
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class Comment(
    val id: Int,
    val placeId: Int,
    val userName: String = "",
    val comment: String = "",
    val rating: Float = 0f,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class User(
    val id: Int,
    val email: String,
    val name: String = "",
    val profileImageUrl: String = "",
    val isLoggedIn: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// Polyline data model for maps feature
@Serializable
data class PolylineData(
    val id: String,
    val placeIds: List<Int>,
    val points: List<LatLng>,
    val color: String = "#F7941D",
    val width: Float = 5f,
    val distance: Double = 0.0,
    val duration: Long = 0L
)

// Route info displayed in the floating box
data class RouteInfo(
    val originName: String,
    val destinationName: String,
    val distanceKm: Double
)

@Serializable
data class LatLng(
    val latitude: Double,
    val longitude: Double
)

// Specialty food item from the Ilonggo food dataset
data class Specialty(
    val id: Int,
    val name: String,
    val description: String,
    val history: String,
    val imageUrl: String,
    val categoryId: Int
)

// Food bookmark for specialty items
@Serializable
data class FoodBookmark(
    val id: Int = 0,
    val specialtyId: Int,
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

// Bookmark sort options
enum class BookmarkSortOption(val displayName: String) {
    DATE_NEWEST("Date (Newest)"),
    DATE_OLDEST("Date (Oldest)"),
    NAME_ASC("Name (A-Z)"),
    RATING_HIGH("Rating (Highest)"),
    RATING_LOW("Rating (Lowest)")
}

// Tracks where a loading action originated for contextual indicator placement
enum class LoadingSource {
    NONE,
    SEARCH_BOX,
    PILL,
    CENTER,
    ROUTE
}

// UI State models
data class PlaceDetail(
    val place: Place,
    val foods: List<Food> = emptyList(),
    val comments: List<Comment> = emptyList(),
    val isBookmarked: Boolean = false,
    val averageRating: Float = 0f
)

data class MapState(
    val places: List<Place> = emptyList(),
    val selectedPlace: Place? = null,
    val polylines: List<PolylineData> = emptyList(),
    val userLocation: LatLng? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSelectedPlaceBookmarked: Boolean = false,
    val locationPermissionGranted: Boolean = false,
    // Map type: 0=NORMAL, 1=SATELLITE, 2=TERRAIN, 3=HYBRID
    val mapTypeIndex: Int = 0,
    // Loading source for contextual indicator placement
    val loadingSource: LoadingSource = LoadingSource.NONE,
    // Route info for the floating box
    val routeInfo: RouteInfo? = null,
    // True when the map is centering/locating an establishment
    val isLocating: Boolean = false,
    // Place ID to locate on map (set from bookmarks "Locate" action)
    val locatePlaceId: Int? = null
)

data class HomeState(
    val featuredPlaces: List<Place> = emptyList(),
    val categoryPlaces: Map<String, List<Place>> = emptyMap(),
    val specialties: List<Specialty> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class BookmarkState(
    val bookmarks: List<Place> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val sortOption: BookmarkSortOption = BookmarkSortOption.DATE_NEWEST
)

data class AuthState(
    val user: User? = null,
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
