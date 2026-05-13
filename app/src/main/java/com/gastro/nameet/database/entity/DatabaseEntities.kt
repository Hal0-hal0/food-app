package com.gastro.nameet.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "places")
data class PlaceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String = "",
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String = "",
    val rating: Float = 0f,
    val category: String = "", // breakfast, lunch, dinner, dessert, snacks, soup
    val phone: String = "",
    val address: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "foods")
data class FoodEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val placeId: Int,
    val name: String,
    val description: String = "",
    val imageUrl: String = "",
    val category: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "bookmarks",
    foreignKeys = [
        ForeignKey(
            entity = PlaceEntity::class,
            parentColumns = ["id"],
            childColumns = ["placeId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val placeId: Int,
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "comments",
    foreignKeys = [
        ForeignKey(
            entity = PlaceEntity::class,
            parentColumns = ["id"],
            childColumns = ["placeId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CommentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val placeId: Int,
    val userName: String = "",
    val comment: String = "",
    val rating: Float = 0f,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val email: String,
    val password: String = "", // In production, use hashed password
    val name: String = "",
    val profileImageUrl: String = "",
    val isLoggedIn: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "food_bookmarks")
data class FoodBookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val specialtyId: Int,
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
