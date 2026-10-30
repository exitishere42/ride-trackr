package de.exit.ridetrackr.data.model

data class ResolvedAttraction(
    val id: String,
    val parkId: String,
    val name: String,
    val waitTimeMinutes: Int?,
    val status: AttractionStatus,
    val primarySource: DataSource,
    val isCrossVerified: Boolean,
    val comparisonNote: String? = null,
    val lastUpdatedMillis: Long = System.currentTimeMillis()
)
