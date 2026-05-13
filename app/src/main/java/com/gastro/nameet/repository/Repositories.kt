package com.gastro.nameet.repository

import com.gastro.nameet.database.dao.BookmarkDao
import com.gastro.nameet.database.dao.CommentDao
import com.gastro.nameet.database.dao.FoodBookmarkDao
import com.gastro.nameet.database.dao.FoodDao
import com.gastro.nameet.database.dao.PlaceDao
import com.gastro.nameet.database.dao.UserDao
import com.gastro.nameet.database.entity.BookmarkEntity
import com.gastro.nameet.database.entity.CommentEntity
import com.gastro.nameet.database.entity.FoodBookmarkEntity
import com.gastro.nameet.database.entity.FoodEntity
import com.gastro.nameet.database.entity.PlaceEntity
import com.gastro.nameet.database.entity.UserEntity
import com.gastro.nameet.model.Place
import com.gastro.nameet.model.Food
import com.gastro.nameet.model.Comment
import com.gastro.nameet.model.Bookmark
import com.gastro.nameet.model.FoodBookmark
import com.gastro.nameet.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaceRepository @Inject constructor(
    private val placeDao: PlaceDao
) {
    // Shared state to trigger map locating from other screens
    val placeToLocate = kotlinx.coroutines.flow.MutableStateFlow<Int?>(null)

    fun getAllPlaces(): Flow<List<Place>> =
        placeDao.getAllPlaces().map { entities ->
            entities.map { it.toModel() }
        }

    fun getPlaceById(id: Int): Flow<Place?> =
        placeDao.getPlaceById(id).map { entity ->
            entity?.toModel()
        }

    fun getPlacesByCategory(category: String): Flow<List<Place>> =
        placeDao.getPlacesByCategory(category).map { entities ->
            entities.map { it.toModel() }
        }

    fun searchPlaces(query: String): Flow<List<Place>> =
        placeDao.searchPlaces("%$query%").map { entities ->
            entities.map { it.toModel() }
        }

    fun getPlacesInBounds(minLat: Double, maxLat: Double, minLon: Double, maxLon: Double): Flow<List<Place>> =
        placeDao.getPlacesInBounds(minLat, maxLat, minLon, maxLon).map { entities ->
            entities.map { it.toModel() }
        }

    suspend fun insertPlace(place: Place): Long =
        placeDao.insertPlace(place.toEntity())

    suspend fun insertPlaces(places: List<Place>) =
        placeDao.insertPlaces(places.map { it.toEntity() })

    suspend fun updatePlace(place: Place) =
        placeDao.updatePlace(place.toEntity())

    suspend fun deletePlace(place: Place) =
        placeDao.deletePlace(place.toEntity())

    // Mapping functions
    private fun PlaceEntity.toModel() = Place(
        id = id,
        name = name,
        description = description,
        latitude = latitude,
        longitude = longitude,
        imageUrl = imageUrl,
        rating = rating,
        category = category,
        phone = phone,
        address = address,
        createdAt = createdAt
    )

    private fun Place.toEntity() = PlaceEntity(
        id = id,
        name = name,
        description = description,
        latitude = latitude,
        longitude = longitude,
        imageUrl = imageUrl,
        rating = rating,
        category = category,
        phone = phone,
        address = address,
        createdAt = createdAt
    )
}

@Singleton
class FoodRepository @Inject constructor(
    private val foodDao: FoodDao
) {
    fun getFoodsByPlace(placeId: Int): Flow<List<Food>> =
        foodDao.getFoodsByPlace(placeId).map { entities ->
            entities.map { it.toModel() }
        }

    fun getFoodsByCategory(category: String): Flow<List<Food>> =
        foodDao.getFoodsByCategory(category).map { entities ->
            entities.map { it.toModel() }
        }

    fun getAllFoods(): Flow<List<Food>> =
        foodDao.getAllFoods().map { entities ->
            entities.map { it.toModel() }
        }

    suspend fun insertFood(food: Food): Long =
        foodDao.insertFood(food.toEntity())

    suspend fun insertFoods(foods: List<Food>) =
        foodDao.insertFoods(foods.map { it.toEntity() })

    private fun FoodEntity.toModel() = Food(
        id = id,
        placeId = placeId,
        name = name,
        description = description,
        imageUrl = imageUrl,
        category = category
    )

    private fun Food.toEntity() = FoodEntity(
        id = id,
        placeId = placeId,
        name = name,
        description = description,
        imageUrl = imageUrl,
        category = category
    )
}

