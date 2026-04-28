package com.example.traktrain

fun getDummyTrainStatus(trainNumber: String): TrainStatus {
    return when (trainNumber) {
        "12560" -> TrainStatus("12560", "Shiv Ganga Express", "Prayagraj Junction", "Running Late", 15, "22:30")
        "22436" -> TrainStatus("22436", "Vande Bharat Express", "Kanpur Central", "On Time", 0, "14:00")
        "12392" -> TrainStatus("12392", "Shramjivi Express", "Allahabad Junction", "Running Late", 30, "03:30")
        else -> TrainStatus(trainNumber, "Train Not Found", "Unknown", "Invalid Number", 0, "N/A")
    }
}

fun getDummyStations(trainNumber: String): List<StationStop> {
    return when (trainNumber) {
        "12560" -> listOf(
            StationStop("New Delhi", "Start", "20:05", 1),
            StationStop("Kanpur Central", "23:15", "23:20", 1),
            StationStop("Prayagraj Jn", "01:30", "01:35", 2),
            StationStop("Varanasi Jn", "04:00", "Destination", 2)
        )
        "22436" -> listOf(
            StationStop("New Delhi", "Start", "06:00", 1),
            StationStop("Kanpur Central", "09:45", "09:50", 1),
            StationStop("Prayagraj Jn", "11:30", "11:35", 1),
            StationStop("Varanasi Jn", "14:00", "Destination", 1)
        )
        else -> listOf(StationStop("No stations found", "N/A", "N/A", 1))
    }
}

fun getDummyPNRStatus(pnrNumber: String): PNRStatus {
    return when (pnrNumber) {
        "1234567890" -> PNRStatus("1234567890", "Shiv Ganga Express", "12560", "28-Apr-2026", "New Delhi", "Varanasi Jn", "Confirmed", "S4", "32")
        "9876543210" -> PNRStatus("9876543210", "Vande Bharat Express", "22436", "29-Apr-2026", "New Delhi", "Varanasi Jn", "Waitlist 5", "N/A", "N/A")
        else -> PNRStatus(pnrNumber, "Not Found", "N/A", "N/A", "N/A", "N/A", "Invalid PNR", "N/A", "N/A")
    }
}