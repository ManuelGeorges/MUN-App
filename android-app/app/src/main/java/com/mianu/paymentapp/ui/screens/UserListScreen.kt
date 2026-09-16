package com.mianu.paymentapp.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mianu.paymentapp.data.models.UserResponse
import com.mianu.paymentapp.data.models.UserRole
import com.mianu.paymentapp.ui.components.Avatar
import com.mianu.paymentapp.ui.components.MianuCard
import com.mianu.paymentapp.ui.components.StatusPill
import com.mianu.paymentapp.ui.theme.MianuTheme

/**
 * A single delegate row.
 *
 * Clean, friendly card layout with prominent meal adjustment pill,
 * quick NFC card link and edit actions, and an overflow menu for management.
 */
@Composable
fun UserRow(
    user: UserResponse,
    modifier: Modifier = Modifier,
    teamName: String? = null,
    onClick: () -> Unit = {},
    onLinkCard: () -> Unit = {},
    onAdjustMeals: () -> Unit = {},
    onEdit: () -> Unit = {},
    onToggleActive: () -> Unit = {},
    onDelete: () -> Unit = {},
) {
    val colors = MianuTheme.colors
    var menuExpanded by remember { mutableStateOf(false) }

    MianuCard(modifier = modifier.fillMaxWidth(), onClick = null) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Top Header: Avatar, Name & Team/Contact, 3-dots Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onClick),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Avatar(
                        initials = user.initials,
                        color = if (user.isActive) roleColor(user.roleEnum) else colors.fgMuted,
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = user.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (user.isActive) colors.fg else colors.fgMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false),
                            )
                            if (!user.isActive) {
                                StatusPill(text = "SUSPENDED", color = colors.danger)
                            }
                        }

                        val subtitle = listOfNotNull(
                            user.email ?: user.phone,
                            teamName?.let { "Team: $it" } ?: "No Team",
                        ).joinToString("  •  ")

                        if (subtitle.isNotBlank()) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.fgMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }

                // 3-dots overflow menu
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options for ${user.name}",
                            tint = colors.fgMuted,
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (user.isActive) "Suspend Delegate" else "Reactivate Delegate",
                                    color = if (user.isActive) colors.warning else colors.success,
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (user.isActive) Icons.Default.PersonOff else Icons.Default.PersonOutline,
                                    contentDescription = null,
                                    tint = if (user.isActive) colors.warning else colors.success,
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onToggleActive()
                            },
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Delete Delegate",
                                    color = colors.danger,
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = colors.danger,
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            },
                        )
                    }
                }
            }

            HorizontalDivider(
                color = colors.border.copy(alpha = 0.5f),
                thickness = 0.5.dp,
            )

            // Bottom Row: Clickable Meal Allowance Pill, Role Pill, and Direct Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Clickable Meal Allowance Pill
                val mealsColor = when {
                    user.mealsBalance <= 0 -> colors.danger
                    user.mealsBalance <= 2 -> colors.warning
                    else -> androidx.compose.ui.graphics.Color(0xFF34D399)
                }

                Surface(
                    onClick = onAdjustMeals,
                    shape = RoundedCornerShape(8.dp),
                    color = mealsColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, mealsColor.copy(alpha = 0.4f)),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = mealsColor,
                            modifier = Modifier.size(15.dp),
                        )
                        Text(
                            text = "${user.mealsBalance}/${user.mealAllowance} MEALS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = mealsColor,
                        )
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Adjust Meals",
                            tint = mealsColor.copy(alpha = 0.8f),
                            modifier = Modifier.size(13.dp),
                        )
                    }
                }

                StatusPill(
                    text = user.roleEnum.displayName.uppercase(),
                    color = roleColor(user.roleEnum),
                )

                if (user.teamRole != null) {
                    StatusPill(
                        text = user.teamRole.name.uppercase(),
                        color = colors.info,
                        icon = Icons.Default.Group,
                    )
                }

                Spacer(Modifier.weight(1f))

                IconButton(
                    onClick = onLinkCard,
                    modifier = Modifier.size(34.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = "Link card to ${user.name}",
                        tint = colors.accent,
                        modifier = Modifier.size(20.dp),
                    )
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(34.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit ${user.name}",
                        tint = colors.fgMuted,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

/** Role accent, kept consistent everywhere a role is shown. */
@Composable
fun roleColor(role: UserRole): androidx.compose.ui.graphics.Color = when (role) {
    UserRole.ADMIN -> androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    UserRole.CHIEF_ORGANIZER -> androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    UserRole.ORGANIZER -> androidx.compose.ui.graphics.Color(0xFF5B96F7)
    UserRole.TEAM_LEADER -> androidx.compose.ui.graphics.Color(0xFF5B96F7)
    UserRole.TEAM_MEMBER -> androidx.compose.ui.graphics.Color(0xFF9CA3AF)
    UserRole.USER -> androidx.compose.ui.graphics.Color(0xFFE5E7EB)
}