@Singleton
class BookmarkRepository @Inject constructor(
    private val bookmarkDao: BookmarkDao
) {
    fun getUserBookmarks(userId: String): Flow<List<Bookmark>> =
        bookmarkDao.getUserBookmarks(userId).map { entities ->
            entities.map { it.toModel() }
        }

    fun isBookmarked(placeId: Int, userId: String): Flow<Bookmark?> =
        bookmarkDao.isBookmarked(placeId, userId).map { entity ->
            entity?.toModel()
        }

    fun getBookmarkCount(placeId: Int): Flow<Int> =
        bookmarkDao.getBookmarkCount(placeId)

    suspend fun insertBookmark(bookmark: Bookmark): Long =
        bookmarkDao.insertBookmark(bookmark.toEntity())

    suspend fun removeBookmark(placeId: Int, userId: String) =
        bookmarkDao.removeBookmark(placeId, userId)

    private fun BookmarkEntity.toModel() = Bookmark(
        id = id,
        placeId = placeId,
        userId = userId,
        createdAt = createdAt
    )

    private fun Bookmark.toEntity() = BookmarkEntity(
        id = id,
        placeId = placeId,
        userId = userId,
        createdAt = createdAt
    )
}

@Singleton
class CommentRepository @Inject constructor(
    private val commentDao: CommentDao
) {
    fun getCommentsByPlace(placeId: Int): Flow<List<Comment>> =
        commentDao.getCommentsByPlace(placeId).map { entities ->
            entities.map { it.toModel() }
        }

    fun getLatestComments(limit: Int = 10): Flow<List<Comment>> =
        commentDao.getLatestComments(limit).map { entities ->
            entities.map { it.toModel() }
        }

    fun getAverageRating(placeId: Int): Flow<Float?> =
        commentDao.getAverageRating(placeId)

    suspend fun insertComment(comment: Comment): Long =
        commentDao.insertComment(comment.toEntity())

    suspend fun insertComments(comments: List<Comment>) =
        commentDao.insertComments(comments.map { it.toEntity() })

    private fun CommentEntity.toModel() = Comment(
        id = id,
        placeId = placeId,
        userName = userName,
        comment = comment,
        rating = rating,
        createdAt = createdAt
    )

    private fun Comment.toEntity() = CommentEntity(
        id = id,
        placeId = placeId,
        userName = userName,
        comment = comment,
        rating = rating,
        createdAt = createdAt
    )
}

@Singleton
class AuthRepository @Inject constructor(
    private val userDao: UserDao
) {
    fun getLoggedInUser(): Flow<User?> =
        userDao.getLoggedInUser().map { entity ->
            entity?.toModel()
        }

    fun getUserByEmail(email: String): Flow<User?> =
        userDao.getUserByEmail(email).map { entity ->
            entity?.toModel()
        }

    suspend fun login(email: String, password: String): Boolean {
        // In production, validate against backend API
        val user = userDao.getUserByEmail(email).firstOrNull()
        return if (user != null && user.password == password) {
            userDao.updateUser(user.copy(isLoggedIn = true))
            true
        } else {
            false
        }
    }

    suspend fun register(email: String, name: String, password: String): Boolean {
        val newUser = UserEntity(
            email = email,
            name = name,
            password = password, // In production, hash this!
            isLoggedIn = true
        )
        val result = userDao.insertUser(newUser)
        return result > 0
    }

    suspend fun logout() {
        userDao.logoutAllUsers()
    }

    private fun UserEntity.toModel() = User(
        id = id,
        email = email,
        name = name,
        profileImageUrl = profileImageUrl,
        isLoggedIn = isLoggedIn,
        createdAt = createdAt
    )
}

@Singleton
class FoodBookmarkRepository @Inject constructor(
    private val foodBookmarkDao: FoodBookmarkDao
) {
    fun getUserBookmarks(userId: String): Flow<List<FoodBookmark>> =
        foodBookmarkDao.getUserBookmarks(userId).map { entities ->
            entities.map { it.toModel() }
        }

    fun isBookmarked(specialtyId: Int, userId: String): Flow<FoodBookmark?> =
        foodBookmarkDao.isBookmarked(specialtyId, userId).map { entity ->
            entity?.toModel()
        }

    suspend fun insertBookmark(bookmark: FoodBookmark): Long =
        foodBookmarkDao.insertBookmark(bookmark.toEntity())

    suspend fun removeBookmark(specialtyId: Int, userId: String) =
        foodBookmarkDao.removeBookmark(specialtyId, userId)

    private fun FoodBookmarkEntity.toModel() = FoodBookmark(
        id = id,
        specialtyId = specialtyId,
        userId = userId,
        createdAt = createdAt
    )

    private fun FoodBookmark.toEntity() = FoodBookmarkEntity(
        id = id,
        specialtyId = specialtyId,
        userId = userId,
        createdAt = createdAt
    )
}
