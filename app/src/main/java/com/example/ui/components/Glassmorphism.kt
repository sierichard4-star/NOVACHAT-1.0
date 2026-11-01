package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Genesis Soil inspired Frosted Glass Card
 * Combines translucent deep forest tones with delicate glowing borders.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = NovaCardGlass,
    borderColor: Color = NovaGlassBorder,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else Modifier

    Surface(
        modifier = modifier
            .clip(shape)
            .then(clickableModifier),
        shape = shape,
        color = backgroundColor,
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun GlassCardDense(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = Color(0xCC0E1A14),
    borderColor: Color = Color(0x2E00E5BE),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderColor, shape),
        content = content
    )
}

/**
 * Electric Cyan Action Button (Genesis Soil Highlight)
 */
@Composable
fun CyanActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    testTag: String = "cyan_action_button"
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = NovaCyanAccent,
            contentColor = Color(0xFF07120D),
            disabledContainerColor = NovaCyanAccent.copy(alpha = 0.4f),
            disabledContentColor = Color(0xFF07120D).copy(alpha = 0.6f)
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * Secondary Glass Button with glowing border
 */
@Composable
fun GlassSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    testTag: String = "glass_secondary_button"
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0x4D00E5BE)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color(0x6612241C),
            contentColor = NovaTextPrimary
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}

/**
 * Pulsing Live Stream Badge
 */
@Composable
fun LiveBadge(
    timeFormatted: String = "01:24:21",
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color(0xB3150808),
        border = BorderStroke(1.dp, NovaLiveRed.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(NovaLiveRed.copy(alpha = alpha))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "LIVE",
                color = NovaLiveRed,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "•",
                color = NovaTextMuted,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = timeFormatted,
                color = NovaTextPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp
            )
        }
    }
}

/**
 * End-to-End Encryption Badge
 */
@Composable
fun E2eeIndicator(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0x6600E5BE).copy(alpha = 0.12f),
        border = BorderStroke(1.dp, NovaCyanAccent.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Encrypted",
                tint = NovaCyanAccent,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "E2EE Verified",
                color = NovaCyanAccent,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
