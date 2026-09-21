package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.call.AudioRoute
import com.example.call.CallState
import com.example.data.local.CallRecordEntity
import com.example.ui.SalamViewModel
import com.example.ui.ScreenNav
import com.example.ui.components.ContactAvatar
import com.example.ui.components.NetworkQualityPill
import com.example.ui.components.formatBangladeshPhoneNumber
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.SalamCallGreen
import com.example.ui.theme.SalamEndCallRed
import com.example.ui.theme.SalamTealAccent
import com.example.ui.theme.SalamTealDark
import com.example.ui.theme.SalamTealPrimary
import java.util.Locale

@Composable
fun IncomingCallScreen(
    viewModel: SalamViewModel,
    modifier: Modifier = Modifier
) {
    val session by viewModel.activeCallSession.collectAsStateWithLifecycle()
    val isMuted by viewModel.isMuted.collectAsStateWithLifecycle()
    val audioRoute by viewModel.audioRoute.collectAsStateWithLifecycle()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatarScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(DarkBackground, SalamTealDark, DarkBackground)
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Calling Banner & Number
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 20.dp)
            ) {
                Text(
                    text = stringResource(R.string.incoming_call_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = SalamTealAccent,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(scale),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .background(SalamCallGreen.copy(alpha = 0.25f))
                    )
                    ContactAvatar(
                        name = session?.contactName ?: "Caller",
                        size = 100.dp,
                        backgroundColor = SalamTealPrimary
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = session?.contactName ?: "Unknown Caller",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = formatBangladeshPhoneNumber(session?.targetNumber ?: "01XXXXXXXXX"),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Route: IPTSP Gateway (Dhaka)",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SalamTealAccent
                    )
                )
            }

            // Bottom Section: Action Buttons (Decline / Accept)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reject Button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .clickable { viewModel.endCall() }
                                .testTag("incoming_reject_btn"),
                            color = SalamEndCallRed,
                            shadowElevation = 8.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CallEnd,
                                    contentDescription = "Reject",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.reject_call),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    // Accept Button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .clickable { viewModel.answerCall() }
                                .testTag("incoming_accept_btn"),
                            color = SalamCallGreen,
                            shadowElevation = 8.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Accept",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.accept_call),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveCallScreen(
    viewModel: SalamViewModel,
    modifier: Modifier = Modifier
) {
    val session by viewModel.activeCallSession.collectAsStateWithLifecycle()
    val callState by viewModel.callState.collectAsStateWithLifecycle()
    val durationSeconds by viewModel.callDurationSeconds.collectAsStateWithLifecycle()
    val isMuted by viewModel.isMuted.collectAsStateWithLifecycle()
    val isOnHold by viewModel.isOnHold.collectAsStateWithLifecycle()
    val audioRoute by viewModel.audioRoute.collectAsStateWithLifecycle()
    val networkQuality by viewModel.networkQuality.collectAsStateWithLifecycle()
    val isRecordingCall by viewModel.isRecordingCall.collectAsStateWithLifecycle()

    var showDtmfSheet by remember { mutableStateOf(false) }

    val formattedTimer = String.format(
        Locale.US,
        "%02d:%02d",
        durationSeconds / 60,
        durationSeconds % 60
    )

    val callStatusText = when (callState) {
        CallState.CONNECTING -> stringResource(R.string.call_status_connecting)
        CallState.RINGING -> stringResource(R.string.call_status_ringing)
        CallState.HOLD -> stringResource(R.string.call_status_hold)
        CallState.ACTIVE -> formattedTimer
        CallState.RECONNECTING -> stringResource(R.string.call_status_reconnecting)
        CallState.BUSY -> stringResource(R.string.call_status_busy)
        CallState.FAILED -> stringResource(R.string.call_status_failed)
        else -> stringResource(R.string.call_status_ended)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Info & Quality
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                NetworkQualityPill(metric = networkQuality)

                Spacer(modifier = Modifier.height(24.dp))

                ContactAvatar(
                    name = session?.contactName ?: "Call",
                    size = 96.dp,
                    backgroundColor = SalamTealPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = session?.contactName ?: "Unknown",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatBangladeshPhoneNumber(session?.targetNumber ?: ""),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = callStatusText,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (callState == CallState.ACTIVE) SalamCallGreen else SalamTealAccent
                    ),
                    modifier = Modifier.testTag("active_call_timer")
                )

                if (isRecordingCall) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SalamEndCallRed.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.FiberManualRecord, contentDescription = null, tint = SalamEndCallRed, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "🔴 কল রেকর্ড হচ্ছে...",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = SalamEndCallRed
                        )
                    }
                }

                if (session?.ratePerMinute ?: 0.0 > 0.0) {
                    Text(
                        text = "Authorized IPTSP • ৳${session?.ratePerMinute}/min",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // In-Call Controls Grid
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Row 1: Mute, Speaker, Keypad
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CallControlButton(
                        icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        label = if (isMuted) stringResource(R.string.btn_unmute) else stringResource(R.string.btn_mute),
                        isActive = isMuted,
                        onClick = { viewModel.toggleMute() },
                        tag = "mute_btn"
                    )

                    CallControlButton(
                        icon = if (audioRoute == AudioRoute.SPEAKER) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        label = stringResource(R.string.btn_speaker),
                        isActive = audioRoute == AudioRoute.SPEAKER,
                        onClick = { viewModel.toggleSpeaker() },
                        tag = "speaker_btn"
                    )

                    CallControlButton(
                        icon = Icons.Default.Dialpad,
                        label = stringResource(R.string.btn_keypad),
                        isActive = showDtmfSheet,
                        onClick = { showDtmfSheet = !showDtmfSheet },
                        tag = "keypad_btn"
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Row 2: Hold, Bluetooth, Voice Record
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CallControlButton(
                        icon = if (isOnHold) Icons.Default.PlayArrow else Icons.Default.Pause,
                        label = if (isOnHold) stringResource(R.string.btn_unhold) else stringResource(R.string.btn_hold),
                        isActive = isOnHold,
                        onClick = { viewModel.toggleHold() },
                        tag = "hold_btn"
                    )

                    CallControlButton(
                        icon = if (isRecordingCall) Icons.Default.FiberManualRecord else Icons.Default.RadioButtonChecked,
                        label = if (isRecordingCall) "রেকর্ডিং বন্ধ" else "ভয়েস রেকর্ড",
                        isActive = isRecordingCall,
                        onClick = { viewModel.toggleCallRecording() },
                        tag = "record_btn"
                    )

                    CallControlButton(
                        icon = Icons.Default.Bluetooth,
                        label = stringResource(R.string.btn_bluetooth),
                        isActive = audioRoute == AudioRoute.BLUETOOTH,
                        onClick = { viewModel.toggleBluetooth() },
                        tag = "bluetooth_btn"
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))

                // End Call Button (Large Red Pill)
                Surface(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .clickable { viewModel.endCall() }
                        .testTag("end_call_btn"),
                    color = SalamEndCallRed,
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CallControlButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    tag: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .clickable { onClick() }
                .testTag("call_ctrl_$tag"),
            color = if (isActive) SalamTealPrimary else DarkSurface,
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isActive) Color.White else Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.8f)
            )
        )
    }
}

@Composable
fun CallSummaryScreen(
    record: CallRecordEntity?,
    onCallAgain: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("call_summary_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.call_summary_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                ContactAvatar(
                    name = record?.contactName ?: "Caller",
                    size = 72.dp,
                    backgroundColor = SalamTealPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = record?.contactName ?: "Unknown",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = formatBangladeshPhoneNumber(record?.phoneNumber ?: ""),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Stats Details Row
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.call_duration),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val mins = (record?.durationSeconds ?: 0) / 60
                            val secs = (record?.durationSeconds ?: 0) % 60
                            Text(
                                text = "${mins}m ${secs}s",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.call_cost),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "৳ ${String.format(Locale.US, "%.2f", record?.totalCost ?: 0.0)}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SalamCallGreen
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onCallAgain,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("summary_call_again_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = SalamCallGreen),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = stringResource(R.string.call_again))
                    }

                    Button(
                        onClick = onDone,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("summary_done_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = SalamTealPrimary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(text = stringResource(R.string.done))
                    }
                }
            }
        }
    }
}
