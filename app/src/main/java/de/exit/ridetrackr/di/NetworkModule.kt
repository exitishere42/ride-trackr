package de.exit.ridetrackr.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.exit.ridetrackr.data.engine.PlausibilityEngine
import de.exit.ridetrackr.data.remote.api.FirebaseRealtimeDbApi
import de.exit.ridetrackr.data.remote.api.ThemeParksWikiApi
import de.exit.ridetrackr.data.remote.api.WartezeitenApi
import de.exit.ridetrackr.data.remote.api.WikipediaApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "RideTrackr/1.0 (Android; https://github.com/exitishere42/ride-trackr)")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideThemeParksWikiApi(okHttpClient: OkHttpClient, json: Json): ThemeParksWikiApi {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://api.themeparks.wiki/v1/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(ThemeParksWikiApi::class.java)
    }

    @Provides
    @Singleton
    fun provideWartezeitenApi(okHttpClient: OkHttpClient, json: Json): WartezeitenApi {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://api.wartezeiten.app/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(WartezeitenApi::class.java)
    }

    @Provides
    @Singleton
    fun provideWikipediaApi(okHttpClient: OkHttpClient, json: Json): WikipediaApi {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://de.wikipedia.org/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(WikipediaApi::class.java)
    }

    @Provides
    @Singleton
    fun providePlausibilityEngine(): PlausibilityEngine {
        return PlausibilityEngine()
    }

    @Provides
    @Singleton
    fun provideTripsdrillWaitTimeEngine(): de.exit.ridetrackr.data.engine.TripsdrillWaitTimeEngine {
        return de.exit.ridetrackr.data.engine.TripsdrillWaitTimeEngine()
    }

    @Provides
    @Singleton
    fun provideFirebaseRealtimeDbApi(okHttpClient: OkHttpClient, json: Json): FirebaseRealtimeDbApi {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://ride-trackr-live-default-rtdb.europe-west1.firebasedatabase.app/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(FirebaseRealtimeDbApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGitHubCloudDbApi(okHttpClient: OkHttpClient, json: Json): de.exit.ridetrackr.data.remote.api.GitHubCloudDbApi {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://raw.githubusercontent.com/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(de.exit.ridetrackr.data.remote.api.GitHubCloudDbApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGitHubReleasesApi(okHttpClient: OkHttpClient, json: Json): de.exit.ridetrackr.data.remote.api.GitHubReleasesApi {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(de.exit.ridetrackr.data.remote.api.GitHubReleasesApi::class.java)
    }
}
