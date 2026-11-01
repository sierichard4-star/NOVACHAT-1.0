package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

/**
 * Real Firestore-persisted User Profile model for NOVACHAT.
 * Stored under collection: `users/{userId}`.
 */
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val phone: String = "",
    val handle: String = "",
    val photoUrl: String? = null,
    val avatarUrl: String? = null,
    val status: String = "Akwaaba! Connect with me on NOVACHAT",
    val content: String = "",
    val senderId: String = "",
    val isOnline: Boolean = false,
    @ServerTimestamp val timestamp: Timestamp? = null,
    @ServerTimestamp val lastSeen: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
    val metadata: Map<String, Any?> = emptyMap()
) {
    val effectivePhone: String
        get() = phoneNumber.ifBlank { phone }

    val effectiveAvatar: String
        get() = photoUrl ?: avatarUrl ?: ""
}

/**
 * Real Firestore-persisted Conversation model for NOVACHAT.
 * Stored under collection: `conversations/{conversationId}`.
 */
@IgnoreExtraProperties
data class Conversation(
    @DocumentId val id: String = "",
    val isGroup: Boolean = false,
    val name: String = "",
    val title: String = "",
    val participants: List<String> = emptyList(),
    val adminUids: List<String> = emptyList(),
    val senderId: String = "",
    val lastMessageSenderId: String = "",
    val content: String = "",
    val lastMessage: String = "",
    @ServerTimestamp val timestamp: Timestamp? = null,
    @ServerTimestamp val lastMessageTimestamp: Timestamp? = null,
    val typing: Map<String, Timestamp?> = emptyMap(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
    val metadata: Map<String, Any?> = emptyMap()
) {
    val effectiveName: String
        get() = name.ifBlank { title.ifBlank { if (isGroup) "Group Chat" else "Conversation" } }

    val effectiveLastMessage: String
        get() = content.ifBlank { lastMessage }

    val effectiveSenderId: String
        get() = senderId.ifBlank { lastMessageSenderId }
}

/**
 * Real Firestore-persisted Message model for NOVACHAT.
 * Stored under subcollection: `conversations/{conversationId}/messages/{messageId}`.
 */
@IgnoreExtraProperties
data class Message(
    @DocumentId val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderAvatar: String? = null,
    val content: String = "",
    val text: String = "",
    val type: String = "TEXT", // TEXT, VOICE, IMAGE, MOMO_TRANSFER, CALL_LOG, SYSTEM
    @ServerTimestamp val timestamp: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    val mediaUrl: String? = null,
    val imageUrl: String? = null,
    val voiceDurationSeconds: Int = 0,
    val momoAmountGhs: Double = 0.0,
    val replyToId: String? = null,
    val replyToText: String? = null,
    val replyToSender: String? = null,
    val reactions: Map<String, String> = emptyMap(), // userId -> emoji
    val readBy: List<String> = emptyList(),
    val deliveredTo: List<String> = emptyList(),
    val participants: List<String> = emptyList(),
    val metadata: Map<String, Any?> = emptyMap()
) {
    val effectiveContent: String
        get() = content.ifBlank { text }

    val effectiveMediaUrl: String?
        get() = mediaUrl ?: imageUrl

    val effectiveTimestamp: Timestamp?
        get() = timestamp ?: createdAt
}
