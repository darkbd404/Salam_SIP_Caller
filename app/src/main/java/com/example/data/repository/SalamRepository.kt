package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.BlockedNumberDao
import com.example.data.local.BlockedNumberEntity
import com.example.data.local.CallDao
import com.example.data.local.CallRecordEntity
import com.example.data.local.CallRecordingDao
import com.example.data.local.CallRecordingEntity
import com.example.data.local.ContactDao
import com.example.data.local.ContactEntity
import com.example.data.local.MessageDao
import com.example.data.local.MessageEntity
import com.example.data.local.PackageDao
import com.example.data.local.PackageEntity
import com.example.data.local.SupportTicketDao
import com.example.data.local.SupportTicketEntity
import com.example.data.local.TransactionDao
import com.example.data.local.TransactionEntity
import com.example.data.local.UserAccountDao
import com.example.data.local.UserAccountEntity
import com.example.data.local.UserJsonManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UserProfile(
    val name: String = "",
    val mobileNumber: String = "",
    val email: String = "",
    val ipNumber: String = "",
    val nidNumber: String = "",
    val nidFrontPath: String = "",
    val nidBackPath: String = "",
    val mainBalance: Double = 0.0,
    val bonusBalance: Double = 0.0,
    val activePackageName: String = "Free IP-to-IP Unlimited",
    val packageRemainingMinutes: Int = 99999,
    val packageExpiryDate: String = "31 Dec 2026",
    val isEmailVerified: Boolean = false,
    val isKycVerified: Boolean = false,
    val isCallAllowedByAdmin: Boolean = false, // If false, cannot call until Admin approves!
    val serviceProvider: String = "Salam IPTSP Telephony Ltd.",
    val isLoggedIn: Boolean = false,
    val role: String = "USER" // "ADMIN" or "USER"
)

