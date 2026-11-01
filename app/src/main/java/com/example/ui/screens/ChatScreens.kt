package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import coil.compose.AsyncImage
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
import com.example.data.NovaChatViewModel
import com.example.model.ChatMessage
import com.example.model.ChatThread
import com.example.model.MessageType
import com.example.ui.components.E2eeIndicator
import com.example.ui.theme.*

@Composable
fun ChatListScreen(
    viewModel: NovaChatViewModel,
    onOpenChat: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val threads by viewModel.threads.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var showNewChatDialog by remember { mutableStateOf(false) }

    val filterOptions = listOf("All", "Encrypted", "Groups", "Official")


    val filteredThreads = threads.filter { thread ->
        val matchesSearch = thread.name.contains(searchQuery, ignoreCase = true) ||
                thread.lastMessage.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "Groups" -> thread.isGroup
            "Official" -> thread.tag == "Official"
            "Encrypted" -> thread.isE2EE
            else -> true
        }
        matchesSearch && matchesFilter
    }

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
            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(22.dp),
                color = Color(0xCC0E1A15),
                border = BorderStroke(1.dp, Color(0x3300E5BE))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = NovaTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = NovaTextPrimary,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_search_chats"),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search messages, friends or groups...",
                                    color = NovaTextMuted,
                                    fontSize = 13.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = NovaTextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterOptions.forEach { filter ->
                    val isSelected = filter == selectedFilter
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { selectedFilter = filter },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) NovaCyanAccent else Color(0x330E1F18),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) NovaCyanAccent else Color(0x2E00E5BE)
                        )
                    ) {
                        Text(
                            text = filter,
                            color = if (isSelected) Color(0xFF04140D) else NovaTextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Chat Threads List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredThreads, key = { it.id }) { thread ->
                    ChatThreadCard(
                        thread = thread,
                        onClick = { onOpenChat(thread.id) }
                    )
                }
            }
        }

        // New Chat or Group Floating Action Button
        FloatingActionButton(
            onClick = { showNewChatDialog = true },
            containerColor = NovaCyanAccent,
            contentColor = Color(0xFF04140D),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 86.dp, end = 16.dp)
                .testTag("fab_new_conversation")
        ) {
            Icon(
                imageVector = Icons.Default.AddComment,
                contentDescription = "New Conversation or Group",
                modifier = Modifier.size(24.dp)
            )
        }

        if (showNewChatDialog) {
            NewChatDialog(
                onDismiss = { showNewChatDialog = false },
                onCreate = { isGroup, name, targetId ->
                    viewModel.createConversation(isGroup, name, targetId) { newId ->
                        onOpenChat(newId)
                    }
                    showNewChatDialog = false
                }
            )
        }
    }
}


