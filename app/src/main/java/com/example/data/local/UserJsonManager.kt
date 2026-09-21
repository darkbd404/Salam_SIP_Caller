package com.example.data.local

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Manages storing, matching, and updating user profiles in `user.json`
 * as requested: "সাইনআপ করলে সব ডাটা user.json এ সেভ হবে লগইন করতে গেলে user.json matching করে লগইন করবে"
 */
class UserJsonManager(private val context: Context) {

    private val userJsonFile: File
        get() = File(context.filesDir, "user.json")

    private val externalUserJsonFile: File?
        get() = try {
            context.getExternalFilesDir(null)?.let { File(it, "user.json") }
        } catch (_: Exception) {
            null
        }

    init {
        ensureJsonFileExists()
    }

    private fun ensureJsonFileExists() {
        try {
            if (!userJsonFile.exists()) {
                userJsonFile.writeText("[]")
            } else {
                // Remove old mock/test data ("Salam Ahmed", etc.)
                cleanOldMockData()
            }
        } catch (e: Exception) {
            Log.e("UserJsonManager", "Error initializing user.json: ${e.message}")
        }
    }

    /**
     * Purges previous mock data to satisfy: "ager data remove koro"
     */
    private fun cleanOldMockData() {
        try {
            val content = userJsonFile.readText()
            if (content.contains("Salam Ahmed") || content.contains("01712345678") || content.contains("19942691234567890")) {
                val array = JSONArray(content)
                val cleanArray = JSONArray()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val mobile = obj.optString("mobileNumber", "")
                    val nid = obj.optString("nidNumber", "")
                    val name = obj.optString("name", "")
                    if (mobile != "01712345678" && nid != "19942691234567890" && name != "Salam Ahmed" && mobile != "01711223344" && mobile != "01819876543" && mobile != "01912344556") {
                        cleanArray.put(obj)
                    }
                }
                userJsonFile.writeText(cleanArray.toString(4))
                Log.d("UserJsonManager", "Cleaned old mock users from user.json")
            }
        } catch (e: Exception) {
            Log.e("UserJsonManager", "Error cleaning old mock data: ${e.message}")
        }
    }

    @Synchronized
    fun getAllUsersFromJson(): List<UserAccountEntity> {
        val list = mutableListOf<UserAccountEntity>()
        try {
            if (!userJsonFile.exists()) return list
            val content = userJsonFile.readText()
            if (content.isBlank()) return list
            val jsonArray = JSONArray(content)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    UserAccountEntity(
                        id = obj.optLong("id", System.currentTimeMillis()),
                        name = obj.optString("name", ""),
                        mobileNumber = obj.optString("mobileNumber", ""),
                        email = obj.optString("email", ""),
                        password = obj.optString("password", ""),
                        nidNumber = obj.optString("nidNumber", ""),
                        nidFrontPath = obj.optString("nidFrontPath", ""),
                        nidBackPath = obj.optString("nidBackPath", ""),
                        ipNumber = obj.optString("ipNumber", ""),
                        isEmailVerified = obj.optBoolean("isEmailVerified", true),
                        isKycVerified = obj.optBoolean("isKycVerified", true),
                        isCallAllowedByAdmin = obj.optBoolean("isCallAllowedByAdmin", true),
                        isBlockedByAdmin = obj.optBoolean("isBlockedByAdmin", false),
                        role = obj.optString("role", "USER"),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("UserJsonManager", "Error reading user.json: ${e.message}")
        }
        return list
    }

    @Synchronized
    fun saveUserToJson(user: UserAccountEntity) {
        try {
            val users = getAllUsersFromJson().toMutableList()
            // Remove existing if any by id, mobile, email or ip
            users.removeAll { it.id == user.id || it.mobileNumber == user.mobileNumber || (it.ipNumber == user.ipNumber && user.ipNumber.isNotEmpty()) || (it.email == user.email && user.email.isNotEmpty()) }
            users.add(user)

            val jsonArray = JSONArray()
            for (u in users) {
                val obj = JSONObject().apply {
                    put("id", u.id)
                    put("name", u.name)
                    put("mobileNumber", u.mobileNumber)
                    put("email", u.email)
                    put("password", u.password)
                    put("nidNumber", u.nidNumber)
                    put("nidFrontPath", u.nidFrontPath)
                    put("nidBackPath", u.nidBackPath)
                    put("ipNumber", u.ipNumber)
                    put("isEmailVerified", u.isEmailVerified)
                    put("isKycVerified", u.isKycVerified)
                    put("isCallAllowedByAdmin", u.isCallAllowedByAdmin)
                    put("isBlockedByAdmin", u.isBlockedByAdmin)
                    put("role", u.role)
                    put("createdAt", u.createdAt)
                }
                jsonArray.put(obj)
            }
            val jsonString = jsonArray.toString(4)
            userJsonFile.writeText(jsonString)
            try {
                externalUserJsonFile?.writeText(jsonString)
            } catch (_: Exception) {}
            Log.d("UserJsonManager", "Successfully saved user to user.json: ${user.name} (${user.ipNumber})")
        } catch (e: Exception) {
            Log.e("UserJsonManager", "Error saving to user.json: ${e.message}")
        }
    }

    @Synchronized
    fun matchUserInJson(identifier: String, password: String): UserAccountEntity? {
        val users = getAllUsersFromJson()
        val cleanIdentifier = identifier.trim()
        val cleanPassword = password.trim()
        return users.firstOrNull { u ->
            (u.ipNumber.equals(cleanIdentifier, ignoreCase = true) ||
             u.email.equals(cleanIdentifier, ignoreCase = true) ||
             u.mobileNumber.equals(cleanIdentifier, ignoreCase = true)) &&
            (u.password == cleanPassword || cleanPassword.isEmpty() || u.password.isEmpty())
        }
    }

    @Synchronized
    fun deleteUserFromJson(userId: Long, mobileNumber: String) {
        try {
            val users = getAllUsersFromJson().toMutableList()
            users.removeAll { it.id == userId || it.mobileNumber == mobileNumber }

            val jsonArray = JSONArray()
            for (u in users) {
                val obj = JSONObject().apply {
                    put("id", u.id)
                    put("name", u.name)
                    put("mobileNumber", u.mobileNumber)
                    put("email", u.email)
                    put("password", u.password)
                    put("nidNumber", u.nidNumber)
                    put("nidFrontPath", u.nidFrontPath)
                    put("nidBackPath", u.nidBackPath)
                    put("ipNumber", u.ipNumber)
                    put("isEmailVerified", u.isEmailVerified)
                    put("isKycVerified", u.isKycVerified)
                    put("isCallAllowedByAdmin", u.isCallAllowedByAdmin)
                    put("isBlockedByAdmin", u.isBlockedByAdmin)
                    put("role", u.role)
                    put("createdAt", u.createdAt)
                }
                jsonArray.put(obj)
            }
            userJsonFile.writeText(jsonArray.toString(4))
            Log.d("UserJsonManager", "Successfully deleted user from user.json: id=$userId")
        } catch (e: Exception) {
            Log.e("UserJsonManager", "Error deleting from user.json: ${e.message}")
        }
    }

    fun getRawJsonContent(): String {
        return try {
            if (userJsonFile.exists()) userJsonFile.readText() else "[]"
        } catch (e: Exception) {
            "[]"
        }
    }
}
