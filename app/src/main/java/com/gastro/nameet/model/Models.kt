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
    val createdAt: Long = System.currentTimeMillis(),
    // Extended Google Places data (delimited "||" because Room cannot store List<String>)
    val googlePlaceId: String = "",
    val photoUrls: String = "",     // "||"-joined image URIs
    val website: String = "",
    val openingHours: String = "",  // "||"-joined weekday strings
    val priceLevel: Int = -1,        // 0..4 from Google Places API, -1 = unknown
    val userRatingsTotal: Int = 0,
    val businessStatus: String = ""
) {
    fun photos(): List<String> = if (photoUrls.isBlank()) emptyList() else photoUrls.split("||").filter { it.isNotBlank() }
    fun openingHoursList(): List<String> = if (openingHours.isBlank()) emptyList() else openingHours.split("||").filter { it.isNotBlank() }
}

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
    val googleReviews: List<GoogleReview> = emptyList(),
    val isBookmarked: Boolean = false,
    val averageRating: Float = 0f,
    // True while we're still pulling extras (extra photos, hours, website)
    // from the Google Places API.
    val isEnrichingDetails: Boolean = false,
    // True while we're still pulling reviews from the Place Details web service.
    val isLoadingReviews: Boolean = false
)

// A review fetched live from the Google Places API
data class GoogleReview(
    val authorName: String,
    val rating: Float,
    val text: String,
    val relativeTime: String,
    val profilePhotoUrl: String = ""
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
    // Daily suggestion shown at the top of Home — driven by time of day.
    val dailySuggestion: DailySuggestion? = null,
    val userName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Personalized suggestion shown on Home. Built from the current time of day
 * (morning => breakfast, midday => lunch, etc.). The matching category drives
 * what places we list.
 */
data class DailySuggestion(
    val greeting: String,
    val mealLabel: String, // "Breakfast", "Lunch", etc.
    val tagline: String,
    val category: String,  // matches Place.category for filtering
    val places: List<Place> = emptyList()
)

enum class BookmarkType { PLACE, SPECIALTY }

/**
 * Unified bookmark entry shown in the Bookmarks screen. Backs both Place
 * bookmarks (real establishments) and Specialty bookmarks (Ilonggo foods)
 * so both surface in one list.
 */
data class BookmarkItem(
    val key: String, // unique across types: "place:1", "specialty:3"
    val title: String,
    val subtitle: String,
    val imageUrl: String,
    val type: BookmarkType,
    val placeId: Int? = null,
    val specialtyId: Int? = null,
    val rating: Float = 0f,
    val createdAt: Long = System.currentTimeMillis()
)

data class BookmarkState(
    val bookmarks: List<BookmarkItem> = emptyList(),
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
