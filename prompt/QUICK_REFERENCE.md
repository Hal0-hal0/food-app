# Gastro Nameet Kotlin - Quick Reference Guide

## 📦 File Manifest

All the files you need are in `/mnt/user-data/outputs/`:

### Documentation
- **PROMPTY_KOTLIN_CONVERSION.md** - Main project specification & requirements
- **IMPLEMENTATION_GUIDE.md** - Step-by-step setup and implementation
- **QUICK_REFERENCE.md** - This file

### Build Configuration
- **app_build.gradle.kts** - App-level Gradle configuration with all dependencies

### Core Application
- **GastroApp.kt** - Application class with Hilt setup and logging

### Theme & UI
- **Color.kt** - All color definitions for Material Design 3
- **Type.kt** - Typography with Poppins font family
- **Theme.kt** - Complete theme configuration (light/dark mode)

### Database Layer
- **DatabaseEntities.kt** - Room entity classes (@Entity annotations)
- **DatabaseDAOs.kt** - Data Access Objects for CRUD operations
- **AppDatabase.kt** - Room database class and singleton

### Domain Models
- **Models.kt** - Data classes for Places, Foods, Comments, Users, Polylines, etc.

### Repository Layer
- **Repositories.kt** - Repository pattern for all data sources
  - PlaceRepository
  - FoodRepository
  - BookmarkRepository
  - CommentRepository
  - AuthRepository

### ViewModel Layer
- **ViewModels.kt** - State management classes
  - AuthViewModel
  - HomeViewModel
  - BookmarkViewModel

### Services & Utilities
- **Utilities.kt** - Helper functions and extensions
  - DistanceUtils
  - DateTimeUtils
  - ValidationUtils
  - StringUtils

### Dependency Injection
- **ServiceLocator.kt** - Hilt DI setup with all providers
  - Database providers
  - Repository providers
  - Network/Retrofit providers
  - DataStore providers

### Android Resources
- **AndroidManifest.xml** - App manifest with permissions and API key
- **strings.xml** - All UI text strings (complete localization ready)

---

## 🏗️ Architecture Overview

```
                        UI Layer (Compose)
                    ↓           ↓           ↓
                 Screens    Components   Navigation
                    ↓           ↓           ↓
        ┌─────────────────────────────────────┐
        │      ViewModel (State Management)    │
        │   • AuthViewModel                   │
        │   • HomeViewModel                   │
        │   • BookmarkViewModel               │
        └─────────────────────────────────────┘
                    ↓
        ┌─────────────────────────────────────┐
        │    Repository (Data Access)         │
        │   • PlaceRepository                 │
        │   • FoodRepository                  │
        │   • BookmarkRepository              │
        │   • CommentRepository               │
        │   • AuthRepository                  │
        └─────────────────────────────────────┘
                    ↓
        ┌─────────────────────────────────────┐
        │    Data Layer                       │
        │  ┌──────────┐    ┌──────────┐      │
        │  │   Room   │    │ DataStore│      │
        │  │ Database │    │Preferences      │
        │  └──────────┘    └──────────┘      │
        └─────────────────────────────────────┘
```

---

## 🎯 Quick Start Checklist

- [ ] **1. Create Android Project**
  - Android Studio → New Project → Empty Activity
  - Package: com.gastro.nameet
  - Min SDK: 24, Target SDK: 34

- [ ] **2. Configure Gradle**
  - Copy content of `app_build.gradle.kts` to app-level build.gradle.kts
  - Run `./gradlew sync`

- [ ] **3. Create Directory Structure**
  ```
  app/src/main/kotlin/com/gastro/nameet/
  ├── database/
  ├── model/
  ├── repository/
  ├── viewmodel/
  ├── ui/
  ├── service/
  ├── util/
  └── di/
  ```

- [ ] **4. Copy Source Files**
  - All .kt files → appropriate directories
  - All .xml files → appropriate res/ directories

- [ ] **5. Add Resources**
  - Download Poppins font
  - Add to res/font/
  - Create colors.xml, dimens.xml

- [ ] **6. Setup API Key**
  - Get Google Maps API key
  - Add to local.properties:
    ```properties
    MAPS_API_KEY=your_key_here
    ```

- [ ] **7. Build & Test**
  - `./gradlew build`
  - Run on emulator

---

## 🔧 Common Tasks

### Add a New Screen
```kotlin
// 1. Create in ui/screens/
@Composable
fun NewScreen(navController: NavController) {
    // Your UI here
}

// 2. Add to navigation graph (AppNavigation.kt)
composable(route = "new_screen") {
    NewScreen(navController = navController)
}
```

### Add Database Entity
```kotlin
// 1. In database/entity/
@Entity(tableName = "new_table")
data class NewEntity(...)

// 2. Create DAO in database/dao/
@Dao
interface NewDao { ... }

// 3. Add to AppDatabase
abstract fun newDao(): NewDao

// 4. Create repository
class NewRepository(val dao: NewDao) { ... }
```

### Use ViewModel in Screen
```kotlin
@Composable
fun MyScreen(viewModel: MyViewModel = hiltViewModel()) {
    val state by viewModel.myState.collectAsState()
    // Use state
}
```

---

## 🎨 Design System

### Colors
```
Primary: #F7941D (Orange)
Secondary: #FFB84D (Light Orange)
Tertiary: #2C3E50 (Dark Blue-Gray)
Error: #D32F2F (Red)
Background: #FFFFFF
Surface: #FFFFFF
```

