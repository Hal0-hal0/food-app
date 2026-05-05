# Gastro Nameet - Kotlin Native Implementation Guide

## 📋 Project Setup Instructions

### Prerequisites
- Android Studio Flamingo or newer
- JDK 17+
- Android SDK 34
- Gradle 8.1+

### Step 1: Create New Android Project

```bash
# Using Android Studio:
1. File → New → New Android Project
2. Select "Empty Activity" template
3. Configure:
   - Name: gastro_nameet_kotlin
   - Package: com.gastro.nameet
   - Min SDK: 24 (Android 7.0)
   - Target SDK: 34
   - Language: Kotlin
```

### Step 2: Project Structure Setup

Create the following directory structure:

```
gastro_nameet_kotlin/
├── app/src/main/
│   ├── kotlin/com/gastro/nameet/
│   │   ├── database/
│   │   │   ├── entity/      (DatabaseEntities.kt)
│   │   │   ├── dao/         (DatabaseDAOs.kt)
│   │   │   └── AppDatabase.kt
│   │   ├── model/           (Models.kt)
│   │   ├── repository/      (Repositories.kt)
│   │   ├── viewmodel/       (ViewModels.kt)
│   │   ├── ui/
│   │   │   ├── theme/       (Color.kt, Type.kt, Theme.kt)
│   │   │   ├── screens/     (Screen composables)
│   │   │   └── components/  (Reusable components)
│   │   ├── service/         (LocationService.kt, etc)
│   │   ├── util/            (Utilities.kt, Constants.kt)
│   │   ├── di/              (ServiceLocator.kt - Hilt)
│   │   ├── MainActivity.kt
│   │   └── GastroApp.kt
│   └── res/
│       ├── values/strings.xml
│       ├── drawable/
│       └── mipmap/
└── build.gradle.kts (use app_build.gradle.kts content)
```

### Step 3: Configure build.gradle.kts

Replace the app-level `build.gradle.kts` with the content from `app_build.gradle.kts`.

### Step 4: Set Local Properties

Create `local.properties` in the project root:

```properties
# Do NOT commit this file to version control
sdk.dir=/path/to/android/sdk
MAPS_API_KEY=YOUR_MAPS_API_KEY_HERE
```

### Step 5: Create Resource Files

#### colors.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="primary_orange">#F7941D</color>
    <color name="light_orange">#FFB84D</color>
    <color name="dark_blue_gray">#2C3E50</color>
    <color name="white">#FFFFFF</color>
    <color name="black">#1C1B1F</color>
    <color name="error">#D32F2F</color>
    <color name="success">#388E3C</color>
    <color name="gray100">#F5F5F5</color>
    <color name="gray500">#9E9E9E</color>
</resources>
```

#### dimens.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Spacing -->
    <dimen name="spacing_4">4dp</dimen>
    <dimen name="spacing_8">8dp</dimen>
    <dimen name="spacing_12">12dp</dimen>
    <dimen name="spacing_16">16dp</dimen>
    <dimen name="spacing_20">20dp</dimen>
    <dimen name="spacing_24">24dp</dimen>
    <dimen name="spacing_32">32dp</dimen>
    
    <!-- Corner radius -->
    <dimen name="corner_small">8dp</dimen>
    <dimen name="corner_medium">12dp</dimen>
    <dimen name="corner_large">16dp</dimen>
    
    <!-- Icons -->
    <dimen name="icon_small">24dp</dimen>
    <dimen name="icon_medium">32dp</dimen>
    <dimen name="icon_large">48dp</dimen>
    
    <!-- Text sizes -->
    <dimen name="text_display">32sp</dimen>
    <dimen name="text_headline">28sp</dimen>
    <dimen name="text_title">20sp</dimen>
    <dimen name="text_body">16sp</dimen>
    <dimen name="text_label">14sp</dimen>
    <dimen name="text_caption">12sp</dimen>
</resources>
```

### Step 6: Add Poppins Font

1. Download Poppins font files from Google Fonts
2. Create `res/font/` directory
3. Add font files:
   - poppins_light.ttf
   - poppins_regular.ttf
   - poppins_medium.ttf
   - poppins_semibold.ttf
   - poppins_bold.ttf

### Step 7: Copy Kotlin Source Files

Copy all the provided Kotlin files to appropriate directories:

