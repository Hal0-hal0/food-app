package com.gastro.nameet.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gastro.nameet.database.FoodDataset
import com.gastro.nameet.database.SampleData
import com.gastro.nameet.model.*
import com.gastro.nameet.repository.*
import com.gastro.nameet.util.Constants
import com.gastro.nameet.util.PolylineDecoder
import com.gastro.nameet.util.RouteConstants
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.model.Place.Field
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.SearchByTextRequest
import com.google.android.gms.maps.model.LatLng as MapsLatLng
import com.gastro.nameet.model.LatLng as ModelLatLng
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
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
                // Load specialty foods from the dataset
                val specialties = FoodDataset.getSpecialties()
                _homeState.update { it.copy(specialties = specialties) }
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
                if (query.isBlank()) {
                    loadPlaces()
                } else {
                    placeRepository.searchPlaces(query).collect { results ->
                        _homeState.update { it.copy(featuredPlaces = results, isLoading = false) }
                    }
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
                    // Load the actual Place objects for each bookmark
                    val places = bookmarks.mapNotNull { bookmark ->
                        placeRepository.getPlaceById(bookmark.placeId).firstOrNull()
                    }
                    _bookmarkState.update {
                        it.copy(
                            bookmarks = sortBookmarks(places, it.sortOption),
                            isLoading = false
                        )
                    }
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

    fun setSortOption(option: BookmarkSortOption) {
        _bookmarkState.update { state ->
            state.copy(
                sortOption = option,
                bookmarks = sortBookmarks(state.bookmarks, option)
            )
        }
    }

    private fun sortBookmarks(places: List<Place>, option: BookmarkSortOption): List<Place> {
        return when (option) {
            BookmarkSortOption.NAME_ASC -> places.sortedBy { it.name }
            BookmarkSortOption.RATING_HIGH -> places.sortedByDescending { it.rating }
            BookmarkSortOption.RATING_LOW -> places.sortedBy { it.rating }
            BookmarkSortOption.DATE_NEWEST -> places.sortedByDescending { it.createdAt }
            BookmarkSortOption.DATE_OLDEST -> places.sortedBy { it.createdAt }
        }
    }

    fun clearError() {
        _bookmarkState.update { it.copy(error = null) }
    }

    fun locatePlace(placeId: Int) {
        placeRepository.placeToLocate.value = placeId
    }
}

