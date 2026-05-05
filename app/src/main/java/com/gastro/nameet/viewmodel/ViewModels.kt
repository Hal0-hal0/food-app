package com.gastro.nameet.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gastro.nameet.database.SampleData
import com.gastro.nameet.model.*
import com.gastro.nameet.repository.*
import com.gastro.nameet.util.RouteConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkLoggedInUser()
    }

    private fun checkLoggedInUser() {
        viewModelScope.launch {
            authRepository.getLoggedInUser().collect { user ->
                _authState.update { 
                    it.copy(user = user, isLoggedIn = user != null)
                }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true) }
            try {
                val success = authRepository.login(email, password)
                if (success) {
                    checkLoggedInUser()
                    _authState.update { it.copy(isLoading = false) }
                } else {
                    _authState.update { it.copy(isLoading = false, error = "Invalid credentials") }
                }
            } catch (e: Exception) {
                _authState.update { it.copy(isLoading = false, error = e.message ?: "Login failed") }
            }
        }
    }

    fun register(email: String, name: String, password: String) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true) }
            try {
                val success = authRepository.register(email, name, password)
                if (success) {
                    checkLoggedInUser()
                    _authState.update { it.copy(isLoading = false) }
                } else {
                    _authState.update { it.copy(isLoading = false, error = "Registration failed") }
                }
            } catch (e: Exception) {
                _authState.update { it.copy(isLoading = false, error = e.message ?: "Registration failed") }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _authState.update { AuthState() }
        }
    }

    fun clearError() {
        _authState.update { it.copy(error = null) }
    }
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val placeRepository: PlaceRepository
) : ViewModel() {
    
    private val _homeState = MutableStateFlow<HomeState>(HomeState())
    val homeState: StateFlow<HomeState> = _homeState.asStateFlow()

    init {
        seedDataAndLoad()
    }

    private fun seedDataAndLoad() {
        viewModelScope.launch {
            _homeState.update { it.copy(isLoading = true) }
            try {
                val currentPlaces = placeRepository.getAllPlaces().first()
                if (currentPlaces.isEmpty()) {
                    val samplePlaces = SampleData.getSamplePlaces().map { entity ->
                        Place(
                            id = entity.id,
                            name = entity.name,
                            description = entity.description,
                            latitude = entity.latitude,
                            longitude = entity.longitude,
                            imageUrl = entity.imageUrl,
                            rating = entity.rating,
                            category = entity.category,
                            phone = entity.phone,
                            address = entity.address,
                            createdAt = entity.createdAt
                        )
                    }
                    placeRepository.insertPlaces(samplePlaces)
                }
                loadPlaces()
            } catch (e: Exception) {
                _homeState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun loadPlaces() {
        viewModelScope.launch {
            placeRepository.getAllPlaces().collect { places ->
                val categories = mapOf(
                    "Breakfast" to places.filter { it.category == "breakfast" },
                    "Lunch" to places.filter { it.category == "lunch" },
                    "Dinner" to places.filter { it.category == "dinner" },
                    "Dessert" to places.filter { it.category == "dessert" },
                    "Snacks" to places.filter { it.category == "snacks" },
                    "Soup" to places.filter { it.category == "soup" }
                )
                _homeState.update { 
                    it.copy(
                        featuredPlaces = places.sortedByDescending { p -> p.rating }.take(5),
                        categoryPlaces = categories,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun searchPlaces(query: String) {
        viewModelScope.launch {
            _homeState.update { it.copy(isLoading = true) }
            try {
                placeRepository.searchPlaces(query).collect { results ->
                    _homeState.update { it.copy(featuredPlaces = results, isLoading = false) }
                }
            } catch (e: Exception) {
                _homeState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun getPlacesByCategory(category: String) {
        viewModelScope.launch {
            _homeState.update { it.copy(isLoading = true) }
            try {
                placeRepository.getPlacesByCategory(category).collect { results ->
                    _homeState.update { 
                        it.copy(
                            featuredPlaces = results,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _homeState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun clearError() {
        _homeState.update { it.copy(error = null) }
    }
}

@HiltViewModel
class BookmarkViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    private val placeRepository: PlaceRepository
) : ViewModel() {
    
    private val _bookmarkState = MutableStateFlow<BookmarkState>(BookmarkState())
    val bookmarkState: StateFlow<BookmarkState> = _bookmarkState.asStateFlow()

    fun loadUserBookmarks(userId: String) {
        viewModelScope.launch {
            _bookmarkState.update { it.copy(isLoading = true) }
            try {
                bookmarkRepository.getUserBookmarks(userId).collect { bookmarks ->
                    // Logic to load places for bookmarks
                    _bookmarkState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _bookmarkState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun addBookmark(placeId: Int, userId: String) {
        viewModelScope.launch {
            try {
                val bookmark = Bookmark(
                    id = 0,
                    placeId = placeId,
                    userId = userId
                )
                bookmarkRepository.insertBookmark(bookmark)
                loadUserBookmarks(userId)
            } catch (e: Exception) {
                _bookmarkState.update { it.copy(error = e.message) }
            }
        }
    }

    fun removeBookmark(placeId: Int, userId: String) {
        viewModelScope.launch {
            try {
                bookmarkRepository.removeBookmark(placeId, userId)
                loadUserBookmarks(userId)
            } catch (e: Exception) {
                _bookmarkState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearError() {
        _bookmarkState.update { it.copy(error = null) }
    }
}

@HiltViewModel
class MapsViewModel @Inject constructor(
    private val placeRepository: PlaceRepository
) : ViewModel() {

    private val _mapState = MutableStateFlow(MapState())
    val mapState: StateFlow<MapState> = _mapState.asStateFlow()

    init {
        loadPlaces()
    }

    private fun loadPlaces() {
        viewModelScope.launch {
            _mapState.update { it.copy(isLoading = true) }
            try {
                placeRepository.getAllPlaces().collect { places ->
                    _mapState.update { it.copy(places = places, isLoading = false) }
                }
            } catch (e: Exception) {
                _mapState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun selectPlace(placeId: Int) {
        val place = _mapState.value.places.find { it.id == placeId }
        _mapState.update { it.copy(selectedPlace = place) }
    }

    fun addPolyline(polyline: PolylineData) {
        _mapState.update { it.copy(polylines = it.polylines + polyline) }
    }

    fun clearPolylines() {
        _mapState.update { it.copy(polylines = emptyList()) }
    }
}

@HiltViewModel
class PlaceDetailsViewModel @Inject constructor(
    private val placeRepository: PlaceRepository,
    private val foodRepository: FoodRepository,
    private val commentRepository: CommentRepository,
    private val bookmarkRepository: BookmarkRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val placeId: Int = checkNotNull(savedStateHandle[RouteConstants.PLACE_ID_ARG])

    private val _uiState = MutableStateFlow<PlaceDetail?>(null)
    val uiState: StateFlow<PlaceDetail?> = _uiState.asStateFlow()

    init {
        loadPlaceDetails()
    }

    private fun loadPlaceDetails() {
        viewModelScope.launch {
            combine(
                placeRepository.getPlaceById(placeId),
                foodRepository.getFoodsByPlace(placeId),
                commentRepository.getCommentsByPlace(placeId),
                commentRepository.getAverageRating(placeId),
                bookmarkRepository.isBookmarked(placeId, "current_user")
            ) { place, foods, comments, avgRating, bookmark ->
                if (place != null) {
                    PlaceDetail(
                        place = place,
                        foods = foods,
                        comments = comments,
                        isBookmarked = bookmark != null,
                        averageRating = avgRating ?: 0f
                    )
                } else {
                    null
                }
            }.collect { detail ->
                _uiState.value = detail
            }
        }
    }

    fun toggleBookmark() {
        viewModelScope.launch {
            val current = _uiState.value ?: return@launch
            if (current.isBookmarked) {
                bookmarkRepository.removeBookmark(placeId, "current_user")
            } else {
                val bookmark = Bookmark(
                    id = 0,
                    placeId = placeId,
                    userId = "current_user"
                )
                bookmarkRepository.insertBookmark(bookmark)
            }
        }
    }
}