class SalamRepository(
    private val context: Context,
    private val userDao: UserAccountDao,
    private val contactDao: ContactDao,
    private val callDao: CallDao,
    private val transactionDao: TransactionDao,
    private val packageDao: PackageDao,
    private val supportTicketDao: SupportTicketDao,
    private val blockedNumberDao: BlockedNumberDao,
    private val messageDao: MessageDao,
    private val callRecordingDao: CallRecordingDao
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("salam_call_prefs", Context.MODE_PRIVATE)

    val userJsonManager = UserJsonManager(context)
    val formSubmitService = com.example.data.remote.FormSubmitService()

    init {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                userDao.deleteMockUsers(listOf("01712345678", "01711223344", "01819876543", "01912344556"))
            } catch (_: Exception) {}
        }
    }

    private val _userProfile = MutableStateFlow(loadInitialProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _currentRate = MutableStateFlow(0.0) // 100% Free IP Calling
    val currentRate: StateFlow<Double> = _currentRate.asStateFlow()

    private val _appLanguage = MutableStateFlow(prefs.getString("app_language", "bn") ?: "bn")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("is_dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Database flows
    val allUsers: Flow<List<UserAccountEntity>> = userDao.getAllUsers()
    val allContacts: Flow<List<ContactEntity>> = contactDao.getAllContacts()
    val ipAppContacts: Flow<List<ContactEntity>> = contactDao.getIpAppContacts()
    val personalContacts: Flow<List<ContactEntity>> = contactDao.getPersonalContacts()
    val favoriteContacts: Flow<List<ContactEntity>> = contactDao.getFavoriteContacts()
    val allCalls: Flow<List<CallRecordEntity>> = callDao.getAllCallRecords()
    val recentCalls: Flow<List<CallRecordEntity>> = callDao.getRecentCalls()
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allPackages: Flow<List<PackageEntity>> = packageDao.getAllPackages()
    val allTickets: Flow<List<SupportTicketEntity>> = supportTicketDao.getAllTickets()
    val allBlockedNumbers: Flow<List<BlockedNumberEntity>> = blockedNumberDao.getAllBlockedNumbers()
    val allCallRecordings: Flow<List<CallRecordingEntity>> = callRecordingDao.getAllRecordings()

    private fun loadInitialProfile(): UserProfile {
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        val mobile = prefs.getString("user_mobile", "") ?: ""

        return if (isLoggedIn && mobile.isNotBlank()) {
            UserProfile(
                name = prefs.getString("user_name", "") ?: "",
                mobileNumber = mobile,
                email = prefs.getString("user_email", "") ?: "",
                ipNumber = prefs.getString("user_ip", "") ?: "",
                nidNumber = prefs.getString("user_nid", "") ?: "",
                nidFrontPath = prefs.getString("user_nid_front", "") ?: "",
                nidBackPath = prefs.getString("user_nid_back", "") ?: "",
                isEmailVerified = prefs.getBoolean("is_email_verified", true),
                isKycVerified = prefs.getBoolean("is_kyc_verified", true),
                isCallAllowedByAdmin = prefs.getBoolean("is_call_allowed", false),
                isLoggedIn = true,
                role = prefs.getString("user_role", "USER") ?: "USER"
            )
        } else {
            UserProfile(
                name = "",
                mobileNumber = "",
                email = "",
                ipNumber = "",
                nidNumber = "",
                isEmailVerified = false,
                isKycVerified = false,
                isCallAllowedByAdmin = false,
                isLoggedIn = false,
                role = "USER"
            )
        }
    }

    fun setLanguage(lang: String) {
        prefs.edit().putString("app_language", lang).apply()
        _appLanguage.value = lang
    }

    fun setDarkMode(dark: Boolean) {
        prefs.edit().putBoolean("is_dark_mode", dark).apply()
        _isDarkMode.value = dark
    }

    fun setAdminRate(newRate: Double) {
        _currentRate.value = newRate
    }

    fun updateProfileCallPermission(allowed: Boolean) {
        prefs.edit().putBoolean("is_call_allowed", allowed).apply()
        _userProfile.value = _userProfile.value.copy(isCallAllowedByAdmin = allowed)
    }

    suspend fun registerNewUser(
        name: String,
        mobileNumber: String,
        email: String,
        password: String,
        nidNumber: String,
        nidFrontUri: String,
        nidBackUri: String,
        selectedIpNumber: String
    ): UserAccountEntity {
        val user = UserAccountEntity(
            name = name,
            mobileNumber = mobileNumber,
            email = email,
            password = password,
            nidNumber = nidNumber,
            nidFrontPath = nidFrontUri,
            nidBackPath = nidBackUri,
            ipNumber = selectedIpNumber,
            isEmailVerified = false, // Must verify via email verification code!
            isKycVerified = true,
            isCallAllowedByAdmin = false, // Admin approval required
            isBlockedByAdmin = false,
            role = "USER"
        )
        val id = userDao.insertUser(user)
        val registeredUser = user.copy(id = id)

        // Save into user.json
        userJsonManager.saveUserToJson(registeredUser)

        // Add into IP contacts
        contactDao.insertContact(
            ContactEntity(
                name = "$name (IP User)",
                phoneNumber = mobileNumber,
                ipNumber = selectedIpNumber,
                email = email,
                isFavorite = false,
                isIpUser = true,
                avatarColorHex = "#00695C",
                statusMessage = "Salam Free IP Telephony User"
            )
        )

        return registeredUser
    }

    suspend fun verifyUserEmail(email: String): Boolean {
        val user = userDao.getUserByMobileOrIpOrEmail(email)
        if (user != null) {
            userDao.updateEmailVerificationStatus(user.id, true)
            val updated = user.copy(isEmailVerified = true)
            userJsonManager.saveUserToJson(updated)
            return true
        }
        return false
    }

    suspend fun matchLoginUser(identifier: String, password: String): UserAccountEntity? {
        // 1. Try matching from user.json as requested
        val jsonMatch = userJsonManager.matchUserInJson(identifier, password)
        if (jsonMatch != null) {
            return jsonMatch
        }
        // 2. Fallback to Room DB matching
        return userDao.loginMatch(identifier, password)
    }

    suspend fun findUserByMobile(identifier: String): UserAccountEntity? {
        return userDao.getUserByMobileOrIpOrEmail(identifier)
    }

    suspend fun toggleUserCallPermission(userId: Long, allowed: Boolean) {
        userDao.updateCallPermission(userId, allowed)
        val current = _userProfile.value
        prefs.edit().putBoolean("is_call_allowed", allowed).apply()
        _userProfile.value = current.copy(isCallAllowedByAdmin = allowed)

        // Sync with user.json
        val user = userDao.getUserByMobileOrIpOrEmail(current.mobileNumber)
        if (user != null) {
            userJsonManager.saveUserToJson(user.copy(isCallAllowedByAdmin = allowed))
        }
    }

    suspend fun toggleUserBlockStatus(userId: Long, blocked: Boolean) {
        userDao.updateBlockStatus(userId, blocked)
    }

    suspend fun deleteUser(user: UserAccountEntity) {
        userDao.deleteUser(user)
        userJsonManager.deleteUserFromJson(user.id, user.mobileNumber)
    }

    suspend fun updateUser(user: UserAccountEntity) {
        userDao.updateUser(user)
        userJsonManager.saveUserToJson(user)
    }

    suspend fun approveAllPendingUsers() {
        userDao.approveAllPendingUsers()
        prefs.edit().putBoolean("is_call_allowed", true).apply()
        _userProfile.value = _userProfile.value.copy(isCallAllowedByAdmin = true)
    }

    fun login(
        mobile: String,
        name: String,
        ipNumber: String = "09612345678",
        email: String = "salam230864@gmail.com",
        role: String = "USER",
        isAllowed: Boolean = false,
        isEmailVerified: Boolean = true
    ) {
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("user_name", name)
            .putString("user_mobile", mobile)
            .putString("user_email", email)
            .putString("user_ip", ipNumber)
            .putString("user_role", role)
            .putBoolean("is_email_verified", isEmailVerified)
            .putBoolean("is_kyc_verified", true)
            .putBoolean("is_call_allowed", isAllowed)
            .apply()

        _userProfile.value = _userProfile.value.copy(
            mobileNumber = mobile,
            name = name,
            ipNumber = ipNumber,
            email = email,
            role = role,
            isEmailVerified = isEmailVerified,
            isKycVerified = true,
            isCallAllowedByAdmin = isAllowed,
            isLoggedIn = true
        )
    }

    fun switchUserSession(account: UserAccountEntity) {
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("user_name", account.name)
            .putString("user_mobile", account.mobileNumber)
            .putString("user_email", account.email)
            .putString("user_ip", account.ipNumber)
            .putString("user_nid", account.nidNumber)
            .putString("user_nid_front", account.nidFrontPath)
            .putString("user_nid_back", account.nidBackPath)
            .putBoolean("is_email_verified", account.isEmailVerified)
            .putBoolean("is_kyc_verified", account.isKycVerified)
            .putBoolean("is_call_allowed", account.isCallAllowedByAdmin)
            .putString("user_role", account.role)
            .apply()

        _userProfile.value = UserProfile(
            name = account.name,
            mobileNumber = account.mobileNumber,
            email = account.email,
            ipNumber = account.ipNumber,
            nidNumber = account.nidNumber,
            nidFrontPath = account.nidFrontPath,
            nidBackPath = account.nidBackPath,
            mainBalance = 0.0,
            bonusBalance = 0.0,
            activePackageName = "Free IP-to-IP Unlimited",
            packageRemainingMinutes = 99999,
            packageExpiryDate = "31 Dec 2026",
            isEmailVerified = account.isEmailVerified,
            isKycVerified = account.isKycVerified,
            isCallAllowedByAdmin = account.isCallAllowedByAdmin,
            isLoggedIn = true,
            role = account.role
        )
    }

    fun logout() {
        prefs.edit().putBoolean("is_logged_in", false).apply()
        _userProfile.value = _userProfile.value.copy(isLoggedIn = false)
    }

    fun getRawUserJson(): String {
        return userJsonManager.getRawJsonContent()
    }

    fun switchActiveUserSession(account: UserAccountEntity) {
        switchUserSession(account)
    }

    suspend fun saveContact(
        name: String,
        number: String,
        ipNumber: String = "",
        email: String = "",
        isIpUser: Boolean = false,
        isFavorite: Boolean = false
    ) {
        val contact = ContactEntity(
            name = name,
            phoneNumber = number,
            ipNumber = ipNumber,
            email = email,
            isIpUser = isIpUser || ipNumber.isNotEmpty() || number.startsWith("096"),
            isFavorite = isFavorite,
            statusMessage = if (ipNumber.isNotEmpty() || number.startsWith("096")) "Online • Free IP Calling" else "Personal Contact"
        )
        contactDao.insertContact(contact)
    }

    suspend fun importPhoneContacts(contacts: List<ContactEntity>) {
        contactDao.insertContacts(contacts)
    }

    suspend fun toggleFavorite(contact: ContactEntity) {
        contactDao.updateFavoriteStatus(contact.id, !contact.isFavorite)
    }

    suspend fun deleteContact(contact: ContactEntity) {
        contactDao.deleteContact(contact)
    }

    suspend fun blockNumber(number: String, name: String) {
        blockedNumberDao.blockNumber(BlockedNumberEntity(phoneNumber = number, name = name))
        contactDao.updateBlockStatus(number, true)
    }

    suspend fun unblockNumber(number: String) {
        blockedNumberDao.unblockNumber(number)
        contactDao.updateBlockStatus(number, false)
    }

    // Call Billing & History
    suspend fun completeCallBilling(
        contactName: String,
        phoneNumber: String,
        callType: String,
        callStatus: String,
        durationSeconds: Long,
        callMode: String = "AUDIO",
        isRecorded: Boolean = false,
        providerRoute: String = "SALAM_FREE_IP_TRUNK"
    ): CallRecordEntity {
        val record = CallRecordEntity(
            contactName = contactName.ifEmpty { phoneNumber },
            phoneNumber = phoneNumber,
            callType = callType,
            callStatus = callStatus,
            callMode = callMode,
            timestamp = System.currentTimeMillis(),
            durationSeconds = durationSeconds,
            ratePerMinute = 0.0,
            totalCost = 0.0,
            isRecorded = isRecorded,
            providerRoute = providerRoute,
            networkQuality = "Opus HD (Free IP Call)"
        )
        val id = callDao.insertCallRecord(record)

        // If call was recorded, save to recordings list
        if (isRecorded && durationSeconds > 0) {
            val recording = CallRecordingEntity(
                callId = id,
                callerName = contactName.ifEmpty { phoneNumber },
                ipNumber = phoneNumber,
                timestamp = System.currentTimeMillis(),
                durationSeconds = durationSeconds,
                filePath = "/recordings/call_${phoneNumber}_${System.currentTimeMillis()}.m4a",
                fileSizeBytes = durationSeconds * 32000L,
                note = "Audio Call Recording (Opus HD)"
            )
            callRecordingDao.insertRecording(recording)
        }

        return record
    }

    // Messaging flows and functions
    fun getMessagesBetween(userIp: String, contactIp: String): Flow<List<MessageEntity>> {
        return messageDao.getMessagesBetween(userIp, contactIp)
    }

    fun getMessagesForContact(contactIp: String): Flow<List<MessageEntity>> {
        val myIp = _userProfile.value.ipNumber
        return messageDao.getMessagesBetween(myIp, contactIp)
    }

    suspend fun sendMessage(
        senderIp: String,
        receiverIp: String,
        senderName: String,
        content: String,
        messageType: String = "TEXT",
        mediaUri: String = ""
    ): MessageEntity {
        val message = MessageEntity(
            senderIp = senderIp,
            receiverIp = receiverIp,
            senderName = senderName,
            content = content,
            messageType = messageType,
            mediaUri = mediaUri,
            timestamp = System.currentTimeMillis(),
            isOutgoing = true,
            isDelivered = true,
            isRead = true
        )
        val id = messageDao.insertMessage(message)
        return message.copy(id = id)
    }

    suspend fun saveCallRecording(recording: CallRecordingEntity): Long {
        return callRecordingDao.insertRecording(recording)
    }

    suspend fun saveCallRecording(
        contactName: String,
        phoneNumber: String,
        audioFilePath: String,
        durationSeconds: Long
    ): Long {
        val recording = CallRecordingEntity(
            callerName = contactName.ifEmpty { phoneNumber },
            ipNumber = phoneNumber,
            durationSeconds = durationSeconds,
            filePath = audioFilePath,
            fileSizeBytes = durationSeconds * 32000L,
            note = "HD Voice Call Recording"
        )
        return callRecordingDao.insertRecording(recording)
    }

    suspend fun deleteCallRecording(recording: CallRecordingEntity) {
        callRecordingDao.deleteRecording(recording)
    }

    suspend fun deleteRecording(recording: CallRecordingEntity) {
        callRecordingDao.deleteRecording(recording)
    }

    suspend fun createSupportTicket(subject: String, category: String, message: String): Long {
        val ticketNumber = "TKT-" + (1000..9999).random()
        val ticket = SupportTicketEntity(
            ticketNumber = ticketNumber,
            subject = subject,
            category = category,
            message = message,
            status = "OPEN"
        )
        return supportTicketDao.insertTicket(ticket)
    }

    suspend fun clearHistory() {
        callDao.clearCallHistory()
    }
}
