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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.VioletDeep
import com.example.ui.theme.VioletGlow
import com.example.ui.theme.VioletNeon
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GlowingAiOrb(
    isListening: Boolean,
    isGenerating: Boolean,
    isSpeaking: Boolean,
    rmsDecibels: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orbTransition")

    // Idle breathing pulse
    val idlePulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idlePulse"
    )

    // Rotation angle for thinking state
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationAngle"
    )

    // Dynamic scale driven by audio volume
    val audioScale = remember { Animatable(1f) }
    LaunchedEffect(rmsDecibels, isListening) {
        if (isListening) {
            val target = 1f + (rmsDecibels / 10f) * 0.45f
            audioScale.animateTo(target, tween(80))
        } else {
            audioScale.animateTo(1f, tween(200))
        }
    }

    val currentScale = when {
        isListening -> audioScale.value
        isGenerating -> idlePulse * 1.08f
        isSpeaking -> idlePulse * 1.12f
        else -> idlePulse
    }

    Box(
        modifier = modifier
            .size(140.dp)
            .testTag("ai_orb_button")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(130.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = (size.minDimension / 2.6f) * currentScale

            // Outer reactive glow ripples
            if (isListening || isSpeaking) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(CyanNeon.copy(alpha = 0.35f), Color.Transparent),
                        center = center,
                        radius = baseRadius * 1.6f
                    ),
                    center = center,
                    radius = baseRadius * 1.6f
                )

                // Expanding soundwave ring
                drawCircle(
                    color = CyanBright.copy(alpha = 0.5f),
                    center = center,
                    radius = baseRadius * 1.35f,
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            // Core radial gradient
            val primaryColor = when {
                isListening -> CyanBright
                isGenerating -> VioletGlow
                isSpeaking -> CyanNeon
                else -> CyanNeon
            }

            val secondaryColor = when {
                isListening -> VioletNeon
                isGenerating -> CyanBright
                isSpeaking -> VioletGlow
                else -> VioletDeep
            }

            val coreBrush = Brush.radialGradient(
                colors = listOf(
                    Color.White,
                    primaryColor,
                    secondaryColor,
                    Color(0xFF0D1B2A)
                ),
                center = center,
                radius = baseRadius
            )

            // Inner core orb
            drawCircle(
                brush = coreBrush,
                center = center,
                radius = baseRadius
            )

            // Dynamic orbit nodes when thinking or active
            if (isGenerating || isListening) {
                val orbitCount = if (isGenerating) 4 else 3
                for (i in 0 until orbitCount) {
                    val angleRad = Math.toRadians((rotationAngle + i * (360f / orbitCount)).toDouble())
                    val orbitDist = baseRadius * 1.15f
                    val nodeX = center.x + (orbitDist * cos(angleRad)).toFloat()
                    val nodeY = center.y + (orbitDist * sin(angleRad)).toFloat()

                    drawCircle(
                        color = Color.White,
                        center = Offset(nodeX, nodeY),
                        radius = 4.dp.toPx()
                    )
                }
            }
        }
    }
}
