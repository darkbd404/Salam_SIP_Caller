package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.ContactEntity
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
import com.example.ui.theme.SalamTealPrimary

@Composable
fun ContactsScreen(
    viewModel: SalamViewModel,
    modifier: Modifier = Modifier
) {
    val contacts by viewModel.filteredContacts.collectAsStateWithLifecycle()
    val ipContacts by viewModel.ipAppContacts.collectAsStateWithLifecycle()
    val personalContacts by viewModel.personalContacts.collectAsStateWithLifecycle()
    val selectedTabIndex by viewModel.contactsTabSelected.collectAsStateWithLifecycle()
    val searchQuery by viewModel.contactSearchQuery.collectAsStateWithLifecycle()
    val appLang by viewModel.appLanguage.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SalamAppHeader(
                title = "কন্টাক্টস (Contacts)",
                subtitle = "Salam IP Users (${ipContacts.size}) • Personal (${personalContacts.size})",
                ipNumber = userProfile.ipNumber,
                onLanguageToggle = {
                    val next = if (appLang == "en") "bn" else "en"
                    viewModel.setLanguage(next)
                },
                currentLang = appLang,
                onAdminClick = { viewModel.navigateTo(ScreenNav.ADMIN_PANEL) }
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.contactSearchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_contacts_input"),
                placeholder = { Text(if (selectedTabIndex == 0) "Search IP Users (096...)" else "Search Personal Contacts...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.contactSearchQuery.value = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SalamTealPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                singleLine = true
            )

            // 2 Distinct Category Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = SalamTealPrimary
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { viewModel.contactsTabSelected.value = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhoneInTalk, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (selectedTabIndex == 0) SalamTealPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Salam IP কন্টাক্ট (${ipContacts.size})",
                                fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { viewModel.contactsTabSelected.value = 1 },
                    text = {
                        Text(
                            text = "ব্যক্তিগত ফোনবুক (${personalContacts.size})",
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            // Info bar for IP Tab
            if (selectedTabIndex == 0) {
                Surface(
                    color = SalamCallGreen.copy(alpha = 0.10f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = SalamCallGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "সকল Salam IP নম্বরে কল সম্পূর্ণ ফ্রি (Apps to Apps Free Call)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = SalamCallGreen
                        )
                    }
                }
            }

            // Contact List
            if (contacts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (selectedTabIndex == 0) "কোনো Salam IP ইউজার পাওয়া যায়নি" else "কোনো ব্যক্তিগত কন্টাক্ট পাওয়া যায়নি",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SalamTealPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("নতুন কন্টাক্ট যোগ করুন")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(contacts, key = { it.id }) { contact ->
                        ContactListItem(
                            contact = contact,
                            isIpTab = selectedTabIndex == 0,
                            onItemClick = { viewModel.selectContact(contact) },
                            onCallClick = {
                                val target = if (contact.ipNumber.isNotBlank()) contact.ipNumber else contact.phoneNumber
                                viewModel.startCall(target, contact.name)
                            },
                            onFavoriteClick = { viewModel.toggleContactFavorite(contact) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            }
        }

        // Floating Action Button: Add Contact
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_contact_fab"),
            containerColor = SalamTealPrimary,
            contentColor = Color.White
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Contact")
        }

        if (showAddDialog) {
            AddContactDialog(
                defaultIsIp = selectedTabIndex == 0,
                onDismiss = { showAddDialog = false },
                onSave = { name, phone, ip, isIp ->
                    viewModel.addContact(name, phone, ip, isFavorite = false)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun ContactListItem(
    contact: ContactEntity,
    isIpTab: Boolean,
    onItemClick: () -> Unit,
    onCallClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .testTag("contact_item_${contact.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                ContactAvatar(name = contact.name, size = 48.dp, colorHex = contact.avatarColorHex)

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = contact.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (contact.isIpUser || contact.ipNumber.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SalamCallGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "FREE IP",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SalamCallGreen
                                )
                            }
                        }
                    }

                    if (contact.ipNumber.isNotEmpty()) {
                        Text(
                            text = "IP: ${contact.ipNumber}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = SalamTealPrimary
                        )
                    } else {
                        Text(
                            text = formatBangladeshPhoneNumber(contact.phoneNumber),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Favorite Icon
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (contact.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (contact.isFavorite) SalamGold else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Call Action Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SalamCallGreen.copy(alpha = 0.15f))
                        .clickable { onCallClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call Contact",
                        tint = SalamCallGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ContactDetailScreen(
    viewModel: SalamViewModel,
    modifier: Modifier = Modifier
) {
    val contact by viewModel.selectedContact.collectAsStateWithLifecycle()

    if (contact == null) {
        viewModel.navigateTo(ScreenNav.CONTACTS)
        return
    }

    val c = contact!!

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo(ScreenNav.CONTACTS) }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = stringResource(R.string.contact_details),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Contact Hero Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ContactAvatar(name = c.name, size = 96.dp, colorHex = c.avatarColorHex)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = c.name,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            if (c.ipNumber.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SalamCallGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SALAM IP LINE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SalamCallGreen
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = c.ipNumber,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = SalamTealPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Free Audio Call Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(SalamCallGreen)
                            .clickable {
                                val target = if (c.ipNumber.isNotBlank()) c.ipNumber else c.phoneNumber
                                viewModel.startCall(target, c.name)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "Call", tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "অডিও কল", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = SalamCallGreen)
                }

                // Video Call Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(SalamTealPrimary)
                            .clickable {
                                val target = if (c.ipNumber.isNotBlank()) c.ipNumber else c.phoneNumber
                                viewModel.startVideoCall(target, c.name)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Videocam, contentDescription = "Video Call", tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "ভিডিও কল", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = SalamTealPrimary)
                }

                // Chat / Messaging Button (Photo, Video, Text)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(SalamTealPrimary.copy(alpha = 0.15f))
                            .clickable { viewModel.openChatWithContact(c) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Message, contentDescription = "Chat", tint = SalamTealPrimary, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "মেসেজ", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = SalamTealPrimary)
                }

                // Favorite Toggle Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(SalamTealPrimary.copy(alpha = 0.12f))
                            .clickable { viewModel.toggleContactFavorite(c) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (c.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (c.isFavorite) SalamGold else SalamTealPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = if (c.isFavorite) "Starred" else "Star", style = MaterialTheme.typography.labelSmall)
                }

                // Block Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(SalamEndCallRed.copy(alpha = 0.12f))
                            .clickable { viewModel.blockContact(c.phoneNumber, c.name) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Block, contentDescription = "Block", tint = SalamEndCallRed, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Block", style = MaterialTheme.typography.labelSmall, color = SalamEndCallRed)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Detail Fields
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "মোবাইল নম্বর",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatBangladeshPhoneNumber(c.phoneNumber),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (c.email.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "ইমেইল",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = c.email,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "কল রেট ও রুট",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "১০০% ফ্রি আইপি কলিং • লাইসেন্সড IPTSP রুট",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = SalamCallGreen
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Delete Contact Button
        Button(
            onClick = { viewModel.deleteContact(c) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SalamEndCallRed.copy(alpha = 0.12f), contentColor = SalamEndCallRed),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "কন্টাক্ট মুছুন (Delete Contact)")
        }
    }
}

@Composable
fun AddContactDialog(
    defaultIsIp: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var ipNumber by remember { mutableStateOf("") }
    var isIp by remember { mutableStateOf(defaultIsIp) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "নতুন কন্টাক্ট যোগ করুন", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("নাম") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("মোবাইল নম্বর (01XXXXXXXXX)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ipNumber,
                    onValueChange = { ipNumber = it },
                    label = { Text("Salam IP নম্বর (096XXXXXXXX - ঐচ্ছিক)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onSave(name, phone, ipNumber, isIp || ipNumber.isNotEmpty())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SalamTealPrimary)
            ) {
                Text("সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}
