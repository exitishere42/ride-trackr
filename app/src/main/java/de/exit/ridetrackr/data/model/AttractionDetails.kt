package de.exit.ridetrackr.data.model

data class AttractionDetails(
    val id: String,
    val name: String,
    val parkName: String? = null,
    val manufacturer: String,
    val minAgeYears: Int,
    val minHeightCm: Int,
    val category: String,
    val topSpeedKmh: Int? = null,
    val heightMeters: Int? = null,
    val trackLengthMeters: Int? = null,
    val inversions: Int = 0,
    val descriptionDe: String,
    val descriptionEn: String,
    val visualType: CoasterVisualType = CoasterVisualType.ROLLER_COASTER
)

enum class CoasterVisualType {
    ROLLER_COASTER,
    LAUNCH_COASTER,
    WOODEN_COASTER,
    WATER_RIDE,
    DARK_RIDE,
    DROP_TOWER,
    FAMILY_RIDE
}
