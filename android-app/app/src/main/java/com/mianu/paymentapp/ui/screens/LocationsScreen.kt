package com.mianu.paymentapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mianu.paymentapp.data.MianuRepository
import com.mianu.paymentapp.data.models.AttendanceSummary
import com.mianu.paymentapp.data.models.DelegateLocation
import com.mianu.paymentapp.data.onFailure
import com.mianu.paymentapp.data.onSuccess
import com.mianu.paymentapp.nfc.OnNfcScan
import com.mianu.paymentapp.ui.components.Avatar
import com.mianu.paymentapp.ui.components.EmptyState
import com.mianu.paymentapp.ui.components.FilterPill
import com.mianu.paymentapp.ui.components.GlassPanel
import com.mianu.paymentapp.ui.components.MianuButton
import com.mianu.paymentapp.ui.components.MianuCard
import com.mianu.paymentapp.ui.components.MianuSecondaryButton
import com.mianu.paymentapp.ui.components.MianuTextField
import com.mianu.paymentapp.ui.components.SectionHeader
import com.mianu.paymentapp.ui.components.SegmentedSelector
import com.mianu.paymentapp.ui.components.StatCard
import com.mianu.paymentapp.ui.components.StatRow
import com.mianu.paymentapp.ui.components.StatusPill
import com.mianu.paymentapp.ui.theme.AmbientBackground
import com.mianu.paymentapp.ui.theme.MianuEnter
import com.mianu.paymentapp.ui.theme.MianuTheme
import com.mianu.paymentapp.ui.theme.MountAnimation
import kotlinx.coroutines.launch

enum class LocationFilter(val label: String) {
    ALL("All"),
    PRESENT("Present"),
    ABSENT("Absent"),
    INSIDE("Inside Halls"),
    EXITED("Exited"),
}

enum class TerminalSwipeAction(val actionKey: String, val label: String) {
    CHECK_IN("check_in", "Check In"),
    CHECK_OUT("check_out", "Check Out"),
    TOGGLE("toggle", "Toggle"),
}

data class SwipeFeedback(
    val title: String,
    val detail: String,
    val isSuccess: Boolean,
)

