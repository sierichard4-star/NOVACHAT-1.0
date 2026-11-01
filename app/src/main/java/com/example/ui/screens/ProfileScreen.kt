package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NovaChatViewModel
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    viewModel: NovaChatViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var biometricEnabled by remember { mutableStateOf(true) }
    var meshEnabled by remember { mutableStateOf(true) }
    var selectedLanguage by remember { mutableStateOf("English (Ghana)") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NovaDarkBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp)
        ) {
            // 1. User Profile Hero Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xCC0D2118),
                    border = BorderStroke(1.dp, Color(0x3300E5BE))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x3300E5BE))
                                        .border(2.dp, NovaCyanAccent, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Profile",
                                        tint = NovaTextPrimary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Kwesi Mensah",
                                            color = NovaTextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = "Verified Identity",
                                            tint = NovaCyanAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = "@kwesi_m • Accra, Ghana",
                                        color = NovaTextSecondary,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "NOVA ID: 8820-9182-GH",
                                        color = NovaTextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            // Personal QR Code Button
                            IconButton(
                                onClick = { viewModel.openQrScanner() },
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x3300E5BE))
                                    .testTag("profile_qr_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCode,
                                    contentDescription = "My QR Code",
                                    tint = NovaCyanAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. NOVA PAY Wallet Summary Shortcut
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { viewModel.setDestination(com.example.data.MainDestination.NOVAPAY) },
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xCC0E1A14),
                    border = BorderStroke(1.dp, Color(0x2600E5BE))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x3300E5BE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = "Wallet",
                                    tint = NovaCyanAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "NOVA PAY Digital Vault",
                                    color = NovaTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "GH₵ ${String.format("%,.2f", uiState.walletBalanceGhs)} available",
                                    color = NovaCyanAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Go to wallet",
                            tint = NovaTextSecondary
                        )
                    }
                }
            }

            // 3. Security & End-to-End Encryption Hub
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xCC0E1A14),
                    border = BorderStroke(1.dp, Color(0x2E00E5BE))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Security",
                                tint = NovaCyanAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Security & Encryption Hub",
                                color = NovaTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        SettingToggleRow(
                            title = "Biometric MoMo Authorization",
                            subtitle = "Require fingerprint / face scan for all transactions",
                            checked = biometricEnabled,
                            onCheckedChange = { biometricEnabled = it }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        SettingToggleRow(
                            title = "Offline E2EE Mesh Bridge",
                            subtitle = "Route encrypted messages over local mesh during network dips",
                            checked = meshEnabled,
                            onCheckedChange = { meshEnabled = it }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        SettingActionRow(
                            title = "Signal Protocol 256-bit Keys",
                            value = "Verified & Active",
                            onClick = {}
                        )

                        SettingActionRow(
                            title = "Connected Devices",
                            value = "2 Devices Linked",
                            onClick = {}
                        )
                    }
                }
            }

            // 4. Preferences & Localization
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xCC0E1A14),
                    border = BorderStroke(1.dp, Color(0x2E00E5BE))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Preferences & Regional Settings",
                            color = NovaTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SettingActionRow(
                            title = "Primary Language",
                            value = selectedLanguage,
                            onClick = {}
                        )

                        SettingActionRow(
                            title = "Local Currency Baseline",
                            value = "Ghanaian Cedi (GH₵)",
                            onClick = {}
                        )

                        SettingActionRow(
                            title = "Genesis Soil Design Theme",
                            value = "Dark Moss Luxury",
                            onClick = {}
                        )
                    }
                }
            }

            // 5. Help & About NOVACHAT
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xCC0E1A14),
                    border = BorderStroke(1.dp, Color(0x2E00E5BE))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "About NOVACHAT",
                            color = NovaTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "NOVACHAT v1.0 • Built for Ghana, West Africa & the Global African Diaspora. Powered by Genesis Soil Design Architecture, Bank of Ghana GhIPSS Interoperability, and End-to-End Encryption.",
                            color = NovaTextMuted,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = NovaTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = NovaTextMuted,
                fontSize = 10.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF04140D),
                checkedTrackColor = NovaCyanAccent,
                uncheckedThumbColor = NovaTextMuted,
                uncheckedTrackColor = Color(0x33FFFFFF)
            )
        )
    }
}

@Composable
private fun SettingActionRow(
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = NovaTextPrimary,
            fontSize = 13.sp
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                color = NovaCyanAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = NovaTextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
