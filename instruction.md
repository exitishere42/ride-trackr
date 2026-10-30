# Android App Spezifikation: Ride Trackr

> **An den ausführenden Entwickler-Agenten:**
> Dieses Dokument ist deine verbindliche Spezifikation für **Ride Trackr**.
> Halte dich strikt an die Modern Android Development (MAD) Standards, Jetpack Compose Best Practices, die Material Design 3 (M3) Richtlinien und die Anti-Halluzinations-Regeln.
> Führe nach jedem Schritt den entsprechenden Verifikations-Befehl aus (`./gradlew testDebugUnitTest` bzw. `./gradlew assembleDebug`).

---

## 1. Übersicht & Scope
- **App Name:** Ride Trackr
- **Application ID:** `de.exit.ridetrackr`
- **Min SDK:** 26 (Android 8.0 Oreo)
- **Target & Compile SDK:** 35 (Android 15)
- **Datenquellen (Dual-API Aggregator):**
  1. [Wartezeiten.APP](https://www.wartezeiten.app/) (`https://api.wartezeiten.app/`)
  2. [ThemeParks.wiki](https://api.themeparks.wiki/) (`https://api.themeparks.wiki/v1/`)
- **In-Scope (v1.0 MVP):**
  - **Dual-API Datenaggregation:** Paralleles Abfragen beider APIs mit intelligentem Plausibilitäts- und Konsens-Check.
  - **Tripsdrill-Unterstützung:** Nahtlose Integration von Erlebnispark Tripsdrill (bereitgestellt via ThemeParks.wiki), da bei Wartezeiten.APP nicht gelistet.
  - **Plausibilitäts- & Korrektheits-Engine:** Automatischer Vergleich von Attraktionen, die in beiden APIs existieren:
    - Ermittlung des korrekteren Werts anhand von Timestamp-Frische (`lastUpdated`), Status-Priorität (aktuelle Störungsmeldung `DOWN`/`CLOSED` hat Priorität vor veralteter `OPERATING`-Wartezeit) und Konsens.
    - Transparente UI-Anzeige: Badge für verifizierte Werte bzw. Quell-Vergleich bei Diskrepanzen (z. B. *"25 min (Verifiziert via Wartezeiten.APP & ThemeParks.wiki)"* oder *"15 min (ThemeParks.wiki, 2 Min alt vs. Wartezeiten.APP 25 Min, 30 Min alt)"*).
  - **Park-Übersicht & Suche:** Liste der verfügbaren Freizeitparks beider APIs mit Such- und Schnellfilterfunktion.
  - **Live-Wartezeiten & Status:** Attraktionsliste des ausgewählten Parks mit aktuellen Wartezeiten in Minuten, Farbcodierung (Grün/Gelb/Rot) und Statusanzeigen (Geöffnet, Geschlossen, Wartung).
  - **Favoriten-Verwaltung:** Markieren von Lieblings-Attraktionen mit persistenter Speicherung in einer lokalen Room-Datenbank.
  - **Hintergrund-Überwachung & Benachrichtigung:** Regelmäßige Prüfung via Jetpack WorkManager mit Android 13+ Notifikation (`POST_NOTIFICATIONS`), wenn eine favorisierte Attraktion unter eine Wunsch-Wartezeit fällt.
  - **Material Design 3 UI/UX:** Komplett in Jetpack Compose mit Material Design 3 (M3), Dynamic Colors (Material You) und barrierefreiem Farbschema (Vibrant Teal `#009688`).
- **Out-of-Scope (nicht in v1.0):**
  - Keine GPS-Live-Standortverfolgung auf interaktiven Parkplänen (Karten-Navigation).
  - Keine Benutzer-Accounts oder Cloud-Synchronisation (reine lokale Speicherung via Room & DataStore).
  - Kein WearOS-Companion oder Ticket-Wallet.

---

## 2. Tech-Stack & Umgebungs-Leitplanken
- **Programmiersprache & Compiler:** Kotlin 2.0+ mit KSP (Kotlin Symbol Processing – kein veraltetes KAPT).
- **Build-System:** Gradle mit Kotlin DSL (`build.gradle.kts`) und Version Catalogs (`gradle/libs.versions.toml`).
- **UI-Framework:** Jetpack Compose mit Material Design 3 (`androidx.compose.material3`).
- **Theming:** Material 3 Semantic Color Tokens (Primary `#009688` Teal, Surface Containers, Dynamic Color Fallback), Edge-to-Edge (`enableEdgeToEdge()`).
- **Architektur-Muster:** MVVM / MVI mit Unidirectional Data Flow (UDF), `StateFlow<UiState>` und Coroutines.
- **Dependency Injection:** Hilt (`@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`, `@Inject`).
- **Navigation:** Type-Safe Navigation Compose (2.8+) mit `@Serializable` Routen-Objekten.
- **Lokale Persistenz:**
  - **Room Database:** Für persistente Favoriten und gecachte Attraktionsdaten.
  - **Jetpack DataStore (Preferences):** Für Einstellungen (letzter Park, Benachrichtigungsschwellenwert, Notifikations-Toggle).
- **Netzwerk & JSON:** Retrofit 2 + OkHttp 4 + Kotlinx Serialization Converter (oder Moshi).
- **Hintergrund-Verarbeitung:** Jetpack WorkManager (`CoroutineWorker`) mit `Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)`.

---

## 3. Anti-Halluzinations- & Android-Sicherheitsregeln

1. **Compose M3 Striktheit:**
   - Verwende ausschließlich Klassen aus `androidx.compose.material3` (z. B. `Scaffold`, `NavigationBar`, `NavigationBarItem`, `Card`, `FilterChip`, `TopAppBar`).
   - Importiere niemals veraltete Klassen aus `androidx.compose.material.*` (Material 2).
2. **Kein Blocking-I/O auf dem Main-Thread:**
   - Führe Room-Datenbankabfragen, Netzwerk-Calls oder Dateioperationen niemals synchron auf `Dispatchers.Main` aus. Verwende Coroutines mit `Dispatchers.IO` oder reaktive Flow-Streams.
3. **Type-Safe Navigation Compose (2.8+):**
   - Es dürfen keine fehleranfälligen String-Routen wie `"attraction/{id}"` verwendet werden.
   - Alle Destinations müssen als `@Serializable sealed interface Screen` mit `data object` oder `data class` deklariert sein.
4. **Edge-to-Edge & Inset-Handling:**
   - In `MainActivity.kt` ist vor `setContent` stets `enableEdgeToEdge()` aufzurufen.
   - Alle Screens müssen in einem M3 `Scaffold` eingebettet sein und das übergebene `innerPadding` an den Content übergeben (`Modifier.padding(innerPadding)`), um Überlappungen mit Statusbar und Gestenleiste zu verhindern.
5. **Android 13+ Notifikationen (API 33+):**
   - Im `AndroidManifest.xml` muss `android.permission.POST_NOTIFICATIONS` deklariert sein.
   - Vor der Aktivierung von Hintergrund-Benachrichtigungen muss Compose die Berechtigung über `rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())` anfordern.
6. **WorkManager Hintergrund-Regeln:**
   - Der Alert-Worker muss als `CoroutineWorker` implementiert sein und mit Netzwerk-Constraints versehen werden (`NetworkType.CONNECTED`), um Akku-Verschwendung im Funkloch zu verhindern.
7. **Keine veralteten SharedPreferences:**
   - Für Nutzereinstellungen darf ausschließlich Jetpack `DataStore` verwendet werden.

---

## 4. UI/UX-Flow, Screens & Navigation

### Type-Safe Destinations
```kotlin
package de.exit.ridetrackr.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen {
    @Serializable
    data object Parks : Screen

    @Serializable
    data class Attractions(val parkId: String, val parkName: String) : Screen

    @Serializable
    data object Favorites : Screen

    @Serializable
    data object Settings : Screen
}
```

### Screens & Verantwortlichkeiten
| Screen | Route | ViewModel | UI-State | Beschreibung |
| :--- | :--- | :--- | :--- | :--- |
| `ParksScreen` | `Screen.Parks` | `ParksViewModel` | `ParksUiState` | Durchsuchbare Liste aller Freizeitparks mit Status-Chips |
| `AttractionsScreen` | `Screen.Attractions` | `AttractionsViewModel` | `AttractionsUiState` | Live-Wartezeiten des Parks, Filterung (Sortierung, Kategorie), Favoriten-Toggle |
| `FavoritesScreen` | `Screen.Favorites` | `FavoritesViewModel` | `FavoritesUiState` | Schnellübersicht aller favorisierten Attraktionen mit Live-Wartezeiten |
| `SettingsScreen` | `Screen.Settings` | `SettingsViewModel` | `SettingsUiState` | Schwellenwert für Alerts (z. B. 15, 30, 45 Min), Notifikations-Schalter, Impressum/API-Credit |

### Top-Level Navigation
- M3 `NavigationBar` am unteren Bildschirmrand mit 3 Hauptelementen:
  1. **Attraktionen / Parks:** Icon `park` / `roller_coaster`
  2. **Favoriten:** Icon `star`
  3. **Einstellungen:** Icon `settings`

---

## 5. Datenmodell, Persistenz & Schnittstellen

### Room Entity: `FavoriteAttractionEntity`
```kotlin
@Entity(tableName = "favorite_attractions")
data class FavoriteAttractionEntity(
    @PrimaryKey
    val id: String,
    val parkId: String,
    val parkName: String,
    val name: String,
    val lastKnownWaitTime: Int = 0,
    val isAlertEnabled: Boolean = true,
    val maxAlertWaitTime: Int = 30,
    val addedAt: Long = System.currentTimeMillis()
)
```

### Room DAO: `FavoritesDao`
- `fun getAllFavorites(): Flow<List<FavoriteAttractionEntity>>`
- `suspend fun insertFavorite(attraction: FavoriteAttractionEntity)`
- `suspend fun deleteFavorite(id: String)`
- `fun isFavorite(id: String): Flow<Boolean>`

### DataStore Preferences: `UserPreferences`
- `last_selected_park_id`: String
- `last_selected_park_name`: String
- `background_alerts_enabled`: Boolean (Default: false)
- `default_alert_threshold`: Int (Default: 30)

### Dual-API Schnittstellen & Aggregator

#### 1. Wartezeiten.APP API (`WartezeitenApi`)
- Base URL: `https://api.wartezeiten.app/`
- Endpunkte:
  - `GET v1/parks` -> Parkliste
  - `GET v1/parks/{parkId}/waitingtimes` -> Attraktionsliste mit Live-Wartezeit & Status

#### 2. ThemeParks.wiki API (`ThemeParksWikiApi`)
- Base URL: `https://api.themeparks.wiki/v1/`
- Endpunkte:
  - `GET destinations` -> Parks & Resorts (inkl. Tripsdrill, Europa-Park, Phantasialand, etc.)
  - `GET entity/{parkId}/live` -> Live-Daten (Attraktionen, Wartezeiten im Standby-Queue, Status `OPERATING`/`CLOSED`/`DOWN`, `lastUpdated`)

#### 3. Korrektheits- & Plausibilitäts-Engine (`PlausibilityEngine`)
```kotlin
data class ResolvedAttraction(
    val id: String,
    val name: String,
    val waitTimeMinutes: Int?,
    val status: AttractionStatus,
    val primarySource: DataSource,
    val isCrossVerified: Boolean,
    val comparisonNote: String? = null,
    val lastUpdatedMillis: Long
)
```
- **Fuzzy Matching:** Attraktionen beider APIs werden anhand normalisierter Namen (Kleinschreibung, Bereinigung von Sonderzeichen wie "powered by...", Levenshtein-Distanz / Token-Match) gemappt.
- **Entscheidungs-Matrix für den korrekteren Wert:**
  1. **Frische-Check (`lastUpdated`):** Wenn eine Quelle signifikant neuer ist (> 15 Min Differenz im Timestamp), wird der neuere Wert als primär eingestuft.
  2. **Konsens-Check:** Weichen die Wartezeiten beider Quellen um <= 5 Minuten voneinander ab, gilt der Wert als `isCrossVerified = true`.
  3. **Ausfall-Priorität (`DOWN`/`CLOSED`):** Meldet eine Quelle eine aktuelle Betriebsstörung (`DOWN`) mit frischem Zeitstempel, während die andere noch eine alte Wartezeit meldet, hat die Störungsmeldung Vorrang (Warnung vor Enttäuschung vor Ort).
  4. **Fallback für exklusive Parks (z. B. Tripsdrill):** Parks, die nur auf ThemeParks.wiki existieren, werden transparent als Ein-Quellen-Daten (`primarySource = THEMEPARKS_WIKI`) ohne Diskrepanz geführt.

---

## 6. Definition of Done (DoD) pro Feature

### Feature 1: Park-Auswahl & Live-Wartezeiten
- **Ablauf:** Nutzer öffnet die App, wählt z. B. "Europa-Park", sieht die Liste der Attraktionen sortiert nach Wartezeit.
- **Akzeptanzkriterien:**
  - Farbcodierung: <= 15 min (Grün / `primaryContainer`), 15–45 min (Gelb/Orange / `tertiaryContainer`), > 45 min (Rot / `errorContainer`).
  - Geschlossene Attraktionen sind visuell klar als "Geschlossen" oder "Wartung" gekennzeichnet.
  - Pull-to-Refresh aktualisiert die Daten sofort.

### Feature 2: Favoriten-Verwaltung
- **Ablauf:** Klick auf den Favoriten-Stern an einer Attraktion speichert diese sofort in Room.
- **Akzeptanzkriterien:**
  - Stern wechselt animiert zwischen gefüllt und umrandet.
  - Auf dem `FavoritesScreen` sind alle gespeicherten Attraktionen auch nach Schließen und Neustarten der App sofort sichtbar.

### Feature 3: Hintergrund-Alerts via WorkManager
- **Ablauf:** Nutzer aktiviert Benachrichtigungen mit 20 min Schwellenwert.
- **Akzeptanzkriterien:**
  - Android 13+ Berechtigungsdialog für `POST_NOTIFICATIONS` wird sauber aufgerufen.
  - PeriodicWorkRequest prüft die Favoriten regelmäßig (im Rahmen der Android WorkManager Mindestintervalle).
  - Wenn eine Favoriten-Attraktion unter den Schwellenwert fällt, wird eine System-Notification mit Klick-Aktion in die App ausgelöst.

---

## 7. Phasenweise Implementierungs-Roadmap

### Phase 1: Projekt-Setup & M3 Theming
- [ ] Root- und App-`build.gradle.kts` mit `libs.versions.toml`, Compose M3, KSP und Hilt konfigurieren.
- [ ] M3 Theme (`Color.kt`, `Theme.kt`, `Type.kt`) mit Vibrant Teal `#009688` und Dynamic Color Support erstellen.
- [ ] `MainActivity` mit `enableEdgeToEdge()` und Basis-Scaffold aufsetzen.
- [ ] **Verifikation:** `./gradlew assembleDebug` kompiliert fehlerfrei.

### Phase 2: Daten-Architektur, Room & API Client
- [ ] Room Database, DAOs und Entities für Favoriten implementieren.
- [ ] DataStore Repository für Einstellungen anlegen.
- [ ] Retrofit/OkHttp Service für `api.wartezeiten.app` mit sauberen DTOs und Error-Handling aufsetzen.
- [ ] **Verifikation:** Unit-Tests für Room DAOs und Repositories bestehen (`./gradlew testDebugUnitTest`).

### Phase 3: Screens & Type-Safe Navigation Compose
- [ ] Navigation Graph mit Type-Safe Routes aufsetzen.
- [ ] `ParksScreen` mit SearchBar und Park-Karten bauen.
- [ ] `AttractionsScreen` mit M3 Filter-Chips, Pull-to-Refresh und Wartezeiten-Karten umsetzen.
- [ ] `FavoritesScreen` für Schnellzugriff fertigstellen.
- [ ] **Verifikation:** UI rendert fehlerfrei, Navigation zwischen allen Screens funktioniert.

### Phase 4: WorkManager & Notifikationen
- [ ] `WaitTimeAlertWorker` mit Android 13+ Notification-Channel und Permission-Prüfung implementieren.
- [ ] `SettingsScreen` mit Schwellenwert-Slider und Notifikations-Schalter ausstatten.
- [ ] **Verifikation:** Worker kann getestet werden und löst bei Schwellenwert-Unterschreitung Benachrichtigungen aus.

### Phase 5: Polish & Release-Ready
- [ ] Shimmer/Loading-Animationen und Empty-States ("Keine Favoriten vorhanden") einbinden.
- [ ] Namensnennung & Backlink zu `wartezeiten.app` gemäß API-Nutzungsbedingungen in Settings einfügen.
- [ ] Vollständige Unit-Test-Suite ausführen.
- [ ] **Verifikation:** `./gradlew testDebugUnitTest` und `./gradlew assembleDebug` laufen ohne Fehler durch.

---

## 8. Wichtige Build- & Test-Befehle
- App kompilieren: `./gradlew assembleDebug`
- Unit-Tests ausführen: `./gradlew testDebugUnitTest`
- Lint-Prüfung: `./gradlew lintDebug`
- App auf angeschlossenem Gerät/Emulator installieren: `./gradlew installDebug`
