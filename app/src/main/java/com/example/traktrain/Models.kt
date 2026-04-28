package com.example.traktrain

data class TrainStatus(
    val trainNumber: String,
    val trainName: String,
    val currentStation: String,
    val status: String,
    val delayMinutes: Int,
    val expectedArrival: String
)

data class StationStop(
    val stationName: String,
    val arrivalTime: String,
    val departureTime: String,
    val day: Int
)

data class PNRStatus(
    val pnrNumber: String,
    val trainName: String,
    val trainNumber: String,
    val journeyDate: String,
    val from: String,
    val to: String,
    val passengerStatus: String,
    val coachNumber: String,
    val seatNumber: String
)