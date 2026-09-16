package com.mianu.paymentapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mianu.paymentapp.ui.components.GlassPanel
import com.mianu.paymentapp.ui.components.MianuButton
import com.mianu.paymentapp.ui.components.MianuTextField
import com.mianu.paymentapp.ui.components.MessageBanner
import com.mianu.paymentapp.ui.theme.AmbientBackground
import com.mianu.paymentapp.ui.theme.EyebrowStyle
import com.mianu.paymentapp.ui.theme.MianuEnter
import com.mianu.paymentapp.ui.theme.MianuTheme
import com.mianu.paymentapp.ui.theme.MountAnimation
import com.mianu.paymentapp.ui.viewmodel.AuthViewModel
import com.mianu.paymentapp.ui.viewmodel.UiMessage

@Composable
fun LoginScreen(
    onAuthenticated: () -> Unit,
    viewModel: AuthViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = MianuTheme.colors

    AmbientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            MountAnimation(enter = MianuEnter.ScaleIn) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceRaised)
                            .border(1.5.dp, Color(0xFF333A48), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "M",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                    Spacer(Modifier.height(18.dp))
                    Text(
                        text = "MIANU-SM IV",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = Color.White,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "MIANU-SM IV • 2, 3 & 4 OCTOBRE 2026",
                        style = EyebrowStyle,
                        color = Color(0xFF9CA3AF),
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            MountAnimation(enter = MianuEnter.FadeInUp, delayMillis = 120) {
                GlassPanel(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "ACCÈS CONFÉRENCE • SIGN IN",
                        style = EyebrowStyle,
                        color = colors.fg,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                    )
                    Spacer(Modifier.height(16.dp))

                    MianuTextField(
                        value = state.username,
                        onValueChange = viewModel::onUsernameChange,
                        label = "Username",
                        leadingIcon = Icons.Default.Person,
                        enabled = !state.isSubmitting,
                    )

                    Spacer(Modifier.height(14.dp))
                    MianuTextField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        label = "Password",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        keyboardType = KeyboardType.Password,
                        enabled = !state.isSubmitting,
                    )

                    if (state.error != null) {
                        Spacer(Modifier.height(14.dp))
                        MessageBanner(
                            message = UiMessage(state.error!!, UiMessage.Tone.Error),
                        )
                    }

                    Spacer(Modifier.height(22.dp))

                    MianuButton(
                        text = "Sign in",
                        onClick = { viewModel.submit(onAuthenticated) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.canSubmit,
                        loading = state.isSubmitting,
                        glow = true,
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = "Official MIANU-SM IV conference operations & delegate access terminal. Accounts are provisioned exclusively by conference administration.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.fgMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}
