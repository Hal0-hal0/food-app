# Gastro Nameet - Kotlin Android Native Conversion

## Project Overview
Converting the Flutter "Gastro Nameet" app to a native Kotlin Android application with the following specifications:

### Key Requirements
✅ **Language**: Kotlin (100% native Android)
✅ **Database**: SQLite (using Room ORM)
✅ **API Integration**: Maps API (with empty key placeholder)
✅ **New Feature**: Polyline on maps
✅ **Removed**: Events page
✅ **UI**: Modern, clean Material Design 3
✅ **Architecture**: MVVM with Kotlin Coroutines

---

## Project Structure

```
gastro_nameet_kotlin/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/
│   │   │   │   └── com/gastro/nameet/
│   │   │   │       ├── MainActivity.kt
│   │   │   │       ├── MyApp.kt
│   │   │   │       ├── ui/
│   │   │   │       │   ├── theme/
│   │   │   │       │   │   ├── Color.kt
│   │   │   │       │   │   ├── Type.kt
│   │   │   │       │   │   └── Theme.kt
│   │   │   │       │   ├── screens/
│   │   │   │       │   │   ├── SplashScreen.kt
│   │   │   │       │   │   ├── StartScreen.kt
│   │   │   │       │   │   ├── LoginScreen.kt
│   │   │   │       │   │   ├── HomeScreen.kt
│   │   │   │       │   │   ├── MapsScreen.kt
│   │   │   │       │   │   ├── SpecialtiesScreen.kt
│   │   │   │       │   │   ├── BookmarksScreen.kt
│   │   │   │       │   │   ├── ProfileScreen.kt
│   │   │   │       │   │   └── PlaceDetailsScreen.kt
│   │   │   │       │   ├── components/
│   │   │   │       │   │   ├── BottomNavBar.kt
│   │   │   │       │   │   ├── FoodCard.kt
│   │   │   │       │   │   ├── FoodModal.kt
│   │   │   │       │   │   └── HorizontalFoodList.kt
│   │   │   │       │   └── navigation/
│   │   │   │       │       └── AppNavigation.kt
│   │   │   │       ├── viewmodel/
│   │   │   │       │   ├── HomeViewModel.kt
│   │   │   │       │   ├── MapsViewModel.kt
│   │   │   │       │   ├── AuthViewModel.kt
│   │   │   │       │   └── BookmarkViewModel.kt
│   │   │   │       ├── model/
│   │   │   │       │   ├── Place.kt
│   │   │   │       │   ├── Food.kt
│   │   │   │       │   ├── Bookmark.kt
│   │   │   │       │   └── Comment.kt
│   │   │   │       ├── database/
│   │   │   │       │   ├── AppDatabase.kt
│   │   │   │       │   ├── dao/
│   │   │   │       │   │   ├── PlaceDao.kt
│   │   │   │       │   │   ├── FoodDao.kt
│   │   │   │       │   │   ├── BookmarkDao.kt
│   │   │   │       │   │   └── CommentDao.kt
│   │   │   │       │   └── entity/
│   │   │   │       │       ├── PlaceEntity.kt
│   │   │   │       │       ├── FoodEntity.kt
│   │   │   │       │       ├── BookmarkEntity.kt
│   │   │   │       │       └── CommentEntity.kt
│   │   │   │       ├── repository/
│   │   │   │       │   ├── PlaceRepository.kt
│   │   │   │       │   ├── FoodRepository.kt
│   │   │   │       │   ├── BookmarkRepository.kt
│   │   │   │       │   └── AuthRepository.kt
│   │   │   │       ├── service/
│   │   │   │       │   ├── AuthService.kt
│   │   │   │       │   ├── PlacesService.kt
│   │   │   │       │   └── LocationService.kt
│   │   │   │       ├── util/
│   │   │   │       │   ├── Constants.kt
│   │   │   │       │   ├── Preferences.kt
│   │   │   │       │   └── Logger.kt
│   │   │   │       └── di/
│   │   │   │           └── ServiceLocator.kt
│   │   │   ├── res/
│   │   │   │   ├── drawable/
│   │   │   │   │   ├── ic_launcher.png
│   │   │   │   │   ├── bg_splash.png
│   │   │   │   │   ├── bg_login.png
│   │   │   │   │   └── (other images)
│   │   │   │   ├── layout/ (if using XML layouts)
│   │   │   │   ├── values/
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   ├── dimens.xml
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   └── styles.xml
│   │   │   │   └── mipmap/
│   │   │   └── AndroidManifest.xml
│   │   ├── test/
│   │   └── androidTest/
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── local.properties
```

