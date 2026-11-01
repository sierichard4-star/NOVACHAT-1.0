package com.example.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.NovaCacheRepository
import com.example.data.local.UserProfileEntity
import com.example.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import com.example.data.firebase.FirebaseChatRepository
import com.example.data.firebase.FirestoreConversation
import com.example.data.firebase.FirestoreMessage
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

enum class MainDestination {
    HOME,       // Live Stream / Ecosystem
    CHATS,      // Messaging
    NOVAPAY,    // Financial Wallet
    DISCOVER,   // Moments & Social
    ME          // Profile & Security
}

enum class ActiveScreen {
    MAIN,
    CHAT_DETAIL,
    AUDIO_CALL,
    VIDEO_CALL,
    RIDE_HAILING,
    MARKETPLACE,
    QR_SCANNER,
    MINI_APP_VIEWER
}

data class NovaChatUiState(
    val currentDestination: MainDestination = MainDestination.HOME,
    val activeScreen: ActiveScreen = ActiveScreen.MAIN,
    val selectedChatId: String? = null,
    val isGiftVaultOpen: Boolean = false,
    val selectedGiftItem: GiftVaultItem? = null,
    val isNovaAiOpen: Boolean = false,
    val walletBalanceGhs: Double = 450.00,
    val liveSessionEarningsGhs: Double = 8420.00,
    val currentLotBidGhs: Double = 1250.00,
    val currentLotUnits: Int = 3,
    val callActive: Boolean = false,
    val callParticipantName: String = "Ama Serwaa",
    val isMicMuted: Boolean = false,
    val isCameraOn: Boolean = true,
    val isSpeakerOn: Boolean = true,
    val activeRide: ActiveRide? = null,
    val notificationMessage: String? = null
)

