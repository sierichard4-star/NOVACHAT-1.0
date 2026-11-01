package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NovaChatViewModel
import com.example.model.RideOption
import com.example.ui.theme.*

@Composable
fun NovaRideScreen(
    viewModel: NovaChatViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val uiState by viewModel.uiState.collectAsState()
    var pickupLocation by remember { mutableStateOf("Kotoka International Airport, Terminal 3") }
    var destinationLocation by remember { mutableStateOf("Osu Oxford Street, Accra") }

    val rideOptions = listOf(
        RideOption("r1", "NovaGo", "Economy hatchback • 4 seats", 35.0, 3, "DirectionsCar"),
        RideOption("r2", "NovaComfort", "Air-conditioned sedan • Extra legroom", 55.0, 5, "AirportShuttle"),
        RideOption("r3", "NovaBoda", "Fast express motorbike • Helmet included", 18.0, 2, "TwoWheeler")
    )

    var selectedOption by remember { mutableStateOf(rideOptions[0]) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NovaDarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("ride_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = NovaTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "NOVA RIDE",
                        color = NovaTextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "Ghana & West Africa Ride-Hailing",
                        color = NovaCyanAccent,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Simulated Dynamic Map Area
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF0F1E17),
                border = BorderStroke(1.dp, Color(0x3300E5BE))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Stylized grid and radar pulse
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
                        val step = 40.dp.toPx()
                        for (x in 0..(size.width / step).toInt()) {
                            drawLine(
                                color = Color(0x1A00E5BE),
                                start = androidx.compose.ui.geometry.Offset(x * step, 0f),
                                end = androidx.compose.ui.geometry.Offset(x * step, size.height),
                                strokeWidth = 1f
                            )
                        }
                        for (y in 0..(size.height / step).toInt()) {
                            drawLine(
                                color = Color(0x1A00E5BE),
                                start = androidx.compose.ui.geometry.Offset(0f, y * step),
                                end = androidx.compose.ui.geometry.Offset(size.width, y * step),
                                strokeWidth = 1f
                            )
                        }
                    }

                    // Route Points
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xCC091711),
                            border = BorderStroke(1.dp, NovaCyanAccent)
                        ) {
                            Text(
                                text = "Accra Central • 5 Drivers Nearby (DVLA Verified)",
                                color = NovaCyanAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pickup & Destination Inputs
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xCC0E1A14),
                border = BorderStroke(1.dp, Color(0x2600E5BE))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(NovaCyanAccent)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("PICKUP", color = NovaTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(pickupLocation, color = NovaTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    HorizontalDivider(
                        color = Color(0x1FFFFFFF),
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(NovaGold)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("DESTINATION", color = NovaTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(destinationLocation, color = NovaTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Active ride card if booked, otherwise options list
            val activeRide = uiState.activeRide
            if (activeRide != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xCC0D2319),
                    border = BorderStroke(1.5.dp, NovaCyanAccent)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = activeRide.status,
                                color = NovaCyanAccent,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Fare: GH₵ ${activeRide.fareGhs.toInt()}",
                                color = NovaTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x3300E5BE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Driver",
                                    tint = NovaTextPrimary
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = activeRide.driverName,
                                    color = NovaTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${activeRide.carModel} • ${activeRide.plateNumber}",
                                    color = NovaTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.cancelRide() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0x33FF5252),
                                contentColor = Color(0xFFFF5252)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancel Ride", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Text(
                    text = "Select Service Tier",
                    color = NovaTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Ride options
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    rideOptions.forEach { option ->
                        val isSelected = option.id == selectedOption.id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { selectedOption = option }
                                .testTag("ride_option_${option.id}"),
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) Color(0x3300E5BE) else Color(0xCC0E1A14),
                            border = BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) NovaCyanAccent else Color(0x1FFFFFFF)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when (option.vehicleIcon) {
                                            "TwoWheeler" -> Icons.Default.TwoWheeler
                                            "AirportShuttle" -> Icons.Default.AirportShuttle
                                            else -> Icons.Default.DirectionsCar
                                        },
                                        contentDescription = option.title,
                                        tint = if (isSelected) NovaCyanAccent else NovaTextPrimary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = option.title,
                                            color = NovaTextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "${option.subtitle} • ${option.etaMinutes} mins away",
                                            color = NovaTextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Text(
                                    text = "GH₵ ${option.estimatedFareGhs.toInt()}",
                                    color = NovaCyanAccent,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        viewModel.bookRide(pickupLocation, destinationLocation, selectedOption)
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NovaCyanAccent,
                        contentColor = Color(0xFF04140D)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .navigationBarsPadding()
                        .testTag("confirm_ride_booking_btn")
                ) {
                    Text(
                        text = "CONFIRM ${selectedOption.title.uppercase()} • GH₵ ${selectedOption.estimatedFareGhs.toInt()}",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
