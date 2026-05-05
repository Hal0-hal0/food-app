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

@Serializable
data class LatLng(
    val latitude: Double,
    val longitude: Double
)

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
    val error: String? = null
)

data class HomeState(
    val featuredPlaces: List<Place> = emptyList(),
    val categoryPlaces: Map<String, List<Place>> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class BookmarkState(
    val bookmarks: List<Place> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class AuthState(
    val user: User? = null,
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
