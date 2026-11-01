package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.MainDestination
import com.example.data.NovaChatViewModel
import com.example.data.local.NovaCacheRepository
import com.example.data.local.NovaDatabase
import com.example.data.local.UserProfileEntity
import com.example.model.ChatMessage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches NOVACHAT`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("NOVACHAT", appName)
    }

    @Test
    fun `verify initial super app destinations and state`() {
        val viewModel = NovaChatViewModel()
        val state = viewModel.uiState.value
        assertEquals(MainDestination.HOME, state.currentDestination)
        assertTrue(state.walletBalanceGhs > 0)
        assertTrue(state.currentLotBidGhs >= 1250.0)
        assertNotNull(viewModel.giftVaultItems)
        assertTrue(viewModel.giftVaultItems.isNotEmpty())
    }

    @Test
    fun `place bid increases lot bid and session earnings`() {
        val viewModel = NovaChatViewModel()
        val initialBid = viewModel.uiState.value.currentLotBidGhs
        val initialEarnings = viewModel.uiState.value.liveSessionEarningsGhs

        viewModel.placeBidOnLiveLot()

        val newBid = viewModel.uiState.value.currentLotBidGhs
        val newEarnings = viewModel.uiState.value.liveSessionEarningsGhs
        assertEquals(initialBid + 50.0, newBid, 0.01)
        assertEquals(initialEarnings + 50.0, newEarnings, 0.01)
    }

    @Test
    fun `send message in chat appends message`() {
        val viewModel = NovaChatViewModel()
        val chatId = "chat_ama"
        val countBefore = viewModel.messages.value[chatId]?.size ?: 0

        viewModel.sendMessage(chatId, "Testing E2EE delivery in Kumasi")

        val countAfter = viewModel.messages.value[chatId]?.size ?: 0
        assertEquals(countBefore + 1, countAfter)
    }

    @Test
    fun `room database stores and retrieves cached messages and user profile`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, NovaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val repo = NovaCacheRepository(db)

        val testProfile = UserProfileEntity(
            id = "user_kwesi",
            name = "Kwesi Mensah",
            handle = "@kwesi_m",
            phone = "+233 24 889 120",
            walletBalanceGhs = 450.00
        )
        repo.saveUserProfile(testProfile)

        val retrievedProfile = repo.getCachedUserProfile("user_kwesi").first()
        assertNotNull(retrievedProfile)
        assertEquals("Kwesi Mensah", retrievedProfile?.name)
        assertEquals(450.00, retrievedProfile?.walletBalanceGhs ?: 0.0, 0.01)

        val testMessage = ChatMessage(
            id = "m_cache_1",
            senderName = "Me",
            text = "Testing offline Room caching",
            timestamp = "12:00 PM",
            isMe = true
        )
        repo.saveMessage("chat_ama", testMessage)

        val retrievedMessages = repo.getCachedMessages("chat_ama").first()
        assertEquals(1, retrievedMessages.size)
        assertEquals("Testing offline Room caching", retrievedMessages[0].text)

        db.close()
    }

    @Test
    fun `verify Room UserProfile and ChatMessage entity schema instantiation`() {
        val userProfile = com.example.data.local.UserProfile(
            id = "user_ama",
            name = "Ama Serwaa",
            handle = "@ama_serwaa",
            phone = "+233 24 555 0192",
            avatarUrl = "artisan_ama",
            bio = "Weaving royal Ashanti heritage",
            location = "Kumasi, Ghana",
            isVerified = true,
            walletBalanceGhs = 8420.00,
            updatedAt = 1700000000L
        )
        assertEquals("user_ama", userProfile.id)
        assertEquals("Kumasi, Ghana", userProfile.location)
        assertEquals(8420.00, userProfile.walletBalanceGhs, 0.01)

        val chatMessage = com.example.data.local.ChatMessageEntity(
            id = "msg_001",
            chatId = "chat_ama",
            senderName = "Kwesi",
            senderAvatar = "",
            text = "Akwaaba! Lot #04 received.",
            timestamp = "12:00 PM",
            isMe = true,
            type = "MOMO_TRANSFER",
            voiceDurationSeconds = 0,
            momoAmountGhs = 1250.00,
            isDelivered = true,
            isRead = true,
            reactionEmoji = "🇬🇭",
            createdAt = 1700000100L
        )
        assertEquals("msg_001", chatMessage.id)
        assertEquals("chat_ama", chatMessage.chatId)
        assertEquals(1250.00, chatMessage.momoAmountGhs, 0.01)
        assertEquals("MOMO_TRANSFER", chatMessage.type)
    }
}
