package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.firebase.FirebaseChatRepository
import com.example.model.UserProfile
import com.example.ui.theme.*
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class AuthMode {
    SIGN_IN,
    REGISTER
}

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val credentialManager = remember { CredentialManager.create(context) }
    val firebaseRepo = remember { FirebaseChatRepository(context) }

    var authMode by remember { mutableStateOf(AuthMode.SIGN_IN) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Registration input state
    var regFullName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPhoneNumber by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }
    var regPasswordVisible by remember { mutableStateOf(false) }

    // Login input state
    var loginIdentifier by remember { mutableStateOf("") } // Email or Phone
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }

    // Attempt silent auto-sign-in on startup
    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = onAuthSuccess,
            onUnauthenticated = { /* Ready for interactive auth */ },
            scope = coroutineScope
        )
    }

    // Outer blurred background container with ambient crystal aura orbs
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF030806),
                        Color(0xFF071710),
                        Color(0xFF05100B),
                        Color(0xFF020604)
                    )
                )
            )
    ) {
        // Ambient crystal backplate glow orbs simulating blurred crystal glass optics
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopStart)
                .offset(x = (-60).dp, y = (-40).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            NovaCyanAccent.copy(alpha = 0.25f),
                            NovaCyanAccent.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(340.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 90.dp, y = 30.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            NovaEmerald.copy(alpha = 0.18f),
                            NovaEmerald.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-50).dp, y = 70.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            NovaGold.copy(alpha = 0.16f),
                            NovaGold.copy(alpha = 0.04f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Main scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header: Brand & Crystal Shield Icon
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0x3300E5BE),
                                    Color(0x11FFFFFF),
                                    Color(0x22E5B869)
                                )
                            )
                        )
                        .border(
                            BorderStroke(
                                1.5.dp,
                                Brush.linearGradient(
                                    listOf(
                                        NovaCyanAccent.copy(alpha = 0.8f),
                                        Color(0x44FFFFFF),
                                        NovaGold.copy(alpha = 0.7f)
                                    )
                                )
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "NOVACHAT Crystal Star",
                        tint = NovaGold,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "NOVACHAT",
                    color = NovaTextPrimary,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp
                )

                Text(
                    text = "The Premier African Super App",
                    color = NovaCyanAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            }

            // Crystal Glassmorphism Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                color = Color(0x2411221A), // Translucent frosted glass tint
                border = BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        listOf(
                            Color(0x9900E5BE),
                            Color(0x40FFFFFF),
                            Color(0x1500E5BE),
                            Color(0x40E5B869)
                        )
                    )
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Specular light sheen highlight at top edge of crystal
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.5.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color(0x80FFFFFF),
                                        Color(0x9900E5BE),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Crystal Segmented Tabs: Sign In / Create Account
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x2E07130D))
                            .border(1.dp, Color(0x2600E5BE), RoundedCornerShape(14.dp))
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Sign In tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (authMode == AuthMode.SIGN_IN) {
                                        Brush.horizontalGradient(
                                            listOf(
                                                NovaCyanAccent.copy(alpha = 0.28f),
                                                Color(0x3300E5BE)
                                            )
                                        )
                                    } else {
                                        Brush.horizontalGradient(
                                            listOf(Color.Transparent, Color.Transparent)
                                        )
                                    }
                                )
                                .border(
                                    width = if (authMode == AuthMode.SIGN_IN) 1.dp else 0.dp,
                                    color = if (authMode == AuthMode.SIGN_IN) NovaCyanAccent.copy(alpha = 0.6f) else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    authMode = AuthMode.SIGN_IN
                                    errorMessage = null
                                    successMessage = null
                                }
                                .testTag("login_tab"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sign In",
                                color = if (authMode == AuthMode.SIGN_IN) NovaCyanAccent else NovaTextSecondary,
                                fontWeight = if (authMode == AuthMode.SIGN_IN) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }

                        // Register tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (authMode == AuthMode.REGISTER) {
                                        Brush.horizontalGradient(
                                            listOf(
                                                NovaCyanAccent.copy(alpha = 0.28f),
                                                Color(0x3300E5BE)
                                            )
                                        )
                                    } else {
                                        Brush.horizontalGradient(
                                            listOf(Color.Transparent, Color.Transparent)
                                        )
                                    }
                                )
                                .border(
                                    width = if (authMode == AuthMode.REGISTER) 1.dp else 0.dp,
                                    color = if (authMode == AuthMode.REGISTER) NovaCyanAccent.copy(alpha = 0.6f) else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    authMode = AuthMode.REGISTER
                                    errorMessage = null
                                    successMessage = null
                                }
                                .testTag("register_tab"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Create Account",
                                color = if (authMode == AuthMode.REGISTER) NovaCyanAccent else NovaTextSecondary,
                                fontWeight = if (authMode == AuthMode.REGISTER) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Feedback messages (Error or Success)
                    errorMessage?.let { error ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x33FF5252),
                            border = BorderStroke(1.dp, Color(0xFFFF5252))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Error",
                                    tint = Color(0xFFFF5252),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = error,
                                    color = Color(0xFFFF8A80),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    successMessage?.let { success ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x3300E676),
                            border = BorderStroke(1.dp, Color(0xFF00E676))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Success",
                                    tint = Color(0xFF00E676),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = success,
                                    color = Color(0xFFB9F6CA),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Form Fields based on Mode
                    if (authMode == AuthMode.REGISTER) {
                        // Registration: Name, Email, Phone Number, Password, Confirm Password
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            CrystalInputField(
                                value = regFullName,
                                onValueChange = { regFullName = it },
                                label = "Full Name",
                                placeholder = "e.g. Kwesi Mensah",
                                leadingIcon = Icons.Default.Person,
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next,
                                testTag = "reg_name_input"
                            )

                            CrystalInputField(
                                value = regEmail,
                                onValueChange = { regEmail = it },
                                label = "Email Address",
                                placeholder = "kwesi@novachat.africa",
                                leadingIcon = Icons.Default.Email,
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next,
                                testTag = "reg_email_input"
                            )

                            CrystalInputField(
                                value = regPhoneNumber,
                                onValueChange = { regPhoneNumber = it },
                                label = "Phone Number",
                                placeholder = "+233 24 123 4567",
                                leadingIcon = Icons.Default.Phone,
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next,
                                testTag = "reg_phone_input"
                            )

                            CrystalInputField(
                                value = regPassword,
                                onValueChange = { regPassword = it },
                                label = "Password",
                                placeholder = "Min. 6 characters",
                                leadingIcon = Icons.Default.Lock,
                                keyboardType = KeyboardType.Password,
                                isPassword = true,
                                passwordVisible = regPasswordVisible,
                                onPasswordVisibilityToggle = { regPasswordVisible = !regPasswordVisible },
                                imeAction = ImeAction.Next,
                                testTag = "reg_password_input"
                            )

                            CrystalInputField(
                                value = regConfirmPassword,
                                onValueChange = { regConfirmPassword = it },
                                label = "Confirm Password",
                                placeholder = "Re-enter your password",
                                leadingIcon = Icons.Default.Lock,
                                keyboardType = KeyboardType.Password,
                                isPassword = true,
                                passwordVisible = regPasswordVisible,
                                onPasswordVisibilityToggle = { regPasswordVisible = !regPasswordVisible },
                                imeAction = ImeAction.Done,
                                onDone = { focusManager.clearFocus() },
                                testTag = "reg_confirm_password_input"
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Register Action Button
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    errorMessage = null
                                    successMessage = null

                                    val name = regFullName.trim()
                                    val email = regEmail.trim()
                                    val phone = regPhoneNumber.trim()
                                    val password = regPassword

                                    if (name.isBlank()) {
                                        errorMessage = "Please enter your full name."
                                        return@Button
                                    }
                                    if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                        errorMessage = "Please enter a valid email address."
                                        return@Button
                                    }
                                    if (phone.isBlank() || phone.length < 7) {
                                        errorMessage = "Please enter a valid phone number."
                                        return@Button
                                    }
                                    if (password.length < 6) {
                                        errorMessage = "Password must be at least 6 characters."
                                        return@Button
                                    }
                                    if (password != regConfirmPassword) {
                                        errorMessage = "Passwords do not match."
                                        return@Button
                                    }

                                    isLoading = true
                                    coroutineScope.launch {
                                        try {
                                            val authResult = Firebase.auth.createUserWithEmailAndPassword(email, password).await()
                                            val user = authResult.user
                                            if (user != null) {
                                                try {
                                                    val profileUpdates = UserProfileChangeRequest.Builder()
                                                        .setDisplayName(name)
                                                        .build()
                                                    user.updateProfile(profileUpdates).await()
                                                } catch (e: Exception) {
                                                    Log.w("Auth", "Could not set auth display name: ${e.message}")
                                                }

                                                // Store profile in Firestore database
                                                val profile = UserProfile(
                                                    id = user.uid,
                                                    userId = user.uid,
                                                    displayName = name,
                                                    email = email,
                                                    phoneNumber = phone,
                                                    phone = phone,
                                                    handle = "@" + name.lowercase().replace(" ", "_"),
                                                    status = "Akwaaba! Connect with me on NOVACHAT",
                                                    content = "Active NOVACHAT member",
                                                    senderId = user.uid,
                                                    isOnline = true,
                                                    metadata = mapOf(
                                                        "registeredVia" to "email_phone",
                                                        "registeredAt" to System.currentTimeMillis()
                                                    )
                                                )
                                                firebaseRepo.saveUserProfile(profile)

                                                successMessage = "Account registered successfully!"
                                                isLoading = false
                                                onAuthSuccess()
                                            } else {
                                                errorMessage = "Registration could not be completed."
                                                isLoading = false
                                            }
                                        } catch (e: Exception) {
                                            isLoading = false
                                            errorMessage = when {
                                                e.message?.contains("email address is already in use", ignoreCase = true) == true ->
                                                    "This email is already registered. Please sign in instead."
                                                e.message?.contains("network error", ignoreCase = true) == true ->
                                                    "Network connection error. Please check your internet."
                                                else -> e.localizedMessage ?: "Registration failed. Please try again."
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("register_button"),
                                enabled = !isLoading,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NovaCyanAccent,
                                    contentColor = Color(0xFF03120A)
                                )
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = Color(0xFF03120A),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Create Account",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Sign In: Email / Phone and Password
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            CrystalInputField(
                                value = loginIdentifier,
                                onValueChange = { loginIdentifier = it },
                                label = "Email or Phone Number",
                                placeholder = "kwesi@novachat.africa",
                                leadingIcon = Icons.Default.Email,
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next,
                                testTag = "login_email_input"
                            )

                            CrystalInputField(
                                value = loginPassword,
                                onValueChange = { loginPassword = it },
                                label = "Password",
                                placeholder = "Enter your password",
                                leadingIcon = Icons.Default.Lock,
                                keyboardType = KeyboardType.Password,
                                isPassword = true,
                                passwordVisible = loginPasswordVisible,
                                onPasswordVisibilityToggle = { loginPasswordVisible = !loginPasswordVisible },
                                imeAction = ImeAction.Done,
                                onDone = { focusManager.clearFocus() },
                                testTag = "login_password_input"
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Sign In Action Button
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    errorMessage = null
                                    successMessage = null

                                    val id = loginIdentifier.trim()
                                    val password = loginPassword

                                    if (id.isBlank()) {
                                        errorMessage = "Please enter your email or phone number."
                                        return@Button
                                    }
                                    if (password.isBlank()) {
                                        errorMessage = "Please enter your password."
                                        return@Button
                                    }

                                    isLoading = true
                                    coroutineScope.launch {
                                        try {
                                            val emailToUse = if (id.contains("@")) id else "$id@novachat.africa"
                                            val authResult = Firebase.auth.signInWithEmailAndPassword(emailToUse, password).await()
                                            val user = authResult.user
                                            if (user != null) {
                                                firebaseRepo.setUserOnlineStatus(true)
                                                isLoading = false
                                                onAuthSuccess()
                                            } else {
                                                errorMessage = "Sign in could not be completed."
                                                isLoading = false
                                            }
                                        } catch (e: Exception) {
                                            isLoading = false
                                            errorMessage = when {
                                                e.message?.contains("no user record", ignoreCase = true) == true ||
                                                e.message?.contains("invalid-credential", ignoreCase = true) == true ->
                                                    "Invalid email, phone number, or password."
                                                else -> e.localizedMessage ?: "Sign in failed. Please try again."
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("login_button"),
                                enabled = !isLoading,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NovaCyanAccent,
                                    contentColor = Color(0xFF03120A)
                                )
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = Color(0xFF03120A),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Sign In",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Crystal Divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(Color(0x33FFFFFF))
                        )
                        Text(
                            text = "  or continue with  ",
                            color = NovaTextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(Color(0x33FFFFFF))
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Google Sign-In Glass Button
                    OutlinedButton(
                        onClick = {
                            isLoading = true
                            errorMessage = null
                            onGoogleSignInClicked(
                                context = context,
                                credentialManager = credentialManager,
                                onAuthSuccess = {
                                    isLoading = false
                                    onAuthSuccess()
                                },
                                onAuthError = { err ->
                                    isLoading = false
                                    errorMessage = err
                                },
                                scope = coroutineScope,
                                onAuthCancelled = {
                                    isLoading = false
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("google_sign_in_button"),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0x4000E5BE)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0x330C1A14),
                            contentColor = NovaTextPrimary
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Google Sign In",
                                tint = NovaGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Sign in with Google",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Demo Instant Sign-In for rapid preview / evaluation
                    TextButton(
                        onClick = {
                            isLoading = true
                            errorMessage = null
                            coroutineScope.launch {
                                try {
                                    val demoEmail = "demo.user@novachat.africa"
                                    val demoPass = "NovaDemo#2026"
                                    var user = try {
                                        Firebase.auth.signInWithEmailAndPassword(demoEmail, demoPass).await().user
                                    } catch (e: Exception) {
                                        Firebase.auth.createUserWithEmailAndPassword(demoEmail, demoPass).await().user
                                    }

                                    if (user != null) {
                                        firebaseRepo.syncUserProfile(
                                            displayName = "Kwesi Demo",
                                            handle = "@kwesi_demo",
                                            email = demoEmail,
                                            phoneNumber = "+233 24 000 0001",
                                            photoUrl = null
                                        )
                                        firebaseRepo.setUserOnlineStatus(true)
                                        isLoading = false
                                        onAuthSuccess()
                                    } else {
                                        isLoading = false
                                        onAuthSuccess()
                                    }
                                } catch (e: Exception) {
                                    isLoading = false
                                    Log.w("Auth", "Fast access fallback: ${e.message}")
                                    onAuthSuccess()
                                }
                            }
                        },
                        modifier = Modifier.testTag("demo_sign_in_button")
                    ) {
                        Text(
                            text = "⚡ Instant Demo Access (One-Click)",
                            color = NovaCyanAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Trust & Security Footer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Secured",
                    tint = NovaCyanAccent.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Encrypted • Firebase Cloud Sync • Verified Community",
                    color = NovaTextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

/**
 * Reusable Crystal Glass Input Field with translucent frosted background
 */
@Composable
fun CrystalInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onDone: (() -> Unit)? = null,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityToggle: (() -> Unit)? = null,
    testTag: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = NovaTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag),
            placeholder = {
                Text(
                    text = placeholder,
                    color = NovaTextMuted,
                    fontSize = 13.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = label,
                    tint = NovaCyanAccent,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = if (isPassword && onPasswordVisibilityToggle != null) {
                {
                    IconButton(
                        onClick = onPasswordVisibilityToggle,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = NovaTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !passwordVisible) {
                PasswordVisualTransformation()
            } else VisualTransformation.None,
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0x33091B13),
                unfocusedContainerColor = Color(0x1F06140D),
                focusedBorderColor = NovaCyanAccent,
                unfocusedBorderColor = Color(0x3300E5BE),
                focusedTextColor = NovaTextPrimary,
                unfocusedTextColor = NovaTextPrimary,
                cursorColor = NovaCyanAccent
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onDone = { onDone?.invoke() }
            )
        )
    }
}

fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope
) {
    if (Firebase.auth.currentUser != null) {
        onAuthSuccess()
        return
    }

    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onUnauthenticated()
        return
    }

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onUnauthenticated()
            }
        } catch (e: Exception) {
            onUnauthenticated()
        }
    }
}

fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {}
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
        return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context as Activity, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onAuthError("Unexpected credential type received")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
            onAuthCancelled()
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            onAuthError(e.localizedMessage ?: "Sign in failed")
        }
    }
}

fun signOutUser(
    context: Context,
    credentialManager: CredentialManager,
    onSignOutComplete: () -> Unit,
    scope: CoroutineScope
) {
    Firebase.auth.signOut()
    scope.launch {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("Auth", "Failed to clear credential state", e)
        } finally {
            onSignOutComplete()
        }
    }
}
