package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.BlockedNumberEntity
import com.example.data.local.SupportTicketEntity
import com.example.data.local.UserAccountEntity
import com.example.ui.SalamViewModel
import com.example.ui.ScreenNav
import com.example.ui.components.ContactAvatar
import com.example.ui.components.SalamAppHeader
import com.example.ui.components.formatBangladeshPhoneNumber
import com.example.ui.theme.SalamCallGreen
import com.example.ui.theme.SalamCyanAccent
import com.example.ui.theme.SalamEndCallRed
import com.example.ui.theme.SalamGold
import com.example.ui.theme.SalamTealAccent
import com.example.ui.theme.SalamTealDark
import com.example.ui.theme.SalamTealPrimary

@Composable
fun ProfileScreen(
    viewModel: SalamViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val appLang by viewModel.appLanguage.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showAboutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SalamAppHeader(
            title = "প্রোফাইল (Profile)",
            subtitle = userProfile.name,
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // User KYC & Profile Details Hero Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_user_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ContactAvatar(name = userProfile.name, size = 64.dp, colorHex = "#00695C")

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = userProfile.name,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "IP: ${userProfile.ipNumber}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = SalamTealPrimary
                                )
                                Text(
                                    text = formatBangladeshPhoneNumber(userProfile.mobileNumber),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Status Badges: KYC Verified & Admin Calling Permission
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // KYC Badge
                            Surface(
                                color = SalamCallGreen.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = SalamCallGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "KYC ভেরিফাইড",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SalamCallGreen
                                    )
                                }
                            }

                            // Admin Call Permission Badge
                            Surface(
                                color = if (userProfile.isCallAllowedByAdmin) SalamCallGreen.copy(alpha = 0.12f) else Color(0xFFFFB300).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (userProfile.isCallAllowedByAdmin) Icons.Default.CheckCircle else Icons.Default.Error,
                                        contentDescription = null,
                                        tint = if (userProfile.isCallAllowedByAdmin) SalamCallGreen else Color(0xFFF57F17),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (userProfile.isCallAllowedByAdmin) "কল অনুমতি: সক্রিয়" else "অ্যাডমিন অনুমতি: পেন্ডিং",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (userProfile.isCallAllowedByAdmin) SalamCallGreen else Color(0xFFE65100)
                                    )
                                }
                            }
                        }

                        if (!userProfile.isCallAllowedByAdmin) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = Color(0xFFFFF8E1),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "⚠️ আপনার অ্যাকাউন্ট ভেরিফাইড হয়েছে। তবে অ্যাডমিন প্যানেল থেকে কল অনুমতি দিলে ফ্রি কল করা যাবে।",
                                    fontSize = 11.sp,
                                    color = Color(0xFF8D6E63),
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Submitted Registration Details & NID Previews
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = SalamTealPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "রেজিস্ট্রেশনের সময় প্রদত্ত তথ্যাবলী:",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        DetailRow(label = "জিমেইল / ইমেইল", value = userProfile.email)
                        DetailRow(label = "জাতীয় পরিচয়পত্র নম্বর (NID)", value = userProfile.nidNumber)
                        DetailRow(label = "বরাদ্দকৃত আইপি লাইন", value = userProfile.ipNumber)
                        DetailRow(label = "টেলিফোন সেবা প্রদানকারী", value = "Salam IPTSP Telephony Ltd. (BTRC)")
                        DetailRow(label = "কলিং চার্জ", value = "১০০% ফ্রি আইপি-টু-আইপি কলিং")

                        Spacer(modifier = Modifier.height(14.dp))

                        // NID Cards Visual Previews
                        Text(
                            text = "সংযুক্ত NID ডকুমেন্টস:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // NID Front Preview Card
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SalamTealPrimary.copy(alpha = 0.08f))
                                    .border(1.dp, SalamTealPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(10.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = SalamTealPrimary, modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("NID Front Part", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("✓ Encrypted", fontSize = 10.sp, color = SalamCallGreen)
                                }
                            }

                            // NID Back Preview Card
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SalamTealPrimary.copy(alpha = 0.08f))
                                    .border(1.dp, SalamTealPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(10.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = SalamTealPrimary, modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("NID Back Part", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("✓ Encrypted", fontSize = 10.sp, color = SalamCallGreen)
                                }
                            }
                        }
                    }
                }
            }

            // Dedicated Admin Control App Mode Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(ScreenNav.ADMIN_PANEL) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SalamTealPrimary.copy(alpha = 0.12f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(SalamTealPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "অ্যাডমিন কন্ট্রোল সেন্টার (Admin App)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = SalamTealPrimary
                                )
                                Text(
                                    text = "ইউজার কল পারমিশন ও ফুল কন্ট্রোল এক্সেস",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Button(
                            onClick = { viewModel.navigateTo(ScreenNav.ADMIN_PANEL) },
                            colors = ButtonDefaults.buttonColors(containerColor = SalamTealPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Open", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Settings Items
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.Mic,
                            title = "রেকর্ড করা ভয়েস কল (Recorded Calls)",
                            subtitle = "কলের রেকর্ডকৃত অডিও শুনুন ও পরিচালনা করুন",
                            onClick = { viewModel.navigateTo(ScreenNav.RECORDINGS) }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        SettingsRow(
                            icon = Icons.Default.Language,
                            title = stringResource(R.string.setting_language),
                            subtitle = if (appLang == "en") "English" else "বাংলা",
                            onClick = {
                                val next = if (appLang == "en") "bn" else "en"
                                viewModel.setLanguage(next)
                            }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        SettingsRow(
                            icon = Icons.Default.DarkMode,
                            title = stringResource(R.string.setting_theme),
                            subtitle = if (isDarkMode) "Dark Mode" else "Light Mode",
                            trailing = {
                                Switch(
                                    checked = isDarkMode,
                                    onCheckedChange = { viewModel.toggleDarkMode() },
                                    colors = SwitchDefaults.colors(checkedThumbColor = SalamTealPrimary)
                                )
                            }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        SettingsRow(
                            icon = Icons.Default.Block,
                            title = stringResource(R.string.setting_blocked),
                            subtitle = "ব্লক করা নম্বরসমূহ দেখুন ও আনব্লক করুন",
                            onClick = { viewModel.navigateTo(ScreenNav.BLOCKED_NUMBERS) }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        SettingsRow(
                            icon = Icons.AutoMirrored.Filled.Help,
                            title = stringResource(R.string.setting_support),
                            subtitle = "হেল্পডেস্ক ও অভিযোগ টিকিট",
                            onClick = { viewModel.navigateTo(ScreenNav.SUPPORT_TICKETS) }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        SettingsRow(
                            icon = Icons.Default.Info,
                            title = stringResource(R.string.setting_about),
                            subtitle = "Salam Call v1.0.0 (IPTSP Licensed)",
                            onClick = { showAboutDialog = true }
                        )
                    }
                }
            }

            // Logout Button
            item {
                OutlinedButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("logout_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SalamEndCallRed)
                ) {
                    Icon(imageVector = Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "লগআউট করুন (Log Out)")
                }
            }

            item { Spacer(modifier = Modifier.height(60.dp)) }
        }

        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = { Text("About Salam Call", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Salam Call is a legitimate Bangladesh IP Telephony (IPTSP) and VoIP application.\n\n" +
                        "• Apps-to-Apps Free Calling via 096 IP lines.\n" +
                        "• Full Admin user approval system.\n" +
                        "• End-to-end Opus HD audio codec."
                    )
                },
                confirmButton = {
                    Button(onClick = { showAboutDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
fun AdminConsoleScreen(
    viewModel: SalamViewModel,
    modifier: Modifier = Modifier
) {
    val isAdminUnlocked by viewModel.isAdminUnlocked.collectAsStateWithLifecycle()
    val adminPasswordInput by viewModel.adminPasswordInput.collectAsStateWithLifecycle()
    val adminPasswordError by viewModel.adminPasswordError.collectAsStateWithLifecycle()
    val rawUserJson by viewModel.rawUserJsonContent.collectAsStateWithLifecycle()
    val users by viewModel.allUsers.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var passwordVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedUserForDetails by remember { mutableStateOf<UserAccountEntity?>(null) }
    var userToEdit by remember { mutableStateOf<UserAccountEntity?>(null) }
    var userToDelete by remember { mutableStateOf<UserAccountEntity?>(null) }
    var editName by remember { mutableStateOf("") }
    var editMobile by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var editIpNumber by remember { mutableStateOf("") }
    var editPassword by remember { mutableStateOf("") }
    var showRawJsonDialog by remember { mutableStateOf(false) }

    // If Admin is locked, require password: salam864
    if (!isAdminUnlocked) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(SalamTealPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Admin Lock",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "অ্যাডমিন কন্ট্রোল লক (Admin Control)",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "এডমিন কন্ট্রোল পাসওয়ার্ড দিয়ে সুরক্ষিত। প্রবেশ করতে পাসওয়ার্ড (salam864) প্রদান করুন।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = adminPasswordInput,
                        onValueChange = { viewModel.adminPasswordInput.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_password_input"),
                        label = { Text("অ্যাডমিন পাসওয়ার্ড (Password)") },
                        placeholder = { Text("salam864") },
                        leadingIcon = { Icon(Icons.Default.Security, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    AnimatedVisibility(visible = adminPasswordError != null) {
                        adminPasswordError?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.unlockAdmin(adminPasswordInput) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("admin_unlock_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = SalamTealPrimary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "আনলক করুন (Unlock Admin)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(
                        onClick = { viewModel.navigateTo(ScreenNav.PROFILE) }
                    ) {
                        Text("ফিরে যান (Back to Profile)")
                    }
                }
            }
        }
        return
    }

    val filteredUsers = if (searchQuery.isBlank()) users else users.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.mobileNumber.contains(searchQuery) ||
        it.ipNumber.contains(searchQuery) ||
        it.nidNumber.contains(searchQuery)
    }

    val pendingCount = users.count { !it.isCallAllowedByAdmin }
    val approvedCount = users.count { it.isCallAllowedByAdmin }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Admin Top Bar
        Surface(
            color = SalamTealDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.navigateTo(ScreenNav.PROFILE) }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Admin Control Panel",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "সম্পূর্ণ নিয়ন্ত্রণ • পাসওয়ার্ড আনলকড",
                            style = MaterialTheme.typography.bodySmall,
                            color = SalamCyanAccent
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Lock Admin button
                    IconButton(onClick = { viewModel.lockAdmin() }) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Lock Admin", tint = Color.White)
                    }

                    // Simulate incoming call test button
                    IconButton(
                        onClick = {
                            viewModel.simulateIncomingCall()
                            Toast.makeText(context, "ইনকামিং টেস্ট কল পাঠানো হয়েছে!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(imageVector = Icons.Default.PhoneInTalk, contentDescription = "Simulate Call", tint = SalamCallGreen)
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Stats Dashboard
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Total Users
                    AdminStatCard(
                        title = "মোট ইউজার",
                        value = users.size.toString(),
                        color = SalamTealPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    // Approved Callers
                    AdminStatCard(
                        title = "কল অনুমোদিত",
                        value = approvedCount.toString(),
                        color = SalamCallGreen,
                        modifier = Modifier.weight(1f)
                    )
                    // Pending Approvals
                    AdminStatCard(
                        title = "অনুমতি পেন্ডিং",
                        value = pendingCount.toString(),
                        color = if (pendingCount > 0) Color(0xFFF57F17) else Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Quick Batch Action: Approve All Pending Users
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SalamCallGreen.copy(alpha = 0.12f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "সকল পেন্ডিং ইউজারদের কল অনুমতি দিন",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = SalamCallGreen
                            )
                            Text(
                                text = "বর্তমানে $pendingCount জন ইউজার অনুমোদনের অপেক্ষায় আছেন",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.approveAllPendingUsers()
                                Toast.makeText(context, "সকল ইউজারকে কল পারমিশন দেওয়া হয়েছে!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SalamCallGreen),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("অনুমোদন দিন", fontSize = 12.sp)
                        }
                    }
                }
            }

            // User Search Filter
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("ইউজার নাম, মোবাইল বা আইপি দিয়ে খুঁজুন...") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Live user.json Database Inspector Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SalamTealPrimary.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = SalamTealPrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "user.json ডাটা ফাইল স্টোরেজ",
                                    fontWeight = FontWeight.Bold,
                                    color = SalamTealPrimary,
                                    fontSize = 13.sp
                                )
                            }

                            Row {
                                TextButton(
                                    onClick = { viewModel.loadRawUserJson() }
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("রিফ্রেশ", fontSize = 11.sp)
                                }
                                TextButton(
                                    onClick = {
                                        viewModel.loadRawUserJson()
                                        showRawJsonDialog = true
                                    }
                                ) {
                                    Text("JSON দেখুন", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }

                        Text(
                            text = "সাইনআপ করা সকল ইউজারের প্রোফাইল, এনআইডি, আইপি ও ক্রেডেনশিয়াল user.json ফাইলে সেভ থাকে এবং লগইনের সময় ম্যাচিং করা হয়।",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Registered Users List
            item {
                Text(
                    text = "রেজিস্টার্ড ইউজারদের তালিকা (${filteredUsers.size}):",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(filteredUsers, key = { it.id }) { user ->
                AdminUserCard(
                    user = user,
                    isCurrentActiveSession = user.mobileNumber == userProfile.mobileNumber,
                    onToggleCallPermission = { allowed ->
                        viewModel.toggleUserCallPermission(user.id, allowed)
                        val msg = if (allowed) "${user.name}-কে কল করার অনুমতি দেওয়া হয়েছে" else "${user.name}-এর কল অনুমতি স্থগিত করা হয়েছে"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    },
                    onToggleBlock = { blocked ->
                        viewModel.toggleUserBlockStatus(user.id, blocked)
                    },
                    onViewDetails = {
                        selectedUserForDetails = user
                    },
                    onSwitchToUser = {
                        viewModel.switchActiveUserSession(user)
                        Toast.makeText(context, "${user.name} হিসেবে সুইচ করা হয়েছে!", Toast.LENGTH_SHORT).show()
                    },
                    onEditUser = {
                        userToEdit = user
                        editName = user.name
                        editMobile = user.mobileNumber
                        editEmail = user.email
                        editIpNumber = user.ipNumber
                        editPassword = user.password
                    },
                    onDeleteUser = {
                        userToDelete = user
                    },
                    onCallUser = {
                        viewModel.startCall(
                            number = user.ipNumber.ifEmpty { user.mobileNumber },
                            name = user.name
                        )
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }

        // User Full Details & NID Modal
        selectedUserForDetails?.let { user ->
            AlertDialog(
                onDismissRequest = { selectedUserForDetails = null },
                title = { Text(text = "${user.name} - NID ও ইউজার তথ্য", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("মোবাইল: ${user.mobileNumber}", fontWeight = FontWeight.Medium)
                        Text("ইমেইল: ${user.email.ifEmpty { "প্রদত্ত হয়নি" }}")
                        Text("আইপি নম্বর: ${user.ipNumber}", fontWeight = FontWeight.Bold, color = SalamTealPrimary)
                        Text("NID নম্বর: ${user.nidNumber}", fontWeight = FontWeight.SemiBold)
                        Text("KYC স্ট্যাটাস: ভেরিফাইড (BTRC ডাটাবেজ)", color = SalamCallGreen)
                        Text("কল পারমিশন: ${if (user.isCallAllowedByAdmin) "অনুমোদিত (Approved)" else "পেন্ডিং (Pending)"}", color = if (user.isCallAllowedByAdmin) SalamCallGreen else Color(0xFFE65100))

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("NID ফটো ডকুমেন্টস:", fontWeight = FontWeight.Bold)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = SalamTealPrimary)
                                    Text("Front Part", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = SalamTealPrimary)
                                    Text("Back Part", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { selectedUserForDetails = null }) {
                        Text("বন্ধ করুন")
                    }
                }
            )
        }

        if (showRawJsonDialog) {
            AlertDialog(
                onDismissRequest = { showRawJsonDialog = false },
                title = { Text(text = "user.json সরাসরি কন্টেন্ট", fontWeight = FontWeight.Bold) },
                text = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = rawUserJson.ifEmpty { "user.json লোড হচ্ছে..." },
                            color = Color(0xFF4CAF50),
                            fontSize = 11.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = { showRawJsonDialog = false }) {
                        Text("ঠিক আছে")
                    }
                }
            )
        }

        // EDIT USER DIALOG (Admin)
        userToEdit?.let { user ->
            AlertDialog(
                onDismissRequest = { userToEdit = null },
                title = { Text(text = "${user.name} সম্পাদনা (Edit User)", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("নাম (Full Name)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editMobile,
                            onValueChange = { editMobile = it },
                            label = { Text("মোবাইল নম্বর") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editEmail,
                            onValueChange = { editEmail = it },
                            label = { Text("ইমেইল") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editIpNumber,
                            onValueChange = { editIpNumber = it },
                            label = { Text("আইপি নম্বর (096...)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editPassword,
                            onValueChange = { editPassword = it },
                            label = { Text("পাসওয়ার্ড (Password)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val updated = user.copy(
                                name = editName.trim(),
                                mobileNumber = editMobile.trim(),
                                email = editEmail.trim(),
                                ipNumber = editIpNumber.trim(),
                                password = editPassword.trim()
                            )
                            viewModel.updateUserByAdmin(updated)
                            Toast.makeText(context, "${updated.name} সফলভাবে আপডেট হয়েছে!", Toast.LENGTH_SHORT).show()
                            userToEdit = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SalamTealPrimary)
                    ) {
                        Text("সেভ করুন")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { userToEdit = null }) {
                        Text("বাতিল")
                    }
                }
            )
        }

        // DELETE CONFIRMATION DIALOG (Admin)
        userToDelete?.let { user ->
            AlertDialog(
                onDismissRequest = { userToDelete = null },
                title = { Text(text = "ইউজার মুছে ফেলবেন?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
                text = {
                    Text("আপনি কি নিশ্চিত যে '${user.name}' (${user.mobileNumber}) ইউজার এবং তার সকল ডাটা user.json ও ডাটাবেজ থেকে মুছে ফেলতে চান?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteUserByAdmin(user)
                            Toast.makeText(context, "${user.name}-কে মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
                            userToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("মুছে ফেলুন")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { userToDelete = null }) {
                        Text("বাতিল")
                    }
                }
            )
        }
    }
}

@Composable
fun AdminUserCard(
    user: UserAccountEntity,
    isCurrentActiveSession: Boolean,
    onToggleCallPermission: (Boolean) -> Unit,
    onToggleBlock: (Boolean) -> Unit,
    onViewDetails: () -> Unit,
    onSwitchToUser: () -> Unit,
    onEditUser: () -> Unit,
    onDeleteUser: () -> Unit,
    onCallUser: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ContactAvatar(name = user.name, size = 44.dp, colorHex = "#00695C")
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = user.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isCurrentActiveSession) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(SalamTealPrimary)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Active Session", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Text(
                            text = "IP: ${user.ipNumber} • ${user.mobileNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Call and Edit Quick Action Buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onCallUser) {
                        Icon(
                            imageVector = Icons.Default.PhoneInTalk,
                            contentDescription = "Call User",
                            tint = SalamCallGreen
                        )
                    }
                    IconButton(onClick = onEditUser) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit User",
                            tint = SalamTealPrimary
                        )
                    }
                    IconButton(onClick = onDeleteUser) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete User",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(10.dp))

            // Interactive Call Permission Switch (ADMIN CONTROL)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "অ্যাপ-টু-অ্যাপ কল করার পারমিশন:",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = if (user.isCallAllowedByAdmin) SalamCallGreen else Color(0xFFE65100)
                    )
                    Text(
                        text = if (user.isCallAllowedByAdmin) "✓ কলিং সক্রিয় (User can make free calls)" else "✗ কলিং বন্ধ (User cannot call)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = user.isCallAllowedByAdmin,
                    onCheckedChange = onToggleCallPermission,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SalamCallGreen,
                        checkedTrackColor = SalamCallGreen.copy(alpha = 0.5f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Actions: View NID & Switch Session
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewDetails,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("NID ডকুমেন্টস", fontSize = 11.sp)
                }

                if (!isCurrentActiveSession) {
                    Button(
                        onClick = onSwitchToUser,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SalamTealPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("এই ইউজার হিসেবে লগইন", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(SalamTealPrimary.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = SalamTealPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        trailing?.invoke()
    }
}

@Composable
fun BlockedNumbersScreen(
    viewModel: SalamViewModel,
    modifier: Modifier = Modifier
) {
    val blockedNumbers by viewModel.allBlockedNumbers.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo(ScreenNav.PROFILE) }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "ব্লক করা নম্বরসমূহ (${blockedNumbers.size})",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        if (blockedNumbers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("কোনো ব্লক করা নম্বর নেই", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(blockedNumbers, key = { it.phoneNumber }) { blocked ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = blocked.name.ifEmpty { blocked.phoneNumber }, fontWeight = FontWeight.Bold)
                                Text(text = blocked.phoneNumber, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Button(
                                onClick = { viewModel.unblockNumber(blocked.phoneNumber) },
                                colors = ButtonDefaults.buttonColors(containerColor = SalamTealPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Unblock", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SupportTicketsScreen(
    viewModel: SalamViewModel,
    modifier: Modifier = Modifier
) {
    val tickets by viewModel.allTickets.collectAsStateWithLifecycle()
    var showNewTicketDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateTo(ScreenNav.PROFILE) }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "হেল্পডেস্ক ও অভিযোগ",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            IconButton(onClick = { showNewTicketDialog = true }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "New Ticket", tint = SalamTealPrimary)
            }
        }

        if (tickets.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("কোনো ওপেন অভিযোগ বা টিকিট নেই", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(tickets, key = { it.id }) { ticket ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(ticket.ticketNumber, fontWeight = FontWeight.Bold, color = SalamTealPrimary)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(SalamCallGreen.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(ticket.status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SalamCallGreen)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(ticket.subject, fontWeight = FontWeight.SemiBold)
                            Text(ticket.message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