```bash
# Database layer
cp DatabaseEntities.kt app/src/main/kotlin/com/gastro/nameet/database/entity/
cp DatabaseDAOs.kt app/src/main/kotlin/com/gastro/nameet/database/dao/
cp AppDatabase.kt app/src/main/kotlin/com/gastro/nameet/database/

# Models
cp Models.kt app/src/main/kotlin/com/gastro/nameet/model/

# Repository layer
cp Repositories.kt app/src/main/kotlin/com/gastro/nameet/repository/

# UI & Theme
cp Color.kt app/src/main/kotlin/com/gastro/nameet/ui/theme/
cp Type.kt app/src/main/kotlin/com/gastro/nameet/ui/theme/
cp Theme.kt app/src/main/kotlin/com/gastro/nameet/ui/theme/

# ViewModels
cp ViewModels.kt app/src/main/kotlin/com/gastro/nameet/viewmodel/

# Utilities
cp Utilities.kt app/src/main/kotlin/com/gastro/nameet/util/
cp Constants.kt app/src/main/kotlin/com/gastro/nameet/util/

# DI
cp ServiceLocator.kt app/src/main/kotlin/com/gastro/nameet/di/

# App
cp GastroApp.kt app/src/main/kotlin/com/gastro/nameet/
```

### Step 8: Create MainActivity

```kotlin
package com.gastro.nameet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.gastro.nameet.ui.navigation.AppNavigation
import com.gastro.nameet.ui.theme.GastroTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GastroTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
```

---

## 🎨 UI Screen Implementation

### 1. Main Screens to Implement

#### Splash Screen
```kotlin
// Compose: Shows splash animation
// Duration: 2 seconds
// Navigation: → StartScreen
```

#### Start Screen
```kotlin
// Options: Login / Sign Up
// Featured restaurants carousel
```

#### Login/Register Screen
```kotlin
// Email validation
// Password confirmation
// Name field (register only)
```

#### Home Screen
```kotlin
// Search bar
// Featured places (top 5 by rating)
// Categories with horizontal scrolling:
//   - Breakfast
//   - Lunch
//   - Dinner
//   - Dessert
//   - Snacks
//   - Soup
// Tap category → see all in that category
```

#### Maps Screen
```kotlin
// Google Maps integration
// Markers for all places
// Polyline feature:
//   - Select multiple places
//   - Draw route between them
//   - Show total distance
//   - Show estimated travel time
// Tap marker → PlaceDetailsScreen
```

#### Place Details Screen
```kotlin
// Place info (name, rating, address)
// Image gallery
// Foods/Menu items
// Comments section
// Bookmark button
// Call/Directions buttons
```

#### Specialties Screen
```kotlin
// Tab-based or list view
// Filter by category
// Search within category
```

#### Bookmarks Screen
```kotlin
// List of bookmarked places
// Remove bookmark option
// Empty state message
```

#### Profile Screen
```kotlin
// User info display
// Edit profile option
// Settings link
// Dark mode toggle
// Logout button
```

---

## 🗺️ Maps & Polyline Implementation

### Key Implementation Points

```kotlin
// In MapsScreen.kt
@Composable
fun MapsScreen(viewModel: MapsViewModel = hiltViewModel()) {
    val state by viewModel.mapState.collectAsState()
    
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onMapClick = { latLng ->
            viewModel.addPolylinePoint(latLng)
        }
    ) {
        // Draw markers for each place
        state.places.forEach { place ->
            Marker(
                position = place.latitude to place.longitude,
                title = place.name,
                onClick = { viewModel.selectPlace(place.id) }
            )
        }
        
        // Draw polylines
        state.polylines.forEach { polyline ->
            Polyline(
                points = polyline.points.map { 
                    it.latitude to it.longitude 
                },
                color = Color(android.graphics.Color.parseColor(polyline.color)),
                width = polyline.width
            )
        }
    }
    
    // UI for polyline controls
    if (state.selectedPlace != null) {
        PlaceInfoBottomSheet(place = state.selectedPlace)
    }
}

// Polyline creation logic
fun createPolyline(placeIds: List<Int>, places: List<Place>) {
    val points = placeIds.mapNotNull { id ->
        places.find { it.id == id }?.let { 
            LatLng(it.latitude, it.longitude) 
        }
    }
    
    val distance = calculateTotalDistance(points)
    val polylineData = PolylineData(
        id = UUID.randomUUID().toString(),
        placeIds = placeIds,
        points = points,
        distance = distance
    )
    
    viewModel.addPolyline(polylineData)
}

// Distance calculation
fun calculateTotalDistance(points: List<LatLng>): Double {
    var totalDistance = 0.0
    for (i in 0 until points.size - 1) {
        totalDistance += DistanceUtils.calculateDistance(
            points[i].latitude, points[i].longitude,
            points[i + 1].latitude, points[i + 1].longitude
        )
    }
    return totalDistance
}
```

