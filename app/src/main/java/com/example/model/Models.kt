package com.example.model

enum class MessageType {
    TEXT,
    VOICE,
    IMAGE,
    MOMO_TRANSFER,
    CALL_LOG,
    SYSTEM_E2EE,
    SYSTEM_SECURE
}

data class ChatMessage(
    val id: String,
    val senderName: String,
    val senderAvatar: String = "",
    val text: String,
    val timestamp: String,
    val isMe: Boolean,
    val type: MessageType = MessageType.TEXT,
    val imageUrl: String? = null,
    val voiceDurationSeconds: Int = 0,
    val momoAmountGhs: Double = 0.0,
    val isDelivered: Boolean = true,
    val isRead: Boolean = true,
    val reactionEmoji: String? = null,
    val reactions: Map<String, String> = emptyMap(),
    val replyToId: String? = null,
    val replyToText: String? = null,
    val replyToSender: String? = null,
    val readBy: List<String> = emptyList(),
    val deliveredTo: List<String> = emptyList()
)

data class ChatThread(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val isGroup: Boolean = false,
    val isVerified: Boolean = false,
    val isE2EE: Boolean = false,
    val isPinned: Boolean = false,
    val tag: String = "",
    val participants: List<String> = emptyList(),
    val isTyping: Boolean = false,
    val typingText: String = ""
)


data class Contact(
    val id: String,
    val name: String,
    val handle: String,
    val phone: String,
    val avatarUrl: String,
    val status: String,
    val isOnline: Boolean = false,
    val isFavorite: Boolean = false,
    val isOfficial: Boolean = false,
    val category: String = "Friends" // Friends, Business, Official
)

data class GiftVaultItem(
    val id: String,
    val name: String,
    val priceGhs: Double,
    val priceUsd: Double,
    val iconName: String, // Cocoa, GyeNyame, GoldStool, Crystal, Crown
    val description: String
)

data class LiveAuctionLot(
    val lotNumber: String,
    val title: String,
    val currentBidGhs: Double,
    val usdEquivalent: Double,
    val unitsLeft: Int,
    val timeLeftFormatted: String,
    val imageResId: Int? = null
)

data class LiveComment(
    val id: String,
    val username: String,
    val badge: String = "", // PRO, ANGEL, VIP PATRON
    val message: String,
    val isPinned: Boolean = false,
    val avatarUrl: String = ""
)

data class PatronActivity(
    val patronName: String,
    val tier: String,
    val actionText: String,
    val paymentMethod: String = "MTN MOMO"
)

enum class PaymentProvider {
    MTN_MOMO,
    TELECEL_CASH,
    AT_MONEY,
    ECOBANK,
    GCB_BANK,
    NOVA_VAULT
}

data class WalletTransaction(
    val id: String,
    val title: String,
    val description: String,
    val amountGhs: Double,
    val isCredit: Boolean,
    val date: String,
    val provider: PaymentProvider,
    val status: String = "Completed",
    val referenceCode: String
)

data class RideOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val estimatedFareGhs: Double,
    val etaMinutes: Int,
    val vehicleIcon: String
)

data class ActiveRide(
    val id: String,
    val driverName: String,
    val driverRating: Float,
    val carModel: String,
    val plateNumber: String,
    val pickupLocation: String,
    val destination: String,
    val fareGhs: Double,
    val status: String // Arriving, On Trip, Completed
)

data class MiniApp(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val iconSymbol: String,
    val rating: Float,
    val usersCount: String
)

data class MarketProduct(
    val id: String,
    val title: String,
    val merchantName: String,
    val originRegion: String,
    val priceGhs: Double,
    val rating: Float,
    val salesCount: Int,
    val category: String,
    val inStock: Boolean = true
)

data class MomentPost(
    val id: String,
    val authorName: String,
    val authorHandle: String,
    val authorAvatar: String,
    val timestamp: String,
    val content: String,
    val location: String = "",
    val likesCount: Int,
    val commentsCount: Int,
    val isLiked: Boolean = false,
    val tags: List<String> = emptyList()
)
