package com.mianu.paymentapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.mianu.paymentapp.ui.theme.MianuShapes
import com.mianu.paymentapp.ui.theme.MianuTheme
import com.mianu.paymentapp.ui.theme.accentGlow

/** Text field wired to the semantic tokens, with inline validation messaging. */
@Composable
fun MianuTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    errorText: String? = null,
    supportingText: String? = null,
) {
    val colors = MianuTheme.colors
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            label = { Text(label) },
            placeholder = placeholder?.let { { Text(it, color = colors.fgMuted) } },
            leadingIcon = leadingIcon?.let {
                { Icon(it, contentDescription = null, tint = colors.fgMuted) }
            },
            trailingIcon = trailingContent,
            singleLine = singleLine,
            isError = errorText != null,
            shape = MianuShapes.Medium,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF14171E),
                unfocusedContainerColor = Color(0xFF0F1217),
                disabledContainerColor = colors.surfaceSunken,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = colors.border,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = colors.fgMuted,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color.White,
                errorBorderColor = colors.danger,
                errorLabelColor = colors.danger,
            ),
        )
        val helper = errorText ?: supportingText
        if (helper != null) {
            Text(
                text = helper,
                style = MaterialTheme.typography.bodySmall,
                color = if (errorText != null) colors.danger else colors.fgMuted,
                modifier = Modifier.padding(start = 14.dp, top = 4.dp),
            )
        }
    }
}

/** Primary action. High-contrast white button with pure black text matching the reference image. */
@Composable
fun MianuButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
    containerColor: Color = Color.White,
    contentColor: Color = Color.Black,
    glow: Boolean = false,
) {
    val haptic = LocalHapticFeedback.current
    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        enabled = enabled && !loading,
        modifier = modifier
            .height(52.dp)
            .then(if (glow && enabled && !loading) Modifier.accentGlow(MianuShapes.Medium, containerColor) else Modifier),
        shape = MianuShapes.Medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = Color(0xFF181B24),
            disabledContentColor = Color(0xFF6B7280),
        ),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = contentColor,
            )
        } else {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = contentColor)
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
            )
        }
    }
}

/** Secondary action. Dark carbon surface with white text and crisp border. */
@Composable
fun MianuSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    tint: Color = Color.White,
) {
    val haptic = LocalHapticFeedback.current
    OutlinedButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        enabled = enabled,
        modifier = modifier.height(48.dp),
        shape = MianuShapes.Medium,
        border = androidx.compose.foundation.BorderStroke(1.dp, MianuTheme.colors.border),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color(0xFF14171E),
            contentColor = tint,
            disabledContainerColor = Color(0xFF0F1217),
            disabledContentColor = Color(0xFF6B7280),
        ),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(17.dp), tint = tint)
            Spacer(Modifier.width(7.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, color = tint)
    }
}

/**
 * Segmented selector for small, mutually exclusive option sets.
 * Selected item is crisp white with black text.
 */
@Composable
fun <T> SegmentedSelector(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: (T) -> String,
) {
    val haptic = LocalHapticFeedback.current
    val colors = MianuTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MianuShapes.Medium)
            .background(colors.surfaceSunken, MianuShapes.Medium)
            .border(1.dp, colors.border, MianuShapes.Medium)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(MianuShapes.Small)
                    .background(
                        if (isSelected) Color.White else Color.Transparent,
                        MianuShapes.Small,
                    )
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSelect(option)
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label(option),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) Color.Black else colors.fgMuted,
                )
            }
        }
    }
}

/** Selectable chip for filters. Turns white with black text when selected. */
@Composable
fun FilterPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val colors = MianuTheme.colors
    Box(
        modifier = modifier
            .clip(MianuShapes.Pill)
            .background(
                if (selected) Color.White else Color(0xFF14171E),
                MianuShapes.Pill,
            )
            .border(
                1.dp,
                if (selected) Color.White else colors.border,
                MianuShapes.Pill,
            )
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color.Black else colors.fgMuted,
        )
    }
}