---

## Technologies & Dependencies

### Core
- **Kotlin 1.9+**
- **Android SDK 34+** (Target)
- **Min SDK 24** (Android 7.0)

### UI & Navigation
- **Jetpack Compose** - Modern declarative UI
- **Material Design 3** - Latest Material design
- **Navigation Compose** - Type-safe navigation
- **Accompanist** - Utility libraries for Compose

### Data & Database
- **Room ORM** - SQLite database abstraction
- **DataStore** - Secure preferences
- **Kotlin Serialization** - JSON serialization

### Networking & Maps
- **Retrofit** - REST client
- **OkHttp** - HTTP client with interceptors
- **Google Maps Compose** - Maps integration
- **Google Play Services Location** - Geolocation

### Architecture
- **Hilt** - Dependency injection
- **Kotlin Coroutines** - Async operations
- **Flow** - Reactive streams
- **ViewModel** - UI state management
- **LiveData** - Reactive data holder

---

## Key Features Implementation

### 1. **Authentication**
- Splash screen with brand logo
- Login/Sign-up flow
- Local user session management via DataStore
- Email validation

### 2. **Home Screen**
- Horizontal scrolling food categories
- Featured restaurants/places
- Search functionality
- Quick navigation to specialties

### 3. **Maps Integration** ⭐
- Google Maps with real restaurant markers
- **Polylines Feature** (NEW)
  - Route between multiple restaurants
  - Polyline customization (color, width, pattern)
  - Distance calculation
  - Tap-to-route feature
- Location-based search
- Place details on tap

### 4. **Place Details**
- Image gallery
- Rating and reviews
- Bookmarking functionality
- Comments section (Firebase comments)
- Location details with map

### 5. **Specialties**
- Breakfast
- Lunch
- Dinner
- Dessert
- Snacks
- Soup

### 6. **Bookmarks**
- Saved restaurants
- Local SQLite persistence
- Quick access

### 7. **Profile**
- User information
- Settings
- Logout functionality

### 8. **Database Structure**

#### places table
```sql
CREATE TABLE places (
  id INTEGER PRIMARY KEY,
  name TEXT NOT NULL,
  description TEXT,
  latitude REAL NOT NULL,
  longitude REAL NOT NULL,
  image_url TEXT,
  rating REAL,
  category TEXT,
  phone TEXT,
  address TEXT,
  created_at TIMESTAMP
);
```

#### foods table
```sql
CREATE TABLE foods (
  id INTEGER PRIMARY KEY,
  place_id INTEGER NOT NULL,
  name TEXT NOT NULL,
  description TEXT,
  image_url TEXT,
  category TEXT,
  FOREIGN KEY(place_id) REFERENCES places(id)
);
```

#### bookmarks table
```sql
CREATE TABLE bookmarks (
  id INTEGER PRIMARY KEY,
  place_id INTEGER NOT NULL,
  user_id TEXT,
  created_at TIMESTAMP,
  FOREIGN KEY(place_id) REFERENCES places(id)
);
```

#### comments table
```sql
CREATE TABLE comments (
  id INTEGER PRIMARY KEY,
  place_id INTEGER NOT NULL,
  user_name TEXT,
  comment TEXT,
  rating REAL,
  created_at TIMESTAMP,
  FOREIGN KEY(place_id) REFERENCES places(id)
);
```

---

## API Key Management

### Secure Storage Strategy
```kotlin
// In BuildConfig or local.properties (NOT committed to git)
MAPS_API_KEY="" // ← Empty for now, add your key here locally

// Access in code:
val mapsApiKey = BuildConfig.MAPS_API_KEY
```

