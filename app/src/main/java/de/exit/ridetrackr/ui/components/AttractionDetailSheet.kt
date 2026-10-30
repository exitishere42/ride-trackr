package de.exit.ridetrackr.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import de.exit.ridetrackr.data.model.AttractionDetails
import de.exit.ridetrackr.data.model.AttractionStatus
import de.exit.ridetrackr.data.model.CoasterVisualType
import de.exit.ridetrackr.data.model.ResolvedAttraction
import de.exit.ridetrackr.ui.i18n.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AttractionDetailSheet(
    attraction: ResolvedAttraction,
    details: AttractionDetails,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onDismiss: () -> Unit,
    isEnglish: Boolean = false,
    viewModel: AttractionDetailViewModel = hiltViewModel()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val strings = LocalAppStrings.current
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    var showReportDialog by remember { mutableStateOf(false) }

    LaunchedEffect(attraction.id, attraction.name, details.parkName) {
        viewModel.loadData(attraction.id, attraction.name, details.parkName)
    }

    val wikiData by viewModel.wikiData.collectAsState()
    val isLoadingWiki by viewModel.isLoadingWiki.collectAsState()
    val latestReport by viewModel.latestUserReport.collectAsState()

    // Determine final values (prefer live Wikipedia, fallback to curated catalog)
    val finalManufacturer = wikiData?.manufacturer ?: details.manufacturer
    val finalSpeed = wikiData?.topSpeedKmh ?: details.topSpeedKmh
    val finalHeight = wikiData?.heightMeters ?: details.heightMeters
    val finalLength = wikiData?.trackLengthMeters ?: details.trackLengthMeters
    val finalInversions = wikiData?.inversions ?: details.inversions
    val finalDescription = wikiData?.description ?: if (isEnglish) details.descriptionEn else details.descriptionDe

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            var isImageLoaded by remember(wikiData?.imageUrl) { mutableStateOf(false) }
            var isImageError by remember(wikiData?.imageUrl) { mutableStateOf(false) }

            // 1. Header with Hero Icon, Attraction Name & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        val heroIcon = when (details.visualType) {
                            CoasterVisualType.LAUNCH_COASTER -> Icons.Default.RocketLaunch
                            CoasterVisualType.WATER_RIDE -> Icons.Default.DirectionsBoat
                            CoasterVisualType.DROP_TOWER -> Icons.Default.Height
                            else -> Icons.Default.LinearScale
                        }
                        Icon(
                            imageVector = heroIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = attraction.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val subtitlePark = details.parkName ?: wikiData?.title
                        subtitlePark?.let { pName ->
                            Text(
                                text = pName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = strings.close,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Wikipedia Hero Image (placed below the Header)
            if (wikiData?.imageUrl != null && !isImageError) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(175.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(wikiData?.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = attraction.name,
                        contentScale = ContentScale.Crop,
                        onSuccess = { isImageLoaded = true },
                        onError = { isImageError = true },
                        modifier = Modifier.fillMaxSize()
                    )
                    if (isImageLoaded) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.65f),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Wikipedia",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
                if (isImageLoaded) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Wait Time & Status Banner
            val isOperating = attraction.status == AttractionStatus.OPERATING
            val statusColor = when (attraction.status) {
                AttractionStatus.OPERATING -> MaterialTheme.colorScheme.primary
                AttractionStatus.CLOSED, AttractionStatus.DOWN -> MaterialTheme.colorScheme.error
                AttractionStatus.MAINTENANCE -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.outline
            }
            val statusText = when (attraction.status) {
                AttractionStatus.OPERATING -> strings.statusOpen
                AttractionStatus.CLOSED -> strings.statusClosed
                AttractionStatus.DOWN -> if (isEnglish) "Down" else "Störung"
                AttractionStatus.MAINTENANCE -> strings.statusMaintenance
                else -> if (isEnglish) "Unknown" else "Unbekannt"
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = statusColor.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${strings.currentStatus}: $statusText",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    val currentWait = latestReport?.waitTimeMinutes ?: attraction.waitTimeMinutes
                    if (isOperating && currentWait != null) {
                        val isEstimated = latestReport == null && attraction.comparisonNote?.let {
                            it.contains("Schätzung", ignoreCase = true) || it.contains("Dynamisch", ignoreCase = true)
                        } == true
                        val waitPrefix = if (isEstimated) "~ " else ""
                        Surface(
                            shape = CircleShape,
                            color = if (latestReport != null) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "$waitPrefix$currentWait ${strings.minutesShort}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Optional note / Schätzung info banner
            val noteToShow = if (latestReport != null) {
                val minAgo = ((System.currentTimeMillis() - latestReport!!.reportedAtMillis) / 60000L).coerceAtLeast(0)
                val ago = if (minAgo < 1) "gerade eben" else "vor $minAgo Min"
                val timeStr = java.text.SimpleDateFormat("HH:mm", java.util.Locale.GERMANY)
                    .format(java.util.Date(latestReport!!.reportedAtMillis))
                "${strings.ownReportBadge} ($timeStr Uhr • $ago): ${latestReport!!.waitTimeMinutes} Min"
            } else {
                attraction.comparisonNote
            }

            noteToShow?.let { note ->
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = if (latestReport != null) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Report Wait Time Card / Action
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedCard(
                onClick = { showReportDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.EditCalendar,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = strings.reportWaitTimeTitle,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            val subtitle = latestReport?.let {
                                val minAgo = ((System.currentTimeMillis() - it.reportedAtMillis) / 60000L).coerceAtLeast(0)
                                val ago = if (minAgo < 1) "gerade eben" else "vor $minAgo Min"
                                val dateFormatted = java.text.SimpleDateFormat("dd.MM.yyyy, HH:mm", java.util.Locale.GERMANY)
                                    .format(java.util.Date(it.reportedAtMillis))
                                "Deine Meldung: ${it.waitTimeMinutes} Min ($dateFormatted Uhr • $ago)"
                            } ?: strings.reportWaitTimeSubtitle
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    Button(
                        onClick = { showReportDialog = true },
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(text = strings.reportWaitTime, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Technical Specifications Grid (Hersteller, Alter, Größe, Speed etc.)
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.rideDetailsTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (isLoadingWiki) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Hersteller (Wikipedia / Katalog)
                        SpecItem(
                            icon = Icons.Default.Build,
                            label = strings.manufacturer,
                            value = finalManufacturer
                        )

                        // Mindestalter
                        SpecItem(
                            icon = Icons.Default.Person,
                            label = strings.minAge,
                            value = if (isEnglish) "from ${details.minAgeYears} years" else "ab ${details.minAgeYears} Jahren"
                        )

                        // Mindestgröße
                        SpecItem(
                            icon = Icons.Default.Height,
                            label = strings.minHeight,
                            value = if (isEnglish) "from ${details.minHeightCm} cm" else "ab ${details.minHeightCm} cm"
                        )

                        // Kategorie / Modell
                        SpecItem(
                            icon = Icons.Default.LinearScale,
                            label = if (wikiData?.model != null) strings.model else strings.category,
                            value = wikiData?.model ?: details.category
                        )

                        // Höchstgeschwindigkeit
                        finalSpeed?.let { speed ->
                            SpecItem(
                                icon = Icons.Default.Speed,
                                label = strings.topSpeed,
                                value = "$speed km/h"
                            )
                        }

                        // Höhe
                        finalHeight?.let { h ->
                            SpecItem(
                                icon = Icons.Default.Height,
                                label = strings.height,
                                value = "$h m"
                            )
                        }

                        // Streckenlänge
                        finalLength?.let { len ->
                            SpecItem(
                                icon = Icons.Default.LinearScale,
                                label = strings.trackLength,
                                value = "$len m"
                            )
                        }

                        // Inversionen
                        SpecItem(
                            icon = Icons.Default.Refresh,
                            label = strings.inversions,
                            value = if (finalInversions > 0) "$finalInversions" else strings.none
                        )

                        // Eröffnung (Wikipedia)
                        wikiData?.openingDate?.let { date ->
                            SpecItem(
                                icon = Icons.Default.CalendarMonth,
                                label = strings.openingYear,
                                value = date
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description Card (Wikipedia / Katalog)
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEnglish) "About this Ride" else "Über dieses Fahrgeschäft",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = if (wikiData != null) strings.sourceWikipedia else "Katalog",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = finalDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    wikiData?.pageUrl?.let { pageUrl ->
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { uriHandler.openUri(pageUrl) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.openInWikipedia,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons (Favorit & Schließen)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onToggleFavorite,
                    modifier = Modifier.weight(1f),
                    colors = if (isFavorite) {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    } else {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isFavorite) strings.removeFromFavorites else strings.addToFavorites,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(0.6f)
                ) {
                    Text(text = strings.close)
                }
            }
        }
    }

    if (showReportDialog) {
        var selectedMinutes by remember { mutableStateOf(latestReport?.waitTimeMinutes ?: attraction.waitTimeMinutes ?: 15) }
        val chipOptions = listOf(0, 5, 10, 15, 20, 25, 30, 45, 60, 75, 90)

        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = {
                Text(
                    text = strings.reportWaitTimeTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = attraction.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = strings.reportWaitTimeSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Wartezeit: $selectedMinutes Min",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        chipOptions.forEach { mins ->
                            FilterChip(
                                selected = selectedMinutes == mins,
                                onClick = { selectedMinutes = mins },
                                label = { Text("$mins min") }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val park = details.parkName ?: attraction.parkId
                        viewModel.submitWaitTime(
                            parkId = attraction.parkId.ifBlank { "tripsdrill" },
                            attractionId = attraction.id,
                            attractionName = attraction.name,
                            minutes = selectedMinutes,
                            onSuccess = {
                                Toast.makeText(context, strings.reportSuccess, Toast.LENGTH_SHORT).show()
                                showReportDialog = false
                            }
                        )
                    }
                ) {
                    Text(text = strings.submit)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showReportDialog = false }) {
                    Text(text = strings.cancel)
                }
            }
        )
    }
}

@Composable
private fun SpecItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.padding(2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
