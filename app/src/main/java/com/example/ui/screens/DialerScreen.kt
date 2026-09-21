package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.SalamViewModel
import com.example.ui.ScreenNav
import com.example.ui.components.SalamAppHeader
import com.example.ui.components.formatBangladeshPhoneNumber
import com.example.ui.theme.SalamCallGreen
import com.example.ui.theme.SalamTealAccent
import com.example.ui.theme.SalamTealDark
import com.example.ui.theme.SalamTealPrimary

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DialerScreen(
    viewModel: SalamViewModel,
    modifier: Modifier = Modifier
) {
    val dialerNumber by viewModel.dialerNumber.collectAsStateWithLifecycle()
    val matchedContact by viewModel.dialerMatchedContact.collectAsStateWithLifecycle()
    val appLang by viewModel.appLanguage.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    val context = LocalContext.current

    fun performHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(30)
            }
        } catch (_: Exception) {}
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SalamAppHeader(
            title = stringResource(R.string.dial_pad),
            subtitle = stringResource(R.string.call_rate_notice),
            ipNumber = userProfile.ipNumber,
            onLanguageToggle = {
                val next = if (appLang == "en") "bn" else "en"
                viewModel.setLanguage(next)
            },
            currentLang = appLang,
            onAdminClick = { viewModel.navigateTo(ScreenNav.ADMIN_PANEL) }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Matched Contact & Formatted Number
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Matched Contact Chip
                AnimatedVisibility(
                    visible = matchedContact != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    matchedContact?.let { contact ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .padding(bottom = 6.dp),
                            color = SalamTealPrimary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "👤 ${contact.name}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = SalamTealPrimary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Number Display Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (dialerNumber.isEmpty()) stringResource(R.string.enter_number_hint)
                        else formatBangladeshPhoneNumber(dialerNumber),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = if (dialerNumber.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.testTag("dialer_number_display")
                    )
                }

                // "Add to Contacts" Button when unsaved number is entered
                AnimatedVisibility(
                    visible = dialerNumber.length >= 8 && matchedContact == null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .combinedClickable(
                                onClick = {
                                    viewModel.addContact(
                                        name = "Contact ${dialerNumber.takeLast(4)}",
                                        number = dialerNumber
                                    )
                                    viewModel.navigateTo(ScreenNav.CONTACTS)
                                }
                            ),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = SalamTealPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.add_to_contacts),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = SalamTealPrimary
                            )
                        }
                    }
                }
            }

            // Middle Section: 3x4 Dial Pad Keypad
            val keyRows = listOf(
                listOf(Triple('1', "", "1"), Triple('2', "ABC", "2"), Triple('3', "DEF", "3")),
                listOf(Triple('4', "GHI", "4"), Triple('5', "JKL", "5"), Triple('6', "MNO", "6")),
                listOf(Triple('7', "PQRS", "7"), Triple('8', "TUV", "8"), Triple('9', "WXYZ", "9")),
                listOf(Triple('*', "", "*"), Triple('0', "+", "0"), Triple('#', "", "#"))
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                keyRows.forEach { rowKeys ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        rowKeys.forEach { (digit, letters, tag) ->
                            DialKeyButton(
                                digit = digit,
                                letters = letters,
                                onClick = {
                                    performHaptic()
                                    viewModel.appendDialerDigit(digit)
                                },
                                onLongClick = {
                                    if (digit == '0') {
                                        performHaptic()
                                        viewModel.appendDialerDigit('+')
                                    }
                                },
                                tag = tag
                            )
                        }
                    }
                }
            }

            // Bottom Section: Call Button and Backspace
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Empty spacer for balance
                Spacer(modifier = Modifier.size(56.dp))

                // Primary Call Button (Large Emerald Green Oval)
                Surface(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .combinedClickable(
                            onClick = {
                                if (dialerNumber.isNotBlank()) {
                                    performHaptic()
                                    viewModel.startCall(
                                        number = dialerNumber,
                                        name = matchedContact?.name ?: dialerNumber
                                    )
                                }
                            }
                        )
                        .testTag("dialer_call_btn"),
                    color = SalamCallGreen,
                    shadowElevation = 6.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Backspace Button
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (dialerNumber.isNotEmpty()) {
                        Surface(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .combinedClickable(
                                    onClick = {
                                        performHaptic()
                                        viewModel.backspaceDialer()
                                    },
                                    onLongClick = {
                                        performHaptic()
                                        viewModel.clearDialer()
                                    }
                                )
                                .testTag("dialer_backspace_btn"),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Backspace",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DialKeyButton(
    digit: Char,
    letters: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    tag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .size(72.dp)
            .clip(CircleShape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("dial_key_$tag"),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        shape = CircleShape
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = digit.toString(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (letters.isNotEmpty()) {
                Text(
                    text = letters,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
