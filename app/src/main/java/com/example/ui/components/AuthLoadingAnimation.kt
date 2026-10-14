package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.R
import com.example.ui.theme.SapphireNavy
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate900
import com.example.ui.theme.SocietyTealPrimary

/**
 * Reusable Lottie-powered authentication loading animation.
 * Renders the custom Lottie security & authentication animation with seamless infinite looping
 * and a robust fallback if the composition is still loading.
 */
@Composable
fun AuthLottieLoading(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    isPlaying: Boolean = true,
    fallbackTint: Color = SocietyTealPrimary
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_auth_loading))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = isPlaying,
        iterations = LottieConstants.IterateForever
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (composition != null) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier
                    .size(size)
                    .testTag("lottie_auth_loading_anim")
            )
        } else {
            // Elegant fallback animation when Lottie composition is loading or in unit testing
            val rotationAnim = remember { Animatable(0f) }
            val scaleAnim = remember { Animatable(0.9f) }

            LaunchedEffect(isPlaying) {
                if (isPlaying) {
                    rotationAnim.animateTo(
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        )
                    )
                }
            }

            LaunchedEffect(isPlaying) {
                if (isPlaying) {
                    scaleAnim.animateTo(
                        targetValue = 1.1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(size)
                    .scale(scaleAnim.value)
                    .rotate(rotationAnim.value)
                    .background(fallbackTint.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(size * 0.85f),
                    color = fallbackTint,
                    strokeWidth = 2.5.dp
                )
            }
        }
    }
}

/**
 * Compact Lottie loader for action buttons during authentication requests.
 */
@Composable
fun AuthButtonLottieLoader(
    modifier: Modifier = Modifier,
    size: Dp = 26.dp,
    tint: Color = Color.White
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        AuthLottieLoading(
            modifier = Modifier.size(size),
            size = size,
            fallbackTint = tint
        )
    }
}

/**
 * High-visibility visual feedback card with Lottie loading animation,
 * displayed during active authentication requests on Login and Registration screens.
 */
@Composable
fun AuthLoadingFeedbackBanner(
    visible: Boolean,
    isRegister: Boolean = false,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
                .testTag("auth_loading_feedback_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = SocietyTealPrimary.copy(alpha = 0.08f)
            ),
            border = BorderStroke(1.dp, SocietyTealPrimary.copy(alpha = 0.25f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lottie Loading Animation
                AuthLottieLoading(
                    size = 54.dp,
                    fallbackTint = SocietyTealPrimary
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isRegister) "Creating Your Account..." else "Authenticating...",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SapphireNavy,
                            fontSize = 15.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (isRegister) {
                            "Setting up your profile & security credentials with Firebase..."
                        } else {
                            "Verifying encrypted credentials with Firebase Auth..."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Slate600,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    )
                }
            }
        }
    }
}
