package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.NovaChatViewModel
import com.example.ui.theme.*

@Composable
fun CallScreen(
    isVideo: Boolean,
    participantName: String,
    viewModel: NovaChatViewModel,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onEndCall() }

    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NovaDarkBackground)
    ) {
        if (isVideo && uiState.isCameraOn) {
            // Video Background
            Image(
                painter = painterResource(id = R.drawable.artisan_ama),
                contentDescription = participantName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dark gradient for controls
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x66000000))
            )

            // Self-view PiP (Picture in Picture)
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 16.dp, end = 16.dp)
                    .size(width = 100.dp, height = 140.dp)
                    .clip(RoundedCornerShape(16.dp)),
                color = Color(0xFF14241D),
                border = BorderStroke(1.5.dp, NovaCyanAccent)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "You",
                        tint = NovaCyanAccent,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }

        // Top Information
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isVideo || !uiState.isCameraOn) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color(0x3300E5BE))
                        .border(2.dp, NovaCyanAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = participantName,
                        tint = NovaTextPrimary,
                        modifier = Modifier.size(54.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            Text(
                text = participantName,
                color = NovaTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0x6600E5BE)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "E2EE",
                        tint = NovaCyanAccent,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "256-bit WebRTC E2EE • Connected",
                        color = NovaCyanAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Bottom Call Controls
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 24.dp, start = 20.dp, end = 20.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color(0xE60E1B15),
            border = BorderStroke(1.dp, Color(0x3300E5BE))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Mic toggle
                IconButton(
                    onClick = { viewModel.toggleMic() },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(if (uiState.isMicMuted) Color(0x40FF5252) else Color(0x33FFFFFF))
                        .testTag("call_mic_toggle")
                ) {
                    Icon(
                        imageVector = if (uiState.isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mute",
                        tint = if (uiState.isMicMuted) Color(0xFFFF5252) else NovaTextPrimary
                    )
                }

                // Camera toggle (if video)
                if (isVideo) {
                    IconButton(
                        onClick = { viewModel.toggleCamera() },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (!uiState.isCameraOn) Color(0x40FF5252) else Color(0x33FFFFFF))
                            .testTag("call_cam_toggle")
                    ) {
                        Icon(
                            imageVector = if (uiState.isCameraOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
                            contentDescription = "Camera",
                            tint = if (uiState.isCameraOn) NovaTextPrimary else Color(0xFFFF5252)
                        )
                    }
                }

                // Speaker toggle
                IconButton(
                    onClick = { viewModel.toggleSpeaker() },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(if (uiState.isSpeakerOn) Color(0x3300E5BE) else Color(0x33FFFFFF))
                        .testTag("call_speaker_toggle")
                ) {
                    Icon(
                        imageVector = if (uiState.isSpeakerOn) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeMute,
                        contentDescription = "Speaker",
                        tint = if (uiState.isSpeakerOn) NovaCyanAccent else NovaTextPrimary
                    )
                }

                // End Call (Red Button)
                IconButton(
                    onClick = onEndCall,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF3B30))
                        .testTag("call_end_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
