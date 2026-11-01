package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.MainDestination
import com.example.ui.theme.*

@Composable
fun NovaTopBar(
    onSecurityClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAiClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = Color(0xCC09120E),
        border = null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Logo + Branding + Status
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Glowing circular logo container
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFF00E5BE).copy(alpha = 0.4f), Color(0xFF0B1713))
                            )
                        )
                        .border(1.dp, NovaCyanAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_novachat_logo),
                        contentDescription = "NOVACHAT Logo",
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "NOVACHAT",
                            color = NovaTextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // Country badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x33FFFFFF)
                        ) {
                            Text(
                                text = "🇬🇭 GH / Accra",
                                color = NovaTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(NovaOnlineGreen)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Online • Encrypted E2EE",
                            color = NovaCyanAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Right Action Icons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // AI Sparkle Assistant Button
                IconButton(
                    onClick = onAiClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0x3300E5BE))
                        .border(1.dp, NovaCyanAccent.copy(alpha = 0.4f), CircleShape)
                        .testTag("top_ai_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "NOVA AI Assistant",
                        tint = NovaCyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Security Verified Shield
                IconButton(
                    onClick = onSecurityClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0x2E1E382D))
                        .border(1.dp, NovaGlassBorder, CircleShape)
                        .testTag("top_security_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Security Status",
                        tint = NovaCyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // User Profile Button
                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0x2E1E382D))
                        .border(1.dp, NovaGold.copy(alpha = 0.6f), CircleShape)
                        .testTag("top_profile_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile & Settings",
                        tint = NovaTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Genesis Soil Glassmorphic Bottom Navigation
 * Features raised glowing center NOVA PAY action button.
 */
@Composable
fun NovaBottomNavigation(
    currentDestination: MainDestination,
    onDestinationSelected: (MainDestination) -> Unit,
    unreadChatsCount: Int = 2,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = Color(0xF209130F), // Glassmorphic deep dark moss
        border = BorderStroke(1.dp, Color(0x2400E5BE))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // 1. Home (Ecosystem & Live Streams)
            BottomNavItem(
                label = "Home",
                icon = Icons.Default.Hub,
                selected = currentDestination == MainDestination.HOME,
                onClick = { onDestinationSelected(MainDestination.HOME) },
                testTag = "nav_home"
            )

            // 2. Chats (Messaging)
            BottomNavItem(
                label = "Chats",
                icon = Icons.Default.ChatBubbleOutline,
                selected = currentDestination == MainDestination.CHATS,
                badgeCount = unreadChatsCount,
                onClick = { onDestinationSelected(MainDestination.CHATS) },
                testTag = "nav_chats"
            )

            // 3. NOVA PAY (Elevated Cyan Button)
            Box(
                modifier = Modifier
                    .offset(y = (-10).dp)
                    .size(58.dp)
                    .shadow(12.dp, CircleShape, spotColor = NovaCyanAccent)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF2EFFF3), Color(0xFF00D2B4), Color(0xFF00A38C))
                        )
                    )
                    .border(2.dp, Color(0xFFE0FFF8), CircleShape)
                    .clickable { onDestinationSelected(MainDestination.NOVAPAY) }
                    .testTag("nav_novapay_hub"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "NOVA PAY",
                        tint = Color(0xFF051711),
                        modifier = Modifier.size(26.dp)
                    )
                    Text(
                        text = "PAY",
                        color = Color(0xFF051711),
                        fontWeight = FontWeight.Black,
                        fontSize = 9.sp
                    )
                }
            }

            // 4. Discover (Moments & Social)
            BottomNavItem(
                label = "Discover",
                icon = Icons.Default.Explore,
                selected = currentDestination == MainDestination.DISCOVER,
                onClick = { onDestinationSelected(MainDestination.DISCOVER) },
                testTag = "nav_discover"
            )

            // 5. Security / Me (Profile & Privacy)
            BottomNavItem(
                label = "Security",
                icon = Icons.Default.Security,
                selected = currentDestination == MainDestination.ME,
                onClick = { onDestinationSelected(MainDestination.ME) },
                testTag = "nav_me"
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0,
    testTag: String = ""
) {
    val tint by animateColorAsState(
        targetValue = if (selected) NovaCyanAccent else NovaTextMuted,
        label = "tint"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .offset(x = 6.dp, y = (-4).dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(NovaLiveRed),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = label,
            color = tint,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
