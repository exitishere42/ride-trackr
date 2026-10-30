package de.exit.ridetrackr.ui.screens.attractions

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.exit.ridetrackr.data.model.ResolvedAttraction
import de.exit.ridetrackr.data.repository.AttractionDetailsProvider
import de.exit.ridetrackr.ui.components.AttractionCard
import de.exit.ridetrackr.ui.components.AttractionDetailSheet
import de.exit.ridetrackr.ui.i18n.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttractionsScreen(
    parkId: String,
    parkName: String,
    viewModel: AttractionsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(parkId) {
        viewModel.initialize(parkId, parkName)
    }

    val uiState by viewModel.uiState.collectAsState()
    val strings = LocalAppStrings.current
    var selectedAttraction by remember { mutableStateOf<ResolvedAttraction?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (parkName.isNotBlank()) parkName else uiState.parkName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.back
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::loadAttractions) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filter and Sort Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = uiState.filterMode == FilterMode.ALL,
                    onClick = { viewModel.setFilterMode(FilterMode.ALL) },
                    label = { Text(strings.filterAll) }
                )

                FilterChip(
                    selected = uiState.filterMode == FilterMode.OPEN_ONLY,
                    onClick = { viewModel.setFilterMode(FilterMode.OPEN_ONLY) },
                    label = { Text(strings.filterOpen) }
                )

                FilterChip(
                    selected = uiState.filterMode == FilterMode.SHORT_WAIT_ONLY,
                    onClick = { viewModel.setFilterMode(FilterMode.SHORT_WAIT_ONLY) },
                    label = { Text("<= 20 min") }
                )

                FilterChip(
                    selected = uiState.sortMode == SortMode.WAIT_TIME,
                    onClick = {
                        viewModel.setSortMode(
                            if (uiState.sortMode == SortMode.WAIT_TIME) SortMode.NAME else SortMode.WAIT_TIME
                        )
                    },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null) },
                    label = { Text(if (uiState.sortMode == SortMode.WAIT_TIME) strings.waitTimeLabel else "A-Z") }
                )
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (uiState.errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else if (uiState.attractions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = strings.noAttractionsFound,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.attractions, key = { it.id }) { attraction ->
                        AttractionCard(
                            attraction = attraction,
                            isFavorite = uiState.favoriteIds.contains(attraction.id),
                            onToggleFavorite = { viewModel.toggleFavorite(attraction) },
                            onClick = { selectedAttraction = attraction }
                        )
                    }
                }
            }
        }
    }

    selectedAttraction?.let { attr ->
        val details = remember(attr) {
            AttractionDetailsProvider.getDetails(attr.name, if (parkName.isNotBlank()) parkName else uiState.parkName)
        }
        AttractionDetailSheet(
            attraction = attr,
            details = details,
            isFavorite = uiState.favoriteIds.contains(attr.id),
            onToggleFavorite = { viewModel.toggleFavorite(attr) },
            onDismiss = { selectedAttraction = null },
            isEnglish = strings.open == "Open"
        )
    }
}
