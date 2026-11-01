package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.example.model.MiniApp
import com.example.model.MomentPost
import com.example.ui.components.CyanActionButton
import com.example.ui.theme.*

@Composable
fun DiscoverScreen(
    viewModel: NovaChatViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Moments", "Mini-Apps", "Channels")
    val moments by viewModel.moments.collectAsState()
    var showCreateMomentDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NovaDarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Sub Tab Bar (Moments, Mini-Apps, Channels)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(22.dp),
                color = Color(0xCC0D1B15),
                border = BorderStroke(1.dp, Color(0x2600E5BE))
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) NovaCyanAccent else Color.Transparent)
                                .clickable { selectedTab = index }
                                .testTag("discover_tab_$index"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                color = if (isSelected) Color(0xFF04140D) else NovaTextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                0 -> {
                    // MOMENTS FEED
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        // Quick post banner
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { showCreateMomentDialog = true }
                                    .testTag("create_moment_banner"),
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xCC0E1A15),
                                border = BorderStroke(1.dp, Color(0x3300E5BE))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddCircleOutline,
                                        contentDescription = "Post Moment",
                                        tint = NovaCyanAccent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Share what's happening in your circle...",
                                        color = NovaTextSecondary,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        items(moments, key = { it.id }) { post ->
                            MomentPostCard(
                                post = post,
                                onLike = { viewModel.toggleLikeMoment(post.id) }
                            )
                        }
                    }
                }

                1 -> {
                    // MINI-APPS ECOSYSTEM (WeChat Mini Programs)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0x3300E5BE)
                            ) {
                                Text(
                                    text = "⚡ Instant Lightweight Mini-Programs. Run directly inside NOVACHAT with zero storage footprint.",
                                    color = NovaCyanAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        // Ride Hailing Mini Program Banner
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable { viewModel.openRideHailing() }
                                    .testTag("launch_nova_ride_app"),
                                shape = RoundedCornerShape(18.dp),
                                color = Color(0xCC0D2118),
                                border = BorderStroke(1.5.dp, NovaCyanAccent)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(CircleShape)
                                                .background(NovaCyanAccent),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DirectionsCar,
                                                contentDescription = "NOVA RIDE",
                                                tint = Color(0xFF04140D),
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "NOVA RIDE",
                                                color = NovaTextPrimary,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 15.sp
                                            )
                                            Text(
                                                text = "On-demand rides in Accra & Kumasi • Live tracking",
                                                color = NovaTextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                        contentDescription = "Open",
                                        tint = NovaCyanAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Mini apps list
                        items(viewModel.miniApps, key = { it.id }) { app ->
                            MiniAppCard(
                                app = app,
                                onOpen = {
                                    if (app.id == "m2") {
                                        viewModel.setDestination(com.example.data.MainDestination.NOVAPAY)
                                    } else {
                                        viewModel.openMarketplace()
                                    }
                                }
                            )
                        }
                    }
                }

                2 -> {
                    // CHANNELS & SHORT VIDEOS
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xCC0E1A14),
                                border = BorderStroke(1.dp, Color(0x3300E5BE))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Trending African Channels",
                                            color = NovaTextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = "120k Live",
                                            color = NovaCyanAccent,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Creator Channels row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        ChannelAvatar(name = "Ama Loom", handle = "@ama_serwaa", isLive = true)
                                        ChannelAvatar(name = "Kofi Code", handle = "@kofi_dev", isLive = false)
                                        ChannelAvatar(name = "Afua Style", handle = "@afua_v", isLive = false)
                                        ChannelAvatar(name = "Accra Beat", handle = "@accra_vibes", isLive = true)
                                    }
                                }
                            }
                        }

                        // Featured video card
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp)),
                                shape = RoundedCornerShape(18.dp),
                                color = Color(0xCC0E1F17),
                                border = BorderStroke(1.dp, Color(0x3300E5BE))
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.stream_kente_bg),
                                            contentDescription = "Featured Video",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .size(50.dp)
                                                .clip(CircleShape)
                                                .background(Color(0x99000000))
                                                .border(2.dp, NovaCyanAccent, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Play",
                                                tint = NovaCyanAccent,
                                                modifier = Modifier.size(30.dp)
                                            )
                                        }
                                    }

                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "Bonwire Master Weavers: Preserving the Sacred Golden Thread",
                                            color = NovaTextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "24.5k views • 4.2k likes • Ashanti Cultural Heritage Series",
                                            color = NovaTextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Create Moment Post Dialog
        if (showCreateMomentDialog) {
            CreateMomentDialog(
                onDismiss = { showCreateMomentDialog = false },
                onPost = { content, location ->
                    viewModel.postMoment(content, location)
                    showCreateMomentDialog = false
                }
            )
        }
    }
}

