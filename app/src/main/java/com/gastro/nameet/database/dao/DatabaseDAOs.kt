package com.gastro.nameet.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.gastro.nameet.database.entity.BookmarkEntity
import com.gastro.nameet.database.entity.CommentEntity
import com.gastro.nameet.database.entity.FoodBookmarkEntity
import com.gastro.nameet.database.entity.FoodEntity
import com.gastro.nameet.database.entity.PlaceEntity
import com.gastro.nameet.database.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {
    // @Upsert performs INSERT-or-UPDATE without DELETE, so it doesn't trigger
    // the bookmark foreign-key CASCADE that would silently erase a user's
    // bookmark every time the place's row is re-saved.
    @Upsert
    suspend fun insertPlace(place: PlaceEntity): Long

    @Upsert
    suspend fun insertPlaces(places: List<PlaceEntity>)

    @Update
    suspend fun updatePlace(place: PlaceEntity)

    @Delete
    suspend fun deletePlace(place: PlaceEntity)

    @Query("SELECT * FROM places WHERE id = :id")
    fun getPlaceById(id: Int): Flow<PlaceEntity?>

    @Query("SELECT * FROM places ORDER BY rating DESC")
    fun getAllPlaces(): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM places WHERE category = :category ORDER BY rating DESC")
    fun getPlacesByCategory(category: String): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM places WHERE name LIKE :query OR description LIKE :query OR category LIKE :query")
    fun searchPlaces(query: String): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM places WHERE latitude BETWEEN :minLat AND :maxLat AND longitude BETWEEN :minLon AND :maxLon")
    fun getPlacesInBounds(minLat: Double, maxLat: Double, minLon: Double, maxLon: Double): Flow<List<PlaceEntity>>
}

@Dao
interface FoodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: FoodEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoods(foods: List<FoodEntity>)

    @Update
    suspend fun updateFood(food: FoodEntity)

    @Delete
    suspend fun deleteFood(food: FoodEntity)

    @Query("SELECT * FROM foods WHERE id = :id")
    fun getFoodById(id: Int): Flow<FoodEntity?>

    @Query("SELECT * FROM foods WHERE placeId = :placeId")
    fun getFoodsByPlace(placeId: Int): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods WHERE category = :category")
    fun getFoodsByCategory(category: String): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods")
    fun getAllFoods(): Flow<List<FoodEntity>>
}

@Dao
interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)

    @Query("SELECT * FROM bookmarks WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserBookmarks(userId: String): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE placeId = :placeId AND userId = :userId")
    fun isBookmarked(placeId: Int, userId: String): Flow<BookmarkEntity?>

    @Query("SELECT COUNT(*) FROM bookmarks WHERE placeId = :placeId")
    fun getBookmarkCount(placeId: Int): Flow<Int>

    @Query("DELETE FROM bookmarks WHERE placeId = :placeId AND userId = :userId")
    suspend fun removeBookmark(placeId: Int, userId: String)
}

@Dao
interface CommentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<CommentEntity>)

    @Update
    suspend fun updateComment(comment: CommentEntity)

    @Delete
    suspend fun deleteComment(comment: CommentEntity)

    @Query("SELECT * FROM comments WHERE id = :id")
    fun getCommentById(id: Int): Flow<CommentEntity?>

    @Query("SELECT * FROM comments WHERE placeId = :placeId ORDER BY createdAt DESC")
    fun getCommentsByPlace(placeId: Int): Flow<List<CommentEntity>>

    @Query("SELECT AVG(rating) FROM comments WHERE placeId = :placeId")
    fun getAverageRating(placeId: Int): Flow<Float?>

    @Query("SELECT * FROM comments ORDER BY createdAt DESC LIMIT :limit")
    fun getLatestComments(limit: Int = 10): Flow<List<CommentEntity>>
}

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE email = :email")
    fun getUserByEmail(email: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: Int): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    fun getLoggedInUser(): Flow<UserEntity?>

    @Query("UPDATE users SET isLoggedIn = 0 WHERE isLoggedIn = 1")
    suspend fun logoutAllUsers()

    @Query("UPDATE users SET name = :name, email = :email, profileImageUrl = :profileImageUrl WHERE id = :id")
    suspend fun updateProfile(id: Int, name: String, email: String, profileImageUrl: String)

    @Query("UPDATE users SET password = :newPassword WHERE id = :id AND password = :currentPassword")
    suspend fun updatePassword(id: Int, currentPassword: String, newPassword: String): Int
}

@Dao
interface FoodBookmarkDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBookmark(bookmark: FoodBookmarkEntity): Long

    @Query("DELETE FROM food_bookmarks WHERE specialtyId = :specialtyId AND userId = :userId")
    suspend fun removeBookmark(specialtyId: Int, userId: String)

    @Query("SELECT * FROM food_bookmarks WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserBookmarks(userId: String): Flow<List<FoodBookmarkEntity>>

    @Query("SELECT * FROM food_bookmarks WHERE specialtyId = :specialtyId AND userId = :userId")
    fun isBookmarked(specialtyId: Int, userId: String): Flow<FoodBookmarkEntity?>
}