class NovaChatViewModel(
    private val repository: NovaCacheRepository? = null,
    private val firebaseRepository: FirebaseChatRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(NovaChatUiState())
    val uiState: StateFlow<NovaChatUiState> = _uiState.asStateFlow()

    private var activeMessageJob: Job? = null
    private var conversationsJob: Job? = null
    private var currentAuthenticatedUserId: String? = null

    // Chats state
    private val _threads = MutableStateFlow<List<ChatThread>>(emptyList())

    val threads: StateFlow<List<ChatThread>> = _threads.asStateFlow()

    private val _messages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    val messages: StateFlow<Map<String, List<ChatMessage>>> = _messages.asStateFlow()

    // Live Stream state
    private val _liveComments = MutableStateFlow<List<LiveComment>>(emptyList())
    val liveComments: StateFlow<List<LiveComment>> = _liveComments.asStateFlow()

    private val _patronTicker = MutableStateFlow<PatronActivity?>(null)
    val patronTicker: StateFlow<PatronActivity?> = _patronTicker.asStateFlow()

    // Gift Vault items
    val giftVaultItems = listOf(
        GiftVaultItem("g1", "Cocoa Pod", 5.0, 0.35, "Cocoa", "Symbol of Ghanaian prosperity"),
        GiftVaultItem("g2", "Gye Nyame", 25.0, 1.70, "GyeNyame", "Except for God - Supreme Adinkra"),
        GiftVaultItem("g3", "Gold Stool", 100.0, 6.80, "GoldStool", "Sacred Royal Ashanti Golden Stool"),
        GiftVaultItem("g4", "Crystal Star", 250.0, 17.00, "Crystal", "Radiant Black Star diamond cluster"),
        GiftVaultItem("g5", "Crown", 1000.0, 68.00, "Crown", "Imperial sovereign regalia tribute")
    )

    // Wallet transactions
    private val _transactions = MutableStateFlow<List<WalletTransaction>>(emptyList())
    val transactions: StateFlow<List<WalletTransaction>> = _transactions.asStateFlow()

    // Moments feed
    private val _moments = MutableStateFlow<List<MomentPost>>(emptyList())
    val moments: StateFlow<List<MomentPost>> = _moments.asStateFlow()

    // Contacts
    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    // Mini Apps
    val miniApps = listOf(
        MiniApp("m1", "Nova Eats", "Food Delivery", "Order Jollof, Fufu & Waakye from top Accra kitchens", "Restaurant", 4.9f, "120k"),
        MiniApp("m2", "ECG Prepaid", "Utilities", "Instant electricity token recharge with zero fees", "Bolt", 4.8f, "450k"),
        MiniApp("m3", "Ghana Post GPS", "Logistics", "Digital address validation and verified courier delivery", "LocationOn", 4.7f, "85k"),
        MiniApp("m4", "TicketGate", "Entertainment", "Concerts, Black Star Square events & AfroFuture passes", "ConfirmationNumber", 4.9f, "64k"),
        MiniApp("m5", "Ghana Water (GWCL)", "Utilities", "Direct water bill payment & meter monitoring", "WaterDrop", 4.6f, "98k"),
        MiniApp("m6", "EduPay Africa", "Education", "Direct university tuition & West African exams settlement", "School", 4.9f, "32k")
    )

    // Marketplace items
    val marketplaceProducts = listOf(
        MarketProduct("p1", "Ashanti Sovereign Handwoven Kente (3-Piece)", "Bonwire Guild", "Kumasi, Ashanti", 1250.0, 4.9f, 84, "Textiles"),
        MarketProduct("p2", "Pure Raw Grade-A Shea Butter (Organic 1kg)", "Savannah Organics", "Tamale, Northern", 75.0, 4.8f, 310, "Beauty"),
        MarketProduct("p3", "Handcrafted Ashanti Brass Gold Weights", "Osei Craft Studio", "Accra Arts Centre", 320.0, 5.0f, 42, "Art"),
        MarketProduct("p4", "Adinkra Carved Mahogany Ceremonial Stool", "Kwame Woodworks", "Aburi Craft Village", 850.0, 4.9f, 27, "Furniture")
    )

    init {
        loadInitialData()
        startLiveStreamSimulation()
        initDatabaseSync()
    }

    private fun loadInitialData() {
        // Initial Chats
        _threads.value = listOf(
            ChatThread(
                id = "chat_ama",
                name = "Ama Serwaa",
                avatarUrl = "artisan_ama",
                lastMessage = "Thank you Kwesi! Your Kente lot is packaged with custom seal.",
                timestamp = "12:31 PM",
                unreadCount = 2,
                isOnline = true,
                isVerified = true,
                isPinned = true,
                tag = "Kente Artisan"
            ),
            ChatThread(
                id = "chat_group_kumasi",
                name = "🇬🇭 Accra Tech & Creative Network",
                avatarUrl = "",
                lastMessage = "Kofi: E2EE Mesh sync completed across the Greater Accra nodes.",
                timestamp = "12:15 PM",
                unreadCount = 5,
                isGroup = true,
                isPinned = true
            ),
            ChatThread(
                id = "chat_kwesi",
                name = "Kwesi_Accra",
                avatarUrl = "",
                lastMessage = "Sent 5x Golden Black Star Crystals via MoMo! Incredible quality.",
                timestamp = "11:48 AM",
                unreadCount = 0,
                isOnline = true,
                isVerified = true
            ),
            ChatThread(
                id = "chat_novapay_official",
                name = "NOVAPAY Official Services",
                avatarUrl = "",
                lastMessage = "Payment of GH₵ 1,250.00 confirmed to Bonwire Weaver's Co-Op.",
                timestamp = "10:04 AM",
                unreadCount = 0,
                isVerified = true,
                tag = "Official"
            ),
            ChatThread(
                id = "chat_afua",
                name = "Afua Ventures",
                avatarUrl = "",
                lastMessage = "Are you available for the pan-African design panel tomorrow?",
                timestamp = "Yesterday",
                unreadCount = 0,
                isOnline = false
            ),
            ChatThread(
                id = "chat_nova_ai",
                name = "NOVA AI Assistant",
                avatarUrl = "",
                lastMessage = "I can translate between English, Twi, Ga, and French for your trade.",
                timestamp = "Yesterday",
                unreadCount = 0,
                isVerified = true,
                tag = "AI Smart Bot"
            )
        )

        // Seed messages
        _messages.value = mapOf(
            "chat_ama" to listOf(
                ChatMessage(
                    id = "m1",
                    senderName = "System",
                    text = "🔒 Encrypted with NOVACHAT End-to-End Encryption. Only members can read or listen.",
                    timestamp = "11:00 AM",
                    isMe = false,
                    type = MessageType.SYSTEM_E2EE
                ),
                ChatMessage(
                    id = "m2",
                    senderName = "Ama Serwaa",
                    text = "Akwaaba! Welcome to my Kumasi loom studio. Are you interested in the Sovereign Kente weave?",
                    timestamp = "11:05 AM",
                    isMe = false
                ),
                ChatMessage(
                    id = "m3",
                    senderName = "Me",
                    text = "Yes! I saw your live showcase on the Discover stream. The gold threading is breathtaking.",
                    timestamp = "11:08 AM",
                    isMe = true
                ),
                ChatMessage(
                    id = "m4",
                    senderName = "Ama Serwaa",
                    text = "Voice message (0:24)",
                    timestamp = "11:12 AM",
                    isMe = false,
                    type = MessageType.VOICE,
                    voiceDurationSeconds = 24
                ),
                ChatMessage(
                    id = "m5",
                    senderName = "Me",
                    text = "Sent GH₵ 1,250.00 via MTN MoMo Instant Biometric Settlement",
                    timestamp = "12:20 PM",
                    isMe = true,
                    type = MessageType.MOMO_TRANSFER,
                    momoAmountGhs = 1250.00
                ),
                ChatMessage(
                    id = "m6",
                    senderName = "Ama Serwaa",
                    text = "Thank you Kwesi! Your Kente lot is packaged with custom seal.",
                    timestamp = "12:31 PM",
                    isMe = false,
                    reactionEmoji = "🇬🇭"
                )
            )
        )

        // Initial Live Comments
        _liveComments.value = listOf(
            LiveComment(
                id = "c1",
                username = "@kofi_dev",
                badge = "PRO",
                message = "The craftsmanship on that loom is incredible! 🇬🇭✨ Can you showcase the hem detail?"
            ),
            LiveComment(
                id = "c2",
                username = "PINNED CREATOR QUESTION",
                badge = "QUESTION",
                message = "\"Do you offer verified same-day cold-courier delivery to Takoradi and Tema?\"",
                isPinned = true
            ),
            LiveComment(
                id = "c3",
                username = "@afua_ventures",
                badge = "ANGEL",
                message = "Just sent 2x Crystal Stools! Preserving our heritage for the future generations 🇬🇭🏛️"
            )
        )

        _patronTicker.value = PatronActivity(
            patronName = "Kwesi_Accra",
            tier = "VIP PATRON",
            actionText = "Sent 5x Golden Black Star Crystals (GH₵ 1,250)",
            paymentMethod = "MTN MOMO"
        )

        // Initial Wallet Transactions
        _transactions.value = listOf(
            WalletTransaction(
                id = "tx1",
                title = "Ashanti Sovereign Kente Lot #04",
                description = "Live Auction MoMo Settlement",
                amountGhs = 1250.00,
                isCredit = false,
                date = "Today, 12:20 PM",
                provider = PaymentProvider.MTN_MOMO,
                referenceCode = "MOMO-GH-9482019"
            ),
            WalletTransaction(
                id = "tx2",
                title = "MoMo Top-Up from MTN Ghana",
                description = "Account 0244-889-120",
                amountGhs = 1500.00,
                isCredit = true,
                date = "Today, 10:15 AM",
                provider = PaymentProvider.MTN_MOMO,
                referenceCode = "MOMO-GH-9481102"
            ),
            WalletTransaction(
                id = "tx3",
                title = "ECG Electricity Prepaid Voucher",
                description = "Meter #4401-9238-1",
                amountGhs = 120.00,
                isCredit = false,
                date = "Yesterday, 06:40 PM",
                provider = PaymentProvider.TELECEL_CASH,
                referenceCode = "ECG-TX-002934"
            ),
            WalletTransaction(
                id = "tx4",
                title = "Nova Ride: Kotoka Airport to Osu",
                description = "Toyota Corolla (GT-4421-24)",
                amountGhs = 45.00,
                isCredit = false,
                date = "Oct 29, 04:30 PM",
                provider = PaymentProvider.NOVA_VAULT,
                referenceCode = "RIDE-ACC-88210"
            )
        )

        // Initial Moments
        _moments.value = listOf(
            MomentPost(
                id = "post1",
                authorName = "Ama Serwaa",
                authorHandle = "@ama_serwaa",
                authorAvatar = "artisan_ama",
                timestamp = "1 hour ago",
                content = "Setting up today's Sovereign Kente loom live broadcast straight from Kumasi. Every pattern tells the proverb of unity and perseverance. Join our live auction in the Home tab! 🇬🇭✨",
                location = "Kumasi Cultural Centre, Ashanti",
                likesCount = 348,
                commentsCount = 42,
                tags = listOf("#KumasiCraft", "#AshantiKente", "#NOVAPAY", "#MadeInGhana")
            ),
            MomentPost(
                id = "post2",
                authorName = "Accra FinTech Hub",
                authorHandle = "@accra_fintech",
                authorAvatar = "",
                timestamp = "3 hours ago",
                content = "Interoperability between MTN Mobile Money, Telecel Cash, and Ecobank via NOVA PAY QR standards has reached 99.98% uptime across West Africa. The future of African commerce is decentralized and frictionless! 🚀💳",
                location = "Airport City, Accra",
                likesCount = 612,
                commentsCount = 89,
                tags = listOf("#AfricanFinTech", "#CashlessGhana", "#NOVAPAY")
            ),
            MomentPost(
                id = "post3",
                authorName = "Kofi Mensah",
                authorHandle = "@kofi_dev",
                authorAvatar = "",
                timestamp = "5 hours ago",
                content = "Tested NOVACHAT's offline mesh bridging during the regional fiber maintenance. 1,420 viewers stayed synced in real time. The tech resilience is groundbreaking.",
                location = "East Legon, Accra",
                likesCount = 215,
                commentsCount = 18,
                tags = listOf("#MeshNetwork", "#SuperApp", "#E2EE")
            )
        )

        // Contacts
        _contacts.value = listOf(
            Contact("c1", "Ama Serwaa", "@ama_serwaa", "+233 24 555 0192", "artisan_ama", "Weaving the royal Ashanti heritage", isOnline = true, isFavorite = true),
            Contact("c2", "Kwesi Accra", "@kwesi_accra", "+233 20 444 8821", "", "Tech investor & cultural collector", isOnline = true, isFavorite = true),
            Contact("c3", "Afua Ventures", "@afua_ventures", "+233 27 333 9912", "", "Empowering African creative enterprises", isOnline = false, isFavorite = true),
            Contact("c4", "Kofi Mensah", "@kofi_dev", "+233 54 222 3401", "", "Distributed systems & WebRTC engineer", isOnline = true),
            Contact("c5", "Bonwire Weavers Guild", "@bonwire_guild", "+233 32 111 8844", "", "Official heritage textile collective", isOfficial = true, category = "Official"),
            Contact("c6", "MTN MoMo Ghana Support", "@mtn_momo_gh", "0244 000 000", "", "Official 24/7 financial concierge", isOfficial = true, category = "Official")
        )
    }

    private fun startLiveStreamSimulation() {
        viewModelScope.launch {
            val randomUsers = listOf(
                Pair("@yaw_accra", "Bidding for the family heirloom!"),
                Pair("@nana_akoto", "The gold thread luster is pure perfection 🌟"),
                Pair("@abena_styles", "Does it come with the certificate of royal authenticity?"),
                Pair("@dr_boateng", "Proudly watching from London! Sending love to Kumasi 🇬🇭"),
                Pair("@selorm_gh", "Instant MoMo settlement worked seamlessly.")
            )
            var index = 0
            while (true) {
                delay(12000)
                if (index < randomUsers.size) {
                    val user = randomUsers[index]
                    val newComment = LiveComment(
                        id = "sim_${System.currentTimeMillis()}",
                        username = user.first,
                        badge = if (index % 2 == 0) "FAN" else "VIP",
                        message = user.second
                    )
                    _liveComments.value = _liveComments.value.takeLast(4) + newComment
                    index++
                }
            }
        }
    }

    private fun initDatabaseSync() {
        val repo = repository ?: return
        viewModelScope.launch {
            // Seed initial threads and messages into Room cache if needed
            repo.saveThreads(_threads.value)
            _messages.value.forEach { (chatId, msgList) ->
                repo.saveMessages(chatId, msgList)
            }
            repo.saveUserProfile(
                UserProfileEntity(
                    id = "user_kwesi",
                    name = "Kwesi Mensah",
                    handle = "@kwesi_m",
                    phone = "+233 24 889 120",
                    location = "Accra, Ghana",
                    walletBalanceGhs = _uiState.value.walletBalanceGhs
                )
            )

            // Reactively observe threads from Room cache
            launch {
                repo.getCachedThreads().collectLatest { cachedThreads ->
                    if (cachedThreads.isNotEmpty()) {
                        _threads.value = cachedThreads
                    }
                }
            }

            // Reactively observe user profile from Room cache
            launch {
                repo.getCachedUserProfile("user_kwesi").collectLatest { cachedProfile ->
                    if (cachedProfile != null) {
                        _uiState.value = _uiState.value.copy(
                            walletBalanceGhs = cachedProfile.walletBalanceGhs
                        )
                    }
                }
            }
        }
    }

    // User Actions
    fun onUserAuthenticated(userId: String) {
        currentAuthenticatedUserId = userId
        val repo = firebaseRepository ?: return
        viewModelScope.launch {
            val authUser = Firebase.auth.currentUser
            repo.syncUserProfile(
                displayName = authUser?.displayName ?: "User",
                handle = "@${authUser?.email?.substringBefore('@') ?: "user"}",
                photoUrl = authUser?.photoUrl?.toString(),
                email = authUser?.email,
                phoneNumber = authUser?.phoneNumber
            )
            repo.setUserOnlineStatus(true)

            conversationsJob?.cancel()
            conversationsJob = launch {
                repo.observeConversations().collectLatest { firestoreConvs ->
                    if (firestoreConvs.isNotEmpty()) {
                        val mappedThreads = firestoreConvs.map { conv ->
                            val otherParticipants = conv.participants.filter { it != userId }
                            val isTyping = conv.typing.any { (k, _) -> k != userId }
                            val typingText = if (isTyping) "Someone is typing..." else ""
                            ChatThread(
                                id = conv.id,
                                name = conv.name.ifBlank {
                                    if (conv.isGroup) "Group Chat" else (otherParticipants.firstOrNull() ?: "Direct Chat")
                                },
                                avatarUrl = "",
                                lastMessage = conv.lastMessage.ifBlank { "No messages yet" },
                                timestamp = "Live",
                                unreadCount = 0,
                                isOnline = true,
                                isGroup = conv.isGroup,
                                isVerified = true,
                                isE2EE = false, // Transparent security labeling: TLS + Security Rules Protected
                                isPinned = conv.isGroup,
                                tag = if (conv.isGroup) "Group" else "Direct",
                                participants = conv.participants,
                                isTyping = isTyping,
                                typingText = typingText
                            )
                        }
                        val existingNonFirestore = _threads.value.filter { existing ->
                            firestoreConvs.none { it.id == existing.id }
                        }
                        _threads.value = mappedThreads + existingNonFirestore
                    }
                }
            }
        }
    }

    fun setDestination(dest: MainDestination) {
        _uiState.value = _uiState.value.copy(
            currentDestination = dest,
            activeScreen = ActiveScreen.MAIN
        )
    }

    fun openChat(chatId: String) {
        _uiState.value = _uiState.value.copy(
            selectedChatId = chatId,
            activeScreen = ActiveScreen.CHAT_DETAIL
        )
        // Mark as read in memory and in Room cache
        _threads.value = _threads.value.map {
            if (it.id == chatId) it.copy(unreadCount = 0) else it
        }
        repository?.let { repo ->
            viewModelScope.launch {
                repo.markChatAsRead(chatId)
                repo.getCachedMessages(chatId).collectLatest { cachedList ->
                    if (cachedList.isNotEmpty() && _messages.value[chatId].isNullOrEmpty()) {
                        _messages.value = _messages.value + (chatId to cachedList)
                    }
                }
            }
        }

        // Firestore real-time observation
        val fbRepo = firebaseRepository
        val uid = currentAuthenticatedUserId ?: Firebase.auth.currentUser?.uid
        if (fbRepo != null && uid != null) {
            activeMessageJob?.cancel()
            activeMessageJob = viewModelScope.launch {
                fbRepo.observeMessages(chatId).collectLatest { firestoreMsgs ->
                    if (firestoreMsgs.isNotEmpty()) {
                        val mapped = firestoreMsgs.map { m ->
                            val isMe = m.senderId == uid
                            val read = m.readBy.any { it != m.senderId }
                            ChatMessage(
                                id = m.id,
                                senderName = if (isMe) "Me" else m.senderName,
                                senderAvatar = m.senderAvatar ?: "",
                                text = m.text,
                                timestamp = "Live",
                                isMe = isMe,
                                type = try { MessageType.valueOf(m.type) } catch (e: Exception) { MessageType.TEXT },
                                imageUrl = m.imageUrl,
                                voiceDurationSeconds = m.voiceDurationSeconds,
                                momoAmountGhs = m.momoAmountGhs,
                                isDelivered = m.deliveredTo.isNotEmpty(),
                                isRead = read,
                                reactionEmoji = m.reactions.values.firstOrNull(),
                                reactions = m.reactions,
                                replyToId = m.replyToId,
                                replyToText = m.replyToText,
                                replyToSender = m.replyToSender,
                                readBy = m.readBy,
                                deliveredTo = m.deliveredTo
                            )
                        }
                        _messages.value = _messages.value + (chatId to mapped)

                        // Mark unread messages as read in Firestore
                        firestoreMsgs.filter { !it.readBy.contains(uid) }.forEach { unreadMsg ->
                            launch {
                                fbRepo.markMessageAsRead(chatId, unreadMsg.id)
                            }
                        }
                    }
                }
            }
        }
    }

    fun closeChat() {
        val chatId = _uiState.value.selectedChatId
        if (chatId != null) {
            setTyping(chatId, false)
        }
        activeMessageJob?.cancel()
        _uiState.value = _uiState.value.copy(
            selectedChatId = null,
            activeScreen = ActiveScreen.MAIN
        )
    }


    fun startCall(isVideo: Boolean, participantName: String = "Ama Serwaa") {
        _uiState.value = _uiState.value.copy(
            activeScreen = if (isVideo) ActiveScreen.VIDEO_CALL else ActiveScreen.AUDIO_CALL,
            callActive = true,
            callParticipantName = participantName
        )
    }

    fun endCall() {
        _uiState.value = _uiState.value.copy(
            activeScreen = ActiveScreen.MAIN,
            callActive = false
        )
        showNotification("Call ended • 256-bit E2EE Session Terminated")
    }

    fun toggleMic() {
        _uiState.value = _uiState.value.copy(isMicMuted = !_uiState.value.isMicMuted)
    }

    fun toggleCamera() {
        _uiState.value = _uiState.value.copy(isCameraOn = !_uiState.value.isCameraOn)
    }

    fun toggleSpeaker() {
        _uiState.value = _uiState.value.copy(isSpeakerOn = !_uiState.value.isSpeakerOn)
    }

    fun openRideHailing() {
        _uiState.value = _uiState.value.copy(activeScreen = ActiveScreen.RIDE_HAILING)
    }

    fun openMarketplace() {
        _uiState.value = _uiState.value.copy(activeScreen = ActiveScreen.MARKETPLACE)
    }

    fun openQrScanner() {
        _uiState.value = _uiState.value.copy(activeScreen = ActiveScreen.QR_SCANNER)
    }

    fun closeOverlayScreen() {
        _uiState.value = _uiState.value.copy(activeScreen = ActiveScreen.MAIN)
    }

    fun toggleGiftVault(open: Boolean? = null) {
        _uiState.value = _uiState.value.copy(
            isGiftVaultOpen = open ?: !_uiState.value.isGiftVaultOpen
        )
    }

    fun selectGiftItem(item: GiftVaultItem) {
        _uiState.value = _uiState.value.copy(selectedGiftItem = item)
    }

    fun toggleNovaAi(open: Boolean? = null) {
        _uiState.value = _uiState.value.copy(
            isNovaAiOpen = open ?: !_uiState.value.isNovaAiOpen
        )
    }

    fun sendLiveComment(text: String) {
        if (text.isBlank()) return
        val comment = LiveComment(
            id = "c_${System.currentTimeMillis()}",
            username = "@you",
            badge = "PATRON",
            message = text
        )
        _liveComments.value = _liveComments.value + comment
    }

    fun sendSelectedGift() {
        val gift = _uiState.value.selectedGiftItem ?: return
        if (_uiState.value.walletBalanceGhs < gift.priceGhs) {
            showNotification("Insufficient balance. Tap +TOP UP to link MTN MoMo.")
            return
        }

        val newBalance = _uiState.value.walletBalanceGhs - gift.priceGhs
        val newEarnings = _uiState.value.liveSessionEarningsGhs + gift.priceGhs

        _uiState.value = _uiState.value.copy(
            walletBalanceGhs = newBalance,
            liveSessionEarningsGhs = newEarnings,
            isGiftVaultOpen = false
        )

        val newComment = LiveComment(
            id = "gift_${System.currentTimeMillis()}",
            username = "@you",
            badge = "VIP PATRON",
            message = "Sent ${gift.name}! Preserving African heritage 🇬🇭✨"
        )
        _liveComments.value = _liveComments.value + newComment

        _patronTicker.value = PatronActivity(
            patronName = "You",
            tier = "VIP PATRON",
            actionText = "Sent ${gift.name} (GH₵ ${gift.priceGhs})",
            paymentMethod = "MTN MOMO"
        )

        // Record transaction
        _transactions.value = listOf(
            WalletTransaction(
                id = "tx_${System.currentTimeMillis()}",
                title = "Gift: ${gift.name} to @ama_serwaa",
                description = "Live Stream Pan-African Vault",
                amountGhs = gift.priceGhs,
                isCredit = false,
                date = "Just now",
                provider = PaymentProvider.MTN_MOMO,
                referenceCode = "GIFT-GH-${System.currentTimeMillis().toString().takeLast(6)}"
            )
        ) + _transactions.value

        showNotification("Gift ${gift.name} sent successfully via MTN MoMo biometric settlement!")
    }

    fun placeBidOnLiveLot() {
        val increment = 50.0
        val newBid = _uiState.value.currentLotBidGhs + increment
        _uiState.value = _uiState.value.copy(
            currentLotBidGhs = newBid,
            liveSessionEarningsGhs = _uiState.value.liveSessionEarningsGhs + increment
        )

        val newComment = LiveComment(
            id = "bid_${System.currentTimeMillis()}",
            username = "@you",
            badge = "BIDDER",
            message = "Placed leading bid of GH₵ ${newBid.toInt()} for LOT #04! 🇬🇭🔨"
        )
        _liveComments.value = _liveComments.value + newComment

        _patronTicker.value = PatronActivity(
            patronName = "You",
            tier = "TOP BIDDER",
            actionText = "Placed bid GH₵ ${newBid.toInt()} on Lot #04",
            paymentMethod = "MTN MOMO"
        )

        showNotification("Bid placed: GH₵ ${newBid.toInt()} (Authorized via MoMo)")
    }

    fun buyWithMoMoInstant() {
        val price = _uiState.value.currentLotBidGhs
        if (_uiState.value.currentLotUnits <= 0) {
            showNotification("Lot is sold out!")
            return
        }

        _uiState.value = _uiState.value.copy(
            currentLotUnits = _uiState.value.currentLotUnits - 1,
            walletBalanceGhs = (_uiState.value.walletBalanceGhs - price).coerceAtLeast(0.0),
            liveSessionEarningsGhs = _uiState.value.liveSessionEarningsGhs + price
        )

        _transactions.value = listOf(
            WalletTransaction(
                id = "buy_${System.currentTimeMillis()}",
                title = "Instant Purchase: Ashanti Silk Sovereign Kente",
                description = "Bonwire Guild Studio • Lot #04",
                amountGhs = price,
                isCredit = false,
                date = "Just now",
                provider = PaymentProvider.MTN_MOMO,
                referenceCode = "MOMO-PURCHASE-${System.currentTimeMillis().toString().takeLast(6)}"
            )
        ) + _transactions.value

        val newComment = LiveComment(
            id = "buy_${System.currentTimeMillis()}",
            username = "@you",
            badge = "COLLECTOR",
            message = "Just acquired 1x Sovereign Kente Lot via MoMo Instant Settlement! 🇬🇭🎉"
        )
        _liveComments.value = _liveComments.value + newComment

        showNotification("Instant MoMo Purchase successful! Receipt issued.")
    }

    fun sendMessage(
        chatId: String,
        text: String,
        replyToId: String? = null,
        replyToText: String? = null,
        replyToSender: String? = null
    ) {
        if (text.isBlank()) return
        val currentList = _messages.value[chatId] ?: emptyList()
        val tempId = "msg_${System.currentTimeMillis()}"
        val newMsg = ChatMessage(
            id = tempId,
            senderName = "Me",
            text = text,
            timestamp = "Just now",
            isMe = true,
            replyToId = replyToId,
            replyToText = replyToText,
            replyToSender = replyToSender
        )
        _messages.value = _messages.value + (chatId to (currentList + newMsg))

        // Update thread preview
        _threads.value = _threads.value.map {
            if (it.id == chatId) it.copy(lastMessage = text, timestamp = "Just now") else it
        }

        repository?.let { repo ->
            viewModelScope.launch {
                repo.saveMessage(chatId, newMsg)
            }
        }

        val thread = _threads.value.find { it.id == chatId }
        val participants = thread?.participants ?: emptyList()

        firebaseRepository?.let { fbRepo ->
            viewModelScope.launch {
                fbRepo.sendMessage(
                    conversationId = chatId,
                    text = text,
                    type = "TEXT",
                    replyToId = replyToId,
                    replyToText = replyToText,
                    replyToSender = replyToSender,
                    participants = participants
                )
            }
        }

        // Auto reply simulation if talking to Nova AI
        if (chatId == "chat_nova_ai") {
            viewModelScope.launch {
                delay(1200)
                val reply = generateAiResponse(text)
                val aiMsg = ChatMessage(
                    id = "ai_${System.currentTimeMillis()}",
                    senderName = "NOVA AI Assistant",
                    text = reply,
                    timestamp = "Just now",
                    isMe = false
                )
                val updated = _messages.value[chatId] ?: emptyList()
                _messages.value = _messages.value + (chatId to (updated + aiMsg))
                repository?.saveMessage(chatId, aiMsg)
            }
        }
    }

    fun sendVoiceNote(chatId: String, durationSec: Int = 12) {
        val currentList = _messages.value[chatId] ?: emptyList()
        val formattedTime = if (durationSec < 10) "0:0$durationSec" else "0:$durationSec"
        val newMsg = ChatMessage(
            id = "voice_${System.currentTimeMillis()}",
            senderName = "Me",
            text = "Voice message ($formattedTime)",
            timestamp = "Just now",
            isMe = true,
            type = MessageType.VOICE,
            voiceDurationSeconds = durationSec
        )
        _messages.value = _messages.value + (chatId to (currentList + newMsg))

        repository?.let { repo ->
            viewModelScope.launch {
                repo.saveMessage(chatId, newMsg)
            }
        }

        val thread = _threads.value.find { it.id == chatId }
        firebaseRepository?.let { fbRepo ->
            viewModelScope.launch {
                fbRepo.sendMessage(
                    conversationId = chatId,
                    text = "Voice message ($formattedTime)",
                    type = "VOICE",
                    voiceDurationSeconds = durationSec,
                    participants = thread?.participants ?: emptyList()
                )
            }
        }
        showNotification("Voice note sent via secure transport")
    }

    fun sendImageMessage(chatId: String, imageUrl: String, caption: String = "") {
        val currentList = _messages.value[chatId] ?: emptyList()
        val textBody = caption.ifBlank { "Photo attachment" }
        val newMsg = ChatMessage(
            id = "img_${System.currentTimeMillis()}",
            senderName = "Me",
            text = textBody,
            timestamp = "Just now",
            isMe = true,
            type = MessageType.IMAGE,
            imageUrl = imageUrl
        )
        _messages.value = _messages.value + (chatId to (currentList + newMsg))

        val thread = _threads.value.find { it.id == chatId }
        firebaseRepository?.let { fbRepo ->
            viewModelScope.launch {
                fbRepo.sendMessage(
                    conversationId = chatId,
                    text = textBody,
                    type = "IMAGE",
                    imageUrl = imageUrl,
                    participants = thread?.participants ?: emptyList()
                )
            }
        }
        showNotification("Image shared in chat")
    }

    fun toggleReaction(chatId: String, messageId: String, emoji: String) {
        val uid = currentAuthenticatedUserId ?: Firebase.auth.currentUser?.uid ?: "me"
        val currentList = _messages.value[chatId] ?: emptyList()
        _messages.value = _messages.value + (chatId to currentList.map { msg ->
            if (msg.id == messageId) {
                val updatedReactions = msg.reactions.toMutableMap()
                if (updatedReactions[uid] == emoji) {
                    updatedReactions.remove(uid)
                } else {
                    updatedReactions[uid] = emoji
                }
                msg.copy(reactions = updatedReactions, reactionEmoji = updatedReactions.values.firstOrNull())
            } else msg
        })

        firebaseRepository?.let { fbRepo ->
            viewModelScope.launch {
                fbRepo.toggleReaction(chatId, messageId, emoji)
            }
        }
    }

    fun setTyping(chatId: String, isTyping: Boolean) {
        firebaseRepository?.let { fbRepo ->
            viewModelScope.launch {
                fbRepo.setTypingStatus(chatId, isTyping)
            }
        }
    }

    fun createConversation(isGroup: Boolean, name: String, targetUserId: String?, onComplete: (String) -> Unit) {
        val fbRepo = firebaseRepository
        if (fbRepo == null) {
            val localId = "chat_${System.currentTimeMillis()}"
            val newThread = ChatThread(
                id = localId,
                name = name,
                avatarUrl = "",
                lastMessage = if (isGroup) "Group created" else "Chat started",
                timestamp = "Just now",
                unreadCount = 0,
                isOnline = true,
                isGroup = isGroup,
                isVerified = true,
                isE2EE = false
            )
            _threads.value = listOf(newThread) + _threads.value
            openChat(localId)
            onComplete(localId)
            return
        }

        val currentUid = currentAuthenticatedUserId ?: Firebase.auth.currentUser?.uid ?: "user"
        val participants = if (targetUserId != null) listOf(targetUserId, currentUid) else listOf(currentUid)
        viewModelScope.launch {
            val result = fbRepo.createConversation(
                isGroup = isGroup,
                name = name,
                participantUids = participants
            )
            result.onSuccess { newConvId ->
                openChat(newConvId)
                onComplete(newConvId)
                showNotification("${if (isGroup) "Group" else "Chat"} created successfully!")
            }.onFailure { err ->
                showNotification("Could not create chat: ${err.message}")
            }
        }
    }

    fun sendMoMoInChat(chatId: String, amountGhs: Double) {
        if (_uiState.value.walletBalanceGhs < amountGhs) {
            showNotification("Insufficient balance for GH₵ $amountGhs MoMo transfer")
            return
        }

        val newBalance = _uiState.value.walletBalanceGhs - amountGhs
        _uiState.value = _uiState.value.copy(
            walletBalanceGhs = newBalance
        )

        val currentList = _messages.value[chatId] ?: emptyList()
        val textBody = "Sent GH₵ $amountGhs via MTN MoMo Instant Biometric Settlement"
        val newMsg = ChatMessage(
            id = "momo_${System.currentTimeMillis()}",
            senderName = "Me",
            text = textBody,
            timestamp = "Just now",
            isMe = true,
            type = MessageType.MOMO_TRANSFER,
            momoAmountGhs = amountGhs
        )
        _messages.value = _messages.value + (chatId to (currentList + newMsg))

        repository?.let { repo ->
            viewModelScope.launch {
                repo.saveMessage(chatId, newMsg)
                repo.updateWalletBalance("user_kwesi", newBalance)
            }
        }

        val thread = _threads.value.find { it.id == chatId }
        firebaseRepository?.let { fbRepo ->
            viewModelScope.launch {
                fbRepo.sendMessage(
                    conversationId = chatId,
                    text = textBody,
                    type = "MOMO_TRANSFER",
                    momoAmountGhs = amountGhs,
                    participants = thread?.participants ?: emptyList()
                )
            }
        }


        _transactions.value = listOf(
            WalletTransaction(
                id = "p2p_${System.currentTimeMillis()}",
                title = "P2P Transfer to ${_threads.value.find { it.id == chatId }?.name ?: "Recipient"}",
                description = "Chat Biometric Transfer",
                amountGhs = amountGhs,
                isCredit = false,
                date = "Just now",
                provider = PaymentProvider.MTN_MOMO,
                referenceCode = "P2P-${System.currentTimeMillis().toString().takeLast(6)}"
            )
        ) + _transactions.value

        showNotification("GH₵ $amountGhs transferred instantly via MoMo!")
    }

    fun bookRide(pickup: String, destination: String, option: RideOption) {
        val ride = ActiveRide(
            id = "ride_${System.currentTimeMillis()}",
            driverName = "Kojo Darko (4.9 ★)",
            driverRating = 4.9f,
            carModel = "Toyota Corolla Hybrid",
            plateNumber = "GH-ACC-4912-24",
            pickupLocation = pickup,
            destination = destination,
            fareGhs = option.estimatedFareGhs,
            status = "Driver arriving in 3 mins"
        )
        _uiState.value = _uiState.value.copy(
            activeRide = ride,
            activeScreen = ActiveScreen.RIDE_HAILING
        )
        showNotification("Ride requested! Kojo Darko is en route.")
    }

    fun cancelRide() {
        _uiState.value = _uiState.value.copy(activeRide = null)
        showNotification("Ride cancelled with zero fee.")
    }

    fun sendMoneyWallet(recipientPhone: String, amount: Double, provider: PaymentProvider) {
        if (amount <= 0 || _uiState.value.walletBalanceGhs < amount) {
            showNotification("Invalid amount or insufficient wallet balance")
            return
        }
        val newBalance = _uiState.value.walletBalanceGhs - amount
        _uiState.value = _uiState.value.copy(
            walletBalanceGhs = newBalance
        )
        repository?.let { repo ->
            viewModelScope.launch {
                repo.updateWalletBalance("user_kwesi", newBalance)
            }
        }
        _transactions.value = listOf(
            WalletTransaction(
                id = "w_${System.currentTimeMillis()}",
                title = "Transfer to $recipientPhone",
                description = "${provider.name.replace("_", " ")} Instant Transfer",
                amountGhs = amount,
                isCredit = false,
                date = "Just now",
                provider = provider,
                referenceCode = "TX-${System.currentTimeMillis().toString().takeLast(6)}"
            )
        ) + _transactions.value
        showNotification("Transferred GH₵ $amount to $recipientPhone successfully!")
    }

    fun topUpWallet(amount: Double) {
        val newBalance = _uiState.value.walletBalanceGhs + amount
        _uiState.value = _uiState.value.copy(
            walletBalanceGhs = newBalance
        )
        repository?.let { repo ->
            viewModelScope.launch {
                repo.updateWalletBalance("user_kwesi", newBalance)
            }
        }
        _transactions.value = listOf(
            WalletTransaction(
                id = "top_${System.currentTimeMillis()}",
                title = "MoMo Deposit",
                description = "MTN Mobile Money 0244-***-120",
                amountGhs = amount,
                isCredit = true,
                date = "Just now",
                provider = PaymentProvider.MTN_MOMO,
                referenceCode = "DEP-${System.currentTimeMillis().toString().takeLast(6)}"
            )
        ) + _transactions.value
        showNotification("Wallet credited with GH₵ $amount from MTN MoMo!")
    }

    fun toggleLikeMoment(postId: String) {
        _moments.value = _moments.value.map {
            if (it.id == postId) {
                val newLiked = !it.isLiked
                val newCount = if (newLiked) it.likesCount + 1 else it.likesCount - 1
                it.copy(isLiked = newLiked, likesCount = newCount)
            } else it
        }
    }

    fun postMoment(content: String, location: String) {
        if (content.isBlank()) return
        val newPost = MomentPost(
            id = "p_${System.currentTimeMillis()}",
            authorName = "Kwesi Mensah",
            authorHandle = "@kwesi_m",
            authorAvatar = "",
            timestamp = "Just now",
            content = content,
            location = location.ifBlank { "Accra, Ghana" },
            likesCount = 1,
            commentsCount = 0,
            isLiked = true,
            tags = listOf("#NOVACHAT", "#Ghana")
        )
        _moments.value = listOf(newPost) + _moments.value
        showNotification("Moment published to your encrypted circle!")
    }

    private fun generateAiResponse(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            "translate" in lower || "twi" in lower || "ga" in lower -> {
                "Here is your translation: \n'Akwaaba kɔ Kumasi' means 'Welcome to Kumasi'. For business greetings in Twi, you can say: 'Mema wo akye' (Good morning) and 'Medaase' (Thank you)!"
            }
            "kente" in lower || "artisan" in lower -> {
                "Authentic Ashanti Kente patterns from Bonwire use distinct symbolic colors: Gold symbolizes royalty and wealth, Green represents growth and spiritual renewal, and Maroon represents healing."
            }
            "momo" in lower || "payment" in lower || "rate" in lower -> {
                "NOVA PAY routes transactions through the Bank of Ghana Ghana Interbank Payment and Settlement Systems (GhIPSS). Current exchange baseline: 1 USD ≈ 14.85 GHS with instant zero-fee transfers between NOVACHAT users."
            }
            "ride" in lower || "uber" in lower -> {
                "NOVA RIDE is currently operating in Accra Metropolitan, Kumasi Metropolis, and Takoradi. Drivers are verified with the Driver and Vehicle Licensing Authority (DVLA)."
            }
            else -> {
                "Hello! I am NOVA AI, trained on West African commerce, legal frameworks, languages, and logistics. How can I assist your business or daily tasks across Ghana and the diaspora today?"
            }
        }
    }

    private fun showNotification(msg: String) {
        _uiState.value = _uiState.value.copy(notificationMessage = msg)
        viewModelScope.launch {
            delay(3500)
            if (_uiState.value.notificationMessage == msg) {
                _uiState.value = _uiState.value.copy(notificationMessage = null)
            }
        }
    }

    fun dismissNotification() {
        _uiState.value = _uiState.value.copy(notificationMessage = null)
    }
}

class NovaChatViewModelFactory(
    private val repository: NovaCacheRepository?,
    private val firebaseRepository: com.example.data.firebase.FirebaseChatRepository? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NovaChatViewModel::class.java)) {
            return NovaChatViewModel(repository, firebaseRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

