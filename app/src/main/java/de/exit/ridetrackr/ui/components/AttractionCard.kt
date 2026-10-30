package de.exit.ridetrackr.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.exit.ridetrackr.data.model.AttractionStatus
import de.exit.ridetrackr.data.model.DataSource
import de.exit.ridetrackr.data.model.ResolvedAttraction

@Composable
fun AttractionCard(
    attraction: ResolvedAttraction,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = attraction.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(onClick = onToggleFavorite) {
                    val starColor by animateColorAsState(
                        targetValue = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "starColor"
                    )
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = if (isFavorite) "Favorit entfernen" else "Als Favorit speichern",
                        tint = starColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Wait Time or Status Badge
                WaitTimeBadge(attraction = attraction)

                // Source Badge
                SourceBadge(source = attraction.primarySource)
            }

            // Comparison note / Schätzung if available
            attraction.comparisonNote?.let { note ->
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = note,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WaitTimeBadge(attraction: ResolvedAttraction) {
    val wait = attraction.waitTimeMinutes

    val (bgColor, textColor, text) = when {
        attraction.status == AttractionStatus.DOWN -> {
            Triple(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer, "Störung")
        }
        attraction.status == AttractionStatus.CLOSED -> {
            Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, "Geschlossen")
        }
        attraction.status == AttractionStatus.MAINTENANCE -> {
            Triple(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer, "Wartung")
        }
        wait != null -> {
            val isEstimated = attraction.comparisonNote?.let { it.contains("Schätzung", ignoreCase = true) || it.contains("Dynamisch", ignoreCase = true) } == true
            val prefix = if (isEstimated) "~ " else ""
            when {
                wait <= 15 -> Triple(Color(0xFFE8F5E9), Color(0xFF1B5E20), "$prefix$wait min")
                wait <= 45 -> Triple(Color(0xFFFFF8E1), Color(0xFFE65100), "$prefix$wait min")
                else -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "$prefix$wait min")
            }
        }
        else -> {
            Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, "Keine Info")
        }
    }

    Surface(
        color = bgColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun SourceBadge(source: DataSource) {
    val (label, icon) = when (source) {
        DataSource.CROSS_VERIFIED -> Pair("Verifiziert (Dual-API)", true)
        DataSource.THEMEPARKS_WIKI -> Pair("ThemeParks.wiki", false)
        DataSource.WARTEZEITEN_APP -> Pair("Wartezeiten.APP", false)
    }

    AssistChip(
        onClick = {},
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall
            )
        },
        leadingIcon = if (icon) {
            {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        } else null,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    )
}
