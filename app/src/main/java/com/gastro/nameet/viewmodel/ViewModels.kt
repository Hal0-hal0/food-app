package com.gastro.nameet.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gastro.nameet.database.FoodDataset
import com.gastro.nameet.database.SampleData
import com.gastro.nameet.model.*
import com.gastro.nameet.repository.*
import com.gastro.nameet.util.Constants
import com.gastro.nameet.util.DistanceUtils
import com.gastro.nameet.util.PolylineDecoder
import com.gastro.nameet.util.RouteConstants
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.model.Place.Field
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.PhotoMetadata
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.SearchByTextRequest
import com.google.android.gms.maps.model.LatLng as MapsLatLng
import com.gastro.nameet.model.LatLng as ModelLatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Provider

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: com.gastro.nameet.repository.UserPreferencesRepository
) : ViewModel() {

    val darkMode: StateFlow<Boolean> = userPreferencesRepository.darkMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setDarkMode(enabled)
        }
    }

    fun updateProfile(id: Int, name: String, email: String, profileImageUrl: String) {
        if (id <= 0) {
            _uiMessage.value = "No active user"
            return
        }
        viewModelScope.launch {
            _isSaving.value = true
            try {
                authRepository.updateProfile(id, name.trim(), email.trim(), profileImageUrl)
                _uiMessage.value = "Profile updated"
            } catch (e: Exception) {
                _uiMessage.value = "Could not update profile: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun updatePassword(id: Int, current: String, new: String) {
        if (id <= 0) {
            _uiMessage.value = "No active user"
            return
        }
        if (new.length < 6) {
            _uiMessage.value = "New password must be at least 6 characters"
            return
        }
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val ok = authRepository.updatePassword(id, current, new)
                _uiMessage.value = if (ok) "Password updated" else "Current password is incorrect"
            } catch (e: Exception) {
                _uiMessage.value = "Could not update password: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun clearMessage() {
        _uiMessage.value = null
    }
}

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
    private val placeRepository: PlaceRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _homeState = MutableStateFlow<HomeState>(HomeState())
    val homeState: StateFlow<HomeState> = _homeState.asStateFlow()

    /** Cached full lists so we can filter locally without re-querying. */
    private var allPlaces: List<Place> = emptyList()
    private var allSpecialties: List<Specialty> = emptyList()

    init {
        observeUser()
        seedDataAndLoad()
    }

    private fun observeUser() {
        viewModelScope.launch {
            authRepository.getLoggedInUser().collect { user ->
                _homeState.update { it.copy(userName = user?.name.orEmpty()) }
            }
        }
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
                allSpecialties = FoodDataset.getSpecialties()
                _homeState.update { it.copy(specialties = allSpecialties) }
                loadPlaces()
            } catch (e: Exception) {
                _homeState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun loadPlaces() {
        viewModelScope.launch {
            placeRepository.getAllPlaces().collect { places ->
                allPlaces = places
                applyHomeState(_searchQuery.value)
            }
        }
    }

    /**
     * Builds the Home UI state from cached lists, applying any active search
     * filter to BOTH places and specialties so the entire screen reacts to
     * the query.
     */
    private fun applyHomeState(query: String) {
        val q = query.trim()

        val filteredPlaces = if (q.isBlank()) {
            allPlaces
        } else {
            allPlaces.filter { p ->
                p.name.contains(q, ignoreCase = true) ||
                p.description.contains(q, ignoreCase = true) ||
                p.category.contains(q, ignoreCase = true) ||
                p.address.contains(q, ignoreCase = true)
            }
        }

        val filteredSpecialties = if (q.isBlank()) {
            allSpecialties
        } else {
            allSpecialties.filter { s ->
                s.name.contains(q, ignoreCase = true) ||
                s.description.contains(q, ignoreCase = true)
            }
        }

        val categories = mapOf(
            "Breakfast" to filteredPlaces.filter { it.category == "breakfast" },
            "Lunch" to filteredPlaces.filter { it.category == "lunch" },
            "Dinner" to filteredPlaces.filter { it.category == "dinner" },
            "Dessert" to filteredPlaces.filter { it.category == "dessert" },
            "Snacks" to filteredPlaces.filter { it.category == "snacks" },
            "Soup" to filteredPlaces.filter { it.category == "soup" }
        )

        _homeState.update {
            it.copy(
                featuredPlaces = filteredPlaces.sortedByDescending { p -> p.rating }.take(10),
                categoryPlaces = categories,
                specialties = filteredSpecialties,
                dailySuggestion = buildDailySuggestion(filteredPlaces),
                isLoading = false
            )
        }
    }

    /**
     * Picks a category and tagline based on the current local time so the
     * Home suggestion section shifts through the day.
     */
    private fun buildDailySuggestion(places: List<Place>): DailySuggestion {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val (greeting, mealLabel, tagline, category) = when (hour) {
            in 5..10 -> Quad("Good morning!", "Breakfast", "Start your day with something warm.", "breakfast")
            in 11..13 -> Quad("Lunch time", "Lunch", "Filling Ilonggo classics to power your afternoon.", "lunch")
            in 14..16 -> Quad("Merienda hour", "Snacks", "A little something to bridge the day.", "snacks")
            in 17..21 -> Quad("Dinner is served", "Dinner", "Wind down with hearty local favorites.", "dinner")
            else -> Quad("Late night cravings", "Soup", "Comforting soups perfect for late nights.", "soup")
        }
        return DailySuggestion(
            greeting = greeting,
            mealLabel = mealLabel,
            tagline = tagline,
            category = category,
            places = places
                .filter { it.category.equals(category, ignoreCase = true) }
                .sortedByDescending { it.rating }
                .take(5)
        )
    }

    private data class Quad(val a: String, val b: String, val c: String, val d: String)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var searchJob: Job? = null

    fun searchPlaces(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            kotlinx.coroutines.delay(200)
            try {
                applyHomeState(query)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _homeState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        applyHomeState("")
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
    private val placeRepository: PlaceRepository,
    private val foodBookmarkRepository: FoodBookmarkRepository
) : ViewModel() {

    private val _bookmarkState = MutableStateFlow<BookmarkState>(BookmarkState())
    val bookmarkState: StateFlow<BookmarkState> = _bookmarkState.asStateFlow()

    /**
     * Loads BOTH place bookmarks and specialty (food) bookmarks for the user
     * and surfaces them as a single unified list, so a user who bookmarked an
     * Ilonggo specialty from Home actually sees it in this screen.
     */
    fun loadUserBookmarks(userId: String) {
        viewModelScope.launch {
            _bookmarkState.update { it.copy(isLoading = true) }
            try {
                combine(
                    bookmarkRepository.getUserBookmarks(userId),
                    foodBookmarkRepository.getUserBookmarks(userId)
                ) { placeBookmarks, foodBookmarks ->
                    val placeItems = placeBookmarks.mapNotNull { bm ->
                        val place = placeRepository.getPlaceById(bm.placeId).firstOrNull()
                            ?: return@mapNotNull null
                        BookmarkItem(
                            key = "place:${place.id}",
                            title = place.name,
                            subtitle = place.address.ifBlank { place.category },
                            imageUrl = place.imageUrl,
                            type = BookmarkType.PLACE,
                            placeId = place.id,
                            rating = place.rating,
                            createdAt = bm.createdAt
                        )
                    }
                    val specialtyItems = foodBookmarks.mapNotNull { fb ->
                        val specialty = FoodDataset.getSpecialtyById(fb.specialtyId)
                            ?: return@mapNotNull null
                        BookmarkItem(
                            key = "specialty:${specialty.id}",
                            title = specialty.name,
                            subtitle = FoodDataset.categories[specialty.categoryId] ?: "Ilonggo specialty",
                            imageUrl = specialty.imageUrl,
                            type = BookmarkType.SPECIALTY,
                            specialtyId = specialty.id,
                            createdAt = fb.createdAt
                        )
                    }
                    placeItems + specialtyItems
                }.collect { items ->
                    _bookmarkState.update {
                        it.copy(
                            bookmarks = sortBookmarks(items, it.sortOption),
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
            } catch (e: Exception) {
                _bookmarkState.update { it.copy(error = e.message) }
            }
        }
    }

    fun removeBookmark(item: BookmarkItem, userId: String) {
        viewModelScope.launch {
            try {
                when (item.type) {
                    BookmarkType.PLACE -> item.placeId?.let {
                        bookmarkRepository.removeBookmark(it, userId)
                    }
                    BookmarkType.SPECIALTY -> item.specialtyId?.let {
                        foodBookmarkRepository.removeBookmark(it, userId)
                    }
                }
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

    private fun sortBookmarks(items: List<BookmarkItem>, option: BookmarkSortOption): List<BookmarkItem> {
        return when (option) {
            BookmarkSortOption.NAME_ASC -> items.sortedBy { it.title }
            BookmarkSortOption.RATING_HIGH -> items.sortedByDescending { it.rating }
            BookmarkSortOption.RATING_LOW -> items.sortedBy { it.rating }
            BookmarkSortOption.DATE_NEWEST -> items.sortedByDescending { it.createdAt }
            BookmarkSortOption.DATE_OLDEST -> items.sortedBy { it.createdAt }
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
    @ApplicationContext private val context: Context,
    private val placesClientProvider: Provider<PlacesClient>,
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
                    // Blank query: do not auto-load DB places (would show stale pins
                    // before the user has searched). Return empty results.
                    if (query.isBlank()) {
                        flow { emit(emptyList<Place>()) }
                    } else {
                        flow {
                            try {
                                if (!Constants.HAS_MAPS_API_KEY) {
                                    placeRepository.searchPlaces(query).collect { emit(it) }
                                    return@flow
                                }

                                val placeFields = listOf(
                                    Field.ID,
                                    Field.NAME,
                                    Field.LAT_LNG,
                                    Field.ADDRESS,
                                    Field.RATING,
                                    Field.TYPES,
                                    Field.PHOTO_METADATAS,
                                    Field.PRICE_LEVEL,
                                    Field.USER_RATINGS_TOTAL,
                                    Field.BUSINESS_STATUS
                                )

                                val iloiloCenter = MapsLatLng(Constants.ILOILO_LAT, Constants.ILOILO_LNG)
                                val iloiloBounds = CircularBounds.newInstance(iloiloCenter, 15000.0) // 15km radius

                                // Improve search by ensuring 'Iloilo' is in the query context
                                val effectiveQuery = if (query.contains("Iloilo", ignoreCase = true)) query else "$query in Iloilo"

                                val request = SearchByTextRequest.builder(effectiveQuery, placeFields)
                                    .setLocationBias(iloiloBounds)
                                    .setMaxResultCount(15)
                                    .build()

                                val response = placesClientProvider.get().searchByText(request).await()
                                val googlePlaces = response.places.mapIndexed { index, googlePlace ->
                                    val placeKey = googlePlace.id ?: googlePlace.name ?: index.toString()
                                    val photos = googlePlace.photoMetadatas.orEmpty().take(5)
                                    val photoUris = photos.mapIndexedNotNull { i, metadata ->
                                        fetchPlacePhotoUri(metadata, "${placeKey}_$i")
                                    }
                                    val primaryUri = photoUris.firstOrNull().orEmpty()

                                    Place(
                                        id = googlePlace.id?.hashCode() ?: index,
                                        name = googlePlace.name ?: "",
                                        description = googlePlace.types?.joinToString(", ") ?: "",
                                        latitude = googlePlace.latLng?.latitude ?: 0.0,
                                        longitude = googlePlace.latLng?.longitude ?: 0.0,
                                        imageUrl = primaryUri,
                                        rating = googlePlace.rating?.toFloat() ?: 0f,
                                        category = googlePlace.types?.firstOrNull()?.name ?: "Food",
                                        address = googlePlace.address ?: "",
                                        googlePlaceId = googlePlace.id.orEmpty(),
                                        photoUrls = photoUris.joinToString("||"),
                                        priceLevel = googlePlace.priceLevel ?: -1,
                                        userRatingsTotal = googlePlace.userRatingsTotal ?: 0,
                                        businessStatus = googlePlace.businessStatus?.name.orEmpty()
                                    )
                                }
                                // Cache the Google results to the DB so PlaceDetails can find them
                                if (googlePlaces.isNotEmpty()) {
                                    placeRepository.insertPlaces(googlePlaces)
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
                    // Don't overwrite places while we're locating a specific place
                    if (_mapState.value.locatePlaceId != null) {
                        _loadingPill.value = null
                        return@collect
                    }
                    _mapState.update {
                        it.copy(
                            places = results,
                            isLoading = false,
                            loadingSource = LoadingSource.NONE,
                            polylines = emptyList(),
                            routeInfo = null
                        )
                    }
                    _loadingPill.value = null
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _mapState.update {
            it.copy(
                isLoading = query.isNotBlank(),
                loadingSource = if (query.isNotBlank()) LoadingSource.SEARCH_BOX else LoadingSource.NONE,
                polylines = emptyList(),
                routeInfo = null,
                // Exit "locate mode" as soon as the user starts a new search
                locatePlaceId = null,
                // Blank query: clear pins immediately
                places = if (query.isBlank()) emptyList() else it.places
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
                isLocating = false,
                locatePlaceId = null
            )
        }
    }

    fun onPillClick(delicacy: String) {
        if (_searchQuery.value == delicacy) {
            _searchQuery.value = ""
            _loadingPill.value = null
            _mapState.update {
                it.copy(
                    isLoading = false,
                    loadingSource = LoadingSource.NONE,
                    polylines = emptyList(),
                    routeInfo = null,
                    locatePlaceId = null,
                    places = emptyList()
                )
            }
        } else {
            _searchQuery.value = delicacy
            _loadingPill.value = delicacy
            _mapState.update {
                it.copy(
                    isLoading = true,
                    loadingSource = LoadingSource.PILL,
                    polylines = emptyList(),
                    routeInfo = null,
                    locatePlaceId = null
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

    /**
     * Fetches richer details (extra photos, hours, website) for a place via the
     * Google Places Details API and persists them so PlaceDetails can show them.
     */
    fun fetchExtendedDetailsForPlace(place: Place) {
        if (!Constants.HAS_MAPS_API_KEY) return
        val googleId = place.googlePlaceId
        if (googleId.isBlank()) return
        viewModelScope.launch {
            try {
                val fields = listOf(
                    Field.ID,
                    Field.NAME,
                    Field.PHONE_NUMBER,
                    Field.WEBSITE_URI,
                    Field.OPENING_HOURS,
                    Field.PHOTO_METADATAS,
                    Field.PRICE_LEVEL,
                    Field.USER_RATINGS_TOTAL,
                    Field.BUSINESS_STATUS,
                    Field.ADDRESS,
                    Field.RATING
                )
                val request = FetchPlaceRequest.builder(googleId, fields).build()
                val response = placesClientProvider.get().fetchPlace(request).await()
                val gp = response.place
                val photoMetas = gp.photoMetadatas.orEmpty().take(8)
                val photoUris = photoMetas.mapIndexedNotNull { i, metadata ->
                    fetchPlacePhotoUri(metadata, "${googleId}_d_$i")
                }
                val updated = place.copy(
                    phone = gp.phoneNumber.orEmpty().ifBlank { place.phone },
                    website = gp.websiteUri?.toString().orEmpty().ifBlank { place.website },
                    openingHours = gp.openingHours?.weekdayText?.joinToString("||").orEmpty()
                        .ifBlank { place.openingHours },
                    photoUrls = if (photoUris.isNotEmpty()) photoUris.joinToString("||") else place.photoUrls,
                    imageUrl = photoUris.firstOrNull().orEmpty().ifBlank { place.imageUrl },
                    priceLevel = gp.priceLevel ?: place.priceLevel,
                    userRatingsTotal = gp.userRatingsTotal ?: place.userRatingsTotal,
                    businessStatus = gp.businessStatus?.name.orEmpty().ifBlank { place.businessStatus },
                    rating = gp.rating?.toFloat() ?: place.rating,
                    address = gp.address.orEmpty().ifBlank { place.address }
                )
                placeRepository.insertPlace(updated)
            } catch (e: Exception) {
                // Silent failure: details enrichment is best-effort.
            }
        }
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
            fetchCurrentLocationInternal()
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun fetchCurrentLocationInternal(): ModelLatLng? {
        return try {
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
                latLng
            } else null
        } catch (e: Exception) {
            _mapState.update { it.copy(error = "Could not get location: ${e.message}") }
            null
        }
    }

    fun setLocationPermissionGranted(granted: Boolean) {
        val wasGranted = _mapState.value.locationPermissionGranted
        _mapState.update { it.copy(locationPermissionGranted = granted) }
        // Auto-fetch location the moment we get permission so the polyline
        // starts from the user's real position the first time they tap Locate
        if (granted && !wasGranted && _mapState.value.userLocation == null) {
            fetchCurrentLocation()
        }
    }

    /**
     * Bug 1 fix: Uses the Google Directions API to get road-following route points
     * instead of drawing a straight line.
     * Always tries to fetch a fresh user location if permission is granted so the
     * polyline reliably starts from the user, not from a stale fallback.
     */
    fun drawRouteToPlace(destination: Place) {
        viewModelScope.launch {
            _mapState.update {
                it.copy(
                    isLoading = true,
                    loadingSource = LoadingSource.ROUTE,
                    polylines = emptyList(),
                    routeInfo = null
                )
            }

            // Always refresh the user's location if we have permission so the
            // origin of the polyline matches where the user actually is.
            val originLoc = if (_mapState.value.locationPermissionGranted) {
                fetchCurrentLocationInternal() ?: _mapState.value.userLocation
                ?: ModelLatLng(Constants.ILOILO_LAT, Constants.ILOILO_LNG)
            } else {
                _mapState.value.userLocation
                    ?: ModelLatLng(Constants.ILOILO_LAT, Constants.ILOILO_LNG)
            }

            try {
                if (!Constants.HAS_MAPS_API_KEY) {
                    drawStraightRoute(destination, originLoc)
                    return@launch
                }

                val origin = "${originLoc.latitude},${originLoc.longitude}"
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
                    drawStraightRoute(destination, originLoc)
                }
            } catch (e: Exception) {
                // Fallback to straight line
                drawStraightRoute(destination, originLoc)
            } finally {
                _mapState.update {
                    it.copy(
                        isLoading = false,
                        loadingSource = LoadingSource.NONE
                    )
                }
            }
        }
    }

    private fun drawStraightRoute(destination: Place, originLoc: ModelLatLng) {
        val distanceKm = DistanceUtils.calculateDistance(
            originLoc.latitude,
            originLoc.longitude,
            destination.latitude,
            destination.longitude
        )
        val route = PolylineData(
            id = "route_${destination.id}",
            placeIds = listOf(destination.id),
            points = listOf(
                originLoc,
                ModelLatLng(destination.latitude, destination.longitude)
            ),
            color = "#F7941D",
            width = 10f,
            distance = distanceKm * 1000.0
        )
        _mapState.update {
            it.copy(
                polylines = listOf(route),
                routeInfo = RouteInfo(
                    originName = if (it.userLocation != null) "Current Location" else "Iloilo City Center",
                    destinationName = destination.name,
                    distanceKm = distanceKm
                )
            )
        }
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

    fun locatePlaceOnMap(placeId: Int, drawRoute: Boolean = false) {
        viewModelScope.launch {
            // Clear any pending search so its results don't overwrite our single pin
            _searchQuery.value = ""
            _mapState.update {
                it.copy(
                    isLocating = true,
                    locatePlaceId = placeId,
                    polylines = emptyList(),
                    routeInfo = null,
                    places = emptyList()
                )
            }
            val place = placeRepository.getPlaceById(placeId).firstOrNull()
            if (place != null) {
                _mapState.update {
                    it.copy(
                        places = listOf(place),
                        selectedPlace = place,
                        isLocating = false,
                        locatePlaceId = placeId
                    )
                }
                observeBookmarkStatus(placeId)
                if (drawRoute) {
                    drawRouteToPlace(place)
                }
            } else {
                _mapState.update { it.copy(isLocating = false, error = "Place not found") }
            }
        }
    }

    fun clearPolylines() {
        _mapState.update { it.copy(polylines = emptyList(), routeInfo = null) }
    }

    /** Called when navigating away from the map screen (e.g. into details). */
    fun prepareForNavigation() {
        _mapState.update {
            it.copy(
                polylines = emptyList(),
                routeInfo = null,
                selectedPlace = null
            )
        }
    }

    /**
     * Aggressively resets every piece of map state that could survive a
     * navigation back to this screen and contaminate a new "Find Near You"
     * search (most importantly: a stale polyline and the locate-mode flag).
     */
    fun resetForNewSearch() {
        bookmarkObserverJob?.cancel()
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
                isLocating = false,
                locatePlaceId = null,
                isSelectedPlaceBookmarked = false
            )
        }
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

    private suspend fun fetchPlacePhotoUri(
        photoMetadata: PhotoMetadata,
        placeKey: String
    ): String? {
        return try {
            val photoDir = File(context.cacheDir, "place_photos").apply { mkdirs() }
            val photoFile = File(photoDir, "${placeKey.stableFileName()}.jpg")
            if (photoFile.exists()) {
                return Uri.fromFile(photoFile).toString()
            }

            val request = FetchPhotoRequest.builder(photoMetadata)
                .setMaxWidth(800)
                .setMaxHeight(500)
                .build()
            val bitmap = placesClientProvider.get().fetchPhoto(request).await().bitmap
            withContext(Dispatchers.IO) {
                photoFile.outputStream().use { stream ->
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 88, stream)
                }
            }
            Uri.fromFile(photoFile).toString()
        } catch (e: Exception) {
            null
        }
    }

    private fun String.stableFileName(): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(toByteArray())
            .joinToString("") { "%02x".format(it) }
        return digest.take(24)
    }
}

@HiltViewModel
class PlaceDetailsViewModel @Inject constructor(
    private val placeRepository: PlaceRepository,
    private val foodRepository: FoodRepository,
    private val commentRepository: CommentRepository,
    private val bookmarkRepository: BookmarkRepository,
    @ApplicationContext private val context: Context,
    private val placesClientProvider: Provider<PlacesClient>,
    private val okHttpClient: OkHttpClient,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val placeId: Int = checkNotNull(savedStateHandle[RouteConstants.PLACE_ID_ARG])

    private val _uiState = MutableStateFlow<PlaceDetail?>(null)
    val uiState: StateFlow<PlaceDetail?> = _uiState.asStateFlow()

    private val _googleReviews = MutableStateFlow<List<GoogleReview>>(emptyList())
    private val _isEnriching = MutableStateFlow(false)
    private val _isLoadingReviews = MutableStateFlow(false)

    private var enrichmentTriggered = false

    init {
        loadPlaceDetails()
    }

    private fun loadPlaceDetails() {
        viewModelScope.launch {
            val baseDetail = combine(
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
                        averageRating = if (avgRating != null && avgRating > 0f) avgRating else place.rating
                    )
                } else {
                    null
                }
            }

            combine(
                baseDetail,
                _googleReviews,
                _isEnriching,
                _isLoadingReviews
            ) { detail, reviews, enriching, loadingReviews ->
                detail?.copy(
                    googleReviews = reviews,
                    isEnrichingDetails = enriching,
                    isLoadingReviews = loadingReviews
                )
            }.collect { detail ->
                _uiState.value = detail
                if (!enrichmentTriggered && detail != null && detail.place.googlePlaceId.isNotBlank()) {
                    enrichmentTriggered = true
                    enrichFromGoogle(detail.place)
                }
            }
        }
    }

    private fun enrichFromGoogle(place: Place) {
        if (!Constants.HAS_MAPS_API_KEY) return
        viewModelScope.launch {
            _isEnriching.value = true
            try {
                val fields = listOf(
                    Field.ID,
                    Field.NAME,
                    Field.PHONE_NUMBER,
                    Field.WEBSITE_URI,
                    Field.OPENING_HOURS,
                    Field.PHOTO_METADATAS,
                    Field.PRICE_LEVEL,
                    Field.USER_RATINGS_TOTAL,
                    Field.BUSINESS_STATUS,
                    Field.ADDRESS,
                    Field.RATING
                )
                val request = FetchPlaceRequest.builder(place.googlePlaceId, fields).build()
                val response = placesClientProvider.get().fetchPlace(request).await()
                val gp = response.place

                val photoMetas = gp.photoMetadatas.orEmpty().take(8)
                val photoUris = photoMetas.mapIndexedNotNull { i, metadata ->
                    fetchPlacePhotoUri(metadata, "${place.googlePlaceId}_dv_$i")
                }
                val updated = place.copy(
                    phone = gp.phoneNumber.orEmpty().ifBlank { place.phone },
                    website = gp.websiteUri?.toString().orEmpty().ifBlank { place.website },
                    openingHours = gp.openingHours?.weekdayText?.joinToString("||").orEmpty()
                        .ifBlank { place.openingHours },
                    photoUrls = if (photoUris.isNotEmpty()) photoUris.joinToString("||") else place.photoUrls,
                    imageUrl = photoUris.firstOrNull().orEmpty().ifBlank { place.imageUrl },
                    priceLevel = gp.priceLevel ?: place.priceLevel,
                    userRatingsTotal = gp.userRatingsTotal ?: place.userRatingsTotal,
                    businessStatus = gp.businessStatus?.name.orEmpty().ifBlank { place.businessStatus },
                    rating = gp.rating?.toFloat() ?: place.rating,
                    address = gp.address.orEmpty().ifBlank { place.address }
                )
                placeRepository.insertPlace(updated)
            } catch (e: Exception) {
                // best-effort
            } finally {
                _isEnriching.value = false
            }
            // Google's Places SDK doesn't expose reviews — fetch via legacy
            // Place Details web service instead.
            fetchReviewsViaWebService(place.googlePlaceId)
        }
    }

    private suspend fun fetchReviewsViaWebService(googlePlaceId: String) {
        if (!Constants.HAS_MAPS_API_KEY) return
        _isLoadingReviews.value = true
        try {
            val url = "https://maps.googleapis.com/maps/api/place/details/json" +
                    "?place_id=$googlePlaceId" +
                    "&fields=reviews,rating,user_ratings_total" +
                    "&key=${Constants.MAPS_API_KEY}"
            val json = withContext(Dispatchers.IO) {
                val request = Request.Builder().url(url).build()
                okHttpClient.newCall(request).execute().body?.string().orEmpty()
            }
            if (json.isBlank()) return
            val root = JSONObject(json)
            val result = root.optJSONObject("result") ?: return
            val reviewsArr = result.optJSONArray("reviews") ?: return
            val parsed = mutableListOf<GoogleReview>()
            for (i in 0 until reviewsArr.length()) {
                val r = reviewsArr.getJSONObject(i)
                parsed += GoogleReview(
                    authorName = r.optString("author_name"),
                    rating = r.optDouble("rating", 0.0).toFloat(),
                    text = r.optString("text"),
                    relativeTime = r.optString("relative_time_description"),
                    profilePhotoUrl = r.optString("profile_photo_url")
                )
            }
            _googleReviews.value = parsed
        } catch (e: Exception) {
            // best-effort
        } finally {
            _isLoadingReviews.value = false
        }
    }

    private suspend fun fetchPlacePhotoUri(
        photoMetadata: PhotoMetadata,
        placeKey: String
    ): String? {
        return try {
            val photoDir = File(context.cacheDir, "place_photos").apply { mkdirs() }
            val photoFile = File(photoDir, "${placeKey.stableFileName()}.jpg")
            if (photoFile.exists()) {
                return Uri.fromFile(photoFile).toString()
            }
            val request = FetchPhotoRequest.builder(photoMetadata)
                .setMaxWidth(800)
                .setMaxHeight(500)
                .build()
            val bitmap = placesClientProvider.get().fetchPhoto(request).await().bitmap
            withContext(Dispatchers.IO) {
                photoFile.outputStream().use { stream ->
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 88, stream)
                }
            }
            Uri.fromFile(photoFile).toString()
        } catch (e: Exception) {
            null
        }
    }

    private fun String.stableFileName(): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(toByteArray())
            .joinToString("") { "%02x".format(it) }
        return digest.take(24)
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
