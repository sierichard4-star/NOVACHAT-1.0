package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room database entity for local caching of UserProfile.
 * Provides schema for offline user profile persistence and instant UI loading.
 */
@Entity(tableName = "cached_user_profiles")
data class UserProfile(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "handle")
    val handle: String,

    @ColumnInfo(name = "phone")
    val phone: String,

    @ColumnInfo(name = "avatarUrl")
    val avatarUrl: String = "",

    @ColumnInfo(name = "bio")
    val bio: String = "",

    @ColumnInfo(name = "location")
    val location: String = "Accra, Ghana",

    @ColumnInfo(name = "isVerified")
    val isVerified: Boolean = true,

    @ColumnInfo(name = "walletBalanceGhs")
    val walletBalanceGhs: Double = 450.00,

    @ColumnInfo(name = "updatedAt")
    val updatedAt: Long = System.currentTimeMillis()
)

typealias UserProfileEntity = UserProfile

/**
 * Room database entity for local caching of ChatMessage.
 * Provides schema for offline conversation history, voice notes, and MoMo transfers.
 */
@Entity(
    tableName = "cached_messages",
    indices = [Index(value = ["chatId", "createdAt"])]
)
data class MessageEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "chatId")
    val chatId: String,

    @ColumnInfo(name = "senderName")
    val senderName: String,

    @ColumnInfo(name = "senderAvatar")
    val senderAvatar: String = "",

    @ColumnInfo(name = "text")
    val text: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: String,

    @ColumnInfo(name = "isMe")
    val isMe: Boolean,

    @ColumnInfo(name = "type")
    val type: String = "TEXT",

    @ColumnInfo(name = "voiceDurationSeconds")
    val voiceDurationSeconds: Int = 0,

    @ColumnInfo(name = "momoAmountGhs")
    val momoAmountGhs: Double = 0.0,

    @ColumnInfo(name = "isDelivered")
    val isDelivered: Boolean = true,

    @ColumnInfo(name = "isRead")
    val isRead: Boolean = true,

    @ColumnInfo(name = "reactionEmoji")
    val reactionEmoji: String? = null,

    @ColumnInfo(name = "createdAt")
    val createdAt: Long = System.currentTimeMillis()
)

typealias ChatMessageEntity = MessageEntity

/**
 * Room database entity for local caching of ChatThread overviews.
 */
@Entity(
    tableName = "cached_chat_threads",
    indices = [Index(value = ["isPinned", "updatedAt"])]
)
data class ChatThreadEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "avatarUrl")
    val avatarUrl: String,

    @ColumnInfo(name = "lastMessage")
    val lastMessage: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: String,

    @ColumnInfo(name = "unreadCount")
    val unreadCount: Int = 0,

    @ColumnInfo(name = "isOnline")
    val isOnline: Boolean = false,

    @ColumnInfo(name = "isGroup")
    val isGroup: Boolean = false,

    @ColumnInfo(name = "isVerified")
    val isVerified: Boolean = false,

    @ColumnInfo(name = "isE2EE")
    val isE2EE: Boolean = true,

    @ColumnInfo(name = "isPinned")
    val isPinned: Boolean = false,

    @ColumnInfo(name = "tag")
    val tag: String = "",

    @ColumnInfo(name = "updatedAt")
    val updatedAt: Long = System.currentTimeMillis()
)
