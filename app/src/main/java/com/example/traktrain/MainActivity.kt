package com.example.traktrain


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.traktrain.ui.theme.TraktrainTheme
import com.example.traktrain.TrainStatus
import com.example.traktrain.StationStop
import com.example.traktrain.PNRStatus

// Colors
val NavyBlue = Color(0xFF1A237E)
val Orange = Color(0xFFFF6F00)
val LightGray = Color(0xFFF5F5F5)
val Green = Color(0xFF2E7D32)
val Red = Color(0xFFC62828)


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TraktrainTheme {
                TrainTrackerApp()
            }
        }
    }
}

@Composable
fun TrainTrackerApp() {

    var selectedTab by remember { mutableStateOf(0) }
    var trainNumber by remember { mutableStateOf("") }
    var searchResult by remember { mutableStateOf<TrainStatus?>(null) }
    var stationList by remember { mutableStateOf<List<StationStop>>(emptyList()) }
    var pnrNumber by remember { mutableStateOf("") }
    var pnrResult by remember { mutableStateOf<PNRStatus?>(null) }
    var isLoadingTrain by remember { mutableStateOf(false) }
    var isLoadingPNR by remember { mutableStateOf(false) }
    var recentSearches by remember { mutableStateOf<List<String>>(emptyList()) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightGray)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavyBlue)
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "🚂 TrakTrain",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Live Train Tracker",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = NavyBlue,
            contentColor = Color.White
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Track Train") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("PNR Status") }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (selectedTab == 0) {
                item {
                    RecentSearchesCard(
                        searches = recentSearches,
                        onSearchClick = { search ->
                            trainNumber = search
                            isLoadingTrain = true
                            Thread {
                                Thread.sleep(1500)
                                searchResult = getDummyTrainStatus(search)
                                stationList = getDummyStations(search)
                                isLoadingTrain = false
                            }.start()
                        }
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Search Train",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = NavyBlue
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = trainNumber,
                                onValueChange = { trainNumber = it },
                                label = { Text("Enter Train Number") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NavyBlue,
                                    focusedLabelColor = NavyBlue
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    if (trainNumber.isEmpty()) {
                                        errorMessage = "Please enter a train number!"
                                    } else {
                                        errorMessage = ""
                                        if (!recentSearches.contains(trainNumber)) {
                                            recentSearches = (listOf(trainNumber) + recentSearches).take(5)
                                        }
                                        isLoadingTrain = true
                                        Thread {
                                            val apiResult = TrainApiService.getTrainStatus(trainNumber)
                                            val result = apiResult ?: getDummyTrainStatus(trainNumber)
                                            if (result.trainName == "Train Not Found") {
                                                errorMessage = "Train $trainNumber not found!"
                                                searchResult = null
                                                stationList = emptyList()
                                            } else {
                                                errorMessage = ""
                                                searchResult = result
                                                stationList = getDummyStations(trainNumber)
                                            }
                                            isLoadingTrain = false
                                        }.start()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NavyBlue,
                                    contentColor = Color.White
                                )
                            ) {
                                if (isLoadingTrain) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        "Track Train",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    searchResult?.let { status ->
                        TrainStatusCard(status)
                    }
                }

                item {
                    if (stationList.isNotEmpty()) {
                        StationListCard(stationList)
                    }
                }
                item {
                    ErrorCard(
                        message = errorMessage,
                        onRetry = { errorMessage = "" }
                    )
                }
            }

            if (selectedTab == 1) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Check PNR Status",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = NavyBlue
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = pnrNumber,
                                onValueChange = { pnrNumber = it },
                                label = { Text("Enter 10 digit PNR Number") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NavyBlue,
                                    focusedLabelColor = NavyBlue
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    if (pnrNumber.isEmpty()) {
                                        errorMessage = "Please enter PNR number!"
                                    } else if (pnrNumber.length != 10) {
                                        errorMessage = "PNR must be 10 digits!"
                                    } else {
                                        errorMessage = ""
                                        isLoadingPNR = true
                                        Thread {
                                            val apiResult = TrainApiService.getPNRStatus(pnrNumber)
                                            val result = apiResult ?: getDummyPNRStatus(pnrNumber)
                                            if (result.trainName == "Not Found") {
                                                errorMessage = "PNR $pnrNumber not found!"
                                                pnrResult = null
                                            } else {
                                                errorMessage = ""
                                                pnrResult = result
                                            }
                                            isLoadingPNR = false
                                        }.start()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NavyBlue,
                                    contentColor = Color.White
                                )
                            ) {
                                if (isLoadingPNR) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        "Check PNR",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    pnrResult?.let { status ->
                        PNRStatusCard(status)
                    }
                }
                item {
                    ErrorCard(
                        message = errorMessage,
                        onRetry = { errorMessage = "" }
                    )
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
        Text(text = value, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

@Composable
fun RecentSearchesCard(
    searches: List<String>,
    onSearchClick: (String) -> Unit
) {
    if (searches.isEmpty()) return
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🕐 Recent Searches",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = NavyBlue
            )
            Spacer(modifier = Modifier.height(8.dp))
            searches.forEach { search ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🚂 $search", fontSize = 14.sp)
                    TextButton(onClick = { onSearchClick(search) }) {
                        Text("Search Again", color = NavyBlue, fontSize = 13.sp)
                    }
                }
                Divider(color = Color.LightGray)
            }
        }
    }
}

@Composable
fun TrainStatusCard(status: TrainStatus) {
    val statusColor = when {
        status.delayMinutes == 0 -> Green
        status.delayMinutes < 30 -> Orange
        else -> Red
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyBlue, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = status.trainName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Train #${status.trainNumber}",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Status", color = Color.Gray, fontSize = 14.sp)
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = status.status,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            InfoRow("📍 Current Station", status.currentStation)
            InfoRow("⏱️ Delay", "${status.delayMinutes} minutes")
            InfoRow("🕐 Expected Arrival", status.expectedArrival)
        }
    }
}

@Composable
fun StationListCard(stations: List<StationStop>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🚉 Station Schedule",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = NavyBlue
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyBlue.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text(
                    "Station",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                    color = NavyBlue,
                    fontSize = 13.sp
                )
                Text("Arr", fontWeight = FontWeight.Bold, color = NavyBlue, fontSize = 13.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Text("Dep", fontWeight = FontWeight.Bold, color = NavyBlue, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            stations.forEachIndexed { index, station ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                if (index == 0) Green
                                else if (index == stations.size - 1) Red
                                else Orange,
                                RoundedCornerShape(50)
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        station.stationName,
                        modifier = Modifier.weight(1f),
                        fontSize = 13.sp
                    )
                    Text(station.arrivalTime, fontSize = 13.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(station.departureTime, fontSize = 13.sp, color = Color.Gray)
                }
                if (index < stations.size - 1) Divider(color = Color.LightGray)
            }
        }
    }
}

@Composable
fun PNRStatusCard(status: PNRStatus) {
    val statusColor = when {
        status.passengerStatus == "Confirmed" -> Green
        status.passengerStatus.startsWith("Waitlist") -> Orange
        else -> Red
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyBlue, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "PNR: ${status.pnrNumber}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${status.trainName} (${status.trainNumber})",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Passenger Status", color = Color.Gray, fontSize = 14.sp)
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = status.passengerStatus,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            InfoRow("📅 Journey Date", status.journeyDate)
            InfoRow("🚉 From", status.from)
            InfoRow("🏁 To", status.to)
            InfoRow("🚃 Coach", status.coachNumber)
            InfoRow("💺 Seat", status.seatNumber)
        }
    }
}
