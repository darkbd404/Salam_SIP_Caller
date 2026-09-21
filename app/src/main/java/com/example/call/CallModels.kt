package com.example.call

enum class CallState {
    IDLE,
    CONNECTING,
    RINGING,
    INCOMING_RINGING,
    ACTIVE,
    HOLD,
    RECONNECTING,
    ENDED,
    BUSY,
    NO_ANSWER,
    REJECTED,
    FAILED
}

enum class AudioRoute {
    EARPIECE,
    SPEAKER,
    BLUETOOTH,
    WIRED_HEADSET
}

enum class CallQuality {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR
}

data class NetworkQualityMetric(
    val quality: CallQuality = CallQuality.EXCELLENT,
    val rttMs: Int = 28,
    val jitterMs: Int = 4,
    val packetLossPercent: Double = 0.1,
    val codec: String = "Opus HD (48kHz)"
)

data class CallSession(
    val callId: String,
    val targetNumber: String,
    val contactName: String = "",
    val isIncoming: Boolean = false,
    val startTime: Long = System.currentTimeMillis(),
    val answerTime: Long? = null,
    val endTime: Long? = null,
    val ratePerMinute: Double = 0.40,
    val providerRoute: String = "IPTSP_SIP_GATEWAY_DHAKA"
)

data class SipAccountConfig(
    val sipDomain: String = "sip.salamcall.com.bd",
    val sipPort: Int = 5060,
    val username: String = "user_09612345678",
    val outboundProxy: String = "proxy.salamcall.com.bd",
    val stunServer: String = "stun:stun.salamcall.com.bd:3478",
    val turnServer: String = "turn:turn.salamcall.com.bd:3478",
    val transport: String = "TLS",
    val preferredCodec: String = "OPUS, G711A, G711U",
    val isRegistered: Boolean = true
)
