package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Woman
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.models.ChatMessage
import com.example.data.models.LauncherAction
import com.example.data.models.SupportedLanguage
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletGlow
import com.example.ui.theme.VioletNeon

@Composable
fun VoiceChatView(
    messages: List<ChatMessage>,
    currentLanguage: SupportedLanguage,
    isListening: Boolean,
    isGenerating: Boolean,
    isSpeaking: Boolean,
    isWakeWordEnabled: Boolean,
    isFemaleVoice: Boolean,
    rmsDecibels: Float,
    partialTranscript: String,
    onLanguageSelected: (SupportedLanguage) -> Unit,
    onSendMessage: (String) -> Unit,
    onStartVoice: () -> Unit,
    onStopVoice: () -> Unit,
    onSpeakMessage: (ChatMessage) -> Unit,
    onStopSpeaking: () -> Unit,
    onToggleWakeWord: (Boolean) -> Unit,
    onToggleFemaleVoice: (Boolean) -> Unit,
    onExecuteAction: (LauncherAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var textInput by remember { mutableStateOf("") }

    // Auto-scroll when new messages arrive
    LaunchedEffect(messages.size, partialTranscript) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val suggestionChips = remember(currentLanguage) {
        when (currentLanguage) {
            SupportedLanguage.HINDI -> listOf(
                "नमस्ते! आप क्या कर सकती हैं? ✨",
                "कैमरा चालू करो 📷",
                "फ़ोन डायल खोलो 📞",
                "एक प्यारा सा चुटकुला सुनाओ 😆",
                "आज का मौसम कैसा रहेगा? ☀️",
                "संदेश ऐप खोलो 💬",
                "घड़ी और अलार्म खोलो ⏰"
            )
            SupportedLanguage.ODIA -> listOf(
                "ନମସ୍କାର! ତୁମେ କଣ କରିପାରିବ? ✨",
                "କ୍ୟାମେରା ଖୋଲ 📷",
                "ଫୋନ୍ ଡାୟାଲ୍ ଖୋଲ 📞",
                "ଗୋଟିଏ ସୁନ୍ଦର କବିତା ଶୁଣାଅ 🎵",
                "ମୋତେ ଗୋଟିଏ ମଜାଳିଆ କଥା କୁହ 😂",
                "ମେସେଜ୍ ଆପ୍ ଖୋଲ 💬",
                "ସେଟିଂସ୍ ଖୋଲ ⚙️"
            )
            SupportedLanguage.ENGLISH -> listOf(
                "Hello! How can you help me? ✨",
                "Open Camera 📷",
                "Open Phone Dialer 📞",
                "Tell me a fun story 😄",
                "Open Messages 💬",
                "Open Settings ⚙️"
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header: Assistant Identity & Controls
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Main Header Row: Avatar, Identity, and Female Voice Quick Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // MISS AI Female Avatar
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 2.dp,
                                    brush = Brush.linearGradient(listOf(CyanBright, VioletGlow)),
                                    shape = CircleShape
                                )
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.miss_ai_avatar),
                                contentDescription = "MISS AI Avatar",
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "MISS AI",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanBright
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(VioletNeon.copy(alpha = 0.25f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isFemaleVoice) "महिला आवाज़" else "Standard",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = VioletGlow
                                    )
                                }
                            }
                            Text(
                                text = "स्मार्ट वॉइस असिस्टेंट • ଓଡ଼ିଆ & हिन्दी",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Female Voice Toggle Button
                    FilterChip(
                        selected = isFemaleVoice,
                        onClick = { onToggleFemaleVoice(!isFemaleVoice) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Woman,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isFemaleVoice) CyanBright else TextMuted
                            )
                        },
                        label = {
                            Text(
                                text = if (isFemaleVoice) "Female" else "Voice",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VioletNeon.copy(alpha = 0.25f),
                            selectedLabelColor = CyanBright,
                            containerColor = SurfaceCard,
                            labelColor = TextMuted
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isFemaleVoice,
                            borderColor = if (isFemaleVoice) VioletGlow else SurfaceBorder
                        ),
                        modifier = Modifier.testTag("female_voice_pill")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Language Selection Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SupportedLanguage.values().forEach { lang ->
                            val isSelected = lang == currentLanguage
                            FilterChip(
                                selected = isSelected,
                                onClick = { onLanguageSelected(lang) },
                                label = {
                                    Text(
                                        text = "${lang.nativeName} (${lang.englishName})",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanNeon.copy(alpha = 0.22f),
                                    selectedLabelColor = CyanBright,
                                    containerColor = SurfaceCard,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) CyanNeon else SurfaceBorder
                                ),
                                modifier = Modifier.testTag("lang_chip_${lang.code}")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Wake Word Status Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isWakeWordEnabled) CyanNeon.copy(alpha = 0.12f) else SurfaceCard)
                        .border(
                            1.dp,
                            if (isWakeWordEnabled) CyanNeon.copy(alpha = 0.35f) else SurfaceBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onToggleWakeWord(!isWakeWordEnabled) }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isWakeWordEnabled) AccentEmerald else TextMuted)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isWakeWordEnabled) {
                                "Wake Word 'Hey MISS' (हे मिस / ହେ ମିସ୍) Active"
                            } else {
                                "Wake Word 'Hey MISS' Inactive (Tap to turn on)"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isWakeWordEnabled) CyanBright else TextSecondary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    }

                    Text(
                        text = if (isWakeWordEnabled) "PAUSE" else "ACTIVATE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isWakeWordEnabled) AccentEmerald else TextMuted
                    )
                }
            }
        }

        // Live Audio Transcription Banner (when user is speaking)
        AnimatedVisibility(
            visible = isListening || partialTranscript.isNotBlank(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                color = SurfaceCard,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, CyanNeon.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Listening",
                        tint = CyanNeon,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (partialTranscript.isNotBlank()) partialTranscript else "Listening to your voice in ${currentLanguage.nativeName}...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onStopVoice,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop Listening",
                            tint = AccentAmber
                        )
                    }
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                UpgradedMessageBubble(
                    message = message,
                    isSpeaking = isSpeaking,
                    isFemaleVoice = isFemaleVoice,
                    onSpeak = { onSpeakMessage(message) },
                    onStopSpeaking = onStopSpeaking,
                    onExecuteAction = onExecuteAction
                )
            }
        }

        // Quick Suggestion Prompts
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(suggestionChips) { prompt ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier
                        .clickable { onSendMessage(prompt) }
                        .testTag("suggestion_chip")
                ) {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Center Aesthetic Mic Button with Waves
        AestheticMicButton(
            isListening = isListening,
            isGenerating = isGenerating,
            isSpeaking = isSpeaking,
            rmsDecibels = rmsDecibels,
            isFemaleVoice = isFemaleVoice,
            languageCode = currentLanguage.code,
            onClick = {
                if (isListening) onStopVoice() else onStartVoice()
            },
            modifier = Modifier.padding(vertical = 4.dp)
        )

        // Bottom Input Row (Text Field & Action Buttons)
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = {
                        Text(
                            text = currentLanguage.hintPrompt,
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_text_input"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard,
                        focusedBorderColor = CyanNeon,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            val msg = textInput
                            textInput = ""
                            onSendMessage(msg)
                        }
                    },
                    enabled = textInput.isNotBlank() && !isGenerating,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (textInput.isNotBlank()) CyanNeon else SurfaceCard)
                        .testTag("send_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send Message",
                        tint = if (textInput.isNotBlank()) Color(0xFF00363D) else TextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun UpgradedMessageBubble(
    message: ChatMessage,
    isSpeaking: Boolean,
    isFemaleVoice: Boolean,
    onSpeak: () -> Unit,
    onStopSpeaking: () -> Unit,
    onExecuteAction: (LauncherAction) -> Unit
) {
    val isUser = message.isUser

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            // MISS AI Avatar
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .border(
                        1.5.dp,
                        Brush.linearGradient(listOf(CyanBright, VioletGlow)),
                        CircleShape
                    )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.miss_ai_avatar),
                    contentDescription = "MISS AI Avatar",
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.width(300.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            if (!isUser) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 3.dp, start = 2.dp)
                ) {
                    Text(
                        text = "MISS AI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanBright
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isFemaleVoice) "• महिला आवाज़" else "• Voice",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser) {
                        VioletNeon.copy(alpha = 0.35f)
                    } else {
                        SurfaceCard
                    }
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isUser) VioletGlow.copy(alpha = 0.5f) else SurfaceBorder
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    if (message.isStreaming && message.text == "...") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = CyanNeon
                            )
                            Text(
                                text = "MISS AI is thinking...",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    } else {
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            lineHeight = 22.sp
                        )
                    }

                    // Suggested Launcher Action Button
                    if (message.suggestedAction != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { onExecuteAction(message.suggestedAction) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanNeon,
                                contentColor = Color(0xFF00363D)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("launcher_action_button")
                        ) {
                            Icon(
                                imageVector = when (message.suggestedAction.type) {
                                    com.example.data.models.LauncherActionType.CAMERA -> Icons.Default.PhotoCamera
                                    com.example.data.models.LauncherActionType.PHONE -> Icons.Default.Phone
                                    else -> Icons.Outlined.OpenInNew
                                },
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${message.suggestedAction.type.title} (${message.suggestedAction.type.hindiTitle} / ${message.suggestedAction.type.odiaTitle})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // TTS Voice playback button for assistant messages
                    if (!isUser && !message.isStreaming) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CyanNeon.copy(alpha = 0.12f))
                                    .clickable {
                                        if (isSpeaking) onStopSpeaking() else onSpeak()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                                    contentDescription = "Read aloud",
                                    tint = CyanBright,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isSpeaking) "रुकें (Stop)" else "सुनें (Listen)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CyanBright
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
