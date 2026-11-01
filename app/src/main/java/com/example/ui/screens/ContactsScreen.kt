package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.model.Contact
import com.example.ui.theme.*

@Composable
fun ContactsScreen(
    viewModel: NovaChatViewModel,
    onOpenChat: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val contacts by viewModel.contacts.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showAddFriendDialog by remember { mutableStateOf(false) }

    val filteredContacts = contacts.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.handle.contains(searchQuery, ignoreCase = true) ||
                it.phone.contains(searchQuery)
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

            // Search Bar & Add Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = Color(0xCC0E1A15),
                    border = BorderStroke(1.dp, Color(0x3300E5BE))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
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
                            modifier = Modifier.weight(1f),
                            decorationBox = { inner ->
                                if (searchQuery.isEmpty()) {
                                    Text("Search contacts or phone...", color = NovaTextMuted, fontSize = 13.sp)
                                }
                                inner()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                IconButton(
                    onClick = { showAddFriendDialog = true },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0x3300E5BE))
                        .testTag("add_contact_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Add Contact",
                        tint = NovaCyanAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Official services / Quick shortcuts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ContactQuickTile(
                    title = "New Friends",
                    icon = Icons.Default.GroupAdd,
                    badge = "3 Requests",
                    onClick = { showAddFriendDialog = true },
                    modifier = Modifier.weight(1f)
                )
                ContactQuickTile(
                    title = "Official Accounts",
                    icon = Icons.Default.Verified,
                    badge = "Verified",
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Contacts List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredContacts, key = { it.id }) { contact ->
                    ContactRow(
                        contact = contact,
                        onChatClick = {
                            if (contact.id == "c1") {
                                onOpenChat("chat_ama")
                            } else {
                                onOpenChat("chat_kwesi")
                            }
                        },
                        onCallClick = {
                            viewModel.startCall(isVideo = false, participantName = contact.name)
                        }
                    )
                }
            }
        }

        if (showAddFriendDialog) {
            AddFriendDialog(onDismiss = { showAddFriendDialog = false })
        }
    }
}

@Composable
private fun ContactQuickTile(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badge: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xCC0E1A14),
        border = BorderStroke(1.dp, Color(0x2600E5BE))
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0x3300E5BE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = NovaCyanAccent, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = title, color = NovaTextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(text = badge, color = NovaCyanAccent, fontSize = 9.sp)
            }
        }
    }
}

@Composable
private fun ContactRow(
    contact: Contact,
    onChatClick: () -> Unit,
    onCallClick: () -> Unit
) {
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
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .border(1.dp, NovaCyanAccent, CircleShape)
                ) {
                    if (contact.avatarUrl == "artisan_ama") {
                        Image(
                            painter = painterResource(id = R.drawable.artisan_ama),
                            contentDescription = contact.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = contact.name,
                            tint = NovaTextPrimary,
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = contact.name,
                            color = NovaTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if (contact.isOfficial) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Official",
                                tint = NovaGold,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                    Text(
                        text = "${contact.handle} • ${contact.phone}",
                        color = NovaTextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onChatClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Chat",
                        tint = NovaCyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onCallClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        tint = NovaTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddFriendDialog(onDismiss: () -> Unit) {
    var friendId by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Friend on NOVACHAT", color = NovaTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Enter their NOVA ID, Ghana phone number, or scan their QR code.", color = NovaTextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = friendId,
                    onValueChange = { friendId = it },
                    label = { Text("Phone / NOVA ID") },
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
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = NovaCyanAccent, contentColor = Color(0xFF04140D))
            ) {
                Text("Send Request", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = NovaTextSecondary) }
        },
        containerColor = Color(0xFF0D1B15)
    )
}
