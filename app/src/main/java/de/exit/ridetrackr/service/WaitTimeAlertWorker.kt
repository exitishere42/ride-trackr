package de.exit.ridetrackr.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import de.exit.ridetrackr.R
import de.exit.ridetrackr.data.repository.AttractionsRepository
import de.exit.ridetrackr.data.repository.FavoritesRepository
import de.exit.ridetrackr.data.repository.SettingsRepository
import kotlinx.coroutines.flow.first

class WaitTimeAlertWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WorkerEntryPoint {
        fun favoritesRepository(): FavoritesRepository
        fun attractionsRepository(): AttractionsRepository
        fun settingsRepository(): SettingsRepository
    }

    companion object {
        const val CHANNEL_ID = "ride_trackr_alerts"
        const val CHANNEL_NAME = "Wartezeiten-Alerts"
    }

    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WorkerEntryPoint::class.java
        )
        val settingsRepository = entryPoint.settingsRepository()
        val favoritesRepository = entryPoint.favoritesRepository()
        val attractionsRepository = entryPoint.attractionsRepository()

        val alertsEnabled = settingsRepository.alertsEnabled.first()
        if (!alertsEnabled) return Result.success()

        val threshold = settingsRepository.alertThresholdMinutes.first()
        val favorites = favoritesRepository.favorites.first()

        createNotificationChannel()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val byPark = favorites.groupBy { it.parkId }
        for ((parkId, favList) in byPark) {
            try {
                val liveAttractions = attractionsRepository.getAttractions(parkId)
                for (fav in favList) {
                    val live = liveAttractions.firstOrNull { it.id == fav.id } ?: continue
                    val wait = live.waitTimeMinutes ?: continue

                    if (wait <= threshold && wait > 0) {
                        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setContentTitle("Kurze Wartezeit: ${live.name}")
                            .setContentText("Aktuell nur $wait Minuten Wartezeit in ${fav.parkName}!")
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setAutoCancel(true)
                            .build()

                        notificationManager.notify(fav.id.hashCode(), notification)
                    }
                }
            } catch (_: Exception) {
                // Ignore transient network errors during background poll
            }
        }

        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Benachrichtigungen bei kurzen Wartezeiten deiner Lieblings-Achterbahnen"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
