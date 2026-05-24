package com.gastro.nameet.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.gastro.nameet.database.AppDatabase
import com.gastro.nameet.database.dao.BookmarkDao
import com.gastro.nameet.database.dao.CommentDao
import com.gastro.nameet.database.dao.FoodBookmarkDao
import com.gastro.nameet.database.dao.FoodDao
import com.gastro.nameet.database.dao.PlaceDao
import com.gastro.nameet.database.dao.UserDao
import com.gastro.nameet.repository.AuthRepository
import com.gastro.nameet.repository.BookmarkRepository
import com.gastro.nameet.repository.CommentRepository
import com.gastro.nameet.repository.FoodBookmarkRepository
import com.gastro.nameet.repository.FoodRepository
import com.gastro.nameet.repository.PlaceRepository
import com.gastro.nameet.repository.UserPreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gastro_preferences")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Database
    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Singleton
    @Provides
    fun providePlaceDao(appDatabase: AppDatabase): PlaceDao {
        return appDatabase.placeDao()
    }

    @Singleton
    @Provides
    fun provideFoodDao(appDatabase: AppDatabase): FoodDao {
        return appDatabase.foodDao()
    }

    @Singleton
    @Provides
    fun provideBookmarkDao(appDatabase: AppDatabase): BookmarkDao {
        return appDatabase.bookmarkDao()
    }

    @Singleton
    @Provides
    fun provideCommentDao(appDatabase: AppDatabase): CommentDao {
        return appDatabase.commentDao()
    }

    @Singleton
    @Provides
    fun provideUserDao(appDatabase: AppDatabase): UserDao {
        return appDatabase.userDao()
    }

    @Singleton
    @Provides
    fun provideFoodBookmarkDao(appDatabase: AppDatabase): FoodBookmarkDao {
        return appDatabase.foodBookmarkDao()
    }

    // Repositories
    @Singleton
    @Provides
    fun providePlaceRepository(placeDao: PlaceDao): PlaceRepository {
        return PlaceRepository(placeDao)
    }

    @Singleton
    @Provides
    fun provideFoodRepository(foodDao: FoodDao): FoodRepository {
        return FoodRepository(foodDao)
    }

    @Singleton
    @Provides
    fun provideBookmarkRepository(bookmarkDao: BookmarkDao): BookmarkRepository {
        return BookmarkRepository(bookmarkDao)
    }

    @Singleton
    @Provides
    fun provideCommentRepository(commentDao: CommentDao): CommentRepository {
        return CommentRepository(commentDao)
    }

    @Singleton
    @Provides
    fun provideAuthRepository(userDao: UserDao): AuthRepository {
        return AuthRepository(userDao)
    }

    @Singleton
    @Provides
    fun provideFoodBookmarkRepository(foodBookmarkDao: FoodBookmarkDao): FoodBookmarkRepository {
        return FoodBookmarkRepository(foodBookmarkDao)
    }

    @Singleton
    @Provides
    fun provideUserPreferencesRepository(dataStore: DataStore<Preferences>): UserPreferencesRepository {
        return UserPreferencesRepository(dataStore)
    }

    // DataStore
    @Singleton
    @Provides
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    // Networking
    @Singleton
    @Provides
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(com.gastro.nameet.util.Constants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(com.gastro.nameet.util.Constants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(com.gastro.nameet.util.Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        return Retrofit.Builder()
            .baseUrl(com.gastro.nameet.util.Constants.BASE_API_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Singleton
    @Provides
    fun providePlacesClient(@ApplicationContext context: Context): PlacesClient {
        return Places.createClient(context)
    }

    @Singleton
    @Provides
    fun provideFusedLocationClient(@ApplicationContext context: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }
}