@Composable
fun LocationsScreen(
    repository: MianuRepository = MianuRepository.instance,
) {
    val colors = MianuTheme.colors
    val scope = rememberCoroutineScope()

    var locations by remember { mutableStateOf<List<DelegateLocation>>(emptyList()) }
    var attendanceSummary by remember { mutableStateOf<AttendanceSummary?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(LocationFilter.ALL) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Swipe Terminal state
    var cardUidInput by remember { mutableStateOf("") }
    var swipeAction by remember { mutableStateOf(TerminalSwipeAction.CHECK_IN) }
    var swipeFeedback by remember { mutableStateOf<SwipeFeedback?>(null) }

    // Bulk selection state
    var selectedUserIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    fun loadData() {
        scope.launch {
            isLoading = true
            errorMessage = null
            repository.getLocations()
                .onSuccess { list ->
                    locations = list
                    isLoading = false
                }
                .onFailure { err ->
                    errorMessage = err.message
                    isLoading = false
                }

            repository.getAttendance()
                .onSuccess { res ->
                    attendanceSummary = res.summary
                }
        }
    }

    fun recordSwipe(cardUid: String) {
        if (cardUid.isBlank() || isSubmitting) return
        scope.launch {
            isSubmitting = true
            repository.recordAttendanceSwipe(cardUid.trim(), swipeAction.actionKey)
                .onSuccess { res ->
                    val user = res.user
                    val isPresent = user.status == "present"
                    val actionVerb = if (user.action == "check_in") "CHECKED IN (PRESENT)" else "CHECKED OUT (ABSENT)"
                    swipeFeedback = SwipeFeedback(
                        title = "${user.name} • $actionVerb",
                        detail = "${user.role} (${user.teamName ?: "Individual"}) • Card: ${user.cardUid}",
                        isSuccess = isPresent,
                    )
                    cardUidInput = ""
                    attendanceSummary = res.summary
                    isSubmitting = false
                    loadData()
                }
                .onFailure { err ->
                    swipeFeedback = SwipeFeedback(
                        title = "Attendance Swipe Failed",
                        detail = err.message,
                        isSuccess = false,
                    )
                    isSubmitting = false
                }
        }
    }

    fun toggleSingleAttendance(userId: String, currentStatus: String) {
        val targetStatus = if (currentStatus == "present") "absent" else "present"
        scope.launch {
            isSubmitting = true
            repository.updateSingleAttendance(userId, targetStatus)
                .onSuccess {
                    isSubmitting = false
                    loadData()
                }
                .onFailure { err ->
                    errorMessage = err.message
                    isSubmitting = false
                }
        }
    }

    fun handleBulkAttendance(status: String, all: Boolean) {
        scope.launch {
            isSubmitting = true
            val ids = if (all) null else selectedUserIds.toList()
            repository.updateBulkAttendance(userIds = ids, all = if (all) true else null, status = status)
                .onSuccess { res ->
                    if (!all) selectedUserIds = emptySet()
                    swipeFeedback = SwipeFeedback(
                        title = "Bulk Attendance Updated",
                        detail = "Marked ${res.count} attendees as ${status.uppercase()}",
                        isSuccess = status == "present",
                    )
                    isSubmitting = false
                    loadData()
                }
                .onFailure { err ->
                    errorMessage = err.message
                    isSubmitting = false
                }
        }
    }

    // Active NFC reader for this screen: taps record attendance instantly
    OnNfcScan(enabled = !isSubmitting) { uid ->
        recordSwipe(uid)
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    val insideCount = remember(locations) { locations.count { it.isInside } }
    val exitedCount = remember(locations) { locations.count { it.isExited } }
    val presentCount = remember(locations) { locations.count { it.isPresent } }
    val absentCount = remember(locations) { locations.count { !it.isPresent } }

    val filteredList = remember(locations, searchQuery, selectedFilter) {
        locations.filter { item ->
            val matchesFilter = when (selectedFilter) {
                LocationFilter.ALL -> true
                LocationFilter.PRESENT -> item.isPresent
                LocationFilter.ABSENT -> !item.isPresent
                LocationFilter.INSIDE -> item.isInside
                LocationFilter.EXITED -> item.isExited
            }
            val matchesQuery = searchQuery.isBlank() ||
                item.name.contains(searchQuery, ignoreCase = true) ||
                (item.teamName?.contains(searchQuery, ignoreCase = true) == true) ||
                (item.cardUid?.contains(searchQuery, ignoreCase = true) == true) ||
                (item.currentHallName?.contains(searchQuery, ignoreCase = true) == true)
            matchesFilter && matchesQuery
        }
    }

    val allFilteredSelected = remember(filteredList, selectedUserIds) {
        filteredList.isNotEmpty() && filteredList.all { selectedUserIds.contains(it.userId) }
    }

    AmbientBackground {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                MountAnimation(enter = MianuEnter.FadeInUp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SectionHeader(
                            title = "Operations & Locations",
                            eyebrow = "MIANU-SM",
                            subtitle = "Conference attendance & hall telemetry",
                        )
                        IconButton(onClick = { loadData() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = colors.fg,
                            )
                        }
                    }
                }
            }

            // Stat Summary Cards
            item {
                MountAnimation(enter = MianuEnter.FadeInUp, delayMillis = 60) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatRow {
                            StatCard(
                                title = "Present in Sum",
                                value = "${attendanceSummary?.totalPresent ?: presentCount} / ${attendanceSummary?.totalRegistered ?: locations.size}",
                                caption = "Rate: ${attendanceSummary?.attendanceRate ?: if (locations.isNotEmpty()) (presentCount * 100 / locations.size) else 0}%",
                                icon = Icons.Default.HowToReg,
                                accent = Color(0xFF34D399),
                                modifier = Modifier.weight(1f),
                            )
                            StatCard(
                                title = "Awaiting Arrival",
                                value = "${attendanceSummary?.totalAbsent ?: absentCount}",
                                caption = "Absent delegates",
                                icon = Icons.Default.People,
                                accent = Color(0xFFF87171),
                                modifier = Modifier.weight(1f),
                            )
                        }
                        StatRow {
                            StatCard(
                                title = "Turnstile Swipes",
                                value = "${attendanceSummary?.totalSwipes ?: 0}",
                                caption = "Conference turnstiles",
                                icon = Icons.Default.CreditCard,
                                modifier = Modifier.weight(1f),
                            )
                            StatCard(
                                title = "Inside Halls",
                                value = insideCount.toString(),
                                caption = "$exitedCount exited halls",
                                icon = Icons.Default.Place,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            // Entrance Turnstile Attendance Scanner
            item {
                MountAnimation(enter = MianuEnter.FadeInUp, delayMillis = 100) {
                    GlassPanel(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.HowToReg,
                                contentDescription = null,
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(24.dp),
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Main Entrance Turnstile Scanner",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.fg,
                                )
                                Text(
                                    text = "Tap NFC badge on device or enter UID manually",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.fgMuted,
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        SegmentedSelector(
                            options = TerminalSwipeAction.values().toList(),
                            selected = swipeAction,
                            onSelect = { swipeAction = it },
                            label = { it.label },
                        )

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            MianuTextField(
                                value = cardUidInput,
                                onValueChange = { cardUidInput = it },
                                label = "Card UID / Manual Badge",
                                placeholder = "e.g. 04A2B3C4D5 or tap NFC",
                                leadingIcon = Icons.Default.CreditCard,
                                modifier = Modifier.weight(1f),
                            )
                            MianuButton(
                                text = "Swipe",
                                onClick = { recordSwipe(cardUidInput) },
                                enabled = cardUidInput.isNotBlank() && !isSubmitting,
                                loading = isSubmitting,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }

                        swipeFeedback?.let { feedback ->
                            Spacer(Modifier.height(12.dp))
                            val bannerBg = if (feedback.isSuccess) Color(0xFF0D2818) else Color(0xFF2E0F14)
                            val bannerBorder = if (feedback.isSuccess) Color(0xFF34D399) else Color(0xFFF87171)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bannerBg)
                                    .border(1.dp, bannerBorder.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = feedback.title,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = bannerBorder,
                                        )
                                        Text(
                                            text = feedback.detail,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colors.fgMuted,
                                        )
                                    }
                                    IconButton(
                                        onClick = { swipeFeedback = null },
                                        modifier = Modifier.size(28.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Dismiss",
                                            tint = colors.fgMuted,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bulk Attendance Actions Bar
            item {
                MountAnimation(enter = MianuEnter.FadeInUp, delayMillis = 140) {
                    MianuCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = allFilteredSelected,
                                    onCheckedChange = { checked ->
                                        selectedUserIds = if (checked) {
                                            selectedUserIds + filteredList.map { it.userId }.toSet()
                                        } else {
                                            selectedUserIds - filteredList.map { it.userId }.toSet()
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color.White,
                                        uncheckedColor = colors.fgMuted,
                                        checkmarkColor = Color.Black,
                                    ),
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = if (selectedUserIds.isEmpty()) "Select (${filteredList.size})" else "Selected (${selectedUserIds.size})",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colors.fg,
                                )
                            }

                            if (selectedUserIds.isNotEmpty()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    MianuSecondaryButton(
                                        text = "Present",
                                        onClick = { handleBulkAttendance("present", all = false) },
                                        enabled = !isSubmitting,
                                        tint = Color(0xFF34D399),
                                        modifier = Modifier.height(38.dp),
                                    )
                                    MianuSecondaryButton(
                                        text = "Absent",
                                        onClick = { handleBulkAttendance("absent", all = false) },
                                        enabled = !isSubmitting,
                                        tint = Color(0xFFF87171),
                                        modifier = Modifier.height(38.dp),
                                    )
                                }
                            } else {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    MianuSecondaryButton(
                                        text = "All Present",
                                        onClick = { handleBulkAttendance("present", all = true) },
                                        enabled = !isSubmitting && locations.isNotEmpty(),
                                        tint = Color(0xFF34D399),
                                        modifier = Modifier.height(38.dp),
                                    )
                                    MianuSecondaryButton(
                                        text = "Reset All",
                                        onClick = { handleBulkAttendance("absent", all = true) },
                                        enabled = !isSubmitting && locations.isNotEmpty(),
                                        tint = Color(0xFFF87171),
                                        modifier = Modifier.height(38.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Search Bar
            item {
                MianuTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = "Search",
                    placeholder = "Search attendee, team, card UID or hall...",
                    leadingIcon = Icons.Default.Search,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Filter Tabs
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(LocationFilter.values()) { filter ->
                        FilterPill(
                            text = when (filter) {
                                LocationFilter.ALL -> "All (${locations.size})"
                                LocationFilter.PRESENT -> "Present ($presentCount)"
                                LocationFilter.ABSENT -> "Absent ($absentCount)"
                                LocationFilter.INSIDE -> "Inside ($insideCount)"
                                LocationFilter.EXITED -> "Exited ($exitedCount)"
                            },
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                        )
                    }
                }
            }

            // Loading / Error / Empty States
            if (isLoading && locations.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = colors.fg)
                    }
                }
            } else if (errorMessage != null && locations.isEmpty()) {
                item {
                    EmptyState(
                        title = "Could not load locations",
                        description = errorMessage ?: "Unknown error",
                        icon = Icons.Default.Place,
                        action = {
                            MianuButton(
                                text = "Retry",
                                onClick = { loadData() },
                            )
                        },
                    )
                }
            } else if (filteredList.isEmpty()) {
                item {
                    EmptyState(
                        title = "No delegates found",
                        description = if (searchQuery.isNotBlank()) "No match for \"$searchQuery\"" else "No delegates match this filter.",
                        icon = Icons.Default.People,
                    )
                }
            } else {
                items(filteredList, key = { it.userId }) { delegate ->
                    DelegateAttendanceCard(
                        delegate = delegate,
                        isSelected = selectedUserIds.contains(delegate.userId),
                        onToggleSelect = {
                            selectedUserIds = if (selectedUserIds.contains(delegate.userId)) {
                                selectedUserIds - delegate.userId
                            } else {
                                selectedUserIds + delegate.userId
                            }
                        },
                        onToggleAttendance = {
                            toggleSingleAttendance(delegate.userId, delegate.attendanceStatus ?: "absent")
                        },
                        isSubmitting = isSubmitting,
                    )
                }
            }
        }
    }
}

@Composable
private fun DelegateAttendanceCard(
    delegate: DelegateLocation,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onToggleAttendance: () -> Unit,
    isSubmitting: Boolean,
) {
    val colors = MianuTheme.colors

    MianuCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelect() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color.White,
                    uncheckedColor = colors.fgMuted,
                    checkmarkColor = Color.Black,
                ),
            )

            Spacer(Modifier.width(8.dp))

            // Status indicator dot / Avatar
            Box(contentAlignment = Alignment.BottomEnd) {
                Avatar(
                    initials = delegate.name.trim().split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }.ifEmpty { "?" },
                    size = 44.dp,
                )
                val dotColor = when {
                    delegate.isPresent -> Color(0xFF34D399) // Bright green
                    else -> Color(0xFFF87171) // Red absent
                }
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color.Black)
                        .padding(2.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(dotColor),
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = delegate.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.fg,
                    )
                    StatusPill(
                        text = if (delegate.isPresent) "PRESENT" else "ABSENT",
                        color = if (delegate.isPresent) Color(0xFF34D399) else Color(0xFFF87171),
                    )
                }

                Spacer(Modifier.height(3.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = delegate.teamName ?: "Individual (${delegate.role})",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.fgMuted,
                    )
                    Text(
                        text = delegate.statusDisplay,
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            delegate.isInside -> Color(0xFF34D399)
                            delegate.isExited -> Color(0xFFFBBF24)
                            else -> colors.fgMuted
                        },
                    )
                }

                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (!delegate.cardUid.isNullOrBlank()) "UID: ${delegate.cardUid}" else "No Card",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.fgMuted.copy(alpha = 0.7f),
                    )
                    if (!delegate.attendanceTime.isNullOrBlank()) {
                        val timeStr = delegate.attendanceTime.take(16).replace('T', ' ')
                        val method = delegate.attendanceMethod ?: "nfc"
                        Text(
                            text = "$timeStr ($method)",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.fgMuted,
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    MianuSecondaryButton(
                        text = if (delegate.isPresent) "Mark Absent" else "Mark Present",
                        onClick = onToggleAttendance,
                        enabled = !isSubmitting,
                        tint = if (delegate.isPresent) Color(0xFFF87171) else Color(0xFF34D399),
                        modifier = Modifier.height(34.dp),
                    )
                }
            }
        }
    }
}

