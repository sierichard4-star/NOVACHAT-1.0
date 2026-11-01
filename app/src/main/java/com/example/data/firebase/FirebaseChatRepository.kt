package com.example.data.firebase

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseChatRepository(
    val db: FirebaseFirestore,
    val auth: FirebaseAuth = Firebase.auth
) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        Firebase.auth
    )

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeConversations(): Flow<List<FirestoreConversation>> = callbackFlow {
        val uid = requireUserId()
        val query = db.collection("conversations")
            .whereArrayContains("participants", uid)

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, "conversations")
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(FirestoreConversation::class.java)?.copy(id = doc.id)
                }
                trySend(list)
            }
        }
        awaitClose { registration.remove() }
    }

    fun observeMessages(conversationId: String): Flow<List<FirestoreMessage>> = callbackFlow {
        val path = "conversations/$conversationId/messages"
        val query = db.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, path)
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(FirestoreMessage::class.java)?.copy(id = doc.id)
                }
                trySend(list)
            }
        }
        awaitClose { registration.remove() }
    }

    suspend fun createConversation(
        isGroup: Boolean,
        name: String,
        participantUids: List<String>
    ): Result<String> {
        return try {
            val uid = requireUserId()
            val allParticipants = (participantUids + uid).distinct()
            val docRef = db.collection("conversations").document()
            val initialText = if (isGroup) "Group created" else "Conversation started"
            val data = hashMapOf(
                "isGroup" to isGroup,
                "name" to name,
                "participants" to allParticipants,
                "adminUids" to listOf(uid),
                "lastMessage" to initialText,
                "lastMessageTimestamp" to FieldValue.serverTimestamp(),
                "lastMessageSenderId" to uid,
                "typing" to emptyMap<String, Any>(),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(data).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "conversations")
            Result.failure(e)
        }
    }

    suspend fun sendMessage(
        conversationId: String,
        text: String,
        type: String = "TEXT",
        imageUrl: String? = null,
        voiceDurationSeconds: Int = 0,
        momoAmountGhs: Double = 0.0,
        replyToId: String? = null,
        replyToText: String? = null,
        replyToSender: String? = null,
        participants: List<String> = emptyList(),
        metadata: Map<String, Any?> = emptyMap()
    ): Result<String> {
        val path = "conversations/$conversationId/messages"
        return try {
            val uid = requireUserId()
            val senderDisplayName = auth.currentUser?.displayName ?: "User"
            val senderPhoto = auth.currentUser?.photoUrl?.toString()

            val msgRef = db.collection("conversations")
                .document(conversationId)
                .collection("messages")
                .document()

            val payload = hashMapOf<String, Any>(
                "conversationId" to conversationId,
                "senderId" to uid,
                "senderName" to senderDisplayName,
                "text" to text,
                "type" to type,
                "voiceDurationSeconds" to voiceDurationSeconds,
                "momoAmountGhs" to momoAmountGhs,
                "reactions" to emptyMap<String, String>(),
                "readBy" to listOf(uid),
                "deliveredTo" to listOf(uid),
                "createdAt" to FieldValue.serverTimestamp()
            )

            if (!senderPhoto.isNullOrBlank()) payload["senderAvatar"] = senderPhoto
            if (!imageUrl.isNullOrBlank()) payload["imageUrl"] = imageUrl
            if (!replyToId.isNullOrBlank()) payload["replyToId"] = replyToId
            if (!replyToText.isNullOrBlank()) payload["replyToText"] = replyToText
            if (!replyToSender.isNullOrBlank()) payload["replyToSender"] = replyToSender
            if (participants.isNotEmpty()) payload["participants"] = participants

            msgRef.set(payload).await()

            // Update parent conversation lastMessage and timestamp
            val summaryText = if (type == "VOICE") "🎤 Voice message ($voiceDurationSeconds s)" else text
            val convUpdate = hashMapOf<String, Any>(
                "lastMessage" to summaryText,
                "lastMessageTimestamp" to FieldValue.serverTimestamp(),
                "lastMessageSenderId" to uid,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("conversations").document(conversationId).update(convUpdate).await()

            Result.success(msgRef.id)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun toggleReaction(conversationId: String, messageId: String, emoji: String): Result<Unit> {
        val path = "conversations/$conversationId/messages/$messageId"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("conversations").document(conversationId).collection("messages").document(messageId)
            val snapshot = docRef.get().await()
            val reactions = (snapshot.get("reactions") as? Map<*, *>)?.toMutableMap() ?: mutableMapOf<Any?, Any?>()
            if (reactions[uid] == emoji) {
                reactions.remove(uid)
            } else {
                reactions[uid] = emoji
            }
            docRef.update("reactions", reactions).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun markMessageAsRead(conversationId: String, messageId: String): Result<Unit> {
        val path = "conversations/$conversationId/messages/$messageId"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("conversations").document(conversationId).collection("messages").document(messageId)
            docRef.update("readBy", FieldValue.arrayUnion(uid)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun setTypingStatus(conversationId: String, isTyping: Boolean): Result<Unit> {
        val path = "conversations/$conversationId"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("conversations").document(conversationId)
            if (isTyping) {
                docRef.update("typing.$uid", FieldValue.serverTimestamp()).await()
            } else {
                docRef.update("typing.$uid", FieldValue.delete()).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun syncUserProfile(
        displayName: String,
        handle: String,
        photoUrl: String? = null,
        email: String? = null,
        phoneNumber: String? = null,
        metadata: Map<String, Any?> = emptyMap()
    ): Result<Unit> {
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid)
            val snapshot = docRef.get().await()

            if (!snapshot.exists()) {
                val data = hashMapOf<String, Any>(
                    "userId" to uid,
                    "displayName" to displayName,
                    "handle" to handle,
                    "isOnline" to true,
                    "status" to "Akwaaba! Connect with me on NOVACHAT",
                    "content" to "Active NOVACHAT member",
                    "senderId" to uid,
                    "lastSeen" to FieldValue.serverTimestamp(),
                    "timestamp" to FieldValue.serverTimestamp(),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                photoUrl?.let { 
                    data["photoUrl"] = it 
                    data["avatarUrl"] = it
                }
                email?.let { data["email"] = it }
                phoneNumber?.let {
                    data["phone"] = it
                    data["phoneNumber"] = it
                }
                if (metadata.isNotEmpty()) {
                    data["metadata"] = metadata
                }
                docRef.set(data).await()
            } else {
                val updates = hashMapOf<String, Any>(
                    "displayName" to displayName,
                    "handle" to handle,
                    "isOnline" to true,
                    "lastSeen" to FieldValue.serverTimestamp(),
                    "timestamp" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                photoUrl?.let { 
                    updates["photoUrl"] = it 
                    updates["avatarUrl"] = it
                }
                email?.let { updates["email"] = it }
                phoneNumber?.let {
                    updates["phone"] = it
                    updates["phoneNumber"] = it
                }
                if (metadata.isNotEmpty()) {
                    updates["metadata"] = metadata
                }
                docRef.update(updates).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "users")
            Result.failure(e)
        }
    }

    suspend fun saveUserProfile(profile: com.example.model.UserProfile): Result<Unit> {
        return syncUserProfile(
            displayName = profile.displayName,
            handle = profile.handle.ifBlank { "@" + profile.displayName.lowercase().replace(" ", "_") },
            photoUrl = profile.effectiveAvatar.ifBlank { null },
            email = profile.email.ifBlank { null },
            phoneNumber = profile.effectivePhone.ifBlank { null },
            metadata = profile.metadata
        )
    }

    suspend fun setUserOnlineStatus(isOnline: Boolean): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.success(Unit)
            val docRef = db.collection("users").document(uid)
            val updates = hashMapOf<String, Any>(
                "isOnline" to isOnline,
                "lastSeen" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "users")
            Result.failure(e)
        }
    }
}
