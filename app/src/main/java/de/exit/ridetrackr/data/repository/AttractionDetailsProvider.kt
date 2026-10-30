package de.exit.ridetrackr.data.repository

import de.exit.ridetrackr.data.model.AttractionDetails
import de.exit.ridetrackr.data.model.CoasterVisualType

object AttractionDetailsProvider {

    // Curated catalog for famous European rides
    private val catalog = mapOf(
        // Tripsdrill
        "mammut" to AttractionDetails(
            id = "mammut",
            name = "Mammut",
            parkName = "Erlebnispark Tripsdrill",
            manufacturer = "Gerstlauer Amusement Rides",
            minAgeYears = 6,
            minHeightCm = 120,
            category = "Holzachterbahn",
            topSpeedKmh = 90,
            heightMeters = 30,
            trackLengthMeters = 860,
            inversions = 0,
            descriptionDe = "Deutschlands erste thematisierte Holzachterbahn führt durch eine historische Sägemühle mit rasanten Kurven und Tunnelfahrten.",
            descriptionEn = "Germany's first themed wooden roller coaster rushes through a historic sawmill with fast turns and tunnels.",
            visualType = CoasterVisualType.WOODEN_COASTER
        ),
        "karacho" to AttractionDetails(
            id = "karacho",
            name = "Karacho",
            parkName = "Erlebnispark Tripsdrill",
            manufacturer = "Gerstlauer Amusement Rides",
            minAgeYears = 8,
            minHeightCm = 125,
            category = "Infinity Coaster (Launch)",
            topSpeedKmh = 100,
            heightMeters = 30,
            trackLengthMeters = 700,
            inversions = 4,
            descriptionDe = "Katapultstart von 0 auf 100 km/h in nur 1,6 Sekunden gefolgt von 4 Überkopfelementen und einem Top-Hat.",
            descriptionEn = "Catapult launch from 0 to 100 km/h in just 1.6 seconds followed by 4 inversions and a top hat element.",
            visualType = CoasterVisualType.LAUNCH_COASTER
        ),
        "hals-über-kopf" to AttractionDetails(
            id = "hals-über-kopf",
            name = "Hals-über-Kopf",
            parkName = "Erlebnispark Tripsdrill",
            manufacturer = "Vekoma Rides Manufacturing",
            minAgeYears = 10,
            minHeightCm = 130,
            category = "Suspended Thrill Coaster",
            topSpeedKmh = 80,
            heightMeters = 30,
            trackLengthMeters = 800,
            inversions = 4,
            descriptionDe = "Hängend unter der Schiene braust der Zug mit freischwebenden Füßen durch 4 spektakuläre Überkopffiguren.",
            descriptionEn = "Suspended below the track, riders soar with hanging feet through 4 thrilling inversions.",
            visualType = CoasterVisualType.ROLLER_COASTER
        ),
        "volldampf" to AttractionDetails(
            id = "volldampf",
            name = "Volldampf",
            parkName = "Erlebnispark Tripsdrill",
            manufacturer = "Vekoma Rides Manufacturing",
            minAgeYears = 4,
            minHeightCm = 95,
            category = "Family Boomerang Coaster",
            topSpeedKmh = 60,
            heightMeters = 22,
            trackLengthMeters = 500,
            inversions = 0,
            descriptionDe = "Familien-Achterbahn im Stil einer historischen Dampflok, die die Strecke vorwärts und rückwärts befährt.",
            descriptionEn = "Family coaster themed as a historic steam locomotive traveling the track forwards and backwards.",
            visualType = CoasterVisualType.FAMILY_RIDE
        ),
        "g'sengte sau" to AttractionDetails(
            id = "g'sengte sau",
            name = "G'sengte Sau",
            parkName = "Erlebnispark Tripsdrill",
            manufacturer = "Gerstlauer Amusement Rides",
            minAgeYears = 4,
            minHeightCm = 100,
            category = "Bobsled Coaster",
            topSpeedKmh = 50,
            heightMeters = 16,
            trackLengthMeters = 480,
            inversions = 0,
            descriptionDe = "Die allererste Achterbahn von Gerstlauer windet sich kurvenreich um die Burg Rauhe Klinge.",
            descriptionEn = "The very first Gerstlauer roller coaster winds around the historic castle Rauhe Klinge.",
            visualType = CoasterVisualType.ROLLER_COASTER
        ),
        "badewannen-fahrt" to AttractionDetails(
            id = "badewannen-fahrt",
            name = "Badewannen-Fahrt",
            parkName = "Erlebnispark Tripsdrill",
            manufacturer = "Mack Rides",
            minAgeYears = 4,
            minHeightCm = 100,
            category = "Wildwasserbahn (Log Flume)",
            topSpeedKmh = 60,
            heightMeters = 14,
            trackLengthMeters = 350,
            inversions = 0,
            descriptionDe = "Erfrischende Schussfahrt in fliegenden Badewannen mit spektakulärem Rückwärtsschuss in den Burgsee.",
            descriptionEn = "Refreshing plunge in flying bathtubs with a thrilling backward splash into the castle lake.",
            visualType = CoasterVisualType.WATER_RIDE
        ),
        "waschzuber-rafting" to AttractionDetails(
            id = "waschzuber-rafting",
            name = "Waschzuber-Rafting",
            parkName = "Erlebnispark Tripsdrill",
            manufacturer = "Hafema",
            minAgeYears = 4,
            minHeightCm = 100,
            category = "Rapid River",
            topSpeedKmh = 35,
            heightMeters = 5,
            trackLengthMeters = 450,
            inversions = 0,
            descriptionDe = "Turbulente Wildwasserfahrt in runden Waschzubern mit Strudeln, Fontänen und spritzigen Stromschnellen.",
            descriptionEn = "Turbulent whitewater ride in round wash tubs featuring whirlpools, water fountains and splashy rapids.",
            visualType = CoasterVisualType.WATER_RIDE
        ),

        // Europa-Park
        "voltron nevera" to AttractionDetails(
            id = "voltron",
            name = "Voltron Nevera",
            parkName = "Europa-Park",
            manufacturer = "Mack Rides",
            minAgeYears = 8,
            minHeightCm = 130,
            category = "Stryker Coaster (Multi-Launch)",
            topSpeedKmh = 100,
            heightMeters = 32,
            trackLengthMeters = 1385,
            inversions = 7,
            descriptionDe = "Weltrekord-Achterbahn mit 105 Grad steilem Launch, 7 Überkopfelementen und Nikola-Tesla-Thematisierung.",
            descriptionEn = "Record-breaking coaster with a 105-degree steep launch, 7 inversions, and Nikola Tesla theming.",
            visualType = CoasterVisualType.LAUNCH_COASTER
        ),
        "silver star" to AttractionDetails(
            id = "silver star",
            name = "Silver Star",
            parkName = "Europa-Park",
            manufacturer = "Bolliger & Mabillard (B&M)",
            minAgeYears = 11,
            minHeightCm = 140,
            category = "Hyper Coaster",
            topSpeedKmh = 127,
            heightMeters = 73,
            trackLengthMeters = 1620,
            inversions = 0,
            descriptionDe = "Einer der höchsten und schnellsten Hyper Coaster Europas mit gigantischer Airtime und 4G-Kräften.",
            descriptionEn = "One of the tallest and fastest hyper coasters in Europe featuring immense airtime and 4G forces.",
            visualType = CoasterVisualType.ROLLER_COASTER
        ),
        "blue fire" to AttractionDetails(
            id = "blue fire",
            name = "Blue Fire Megacoaster",
            parkName = "Europa-Park",
            manufacturer = "Mack Rides",
            minAgeYears = 7,
            minHeightCm = 130,
            category = "Launch Coaster (LSM)",
            topSpeedKmh = 100,
            heightMeters = 38,
            trackLengthMeters = 1056,
            inversions = 4,
            descriptionDe = "Katapultiert die Fahrgäste in 2,5 Sekunden auf 100 km/h, inklusive dem höchsten Looping eines Katapult-Coasters in Europa.",
            descriptionEn = "Launches riders to 100 km/h in 2.5 seconds, featuring the highest loop on any catapult coaster in Europe.",
            visualType = CoasterVisualType.LAUNCH_COASTER
        ),
        "wodan" to AttractionDetails(
            id = "wodan",
            name = "WODAN - Timburcoaster",
            parkName = "Europa-Park",
            manufacturer = "Great Coasters International (GCI)",
            minAgeYears = 6,
            minHeightCm = 120,
            category = "Wooden Coaster",
            topSpeedKmh = 100,
            heightMeters = 40,
            trackLengthMeters = 1050,
            inversions = 0,
            descriptionDe = "Gigantische Holzachterbahn aus nordischem Kiefernholz, die sich kreuz und quer durch Blue Fire und Atlantica SuperSplash schlängelt.",
            descriptionEn = "Massive wooden coaster constructed from pine wood criss-crossing Blue Fire and Atlantica SuperSplash.",
            visualType = CoasterVisualType.WOODEN_COASTER
        ),

        // Phantasialand
        "taron" to AttractionDetails(
            id = "taron",
            name = "Taron",
            parkName = "Phantasialand",
            manufacturer = "Intamin",
            minAgeYears = 8,
            minHeightCm = 130,
            category = "Multi-Launch Coaster",
            topSpeedKmh = 117,
            heightMeters = 30,
            trackLengthMeters = 1320,
            inversions = 0,
            descriptionDe = "Der schnellste Multi-Launch Coaster der Welt schlängelt sich durch die vulkanische Basalt-Landschaft von Klugheim mit 58 Schienenkreuzungen.",
            descriptionEn = "The world's fastest multi-launch coaster threading through Klugheim's volcanic landscape with 58 track intersections.",
            visualType = CoasterVisualType.LAUNCH_COASTER
        ),
        "black mamba" to AttractionDetails(
            id = "black mamba",
            name = "Black Mamba",
            parkName = "Phantasialand",
            manufacturer = "Bolliger & Mabillard (B&M)",
            minAgeYears = 10,
            minHeightCm = 140,
            category = "Inverted Coaster",
            topSpeedKmh = 80,
            heightMeters = 26,
            trackLengthMeters = 768,
            inversions = 4,
            descriptionDe = "Inverted Coaster tief in den afrikanischen Dschungel und Felsenschluchten eingegraben mit Near-Miss-Effekten.",
            descriptionEn = "Inverted coaster deeply embedded into African ravines and jungle with intense near-miss effects.",
            visualType = CoasterVisualType.ROLLER_COASTER
        ),
        "f.l.y." to AttractionDetails(
            id = "fly",
            name = "F.L.Y.",
            parkName = "Phantasialand",
            manufacturer = "Vekoma Rides Manufacturing",
            minAgeYears = 8,
            minHeightCm = 130,
            category = "Flying Launch Coaster",
            topSpeedKmh = 78,
            heightMeters = 25,
            trackLengthMeters = 1236,
            inversions = 2,
            descriptionDe = "Der längste Flying Coaster der Welt im Steampunk-Themenbereich Rookburgh mit zwei Katapultstarts im Liegen.",
            descriptionEn = "The world's longest flying coaster in the steampunk realm Rookburgh with two horizontal launches.",
            visualType = CoasterVisualType.LAUNCH_COASTER
        ),

        // Heide Park
        "colossos" to AttractionDetails(
            id = "colossos",
            name = "Colossos - Kampf der Giganten",
            parkName = "Heide Park Resort",
            manufacturer = "Intamin",
            minAgeYears = 12,
            minHeightCm = 140,
            category = "Holzachterbahn (Prefabricated)",
            topSpeedKmh = 110,
            heightMeters = 52,
            trackLengthMeters = 1344,
            inversions = 0,
            descriptionDe = "Höchste Holzachterbahn Europas mit 61 Grad steiler Abfahrt und erbittertem Kampf gegen das gigantische Holzfeuer-Monster.",
            descriptionEn = "Europe's tallest wooden coaster featuring a 61-degree drop into the belly of the giant fire demon.",
            visualType = CoasterVisualType.WOODEN_COASTER
        ),
        "flug der dämonen" to AttractionDetails(
            id = "flug der dämonen",
            name = "Flug der Dämonen",
            parkName = "Heide Park Resort",
            manufacturer = "Bolliger & Mabillard (B&M)",
            minAgeYears = 10,
            minHeightCm = 140,
            category = "Wing Coaster",
            topSpeedKmh = 100,
            heightMeters = 40,
            trackLengthMeters = 772,
            inversions = 5,
            descriptionDe = "Deutschlands einziger Wing Coaster, bei dem man links und rechts neben der Schiene frei im Wind sitzt.",
            descriptionEn = "Germany's only wing coaster placing riders alongside the track with nothing above or below them.",
            visualType = CoasterVisualType.ROLLER_COASTER
        )
    )

