package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.call.SipCallEngine
import com.example.data.local.SalamDatabase
import com.example.data.repository.SalamRepository
import com.example.ui.SalamCallApp
import com.example.ui.SalamViewModel
import com.example.ui.SalamViewModelFactory
import com.example.ui.theme.SalamCallTheme

class MainActivity : ComponentActivity() {

    private val viewModel: SalamViewModel by viewModels {
        val database = SalamDatabase.getDatabase(applicationContext)
        val repository = SalamRepository(
            context = applicationContext,
            userDao = database.userDao(),
            contactDao = database.contactDao(),
            callDao = database.callDao(),
            transactionDao = database.transactionDao(),
            packageDao = database.packageDao(),
            supportTicketDao = database.supportTicketDao(),
            blockedNumberDao = database.blockedNumberDao(),
            messageDao = database.messageDao(),
            callRecordingDao = database.callRecordingDao()
        )
        val callEngine = SipCallEngine(applicationContext)
        SalamViewModelFactory(repository, callEngine)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkPref by viewModel.isDarkMode.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val darkTheme = isDarkPref || systemDark

            SalamCallTheme(darkTheme = darkTheme) {
                SalamCallApp(viewModel = viewModel)
            }
        }
    }
}

