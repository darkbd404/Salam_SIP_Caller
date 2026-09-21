package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserAccountEntity::class,
        ContactEntity::class,
        CallRecordEntity::class,
        TransactionEntity::class,
        PackageEntity::class,
        SupportTicketEntity::class,
        BlockedNumberEntity::class,
        MessageEntity::class,
        CallRecordingEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class SalamDatabase : RoomDatabase() {
    abstract fun userDao(): UserAccountDao
    abstract fun contactDao(): ContactDao
    abstract fun callDao(): CallDao
    abstract fun transactionDao(): TransactionDao
    abstract fun packageDao(): PackageDao
    abstract fun supportTicketDao(): SupportTicketDao
    abstract fun blockedNumberDao(): BlockedNumberDao
    abstract fun messageDao(): MessageDao
    abstract fun callRecordingDao(): CallRecordingDao

    companion object {
        @Volatile
        private var INSTANCE: SalamDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
        ): SalamDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SalamDatabase::class.java,
                    "salam_call_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope, context.applicationContext))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope,
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database, context)
                }
            }
        }

        suspend fun populateInitialData(db: SalamDatabase, ctx: Context) {
            val jsonManager = UserJsonManager(ctx)
            // No mock/fake users seeded. Real users are registered via Signup and saved to user.json

            // Seed Support / Help Desk Contacts
            val contacts = listOf(
                ContactEntity(
                    name = "Salam Ahmed (Admin IP)",
                    phoneNumber = "01712345678",
                    ipNumber = "09612345678",
                    email = "salam230864@gmail.com",
                    isFavorite = true,
                    isIpUser = true,
                    avatarColorHex = "#00695C",
                    statusMessage = "Online • Free IP Audio & Video Call"
                ),
                ContactEntity(
                    name = "Rafiqul Islam",
                    phoneNumber = "01711223344",
                    ipNumber = "09612001122",
                    email = "rafiqul.islam@gmail.com",
                    isFavorite = true,
                    isIpUser = true,
                    avatarColorHex = "#00897B",
                    statusMessage = "Online • HD Voice Active"
                ),
                ContactEntity(
                    name = "Tasnim Rahman",
                    phoneNumber = "01819876543",
                    ipNumber = "09612003344",
                    email = "tasnim.rahman@gmail.com",
                    isFavorite = false,
                    isIpUser = true,
                    avatarColorHex = "#26A69A",
                    statusMessage = "Available for IP Chat & Calls"
                ),
                ContactEntity(
                    name = "Farhana Akter",
                    phoneNumber = "01912344556",
                    ipNumber = "09612005566",
                    email = "farhana.akter@gmail.com",
                    isFavorite = false,
                    isIpUser = true,
                    avatarColorHex = "#00796B",
                    statusMessage = "Online • 24/7 Salam Line"
                ),
                ContactEntity(
                    name = "Dr. Anwar Hossain",
                    phoneNumber = "01722334455",
                    ipNumber = "09612007788",
                    email = "anwar.doc@gmail.com",
                    isFavorite = true,
                    isIpUser = true,
                    avatarColorHex = "#004D40",
                    statusMessage = "Available for Emergency Calls"
                ),
                ContactEntity(
                    name = "Abdur Rahim (Personal)",
                    phoneNumber = "01715556677",
                    ipNumber = "",
                    email = "rahim@gmail.com",
                    isFavorite = false,
                    isIpUser = false,
                    avatarColorHex = "#455A64",
                    statusMessage = "Phonebook Contact"
                ),
                ContactEntity(
                    name = "Karim Ullah (Personal)",
                    phoneNumber = "01813334455",
                    ipNumber = "",
                    email = "karim@gmail.com",
                    isFavorite = false,
                    isIpUser = false,
                    avatarColorHex = "#546E7A",
                    statusMessage = "Phonebook Contact"
                )
            )
            contacts.forEach { db.contactDao().insertContact(it) }

            // Seed Initial Messages
            val messages = listOf(
                MessageEntity(
                    senderIp = "09612001122",
                    receiverIp = "09612345678",
                    senderName = "Rafiqul Islam",
                    content = "আসসালামু আলাইকুম! সালাম কল অ্যাপ দিয়ে কথা বলার ভয়েস কোয়ালিটি অনেক সুন্দর।",
                    messageType = "TEXT",
                    timestamp = System.currentTimeMillis() - 3600000,
                    isOutgoing = false
                ),
                MessageEntity(
                    senderIp = "09612345678",
                    receiverIp = "09612001122",
                    senderName = "Salam Ahmed",
                    content = "ওয়ালাইকুম আসসালাম! এটি ১০০% ফ্রি আইপি টেলিফোনি সেবা।",
                    messageType = "TEXT",
                    timestamp = System.currentTimeMillis() - 3000000,
                    isOutgoing = true
                )
            )
            messages.forEach { db.messageDao().insertMessage(it) }

            // Seed Sample Call Recording
            db.callRecordingDao().insertRecording(
                CallRecordingEntity(
                    callId = 101,
                    callerName = "Rafiqul Islam",
                    ipNumber = "09612001122",
                    timestamp = System.currentTimeMillis() - 7200000,
                    durationSeconds = 142,
                    filePath = "/recordings/call_09612001122_sample.m4a",
                    fileSizeBytes = 458200,
                    note = "Opus HD Crystal Voice Recording"
                )
            )
        }
    }
}
