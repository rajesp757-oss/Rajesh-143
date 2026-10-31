package com.example.ui

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.ChatMessage
import com.example.data.models.InstalledAppInfo
import com.example.data.models.LauncherAction
import com.example.data.models.LauncherActionType
import com.example.data.models.SupportedLanguage
import com.example.services.LauncherManager
import com.example.services.VoiceAssistantManager
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

private const val TAG = "AiAssistantViewModel"

enum class NavTab {
    VOICE_CHAT,
    PHONE_LAUNCHER,
    SETTINGS
}

private const val SYSTEM_PROMPT = """
You are MISS AI (Multilingual Intelligent Smart System), a smart, warm, polite, and caring female personal AI voice assistant (महिला एआई सहायक / ମହିଳା ଏଆଇ ସହାୟିକା) for Android smartphones.
You speak with a sweet, friendly, natural feminine tone.
You specialize in Hindi (हिन्दी) and Odia (ଓଡ଼ିଆ), along with English and Hinglish.

Core Guidelines:
1. When spoken to in Hindi (हिन्दी), answer directly in natural, authentic, polite Hindi in Devanagari script (e.g. "नमस्ते! मैं मिस एआई हूँ, मैं आपकी सहायता के लिए तैयार हूँ।").
2. When spoken to in Odia (ଓଡ଼ିଆ), answer directly in polite, authentic Odia in Odia script (e.g. "ନମସ୍କାର! ମୁଁ ମିସ୍ ଏଆଇ, ଆପଣଙ୍କୁ ସାହାଯ୍ୟ କରିବା ମୋ ପାଇଁ ଆନନ୍ଦର କଥା।").
3. When spoken to in English or mixed Hinglish/Odia-English, respond naturally and intelligently.
4. Keep spoken responses concise, friendly, and engaging (1-3 clear sentences) since responses are read aloud via Text-to-Speech.
5. If the user asks to launch or perform a phone task, acknowledge it warmly in the chosen language and append one of these action tags at the very end:
   - [ACTION:PHONE] -> for calling, dialer, फोन कॉल, ଫୋନ୍
   - [ACTION:CAMERA] -> for camera, photo, तस्वीर, କ୍ୟାମେରା, ଫଟୋ
   - [ACTION:MESSAGES] -> for sms, messaging, संदेश, ମେସେଜ୍
   - [ACTION:CONTACTS] -> for address book, contacts, संपर्क, କଣ୍ଟାକ୍ଟସ୍
   - [ACTION:SETTINGS] -> for phone settings, सेटिंग्स, ସେଟିଂସ୍
   - [ACTION:CLOCK] -> for clock, alarm, timer, अलार्म, ଘଣ୍ଟା, ଆଲାର୍ମ
   - [ACTION:CALCULATOR] -> for math calculations, calculator, कैलकुलेटर, କାଲକୁଲେଟର୍
   - [ACTION:BROWSER:https://...] -> for web searches or web pages
6. Never make up sensitive personal device data. Be charming, respectful, and helpful.
"""

class AiAssistantViewModel(application: Application) : AndroidViewModel(application) {

    val voiceManager = VoiceAssistantManager(application.applicationContext)

