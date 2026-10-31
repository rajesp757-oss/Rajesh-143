package com.example.data.models

import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val language: SupportedLanguage = SupportedLanguage.HINDI,
    val suggestedAction: LauncherAction? = null,
    val isStreaming: Boolean = false
)
