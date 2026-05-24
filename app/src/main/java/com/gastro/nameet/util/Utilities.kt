package com.gastro.nameet.util

import com.gastro.nameet.BuildConfig
import com.gastro.nameet.model.LatLng

object Constants {
    // App Info
    const val APP_NAME = "Gastro Nameet"
    const val APP_VERSION = "1.0.0"

    // API Configuration
    const val MAPS_API_KEY = BuildConfig.MAPS_API_KEY
    val HAS_MAPS_API_KEY: Boolean = MAPS_API_KEY.isNotBlank()
    const val BASE_API_URL = "https://api.gastronameet.com/"

    // Database
    const val DATABASE_NAME = "gastro_nameet.db"

    // Shared Preferences Keys
    object PreferenceKeys {
        const val USER_ID = "user_id"
        const val USER_EMAIL = "user_email"
        const val USER_NAME = "user_name"
        const val IS_LOGGED_IN = "is_logged_in"
        const val THEME_MODE = "theme_mode"
    }

    // Place Categories
    enum class FoodCategory(val displayName: String) {
        BREAKFAST("Breakfast"),
        LUNCH("Lunch"),
        DINNER("Dinner"),
        DESSERT("Dessert"),
        SNACKS("Snacks"),
        SOUP("Soup")
    }

    // Map
    const val DEFAULT_ZOOM = 13f
    const val MAX_ZOOM = 20f
    const val MIN_ZOOM = 5f

    // Default Iloilo City center
    const val ILOILO_LAT = 10.7202
    const val ILOILO_LNG = 122.5621

    // Polyline
    const val POLYLINE_WIDTH = 5f
    const val POLYLINE_COLOR = "#F7941D"

    // Animation Durations
    const val ANIMATION_DURATION_SHORT = 200
    const val ANIMATION_DURATION_MEDIUM = 300
    const val ANIMATION_DURATION_LONG = 500

    // Network Timeouts
    const val CONNECT_TIMEOUT = 15L // seconds
    const val READ_TIMEOUT = 15L
    const val WRITE_TIMEOUT = 15L
}

object RouteConstants {
    const val SPLASH = "splash"
    const val START = "start"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val MAPS = "maps"
    const val PLACE_DETAILS = "place_details"
    const val SPECIALTIES = "specialties"
    const val ILONGGO_SPECIALTIES = "ilonggo_specialties"
    const val BOOKMARKS = "bookmarks"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val FOOD_DETAIL = "food_detail"

    // Arguments
    const val PLACE_ID_ARG = "placeId"
    const val CATEGORY_ARG = "category"
    const val FOOD_ID_ARG = "foodId"
    const val MAP_QUERY_ARG = "mapQuery"
    const val LOCATE_PLACE_ID_ARG = "locatePlaceId"
    const val NO_PLACE_ID = -2147483648
}

object DistanceUtils {
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0 // Earth's radius in kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }

    fun formatDistance(km: Double): String {
        return when {
            km < 0.1 -> "${(km * 1000).toInt()} m"
            km < 1.0 -> String.format("%.2f km", km)
            else -> String.format("%.1f km", km)
        }
    }
}

object DateTimeUtils {
    fun formatTimestamp(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }

    fun formatTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        return when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000} min ago"
            diff < 86400000 -> "${diff / 3600000} hours ago"
            diff < 604800000 -> "${diff / 86400000} days ago"
            else -> formatTimestamp(timestamp)
        }
    }
}

object ValidationUtils {
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        // Minimum 6 characters
        return password.length >= 6
    }

    fun isValidName(name: String): Boolean {
        return name.isNotBlank() && name.length >= 2
    }

    fun isValidPhone(phone: String): Boolean {
        return android.util.Patterns.PHONE.matcher(phone).matches()
    }
}

object StringUtils {
    fun truncate(text: String, maxLength: Int): String {
        return if (text.length > maxLength) {
            text.substring(0, maxLength) + "..."
        } else {
            text
        }
    }

    fun capitalize(text: String): String {
        return text.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}

/**
 * Decodes a Google Maps encoded polyline string into a list of LatLng points.
 * @see <a href="https://developers.google.com/maps/documentation/utilities/polylinealgorithm">Polyline Algorithm</a>
 */
object PolylineDecoder {
    fun decode(encoded: String): List<LatLng> {
        val poly = mutableListOf<LatLng>()
        var index = 0
        var lat = 0
        var lng = 0

        while (index < encoded.length) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            poly.add(LatLng(lat / 1E5, lng / 1E5))
        }
        return poly
    }
}

// Extension functions
fun String.toLatLng(): LatLng? {
    val parts = this.split(",")
    return if (parts.size == 2) {
        try {
            LatLng(parts[0].toDouble(), parts[1].toDouble())
        } catch (e: NumberFormatException) {
            null
        }
    } else {
        null
    }
}

fun LatLng.toGoogleLatLng(): com.google.android.gms.maps.model.LatLng {
    return com.google.android.gms.maps.model.LatLng(this.latitude, this.longitude)
}

fun List<LatLng>.toGoogleLatLngList(): List<com.google.android.gms.maps.model.LatLng> {
    return this.map { it.toGoogleLatLng() }
}

fun Float.toPercentage(): String {
    return String.format("%.1f%%", this * 100)
}
