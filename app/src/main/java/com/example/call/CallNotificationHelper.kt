package com.example.call

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

/**
 * System Notification Bar Service for Salam Call.
 * Displays real-time call status (Incoming, Ringing, Active duration)
 * with interactive Accept/End actions in the Android Notification Shade.
 */
object CallNotificationHelper {

    private const val CHANNEL_ID = "salam_call_channel_v2"
    private const val CHANNEL_NAME = "Salam Call Telephony Status"
    private const val NOTIFICATION_ID = 2026

    fun showCallNotification(
        context: Context,
        callState: CallState,
        callerName: String,
        callerNumber: String,
        durationSeconds: Long = 0L,
        isIncoming: Boolean = false
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Salam Call Live VoIP & In-Call Audio Status"
                setSound(null, null)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val durationFormatted = String.format(
            "%02d:%02d",
            durationSeconds / 60,
            durationSeconds % 60
        )

        val title = when (callState) {
            CallState.INCOMING_RINGING -> "📞 ইনকামিং সালাম কল: $callerName"
            CallState.CONNECTING -> "🔄 কল সংযোগ হচ্ছে... ($callerName)"
            CallState.RINGING -> "🔔 রিং হচ্ছে... ($callerName)"
            CallState.ACTIVE -> "🟢 সালাম কল চলমান: $callerName ($durationFormatted)"
            CallState.HOLD -> "⏸️ কল হোল্ডে রাখা হয়েছে ($callerName)"
            else -> "📞 সালাম আইপি কল: $callerName"
        }

        val content = when (callState) {
            CallState.ACTIVE -> "সময়: $durationFormatted • IP: $callerNumber • ফ্রি HD Opus ভয়েস"
            CallState.INCOMING_RINGING -> "নম্বর: $callerNumber • সালাম ফ্রি আইপি কল"
            CallState.RINGING -> "উত্তর দেওয়ার অপেক্ষায় • IP: $callerNumber"
            else -> "আইপি নম্বর: $callerNumber • লাইভ কলিং"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_phone_call)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(pendingIntent)
            .setOngoing(callState == CallState.ACTIVE || callState == CallState.INCOMING_RINGING || callState == CallState.RINGING)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(false)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    fun dismissNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.cancel(NOTIFICATION_ID)
    }
}
