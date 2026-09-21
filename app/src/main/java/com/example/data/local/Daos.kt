package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserAccountDao {
    @Query("SELECT * FROM user_accounts ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserAccountEntity>>

    @Query("SELECT * FROM user_accounts WHERE id = :id LIMIT 1")
    fun getUserById(id: Long): Flow<UserAccountEntity?>

    @Query("SELECT * FROM user_accounts WHERE mobileNumber = :identifier OR ipNumber = :identifier OR email = :identifier LIMIT 1")
    suspend fun getUserByMobileOrIpOrEmail(identifier: String): UserAccountEntity?

    @Query("SELECT * FROM user_accounts WHERE (ipNumber = :identifier OR email = :identifier OR mobileNumber = :identifier) AND password = :password LIMIT 1")
    suspend fun loginMatch(identifier: String, password: String): UserAccountEntity?

    @Query("SELECT * FROM user_accounts WHERE role = 'ADMIN' LIMIT 1")
    suspend fun getAdminUser(): UserAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccountEntity): Long

    @Update
    suspend fun updateUser(user: UserAccountEntity)

    @Delete
    suspend fun deleteUser(user: UserAccountEntity)

    @Query("UPDATE user_accounts SET isCallAllowedByAdmin = :allowed WHERE id = :userId")
    suspend fun updateCallPermission(userId: Long, allowed: Boolean)

    @Query("UPDATE user_accounts SET isBlockedByAdmin = :blocked WHERE id = :userId")
    suspend fun updateBlockStatus(userId: Long, blocked: Boolean)

    @Query("UPDATE user_accounts SET isEmailVerified = :verified WHERE id = :userId")
    suspend fun updateEmailVerificationStatus(userId: Long, verified: Boolean)

    @Query("UPDATE user_accounts SET isKycVerified = :verified WHERE id = :userId")
    suspend fun updateKycStatus(userId: Long, verified: Boolean)

    @Query("UPDATE user_accounts SET isCallAllowedByAdmin = 1 WHERE isCallAllowedByAdmin = 0")
    suspend fun approveAllPendingUsers()

    @Query("DELETE FROM user_accounts WHERE mobileNumber IN (:numbers)")
    suspend fun deleteMockUsers(numbers: List<String>)
}

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts WHERE isBlocked = 0 ORDER BY name ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE isIpUser = 1 AND isBlocked = 0 ORDER BY name ASC")
    fun getIpAppContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE isIpUser = 0 AND isBlocked = 0 ORDER BY name ASC")
    fun getPersonalContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE isFavorite = 1 AND isBlocked = 0 ORDER BY name ASC")
    fun getFavoriteContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE phoneNumber = :number OR ipNumber = :number LIMIT 1")
    suspend fun getContactByNumber(number: String): ContactEntity?

    @Query("SELECT * FROM contacts WHERE name LIKE '%' || :query || '%' OR phoneNumber LIKE '%' || :query || '%' OR ipNumber LIKE '%' || :query || '%'")
    fun searchContacts(query: String): Flow<List<ContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<ContactEntity>)

    @Update
    suspend fun updateContact(contact: ContactEntity)

    @Delete
    suspend fun deleteContact(contact: ContactEntity)

    @Query("UPDATE contacts SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)

    @Query("UPDATE contacts SET isBlocked = :isBlocked WHERE phoneNumber = :number")
    suspend fun updateBlockStatus(number: String, isBlocked: Boolean)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE (senderIp = :userIp AND receiverIp = :contactIp) OR (senderIp = :contactIp AND receiverIp = :userIp) ORDER BY timestamp ASC")
    fun getMessagesBetween(userIp: String, contactIp: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Delete
    suspend fun deleteMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE (senderIp = :userIp AND receiverIp = :contactIp) OR (senderIp = :contactIp AND receiverIp = :userIp)")
    suspend fun clearConversation(userIp: String, contactIp: String)
}

@Dao
interface CallRecordingDao {
    @Query("SELECT * FROM call_recordings ORDER BY timestamp DESC")
    fun getAllRecordings(): Flow<List<CallRecordingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recording: CallRecordingEntity): Long

    @Delete
    suspend fun deleteRecording(recording: CallRecordingEntity)

    @Query("DELETE FROM call_recordings")
    suspend fun clearAllRecordings()
}

@Dao
interface CallDao {
    @Query("SELECT * FROM call_history ORDER BY timestamp DESC")
    fun getAllCallRecords(): Flow<List<CallRecordEntity>>

    @Query("SELECT * FROM call_history WHERE callType = :type ORDER BY timestamp DESC")
    fun getCallsByType(type: String): Flow<List<CallRecordEntity>>

    @Query("SELECT * FROM call_history ORDER BY timestamp DESC LIMIT 5")
    fun getRecentCalls(): Flow<List<CallRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallRecord(callRecord: CallRecordEntity): Long

    @Query("DELETE FROM call_history WHERE id = :id")
    suspend fun deleteCallRecord(id: Long)

    @Query("DELETE FROM call_history")
    suspend fun clearCallHistory()
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long
}

@Dao
interface PackageDao {
    @Query("SELECT * FROM packages")
    fun getAllPackages(): Flow<List<PackageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackages(packages: List<PackageEntity>)
}

@Dao
interface SupportTicketDao {
    @Query("SELECT * FROM support_tickets ORDER BY createdAt DESC")
    fun getAllTickets(): Flow<List<SupportTicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: SupportTicketEntity): Long

    @Update
    suspend fun updateTicket(ticket: SupportTicketEntity)
}

@Dao
interface BlockedNumberDao {
    @Query("SELECT * FROM blocked_numbers ORDER BY blockedAt DESC")
    fun getAllBlockedNumbers(): Flow<List<BlockedNumberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun blockNumber(blocked: BlockedNumberEntity)

    @Query("DELETE FROM blocked_numbers WHERE phoneNumber = :number")
    suspend fun unblockNumber(number: String)

    @Query("SELECT EXISTS(SELECT 1 FROM blocked_numbers WHERE phoneNumber = :number)")
    suspend fun isBlocked(number: String): Boolean
}
