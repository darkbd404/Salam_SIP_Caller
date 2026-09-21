package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import coil.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.SalamViewModel
import com.example.ui.ScreenNav
import com.example.ui.theme.SalamCallGreen
import com.example.ui.theme.SalamCyanAccent
import com.example.ui.theme.SalamEndCallRed
import com.example.ui.theme.SalamTealDark
import com.example.ui.theme.SalamTealPrimary

// ====================================================================
// 1. SIGNUP SCREEN (লাইনআপ অপশন)
// ====================================================================
@Composable
fun SignupScreen(
    viewModel: SalamViewModel,
    modifier: Modifier = Modifier
) {
    val name by viewModel.regName.collectAsStateWithLifecycle()
    val mobile by viewModel.regMobile.collectAsStateWithLifecycle()
    val email by viewModel.regEmail.collectAsStateWithLifecycle()
    val password by viewModel.regPassword.collectAsStateWithLifecycle()
    val customIpDigits by viewModel.regCustomIpDigits.collectAsStateWithLifecycle()
    val nidFrontUri by viewModel.regNidFrontUri.collectAsStateWithLifecycle()
    val nidBackUri by viewModel.regNidBackUri.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()

    var passwordVisible by remember { mutableStateOf(false) }

    val nidFrontPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.regNidFrontUri.value = uri.toString()
        } else if (viewModel.regNidFrontUri.value == null) {
            viewModel.regNidFrontUri.value = "nid_front_selected_${System.currentTimeMillis()}.jpg"
        }
    }

    val nidBackPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.regNidBackUri.value = uri.toString()
        } else if (viewModel.regNidBackUri.value == null) {
            viewModel.regNidBackUri.value = "nid_back_selected_${System.currentTimeMillis()}.jpg"
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Branding
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(SalamTealPrimary, SalamCallGreen)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneInTalk,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "লাইনআপ (Sign Up Registration)",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "ফ্রি IP-to-IP কলিং অ্যাকাউন্ট তৈরি করুন",
                        style = MaterialTheme.typography.bodySmall,
                        color = SalamTealPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Information Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "রেজিস্ট্রেশন তথ্য পূরণ করুন",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. Name (নাম)
                    OutlinedTextField(
                        value = name,
                        onValueChange = { viewModel.regName.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_name_input"),
                        label = { Text("নাম (Full Name)") },
                        placeholder = { Text("আপনার নাম লিখুন") },
                        leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Mobile Number (মোবাইল নম্বর)
                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { viewModel.regMobile.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_mobile_input"),
                        label = { Text("মোবাইল নম্বর (Mobile Number)") },
                        placeholder = { Text("01XXXXXXXXX") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Gmail (জিমেইল)
                    OutlinedTextField(
                        value = email,
                        onValueChange = { viewModel.regEmail.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_email_input"),
                        label = { Text("জিমেইল (Gmail)") },
                        placeholder = { Text("example@gmail.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. Password (পাসওয়ার্ড)
                    OutlinedTextField(
                        value = password,
                        onValueChange = { viewModel.regPassword.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_password_input"),
                        label = { Text("পাসওয়ার্ড (Password)") },
                        placeholder = { Text("কমপক্ষে ৪ ডিজিট বা অক্ষর") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Custom IP Number: 09612 + পছন্দ মত ডিজিট
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SalamTealPrimary.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PhoneInTalk, contentDescription = null, tint = SalamTealPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "09612 পছন্দ মত আইপি নাম্বার নির্বাচন করুন:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = SalamTealPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Prefix Box
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SalamTealPrimary)
                                .padding(horizontal = 14.dp, vertical = 14.dp)
                        ) {
                            Text(
                                text = "09612",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Custom Digits Input
                        OutlinedTextField(
                            value = customIpDigits,
                            onValueChange = { if (it.length <= 8) viewModel.regCustomIpDigits.value = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reg_ip_digits_input"),
                            label = { Text("ইচ্ছামতো বা পছন্দমত নম্বর") },
                            placeholder = { Text("যেমন: 888999") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    Text(
                        text = "আপনার সম্পূর্ণ আইপি নম্বর হবে: 09612${customIpDigits.ifEmpty { "XXXXXX" }}",
                        fontSize = 12.sp,
                        color = SalamCallGreen,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 6. National ID Card: সামনের Part ও পিছনের Part
            Text(
                text = "National ID Card (সামনের part ও পিছনের part):",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Front Part
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            try {
                                nidFrontPicker.launch("image/*")
                            } catch (_: Exception) {
                                viewModel.regNidFrontUri.value = "nid_front_photo_${System.currentTimeMillis()}.jpg"
                            }
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (nidFrontUri != null) SalamCallGreen.copy(alpha = 0.08f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    if (nidFrontUri != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.Black.copy(alpha = 0.05f))
                            ) {
                                AsyncImage(
                                    model = nidFrontUri,
                                    contentDescription = "NID সামনের ছবি",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .background(SalamCallGreen, CircleShape)
                                        .size(20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "NID সামনের ছবি",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = SalamCallGreen
                            )
                            Text(
                                text = "পরিবর্তন করতে ট্যাপ করুন",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = null,
                                tint = SalamTealPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "NID সামনের Part",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "ছবি সিলেক্ট করুন",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Back Part
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            try {
                                nidBackPicker.launch("image/*")
                            } catch (_: Exception) {
                                viewModel.regNidBackUri.value = "nid_back_photo_${System.currentTimeMillis()}.jpg"
                            }
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (nidBackUri != null) SalamCallGreen.copy(alpha = 0.08f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    if (nidBackUri != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.Black.copy(alpha = 0.05f))
                            ) {
                                AsyncImage(
                                    model = nidBackUri,
                                    contentDescription = "NID পিছনের ছবি",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .background(SalamCallGreen, CircleShape)
                                        .size(20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "NID পিছনের ছবি",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = SalamCallGreen
                            )
                            Text(
                                text = "পরিবর্তন করতে ট্যাপ করুন",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = null,
                                tint = SalamTealPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "NID পিছনের Part",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "ছবি সিলেক্ট করুন",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = authError != null) {
                authError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 7. Registration Button (রেজিস্ট্রেশন বাটন)
            Button(
                onClick = { viewModel.submitSignupAndSendGmailVerification() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("reg_submit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = SalamTealPrimary),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "রেজিস্ট্রেশন করুন (Send Gmail Verification)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Already have an account? Go to Login
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "ইতিমধ্যে অ্যাকাউন্ট আছে?",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = { viewModel.navigateTo(ScreenNav.AUTH_LOGIN) },
                    modifier = Modifier.testTag("goto_login_button")
                ) {
                    Text(
                        text = "লগইন করুন (Login)",
                        fontWeight = FontWeight.Bold,
                        color = SalamTealPrimary
                    )
                }
            }
        }
    }
}

// ====================================================================
// 2. GMAIL VERIFICATION SCREEN (জিমেইল ভেরিফিকেশন)
// ====================================================================
@Composable
fun EmailVerificationScreen(
    viewModel: SalamViewModel,
    modifier: Modifier = Modifier
) {
    val email by viewModel.regEmail.collectAsStateWithLifecycle()
    val generatedOtp by viewModel.regGeneratedOtp.collectAsStateWithLifecycle()
    val verificationInput by viewModel.regVerificationInput.collectAsStateWithLifecycle()
    val notice by viewModel.emailVerificationNotice.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(SalamTealPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MarkEmailRead,
                    contentDescription = null,
                    tint = SalamTealPrimary,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "জিমেইল ভেরিফিকেশন (Gmail Verification)",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "একটি ৬ ডিজিটের সিকিউরিটি কোড আপনার জিমেইলে পাঠানো হয়েছে। কোডটি দিয়ে ভেরিফিকেশন সম্পন্ন করুন।",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Active Gmail Notification simulation banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SalamCallGreen.copy(alpha = 0.12f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = SalamCallGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "জিমেইল ইনবক্স নোটিফিকেশন:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = SalamCallGreen
                        )
                        Text(
                            text = "প্রাপক: $email\nআপনার সালাম কল ভেরিফিকেশন কোড: $generatedOtp",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Code Input
            OutlinedTextField(
                value = verificationInput,
                onValueChange = { if (it.length <= 6) viewModel.regVerificationInput.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gmail_otp_input"),
                label = { Text("৬ ডিজিটের ভেরিফিকেশন কোড লিখুন") },
                placeholder = { Text(generatedOtp) },
                leadingIcon = { Icon(Icons.Default.Security, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            AnimatedVisibility(visible = authError != null) {
                authError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Verify and Complete Button
            Button(
                onClick = { viewModel.verifyGmailAndCompleteRegistration() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("verify_gmail_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = SalamCallGreen),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "ভেরিফিকেশন সম্পন্ন করুন (Complete)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = { viewModel.navigateTo(ScreenNav.AUTH_SIGNUP) }
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("তথ্য সংশোধন করতে ফিরে যান")
            }
        }
    }
}

// ====================================================================
// 3. LOGIN SCREEN (লগইন অপশন)
// ====================================================================
@Composable
fun LoginScreen(
    viewModel: SalamViewModel,
    modifier: Modifier = Modifier
) {
    val loginId by viewModel.loginIdentifier.collectAsStateWithLifecycle()
    val loginPassword by viewModel.loginPassword.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()
    val emailNotice by viewModel.emailVerificationNotice.collectAsStateWithLifecycle()

    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(SalamTealPrimary, SalamCallGreen)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneInTalk,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "SALAM CALL",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                ),
                color = SalamTealPrimary
            )

            Text(
                text = "১০০% ফ্রি আইপি-টু-আইপি আনলিমিটেড কলিং",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(visible = emailNotice != null) {
                emailNotice?.let {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SalamCallGreen.copy(alpha = 0.12f))
                    ) {
                        Text(
                            text = it,
                            fontSize = 12.sp,
                            color = SalamCallGreen,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // Login Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "লগইন করুন (Login)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. IP Number or Gmail (IP number / Gmail)
                    OutlinedTextField(
                        value = loginId,
                        onValueChange = { viewModel.loginIdentifier.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_id_input"),
                        label = { Text("IP নম্বর অথবা জিমেইল (IP number / Gmail)") },
                        placeholder = { Text("09612... অথবা email@gmail.com") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null)
                        },
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Password (পাসওয়ার্ড)
                    OutlinedTextField(
                        value = loginPassword,
                        onValueChange = { viewModel.loginPassword.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input"),
                        label = { Text("পাসওয়ার্ড (Password)") },
                        placeholder = { Text("আপনার পাসওয়ার্ড লিখুন") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                        },
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

                    AnimatedVisibility(visible = authError != null) {
                        authError?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 3. Login Button
                    Button(
                        onClick = { viewModel.loginWithUserJsonMatch() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_submit_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = SalamTealPrimary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "লগইন করুন (Login)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Create new account / Lineup
            OutlinedButton(
                onClick = { viewModel.navigateTo(ScreenNav.AUTH_SIGNUP) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("goto_signup_btn"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Default.Badge, contentDescription = null, tint = SalamTealPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "নতুন অ্যাকাউন্ট লাইনআপ করুন (Sign Up)",
                    fontWeight = FontWeight.SemiBold,
                    color = SalamTealPrimary
                )
            }
        }
    }
}
