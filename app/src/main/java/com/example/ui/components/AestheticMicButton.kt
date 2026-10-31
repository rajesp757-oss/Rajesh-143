package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletDeep
import com.example.ui.theme.VioletGlow
import com.example.ui.theme.VioletNeon
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AestheticMicButton(
    isListening: Boolean,
    isGenerating: Boolean,
    isSpeaking: Boolean,
    rmsDecibels: Float,
    isFemaleVoice: Boolean,
    languageCode: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "micButtonTransitions")

    // Idle breathing pulse
    val idlePulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idleBreath"
    )

    // Wave ripple animation for active listening
    val rippleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleProgress"
    )

    // Rotation angle for thinking state
    val thinkingRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "thinkingRotation"
    )

    // Dynamic scale driven by audio volume
    val audioScale = remember { Animatable(1f) }
    LaunchedEffect(rmsDecibels, isListening) {
        if (isListening) {
            val target = 1f + (rmsDecibels / 10f) * 0.35f
            audioScale.animateTo(target, tween(90))
        } else {
            audioScale.animateTo(1f, tween(180))
        }
    }

    val currentButtonScale = when {
        isListening -> audioScale.value
        isGenerating -> idlePulse * 1.05f
        isSpeaking -> idlePulse * 1.08f
        else -> idlePulse
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Equalizer soundwave bars (visible when listening or speaking)
        if (isListening || isSpeaking) {
            Row(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .height(28.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val barCount = 7
                for (i in 0 until barCount) {
                    val multiplier = listOf(0.4f, 0.7f, 1.0f, 0.85f, 0.95f, 0.6f, 0.35f)[i]
                    val baseHeight = if (isListening) {
                        (8 + (rmsDecibels * 2.2f * multiplier)).coerceIn(6f, 26f)
                    } else {
                        (10 + (sin(rippleProgress * 6.28 + i) * 6)).toFloat().coerceIn(6f, 24f)
                    }
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(baseHeight.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(CyanBright, VioletGlow)
                                )
                            )
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Concentric Ripple Box & Central Mic Button
        Box(
            modifier = Modifier
                .size(130.dp)
                .testTag("aesthetic_mic_container"),
            contentAlignment = Alignment.Center
        ) {
            // Animated Canvas for Ripple Rings and Glow
            Canvas(modifier = Modifier.size(130.dp)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val baseRadius = 46.dp.toPx()

                // Expanding audio ripples when listening
                if (isListening) {
                    // Outer Ripple 1
                    val r1 = baseRadius + (rippleProgress * 22.dp.toPx())
                    val alpha1 = (1f - rippleProgress) * 0.6f
                    drawCircle(
                        color = CyanNeon.copy(alpha = alpha1),
                        center = center,
                        radius = r1,
                        style = Stroke(width = 2.5.dp.toPx())
                    )

                    // Outer Ripple 2
                    val prog2 = (rippleProgress + 0.5f) % 1f
                    val r2 = baseRadius + (prog2 * 22.dp.toPx())
                    val alpha2 = (1f - prog2) * 0.5f
                    drawCircle(
                        color = VioletGlow.copy(alpha = alpha2),
                        center = center,
                        radius = r2,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                // Rotating glowing particles for thinking state
                if (isGenerating) {
                    for (i in 0 until 4) {
                        val angle = Math.toRadians((thinkingRotation + i * 90f).toDouble())
                        val orbitDist = baseRadius * 1.25f
                        val nodeX = center.x + (orbitDist * cos(angle)).toFloat()
                        val nodeY = center.y + (orbitDist * sin(angle)).toFloat()
                        drawCircle(
                            color = CyanBright,
                            center = Offset(nodeX, nodeY),
                            radius = 3.5.dp.toPx()
                        )
                    }
                }
            }

            // Central Gradient Glowing Mic Button
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(currentButtonScale)
                    .shadow(
                        elevation = if (isListening) 16.dp else 8.dp,
                        shape = CircleShape,
                        spotColor = if (isListening) CyanBright else VioletGlow,
                        ambientColor = CyanNeon
                    )
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = when {
                                isListening -> listOf(CyanBright, CyanNeon, VioletDeep)
                                isGenerating -> listOf(VioletGlow, VioletNeon, Color(0xFF1E0B36))
                                isSpeaking -> listOf(CyanNeon, VioletGlow, VioletDeep)
                                else -> listOf(Color(0xFF00E5FF), Color(0xFF7C4DFF), Color(0xFF10162A))
                            }
                        )
                    )
                    .border(
                        width = 2.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(Color.White.copy(alpha = 0.8f), CyanNeon, VioletGlow)
                        ),
                        shape = CircleShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
                    .testTag("aesthetic_mic_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        isSpeaking -> Icons.Default.Stop
                        isListening -> Icons.Default.Mic
                        isGenerating -> Icons.Default.GraphicEq
                        else -> Icons.Default.Mic
                    },
                    contentDescription = "Microphone Button",
                    tint = if (isListening) Color(0xFF003038) else Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // State & Prompt Guidance Label
        Text(
            text = when {
                isListening -> when (languageCode) {
                    "or" -> "ମୁଁ ଶୁଣୁଛି... କୁହନ୍ତୁ (Listening)"
                    "en" -> "Listening to you... Speak now"
                    else -> "मैं सुन रही हूँ... बोलिए (Listening)"
                }
                isGenerating -> "MISS AI सोच रही है... (Thinking)"
                isSpeaking -> when (languageCode) {
                    "or" -> "ମିସ୍ ଉତ୍ତର ଦେଉଛନ୍ତି... (Speaking)"
                    "en" -> "MISS is speaking..."
                    else -> "मिस एआई बोल रही हैं... (Speaking)"
                }
                else -> when (languageCode) {
                    "or" -> "ମାଇକ୍ ଛୁଅନ୍ତୁ ବା 'Hey MISS' କୁହନ୍ତୁ"
                    "en" -> "Tap Mic or say 'Hey MISS'"
                    else -> "माइक छुएं या 'Hey MISS' बोलें"
                }
            },
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = when {
                isListening -> CyanBright
                isGenerating -> AccentAmber
                isSpeaking -> VioletGlow
                else -> TextSecondary
            }
        )
    }
}
