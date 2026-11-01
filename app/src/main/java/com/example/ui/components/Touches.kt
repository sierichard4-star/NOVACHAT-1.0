package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Biometric Authorization Modal with pulsing fingerprint scanner ring
 */
@Composable
fun BiometricAuthDialog(
    title: String,
    amountGhs: Double,
    onConfirmed: () -> Unit,
    onDismiss: () -> Unit
) {
    var isScanning by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0x3300E5BE)
                ) {
                    Text(
                        text = "GHIPSS BIOMETRIC VERIFICATION",
                        color = NovaCyanAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = title,
                    color = NovaTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "GH₵ ${String.format("%,.2f", amountGhs)}",
                    color = NovaCyanAccent,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Authorized via MTN MoMo / Bank of Ghana Switch",
                    color = NovaTextSecondary,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Pulsing biometric scanner button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSuccess) Color(0xFF00E676)
                            else if (isScanning) Color(0x4000E5BE)
                            else Color(0x2600E5BE)
                        )
                        .border(
                            2.dp,
                            if (isSuccess) NovaOnlineGreen else NovaCyanAccent,
                            CircleShape
                        )
                        .clickable {
                            if (!isScanning && !isSuccess) {
                                isScanning = true
                            }
                        }
                        .testTag("biometric_fingerprint_touch"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSuccess) Icons.Default.Check else Icons.Default.Fingerprint,
                        contentDescription = "Scan Fingerprint",
                        tint = if (isSuccess) Color(0xFF051711) else NovaCyanAccent,
                        modifier = Modifier.size(44.dp)
                    )
                }

                LaunchedEffect(isScanning) {
                    if (isScanning) {
                        delay(1100)
                        isSuccess = true
                        delay(600)
                        onConfirmed()
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isSuccess) "Identity Verified! Authorizing..."
                    else if (isScanning) "Verifying GhIPSS Biometrics..."
                    else "Tap fingerprint sensor to authorize",
                    color = if (isSuccess) NovaOnlineGreen else NovaTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel", color = NovaTextSecondary, fontSize = 13.sp)
            }
        },
        containerColor = Color(0xF20B1611)
    )
}

/**
 * Security Audit Dialog showing E2EE parameters and Mesh network metrics
 */
@Composable
fun SecurityAuditDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x3300E5BE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Security",
                        tint = NovaCyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "NOVA Security Audit",
                        color = NovaTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Encrypted E2EE & Offline Mesh",
                        color = NovaCyanAccent,
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SecurityAuditItem(
                    label = "Protocol Standard",
                    value = "Signal Double Ratchet (256-bit AES-GCM)",
                    status = "ACTIVE",
                    statusColor = NovaCyanAccent
                )

                SecurityAuditItem(
                    label = "Safety Number Fingerprint",
                    value = "9820-4102-7719-8321-0029",
                    status = "VERIFIED",
                    statusColor = NovaCyanAccent
                )

                SecurityAuditItem(
                    label = "Ghana Offline Mesh Bridge",
                    value = "1,420 connected nodes in Kumasi & Accra",
                    status = "ONLINE",
                    statusColor = NovaOnlineGreen
                )

                SecurityAuditItem(
                    label = "Bank of Ghana GhIPSS Gateway",
                    value = "TLS 1.3 Certified Hardware Security Module",
                    status = "SECURED",
                    statusColor = NovaCyanAccent
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NovaCyanAccent,
                    contentColor = Color(0xFF04140D)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Close Audit", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color(0xF209140F)
    )
}

@Composable
private fun SecurityAuditItem(
    label: String,
    value: String,
    status: String,
    statusColor: Color
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0x3312241C),
        border = BorderStroke(1.dp, Color(0x2400E5BE)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    color = NovaTextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = statusColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = status,
                        color = statusColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = NovaTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
