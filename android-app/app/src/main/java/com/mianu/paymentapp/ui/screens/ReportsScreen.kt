package com.mianu.paymentapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mianu.paymentapp.ui.components.GlassCard
import com.mianu.paymentapp.ui.components.IconBadge
import com.mianu.paymentapp.ui.components.LoadingList
import com.mianu.paymentapp.ui.components.MessageBanner
import com.mianu.paymentapp.ui.components.MianuButton
import com.mianu.paymentapp.ui.components.MianuCard
import com.mianu.paymentapp.ui.components.SectionHeader
import com.mianu.paymentapp.ui.components.StatCard
import com.mianu.paymentapp.ui.components.StatRow
import com.mianu.paymentapp.ui.theme.AmbientBackground
import com.mianu.paymentapp.ui.theme.MianuTheme
import com.mianu.paymentapp.ui.viewmodel.OpsViewModel

/** Analytics summaries, report record counts, and export. */
@Composable
fun ReportsScreen(viewModel: OpsViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = MianuTheme.colors

    AmbientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SectionHeader(
                title = "Reports",
                eyebrow = "Analytics",
                subtitle = "Totals across the conference",
            )

            state.message?.let { message ->
                MessageBanner(message = message, onDismiss = viewModel::consumeMessage)
            }

            if (state.isLoading) {
                LoadingList(rows = 3, rowHeight = 104.dp)
            } else {
                StatRow {
                    StatCard(
                        title = state.daily?.metricLabel ?: "Today",
                        value = state.daily?.total?.toString() ?: "—",
                        caption = "Daily total",
                        icon = Icons.Default.InsertChart,
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        title = state.trends?.metricLabel ?: "Trend",
                        value = state.trends?.total?.toString() ?: "—",
                        caption = "Running total",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        modifier = Modifier.weight(1f),
                    )
                }

                SectionHeader(title = "Record counts", eyebrow = "Reports")

                MianuCard(modifier = Modifier.fillMaxWidth()) {
                    ReportLine(
                        label = "Meals",
                        count = state.mealsReport?.totalRecords,
                        generatedAt = state.mealsReport?.generatedAt,
                        icon = Icons.Default.Restaurant,
                    )
                    ReportLine(
                        label = "Transactions",
                        count = state.transactionsReport?.totalRecords,
                        generatedAt = state.transactionsReport?.generatedAt,
                        icon = Icons.Default.SwapHoriz,
                    )
                    ReportLine(
                        label = "Delegates",
                        count = state.usersReport?.totalRecords,
                        generatedAt = state.usersReport?.generatedAt,
                        icon = Icons.Default.People,
                    )
                }

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    SectionHeader(title = "Export", eyebrow = "Data")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Generate a full export of meals, transactions and delegates.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.fgMuted,
                    )
                    Spacer(Modifier.height(14.dp))
                    MianuButton(
                        text = "Generate export",
                        onClick = viewModel::export,
                        modifier = Modifier.fillMaxWidth(),
                        loading = state.isExporting,
                        icon = Icons.Default.Download,
                    )
                    state.exportPath?.let { path ->
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = path,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.success,
                        )
                    }
                }

                MianuButton(
                    text = "Refresh",
                    onClick = viewModel::refresh,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = colors.surfaceRaised,
                )
            }
        }
    }
}

@Composable
private fun ReportLine(
    label: String,
    count: Int?,
    generatedAt: String?,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    val colors = MianuTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        IconBadge(icon = icon, size = 34.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = colors.fg,
            )
            if (generatedAt != null) {
                Text(
                    text = "Generated ${generatedAt.take(19).replace('T', ' ')}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.fgMuted,
                )
            }
        }
        Text(
            // An unavailable count shows an em dash rather than 0 — they mean different things.
            text = count?.toString() ?: "—",
            style = MaterialTheme.typography.titleMedium,
            color = if (count != null) colors.fg else colors.fgMuted,
        )
    }
}
