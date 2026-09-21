package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.CallRecordEntity
import com.example.data.local.ContactEntity
import com.example.ui.SalamViewModel
import com.example.ui.ScreenNav
import com.example.ui.components.ContactAvatar
import com.example.ui.components.NetworkQualityPill
import com.example.ui.components.SalamAppHeader
import com.example.ui.components.formatBangladeshPhoneNumber
import com.example.ui.theme.SalamCallGreen
import com.example.ui.theme.SalamCyanAccent
import com.example.ui.theme.SalamEndCallRed
import com.example.ui.theme.SalamGold
import com.example.ui.theme.SalamTealAccent
import com.example.ui.theme.SalamTealDark
import com.example.ui.theme.SalamTealPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: SalamViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val ipContacts by viewModel.ipAppContacts.collectAsStateWithLifecycle()
    val personalContacts by viewModel.personalContacts.collectAsStateWithLifecycle()
    val recentCalls by viewModel.recentCalls.collectAsStateWithLifecycle()
    val appLang by viewModel.appLanguage.collectAsStateWithLifecycle()
    val networkQuality by viewModel.networkQuality.collectAsStateWithLifecycle()
    val adminNotice by viewModel.adminPermissionNotice.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SalamAppHeader(
            title = "SALAM CALL",
            subtitle = "১০০% ফ্রি অ্যাপ-টু-অ্যাপ আইপি টেলিফোনি",
            ipNumber = userProfile.ipNumber,
            onLanguageToggle = {
                val next = if (appLang == "en") "bn" else "en"
                viewModel.setLanguage(next)
            },
            currentLang = appLang,
            onAdminClick = { viewModel.navigateTo(ScreenNav.ADMIN_PANEL) }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(2.dp)) }

            // 1. User Status & Assigned IP Line Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ip_number_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SalamTealDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = SalamCallGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "স্বয়ংক্রিয় KYC অনুমোদিত প্রোফাইল",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = SalamCyanAccent
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = userProfile.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                            NetworkQualityPill(metric = networkQuality)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Virtual IP Number Banner
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.10f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "আপনার নির্বাচিত IP নম্বর:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                    Text(
                                        text = userProfile.ipNumber,
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 1.sp
                                        ),
                                        color = Color.White
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(userProfile.ipNumber))
                                        Toast.makeText(context, "IP নম্বর কপি হয়েছে: ${userProfile.ipNumber}", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.15f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy IP",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Free IP Calling Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SalamCallGreen)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "১০০% সম্পূর্ণ ফ্রি",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "আনলিমিটেড অ্যাপ-টু-অ্যাপ কল",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }

                            Text(
                                text = "৳ 0.00 / মিনিট",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SalamCyanAccent
                            )
                        }
                    }
                }
            }

            // 2. Admin Call Permission Status Indicator
            item {
                if (userProfile.isCallAllowedByAdmin) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SalamCallGreen.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SalamCallGreen, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "কল পারমিশন সক্রিয় (Approved by Admin)",
                                    fontWeight = FontWeight.Bold,
                                    color = SalamCallGreen,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "আপনি যেকোনো Salam IP নম্বরে ফ্রি কল করতে পারবেন।",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(26.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "অ্যাডমিন অনুমতি পেন্ডিং!",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFBF360C),
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "অ্যাডমিন অ্যাপ থেকে কল করার পারমিশন দিলে ফ্রি কলিং চালু হবে।",
                                        fontSize = 11.sp,
                                        color = Color(0xFF5D4037)
                                    )
                                }
                            }

                            Button(
                                onClick = { viewModel.navigateTo(ScreenNav.ADMIN_PANEL) },
                                colors = ButtonDefaults.buttonColors(containerColor = SalamTealPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Admin App", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // 3. Quick Action Feature Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Category 1: IP Contacts
                    QuickActionCard(
                        title = "IP Users (${ipContacts.size})",
                        subtitle = "ফ্রি IP কলিং",
                        icon = Icons.Default.PhoneInTalk,
                        color = SalamCallGreen,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.contactsTabSelected.value = 0
                            viewModel.navigateTo(ScreenNav.CONTACTS)
                        }
                    )

                    // Category 2: Personal Contacts
                    QuickActionCard(
                        title = "Personal (${personalContacts.size})",
                        subtitle = "ফোনবুক",
                        icon = Icons.Default.Group,
                        color = SalamTealPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.contactsTabSelected.value = 1
                            viewModel.navigateTo(ScreenNav.CONTACTS)
                        }
                    )

                    // Dial Pad
                    QuickActionCard(
                        title = "Dialer",
                        subtitle = "ডায়াল প্যাড",
                        icon = Icons.Default.Dialpad,
                        color = SalamTealAccent,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(ScreenNav.DIAL) }
                    )

                    // Admin Console
                    QuickActionCard(
                        title = "Admin App",
                        subtitle = "কন্ট্রোল সেন্টার",
                        icon = Icons.Default.AdminPanelSettings,
                        color = SalamTealDark,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(ScreenNav.ADMIN_PANEL) }
                    )
                }
            }

            // 4. Online Salam IP Contacts (One-tap Calling)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Salam IP Users (অ্যাপ-টু-অ্যাপ কল):",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = {
                        viewModel.contactsTabSelected.value = 0
                        viewModel.navigateTo(ScreenNav.CONTACTS)
                    }) {
                        Text("সব দেখুন", color = SalamTealPrimary)
                    }
                }
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ipContacts) { contact ->
                        Card(
                            modifier = Modifier
                                .width(130.dp)
                                .clickable {
                                    viewModel.startCall(contact.ipNumber.ifEmpty { contact.phoneNumber }, contact.name)
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                ContactAvatar(name = contact.name, size = 42.dp, colorHex = contact.avatarColorHex)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = contact.name,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = contact.ipNumber,
                                    fontSize = 10.sp,
                                    color = SalamTealPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SalamCallGreen)
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Free Call", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Recent Calls History
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "সাম্প্রতিক কল হিস্ট্রি:",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = { viewModel.navigateTo(ScreenNav.HISTORY) }) {
                        Text("হিস্ট্রি", color = SalamTealPrimary)
                    }
                }
            }

            if (recentCalls.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "কোনো সাম্প্রতিক কল নেই",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            } else {
                items(recentCalls.take(5), key = { it.id }) { call ->
                    HomeCallItem(
                        call = call,
                        onClick = { viewModel.startCall(call.phoneNumber, call.contactName) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }

        // Admin Permission Required Dialog
        adminNotice?.let { notice ->
            AlertDialog(
                onDismissRequest = { viewModel.dismissAdminNotice() },
                icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(36.dp)) },
                title = { Text(text = "অ্যাডমিন অনুমতি প্রয়োজন", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        text = notice,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.dismissAdminNotice()
                            viewModel.navigateTo(ScreenNav.ADMIN_PANEL)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SalamTealPrimary)
                    ) {
                        Text("Admin App-এ যান")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissAdminNotice() }) {
                        Text("বাতিল")
                    }
                }
            )
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
fun HomeCallItem(
    call: CallRecordEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = when (call.callType) {
                    "OUTGOING" -> Icons.AutoMirrored.Filled.CallMade
                    "INCOMING" -> Icons.AutoMirrored.Filled.CallReceived
                    else -> Icons.Default.CallMissed
                }
                val iconColor = when (call.callStatus) {
                    "MISSED" -> SalamEndCallRed
                    "ANSWERED" -> SalamCallGreen
                    else -> SalamTealPrimary
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = call.contactName.ifEmpty { call.phoneNumber },
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${formatBangladeshPhoneNumber(call.phoneNumber)} • ${call.networkQuality}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(SalamCallGreen.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("Free Call", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SalamCallGreen)
            }
        }
    }
}
