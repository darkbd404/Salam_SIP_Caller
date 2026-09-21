package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val mobileNumber: String,
    val email: String = "",
    val password: String = "",
    val nidNumber: String = "",
    val nidFrontPath: String = "",
    val nidBackPath: String = "",
    val ipNumber: String,
    val isEmailVerified: Boolean = false,
    val isKycVerified: Boolean = true,
    val isCallAllowedByAdmin: Boolean = false, // ADMIN CONTROLS THIS!
    val isBlockedByAdmin: Boolean = false,
    val role: String = "USER", // USER or ADMIN
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val ipNumber: String = "",
    val email: String = "",
    val isFavorite: Boolean = false,
    val isBlocked: Boolean = false,
    val isIpUser: Boolean = false, // True = Salam Call IP User (096), False = Personal Contact
    val avatarColorHex: String = "#00695C",
    val statusMessage: String = "Available for Free IP Call & Chat",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "call_history")
data class CallRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contactName: String,
    val phoneNumber: String,
    val callType: String, // INCOMING, OUTGOING, MISSED
    val callStatus: String, // ANSWERED, BUSY, REJECTED, FAILED, NO_ANSWER, CANCELLED
    val callMode: String = "AUDIO", // AUDIO, VIDEO
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Long = 0,
    val ratePerMinute: Double = 0.0,
    val totalCost: Double = 0.0,
    val isRecorded: Boolean = false,
    val providerRoute: String = "SALAM_IPTSP_HD_VOIP",
    val networkQuality: String = "Opus HD (Free IP Call)"
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderIp: String,
    val receiverIp: String,
    val senderName: String,
    val content: String = "",
    val messageType: String = "TEXT", // TEXT, IMAGE, VIDEO, VOICE
    val mediaUri: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isOutgoing: Boolean = true,
    val isDelivered: Boolean = true,
    val isRead: Boolean = true
)

@Entity(tableName = "call_recordings")
data class CallRecordingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val callId: Long = 0,
    val callerName: String,
    val ipNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Long = 0,
    val filePath: String = "",
    val fileSizeBytes: Long = 0,
    val note: String = "HD Audio Call Record"
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transactionId: String,
    val type: String, // RECHARGE, CALL_CHARGE, PACKAGE_PURCHASE, BONUS, REFUND
    val title: String,
    val amount: Double,
    val balanceAfter: Double,
    val paymentMethod: String, // BKASH, NAGAD, ROCKET, CARDS, SYSTEM
    val status: String, // SUCCESS, PENDING, FAILED
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "packages")
data class PackageEntity(
    @PrimaryKey val id: String,
    val name: String,
    val price: Double,
    val minutes: Int,
    val validityDays: Int,
    val description: String,
    val isPopular: Boolean = false
)

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ticketNumber: String,
    val subject: String,
    val category: String,
    val message: String,
    val status: String, // OPEN, PENDING, RESOLVED, CLOSED
    val adminReply: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "blocked_numbers")
data class BlockedNumberEntity(
    @PrimaryKey val phoneNumber: String,
    val name: String = "",
    val reason: String = "Spam / Unwanted",
    val blockedAt: Long = System.currentTimeMillis()
)
