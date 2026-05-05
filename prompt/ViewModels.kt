package com.gastro.nameet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gastro.nameet.model.AuthState
import com.gastro.nameet.model.Place
import com.gastro.nameet.model.Bookmark
import com.gastro.nameet.model.HomeState
import com.gastro.nameet.model.BookmarkState
import com.gastro.nameet.repository.AuthRepository
import com.gastro.nameet.repository.BookmarkRepository
import com.gastro.nameet.repository.PlaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
        loadPlaces()
    }

    private fun loadPlaces() {
        viewModelScope.launch {
            _homeState.update { it.copy(isLoading = true) }
            try {
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
            } catch (e: Exception) {
                _homeState.update { it.copy(isLoading = false, error = e.message) }
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
                    val places = bookmarks.mapNotNull { 
                        placeRepository.getPlaceById(it.placeId)
                    }
                    var placesList = emptyList<Place>()
                    places.forEach { placeFlow ->
                        placeFlow.collect { place ->
                            if (place != null) {
                                placesList = placesList + place
                            }
                        }
                    }
                    _bookmarkState.update { 
                        it.copy(bookmarks = placesList, isLoading = false)
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

    fun clearError() {
        _bookmarkState.update { it.copy(error = null) }
    }
}
