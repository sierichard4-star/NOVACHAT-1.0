package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.model.PaymentProvider
import com.example.model.WalletTransaction
import com.example.ui.components.CyanActionButton
import com.example.ui.components.GlassCard
import com.example.ui.theme.*

@Composable
fun NovaPayScreen(
    viewModel: NovaChatViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    var showSendMoneyDialog by remember { mutableStateOf(false) }
    var showQrReceiveDialog by remember { mutableStateOf(false) }
    var showTopUpDialog by remember { mutableStateOf(false) }

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
            // 1. Wallet Balance Hero Card (Genesis Soil Translucent Emerald)
            item {
                WalletBalanceHero(
                    balanceGhs = uiState.walletBalanceGhs,
                    onTopUpClick = { showTopUpDialog = true },
                    onReceiveClick = { showQrReceiveDialog = true },
                    onSendClick = { showSendMoneyDialog = true }
                )
            }

            // 2. Primary Financial Operations Grid (Scan, Pay Code, Send, Bank)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PayActionTile(
                        title = "Scan QR",
                        subtitle = "Merchant Pay",
                        icon = Icons.Default.QrCodeScanner,
                        bgColor = Color(0x3300E5BE),
                        iconTint = NovaCyanAccent,
                        onClick = { viewModel.openQrScanner() },
                        modifier = Modifier.weight(1f),
                        testTag = "pay_action_scan"
                    )
                    PayActionTile(
                        title = "Receive",
                        subtitle = "Personal QR",
                        icon = Icons.Default.QrCode,
                        bgColor = Color(0x33E5B869),
                        iconTint = NovaGold,
                        onClick = { showQrReceiveDialog = true },
                        modifier = Modifier.weight(1f),
                        testTag = "pay_action_receive"
                    )
                    PayActionTile(
                        title = "Transfer",
                        subtitle = "MoMo & Bank",
                        icon = Icons.AutoMirrored.Filled.Send,
                        bgColor = Color(0x332EC486),
                        iconTint = NovaEmerald,
                        onClick = { showSendMoneyDialog = true },
                        modifier = Modifier.weight(1f),
                        testTag = "pay_action_transfer"
                    )
                }
            }

            // 3. Supported African Payment Providers (MTN MoMo, Telecel, AT, Ecobank, GCB)
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xCC0E1A14),
                    border = BorderStroke(1.dp, Color(0x2E00E5BE))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Interoperable Networks",
                                color = NovaTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "GhIPSS Linked",
                                color = NovaCyanAccent,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ProviderBadge(name = "MTN MoMo", color = Color(0xFFFFCC00), textColor = Color(0xFF003366))
                            ProviderBadge(name = "Telecel", color = Color(0xFFE60000), textColor = Color.White)
                            ProviderBadge(name = "AT Money", color = Color(0xFF006699), textColor = Color.White)
                            ProviderBadge(name = "Ecobank", color = Color(0xFF005A9C), textColor = Color.White)
                            ProviderBadge(name = "GCB Bank", color = Color(0xFF008000), textColor = Color.White)
                        }
                    }
                }
            }

            // 4. Everyday Utilities & Bills
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xCC0E1A14),
                    border = BorderStroke(1.dp, Color(0x2E00E5BE))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Everyday Utilities & Bills",
                            color = NovaTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            UtilityIcon(name = "ECG Power", icon = Icons.Default.Bolt, tint = NovaGold)
                            UtilityIcon(name = "GWCL Water", icon = Icons.Default.WaterDrop, tint = Color(0xFF64B5F6))
                            UtilityIcon(name = "Airtime & Data", icon = Icons.Default.PhoneIphone, tint = NovaCyanAccent)
                            UtilityIcon(name = "DSTV / Cable", icon = Icons.Default.Tv, tint = Color(0xFFCE93D8))
                        }
                    }
                }
            }

            // 5. Recent Transaction History Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Recent Transactions",
                        color = NovaTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Export Statement",
                        color = NovaCyanAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Transaction items
            items(transactions, key = { it.id }) { tx ->
                TransactionRow(tx = tx)
            }
        }

        // Send Money Modal
        if (showSendMoneyDialog) {
            SendMoneyDialog(
                onDismiss = { showSendMoneyDialog = false },
                onSend = { phone, amount, provider ->
                    viewModel.sendMoneyWallet(phone, amount, provider)
                    showSendMoneyDialog = false
                }
            )
        }

        // Receive QR Modal
        if (showQrReceiveDialog) {
            ReceiveQrDialog(
                walletBalanceGhs = uiState.walletBalanceGhs,
                onDismiss = { showQrReceiveDialog = false }
            )
        }

        // Top Up Modal
        if (showTopUpDialog) {
            TopUpMoMoDialog(
                onDismiss = { showTopUpDialog = false },
                onTopUp = { amt ->
                    viewModel.topUpWallet(amt)
                    showTopUpDialog = false
                }
            )
        }
    }
}

