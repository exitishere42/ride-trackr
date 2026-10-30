package de.exit.ridetrackr.data.model

data class Park(
    val id: String,
    val name: String,
    val source: DataSource,
    val country: String? = "Deutschland",
    val isOpen: Boolean = true
)
