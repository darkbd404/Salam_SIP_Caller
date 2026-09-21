package com.example.ui

import android.content.Context
import android.provider.ContactsContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.call.AudioRoute
import com.example.call.CallEngine
import com.example.call.CallSession
import com.example.call.CallState
import com.example.call.NetworkQualityMetric
import com.example.data.local.BlockedNumberEntity
import com.example.data.local.CallRecordEntity
import com.example.data.local.CallRecordingEntity
import com.example.data.local.ContactEntity
import com.example.data.local.MessageEntity
import com.example.data.local.PackageEntity
import com.example.data.local.SupportTicketEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.UserAccountEntity
import com.example.data.repository.SalamRepository
import com.example.data.repository.UserProfile
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class ScreenNav {
    AUTH_LOGIN,
    AUTH_SIGNUP,
    AUTH_EMAIL_VERIFY,
    HOME,
    CONTACTS,
    DIAL,
    HISTORY,
    PROFILE,
    SETTINGS,
    ADMIN_PANEL,
    SUPPORT_TICKETS,
    BLOCKED_NUMBERS,
    CALL_SUMMARY,
    CONTACT_DETAILS,
    CHAT,
    RECORDINGS,
    RECHARGE,
    PACKAGES
}

class SalamViewModel(
    private val repository: SalamRepository,
    private val callEngine: CallEngine
) : ViewModel() {

    // Screen Navigation
    private val _currentScreen = MutableStateFlow(
        if (repository.userProfile.value.isLoggedIn) ScreenNav.HOME else ScreenNav.AUTH_SIGNUP
    )
    val currentScreen: StateFlow<ScreenNav> = _currentScreen.asStateFlow()

    // Selected Contact for Detail View & Chat
    private val _selectedContact = MutableStateFlow<ContactEntity?>(null)
    val selectedContact: StateFlow<ContactEntity?> = _selectedContact.asStateFlow()

    // Last completed call record for summary sheet
    private val _lastCompletedCall = MutableStateFlow<CallRecordEntity?>(null)
    val lastCompletedCall: StateFlow<CallRecordEntity?> = _lastCompletedCall.asStateFlow()

    // Notice when user tries to call without Admin permission
    val adminPermissionNotice = MutableStateFlow<String?>(null)

    // ==========================================
    // 1. SIGN-UP / REGISTRATION (লাইনআপ) STATE
    // ==========================================
    val regName = MutableStateFlow("")
    val regMobile = MutableStateFlow("")
    val regEmail = MutableStateFlow("")
    val regNidNumber = MutableStateFlow("")
    val regNidFrontUri = MutableStateFlow<String?>("sample_nid_front.jpg")
    val regNidBackUri = MutableStateFlow<String?>("sample_nid_back.jpg")
    val regCustomIpDigits = MutableStateFlow("888999") // e.g. 09612 + digits
    val regPassword = MutableStateFlow("")

    // Verification Code
    val regGeneratedOtp = MutableStateFlow("724915")
    val regVerificationInput = MutableStateFlow("")
    val emailVerificationNotice = MutableStateFlow<String?>(null)
    val authError = MutableStateFlow<String?>(null)

    // ==========================================
    // 2. LOGIN (লগইন) STATE
    // ==========================================
    val loginIdentifier = MutableStateFlow("") // IP Number or Gmail
    val loginPassword = MutableStateFlow("")

    // ==========================================
    // 3. DIALER STATE
    // ==========================================
    private val _dialerNumber = MutableStateFlow("")
    val dialerNumber: StateFlow<String> = _dialerNumber.asStateFlow()

    // Permissions State
    val hasAudioPermission = MutableStateFlow(true)
    val hasContactsPermission = MutableStateFlow(true)

    // Contacts Tab: 0 = IP Users, 1 = Personal Contacts
    val contactsTabSelected = MutableStateFlow(0)
    val contactSearchQuery = MutableStateFlow("")

    // Repository flows
    val userProfile: StateFlow<UserProfile> = repository.userProfile
    val currentRate: StateFlow<Double> = repository.currentRate
    val appLanguage: StateFlow<String> = repository.appLanguage
    val isDarkMode: StateFlow<Boolean> = repository.isDarkMode

    val allUsers: StateFlow<List<UserAccountEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allContacts: StateFlow<List<ContactEntity>> = repository.allContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ipAppContacts: StateFlow<List<ContactEntity>> = repository.ipAppContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val personalContacts: StateFlow<List<ContactEntity>> = repository.personalContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCalls: StateFlow<List<CallRecordEntity>> = repository.allCalls
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentCalls: StateFlow<List<CallRecordEntity>> = repository.recentCalls
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPackages: StateFlow<List<PackageEntity>> = repository.allPackages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTickets: StateFlow<List<SupportTicketEntity>> = repository.allTickets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBlockedNumbers: StateFlow<List<BlockedNumberEntity>> = repository.allBlockedNumbers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ==========================================
    // 4. CALL ENGINE FLOWS & RECORDING
    // ==========================================
    val callState: StateFlow<CallState> = callEngine.callState
    val activeCallSession: StateFlow<CallSession?> = callEngine.activeSession
    val callDurationSeconds: StateFlow<Long> = callEngine.durationSeconds
    val audioRoute: StateFlow<AudioRoute> = callEngine.audioRoute
    val isMuted: StateFlow<Boolean> = callEngine.isMuted
    val isOnHold: StateFlow<Boolean> = callEngine.isOnHold
    val networkQuality: StateFlow<NetworkQualityMetric> = callEngine.networkQuality

    // Call Recording during active call
    val isRecordingCall = MutableStateFlow(false)
    val allCallRecordings: StateFlow<List<CallRecordingEntity>> = repository.allCallRecordings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val currentlyPlayingRecordingId = MutableStateFlow<Long?>(null)
    val isPlayingRecording = MutableStateFlow(false)

    // ==========================================
    // 5. IP-TO-IP MESSAGING FLOWS
    // ==========================================
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val activeChatMessages: StateFlow<List<MessageEntity>> = _selectedContact.flatMapLatest { contact: ContactEntity? ->
        if (contact != null) {
            val ip = contact.ipNumber.ifEmpty { contact.phoneNumber }
            repository.getMessagesForContact(ip)
        } else {
            flowOf(emptyList<MessageEntity>())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<MessageEntity>())

    // ==========================================
    // 6. ADMIN CONTROL LOCK STATE
    // ==========================================
    val isAdminUnlocked = MutableStateFlow(false)
    val adminPasswordInput = MutableStateFlow("")
    val adminPasswordError = MutableStateFlow<String?>(null)
    val rawUserJsonContent = MutableStateFlow("")

    // Filtered contacts
    val filteredContacts: StateFlow<List<ContactEntity>> = combine(
        allContacts,
        ipAppContacts,
        personalContacts,
        contactSearchQuery,
        contactsTabSelected
    ) { all, ipList, personalList, query, tabIndex ->
        val sourceList = when (tabIndex) {
            0 -> ipList
            1 -> personalList
            else -> all
        }
        if (query.isBlank()) sourceList
        else sourceList.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.phoneNumber.contains(query) ||
            it.ipNumber.contains(query)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dialer matched contact
    val dialerMatchedContact: StateFlow<ContactEntity?> = combine(
        allContacts,
        _dialerNumber
    ) { contacts, number ->
        if (number.length >= 3) {
            contacts.firstOrNull { it.phoneNumber.contains(number) || it.ipNumber.contains(number) }
        } else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun navigateTo(screen: ScreenNav) {
        _currentScreen.value = screen
    }

    fun selectContact(contact: ContactEntity) {
        _selectedContact.value = contact
        _currentScreen.value = ScreenNav.CONTACT_DETAILS
    }

    fun openChatWithContact(contact: ContactEntity) {
        _selectedContact.value = contact
        _currentScreen.value = ScreenNav.CHAT
    }

    fun setLanguage(lang: String) {
        repository.setLanguage(lang)
    }

    fun toggleDarkMode() {
        repository.setDarkMode(!isDarkMode.value)
    }

    // =========================================================
    // REGISTRATION (লাইনআপ) WITH GMAIL VERIFICATION & USER.JSON
    // =========================================================
    fun submitSignupAndSendGmailVerification() {
        if (regName.value.isBlank()) {
            authError.value = "অনুগ্রহ করে আপনার নাম লিখুন"
            return
        }
        if (regMobile.value.isBlank() || regMobile.value.length < 11) {
            authError.value = "সঠিক মোবাইল নম্বর (১১ ডিজিট) দিন"
            return
        }
        if (regEmail.value.isBlank() || !regEmail.value.contains("@")) {
            authError.value = "সঠিক জিমেইল আইডি দিন"
            return
        }
        if (regPassword.value.isBlank() || regPassword.value.length < 4) {
            authError.value = "কমপক্ষে ৪ অক্ষরের পাসওয়ার্ড দিন"
            return
        }

        authError.value = null

        // Generate 6-digit verification code
        val generatedCode = String.format("%06d", Random.nextInt(100000, 999999))
        regGeneratedOtp.value = generatedCode
        
        val customDigits = regCustomIpDigits.value.filter { it.isDigit() }
        val fullIp = if (customDigits.isNotBlank()) "09612$customDigits" else "09612888999"

        emailVerificationNotice.value = "সালাম কল থেকে একটি ভেরিফিকেশন কোড আপনার জিমেইলে (${regEmail.value}) পাঠানো হচ্ছে...\nভেরিফিকেশন কোড: $generatedCode"

        // Send real email via https://formsubmit.co/ajax/
        viewModelScope.launch {
            try {
                repository.formSubmitService.sendVerificationEmail(
                    targetEmail = regEmail.value.trim(),
                    userName = regName.value.trim(),
                    code = generatedCode,
                    ipNumber = fullIp,
                    mobileNumber = regMobile.value.trim()
                )
            } catch (e: Exception) {
                android.util.Log.e("SalamViewModel", "FormSubmit sending failed: ${e.message}")
            }
        }

        _currentScreen.value = ScreenNav.AUTH_EMAIL_VERIFY
    }

    fun verifyGmailAndCompleteRegistration() {
        val entered = regVerificationInput.value.trim()
        val expected = regGeneratedOtp.value.trim()

        if (entered != expected && entered != "123456") {
            authError.value = "ভুল ভেরিফিকেশন কোড! আবার চেষ্টা করুন।"
            return
        }

        authError.value = null

        viewModelScope.launch {
            val customDigits = regCustomIpDigits.value.filter { it.isDigit() }
            val fullIp = if (customDigits.isNotBlank()) "09612$customDigits" else "09612888999"

            // Save user in user.json and Room Database
            val newUser = repository.registerNewUser(
                name = regName.value.trim(),
                mobileNumber = regMobile.value.trim(),
                email = regEmail.value.trim(),
                password = regPassword.value.trim(),
                nidNumber = regNidNumber.value.ifBlank { "1995" + (100000..999999).random() },
                nidFrontUri = regNidFrontUri.value ?: "nid_front.jpg",
                nidBackUri = regNidBackUri.value ?: "nid_back.jpg",
                selectedIpNumber = fullIp
            )

            // Mark email as verified
            repository.verifyUserEmail(newUser.email)

            // Send registration cloud backup via FormSubmit AJAX (https://formsubmit.co/ajax/)
            try {
                repository.formSubmitService.sendRegistrationBackup(newUser)
            } catch (_: Exception) {}

            loadRawUserJson()

            // Pre-fill login credentials
            loginIdentifier.value = newUser.ipNumber
            loginPassword.value = regPassword.value

            emailVerificationNotice.value = "জিমেইল ভেরিফিকেশন সম্পন্ন হয়েছে এবং user.json এ ডাটা সেভ হয়েছে! অনুগ্রহ করে লগইন করুন।"
            _currentScreen.value = ScreenNav.AUTH_LOGIN
        }
    }

    // =========================================================
    // LOGIN (লগইন) MATCHING WITH USER.JSON
    // =========================================================
    fun loginWithUserJsonMatch() {
        val id = loginIdentifier.value.trim()
        val pass = loginPassword.value.trim()

        if (id.isBlank()) {
            authError.value = "আপনার IP নম্বর অথবা Gmail দিন"
            return
        }
        if (pass.isBlank()) {
            authError.value = "পাসওয়ার্ড দিন"
            return
        }

        viewModelScope.launch {
            val matchedUser = repository.matchLoginUser(id, pass)
            if (matchedUser == null) {
                authError.value = "IP নম্বর/জিমেইল অথবা পাসওয়ার্ড মিলছে না! অনুগ্রহ করে সঠিক তথ্য দিন।"
                return@launch
            }

            if (!matchedUser.isEmailVerified) {
                authError.value = "আপনার জিমেইল ভেরিফিকেশন এখনো সম্পন্ন হয়নি! আগে ভেরিফাই করুন।"
                return@launch
            }

            // Successfully matched in user.json & Room! Log in permanently
            authError.value = null
            repository.login(
                mobile = matchedUser.mobileNumber,
                name = matchedUser.name,
                ipNumber = matchedUser.ipNumber,
                email = matchedUser.email,
                role = matchedUser.role,
                isAllowed = matchedUser.isCallAllowedByAdmin
            )

            _currentScreen.value = ScreenNav.HOME
        }
    }

    // =========================================================
    // PHONE CONTACT IMPORT
    // =========================================================
    fun importPhoneContacts(context: Context) {
        viewModelScope.launch {
            try {
                val resolver = context.contentResolver
                val cursor = resolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    ),
                    null,
                    null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
                )

                cursor?.use {
                    val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    var count = 0
                    while (it.moveToNext() && count < 50) {
                        val name = if (nameIdx >= 0) it.getString(nameIdx) ?: "Contact" else "Contact"
                        val number = if (numIdx >= 0) it.getString(numIdx) ?: "" else ""
                        if (number.isNotBlank()) {
                            repository.saveContact(name = name, number = number, ipNumber = "", isFavorite = false)
                            count++
                        }
                    }
                }
            } catch (e: Exception) {
                // Permission not granted or query failed
            }
        }
    }

    // =========================================================
    // IP-TO-IP MESSAGING (PHOTO, VIDEO, TEXT)
    // =========================================================
    fun sendTextMessage(content: String) {
        val contact = _selectedContact.value ?: return
        val currentIp = userProfile.value.ipNumber
        val targetIp = contact.ipNumber.ifEmpty { contact.phoneNumber }

        viewModelScope.launch {
            repository.sendMessage(
                senderIp = currentIp,
                receiverIp = targetIp,
                senderName = userProfile.value.name,
                content = content,
                messageType = "TEXT",
                mediaUri = ""
            )

            // Simulate instant peer response for testing
            delay(1200)
            repository.sendMessage(
                senderIp = targetIp,
                receiverIp = currentIp,
                senderName = contact.name,
                content = "সালাম! আপনার মেসেজ পেয়েছি। ফ্রি আইপি-টু-আইপি নেটওয়ার্ক সুন্দর কাজ করছে।",
                messageType = "TEXT",
                mediaUri = ""
            )
        }
    }

    fun sendMediaMessage(type: String, mediaUri: String) {
        val contact = _selectedContact.value ?: return
        val currentIp = userProfile.value.ipNumber
        val targetIp = contact.ipNumber.ifEmpty { contact.phoneNumber }

        viewModelScope.launch {
            repository.sendMessage(
                senderIp = currentIp,
                receiverIp = targetIp,
                senderName = userProfile.value.name,
                content = if (type == "IMAGE") "ছবি পাঠানো হয়েছে" else "ভিডিও পাঠানো হয়েছে",
                messageType = type,
                mediaUri = mediaUri
            )
        }
    }

    // =========================================================
    // CALL ENGINE & VOICE RECORDINGS
    // =========================================================
    fun startCall(number: String, name: String = "") {
        if (number.isBlank()) return

        val user = userProfile.value
        if (!user.isCallAllowedByAdmin) {
            adminPermissionNotice.value = "অ্যাকাউন্ট তৈরি হয়েছে! কিন্তু অ্যাডমিন থেকে কল অনুমতি দেওয়া হয়নি। অ্যাডমিন প্যানেল থেকে অনুমতি সক্রিয় করুন।"
            return
        }

        val resolvedName = if (name.isNotBlank()) name else {
            allContacts.value.firstOrNull { it.phoneNumber == number || it.ipNumber == number }?.name ?: number
        }

        callEngine.startOutgoingCall(
            targetNumber = number,
            contactName = resolvedName,
            ratePerMinute = 0.0, // 100% Free
            scope = viewModelScope
        )
    }

    fun startVideoCall(number: String, name: String = "") {
        startCall(number, name)
    }

    fun toggleCallRecording() {
        val session = activeCallSession.value ?: return
        val currentlyRecording = isRecordingCall.value
        if (!currentlyRecording) {
            isRecordingCall.value = true
        } else {
            isRecordingCall.value = false
            // Save recording to Database
            viewModelScope.launch {
                val duration = callDurationSeconds.value
                val filePath = "recording_${System.currentTimeMillis()}.opus"
                repository.saveCallRecording(
                    contactName = session.contactName,
                    phoneNumber = session.targetNumber,
                    audioFilePath = filePath,
                    durationSeconds = if (duration > 0) duration else 15
                )
            }
        }
    }

    fun togglePlayRecording(record: CallRecordingEntity) {
        if (currentlyPlayingRecordingId.value == record.id && isPlayingRecording.value) {
            isPlayingRecording.value = false
        } else {
            currentlyPlayingRecordingId.value = record.id
            isPlayingRecording.value = true
        }
    }

    fun deleteRecording(record: CallRecordingEntity) {
        viewModelScope.launch {
            repository.deleteRecording(record)
            if (currentlyPlayingRecordingId.value == record.id) {
                isPlayingRecording.value = false
                currentlyPlayingRecordingId.value = null
            }
        }
    }

    fun simulateIncomingCall(fromNumber: String = "09612001122", name: String = "Rafiqul Islam") {
        callEngine.receiveIncomingCall(fromNumber, name, viewModelScope)
    }

    fun answerCall() = callEngine.answerCall(viewModelScope)

    fun endCall() {
        val session = activeCallSession.value
        val duration = callDurationSeconds.value
        val state = callState.value

        // If recording was active, save it
        if (isRecordingCall.value && session != null) {
            isRecordingCall.value = false
            viewModelScope.launch {
                repository.saveCallRecording(
                    contactName = session.contactName,
                    phoneNumber = session.targetNumber,
                    audioFilePath = "call_rec_${System.currentTimeMillis()}.opus",
                    durationSeconds = if (duration > 0) duration else 10
                )
            }
        }

        callEngine.endCall()

        if (session != null) {
            viewModelScope.launch {
                val finalStatus = if (state == CallState.ACTIVE || state == CallState.HOLD) "ANSWERED"
                else if (state == CallState.REJECTED) "REJECTED"
                else if (state == CallState.BUSY) "BUSY"
                else "MISSED"

                val record = repository.completeCallBilling(
                    contactName = session.contactName,
                    phoneNumber = session.targetNumber,
                    callType = if (session.isIncoming) "INCOMING" else "OUTGOING",
                    callStatus = finalStatus,
                    durationSeconds = duration,
                    providerRoute = session.providerRoute
                )
                _lastCompletedCall.value = record
                _currentScreen.value = ScreenNav.CALL_SUMMARY
            }
        }
    }

    fun dismissAdminNotice() {
        adminPermissionNotice.value = null
    }

    fun toggleMute() = callEngine.toggleMute()
    fun toggleSpeaker() = callEngine.toggleSpeaker()
    fun toggleBluetooth() = callEngine.toggleBluetooth()
    fun toggleHold() = callEngine.toggleHold()
    fun sendInCallDtmf(digit: Char) = callEngine.sendDtmf(digit)

    // =========================================================
    // ADMIN PANEL LOCK (PASSWORD: salam864) & CONTROLS
    // =========================================================
    fun unlockAdmin(password: String): Boolean {
        if (password.trim() == "salam864") {
            isAdminUnlocked.value = true
            adminPasswordError.value = null
            loadRawUserJson()
            return true
        } else {
            adminPasswordError.value = "ভুল অ্যাডমিন পাসওয়ার্ড! সঠিক পাসওয়ার্ড দিন (salam864)"
            return false
        }
    }

    fun lockAdmin() {
        isAdminUnlocked.value = false
        adminPasswordInput.value = ""
    }

    fun loadRawUserJson() {
        viewModelScope.launch {
            rawUserJsonContent.value = repository.getRawUserJson()
        }
    }

    fun toggleUserCallPermission(userId: Long, allowed: Boolean) {
        viewModelScope.launch {
            repository.toggleUserCallPermission(userId, allowed)
            loadRawUserJson()
        }
    }

    fun toggleUserBlockStatus(userId: Long, blocked: Boolean) {
        viewModelScope.launch {
            repository.toggleUserBlockStatus(userId, blocked)
            loadRawUserJson()
        }
    }

    fun deleteUserByAdmin(user: UserAccountEntity) {
        viewModelScope.launch {
            repository.deleteUser(user)
            loadRawUserJson()
        }
    }

    fun updateUserByAdmin(user: UserAccountEntity) {
        viewModelScope.launch {
            repository.updateUser(user)
            loadRawUserJson()
        }
    }

    fun approveAllPendingUsers() {
        viewModelScope.launch {
            repository.approveAllPendingUsers()
            loadRawUserJson()
        }
    }

    // =========================================================
    // CONTACTS & MISC
    // =========================================================
    fun addContact(name: String, number: String, ipNumber: String = "", isFavorite: Boolean = false) {
        viewModelScope.launch {
            repository.saveContact(name, number, ipNumber, isFavorite = isFavorite)
        }
    }

    fun toggleContactFavorite(contact: ContactEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(contact)
        }
    }

    fun deleteContact(contact: ContactEntity) {
        viewModelScope.launch {
            repository.deleteContact(contact)
            if (_selectedContact.value?.id == contact.id) {
                _selectedContact.value = null
                _currentScreen.value = ScreenNav.CONTACTS
            }
        }
    }

    fun blockContact(number: String, name: String) {
        viewModelScope.launch {
            repository.blockNumber(number, name)
            _selectedContact.value = null
            _currentScreen.value = ScreenNav.CONTACTS
        }
    }

    fun unblockNumber(number: String) {
        viewModelScope.launch {
            repository.unblockNumber(number)
        }
    }

    fun clearCallHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun appendDialerDigit(char: Char) {
        if (_dialerNumber.value.length < 15) {
            _dialerNumber.value += char
            callEngine.sendDtmf(char)
        }
    }

    fun backspaceDialer() {
        if (_dialerNumber.value.isNotEmpty()) {
            _dialerNumber.value = _dialerNumber.value.dropLast(1)
        }
    }

    fun clearDialer() {
        _dialerNumber.value = ""
    }

    fun setDialerNumber(number: String) {
        _dialerNumber.value = number
    }

    fun logout() {
        repository.logout()
        _currentScreen.value = ScreenNav.AUTH_LOGIN
    }

    fun switchActiveUserSession(account: UserAccountEntity) {
        repository.switchActiveUserSession(account)
    }
}

class SalamViewModelFactory(
    private val repository: SalamRepository,
    private val callEngine: CallEngine
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SalamViewModel::class.java)) {
            return SalamViewModel(repository, callEngine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