@Composable
private fun WalletBalanceHero(
    balanceGhs: Double,
    onTopUpClick: () -> Unit,
    onReceiveClick: () -> Unit,
    onSendClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color(0xE60E241B),
        border = BorderStroke(1.5.dp, Color(0x4D00E5BE))
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0x3300E5BE),
                            Color(0x00000000),
                            Color(0x26E5B869)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(NovaCyanAccent)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "NOVA PAY DIGITAL VAULT",
                            color = NovaCyanAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x3300E5BE)
                    ) {
                        Text(
                            text = "GH₵ Cedi Account",
                            color = NovaTextPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Total Available Balance",
                    color = NovaTextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "GH₵ ${String.format("%,.2f", balanceGhs)}",
                        color = NovaTextPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val usdVal = (balanceGhs / 14.85).toInt()
                    Text(
                        text = "≈ $$usdVal USD",
                        color = NovaTextSecondary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: +Top Up, Send, Receive
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onTopUpClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NovaCyanAccent,
                            contentColor = Color(0xFF04140D)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("wallet_top_up_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = "Top Up", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Top Up", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = onSendClick,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0x66FFFFFF)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0x26142720),
                            contentColor = NovaTextPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("wallet_send_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = "Send", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Send", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = onReceiveClick,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0x66FFFFFF)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0x26142720),
                            contentColor = NovaTextPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("wallet_receive_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = "Receive", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Receive", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PayActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    bgColor: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xCC0E1A15),
        border = BorderStroke(1.dp, Color(0x2600E5BE))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                color = NovaTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = subtitle,
                color = NovaTextMuted,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun ProviderBadge(name: String, color: Color, textColor: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color
    ) {
        Text(
            text = name,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun UtilityIcon(name: String, icon: ImageVector, tint: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0x33172E23)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = name, tint = tint, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = name, color = NovaTextSecondary, fontSize = 10.sp)
    }
}

@Composable
private fun TransactionRow(tx: WalletTransaction) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xCC0E1914),
        border = BorderStroke(1.dp, Color(0x1FFFFFFF))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (tx.isCredit) Color(0x3300E676) else Color(0x33FF5252)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (tx.isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = if (tx.isCredit) NovaOnlineGreen else Color(0xFFFF5252),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = tx.title,
                        color = NovaTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "${tx.date} • ${tx.referenceCode}",
                        color = NovaTextMuted,
                        fontSize = 10.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                val prefix = if (tx.isCredit) "+" else "-"
                val textColor = if (tx.isCredit) NovaOnlineGreen else NovaTextPrimary
                Text(
                    text = "$prefix GH₵ ${String.format("%,.2f", tx.amountGhs)}",
                    color = textColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
                Text(
                    text = tx.status,
                    color = NovaCyanAccent,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun SendMoneyDialog(
    onDismiss: () -> Unit,
    onSend: (String, Double, PaymentProvider) -> Unit
) {
    var recipientPhone by remember { mutableStateOf("0244889120") }
    var amountText by remember { mutableStateOf("100") }
    var selectedProvider by remember { mutableStateOf(PaymentProvider.MTN_MOMO) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Send Money (Ghana & West Africa)",
                color = NovaTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Transfer Cedis directly to any mobile money wallet or verified bank account.",
                    color = NovaTextSecondary,
                    fontSize = 12.sp
                )

                OutlinedTextField(
                    value = recipientPhone,
                    onValueChange = { recipientPhone = it },
                    label = { Text("Recipient Phone / Account") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = NovaTextPrimary,
                        unfocusedTextColor = NovaTextPrimary,
                        focusedBorderColor = NovaCyanAccent,
                        unfocusedBorderColor = Color(0x33FFFFFF)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (GH₵)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = NovaTextPrimary,
                        unfocusedTextColor = NovaTextPrimary,
                        focusedBorderColor = NovaCyanAccent,
                        unfocusedBorderColor = Color(0x33FFFFFF)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Provider: MTN Mobile Money (Verified)",
                    color = NovaGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 100.0
                    onSend(recipientPhone, amt, selectedProvider)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NovaCyanAccent,
                    contentColor = Color(0xFF04140D)
                )
            ) {
                Text("Authorize & Send", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = NovaTextSecondary)
            }
        },
        containerColor = Color(0xFF0D1B15)
    )
}

@Composable
private fun ReceiveQrDialog(
    walletBalanceGhs: Double,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Your NOVA PAY QR Code",
                color = NovaTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Show this QR code to receive payments from any Ghanaian bank or MoMo app.",
                    color = NovaTextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Simulated QR Graphic in a stylish glass frame
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    modifier = Modifier.size(180.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "QR Code",
                            tint = Color(0xFF051711),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "ID: NOVA-GH-ACC-992019",
                    color = NovaCyanAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NovaCyanAccent,
                    contentColor = Color(0xFF04140D)
                )
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color(0xFF0D1B15)
    )
}
