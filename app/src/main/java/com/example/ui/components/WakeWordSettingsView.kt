package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Woman
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun WakeWordSettingsView(
    isWakeWordEnabled: Boolean,
    autoSpeakEnabled: Boolean,
    isFemaleVoice: Boolean,
    currentLanguage: SupportedLanguage,
    hasAudioPermission: Boolean,
    onRequestPermission: () -> Unit,
    onToggleWakeWord: (Boolean) -> Unit,
    onToggleAutoSpeak: (Boolean) -> Unit,
    onToggleFemaleVoice: (Boolean) -> Unit,
    onTestVoice: () -> Unit,
    onClearChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Permissions & Privacy
        SettingsHeader(
            title = "SAFE PERMISSIONS & PRIVACY",
            subtitle = "सुरक्षित अनुमतियाँ / ସୁରକ୍ଷିତ ଅନୁମତି",
            icon = Icons.Default.Security
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CyanNeon.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = CyanBright,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Microphone (Audio Record)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (hasAudioPermission) "Permission Granted" else "Action Required",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (hasAudioPermission) AccentEmerald else AccentAmber
                            )
                        }
                    }

                    if (hasAudioPermission) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Granted",
                            tint = AccentEmerald,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Button(
                            onClick = onRequestPermission,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanNeon,
                                contentColor = Color(0xFF00363D)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("grant_permission_button")
                        ) {
                            Text("Grant", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "MISS AI only uses your microphone on-demand to convert voice to text in Hindi & Odia and detect the wake word. Zero audio recordings are stored or sent to ad trackers.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PrivacyTip,
                        contentDescription = null,
                        tint = AccentEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "100% Google Play Safe: No SMS or Call Log permissions used.",
                        fontSize = 11.sp,
                        color = AccentEmerald,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Section: Wake Word "Hey MISS"
        SettingsHeader(
            title = "WAKE WORD (वेक वर्ड / ୱେକ୍ ୱାର୍ଡ୍)",
            subtitle = "Voice activation via 'Hey MISS'",
            icon = Icons.Default.Hearing
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Wake Word: 'Hey MISS'",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Listen for wake phrases while app is open",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Switch(
                        checked = isWakeWordEnabled,
                        onCheckedChange = { onToggleWakeWord(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00363D),
                            checkedTrackColor = CyanNeon,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = SurfaceBorder
                        ),
                        modifier = Modifier.testTag("wake_word_switch")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Supported Wake Phrases:",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanBright,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0D1424))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("• English: \"Hey MISS\", \"Hey Miss AI\", \"Miss AI\"", fontSize = 12.sp, color = TextPrimary)
                    Text("• हिन्दी: \"हे मिस\", \"अरे मिस\", \"सुनो मिस\", \"मिस एआई\"", fontSize = 12.sp, color = TextPrimary)
                    Text("• ଓଡ଼ିଆ: \"ହେ ମିସ୍\", \"ନମସ୍କାର ମିସ୍\", \"ଶୁଣ ମିସ୍\", \"ମିସ୍ ଏଆଇ\"", fontSize = 12.sp, color = TextPrimary)
                }
            }
        }

        // Section: Voice & Speech (TTS)
        SettingsHeader(
            title = "VOICE & SPEECH (आवाज़ / କଣ୍ଠସ୍ୱର)",
            subtitle = "Text-to-Speech audio readout options",
            icon = Icons.Default.RecordVoiceOver
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Female Voice Toggle Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(VioletNeon.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Woman,
                                contentDescription = null,
                                tint = VioletGlow,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Female Voice (महिला आवाज़ / ମହିଳା କଣ୍ଠସ୍ୱର)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (isFemaleVoice) "Sweet & natural feminine assistant tone active" else "Standard speech pitch",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isFemaleVoice) CyanBright else TextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = isFemaleVoice,
                        onCheckedChange = onToggleFemaleVoice,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00363D),
                            checkedTrackColor = CyanNeon,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = SurfaceBorder
                        ),
                        modifier = Modifier.testTag("female_voice_switch")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Auto-Read Aloud Toggle Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-Read Responses Aloud",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Automatically speak AI answers using speech engine",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Switch(
                        checked = autoSpeakEnabled,
                        onCheckedChange = onToggleAutoSpeak,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00363D),
                            checkedTrackColor = CyanNeon,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = SurfaceBorder
                        ),
                        modifier = Modifier.testTag("auto_speak_switch")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = onTestVoice,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("test_voice_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = CyanBright
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanNeon.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Test MISS AI Voice (${currentLanguage.nativeName})",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Section: Maintenance
        SettingsHeader(
            title = "SESSION (सत्र / ସେସନ୍)",
            subtitle = "Reset or clear conversation",
            icon = Icons.Default.Delete
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Clear Current Conversation",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Resets conversation memory and starts a fresh session.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onClearChat,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AccentAmber
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentAmber.copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clear Chat History", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SettingsHeader(
    title: String,
    subtitle: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CyanNeon,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = CyanBright,
                letterSpacing = 0.8.sp
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                fontSize = 11.sp
            )
        }
    }
}
