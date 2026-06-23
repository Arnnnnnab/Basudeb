package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.Announcement
import com.example.data.database.ScheduleEvent
import com.example.ui.RathaYatraViewModel
import com.example.ui.theme.SaffronDark
import com.example.ui.theme.SaffronPrimary
import com.example.ui.theme.GoldAccent
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatesScreen(viewModel: RathaYatraViewModel) {
    val events by viewModel.scheduleEvents.collectAsState()
    val announcements by viewModel.announcements.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var activeTab by remember { mutableStateOf("All") } // "All", "Broadcasts", "Timeline"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Ratha Yatra Updates",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = SaffronDark
                        )
                        Text(
                            text = "Announcements, broadcasts & live schedules",
                            style = MaterialTheme.typography.bodySmall,
                            color = SaffronDark.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.triggerRefresh() },
                        modifier = Modifier.testTag("refresh_updates_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Sync,
                            contentDescription = "Sync bulletins",
                            tint = SaffronPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .testTag("updates_screen_container")
        ) {
            if (isRefreshing) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = SaffronPrimary,
                    trackColor = GoldAccent.copy(alpha = 0.2f)
                )
            }

            // High aesthetic design element - Top Broadcast Ticker
            BroadcastTicker(announcements)

            // Segmented Tab Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tabs = listOf("All", "Broadcasts", "Timeline")
                tabs.forEach { tab ->
                    val isSelected = activeTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) SaffronPrimary else Color.Transparent)
                            .clickable { activeTab = tab }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else SaffronDark.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Beautiful status or feed list
            val timeOrderedFeed = remember(activeTab, events, announcements) {
                when (activeTab) {
                    "Broadcasts" -> announcements.sortedByDescending { it.timestamp }
                    "Timeline" -> events.sortedBy { it.startTime }
                    else -> {
                        // Merge them beautifully list or interleaved
                        val combined = mutableListOf<Any>()
                        // High priority announcements first, then chronological events
                        val criticalAnnouncements = announcements.filter { it.priority == "CRITICAL" }
                        val normalAnnouncements = announcements.filter { it.priority != "CRITICAL" }
                        
                        combined.addAll(criticalAnnouncements.sortedByDescending { it.timestamp })
                        combined.addAll(events.sortedBy { it.startTime })
                        combined.addAll(normalAnnouncements.sortedByDescending { it.timestamp })
                        combined
                    }
                }
            }

            if (timeOrderedFeed.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Campaign,
                            contentDescription = "Empty announce",
                            tint = SaffronPrimary.copy(alpha = 0.4f),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No real-time bulletins loaded",
                            style = MaterialTheme.typography.titleMedium,
                            color = SaffronDark,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap the live sync button at the top to download offline broadcasts.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("updates_feed_list"),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(timeOrderedFeed) { item ->
                        when (item) {
                            is Announcement -> AnnouncementCard(item)
                            is ScheduleEvent -> TimelineEventCard(item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BroadcastTicker(announcements: List<Announcement>) {
    val liveCount = announcements.filter { it.priority == "CRITICAL" }.size
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            .border(1.dp, SaffronPrimary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(if (liveCount > 0) Color.Red else Color(0xFF4CAF50), CircleShape)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = if (liveCount > 0) {
                "🔥 $liveCount URGENT BROADCASTS REQUIRING ATTENTION"
            } else {
                "✨ All gateways clear. Secure offline connection synced."
            },
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = SaffronDark,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AnnouncementCard(announcement: Announcement) {
    val isCritical = announcement.priority == "CRITICAL"
    val isSpiritual = announcement.priority == "SPIRITUAL"

    val containerBg = if (isCritical) {
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
    } else if (isSpiritual) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val accentBorderColor = if (isCritical) {
        MaterialTheme.colorScheme.error
    } else if (isSpiritual) {
        SaffronPrimary.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    }

    val headingColor = if (isCritical) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        SaffronDark
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, accentBorderColor, RoundedCornerShape(24.dp))
            .testTag("announcement_card_${announcement.id}"),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isCritical) Icons.Outlined.Campaign else Icons.Outlined.NotificationsActive,
                        contentDescription = null,
                        tint = if (isCritical) MaterialTheme.colorScheme.error else SaffronPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isCritical) "CRITICAL BROADCAST" else if (isSpiritual) "SPIRITUAL DIRECTIVE" else "ANNOUNCEMENT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = if (isCritical) MaterialTheme.colorScheme.error else SaffronPrimary
                    )
                }
                
                // Fine-line time tag
                val timeString = remember(announcement.timestamp) {
                    val sdf = SimpleDateFormat("h:mm a, MMM dd", Locale.getDefault())
                    sdf.format(Date(announcement.timestamp))
                }
                Text(
                    text = timeString,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = announcement.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = headingColor
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = announcement.content,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                color = if (isCritical) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun TimelineEventCard(event: ScheduleEvent) {
    val formatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val startTimeStr = formatter.format(Date(event.startTime))
    val endTimeStr = formatter.format(Date(event.endTime))

    // Pulse animation for LIVE events
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_live")
    val livePulse = if (event.isLive) {
        infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulsePulse"
        ).value
    } else 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (event.isLive) SaffronPrimary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                shape = RoundedCornerShape(24.dp)
            )
            .testTag("event_card_${event.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (event.isLive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Elegant time banner column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(76.dp)
            ) {
                Text(
                    text = startTimeStr,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (event.isLive) SaffronDark else SaffronPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "to",
                    fontSize = 10.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = endTimeStr,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Thin dividers matching elegant theme constraints
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(54.dp)
                    .background(Color.LightGray.copy(alpha = 0.4f))
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Information details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SaffronDark,
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (event.isLive) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SaffronPrimary.copy(alpha = livePulse))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "LIVE",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Location line art icon pairing
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Place,
                        contentDescription = null,
                        tint = SaffronPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.location,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SaffronPrimary
                    )
                }
            }
        }
    }
}
