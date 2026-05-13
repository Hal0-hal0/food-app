package com.gastro.nameet.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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

@Database(
    entities = [
        PlaceEntity::class,
        FoodEntity::class,
        BookmarkEntity::class,
        CommentEntity::class,
        UserEntity::class,
        FoodBookmarkEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun placeDao(): PlaceDao
    abstract fun foodDao(): FoodDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun commentDao(): CommentDao
    abstract fun userDao(): UserDao
    abstract fun foodBookmarkDao(): FoodBookmarkDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gastro_nameet.db"
                )
                    .fallbackToDestructiveMigration() // For dev only, remove for production
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
