package com.example.data.firebase

import com.example.model.Conversation
import com.example.model.Message
import com.example.model.UserProfile
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

// Unified Type Aliases for Firestore Data Models
typealias FirestoreUser = UserProfile
typealias FirestoreConversation = Conversation
typealias FirestoreMessage = Message
