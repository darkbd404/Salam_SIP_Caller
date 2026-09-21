package com.example.ui

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.call.CallState
import com.example.ui.components.SalamBottomNavBar
import com.example.ui.screens.ActiveCallScreen
import com.example.ui.screens.AdminConsoleScreen
import com.example.ui.screens.BlockedNumbersScreen
import com.example.ui.screens.CallSummaryScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.ContactDetailScreen
import com.example.ui.screens.ContactsScreen
import com.example.ui.screens.DialerScreen
import com.example.ui.screens.EmailVerificationScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.IncomingCallScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RecordingsScreen
import com.example.ui.screens.SignupScreen
import com.example.ui.screens.SupportTicketsScreen

@Composable
fun SalamCallApp(
    viewModel: SalamViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val callState by viewModel.callState.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val lastCompletedCall by viewModel.lastCompletedCall.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Runtime Permission Launcher for VoIP & Contacts
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val recordAudioGranted = permissionsMap[Manifest.permission.RECORD_AUDIO] ?: false
        val contactsGranted = permissionsMap[Manifest.permission.READ_CONTACTS] ?: false
        viewModel.hasAudioPermission.value = recordAudioGranted
        viewModel.hasContactsPermission.value = contactsGranted
        if (contactsGranted) {
            viewModel.importPhoneContacts(context)
        }
    }

    LaunchedEffect(Unit) {
        permissionsLauncher.launch(
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CONTACTS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CAMERA
            )
        )
    }

    // Handle back presses
    BackHandler(enabled = currentScreen != ScreenNav.HOME && currentScreen != ScreenNav.AUTH_LOGIN) {
        when (currentScreen) {
            ScreenNav.AUTH_EMAIL_VERIFY -> viewModel.navigateTo(ScreenNav.AUTH_SIGNUP)
            ScreenNav.AUTH_SIGNUP -> viewModel.navigateTo(ScreenNav.AUTH_LOGIN)
            ScreenNav.CONTACT_DETAILS -> viewModel.navigateTo(ScreenNav.CONTACTS)
            ScreenNav.CHAT -> viewModel.navigateTo(ScreenNav.CONTACT_DETAILS)
            ScreenNav.RECORDINGS -> viewModel.navigateTo(ScreenNav.PROFILE)
            ScreenNav.ADMIN_PANEL, ScreenNav.SUPPORT_TICKETS,
            ScreenNav.BLOCKED_NUMBERS, ScreenNav.CALL_SUMMARY -> viewModel.navigateTo(ScreenNav.HOME)
            else -> viewModel.navigateTo(ScreenNav.HOME)
        }
    }

    // Overlay 1: Incoming Call Screen
    if (callState == CallState.INCOMING_RINGING) {
        IncomingCallScreen(viewModel = viewModel)
        return
    }

    // Overlay 2: Active or Connecting Call Screen
    if (callState == CallState.CONNECTING ||
        callState == CallState.RINGING ||
        callState == CallState.ACTIVE ||
        callState == CallState.HOLD ||
        callState == CallState.RECONNECTING
    ) {
        ActiveCallScreen(viewModel = viewModel)
        return
    }

    // Overlay 3: Call Summary Screen
    if (currentScreen == ScreenNav.CALL_SUMMARY) {
        CallSummaryScreen(
            record = lastCompletedCall,
            onCallAgain = {
                lastCompletedCall?.let {
                    viewModel.startCall(it.phoneNumber, it.contactName)
                }
            },
            onDone = {
                viewModel.navigateTo(ScreenNav.HOME)
            }
        )
        return
    }

    // Auth Screens (Login, Sign-Up, Email Verification)
    if (!userProfile.isLoggedIn ||
        currentScreen == ScreenNav.AUTH_LOGIN ||
        currentScreen == ScreenNav.AUTH_SIGNUP ||
        currentScreen == ScreenNav.AUTH_EMAIL_VERIFY
    ) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "auth_transition"
        ) { screen ->
            when (screen) {
                ScreenNav.AUTH_SIGNUP -> SignupScreen(viewModel = viewModel)
                ScreenNav.AUTH_EMAIL_VERIFY -> EmailVerificationScreen(viewModel = viewModel)
                else -> LoginScreen(viewModel = viewModel)
            }
        }
        return
    }

    // Main App Navigation Scaffold
    val showBottomBar = currentScreen in listOf(
        ScreenNav.HOME,
        ScreenNav.CONTACTS,
        ScreenNav.DIAL,
        ScreenNav.HISTORY,
        ScreenNav.PROFILE
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                SalamBottomNavBar(
                    currentScreen = currentScreen,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { targetScreen ->
                when (targetScreen) {
                    ScreenNav.HOME -> HomeScreen(viewModel = viewModel)
                    ScreenNav.CONTACTS -> ContactsScreen(viewModel = viewModel)
                    ScreenNav.DIAL -> DialerScreen(viewModel = viewModel)
                    ScreenNav.HISTORY -> HistoryScreen(viewModel = viewModel)
                    ScreenNav.PROFILE -> ProfileScreen(viewModel = viewModel)
                    ScreenNav.CONTACT_DETAILS -> ContactDetailScreen(viewModel = viewModel)
                    ScreenNav.CHAT -> ChatScreen(viewModel = viewModel)
                    ScreenNav.RECORDINGS -> RecordingsScreen(viewModel = viewModel)
                    ScreenNav.BLOCKED_NUMBERS -> BlockedNumbersScreen(viewModel = viewModel)
                    ScreenNav.SUPPORT_TICKETS -> SupportTicketsScreen(viewModel = viewModel)
                    ScreenNav.ADMIN_PANEL -> AdminConsoleScreen(viewModel = viewModel)
                    else -> HomeScreen(viewModel = viewModel)
                }
            }
        }
    }
}
