package de.exit.ridetrackr.data.model

enum class DataSource {
    WARTEZEITEN_APP,
    THEMEPARKS_WIKI,
    CROSS_VERIFIED
}

enum class AttractionStatus {
    OPERATING,
    CLOSED,
    DOWN,
    MAINTENANCE,
    UNKNOWN
}