    // Lazy initialization of Gemini model to prevent early initialization crashes
    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = com.google.firebase.ai.type.Content(
                role = "system",
                parts = listOf(com.google.firebase.ai.type.TextPart(SYSTEM_PROMPT))
            )
        )
    }

    // State
    private val _selectedTab = MutableStateFlow(NavTab.VOICE_CHAT)
    val selectedTab: StateFlow<NavTab> = _selectedTab.asStateFlow()

    private val _currentLanguage = MutableStateFlow(SupportedLanguage.HINDI)
    val currentLanguage: StateFlow<SupportedLanguage> = _currentLanguage.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _isWakeWordEnabled = MutableStateFlow(false)
    val isWakeWordEnabled: StateFlow<Boolean> = _isWakeWordEnabled.asStateFlow()

    private val _autoSpeakEnabled = MutableStateFlow(true)
    val autoSpeakEnabled: StateFlow<Boolean> = _autoSpeakEnabled.asStateFlow()

    private val _installedApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppInfo>> = _installedApps.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _actionEvents = MutableSharedFlow<LauncherAction>(extraBufferCapacity = 1)
    val actionEvents: SharedFlow<LauncherAction> = _actionEvents.asSharedFlow()

    private val _hasAudioPermission = MutableStateFlow(false)
    val hasAudioPermission: StateFlow<Boolean> = _hasAudioPermission.asStateFlow()
    val isFemaleVoice: StateFlow<Boolean> = voiceManager.isFemaleVoice

    init {
        // Initial welcome message in selected language
        postWelcomeMessage(_currentLanguage.value)
        loadInstalledApps()
        observeVoiceEvents()
    }

    private fun postWelcomeMessage(lang: SupportedLanguage) {
        val welcomeMsg = ChatMessage(
            text = lang.greeting,
            isUser = false,
            language = lang
        )
        _messages.value = listOf(welcomeMsg)
    }

    private fun observeVoiceEvents() {
        viewModelScope.launch {
            voiceManager.onSpeechResult.collect { recognizedText ->
                if (recognizedText.isNotBlank()) {
                    sendMessage(recognizedText)
                }
            }
        }

        viewModelScope.launch {
            voiceManager.onWakeWordDetected.collect {
                // Wake word "Hey MISS" triggered
                Log.d(TAG, "Wake word triggered UI feedback")
            }
        }
    }

    fun setAudioPermission(granted: Boolean) {
        _hasAudioPermission.value = granted
        if (!granted && _isWakeWordEnabled.value) {
            setWakeWordEnabled(false)
        }
    }

    fun selectTab(tab: NavTab) {
        _selectedTab.value = tab
    }

    fun setLanguage(language: SupportedLanguage) {
        if (_currentLanguage.value != language) {
            _currentLanguage.value = language
            postWelcomeMessage(language)
            if (_isWakeWordEnabled.value) {
                // Restart wake word listener with new locale
                voiceManager.stopWakeWordListening()
                voiceManager.startWakeWordListening(language)
            }
        }
    }

    fun setWakeWordEnabled(enabled: Boolean) {
        _isWakeWordEnabled.value = enabled
        if (enabled) {
            voiceManager.startWakeWordListening(_currentLanguage.value)
        } else {
            voiceManager.stopWakeWordListening()
        }
    }

    fun setAutoSpeakEnabled(enabled: Boolean) {
        _autoSpeakEnabled.value = enabled
        if (!enabled) {
            voiceManager.stopTts()
        }
    }

    fun setFemaleVoice(female: Boolean) {
        voiceManager.setFemaleVoice(female)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun startVoiceInput() {
        voiceManager.startActiveListening(_currentLanguage.value)
    }

    fun stopVoiceInput() {
        voiceManager.stopListening()
    }

    fun speakMessage(message: ChatMessage) {
        voiceManager.speak(message.text, message.language)
    }

    fun stopSpeaking() {
        voiceManager.stopTts()
    }

    fun clearChat() {
        voiceManager.stopTts()
        postWelcomeMessage(_currentLanguage.value)
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        val trimmed = userText.trim()
        val userMsg = ChatMessage(
            text = trimmed,
            isUser = true,
            language = _currentLanguage.value
        )
        _messages.value = _messages.value + userMsg

        // AI Inference
        viewModelScope.launch {
            _isGenerating.value = true
            val botMsgId = java.util.UUID.randomUUID().toString()
            val initialBotMsg = ChatMessage(
                id = botMsgId,
                text = "...",
                isUser = false,
                language = _currentLanguage.value,
                isStreaming = true
            )
            _messages.value = _messages.value + initialBotMsg

            try {
                val prompt = "User's selected language: ${_currentLanguage.value.englishName} (${_currentLanguage.value.nativeName})\nUser says: $trimmed"
                // 30-60s timeout for AI response
                val response = withTimeout(60_000L) {
                    model.generateContent(prompt)
                }

                val fullResponseText = response.text.orEmpty()
                val (cleanText, action) = parseActionFromResponse(fullResponseText)

                val finalBotMsg = ChatMessage(
                    id = botMsgId,
                    text = if (cleanText.isNotBlank()) cleanText else "I'm here to help!",
                    isUser = false,
                    language = _currentLanguage.value,
                    suggestedAction = action,
                    isStreaming = false
                )

                _messages.value = _messages.value.map { if (it.id == botMsgId) finalBotMsg else it }

                // Auto speak aloud if enabled
                if (_autoSpeakEnabled.value && cleanText.isNotBlank()) {
                    voiceManager.speak(cleanText, _currentLanguage.value)
                }

                // If action was detected, emit action event
                if (action != null) {
                    _actionEvents.tryEmit(action)
                }

            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "AI Generation timed out", e)
                val errorMsg = when (_currentLanguage.value) {
                    SupportedLanguage.HINDI -> "क्षमा करें, उत्तर देने में समय अधिक लग गया। कृपया पुनः प्रयास करें।"
                    SupportedLanguage.ODIA -> "କ୍ଷମା କରନ୍ତୁ, ଉତ୍ତର ଦେବାକୁ ଅଧିକ ସମୟ ଲାଗିଲା। ଦୟାକରି ପୁଣି ଚେଷ୍ଟା କରନ୍ତୁ।"
                    SupportedLanguage.ENGLISH -> "Request timed out. Please try again."
                }
                updateErrorMessage(botMsgId, errorMsg)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI Generation failed", e)
                val errorMsg = when (_currentLanguage.value) {
                    SupportedLanguage.HINDI -> "त्रुटि: ${e.localizedMessage ?: "संपर्क नहीं हो सका"}"
                    SupportedLanguage.ODIA -> "ତ୍ରୁଟି: ${e.localizedMessage ?: "ସଂଯୋଗ ହୋଇପାରିଲା ନାହିଁ"}"
                    SupportedLanguage.ENGLISH -> "Error: ${e.localizedMessage ?: "Failed to generate response"}"
                }
                updateErrorMessage(botMsgId, errorMsg)
            } finally {
                _isGenerating.value = false
            }
        }
    }

    private fun updateErrorMessage(msgId: String, errorText: String) {
        val errorBotMsg = ChatMessage(
            id = msgId,
            text = errorText,
            isUser = false,
            language = _currentLanguage.value,
            isStreaming = false
        )
        _messages.value = _messages.value.map { if (it.id == msgId) errorBotMsg else it }
    }

    private fun parseActionFromResponse(rawText: String): Pair<String, LauncherAction?> {
        val actionRegex = "\\[ACTION:([A-Z_]+)(?::(.*))?]".toRegex()
        val match = actionRegex.find(rawText)

        if (match != null) {
            val actionKey = match.groupValues[1]
            val payload = match.groupValues.getOrNull(2).orEmpty()
            val cleanText = rawText.replace(match.value, "").trim()

            val actionType = when (actionKey) {
                "PHONE" -> LauncherActionType.PHONE
                "CAMERA" -> LauncherActionType.CAMERA
                "MESSAGES" -> LauncherActionType.MESSAGES
                "CONTACTS" -> LauncherActionType.CONTACTS
                "SETTINGS" -> LauncherActionType.SETTINGS
                "CLOCK" -> LauncherActionType.CLOCK
                "CALCULATOR" -> LauncherActionType.CALCULATOR
                "BROWSER" -> LauncherActionType.BROWSER
                else -> null
            }

            val launcherAction = actionType?.let { LauncherAction(it, payload) }
            return Pair(cleanText, launcherAction)
        }

        return Pair(rawText.trim(), null)
    }

    fun executeLauncherAction(context: Context, action: LauncherAction) {
        LauncherManager.executeAction(context, action.type, action.payload)
    }

    fun launchInstalledApp(context: Context, packageName: String) {
        LauncherManager.launchAppByPackage(context, packageName)
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            val apps = LauncherManager.getInstalledApps(getApplication())
            _installedApps.value = apps
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
    }
}
