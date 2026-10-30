package de.exit.ridetrackr.data.model

data class WikipediaRideData(
    val title: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val pageUrl: String? = null,
    val manufacturer: String? = null,
    val topSpeedKmh: Int? = null,
    val heightMeters: Int? = null,
    val trackLengthMeters: Int? = null,
    val inversions: Int? = null,
    val openingDate: String? = null,
    val model: String? = null
)