@HiltViewModel
class MapsViewModel @Inject constructor(
    private val placeRepository: PlaceRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val placesClient: PlacesClient,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val okHttpClient: OkHttpClient
) : ViewModel() {

    private val _mapState = MutableStateFlow(MapState())
    val mapState: StateFlow<MapState> = _mapState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val delicacies = listOf("Batchoy", "Pancit Molo", "Biscocho", "Butterscotch", "Kansi", "Chicken Binakol", "Laswa")

    // Job to track bookmark observation so we can cancel/restart it
    private var bookmarkObserverJob: Job? = null

    // Track the pill that is loading (null when not triggered by pill)
    private val _loadingPill = MutableStateFlow<String?>(null)
    val loadingPill = _loadingPill.asStateFlow()

    init {
        viewModelScope.launch {
            @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
            _searchQuery
                .debounce(500)
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    _mapState.update { it.copy(isLoading = true) }
                    if (query.isBlank()) {
                        flow {
                            var places = placeRepository.getAllPlaces().first()
                            if (places.isEmpty()) {
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
                                places = placeRepository.getAllPlaces().first()
                            }
                            emit(places)
                        }
                    } else {
                        flow {
                            try {
                                val placeFields = listOf(
                                    Field.ID,
                                    Field.NAME,
                                    Field.LAT_LNG,
                                    Field.ADDRESS,
                                    Field.RATING,
                                    Field.TYPES
                                )

                                val iloiloCenter = MapsLatLng(Constants.ILOILO_LAT, Constants.ILOILO_LNG)
                                val iloiloBounds = CircularBounds.newInstance(iloiloCenter, 15000.0) // 15km radius
                                
                                // Improve search by ensuring 'Iloilo' is in the query context
                                val effectiveQuery = if (query.contains("Iloilo", ignoreCase = true)) query else "$query in Iloilo"
                                
                                val request = SearchByTextRequest.builder(effectiveQuery, placeFields)
                                    .setLocationBias(iloiloBounds)
                                    .setMaxResultCount(15)
                                    .build()

                                val response = placesClient.searchByText(request).await()
                                val googlePlaces = response.places.mapIndexed { index, googlePlace ->
                                    Place(
                                        id = googlePlace.id?.hashCode() ?: index,
                                        name = googlePlace.name ?: "",
                                        description = googlePlace.types?.joinToString(", ") ?: "",
                                        latitude = googlePlace.latLng?.latitude ?: 0.0,
                                        longitude = googlePlace.latLng?.longitude ?: 0.0,
                                        imageUrl = "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800", // Fallback image since Places SDK doesn't provide direct URLs
                                        rating = googlePlace.rating?.toFloat() ?: 0f,
                                        category = googlePlace.types?.firstOrNull()?.name ?: "Food",
                                        address = googlePlace.address ?: ""
                                    )
                                }
                                emit(googlePlaces)
                            } catch (e: Exception) {
                                _mapState.update { it.copy(error = "Search issue: ${e.message}") }
                                placeRepository.searchPlaces(query).collect { emit(it) }
                            }
                        }
                    }
                }
                .collect { results ->
                    _mapState.update {
                        it.copy(
                            places = results,
                            isLoading = false,
                            loadingSource = LoadingSource.NONE,
                            // Bug 2 fix: clear polylines when search results change
                            polylines = emptyList()
                        )
                    }
                    _loadingPill.value = null
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        // Bug 2 fix: clear polylines on any search change
        _mapState.update {
            it.copy(
                isLoading = true,
                loadingSource = LoadingSource.SEARCH_BOX,
                polylines = emptyList(),
                routeInfo = null
            )
        }
        _loadingPill.value = null
    }

    /** Feature: Reset search and clear all pins */
    fun resetSearch() {
        _searchQuery.value = ""
        _loadingPill.value = null
        _mapState.update {
            it.copy(
                places = emptyList(),
                selectedPlace = null,
                polylines = emptyList(),
                routeInfo = null,
                isLoading = false,
                loadingSource = LoadingSource.NONE,
                isLocating = false
            )
        }
    }

    fun onPillClick(delicacy: String) {
        if (_searchQuery.value == delicacy) {
            _searchQuery.value = ""
            _loadingPill.value = null
            _mapState.update {
                it.copy(
                    isLoading = true,
                    loadingSource = LoadingSource.NONE,
                    polylines = emptyList()
                )
            }
        } else {
            _searchQuery.value = delicacy
            _loadingPill.value = delicacy
            _mapState.update {
                it.copy(
                    isLoading = true,
                    loadingSource = LoadingSource.PILL,
                    polylines = emptyList()
                )
            }
        }
    }

    fun filterByCategory(category: String) {
        viewModelScope.launch {
            _mapState.update { it.copy(isLoading = true, polylines = emptyList()) }
            placeRepository.getPlacesByCategory(category).collect { results ->
                _mapState.update { it.copy(places = results, isLoading = false) }
            }
        }
    }

    fun selectPlace(placeId: Int) {
        viewModelScope.launch {
            val place = _mapState.value.places.find { it.id == placeId }
            if (place != null) {
                // Bug 3 fix: always update selectedPlace even if same place
                // Force a state change by first setting to null then to the place
                _mapState.update { it.copy(selectedPlace = null) }
                _mapState.update { it.copy(selectedPlace = place) }
                // Start observing bookmark status for the selected place
                observeBookmarkStatus(placeId)
            }
        }
    }

    /** Bug 3 fix: clear selectedPlace when bottom sheet is dismissed */
    fun clearSelectedPlace() {
        bookmarkObserverJob?.cancel()
        _mapState.update { it.copy(selectedPlace = null, isSelectedPlaceBookmarked = false) }
    }

    private fun observeBookmarkStatus(placeId: Int) {
        bookmarkObserverJob?.cancel()
        bookmarkObserverJob = viewModelScope.launch {
            bookmarkRepository.isBookmarked(placeId, "current_user").collect { bookmark ->
                _mapState.update { it.copy(isSelectedPlaceBookmarked = bookmark != null) }
            }
        }
    }

    /**
     * Toggles the bookmark for the selected place.
     * If the place doesn't exist in the local DB (e.g. from Google Places search),
     * it is inserted first to satisfy the foreign key constraint.
     */
    fun toggleBookmark(placeId: Int) {
        viewModelScope.launch {
            try {
                val isBookmarked = _mapState.value.isSelectedPlaceBookmarked
                if (isBookmarked) {
                    bookmarkRepository.removeBookmark(placeId, "current_user")
                } else {
                    // Ensure the place exists in the DB before bookmarking (FK constraint)
                    val existingPlace = placeRepository.getPlaceById(placeId).firstOrNull()
                    if (existingPlace == null) {
                        val place = _mapState.value.places.find { it.id == placeId }
                            ?: _mapState.value.selectedPlace
                        if (place != null) {
                            placeRepository.insertPlace(place)
                        }
                    }
                    bookmarkRepository.insertBookmark(
                        Bookmark(id = 0, placeId = placeId, userId = "current_user")
                    )
                }
                // The bookmark observer flow will automatically update the state
            } catch (e: Exception) {
                _mapState.update { it.copy(error = e.message) }
            }
        }
    }

    /**
     * Bug 4 fix: saves a place to the local DB so PlaceDetailsScreen can find it.
     * Called before navigating to the details screen.
     */
    fun savePlaceForDetails(place: Place) {
        viewModelScope.launch {
            try {
                val existing = placeRepository.getPlaceById(place.id).firstOrNull()
                if (existing == null) {
                    placeRepository.insertPlace(place)
                }
            } catch (e: Exception) {
                // If insert fails (unlikely), details screen will show loading state
                _mapState.update { it.copy(error = "Could not save place: ${e.message}") }
            }
        }
    }

    fun updateUserLocation(latLng: ModelLatLng) {
        _mapState.update { it.copy(userLocation = latLng) }
    }

    /**
     * Fetches the device's real GPS location using FusedLocationProviderClient.
     * Requires location permission to be granted beforehand.
     */
    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation() {
        viewModelScope.launch {
            try {
                val cancellationTokenSource = CancellationTokenSource()
                val location = fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                ).await()
                if (location != null) {
                    val latLng = ModelLatLng(location.latitude, location.longitude)
                    _mapState.update {
                        it.copy(
                            userLocation = latLng,
                            locationPermissionGranted = true
                        )
                    }
                }
            } catch (e: Exception) {
                _mapState.update { it.copy(error = "Could not get location: ${e.message}") }
            }
        }
    }

    fun setLocationPermissionGranted(granted: Boolean) {
        _mapState.update { it.copy(locationPermissionGranted = granted) }
    }

    /**
     * Bug 1 fix: Uses the Google Directions API to get road-following route points
     * instead of drawing a straight line.
     */
    fun drawRouteToPlace(destination: Place) {
        val userLoc = _mapState.value.userLocation ?: return
        viewModelScope.launch {
            try {
                val origin = "${userLoc.latitude},${userLoc.longitude}"
                val dest = "${destination.latitude},${destination.longitude}"
                val url = "https://maps.googleapis.com/maps/api/directions/json" +
                        "?origin=$origin&destination=$dest&mode=driving&key=${Constants.MAPS_API_KEY}"

                val (routePoints, distance) = withContext(Dispatchers.IO) {
                    val request = Request.Builder().url(url).build()
                    val response = okHttpClient.newCall(request).execute()
                    val body = response.body?.string() ?: ""
                    val points = parseDirectionsResponse(body)
                    val dist = parseDirectionsDistance(body)
                    Pair(points, dist)
                }

                if (routePoints.isNotEmpty()) {
                    val route = PolylineData(
                        id = "route_${destination.id}",
                        placeIds = listOf(destination.id),
                        points = routePoints,
                        color = "#F7941D",
                        width = 10f,
                        distance = distance
                    )
                    _mapState.update { 
                        it.copy(
                            polylines = listOf(route),
                            routeInfo = RouteInfo(
                                originName = "Current Location",
                                destinationName = destination.name,
                                distanceKm = distance / 1000.0
                            )
                        )
                    }
                } else {
                    // Fallback to straight line if Directions API fails
                    drawStraightRoute(destination)
                }
            } catch (e: Exception) {
                // Fallback to straight line
                drawStraightRoute(destination)
            }
        }
    }

    private fun drawStraightRoute(destination: Place) {
        val userLoc = _mapState.value.userLocation ?: return
        val route = PolylineData(
            id = "route_${destination.id}",
            placeIds = listOf(destination.id),
            points = listOf(
                userLoc,
                ModelLatLng(destination.latitude, destination.longitude)
            ),
            color = "#F7941D",
            width = 10f
        )
        _mapState.update { it.copy(polylines = listOf(route)) }
    }

    private fun parseDirectionsResponse(json: String): List<ModelLatLng> {
        return try {
            val jsonObject = JSONObject(json)
            val routes = jsonObject.getJSONArray("routes")
            if (routes.length() == 0) return emptyList()

            val route = routes.getJSONObject(0)
            val overviewPolyline = route.getJSONObject("overview_polyline")
            val encodedPoints = overviewPolyline.getString("points")
            PolylineDecoder.decode(encodedPoints)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseDirectionsDistance(json: String): Double {
        return try {
            val jsonObject = JSONObject(json)
            val routes = jsonObject.getJSONArray("routes")
            if (routes.length() == 0) return 0.0

            val legs = routes.getJSONObject(0).getJSONArray("legs")
            if (legs.length() == 0) return 0.0
            
            legs.getJSONObject(0).getJSONObject("distance").getDouble("value")
        } catch (e: Exception) {
            0.0
        }
    }

    fun locatePlaceOnMap(placeId: Int) {
        viewModelScope.launch {
            _mapState.update { it.copy(isLocating = true) }
            val place = placeRepository.getPlaceById(placeId).firstOrNull()
            if (place != null) {
                _mapState.update { 
                    it.copy(
                        places = listOf(place),
                        selectedPlace = place,
                        isLocating = false
                    )
                }
            } else {
                _mapState.update { it.copy(isLocating = false, error = "Place not found") }
            }
        }
    }

    fun clearPolylines() {
        _mapState.update { it.copy(polylines = emptyList(), routeInfo = null) }
    }

    /** Feature: cycle through map types */
    fun cycleMapType() {
        _mapState.update {
            it.copy(mapTypeIndex = (it.mapTypeIndex + 1) % 4)
        }
    }

    fun setMapType(index: Int) {
        _mapState.update { it.copy(mapTypeIndex = index.coerceIn(0, 3)) }
    }

    fun clearError() {
        _mapState.update { it.copy(error = null) }
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

    fun locatePlace() {
        placeRepository.placeToLocate.value = placeId
    }
}

@HiltViewModel
class FoodDetailViewModel @Inject constructor(
    private val foodBookmarkRepository: FoodBookmarkRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val foodId: Int = checkNotNull(savedStateHandle[RouteConstants.FOOD_ID_ARG])

    val specialty: Specialty? = FoodDataset.getSpecialtyById(foodId)

    private val _isBookmarked = MutableStateFlow(false)
    val isBookmarked: StateFlow<Boolean> = _isBookmarked.asStateFlow()

    init {
        observeBookmark()
    }

    private fun observeBookmark() {
        viewModelScope.launch {
            foodBookmarkRepository.isBookmarked(foodId, "current_user").collect { bookmark ->
                _isBookmarked.value = bookmark != null
            }
        }
    }

    fun toggleBookmark() {
        viewModelScope.launch {
            if (_isBookmarked.value) {
                foodBookmarkRepository.removeBookmark(foodId, "current_user")
            } else {
                foodBookmarkRepository.insertBookmark(
                    FoodBookmark(specialtyId = foodId, userId = "current_user")
                )
            }
        }
    }
}
