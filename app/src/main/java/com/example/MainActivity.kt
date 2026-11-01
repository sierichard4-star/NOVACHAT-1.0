package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ActiveScreen
import com.example.data.MainDestination
import com.example.data.NovaChatViewModel
import com.example.data.NovaChatViewModelFactory
import com.example.data.local.NovaCacheRepository
import com.example.data.local.NovaDatabase
import com.example.ui.components.NovaBottomNavigation
import com.example.ui.components.NovaTopBar
import com.example.ui.components.SecurityAuditDialog
import com.example.ui.screens.*
import com.example.ui.theme.NovaChatTheme
import com.example.ui.theme.NovaCyanAccent
import com.example.ui.theme.NovaDarkBackground
import com.example.ui.theme.NovaTextPrimary

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : ComponentActivity() {

    private val viewModel: NovaChatViewModel by viewModels {
        val database = NovaDatabase.getDatabase(applicationContext)
        val repository = NovaCacheRepository(database)
        val firebaseRepo = com.example.data.firebase.FirebaseChatRepository(applicationContext)
        NovaChatViewModelFactory(repository, firebaseRepo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NovaChatTheme {
                NovaChatApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun NovaChatApp(viewModel: NovaChatViewModel) {
    var currentUser by remember { 
        mutableStateOf<FirebaseUser?>(FirebaseAuth.getInstance().currentUser) 
    }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
            if (auth.currentUser != null) {
                viewModel.onUserAuthenticated(auth.currentUser!!.uid)
            }
        }
        val authInstance = FirebaseAuth.getInstance()
        authInstance.addAuthStateListener(listener)
        onDispose {
            authInstance.removeAuthStateListener(listener)
        }
    }

    if (currentUser == null) {
        AuthScreen(
            onAuthSuccess = {
                val user = FirebaseAuth.getInstance().currentUser
                currentUser = user
                if (user != null) {
                    viewModel.onUserAuthenticated(user.uid)
                }
            }
        )
        return
    }

    val uiState by viewModel.uiState.collectAsState()
    val threads by viewModel.threads.collectAsState()
    val unreadCount = threads.sumOf { it.unreadCount }
    var showSecurityAudit by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier

            .fillMaxSize()
            .background(NovaDarkBackground)
    ) {
        // Main Screen Scaffold (when not in full-screen overlay like Call or Chat Detail)
        Scaffold(
            topBar = {
                if (uiState.activeScreen == ActiveScreen.MAIN) {
                    NovaTopBar(
                        onSecurityClick = { showSecurityAudit = true },
                        onProfileClick = { viewModel.setDestination(MainDestination.ME) },
                        onAiClick = { viewModel.toggleNovaAi(true) }
                    )
                }
            },
            bottomBar = {
                if (uiState.activeScreen == ActiveScreen.MAIN) {
                    NovaBottomNavigation(
                        currentDestination = uiState.currentDestination,
                        onDestinationSelected = { dest ->
                            viewModel.setDestination(dest)
                        },
                        unreadChatsCount = unreadCount
                    )
                }
            },
            containerColor = NovaDarkBackground,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Main Navigation Tabs
                when (uiState.currentDestination) {
                    MainDestination.HOME -> {
                        LiveStreamScreen(
                            viewModel = viewModel,
                            uiState = uiState
                        )
                    }

                    MainDestination.CHATS -> {
                        ChatListScreen(
                            viewModel = viewModel,
                            onOpenChat = { chatId -> viewModel.openChat(chatId) }
                        )
                    }

                    MainDestination.NOVAPAY -> {
                        NovaPayScreen(viewModel = viewModel)
                    }

                    MainDestination.DISCOVER -> {
                        DiscoverScreen(viewModel = viewModel)
                    }

                    MainDestination.ME -> {
                        ProfileScreen(viewModel = viewModel)
                    }
                }
            }
        }

        // Sub-Screen Overlays (Full Screen with BackHandler)
        when (uiState.activeScreen) {
            ActiveScreen.CHAT_DETAIL -> {
                uiState.selectedChatId?.let { chatId ->
                    ChatDetailScreen(
                        chatId = chatId,
                        viewModel = viewModel,
                        onBack = { viewModel.closeChat() }
                    )
                }
            }

            ActiveScreen.AUDIO_CALL -> {
                CallScreen(
                    isVideo = false,
                    participantName = uiState.callParticipantName,
                    viewModel = viewModel,
                    onEndCall = { viewModel.endCall() }
                )
            }

            ActiveScreen.VIDEO_CALL -> {
                CallScreen(
                    isVideo = true,
                    participantName = uiState.callParticipantName,
                    viewModel = viewModel,
                    onEndCall = { viewModel.endCall() }
                )
            }

            ActiveScreen.RIDE_HAILING -> {
                NovaRideScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.closeOverlayScreen() }
                )
            }

            ActiveScreen.MARKETPLACE -> {
                MarketplaceScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.closeOverlayScreen() }
                )
            }

            ActiveScreen.QR_SCANNER -> {
                QrScannerScreen(
                    onBack = { viewModel.closeOverlayScreen() }
                )
            }

            else -> {
                // No overlay active
            }
        }

        // NOVA AI Assistant Sheet
        AnimatedVisibility(
            visible = uiState.isNovaAiOpen,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            NovaAiSheet(
                viewModel = viewModel,
                onClose = { viewModel.toggleNovaAi(false) }
            )
        }

        // In-App Notification Toast
        uiState.notificationMessage?.let { notification ->
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 16.dp, start = 20.dp, end = 20.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xF20F261D),
                border = BorderStroke(1.dp, NovaCyanAccent)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = NovaCyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = notification,
                        color = NovaTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Security Audit Dialog
        if (showSecurityAudit) {
            SecurityAuditDialog(
                onDismiss = { showSecurityAudit = false }
            )
        }
    }
}

@Composable
fun QrScannerScreen(onBack: () -> Unit) {
    BackHandler { onBack() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF040A07))
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = NovaTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Scan NOVA PAY / Contact QR",
                    color = NovaTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Surface(
                modifier = Modifier
                    .size(260.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color(0x3300E5BE),
                border = BorderStroke(2.dp, NovaCyanAccent)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Point camera at QR code\n(GhIPSS / MoMo / NOVA)",
                        color = NovaCyanAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Supports Ghana Interbank QR, MTN MoMo, Telecel Cash & Contact Invites",
                color = com.example.ui.theme.NovaTextSecondary,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