### How to Add Key Later
1. Get your Google Maps API key from Google Cloud Console
2. Add to `local.properties`:
   ```
   MAPS_API_KEY=your_actual_key_here
   ```
3. The build system loads it at compile time
4. Never commit `local.properties` or `secrets.properties`

---

## Modern, Clean UI Design

### Design Philosophy
✨ **Clean Minimalism**
- White/light backgrounds
- Consistent spacing (8dp grid)
- Rounded corners (12-16dp)
- Shadow depth for elevation
- Color harmony from Material 3

### Color Palette
```kotlin
Primary:        #F7941D (Orange) - Brand color
Secondary:      #FFB84D (Light Orange)
Tertiary:       #2C3E50 (Dark Blue-Gray)
Error:          #D32F2F (Red)
Surface:        #FFFFFF (White)
OnSurface:      #1C1B1F (Black)
```

### Typography
- **Poppins Font Family** (kept from original)
- Headlines: Bold, 24-28sp
- Body: Regular, 14-16sp
- Captions: Light, 12sp

### Components Style
- **Cards**: Elevation 2-4dp, 12dp radius
- **Buttons**: 10dp radius, 48dp min height
- **Inputs**: Outline style, 8dp radius
- **Icons**: 24x24dp standard size

---

## Build & Configuration

### build.gradle.kts (Project)
```kotlin
plugins {
    id("com.android.application") version "8.1.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.google.dagger.hilt.android") version "2.47" apply false
    id("com.google.devtools.ksp") version "1.9.0-1.0.11" apply false
}
```

### build.gradle.kts (App)
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
    id("kotlinx-serialization")
}

dependencies {
    // Jetpack Compose
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.navigation:navigation-compose:2.7.5")
    
    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.47")
    kapt("com.google.dagger:hilt-compiler:2.47")
    
    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    
    // Google Play Services
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.maps.android:maps-ktx:5.0.0")
    implementation("com.google.maps.android:maps-compose:4.1.1")
    
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
```

---

## Development Checklist

- [ ] Create Kotlin project structure
- [ ] Setup database with Room
- [ ] Implement authentication (local)
- [ ] Create base UI components
- [ ] Build home screen
- [ ] Integrate Google Maps
- [ ] Implement polyline feature
- [ ] Add place details screen
- [ ] Create specialty screens
- [ ] Add bookmark functionality
- [ ] Profile & settings
- [ ] Notifications setup
- [ ] Error handling & logging
- [ ] Unit tests
- [ ] UI tests
- [ ] Performance optimization

---

## Notes

### Removed Features
- **Events Page**: Removed from navigation and models
- All event-related database tables removed
- Navigation updated to exclude events tab

### Placeholder Images
- All image resources are temporary placeholders
- Replace with actual assets when available
- Same Poppins font family maintained for consistency

### Security Considerations
- API keys stored in local.properties (not in repo)
- Sensitive data in encrypted DataStore
- HTTPS only for all API calls
- User data stored locally with Room

---

## Next Steps

1. **Generate Kotlin Project**
   - Use Android Studio → New Project → Empty Views Activity
   - Min SDK: 24, Target SDK: 34

2. **Copy Structure**
   - Create all directories and base files
   - Add dependencies to build.gradle.kts

3. **Database Setup**
   - Create Room entities
   - Implement DAOs
   - Add AppDatabase

4. **UI Implementation**
   - Setup Compose theme
   - Create base screens
   - Build navigation graph

5. **Integration**
   - Add Maps API
   - Implement location services
   - Connect to backend APIs

6. **Testing**
   - Unit tests for ViewModels
   - Integration tests for database
   - UI tests with Espresso/Compose testing

---

## Resources

- [Android Developers](https://developer.android.com)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Room Persistence Library](https://developer.android.com/training/data-storage/room)
- [Google Maps for Android](https://developers.google.com/maps/documentation/android-sdk)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Hilt Dependency Injection](https://dagger.dev/hilt)

---

**Version**: 1.0  
**Last Updated**: May 3, 2026  
**Status**: Ready for Implementation
