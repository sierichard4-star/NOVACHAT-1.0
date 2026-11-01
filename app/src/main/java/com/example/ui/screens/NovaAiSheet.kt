package com.example.ui.screens

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
import com.example.ui.theme.*

data class AiMessage(
    val id: String,
    val sender: String,
    val text: String,
    val isUser: Boolean
)

@Composable
fun NovaAiSheet(
    viewModel: NovaChatViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var queryText by remember { mutableStateOf("") }
    var chatHistory by remember {
        mutableStateOf(
            listOf(
                AiMessage(
                    id = "welcome",
                    sender = "NOVA AI",
                    text = "Akwaaba! I am your NOVA AI assistant, specialized in West African trade, logistics, GhIPSS payments, and multilingual translations (English, Twi, Ga, Yoruba, French). How can I assist you?",
                    isUser = false
                )
            )
        )
    }

    val promptChips = listOf(
        "Translate English to Twi",
        "Ashanti Kente Symbolism",
        "MoMo GhIPSS Settlement",
        "Draft Trade Message"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        color = Color(0xF209140F),
        border = BorderStroke(1.dp, Color(0x3300E5BE))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0x3300E5BE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "NOVA AI",
                            tint = NovaCyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "NOVA AI Assistant",
                                color = NovaTextPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0x3300E5BE)
                            ) {
                                Text(
                                    text = "FAST",
                                    color = NovaCyanAccent,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "African Commerce & Cultural Intelligence",
                            color = NovaTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = NovaTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Prompt Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(promptChips) { chip ->
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                queryText = chip
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0x2E00E5BE),
                        border = BorderStroke(1.dp, Color(0x3300E5BE))
                    ) {
                        Text(
                            text = chip,
                            color = NovaCyanAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Conversation Thread
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(chatHistory, key = { it.id }) { msg ->
                    if (msg.isUser) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF133628),
                            border = BorderStroke(1.dp, Color(0x4000E5BE)),
                            modifier = Modifier
                                .align(Alignment.End)
                                .widthIn(max = 280.dp)
                        ) {
                            Text(
                                text = msg.text,
                                color = NovaTextPrimary,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xCC0E1F17),
                            border = BorderStroke(1.dp, Color(0x2600E5BE)),
                            modifier = Modifier
                                .align(Alignment.Start)
                                .fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = msg.sender,
                                    color = NovaCyanAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = msg.text,
                                    color = NovaTextPrimary,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Input Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xCC0D1B16),
                border = BorderStroke(1.dp, Color(0x33FFFFFF))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.foundation.text.BasicTextField(
                        value = queryText,
                        onValueChange = { queryText = it },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = NovaTextPrimary,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("nova_ai_input"),
                        decorationBox = { innerTextField ->
                            if (queryText.isEmpty()) {
                                Text(
                                    text = "Ask NOVA AI or request a translation...",
                                    color = NovaTextMuted,
                                    fontSize = 13.sp
                                )
                            }
                            innerTextField()
                        }
                    )

                    IconButton(
                        onClick = {
                            if (queryText.isNotBlank()) {
                                val userQ = queryText
                                val userMsg = AiMessage("u_${System.currentTimeMillis()}", "You", userQ, true)
                                val responseText = when {
                                    "translate" in userQ.lowercase() || "twi" in userQ.lowercase() ->
                                        "Translation: 'Akwaaba kɔ Ghana' means 'Welcome to Ghana'. \n'Ɛte sɛn?' means 'How are you?'. \n'Me ho yɛ' means 'I am well'."
                                    "kente" in userQ.lowercase() ->
                                        "Ashanti Kente is the royal cloth of the Akan people. Yellow/Gold represents royalty, prosperity, and glory. Green signifies renewal and harvest. Blue symbolizes peace and love."
                                    "momo" in userQ.lowercase() || "ghipss" in userQ.lowercase() ->
                                        "Under GhIPSS interoperability guidelines, instant P2P transfers are routed through national switches with zero intermediary delay and biometric 2FA."
                                    else ->
                                        "Understood! For your African super-app workflow, NOVACHAT provides unified E2EE messaging, live auctions, and digital MoMo settlements across all ECOWAS regions."
                                }
                                val aiReply = AiMessage("a_${System.currentTimeMillis()}", "NOVA AI", responseText, false)
                                chatHistory = chatHistory + userMsg + aiReply
                                queryText = ""
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Query",
                            tint = NovaCyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
