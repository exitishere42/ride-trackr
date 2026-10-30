package de.exit.ridetrackr.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.exit.ridetrackr.data.local.AppDatabase
import de.exit.ridetrackr.data.local.dao.FavoritesDao
import de.exit.ridetrackr.data.local.dao.UserReportsDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "ride_trackr.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideFavoritesDao(database: AppDatabase): FavoritesDao {
        return database.favoritesDao()
    }

    @Provides
    fun provideUserReportsDao(database: AppDatabase): UserReportsDao {
        return database.userReportsDao()
    }
}
