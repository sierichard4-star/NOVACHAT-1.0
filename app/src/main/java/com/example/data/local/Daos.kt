package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM cached_messages WHERE chatId = :chatId ORDER BY createdAt ASC")
    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Query("DELETE FROM cached_messages WHERE chatId = :chatId")
    suspend fun deleteMessagesForChat(chatId: String)

    @Query("SELECT COUNT(*) FROM cached_messages WHERE chatId = :chatId")
    suspend fun getMessageCount(chatId: String): Int
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM cached_user_profiles WHERE id = :userId")
    fun getUserProfile(userId: String): Flow<UserProfileEntity?>

    @Query("SELECT * FROM cached_user_profiles")
    fun getAllUserProfiles(): Flow<List<UserProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity)

    @Query("UPDATE cached_user_profiles SET walletBalanceGhs = :newBalance, updatedAt = :timestamp WHERE id = :userId")
    suspend fun updateWalletBalance(userId: String, newBalance: Double, timestamp: Long = System.currentTimeMillis())
}

@Dao
interface ChatThreadDao {
    @Query("SELECT * FROM cached_chat_threads ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllThreads(): Flow<List<ChatThreadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThread(thread: ChatThreadEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThreads(threads: List<ChatThreadEntity>)

    @Query("UPDATE cached_chat_threads SET lastMessage = :lastMessage, timestamp = :timestamp, updatedAt = :updatedAt WHERE id = :chatId")
    suspend fun updateLastMessage(chatId: String, lastMessage: String, timestamp: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE cached_chat_threads SET unreadCount = 0 WHERE id = :chatId")
    suspend fun markChatAsRead(chatId: String)
}
