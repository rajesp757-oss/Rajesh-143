package com.example.services

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.data.models.SupportedLanguage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

private const val TAG = "VoiceAssistantMgr"

class VoiceAssistantManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

    // State flows
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isWakeWordListening = MutableStateFlow(false)
    val isWakeWordListening: StateFlow<Boolean> = _isWakeWordListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _rmsDecibels = MutableStateFlow(0f)
    val rmsDecibels: StateFlow<Float> = _rmsDecibels.asStateFlow()

    private val _isFemaleVoice = MutableStateFlow(true)
    val isFemaleVoice: StateFlow<Boolean> = _isFemaleVoice.asStateFlow()

    private val _partialTranscript = MutableStateFlow("")
    val partialTranscript: StateFlow<String> = _partialTranscript.asStateFlow()

    // Events
    private val _onSpeechResult = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val onSpeechResult: SharedFlow<String> = _onSpeechResult.asSharedFlow()

    private val _onWakeWordDetected = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val onWakeWordDetected: SharedFlow<Unit> = _onWakeWordDetected.asSharedFlow()

    private val _errorMessages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val errorMessages: SharedFlow<String> = _errorMessages.asSharedFlow()

    private var currentMode: ListeningMode = ListeningMode.IDLE
    private var activeLanguage: SupportedLanguage = SupportedLanguage.HINDI

    private enum class ListeningMode {
        IDLE,
        WAKE_WORD,
        ACTIVE_PROMPT
    }

    init {
        initTts()
    }

    private fun initTts() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsInitialized = true
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }
                })
            } else {
                Log.e(TAG, "TTS initialization failed with code: $status")
            }
        }
    }

    private fun ensureSpeechRecognizer(): Boolean {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.e(TAG, "SpeechRecognizer is not available on this device")
            return false
        }
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createRecognitionListener())
            }
        }
        return true
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                if (currentMode == ListeningMode.ACTIVE_PROMPT) {
                    _isListening.value = true
                }
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {
                _rmsDecibels.value = rmsdB.coerceIn(0f, 10f)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _isListening.value = false
                _rmsDecibels.value = 0f
            }

            override fun onError(error: Int) {
                _isListening.value = false
                _rmsDecibels.value = 0f
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client speech error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                    SpeechRecognizer.ERROR_NETWORK -> "Network connection issue"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timed out"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech service is busy"
                    SpeechRecognizer.ERROR_SERVER -> "Recognition server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
                    else -> "Speech recognition error: $error"
                }

                if (currentMode == ListeningMode.WAKE_WORD && _isWakeWordListening.value) {
                    // In wake word mode, silently restart recognition after brief debounce
                    scope.launch {
                        delay(600)
                        if (_isWakeWordListening.value && currentMode == ListeningMode.WAKE_WORD) {
                            startWakeWordListening(activeLanguage)
                        }
                    }
                } else if (currentMode == ListeningMode.ACTIVE_PROMPT) {
                    if (error != SpeechRecognizer.ERROR_NO_MATCH && error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                        _errorMessages.tryEmit(errorMessage)
                    }
                    currentMode = ListeningMode.IDLE
                }
            }

            override fun onResults(results: Bundle?) {
                _isListening.value = false
                _rmsDecibels.value = 0f
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val topMatch = matches?.firstOrNull()?.trim().orEmpty()

                if (currentMode == ListeningMode.WAKE_WORD) {
                    if (isWakeWordContained(matches)) {
                        Log.d(TAG, "Wake word 'Hey MISS' detected in matches: $matches")
                        _onWakeWordDetected.tryEmit(Unit)
                        // Transition to active listening
                        startActiveListening(activeLanguage)
                    } else if (_isWakeWordListening.value) {
                        // Restart wake word loop
                        scope.launch {
                            delay(400)
                            if (_isWakeWordListening.value && currentMode == ListeningMode.WAKE_WORD) {
                                startWakeWordListening(activeLanguage)
                            }
                        }
                    }
                } else if (currentMode == ListeningMode.ACTIVE_PROMPT) {
                    currentMode = ListeningMode.IDLE
                    if (topMatch.isNotBlank()) {
                        _partialTranscript.value = topMatch
                        _onSpeechResult.tryEmit(topMatch)
                    }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partials = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partialText = partials?.firstOrNull().orEmpty()
                if (currentMode == ListeningMode.ACTIVE_PROMPT) {
                    _partialTranscript.value = partialText
                } else if (currentMode == ListeningMode.WAKE_WORD && isWakeWordContained(partials)) {
                    _onWakeWordDetected.tryEmit(Unit)
                    startActiveListening(activeLanguage)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    private fun isWakeWordContained(phrases: List<String>?): Boolean {
        if (phrases.isNullOrEmpty()) return false
        val wakeKeywords = listOf(
            "hey miss", "hey miss ai", "miss ai", "hello miss", "ok miss", "hi miss",
            "hey mis", "mis ai", "हे मिस", "अरे मिस", "मिस एआई", "सुनो मिस",
            "ମିସ୍", "ହେ ମିସ୍", "ନମସ୍କାର ମିସ୍", "ମିସ୍ ଏଆଇ", "ଶୁଣ ମିସ୍"
        )
        return phrases.any { phrase ->
            val clean = phrase.lowercase().replace("[^a-zA-Z0-9\\u0900-\\u097F\\u0B00-\\u0B7F\\s]".toRegex(), "")
            wakeKeywords.any { kw -> clean.contains(kw) }
        }
    }

    fun startActiveListening(language: SupportedLanguage) {
        stopTts()
        if (!ensureSpeechRecognizer()) return

        activeLanguage = language
        currentMode = ListeningMode.ACTIVE_PROMPT
        _partialTranscript.value = ""

        try {
            speechRecognizer?.cancel()
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, language.speechLocaleTag)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language.speechLocaleTag)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }
            speechRecognizer?.startListening(intent)
            _isListening.value = true
        } catch (e: Exception) {
            Log.e(TAG, "Error starting active speech listening", e)
            _isListening.value = false
            currentMode = ListeningMode.IDLE
        }
    }

    fun startWakeWordListening(language: SupportedLanguage) {
        if (!ensureSpeechRecognizer()) return

        activeLanguage = language
        currentMode = ListeningMode.WAKE_WORD
        _isWakeWordListening.value = true

        try {
            speechRecognizer?.cancel()
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, language.speechLocaleTag)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting wake word listening", e)
        }
    }

    fun stopWakeWordListening() {
        _isWakeWordListening.value = false
        if (currentMode == ListeningMode.WAKE_WORD) {
            currentMode = ListeningMode.IDLE
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.cancel()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping wake word listener", e)
            }
        }
    }

    fun stopListening() {
        _isListening.value = false
        _rmsDecibels.value = 0f
        currentMode = ListeningMode.IDLE
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping listener", e)
        }
    }

    fun setFemaleVoice(female: Boolean) {
        _isFemaleVoice.value = female
    }

    fun speak(text: String, language: SupportedLanguage) {
        if (!isTtsInitialized || text.isBlank()) return

        stopTts()

        val locale = when (language) {
            SupportedLanguage.HINDI -> Locale("hi", "IN")
            SupportedLanguage.ODIA -> {
                // Try Odia locale; if not supported in device engine, fall back cleanly
                val odiaLocale = Locale("or", "IN")
                val res = textToSpeech?.isLanguageAvailable(odiaLocale)
                if (res == TextToSpeech.LANG_AVAILABLE || res == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                    odiaLocale
                } else {
                    Locale("hi", "IN")
                }
            }
            SupportedLanguage.ENGLISH -> Locale("en", "IN")
        }

        try {
            textToSpeech?.language = locale

            val isFemale = _isFemaleVoice.value
            if (isFemale) {
                textToSpeech?.setPitch(1.25f)
                textToSpeech?.setSpeechRate(1.02f)
                val availableVoices = textToSpeech?.voices
                if (!availableVoices.isNullOrEmpty()) {
                    val targetVoice = availableVoices.firstOrNull { voice ->
                        voice.locale.language.equals(locale.language, ignoreCase = true) &&
                        (voice.name.contains("female", ignoreCase = true) ||
                         voice.name.contains("fem", ignoreCase = true) ||
                         voice.name.contains("woman", ignoreCase = true) ||
                         voice.name.contains("-f-", ignoreCase = true) ||
                         voice.features?.any { it.contains("female", ignoreCase = true) } == true)
                    } ?: availableVoices.firstOrNull { voice ->
                        voice.locale.language.equals(locale.language, ignoreCase = true) &&
                        !voice.name.contains("male", ignoreCase = true)
                    }
                    if (targetVoice != null) {
                        textToSpeech?.voice = targetVoice
                    }
                }
            } else {
                textToSpeech?.setPitch(1.0f)
                textToSpeech?.setSpeechRate(1.0f)
            }

            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "MISS_UTTERANCE_${System.currentTimeMillis()}")
            _isSpeaking.value = true
        } catch (e: Exception) {
            Log.e(TAG, "Error in TTS speak", e)
            _isSpeaking.value = false
        }
    }

    fun stopTts() {
        try {
            textToSpeech?.stop()
            _isSpeaking.value = false
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping TTS", e)
        }
    }

    fun destroy() {
        try {
            stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
            textToSpeech?.stop()
            textToSpeech?.shutdown()
            textToSpeech = null
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
}
