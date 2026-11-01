package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.NovaChatUiState
import com.example.data.NovaChatViewModel
import com.example.model.GiftVaultItem
import com.example.model.LiveComment
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun LiveStreamScreen(
    viewModel: NovaChatViewModel,
    uiState: NovaChatUiState,
    modifier: Modifier = Modifier
) {
    val liveComments by viewModel.liveComments.collectAsState()
    val patronTicker by viewModel.patronTicker.collectAsState()
    var commentInput by remember { mutableStateOf("") }
    var showTopUpDialog by remember { mutableStateOf(false) }

    // Live countdown timer for the auction
    var secondsRemaining by remember { mutableStateOf(252) }
    LaunchedEffect(Unit) {
        while (secondsRemaining > 0) {
            kotlinx.coroutines.delay(1000)
            secondsRemaining--
        }
    }
    val minutes = secondsRemaining / 60
    val seconds = secondsRemaining % 60
    val dynamicTimeLeft = String.format("%02d:%02d", minutes, seconds)

    // Floating reaction emojis
    var floatingReactions by remember { mutableStateOf(listOf<Triple<Long, String, Float>>()) }
    val spawnReaction = {
        val emojis = listOf("🇬🇭", "✨", "💛", "🔥", "👑", "💎")
        val randomEmoji = emojis.random()
        val randomOffset = (0.2f + (0.6f * Math.random().toFloat()))
        floatingReactions = (floatingReactions + Triple(System.currentTimeMillis(), randomEmoji, randomOffset)).takeLast(8)
    }

    // Biometric Settlement Dialog state
    var showBiometricAuth by remember { mutableStateOf(false) }
    var biometricTitle by remember { mutableStateOf("") }
    var biometricAmount by remember { mutableStateOf(0.0) }
    var onBiometricConfirmed by remember { mutableStateOf<(() -> Unit)?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NovaDarkBackground)
    ) {
        // Background Image - The Authentic Live Stream Loom Studio
        Image(
            painter = painterResource(id = R.drawable.stream_kente_bg),
            contentDescription = "Live Stream Broadcast",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clickable { spawnReaction() }
        )

        // Subtle gradient overlay for reading comments and glass cards
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xCC070E0B),
                            Color(0x40070E0B),
                            Color(0x99070E0B),
                            Color(0xF2070E0B)
                        )
                    )
                )
        )

        // Floating Hearts & Emojis overlay
        floatingReactions.forEach { reaction ->
            key(reaction.first) {
                var animatedY by remember { mutableStateOf(0.9f) }
                var animatedAlpha by remember { mutableStateOf(1f) }

                LaunchedEffect(Unit) {
                    val startTime = System.currentTimeMillis()
                    while (System.currentTimeMillis() - startTime < 1800) {
                        val progress = (System.currentTimeMillis() - startTime) / 1800f
                        animatedY = 0.9f - (0.5f * progress)
                        animatedAlpha = (1f - progress).coerceAtLeast(0f)
                        kotlinx.coroutines.delay(16)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    Text(
                        text = reaction.second,
                        fontSize = 24.sp,
                        modifier = Modifier
                            .offset(
                                x = (reaction.third * 260).dp,
                                y = (animatedY * 600).dp
                            )
                    )
                }
            }
        }

        // Main Stream Overlay Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 1. Creator Header Bar
            StreamerHeader(
                likesCount = "2.1M",
                studioLocation = "Kumasi Studio",
                timeLive = "01:24:21",
                onFollowClick = {}
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2. LIVE AUCTION CARD (Lot #04 Ashanti Sovereign Kente)
            LiveAuctionCard(
                lotNumber = "LOT #04",
                title = "Ashanti Silk Sovereign Kente (Bafour...)",
                currentBidGhs = uiState.currentLotBidGhs,
                unitsLeft = uiState.currentLotUnits,
                timeLeft = dynamicTimeLeft,
                onBidClick = {
                    viewModel.placeBidOnLiveLot()
                    spawnReaction()
                },
                onBuyMoMoClick = {
                    biometricTitle = "Purchase Ashanti Sovereign Kente (Lot #04)"
                    biometricAmount = uiState.currentLotBidGhs
                    onBiometricConfirmed = {
                        viewModel.buyWithMoMoInstant()
                        showBiometricAuth = false
                        spawnReaction()
                    }
                    showBiometricAuth = true
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Patron Activity Banner (Kwesi_Accra VIP Patron)
            patronTicker?.let { ticker ->
                PatronActivityBanner(ticker = ticker)
            }

            Spacer(modifier = Modifier.weight(1f))

            // 4. E2EE Mesh Bridge Indicator
            MeshBridgePill(viewerCount = "1,420", location = "Kumasi")

            Spacer(modifier = Modifier.height(6.dp))

            // 5. Live Comments Stream
            LiveCommentsList(
                comments = liveComments,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 220.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 6. Session Live Earnings Pill
            SessionEarningsBar(
                earningsGhs = uiState.liveSessionEarningsGhs,
                onPayoutClick = { viewModel.setDestination(com.example.data.MainDestination.NOVAPAY) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 7. Interactive Bottom Controls (Comment, Emoji, Add, Mic, Heart, Gifts)
            StreamBottomInputBar(
                commentText = commentInput,
                onCommentChange = { commentInput = it },
                onSendComment = {
                    viewModel.sendLiveComment(commentInput)
                    commentInput = ""
                },
                onHeartClick = { spawnReaction() },
                onGiftsClick = { viewModel.toggleGiftVault(true) }
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Pan-African Gift Vault Drawer (Bottom Sheet)
        AnimatedVisibility(
            visible = uiState.isGiftVaultOpen,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            PanAfricanGiftVaultSheet(
                walletBalanceGhs = uiState.walletBalanceGhs,
                items = viewModel.giftVaultItems,
                selectedItem = uiState.selectedGiftItem ?: viewModel.giftVaultItems[1],
                onSelectItem = { viewModel.selectGiftItem(it) },
                onSendGift = {
                    val item = uiState.selectedGiftItem ?: viewModel.giftVaultItems[1]
                    biometricTitle = "Send ${item.name} Tribute"
                    biometricAmount = item.priceGhs
                    onBiometricConfirmed = {
                        viewModel.sendSelectedGift()
                        showBiometricAuth = false
                        spawnReaction()
                    }
                    showBiometricAuth = true
                },
                onTopUpClick = { showTopUpDialog = true },
                onClose = { viewModel.toggleGiftVault(false) }
            )
        }

        // Quick MoMo Top-Up Dialog
        if (showTopUpDialog) {
            TopUpMoMoDialog(
                onDismiss = { showTopUpDialog = false },
                onTopUp = { amount ->
                    viewModel.topUpWallet(amount)
                    showTopUpDialog = false
                }
            )
        }

        // Biometric Authorization Modal
        if (showBiometricAuth) {
            BiometricAuthDialog(
                title = biometricTitle,
                amountGhs = biometricAmount,
                onConfirmed = {
                    onBiometricConfirmed?.invoke()
                },
                onDismiss = { showBiometricAuth = false }
            )
        }
    }
}

@Composable
private fun StreamerHeader(
    likesCount: String,
    studioLocation: String,
    timeLive: String,
    onFollowClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Profile image with vibrant cyan border
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .border(2.dp, NovaCyanAccent, CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.artisan_ama),
                    contentDescription = "Ama Serwaa",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "@ama_serwaa",
                        color = NovaTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified Creator",
                        tint = NovaCyanAccent,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Text(
                    text = "$likesCount Likes • $studioLocation",
                    color = NovaTextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0x3300E5BE),
                border = BorderStroke(1.dp, NovaCyanAccent.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "STUDIO",
                    color = NovaCyanAccent,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            LiveBadge(timeFormatted = timeLive)
        }
    }
}

@Composable
private fun LiveAuctionCard(
    lotNumber: String,
    title: String,
    currentBidGhs: Double,
    unitsLeft: Int,
    timeLeft: String,
    onBidClick: () -> Unit,
    onBuyMoMoClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xCC0E1C15), // Translucent deep moss
        border = BorderStroke(1.dp, Color(0x4000E5BE))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Header: Lot badge, Flame, Units, Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0x4D00E5BE)
                    ) {
                        Text(
                            text = lotNumber,
                            color = NovaCyanAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE AUCTION",
                        color = NovaGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "🔥 $unitsLeft Units Left",
                        color = Color(0xFFFF9500),
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0x661A2B4C)
                ) {
                    Text(
                        text = "TIME LEFT $timeLeft",
                        color = Color(0xFF64B5F6),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Item Title
            Text(
                text = title,
                color = NovaTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Price Row
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "GH₵ ${String.format("%,.0f", currentBidGhs)}",
                    color = NovaCyanAccent,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                val usdVal = (currentBidGhs / 14.85).toInt()
                Text(
                    text = "($$usdVal USD)",
                    color = NovaTextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Bid +GH₵ 50 and Buy with MoMo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Bid Button
                OutlinedButton(
                    onClick = onBidClick,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0x66FFFFFF)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0x3314241D),
                        contentColor = NovaTextPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("button_bid_increment")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = "Bid",
                            modifier = Modifier.size(16.dp),
                            tint = NovaTextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "BID +GH₵ 50",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                // Buy With MoMo
                Button(
                    onClick = onBuyMoMoClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NovaCyanAccent,
                        contentColor = Color(0xFF06140E)
                    ),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(44.dp)
                        .testTag("button_buy_momo")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = "Instant",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF06140E)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "BUY WITH MOMO",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PatronActivityBanner(ticker: com.example.model.PatronActivity) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xBF0A1611),
        border = BorderStroke(1.dp, Color(0x2E00E5BE))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(NovaCyanAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Patron",
                        tint = Color(0xFF051710),
                        modifier = Modifier.size(15.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = ticker.patronName,
                            color = NovaTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0x3300E5BE)
                        ) {
                            Text(
                                text = ticker.tier,
                                color = NovaCyanAccent,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = ticker.actionText,
                        color = NovaTextSecondary,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xE6FFCC00)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF003366))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = ticker.paymentMethod,
                        color = Color(0xFF003366),
                        fontWeight = FontWeight.Black,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MeshBridgePill(viewerCount: String, location: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xCC091510),
        border = BorderStroke(1.dp, Color(0x3300E5BE))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Encrypted Mesh",
                tint = NovaCyanAccent,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "E2EE Mesh Bridge: $viewerCount viewers linked offline in $location",
                color = NovaTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun LiveCommentsList(
    comments: List<LiveComment>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        reverseLayout = true
    ) {
        items(comments.reversed(), key = { it.id }) { comment ->
            CommentBubble(comment = comment)
        }
    }
}

@Composable
private fun CommentBubble(comment: LiveComment) {
    val isQuestion = comment.isPinned
    val bgColor = if (isQuestion) Color(0xCC1A3052) else Color(0xCC0E1B15)
    val borderColor = if (isQuestion) Color(0x6664B5F6) else Color(0x2600E5BE)

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (comment.badge.isNotEmpty()) {
                    val badgeColor = when (comment.badge) {
                        "PRO" -> NovaCyanAccent
                        "ANGEL" -> NovaGold
                        "QUESTION" -> Color(0xFF64B5F6)
                        else -> NovaTextSecondary
                    }
                    Text(
                        text = comment.username,
                        color = NovaTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = badgeColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = comment.badge,
                            color = badgeColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                } else {
                    Text(
                        text = comment.username,
                        color = NovaCyanAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = comment.message,
                color = NovaTextPrimary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun SessionEarningsBar(
    earningsGhs: Double,
    onPayoutClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xCC0C1813),
        border = BorderStroke(1.dp, Color(0x2900E5BE))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color(0x3300E5BE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Earnings",
                        tint = NovaCyanAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Session Live Earnings",
                        color = NovaTextSecondary,
                        fontSize = 10.sp
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "GH₵ ${String.format("%,.2f", earningsGhs)}",
                            color = NovaTextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        val usd = (earningsGhs / 14.85).toInt()
                        Text(
                            text = "($$usd USD)",
                            color = NovaTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Button(
                onClick = onPayoutClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0x401E392F),
                    contentColor = NovaTextPrimary
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier
                    .height(32.dp)
                    .testTag("button_payout")
            ) {
                Text(
                    text = "PAYOUT →",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun StreamBottomInputBar(
    commentText: String,
    onCommentChange: (String) -> Unit,
    onSendComment: () -> Unit,
    onHeartClick: () -> Unit,
    onGiftsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Comment Input Box
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(44.dp),
            shape = RoundedCornerShape(22.dp),
            color = Color(0xCC0B1712),
            border = BorderStroke(1.dp, Color(0x33FFFFFF))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = "Chat",
                    tint = NovaTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                androidx.compose.foundation.text.BasicTextField(
                    value = commentText,
                    onValueChange = onCommentChange,
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = NovaTextPrimary,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_live_comment"),
                    decorationBox = { innerTextField ->
                        if (commentText.isEmpty()) {
                            Text(
                                text = "Say something to broadcast...",
                                color = NovaTextMuted,
                                fontSize = 12.sp
                            )
                        }
                        innerTextField()
                    }
                )
                if (commentText.isNotEmpty()) {
                    IconButton(
                        onClick = onSendComment,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = NovaCyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Action Icons: Add, Mic, Heart Reaction, Gifts Button
        IconButton(
            onClick = {},
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0x990E1B15))
                .border(1.dp, Color(0x33FFFFFF), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.PersonAddAlt,
                contentDescription = "Invite Friend",
                tint = NovaTextPrimary,
                modifier = Modifier.size(18.dp)
            )
        }

        IconButton(
            onClick = {},
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0x990E1B15))
                .border(1.dp, Color(0x33FFFFFF), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Voice",
                tint = NovaTextPrimary,
                modifier = Modifier.size(18.dp)
            )
        }

        // Floating Heart Reaction Trigger
        IconButton(
            onClick = onHeartClick,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0x990E1B15))
                .border(1.dp, Color(0x3300E5BE), CircleShape)
                .testTag("stream_heart_reaction_btn")
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Send Heart",
                tint = NovaLiveRed,
                modifier = Modifier.size(20.dp)
            )
        }

        // Cyan GIFTS Button
        Button(
            onClick = onGiftsClick,
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = NovaCyanAccent,
                contentColor = Color(0xFF04140D)
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            modifier = Modifier
                .height(44.dp)
                .testTag("button_open_gifts")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CardGiftcard,
                    contentDescription = "Gift",
                    tint = Color(0xFF04140D),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "GIFTS",
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * Pan-African Gift Vault Drawer
 * Matching the screenshot with Cocoa Pod, Gye Nyame, Gold Stool, Crystal Star, Crown!
 */
@Composable
fun PanAfricanGiftVaultSheet(
    walletBalanceGhs: Double,
    items: List<GiftVaultItem>,
    selectedItem: GiftVaultItem,
    onSelectItem: (GiftVaultItem) -> Unit,
    onSendGift: () -> Unit,
    onTopUpClick: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        color = Color(0xF209140F),
        border = BorderStroke(1.dp, Color(0x3300E5BE))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header: Title + Balance + Top Up + Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(NovaCyanAccent)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pan-African Gift Vault",
                        color = NovaTextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x40162B21),
                        border = BorderStroke(1.dp, Color(0x3300E5BE))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "Balance",
                                tint = NovaCyanAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "GH₵ ${String.format("%,.2f", walletBalanceGhs)}",
                                color = NovaTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "+TOP UP",
                                color = NovaCyanAccent,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp,
                                modifier = Modifier
                                    .clickable(onClick = onTopUpClick)
                                    .testTag("vault_top_up")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = NovaTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5 Gift Vault Cards in a Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { item ->
                    val isSelected = item.id == selectedItem.id
                    GiftItemCard(
                        item = item,
                        isSelected = isSelected,
                        onClick = { onSelectItem(item) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer: Settlement notice + Send Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "Biometric Settlement",
                        tint = NovaCyanAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Instant MoMo Biometric Settlement",
                        color = NovaTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = onSendGift,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NovaCyanAccent,
                        contentColor = Color(0xFF04140D)
                    ),
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("button_send_selected_gift")
                ) {
                    Text(
                        text = "SEND SELECTED GIFT",
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun GiftItemCard(
    item: GiftVaultItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) NovaCyanAccent else Color(0x2E00E5BE)
    val bgColor = if (isSelected) Color(0x4D00E5BE) else Color(0x400F1F17)

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("gift_${item.id}"),
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon
            val iconVector = when (item.iconName) {
                "Cocoa" -> Icons.Default.Spa
                "GyeNyame" -> Icons.Default.Fingerprint
                "GoldStool" -> Icons.Default.Weekend
                "Crystal" -> Icons.Default.Diamond
                "Crown" -> Icons.Default.MilitaryTech
                else -> Icons.Default.CardGiftcard
            }

            Icon(
                imageVector = iconVector,
                contentDescription = item.name,
                tint = if (isSelected) NovaCyanAccent else NovaTextPrimary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.name,
                color = NovaTextPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "GH₵ ${item.priceGhs.toInt()}",
                color = NovaCyanAccent,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp
            )

            Text(
                text = if (item.priceUsd < 1.0) "${(item.priceUsd * 100).toInt()}¢" else "$${item.priceUsd.toInt()}",
                color = NovaTextMuted,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
fun TopUpMoMoDialog(
    onDismiss: () -> Unit,
    onTopUp: (Double) -> Unit
) {
    var topUpAmount by remember { mutableStateOf("200") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Top-Up via MTN Mobile Money",
                color = NovaTextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Deposit directly into your NOVA PAY vault from your registered MTN MoMo number.",
                    color = NovaTextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = topUpAmount,
                    onValueChange = { topUpAmount = it },
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = topUpAmount.toDoubleOrNull() ?: 200.0
                    onTopUp(amt)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NovaCyanAccent,
                    contentColor = Color(0xFF04140D)
                )
            ) {
                Text("Confirm Deposit", fontWeight = FontWeight.Bold)
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