---

## 📱 Database Seeding (Initial Data)

Create a sample data seeder for testing:

```kotlin
// In database/SampleData.kt
object SampleData {
    fun getSamplePlaces() = listOf(
        Place(
            id = 1,
            name = "La Paz Batchoy",
            description = "Traditional Ilocano noodle soup",
            latitude = 10.3157,
            longitude = 123.8854,
            imageUrl = "https://example.com/lapaz.jpg",
            rating = 4.8f,
            category = "lunch",
            phone = "+63 32 XXX XXXX",
            address = "La Paz, Iloilo"
        ),
        // ... more places
    )
}
```

Insert on app first launch:

```kotlin
@HiltViewModel
class AppViewModel @Inject constructor(
    private val placeRepository: PlaceRepository
) : ViewModel() {
    init {
        viewModelScope.launch {
            // Check if database is empty
            val places = placeRepository.getAllPlaces().first()
            if (places.isEmpty()) {
                placeRepository.insertPlaces(SampleData.getSamplePlaces())
            }
        }
    }
}
```

---

## 🔐 API Key Management

### Setup Process

1. **Get API Key**
   - Go to Google Cloud Console
   - Create new project or select existing
   - Enable Maps SDK for Android
   - Create Android API Key
   - Add SHA-1 fingerprints

2. **Add to local.properties**
   ```properties
   MAPS_API_KEY=YOUR_KEY_HERE
   ```

3. **BuildConfig Access**
   ```kotlin
   val apiKey = BuildConfig.MAPS_API_KEY
   ```

4. **AndroidManifest.xml**
   ```xml
   <meta-data
       android:name="com.google.android.geo.API_KEY"
       android:value="@string/google_maps_api_key" />
   ```

5. **strings.xml**
   ```xml
   <string name="google_maps_api_key">${MAPS_API_KEY}</string>
   ```

---

## ✅ Testing Checklist

- [ ] Splash screen displays correctly
- [ ] Login/Register validation works
- [ ] Home screen loads sample data
- [ ] Search functionality works
- [ ] Maps load with markers
- [ ] Polyline creation works
- [ ] Place details display correctly
- [ ] Bookmarks save/remove correctly
- [ ] Comments load and display
- [ ] Profile settings work
- [ ] Dark mode toggles
- [ ] Permissions requested properly
- [ ] Database operations work offline
- [ ] Error handling shows user-friendly messages

---

## 🚀 Build & Run

```bash
# Build project
./gradlew build

# Run on emulator/device
./gradlew installDebug

# Release build
./gradlew assembleRelease
```

---

## 📚 Key Dependencies Explained

| Dependency | Purpose |
|-----------|---------|
| **Jetpack Compose** | Modern declarative UI framework |
| **Room** | SQLite abstraction with type safety |
| **Hilt** | Dependency injection for clean code |
| **Retrofit** | REST API calls |
| **Coroutines** | Async operations non-blocking |
| **Google Maps** | Map display and location |
| **Material 3** | Latest Material Design system |
| **Coil** | Image loading library |

---

## 🎯 Next Steps

1. ✅ Create project structure
2. ✅ Add dependencies
3. ✅ Setup database
4. → Implement UI screens
5. → Add navigation
6. → Integrate Maps API
7. → Test thoroughly
8. → Optimize performance
9. → Release to Play Store

---

## 🆘 Troubleshooting

**Issue: Compose not working**
- Solution: Ensure `buildFeatures { compose = true }` in build.gradle.kts

**Issue: Hilt not injecting**
- Solution: Add `@HiltAndroidApp` to Application class and `@AndroidEntryPoint` to Activities

**Issue: Maps blank**
- Solution: Verify API key in local.properties and AndroidManifest.xml

**Issue: Database empty**
- Solution: Call seed data in ViewModel.init()

---

**Last Updated**: May 3, 2026  
**Version**: 1.0  
**Status**: Ready for Implementation
