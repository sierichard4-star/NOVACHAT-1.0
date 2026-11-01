package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.firebase.FirebaseChatRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ChatRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createConversation_and_sendMessage_succeeds() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val bobUid = "bob_test_uid"
        val repository = FirebaseChatRepository(firestore, auth)

        val convResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createConversation(
                isGroup = false,
                name = "Direct Chat",
                participantUids = listOf(bobUid)
            )
        }
        assertTrue("Conversation creation should succeed", convResult.isSuccess)
        val convId = convResult.getOrThrow()
        assertTrue("Conversation ID should not be empty", convId.isNotEmpty())

        val msgResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.sendMessage(
                conversationId = convId,
                text = "Hello from NOVACHAT!",
                type = "TEXT",
                participants = listOf(aliceUid, bobUid)
            )
        }
        assertTrue("Message send should succeed", msgResult.isSuccess)
        val msgId = msgResult.getOrThrow()
        assertTrue("Message ID should not be empty", msgId.isNotEmpty())
    }

    @Test
    fun observeMessages_authenticatedParticipant_emitsUpdates() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val bobUid = "bob_test_uid"
        val repository = FirebaseChatRepository(firestore, auth)

        val convId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createConversation(
                isGroup = false,
                name = "Direct Chat",
                participantUids = listOf(bobUid)
            ).getOrThrow()
        }

        val msgId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.sendMessage(
                conversationId = convId,
                text = "Real-time sync test",
                type = "TEXT",
                participants = listOf(aliceUid, bobUid)
            ).getOrThrow()
        }

        val messages = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeMessages(convId).first { list -> list.any { it.id == msgId } }
        }
        assertTrue("Emitted messages should contain sent message", messages.any { it.id == msgId })
    }

    @Test
    fun unauthenticatedCaller_failsWithPermissionDenied() = runBlocking {
        // Sign out to simulate an unauthenticated caller
        auth.signOut()
        val repository = FirebaseChatRepository(firestore, auth)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeMessages("unauth_conv").first()
            }
            fail("Expected exception for unauthenticated access")
        } catch (e: Exception) {
            val firestoreException = checkNotNull(e.unwrapFirestoreException()) {
                "Expected nested FirebaseFirestoreException, got $e"
            }
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice@novachat.test"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
