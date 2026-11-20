package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.content.CuratedContent
import com.example.data.model.MemberProfileDoc
import com.example.data.model.PartnerRole
import com.example.ui.components.OnlineStatusDot
import com.example.ui.components.TwoMindsCard
import com.example.ui.components.WeeklyDualLineChart
import com.example.ui.theme.TwoMindsThemeTokens

@Composable
fun DashboardScreen(
    activeRole: PartnerRole,
    myProfile: MemberProfileDoc?,
    partnerProfile: MemberProfileDoc?,
    onToggleChecklist: (Int) -> Unit,
    onNavigateToRoute: (String) -> Unit
) {
    val colors = TwoMindsThemeTokens.colors
    val myName = myProfile?.displayName ?: activeRole.defaultName
    val partnerName = partnerProfile?.displayName ?: activeRole.partner().defaultName
    val checklist = myProfile?.checklistStates ?: List(7) { it < 5 }
    val doneTasks = checklist.count { it }
    val totalTasks = checklist.size.coerceAtLeast(1)
    val quranMins = myProfile?.quranMinutes ?: 45L
    val workoutDone = myProfile?.workoutCompleted ?: true
    val myPct = myProfile?.overallPercent ?: 78L
    val partnerPct = partnerProfile?.overallPercent ?: 82L
    val partnerOnline = partnerProfile?.online == true

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Greeting & Partner Sync Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Assalamu Alaikum!",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Let's make today count, together.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PartnerBadgePill(
                        label = "You",
                        online = true,
                        accent = colors.gold
                    )
                    PartnerBadgePill(
                        label = partnerName.split(" ").first(),
                        online = partnerOnline,
                        accent = colors.emerald,
                        onClick = { onNavigateToRoute("together") }
                    )
                }
            }
        }

        // 4 Summary Metric Cards (Responsive 2x2 grid)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricSummaryCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.CheckCircle,
                        iconTint = colors.emerald,
                        iconBg = colors.emeraldBg,
                        value = "$doneTasks / $totalTasks",
                        label = "Tasks Today",
                        onClick = { onNavigateToRoute("deen") }
                    )
                    MetricSummaryCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        iconTint = colors.gold,
                        iconBg = colors.goldBg,
                        value = "$quranMins min",
                        label = "Quran Reading",
                        onClick = { onNavigateToRoute("deen") }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricSummaryCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.FitnessCenter,
                        iconTint = if (workoutDone) colors.emerald else colors.gold,
                        iconBg = if (workoutDone) colors.emeraldBg else colors.goldBg,
                        value = if (workoutDone) "Completed" else "Pending",
                        label = "Workout",
                        onClick = { onNavigateToRoute("fitness") }
                    )
                    MetricSummaryCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.BarChart,
                        iconTint = colors.emerald,
                        iconBg = colors.emeraldBg,
                        value = "$myPct% / $partnerPct%",
                        label = "Our Progress",
                        onClick = { onNavigateToRoute("together") }
                    )
                }
            }
        }

        // Quran Quote Hero Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(172.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                    .clickable { onNavigateToRoute("deen") }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_quran_hero),
                    contentDescription = "Twilight mosque spiritual backdrop",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xE60B0C0F),
                                    Color(0x990B0C0F),
                                    Color(0x330B0C0F)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "\"And whoever strives only\nstrives for himself.\"",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFFF6F1E6)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "— Quran 29:6",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.gold
                    )
                }
            }
        }

        // Today's Checklist Card
        item {
            TwoMindsCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Today's Checklist",
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "$myName • Live Synced",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                    TextButton(onClick = { onNavigateToRoute("together") }) {
                        Text("Compare ->", color = colors.gold)
                    }
                }
                Spacer(Modifier.height(8.dp))
                CuratedContent.defaultChecklistTitles.forEachIndexed { idx, title ->
                    val checked = checklist.getOrElse(idx) { false }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onToggleChecklist(idx) }
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                            .testTag("checklist_item_$idx"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (checked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = if (checked) "Completed" else "Pending",
                            tint = if (checked) colors.emerald else colors.textSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (checked) colors.textPrimary else colors.textSecondary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Weekly Progress Chart (You vs Ubaid)
        item {
            TwoMindsCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Weekly Progress",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textPrimary
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).clip(CircleShape).background(colors.gold))
                            Spacer(Modifier.width(4.dp))
                            Text("You", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).clip(CircleShape).background(colors.emerald))
                            Spacer(Modifier.width(4.dp))
                            Text(partnerName.split(" ").first(), style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                WeeklyDualLineChart(
                    myScores = myProfile?.weeklyScores.orEmpty(),
                    partnerScores = partnerProfile?.weeklyScores.orEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Quick Access Module Grid
        item {
            TwoMindsCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Quick Access",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary
                )
                Spacer(Modifier.height(12.dp))
                val shortcuts = listOf(
                    Triple("Deen & Dua", Icons.Default.Mosque, "deen"),
                    Triple("Books PDF", Icons.AutoMirrored.Filled.MenuBook, "books"),
                    Triple("Fitness Plan", Icons.Default.FitnessCenter, "fitness"),
                    Triple("Live Chat", Icons.Default.ChatBubble, "together"),
                    Triple("Trading Lab", Icons.Default.CandlestickChart, "trading"),
                    Triple("Drive & Biz", Icons.Default.CloudQueue, "business"),
                    Triple("Skills", Icons.Default.School, "skills"),
                    Triple("Languages", Icons.Default.Translate, "languages")
                )
                shortcuts.chunked(4).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { (label, icon, route) ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(colors.subtleSurface)
                                    .clickable { onNavigateToRoute(route) }
                                    .padding(vertical = 12.dp, horizontal = 4.dp)
                                    .testTag("quick_access_$route"),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = colors.gold,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PartnerBadgePill(
    label: String,
    online: Boolean,
    accent: Color,
    onClick: (() -> Unit)? = null
) {
    val colors = TwoMindsThemeTokens.colors
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(colors.card)
            .border(1.dp, colors.border, RoundedCornerShape(50))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        OnlineStatusDot(online = online)
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = accent
        )
    }
}

@Composable
private fun MetricSummaryCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    value: String,
    label: String,
    onClick: () -> Unit
) {
    val colors = TwoMindsThemeTokens.colors
    TwoMindsCard(
        modifier = modifier,
        onClick = onClick,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
