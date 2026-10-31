package com.example.data.models

enum class SupportedLanguage(
    val code: String,
    val nativeName: String,
    val englishName: String,
    val greeting: String,
    val speechLocaleTag: String,
    val hintPrompt: String
) {
    HINDI(
        code = "hi",
        nativeName = "हिन्दी",
        englishName = "Hindi",
        greeting = "नमस्ते! मैं मिस (MISS) हूँ। आप मुझसे हिन्दी में बात कर सकते हैं।",
        speechLocaleTag = "hi-IN",
        hintPrompt = "मिस से कुछ भी पूछें... या 'Hey MISS' बोलें"
    ),
    ODIA(
        code = "or",
        nativeName = "ଓଡ଼ିଆ",
        englishName = "Odia",
        greeting = "ନମସ୍କାର! ମୁଁ ମିସ୍ (MISS)। ଆପଣ ମୋତେ ଓଡ଼ିଆରେ କଥା ହୋଇପାରିବେ।",
        speechLocaleTag = "or-IN",
        hintPrompt = "ମିସ୍‌ଙ୍କୁ କିଛି ପଚାରନ୍ତୁ... ବା 'Hey MISS' କୁହନ୍ତୁ"
    ),
    ENGLISH(
        code = "en",
        nativeName = "English",
        englishName = "English",
        greeting = "Hello! I am MISS, your smart phone assistant. How can I help you?",
        speechLocaleTag = "en-IN",
        hintPrompt = "Ask MISS anything... or say 'Hey MISS'"
    )
}