    /**
     * Resolves complete attraction details for any ride.
     * Checks curated catalog first; falls back to an intelligent heuristic engine.
     */
    fun getDetails(rideName: String, parkName: String? = null): AttractionDetails {
        val normalized = rideName.trim().lowercase()

        // 1. Direct match in curated catalog
        for ((key, details) in catalog) {
            if (normalized.contains(key) || key.contains(normalized)) {
                return details.copy(name = rideName, parkName = parkName ?: details.parkName)
            }
        }

        // 2. Intelligent heuristic engine based on attraction name & keywords
        return generateHeuristicDetails(rideName, parkName)
    }

    private fun generateHeuristicDetails(name: String, parkName: String?): AttractionDetails {
        val lower = name.lowercase()

        return when {
            // Water rides
            lower.contains("wasser") || lower.contains("flume") || lower.contains("rafting") ||
                    lower.contains("splash") || lower.contains("boot") || lower.contains("river") -> {
                AttractionDetails(
                    id = name.hashCode().toString(),
                    name = name,
                    parkName = parkName,
                    manufacturer = "Mack Rides / Hafema",
                    minAgeYears = 4,
                    minHeightCm = 100,
                    category = "Wildwasserbahn & Rafting",
                    topSpeedKmh = 45,
                    heightMeters = 15,
                    trackLengthMeters = 420,
                    inversions = 0,
                    descriptionDe = "Erfrischende Wasserattraktion mit spritzigen Stromschnellen, Kurven und einer rasanten Schussfahrt.",
                    descriptionEn = "Refreshing water attraction with fast rapids, turns, and an exciting plunge.",
                    visualType = CoasterVisualType.WATER_RIDE
                )
            }

            // Launch / High Speed Coasters
            lower.contains("launch") || lower.contains("speed") || lower.contains("blitz") ||
                    lower.contains("express") || lower.contains("bullet") || lower.contains("turbo") -> {
                AttractionDetails(
                    id = name.hashCode().toString(),
                    name = name,
                    parkName = parkName,
                    manufacturer = "Intamin / Mack Rides",
                    minAgeYears = 8,
                    minHeightCm = 130,
                    category = "Katapult-Achterbahn (Launch)",
                    topSpeedKmh = 105,
                    heightMeters = 35,
                    trackLengthMeters = 980,
                    inversions = 3,
                    descriptionDe = "Hochenergetischer Katapultstart mit gewaltigen Beschleunigungskräften und dynamischen Kurvenlagen.",
                    descriptionEn = "High-energy catapult launch with intense acceleration and high-speed banked turns.",
                    visualType = CoasterVisualType.LAUNCH_COASTER
                )
            }

            // Wooden Coasters
            lower.contains("wood") || lower.contains("holz") || lower.contains("timber") || lower.contains("beast") -> {
                AttractionDetails(
                    id = name.hashCode().toString(),
                    name = name,
                    parkName = parkName,
                    manufacturer = "Great Coasters International (GCI)",
                    minAgeYears = 6,
                    minHeightCm = 120,
                    category = "Holzachterbahn",
                    topSpeedKmh = 85,
                    heightMeters = 32,
                    trackLengthMeters = 950,
                    inversions = 0,
                    descriptionDe = "Klassische Holzachterbahn mit unverwechselbarem Rattern, wilden Kurven und reichlich Airtime-Hügeln.",
                    descriptionEn = "Classic wooden coaster with its unmistakable roar, thrilling curves, and airtime hills.",
                    visualType = CoasterVisualType.WOODEN_COASTER
                )
            }

            // Inverted / Looping
            lower.contains("loop") || lower.contains("invert") || lower.contains("spin") ||
                    lower.contains("twist") || lower.contains("cobra") || lower.contains("flight") -> {
                AttractionDetails(
                    id = name.hashCode().toString(),
                    name = name,
                    parkName = parkName,
                    manufacturer = "Vekoma / Bolliger & Mabillard",
                    minAgeYears = 10,
                    minHeightCm = 130,
                    category = "Looping-Achterbahn",
                    topSpeedKmh = 82,
                    heightMeters = 30,
                    trackLengthMeters = 750,
                    inversions = 4,
                    descriptionDe = "Aufregende Überkopffahrten mit intensiven G-Kräften und dynamischen Inversionen.",
                    descriptionEn = "Thrilling inversions with intense G-forces and dynamic overbanked turns.",
                    visualType = CoasterVisualType.ROLLER_COASTER
                )
            }

            // Drop towers / Freefall
            lower.contains("tower") || lower.contains("turm") || lower.contains("drop") || lower.contains("freefall") -> {
                AttractionDetails(
                    id = name.hashCode().toString(),
                    name = name,
                    parkName = parkName,
                    manufacturer = "Funtime / Intamin",
                    minAgeYears = 8,
                    minHeightCm = 120,
                    category = "Freifallturm (Freefall Tower)",
                    topSpeedKmh = 95,
                    heightMeters = 60,
                    trackLengthMeters = null,
                    inversions = 0,
                    descriptionDe = "Atemberaubender Ausblick über den Park gefolgt von einem plötzlichen, schwerelosen freien Fall in die Tiefe.",
                    descriptionEn = "Breathtaking panoramic view over the park followed by a sudden, weightless free fall.",
                    visualType = CoasterVisualType.DROP_TOWER
                )
            }

            // Family / Kids rides
            lower.contains("kiddy") || lower.contains("mini") || lower.contains("kind") ||
                    lower.contains("familie") || lower.contains("wurm") || lower.contains("zug") -> {
                AttractionDetails(
                    id = name.hashCode().toString(),
                    name = name,
                    parkName = parkName,
                    manufacturer = "Zierer / Gerstlauer",
                    minAgeYears = 4,
                    minHeightCm = 95,
                    category = "Familienachterbahn",
                    topSpeedKmh = 40,
                    heightMeters = 12,
                    trackLengthMeters = 380,
                    inversions = 0,
                    descriptionDe = "Sanfter Fahrspaß für die ganze Familie mit sanften Hügeln und kinderfreundlicher Kurvenführung.",
                    descriptionEn = "Gentle ride experience for the whole family with friendly hills and curves.",
                    visualType = CoasterVisualType.FAMILY_RIDE
                )
            }

            // Dark rides / Themed indoor
            lower.contains("dark") || lower.contains("schloss") || lower.contains("geister") ||
                    lower.contains("ghost") || lower.contains("quest") || lower.contains("abenteuer") -> {
                AttractionDetails(
                    id = name.hashCode().toString(),
                    name = name,
                    parkName = parkName,
                    manufacturer = "Mack Rides / ETF Ride Systems",
                    minAgeYears = 4,
                    minHeightCm = 90,
                    category = "Themenfahrt (Dark Ride)",
                    topSpeedKmh = 25,
                    heightMeters = 8,
                    trackLengthMeters = 320,
                    inversions = 0,
                    descriptionDe = "Aufwändig thematisierte Indoor-Erlebnisfahrt mit Spezialeffekten, Animatronics und stimmungsvoller Musik.",
                    descriptionEn = "Elaborately themed indoor journey with special effects, animatronics, and atmospheric music.",
                    visualType = CoasterVisualType.DARK_RIDE
                )
            }

            // Generic Roller Coaster / Theme Park Ride
            else -> {
                AttractionDetails(
                    id = name.hashCode().toString(),
                    name = name,
                    parkName = parkName,
                    manufacturer = "Gerstlauer / Mack Rides",
                    minAgeYears = 6,
                    minHeightCm = 110,
                    category = "Achterbahn & Fahrgeschäft",
                    topSpeedKmh = 65,
                    heightMeters = 24,
                    trackLengthMeters = 650,
                    inversions = 0,
                    descriptionDe = "Beliebte Park-Attraktion mit rasanter Streckenführung, schnellen Richtungswechseln und tollem Fahrerlebnis.",
                    descriptionEn = "Popular park attraction featuring fast-paced tracks, swift turns, and great ride thrills.",
                    visualType = CoasterVisualType.ROLLER_COASTER
                )
            }
        }
    }
}
