package com.example.data.local

import android.util.Log
import com.example.model.ChatMessage
import com.example.model.ChatThread
import com.example.model.MessageType
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class NovaCacheRepository(
    private val database: NovaDatabase,
    private val firestoreInstance: FirebaseFirestore? = null
) {

    private val messageDao = database.messageDao()
    private val userProfileDao = database.userProfileDao()
    private val chatThreadDao = database.chatThreadDao()

    // Initialize Firestore instance for cloud structured data management
    val firestore: FirebaseFirestore? by lazy {
        firestoreInstance ?: try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("NovaCacheRepository", "Firestore initialization deferred: ${e.message}")
            null
        }
    }

    // Threads
    fun getCachedThreads(): Flow<List<ChatThread>> {
        return chatThreadDao.getAllThreads().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun saveThreads(threads: List<ChatThread>) {
        chatThreadDao.insertThreads(threads.map { it.toEntity() })
    }

    suspend fun updateThreadLastMessage(chatId: String, lastMessage: String, timestamp: String) {
        chatThreadDao.updateLastMessage(chatId, lastMessage, timestamp)
    }

    suspend fun markChatAsRead(chatId: String) {
        chatThreadDao.markChatAsRead(chatId)
    }

    // Messages
    fun getCachedMessages(chatId: String): Flow<List<ChatMessage>> {
        return messageDao.getMessagesForChat(chatId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun saveMessage(chatId: String, message: ChatMessage) {
        messageDao.insertMessage(message.toEntity(chatId))
        chatThreadDao.updateLastMessage(chatId, message.text, message.timestamp)
        syncMessageToCloud(chatId, message)
    }

    suspend fun saveMessages(chatId: String, messages: List<ChatMessage>) {
        messageDao.insertMessages(messages.map { it.toEntity(chatId) })
    }

    suspend fun hasCachedMessages(chatId: String): Boolean {
        return messageDao.getMessageCount(chatId) > 0
    }

    // Profile
    fun getCachedUserProfile(userId: String): Flow<UserProfileEntity?> {
        return userProfileDao.getUserProfile(userId)
    }

    suspend fun saveUserProfile(profile: UserProfileEntity) {
        userProfileDao.insertUserProfile(profile)
        syncUserProfileToCloud(profile)
    }

    suspend fun updateWalletBalance(userId: String, newBalance: Double) {
        userProfileDao.updateWalletBalance(userId, newBalance)
        val db = firestore ?: return
        try {
            db.collection("users").document(userId)
                .update("walletBalanceGhs", newBalance, "updatedAt", System.currentTimeMillis())
        } catch (e: Exception) {
            Log.w("NovaCacheRepository", "Could not sync wallet balance to Firestore: ${e.message}")
        }
    }

    // Firestore Structured Cloud Data Management
    suspend fun syncUserProfileToCloud(profile: UserProfileEntity) {
        val db = firestore ?: return
        try {
            val userMap = hashMapOf(
                "id" to profile.id,
                "name" to profile.name,
                "handle" to profile.handle,
                "phone" to profile.phone,
                "avatarUrl" to profile.avatarUrl,
                "bio" to profile.bio,
                "location" to profile.location,
                "isVerified" to profile.isVerified,
                "walletBalanceGhs" to profile.walletBalanceGhs,
                "updatedAt" to profile.updatedAt
            )
            db.collection("users").document(profile.id)
                .set(userMap, SetOptions.merge())
                .await()
            Log.d("NovaCacheRepository", "User profile synced to Firestore: ${profile.id}")
        } catch (e: Exception) {
            Log.w("NovaCacheRepository", "Firestore user sync deferred: ${e.message}")
        }
    }

    suspend fun syncMessageToCloud(chatId: String, message: ChatMessage) {
        val db = firestore ?: return
        try {
            val messageMap = hashMapOf(
                "id" to message.id,
                "chatId" to chatId,
                "senderName" to message.senderName,
                "senderAvatar" to message.senderAvatar,
                "text" to message.text,
                "timestamp" to message.timestamp,
                "isMe" to message.isMe,
                "type" to message.type.name,
                "voiceDurationSeconds" to message.voiceDurationSeconds,
                "momoAmountGhs" to message.momoAmountGhs,
                "isDelivered" to message.isDelivered,
                "isRead" to message.isRead,
                "reactionEmoji" to message.reactionEmoji,
                "createdAt" to System.currentTimeMillis()
            )
            db.collection("chats").document(chatId)
                .collection("messages").document(message.id)
                .set(messageMap, SetOptions.merge())
                .await()

            val threadSummary = hashMapOf(
                "lastMessage" to message.text,
                "timestamp" to message.timestamp,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("chats").document(chatId)
                .set(threadSummary, SetOptions.merge())
                .await()
            Log.d("NovaCacheRepository", "Message synced to Firestore: ${message.id} in $chatId")
        } catch (e: Exception) {
            Log.w("NovaCacheRepository", "Firestore message sync deferred: ${e.message}")
        }
    }
}

// Extension mappers
fun MessageEntity.toDomainModel(): ChatMessage {
    val messageType = try {
        MessageType.valueOf(this.type)
    } catch (_: Exception) {
        MessageType.TEXT
    }
    return ChatMessage(
        id = this.id,
        senderName = this.senderName,
        senderAvatar = this.senderAvatar,
        text = this.text,
        timestamp = this.timestamp,
        isMe = this.isMe,
        type = messageType,
        voiceDurationSeconds = this.voiceDurationSeconds,
        momoAmountGhs = this.momoAmountGhs,
        isDelivered = this.isDelivered,
        isRead = this.isRead,
        reactionEmoji = this.reactionEmoji
    )
}

fun ChatMessage.toEntity(chatId: String): MessageEntity {
    return MessageEntity(
        id = this.id,
        chatId = chatId,
        senderName = this.senderName,
        senderAvatar = this.senderAvatar,
        text = this.text,
        timestamp = this.timestamp,
        isMe = this.isMe,
        type = this.type.name,
        voiceDurationSeconds = this.voiceDurationSeconds,
        momoAmountGhs = this.momoAmountGhs,
        isDelivered = this.isDelivered,
        isRead = this.isRead,
        reactionEmoji = this.reactionEmoji
    )
}

fun ChatThreadEntity.toDomainModel(): ChatThread {
    return ChatThread(
        id = this.id,
        name = this.name,
        avatarUrl = this.avatarUrl,
        lastMessage = this.lastMessage,
        timestamp = this.timestamp,
        unreadCount = this.unreadCount,
        isOnline = this.isOnline,
        isGroup = this.isGroup,
        isVerified = this.isVerified,
        isE2EE = this.isE2EE,
        isPinned = this.isPinned,
        tag = this.tag
    )
}

fun ChatThread.toEntity(): ChatThreadEntity {
    return ChatThreadEntity(
        id = this.id,
        name = this.name,
        avatarUrl = this.avatarUrl,
        lastMessage = this.lastMessage,
        timestamp = this.timestamp,
        unreadCount = this.unreadCount,
        isOnline = this.isOnline,
        isGroup = this.isGroup,
        isVerified = this.isVerified,
        isE2EE = this.isE2EE,
        isPinned = this.isPinned,
        tag = this.tag
    )
}
