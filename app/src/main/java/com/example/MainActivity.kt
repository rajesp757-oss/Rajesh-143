package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.models.SupportedLanguage
import com.example.ui.AiAssistantViewModel
import com.example.ui.NavTab
import com.example.ui.components.PhoneLauncherView
import com.example.ui.components.VoiceChatView
import com.example.ui.components.WakeWordSettingsView
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletNeon

class MainActivity : ComponentActivity() {

    private val viewModel: AiAssistantViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // MANDATORY: App Check must be configured before Compose UI or any Firebase calls
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: AiAssistantViewModel) {
    val context = LocalContext.current

    val selectedTab by viewModel.selectedTab.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val isWakeWordEnabled by viewModel.isWakeWordEnabled.collectAsState()
    val autoSpeakEnabled by viewModel.autoSpeakEnabled.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val hasAudioPermission by viewModel.hasAudioPermission.collectAsState()
    val isFemaleVoice by viewModel.isFemaleVoice.collectAsState()

    val isListening by viewModel.voiceManager.isListening.collectAsState()
    val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsState()
    val rmsDecibels by viewModel.voiceManager.rmsDecibels.collectAsState()
    val partialTranscript by viewModel.voiceManager.partialTranscript.collectAsState()

    // Permission launcher for microphone (RECORD_AUDIO)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.setAudioPermission(isGranted)
        if (isGranted) {
            Toast.makeText(context, "Microphone permission granted for MISS AI", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Microphone permission is needed for voice chat", Toast.LENGTH_LONG).show()
        }
    }

    // Check initial permission
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.setAudioPermission(granted)
    }

    // Observe error events from speech recognizer
    LaunchedEffect(Unit) {
        viewModel.voiceManager.errorMessages.collect { err ->
            Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
        }
    }

    // Observe launcher actions from AI suggestions
    LaunchedEffect(Unit) {
        viewModel.actionEvents.collect { action ->
            viewModel.executeLauncherAction(context, action)
        }
    }

    // Predictive Back Handling
    BackHandler(enabled = selectedTab != NavTab.VOICE_CHAT) {
        viewModel.selectTab(NavTab.VOICE_CHAT)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar")
            ) {
                // 1. Voice AI
                val isVoiceSelected = selectedTab == NavTab.VOICE_CHAT
                NavigationBarItem(
                    selected = isVoiceSelected,
                    onClick = { viewModel.selectTab(NavTab.VOICE_CHAT) },
                    icon = {
                        Icon(
                            imageVector = if (isVoiceSelected) Icons.Filled.RecordVoiceOver else Icons.Outlined.RecordVoiceOver,
                            contentDescription = "Voice AI"
                        )
                    },
                    label = {
                        Text(
                            text = when (currentLanguage) {
                                SupportedLanguage.HINDI -> "आवाज़ (Voice)"
                                SupportedLanguage.ODIA -> "କଣ୍ଠସ୍ୱର (Voice)"
                                SupportedLanguage.ENGLISH -> "Voice AI"
                            },
                            fontSize = 11.sp,
                            fontWeight = if (isVoiceSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00363D),
                        selectedTextColor = CyanBright,
                        indicatorColor = CyanNeon,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_voice")
                )

                // 2. Phone Launcher
                val isLauncherSelected = selectedTab == NavTab.PHONE_LAUNCHER
                NavigationBarItem(
                    selected = isLauncherSelected,
                    onClick = { viewModel.selectTab(NavTab.PHONE_LAUNCHER) },
                    icon = {
                        Icon(
                            imageVector = if (isLauncherSelected) Icons.Filled.Apps else Icons.Outlined.Apps,
                            contentDescription = "Launcher"
                        )
                    },
                    label = {
                        Text(
                            text = when (currentLanguage) {
                                SupportedLanguage.HINDI -> "लॉन्चर (Apps)"
                                SupportedLanguage.ODIA -> "ଲଞ୍ଚର୍ (Apps)"
                                SupportedLanguage.ENGLISH -> "Launcher"
                            },
                            fontSize = 11.sp,
                            fontWeight = if (isLauncherSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00363D),
                        selectedTextColor = CyanBright,
                        indicatorColor = CyanNeon,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_launcher")
                )

                // 3. Settings
                val isSettingsSelected = selectedTab == NavTab.SETTINGS
                NavigationBarItem(
                    selected = isSettingsSelected,
                    onClick = { viewModel.selectTab(NavTab.SETTINGS) },
                    icon = {
                        Icon(
                            imageVector = if (isSettingsSelected) Icons.Filled.Settings else Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    label = {
                        Text(
                            text = when (currentLanguage) {
                                SupportedLanguage.HINDI -> "सेटिंग्स (Setup)"
                                SupportedLanguage.ODIA -> "ସେଟିଂସ୍ (Setup)"
                                SupportedLanguage.ENGLISH -> "Settings"
                            },
                            fontSize = 11.sp,
                            fontWeight = if (isSettingsSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00363D),
                        selectedTextColor = CyanBright,
                        indicatorColor = CyanNeon,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                NavTab.VOICE_CHAT -> {
                    VoiceChatView(
                        messages = messages,
                        currentLanguage = currentLanguage,
                        isListening = isListening,
                        isGenerating = isGenerating,
                        isSpeaking = isSpeaking,
                        isWakeWordEnabled = isWakeWordEnabled,
                        isFemaleVoice = isFemaleVoice,
                        rmsDecibels = rmsDecibels,
                        partialTranscript = partialTranscript,
                        onLanguageSelected = { viewModel.setLanguage(it) },
                        onSendMessage = { viewModel.sendMessage(it) },
                        onStartVoice = {
                            if (hasAudioPermission) {
                                viewModel.startVoiceInput()
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        onStopVoice = { viewModel.stopVoiceInput() },
                        onSpeakMessage = { viewModel.speakMessage(it) },
                        onStopSpeaking = { viewModel.stopSpeaking() },
                        onToggleWakeWord = { enable ->
                            if (enable && !hasAudioPermission) {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else {
                                viewModel.setWakeWordEnabled(enable)
                            }
                        },
                        onToggleFemaleVoice = { viewModel.setFemaleVoice(it) },
                        onExecuteAction = { action ->
                            viewModel.executeLauncherAction(context, action)
                        }
                    )
                }

                NavTab.PHONE_LAUNCHER -> {
                    PhoneLauncherView(
                        installedApps = installedApps,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                        onExecuteAction = { type ->
                            viewModel.executeLauncherAction(
                                context,
                                com.example.data.models.LauncherAction(type)
                            )
                        },
                        onLaunchPackage = { pkg ->
                            viewModel.launchInstalledApp(context, pkg)
                        }
                    )
                }

                NavTab.SETTINGS -> {
                    WakeWordSettingsView(
                        isWakeWordEnabled = isWakeWordEnabled,
                        autoSpeakEnabled = autoSpeakEnabled,
                        isFemaleVoice = isFemaleVoice,
                        currentLanguage = currentLanguage,
                        hasAudioPermission = hasAudioPermission,
                        onRequestPermission = {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        },
                        onToggleWakeWord = { enable ->
                            if (enable && !hasAudioPermission) {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else {
                                viewModel.setWakeWordEnabled(enable)
                            }
                        },
                        onToggleAutoSpeak = { viewModel.setAutoSpeakEnabled(it) },
                        onToggleFemaleVoice = { viewModel.setFemaleVoice(it) },
                        onTestVoice = {
                            val testPhrase = when (currentLanguage) {
                                SupportedLanguage.HINDI -> if (isFemaleVoice) "नमस्ते! मैं मिस एआई हूँ, यह मेरी महिला आवाज़ है।" else "नमस्ते! मैं मिस एआई हूँ। आपकी आवाज़ एकदम साफ़ आ रही है।"
                                SupportedLanguage.ODIA -> if (isFemaleVoice) "ନମସ୍କାର! ମୁଁ ମିସ୍ ଏଆଇ, ଏହା ମୋର ମହିଳା ସ୍ୱର।" else "ନମସ୍କାର! ମୁଁ ମିସ୍ ଏଆଇ। ମୋର ସ୍ୱର ଠିକ୍ ଭାବରେ ଶୁଣାଯାଉଛି।"
                                SupportedLanguage.ENGLISH -> if (isFemaleVoice) "Hello! I am MISS AI, speaking with female voice." else "Hello! I am MISS AI. Your voice assistant is working perfectly."
                            }
                            viewModel.voiceManager.speak(testPhrase, currentLanguage)
                        },
                        onClearChat = { viewModel.clearChat() }
                    )
                }
            }
        }
    }
}