### Typography
```
Display: 32sp Bold
Headline: 28sp SemiBold
Title: 20sp SemiBold
Body: 16sp Regular
Label: 14sp Medium
Caption: 12sp Regular
```

### Spacing (8dp grid)
```
xs: 4dp
sm: 8dp
md: 12dp
lg: 16dp
xl: 24dp
2xl: 32dp
```

### Corner Radius
```
Small: 8dp
Medium: 12dp
Large: 16dp
```

---

## 🔑 Key Kotlin Features Used

| Feature | Usage |
|---------|-------|
| **Data Classes** | Models, Entities |
| **Extension Functions** | String.toLatLng(), etc. |
| **Sealed Classes** | State management |
| **Flow** | Reactive data streams |
| **Coroutines** | Async operations |
| **Inline Classes** | Type-safe wrappers |
| **Delegation** | Interface implementation |

---

## 📍 Package Structure

```
com.gastro.nameet/
├── database/          → Room DAOs, Entities, AppDatabase
├── model/             → Data classes (Place, Food, etc.)
├── repository/        → Data access layer
├── viewmodel/         → UI state management
├── ui/
│   ├── theme/         → Colors, Typography, Theme
│   ├── screens/       → Screen composables
│   ├── components/    → Reusable UI components
│   └── navigation/    → Navigation graph
├── service/           → Services (Location, Auth, etc.)
├── util/              → Constants, Utils, Extensions
├── di/                → Dependency Injection (Hilt)
├── GastroApp.kt       → Application class
└── MainActivity.kt    → Entry point
```

---

## 🔐 Security Checklist

- [ ] API key in local.properties (not committed)
- [ ] Sensitive data encrypted in DataStore
- [ ] HTTPS for all API calls
- [ ] Input validation on all forms
- [ ] No hardcoded secrets
- [ ] Proper permission handling
- [ ] User data encrypted at rest
- [ ] Clear sensitive data on logout

---

## 🧪 Testing Commands

```bash
# Unit tests
./gradlew test

# Android tests
./gradlew connectedAndroidTest

# Run specific test
./gradlew testDebugUnitTest --tests com.gastro.nameet.*

# Code coverage
./gradlew jacocoTestReport

# Lint check
./gradlew lint
```

---

## 📊 Database Schema

### Places Table
```sql
CREATE TABLE places (
  id INTEGER PRIMARY KEY,
  name TEXT NOT NULL,
  description TEXT,
  latitude REAL NOT NULL,
  longitude REAL NOT NULL,
  imageUrl TEXT,
  rating REAL,
  category TEXT,
  phone TEXT,
  address TEXT,
  createdAt TIMESTAMP
);
```

### Foods Table
```sql
CREATE TABLE foods (
  id INTEGER PRIMARY KEY,
  placeId INTEGER NOT NULL,
  name TEXT NOT NULL,
  description TEXT,
  imageUrl TEXT,
  category TEXT,
  FOREIGN KEY(placeId) REFERENCES places(id)
);
```

### Bookmarks Table
```sql
CREATE TABLE bookmarks (
  id INTEGER PRIMARY KEY,
  placeId INTEGER NOT NULL,
  userId TEXT,
  createdAt TIMESTAMP,
  FOREIGN KEY(placeId) REFERENCES places(id)
);
```

### Comments Table
```sql
CREATE TABLE comments (
  id INTEGER PRIMARY KEY,
  placeId INTEGER NOT NULL,
  userName TEXT,
  comment TEXT,
  rating REAL,
  createdAt TIMESTAMP,
  FOREIGN KEY(placeId) REFERENCES places(id)
);
```

---

## 🚀 Performance Tips

1. **Lazy Loading**
   - Use LazyColumn for lists
   - Paginate large datasets
   - Load images asynchronously

2. **Database**
   - Use indexes on frequently queried columns
   - Batch inserts when possible
   - Use Flow for reactive updates

3. **Maps**
   - Load only visible markers
   - Cluster markers for large datasets
   - Cache polyline calculations

4. **Memory**
   - Use weak references for callbacks
   - Clear listeners when components destroyed
   - Properly dispose of resources

---

## 🐛 Common Bugs & Fixes

| Issue | Solution |
|-------|----------|
| Compose recomposing too much | Use `remember` and state hoisting |
| Database locks | Ensure queries on background thread |
| Maps not showing | Check API key and permissions |
| Polyline not rendering | Verify coordinates are valid |
| Images not loading | Check image URL and permissions |
| Coroutine leaks | Use viewModelScope |

---

## 📚 Key Files to Know

| File | Purpose |
|------|---------|
| AppDatabase.kt | Database singleton |
| ServiceLocator.kt | Hilt configuration |
| Theme.kt | Material Design theme |
| Models.kt | Data classes |
| ViewModels.kt | State management |
| Utilities.kt | Helper functions |

---

## 🆘 Need Help?

1. **Gradle Issues** → Check build.gradle.kts syntax
2. **Compilation Errors** → Run `./gradlew clean`
3. **Database Errors** → Check entity relationships
4. **Maps Errors** → Verify API key and permissions
5. **Hilt Errors** → Ensure @HiltAndroidApp and @AndroidEntryPoint

---

**Version**: 1.0  
**Last Updated**: May 3, 2026  
**Status**: Complete ✅