@Composable
private fun ChatThreadCard(
    thread: ChatThread,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("chat_thread_${thread.id}"),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xCC0D1B15),
        border = BorderStroke(1.dp, Color(0x2600E5BE))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(0x3300E5BE))
                    .border(1.dp, if (thread.isOnline) NovaCyanAccent else Color(0x33FFFFFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (thread.avatarUrl == "artisan_ama") {
                    Image(
                        painter = painterResource(id = R.drawable.artisan_ama),
                        contentDescription = thread.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (thread.isGroup) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = "Group",
                        tint = NovaCyanAccent,
                        modifier = Modifier.size(26.dp)
                    )
                } else if (thread.tag == "Official") {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Official",
                        tint = NovaGold,
                        modifier = Modifier.size(24.dp)
                    )
                } else if (thread.tag == "AI Smart Bot") {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Nova AI",
                        tint = NovaCyanAccent,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = thread.name,
                        tint = NovaTextPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Online Dot
                if (thread.isOnline) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(NovaOnlineGreen)
                            .border(2.dp, Color(0xFF09120E), CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Thread Information
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = thread.name,
                            color = NovaTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (thread.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                tint = NovaCyanAccent,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Text(
                        text = thread.timestamp,
                        color = NovaTextMuted,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (thread.isTyping) "typing..." else thread.lastMessage,
                        color = if (thread.isTyping) NovaCyanAccent else if (thread.unreadCount > 0) NovaTextPrimary else NovaTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = if (thread.isTyping) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )


                    if (thread.unreadCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(NovaCyanAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = thread.unreadCount.toString(),
                                color = Color(0xFF04140D),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Chat Detail Screen
 */
@Composable
fun ChatDetailScreen(
    chatId: String,
    viewModel: NovaChatViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val threads by viewModel.threads.collectAsState()
    val allMessages by viewModel.messages.collectAsState()
    val thread = threads.find { it.id == chatId } ?: return
    val messages = allMessages[chatId] ?: emptyList()

    var messageInput by remember { mutableStateOf("") }
    var inChatSearch by remember { mutableStateOf("") }
    var isSearchingInChat by remember { mutableStateOf(false) }
    var replyingTo by remember { mutableStateOf<ChatMessage?>(null) }
    var showMoMoSheet by remember { mutableStateOf(false) }
    var showImageDialog by remember { mutableStateOf(false) }
    var activeReactionMessageId by remember { mutableStateOf<String?>(null) }

    val displayedMessages = if (inChatSearch.isBlank()) {
        messages
    } else {
        messages.filter { it.text.contains(inChatSearch, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NovaDarkBackground)
    ) {
        // Chat Header
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
            color = Color(0xCC0A1711),
            border = BorderStroke(1.dp, Color(0x2600E5BE))
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("chat_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = NovaTextPrimary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .border(1.dp, NovaCyanAccent, CircleShape)
                        ) {
                            if (thread.avatarUrl == "artisan_ama") {
                                Image(
                                    painter = painterResource(id = R.drawable.artisan_ama),
                                    contentDescription = thread.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = if (thread.isGroup) Icons.Default.Groups else Icons.Default.Person,
                                    contentDescription = thread.name,
                                    tint = NovaCyanAccent,
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .fillMaxSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = thread.name,
                                    color = NovaTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (thread.isVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified",
                                        tint = NovaCyanAccent,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                            Text(
                                text = when {
                                    thread.isTyping -> "typing..."
                                    thread.isOnline -> "Active now • TLS + Rules Protected"
                                    else -> "Offline • Authorized Channel"
                                },
                                color = if (thread.isTyping || thread.isOnline) NovaCyanAccent else NovaTextMuted,
                                fontSize = 11.sp,
                                fontWeight = if (thread.isTyping) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    // Header Actions
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                isSearchingInChat = !isSearchingInChat
                                if (!isSearchingInChat) inChatSearch = ""
                            },
                            modifier = Modifier.testTag("chat_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Messages",
                                tint = if (isSearchingInChat) NovaCyanAccent else NovaTextSecondary
                            )
                        }
                        IconButton(
                            onClick = { viewModel.startCall(isVideo = false, participantName = thread.name) },
                            modifier = Modifier.testTag("chat_audio_call")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Audio Call",
                                tint = NovaCyanAccent
                            )
                        }
                        IconButton(
                            onClick = { viewModel.startCall(isVideo = true, participantName = thread.name) },
                            modifier = Modifier.testTag("chat_video_call")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Video Call",
                                tint = NovaCyanAccent
                            )
                        }
                    }
                }

                // In-Chat Search Bar
                AnimatedVisibility(visible = isSearchingInChat) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xCC0D1B16),
                        border = BorderStroke(1.dp, Color(0x3300E5BE))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = NovaCyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            androidx.compose.foundation.text.BasicTextField(
                                value = inChatSearch,
                                onValueChange = { inChatSearch = it },
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = NovaTextPrimary,
                                    fontSize = 13.sp
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_inchat_search"),
                                decorationBox = { inner ->
                                    if (inChatSearch.isEmpty()) {
                                        Text(
                                            text = "Filter messages in this chat...",
                                            color = NovaTextMuted,
                                            fontSize = 12.sp
                                        )
                                    }
                                    inner()
                                }
                            )
                            if (inChatSearch.isNotEmpty()) {
                                IconButton(
                                    onClick = { inChatSearch = "" },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = NovaTextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Truthful Security Banner (No unbacked E2EE claim)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0x3300E5BE)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Security",
                    tint = NovaCyanAccent,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "🔒 Authorized Channel. Participant-isolated access enforced via Firebase Security Rules.",
                    color = NovaCyanAccent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Message Thread List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(displayedMessages, key = { it.id }) { msg ->
                MessageBubble(
                    msg = msg,
                    onReply = { replyingTo = msg },
                    onReact = { activeReactionMessageId = msg.id },
                    onReactionClick = { emoji ->
                        viewModel.toggleReaction(chatId, msg.id, emoji)
                    }
                )
            }
        }

        // Quoted Reply Preview Bar
        AnimatedVisibility(visible = replyingTo != null) {
            replyingTo?.let { target ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x3300E5BE),
                    border = BorderStroke(1.dp, NovaCyanAccent)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(26.dp)
                                .background(NovaCyanAccent, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Replying to ${target.senderName}",
                                color = NovaCyanAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = target.text,
                                color = NovaTextPrimary,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = { replyingTo = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel reply",
                                tint = NovaTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Bottom Input Area
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            color = Color(0xF20A1712),
            border = BorderStroke(1.dp, Color(0x2600E5BE))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // MoMo Payment Button
                IconButton(
                    onClick = { showMoMoSheet = true },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFCC00))
                        .testTag("chat_open_momo_transfer")
                ) {
                    Text(
                        text = "₵",
                        color = Color(0xFF003366),
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                }

                // Image Attachment Button
                IconButton(
                    onClick = { showImageDialog = true },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0x3300E5BE))
                        .testTag("chat_open_image_picker")
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Share Image",
                        tint = NovaCyanAccent,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Voice Note Trigger
                IconButton(
                    onClick = { viewModel.sendVoiceNote(chatId) },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0x3300E5BE))
                        .testTag("chat_send_voicenote")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Note",
                        tint = NovaCyanAccent,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Text Input Field
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = Color(0xCC0D1B16),
                    border = BorderStroke(1.dp, Color(0x33FFFFFF))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.foundation.text.BasicTextField(
                            value = messageInput,
                            onValueChange = {
                                messageInput = it
                                viewModel.setTyping(chatId, it.isNotEmpty())
                            },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = NovaTextPrimary,
                                fontSize = 14.sp
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_message_input"),
                            decorationBox = { innerTextField ->
                                if (messageInput.isEmpty()) {
                                    Text(
                                        text = "Type a message...",
                                        color = NovaTextMuted,
                                        fontSize = 13.sp
                                    )
                                }
                                innerTextField()
                            }
                        )

                        if (messageInput.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    viewModel.sendMessage(
                                        chatId = chatId,
                                        text = messageInput,
                                        replyToId = replyingTo?.id,
                                        replyToText = replyingTo?.text,
                                        replyToSender = replyingTo?.senderName
                                    )
                                    messageInput = ""
                                    replyingTo = null
                                    viewModel.setTyping(chatId, false)
                                },
                                modifier = Modifier
                                    .size(30.dp)
                                    .testTag("chat_send_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = NovaCyanAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // MoMo P2P Sheet Dialog
        if (showMoMoSheet) {
            MoMoTransferDialog(
                recipientName = thread.name,
                onDismiss = { showMoMoSheet = false },
                onTransfer = { amount ->
                    viewModel.sendMoMoInChat(chatId, amount)
                    showMoMoSheet = false
                }
            )
        }

        // Image Attachment Dialog
        if (showImageDialog) {
            ImageAttachmentDialog(
                onDismiss = { showImageDialog = false },
                onSendImage = { imageUrl, caption ->
                    viewModel.sendImageMessage(chatId, imageUrl, caption)
                    showImageDialog = false
                }
            )
        }

        // Reaction Picker Dialog
        if (activeReactionMessageId != null) {
            ReactionPickerDialog(
                onDismiss = { activeReactionMessageId = null },
                onSelectEmoji = { emoji ->
                    viewModel.toggleReaction(chatId, activeReactionMessageId!!, emoji)
                    activeReactionMessageId = null
                }
            )
        }
    }
}

@Composable
private fun MessageBubble(
    msg: ChatMessage,
    onReply: () -> Unit = {},
    onReact: () -> Unit = {},
    onReactionClick: (String) -> Unit = {}
) {
    if (msg.type == MessageType.SYSTEM_E2EE || msg.type == MessageType.SYSTEM_SECURE) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = Color(0x2600E5BE)
        ) {
            Text(
                text = msg.text,
                color = NovaCyanAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        return
    }

    val isMe = msg.isMe
    val alignment = if (isMe) Alignment.End else Alignment.Start
    val bgColor = if (isMe) Color(0xFF0F3628) else Color(0xFF13251E)
    val borderColor = if (isMe) Color(0x6600E5BE) else Color(0x33FFFFFF)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMe) 16.dp else 4.dp,
                bottomEnd = if (isMe) 4.dp else 16.dp
            ),
            color = bgColor,
            border = BorderStroke(1.dp, borderColor),
            modifier = Modifier
                .widthIn(max = 290.dp)
                .testTag("message_bubble_${msg.id}")
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                // Sender label in group chats
                if (!isMe && msg.senderName.isNotBlank() && msg.senderName != "User") {
                    Text(
                        text = msg.senderName,
                        color = NovaCyanAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                // Quoted Reply Box if present
                if (!msg.replyToText.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x2200E5BE),
                        border = BorderStroke(1.dp, Color(0x4D00E5BE)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(24.dp)
                                    .background(NovaCyanAccent, RoundedCornerShape(1.dp))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = msg.replyToSender ?: "Reply",
                                    color = NovaCyanAccent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = msg.replyToText,
                                    color = NovaTextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Message Content
                when (msg.type) {
                    MessageType.VOICE -> {
                        var isPlaying by remember { mutableStateOf(false) }
                        val infiniteTransition = rememberInfiniteTransition(label = "wave_pulse")
                        val pulseFactor by infiniteTransition.animateFloat(
                            initialValue = 0.6f,
                            targetValue = 1.3f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(500, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulse"
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { isPlaying = !isPlaying }
                        ) {
                            IconButton(
                                onClick = { isPlaying = !isPlaying },
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(NovaCyanAccent.copy(alpha = 0.2f))
                                    .testTag("button_play_voice")
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause Voice Note" else "Play Voice Note",
                                    tint = NovaCyanAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            // Interactive waveform
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                val baseHeights = listOf(10, 18, 8, 22, 14, 26, 12, 20, 9, 16, 24)
                                baseHeights.forEachIndexed { idx, height ->
                                    val dynamicHeight = if (isPlaying) {
                                        val phase = if (idx % 2 == 0) pulseFactor else (2.0f - pulseFactor)
                                        (height * phase.coerceIn(0.5f, 1.4f)).dp
                                    } else {
                                        height.dp
                                    }
                                    Box(
                                        modifier = Modifier
                                            .width(3.dp)
                                            .height(dynamicHeight)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(if (isPlaying) NovaCyanAccent else NovaCyanAccent.copy(alpha = 0.7f))
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isPlaying) "Playing..." else "0:${if (msg.voiceDurationSeconds < 10) "0${msg.voiceDurationSeconds}" else "${msg.voiceDurationSeconds}"}",
                                color = if (isPlaying) NovaCyanAccent else NovaTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    MessageType.IMAGE -> {
                        Column {
                            if (msg.imageUrl == "artisan_ama") {
                                Image(
                                    painter = painterResource(id = R.drawable.artisan_ama),
                                    contentDescription = "Photo Attachment",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            } else if (!msg.imageUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = msg.imageUrl,
                                    contentDescription = "Photo Attachment",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            }
                            if (msg.text.isNotBlank() && msg.text != "Photo attachment") {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = msg.text,
                                    color = NovaTextPrimary,
                                    fontSize = 13.sp,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }

                    MessageType.MOMO_TRANSFER -> {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFCC00)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "₵",
                                        color = Color(0xFF003366),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "MTN MoMo Settlement",
                                        color = NovaGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "GH₵ ${String.format("%,.2f", msg.momoAmountGhs)}",
                                        color = NovaTextPrimary,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Biometric Verification • GhIPSS Confirmed",
                                color = NovaCyanAccent,
                                fontSize = 10.sp
                            )
                        }
                    }

                    else -> {
                        Text(
                            text = msg.text,
                            color = NovaTextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Footer Row: Timestamp, Read Receipts & Action Triggers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Action icons: reply and add reaction
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onReply,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Reply,
                                contentDescription = "Reply",
                                tint = NovaTextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = onReact,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddReaction,
                                contentDescription = "React",
                                tint = NovaTextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    // Timestamp and Read Receipts
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = msg.timestamp,
                            color = NovaTextMuted,
                            fontSize = 10.sp
                        )
                        if (isMe) {
                            Spacer(modifier = Modifier.width(4.dp))
                            if (msg.isRead) {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = "Read",
                                    tint = NovaCyanAccent,
                                    modifier = Modifier.size(13.dp)
                                )
                            } else if (msg.isDelivered) {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = "Delivered",
                                    tint = NovaTextMuted,
                                    modifier = Modifier.size(13.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Sent",
                                    tint = NovaTextMuted,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Reaction Pills underneath the bubble
        if (msg.reactions.isNotEmpty() || !msg.reactionEmoji.isNullOrBlank()) {
            val emojiCounts = mutableMapOf<String, Int>()
            msg.reactions.values.forEach { emoji ->
                emojiCounts[emoji] = (emojiCounts[emoji] ?: 0) + 1
            }
            if (emojiCounts.isEmpty() && !msg.reactionEmoji.isNullOrBlank()) {
                emojiCounts[msg.reactionEmoji] = 1
            }

            Row(
                modifier = Modifier
                    .padding(top = 2.dp, start = if (isMe) 0.dp else 4.dp, end = if (isMe) 4.dp else 0.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                emojiCounts.forEach { (emoji, count) ->
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onReactionClick(emoji) },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0x3300E5BE),
                        border = BorderStroke(1.dp, Color(0x4D00E5BE))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = emoji, fontSize = 12.sp)
                            if (count > 1) {
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = count.toString(),
                                    color = NovaCyanAccent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NewChatDialog(
    onDismiss: () -> Unit,
    onCreate: (isGroup: Boolean, name: String, targetId: String?) -> Unit
) {
    var isGroup by remember { mutableStateOf(false) }
    var chatName by remember { mutableStateOf("") }
    var participantInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isGroup) "Create Group Conversation" else "Start 1:1 Conversation",
                color = NovaTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Toggle between Direct and Group
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !isGroup,
                        onClick = { isGroup = false },
                        label = { Text("1:1 Direct") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NovaCyanAccent,
                            selectedLabelColor = Color(0xFF04140D)
                        )
                    )
                    FilterChip(
                        selected = isGroup,
                        onClick = { isGroup = true },
                        label = { Text("Group Conversation") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NovaCyanAccent,
                            selectedLabelColor = Color(0xFF04140D)
                        )
                    )
                }

                OutlinedTextField(
                    value = chatName,
                    onValueChange = { chatName = it },
                    label = { Text(if (isGroup) "Group Name" else "Contact / Chat Title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = NovaTextPrimary,
                        unfocusedTextColor = NovaTextPrimary,
                        focusedBorderColor = NovaCyanAccent,
                        unfocusedBorderColor = Color(0x33FFFFFF)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_new_chat_name")
                )

                OutlinedTextField(
                    value = participantInput,
                    onValueChange = { participantInput = it },
                    label = { Text(if (isGroup) "Participant User IDs (comma separated)" else "Partner User ID / Phone") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = NovaTextPrimary,
                        unfocusedTextColor = NovaTextPrimary,
                        focusedBorderColor = NovaCyanAccent,
                        unfocusedBorderColor = Color(0x33FFFFFF)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_new_chat_participant")
                )

                Text(
                    text = "🔒 Conversations are protected by Firestore security rules. Only authorized participants can access.",
                    color = NovaTextMuted,
                    fontSize = 11.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (chatName.isNotBlank()) {
                        onCreate(isGroup, chatName.trim(), participantInput.trim().ifBlank { null })
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NovaCyanAccent,
                    contentColor = Color(0xFF04140D)
                ),
                enabled = chatName.isNotBlank(),
                modifier = Modifier.testTag("btn_confirm_create_chat")
            ) {
                Text("Start Chat", fontWeight = FontWeight.Bold)
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
fun ImageAttachmentDialog(
    onDismiss: () -> Unit,
    onSendImage: (imageUrl: String, caption: String) -> Unit
) {
    var selectedPreset by remember { mutableStateOf("artisan_ama") }
    var captionText by remember { mutableStateOf("") }

    val presets = listOf(
        Pair("artisan_ama", "Ashanti Sovereign Kente"),
        Pair("https://images.unsplash.com/photo-1547471080-7cc2caa01a7e?w=600", "Savannah Sunset Arts"),
        Pair("https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=600", "Accra Gallery Artifacts")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Share Photo in Conversation",
                color = NovaTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Select an African artisanal image or craft showcase:",
                    color = NovaTextSecondary,
                    fontSize = 12.sp
                )

                presets.forEach { (url, label) ->
                    val isSelected = selectedPreset == url
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedPreset = url },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color(0x3300E5BE) else Color(0x1AFFFFFF),
                        border = BorderStroke(1.dp, if (isSelected) NovaCyanAccent else Color(0x22FFFFFF))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedPreset = url },
                                colors = RadioButtonDefaults.colors(selectedColor = NovaCyanAccent)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                color = if (isSelected) NovaCyanAccent else NovaTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = captionText,
                    onValueChange = { captionText = it },
                    label = { Text("Caption (optional)") },
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
                onClick = { onSendImage(selectedPreset, captionText) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NovaCyanAccent,
                    contentColor = Color(0xFF04140D)
                ),
                modifier = Modifier.testTag("btn_confirm_send_image")
            ) {
                Text("Share Image", fontWeight = FontWeight.Bold)
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
fun ReactionPickerDialog(
    onDismiss: () -> Unit,
    onSelectEmoji: (String) -> Unit
) {
    val emojis = listOf("❤️", "👍", "🇬🇭", "🔥", "✨", "👑", "👏")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "React to Message",
                color = NovaTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                emojis.forEach { emoji ->
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0x3300E5BE))
                            .clickable { onSelectEmoji(emoji) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 20.sp)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss", color = NovaTextSecondary)
            }
        },
        containerColor = Color(0xFF0D1B15)
    )
}

@Composable
private fun MoMoTransferDialog(
    recipientName: String,
    onDismiss: () -> Unit,
    onTransfer: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("50") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFCC00)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("₵", color = Color(0xFF003366), fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Send MoMo to $recipientName",
                    color = NovaTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Instant P2P settlement with zero transfer fee on NOVACHAT network.",
                    color = NovaTextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("momo_amount_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 50.0
                    onTransfer(amt)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NovaCyanAccent,
                    contentColor = Color(0xFF04140D)
                ),
                modifier = Modifier.testTag("confirm_momo_transfer")
            ) {
                Text("Confirm & Send", fontWeight = FontWeight.Bold)
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

