package de.exit.ridetrackr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import de.exit.ridetrackr.data.repository.SettingsRepository
import de.exit.ridetrackr.ui.i18n.LocalAppStrings
import de.exit.ridetrackr.ui.i18n.StringsDe
import de.exit.ridetrackr.ui.i18n.StringsEn
import de.exit.ridetrackr.ui.navigation.MainAppScaffold
import de.exit.ridetrackr.ui.theme.RideTrackrTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val colorAccent by settingsRepository.colorAccent.collectAsState(initial = "TEAL")
            val language by settingsRepository.language.collectAsState(initial = "DE")
            val strings = if (language.equals("EN", ignoreCase = true)) StringsEn else StringsDe

            CompositionLocalProvider(
                LocalAppStrings provides strings
            ) {
                RideTrackrTheme(accent = colorAccent) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MainAppScaffold()
                    }
                }
            }
        }
    }
}