@Composable
private fun MomentPostCard(
    post: MomentPost,
    onLike: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xCC0D1B15),
        border = BorderStroke(1.dp, Color(0x2600E5BE))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Author row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(1.dp, NovaCyanAccent, CircleShape)
                    ) {
                        if (post.authorAvatar == "artisan_ama") {
                            Image(
                                painter = painterResource(id = R.drawable.artisan_ama),
                                contentDescription = post.authorName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = post.authorName,
                                tint = NovaCyanAccent,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = post.authorName,
                            color = NovaTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${post.authorHandle} • ${post.timestamp}",
                            color = NovaTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                if (post.location.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = NovaCyanAccent,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = post.location.split(",").firstOrNull() ?: "",
                            color = NovaCyanAccent,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Content
            Text(
                text = post.content,
                color = NovaTextPrimary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            // Tags
            if (post.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    post.tags.forEach { tag ->
                        Text(
                            text = tag,
                            color = NovaCyanAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action row (Like, Comment, Share)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onLike,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (post.isLiked) NovaLiveRed else NovaTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = post.likesCount.toString(),
                        color = NovaTextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Comments",
                        tint = NovaTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = post.commentsCount.toString(),
                        color = NovaTextSecondary,
                        fontSize = 12.sp
                    )
                }

                IconButton(
                    onClick = {},
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = NovaTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniAppCard(
    app: MiniApp,
    onOpen: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onOpen)
            .testTag("mini_app_${app.id}"),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xCC0E1914),
        border = BorderStroke(1.dp, Color(0x2600E5BE))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x3300E5BE)),
                    contentAlignment = Alignment.Center
                ) {
                    val iconVector = when (app.iconSymbol) {
                        "Restaurant" -> Icons.Default.Restaurant
                        "Bolt" -> Icons.Default.Bolt
                        "LocationOn" -> Icons.Default.LocationOn
                        "ConfirmationNumber" -> Icons.Default.ConfirmationNumber
                        "WaterDrop" -> Icons.Default.WaterDrop
                        "School" -> Icons.Default.School
                        else -> Icons.Default.Apps
                    }
                    Icon(
                        imageVector = iconVector,
                        contentDescription = app.name,
                        tint = NovaCyanAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = app.name,
                            color = NovaTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0x26FFFFFF)
                        ) {
                            Text(
                                text = app.category,
                                color = NovaTextSecondary,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = app.description,
                        color = NovaTextMuted,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            Button(
                onClick = onOpen,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0x3300E5BE),
                    contentColor = NovaCyanAccent
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text("Open", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun ChannelAvatar(name: String, handle: String, isLive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Color(0x3300E5BE))
                .border(2.dp, if (isLive) NovaLiveRed else NovaCyanAccent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = name,
                tint = NovaTextPrimary,
                modifier = Modifier.size(26.dp)
            )
            if (isLive) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = NovaLiveRed,
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Text(
                        text = "LIVE",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 8.sp,
                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = name, color = NovaTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CreateMomentDialog(
    onDismiss: () -> Unit,
    onPost: (String, String) -> Unit
) {
    var contentText by remember { mutableStateOf("") }
    var locationText by remember { mutableStateOf("Accra, Ghana") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Post to Moments",
                color = NovaTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = contentText,
                    onValueChange = { contentText = it },
                    label = { Text("What's on your mind?") },
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = NovaTextPrimary,
                        unfocusedTextColor = NovaTextPrimary,
                        focusedBorderColor = NovaCyanAccent,
                        unfocusedBorderColor = Color(0x33FFFFFF)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = locationText,
                    onValueChange = { locationText = it },
                    label = { Text("Location tag") },
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
                onClick = { onPost(contentText, locationText) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NovaCyanAccent,
                    contentColor = Color(0xFF04140D)
                )
            ) {
                Text("Publish Moment", fontWeight = FontWeight.Bold)
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
