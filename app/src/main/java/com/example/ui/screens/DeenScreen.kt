package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.content.CuratedContent
import com.example.data.model.MemberProfileDoc
import com.example.data.model.PartnerRole
import com.example.ui.components.CircularProgressRing
import com.example.ui.components.PartnerReadOnlyBanner
import com.example.ui.components.TwoMindsCard
import com.example.ui.theme.TwoMindsThemeTokens

@Composable
fun DeenScreen(
    activeRole: PartnerRole,
    myProfile: MemberProfileDoc?,
    partnerProfile: MemberProfileDoc?,
    onToggleNamaz: (Int) -> Unit,
    onToggleSunnat: (Int) -> Unit,
    onIncrementTasbeeh: () -> Unit,
    onResetTasbeeh: () -> Unit,
    onSelectTasbeehPreset: (String, Int) -> Unit,
    onUpdateQuran: (String, Long, Long) -> Unit
) {
    val colors = TwoMindsThemeTokens.colors
    val haptics = LocalHapticFeedback.current
    var viewingPartner by remember { mutableStateOf(false) }
    var showDuasDialog by remember { mutableStateOf(false) }
    var selectedDuaCategory by remember { mutableStateOf("All") }
    var showQuranDialog by remember { mutableStateOf(false) }
    var showTasbeehPicker by remember { mutableStateOf(false) }

    val displayedProfile = if (viewingPartner) partnerProfile else myProfile
    val isReadOnly = viewingPartner
    val partnerName = partnerProfile?.displayName ?: activeRole.partner().defaultName
    val namazStates = displayedProfile?.namazStates ?: List(5) { it < 3 }
    val sunnatStates = displayedProfile?.sunnatStates ?: List(6) { it < 3 }
    val quranPage = displayedProfile?.quranPage ?: 112L
    val quranTotal = (displayedProfile?.quranTotalPages ?: 172L).coerceAtLeast(1L)
    val quranPct = ((quranPage.toFloat() / quranTotal) * 100f).toInt().coerceIn(0, 100)
    val tasbeehLabel = displayedProfile?.tasbeehLabel ?: "SubhanAllah"
    val tasbeehCount = displayedProfile?.tasbeehCount ?: 78L
    val tasbeehTarget = (displayedProfile?.tasbeehTarget ?: 100L).coerceAtLeast(1L)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header + You / Partner Toggle Pill
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Deen & Ibadah",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(colors.card)
                        .border(1.dp, colors.border, RoundedCornerShape(50))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val meSelected = !viewingPartner
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (meSelected) colors.gold else Color.Transparent)
                            .clickable { viewingPartner = false }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "You",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (meSelected) colors.background else colors.textSecondary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (!meSelected) colors.emerald else Color.Transparent)
                            .clickable { viewingPartner = true }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = partnerName.split(" ").first(),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (!meSelected) Color.White else colors.textSecondary
                        )
                    }
                }
            }
        }

        if (isReadOnly) {
            item { PartnerReadOnlyBanner(partnerName) }
        }

        // Five Daily Namaaz Card
        item {
            TwoMindsCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Five Daily Namaaz",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "${namazStates.count { it }} / 5 Prayed",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.emerald
                    )
                }
                Spacer(Modifier.height(10.dp))
                CuratedContent.dailyPrayers.forEachIndexed { idx, prayer ->
                    val prayed = namazStates.getOrElse(idx) { false }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = !isReadOnly) { onToggleNamaz(idx) }
                            .padding(vertical = 9.dp, horizontal = 4.dp)
                            .testTag("namaz_row_$idx"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = prayer.name,
                            tint = if (prayed) colors.gold else colors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = prayer.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "${prayer.time} • ${prayer.sunnahNote}",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                        Icon(
                            imageVector = if (prayed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = if (prayed) "Prayed" else "Not yet",
                            tint = if (prayed) colors.emerald else colors.textSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Quran Reading Card (matches mockup)
        item {
            TwoMindsCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Quran Reading & Learning",
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.textPrimary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressRing(
                                progress = quranPct / 100f,
                                color = colors.gold,
                                trackColor = colors.subtleSurface,
                                size = 76.dp,
                                strokeWidth = 8.dp
                            ) {
                                Text(
                                    text = "$quranPct%",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textPrimary
                                )
                            }
                            Column {
                                Text(
                                    text = displayedProfile?.currentSurah ?: "Surah Al-Baqarah",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "($quranPage / $quranTotal pages)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                                Text(
                                    text = "${displayedProfile?.quranMinutes ?: 45} min today",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colors.emerald
                                )
                            }
                        }
                        Button(
                            onClick = { showQuranDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.gold,
                                contentColor = colors.background
                            )
                        ) {
                            Text("Continue Reading & Tajweed ->")
                        }
                    }
                    Image(
                        painter = painterResource(id = R.drawable.img_quran_cover),
                        contentDescription = "Quran artwork",
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // Tasbeeh Counter Card
        item {
            TwoMindsCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Tasbeeh Counter",
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.textPrimary
                        )
                        Text(
                            text = tasbeehLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.gold
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { showTasbeehPicker = true },
                            enabled = !isReadOnly,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Switch Dhikr", style = MaterialTheme.typography.labelMedium)
                        }
                        IconButton(
                            onClick = onResetTasbeeh,
                            enabled = !isReadOnly
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset Tasbeeh",
                                tint = colors.textSecondary
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$tasbeehCount / $tasbeehTarget",
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Button(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onIncrementTasbeeh()
                        },
                        enabled = !isReadOnly,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("tasbeeh_count_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.gold,
                            contentColor = colors.background
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Count", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (tasbeehCount.toFloat() / tasbeehTarget).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50)),
                    color = colors.emerald,
                    trackColor = colors.subtleSurface
                )
            }
        }

        // Duas Scenic Card (Small Duas, Big Duas, Morning, Evening, Night)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, colors.border, RoundedCornerShape(18.dp))
                    .clickable { showDuasDialog = true }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_dua_scenic),
                    contentDescription = "Duas mountain dawn backdrop",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xD90B0C0F), Color(0x660B0C0F))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Small & Big Duas",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        Text(
                            text = "Morning • Evening • Night • Sayyidul Istighfar",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFDCD6C8)
                        )
                    }
                    Button(
                        onClick = { showDuasDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.gold,
                            contentColor = colors.background
                        )
                    ) {
                        Text("View All Duas ->")
                    }
                }
            }
        }

        // Sunnat Tracker
        item {
            TwoMindsCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sunnat Tracker",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textPrimary
                    )
                    Surface(
                        color = colors.emeraldBg,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "${sunnatStates.count { it }}/${CuratedContent.sunnatItems.size} Sunnahs",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.emerald,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                CuratedContent.sunnatItems.forEachIndexed { idx, sunnah ->
                    val checked = sunnatStates.getOrElse(idx) { false }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(enabled = !isReadOnly) { onToggleSunnat(idx) }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (checked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = sunnah,
                            tint = if (checked) colors.emerald else colors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = sunnah,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (checked) colors.textPrimary else colors.textSecondary
                        )
                    }
                }
            }
        }
    }

    // Duas Sheet Dialog
    if (showDuasDialog) {
        val categories = listOf("All", "Small Duas", "Big Duas", "Morning", "Evening", "Night")
        val filteredDuas = if (selectedDuaCategory == "All") CuratedContent.duasList
        else CuratedContent.duasList.filter { it.category == selectedDuaCategory }

        AlertDialog(
            onDismissRequest = { showDuasDialog = false },
            containerColor = colors.card,
            title = {
                Text("Small & Big Duas", style = MaterialTheme.typography.headlineSmall, color = colors.textPrimary)
            },
            text = {
                Column(modifier = Modifier.heightIn(max = 460.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = selectedDuaCategory == cat,
                                onClick = { selectedDuaCategory = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(filteredDuas, key = { it.id }) { dua ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = colors.subtleSurface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(dua.title, style = MaterialTheme.typography.titleMedium, color = colors.gold, modifier = Modifier.weight(1f))
                                        Text(dua.repeatCount, style = MaterialTheme.typography.labelSmall, color = colors.emerald)
                                    }
                                    Text(
                                        text = dua.arabic,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = colors.textPrimary,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text(dua.transliteration, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                                    Text(dua.translation, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDuasDialog = false }) {
                    Text("Close", color = colors.gold)
                }
            }
        )
    }

    // Quran Reading & Learning Dialog
    if (showQuranDialog) {
        AlertDialog(
            onDismissRequest = { showQuranDialog = false },
            containerColor = colors.card,
            title = {
                Text("Quran Reading & Tajweed", style = MaterialTheme.typography.headlineSmall, color = colors.textPrimary)
            },
            text = {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 460.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(CuratedContent.surahLessons, key = { it.number }) { surah ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = colors.subtleSurface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${surah.number}. ${surah.name}", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                                    Text(surah.arabicName, style = MaterialTheme.typography.titleMedium, color = colors.gold)
                                }
                                Text("${surah.ayahs} Ayahs • ${surah.keyTheme}", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                                Text(surah.studyArabicSample, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                                Text("\"${surah.studyTranslation}\"", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                                Text("Tajweed Tip: ${surah.tajweedTip}", style = MaterialTheme.typography.labelMedium, color = colors.emerald)
                                if (!isReadOnly) {
                                    Button(
                                        onClick = {
                                            onUpdateQuran(surah.name, 2L, 10L)
                                            showQuranDialog = false
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = colors.gold, contentColor = colors.background),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Log +2 Pages & +10 Min")
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQuranDialog = false }) {
                    Text("Done", color = colors.gold)
                }
            }
        )
    }

    // Tasbeeh Preset Picker Dialog
    if (showTasbeehPicker) {
        AlertDialog(
            onDismissRequest = { showTasbeehPicker = false },
            containerColor = colors.card,
            title = { Text("Choose Tasbeeh Dhikr", color = colors.textPrimary) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(CuratedContent.tasbeehPresets) { preset ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = colors.subtleSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectTasbeehPreset(preset.title, preset.target)
                                    showTasbeehPicker = false
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(preset.title, style = MaterialTheme.typography.titleMedium, color = colors.gold)
                                    Text("${preset.target}x", style = MaterialTheme.typography.labelLarge, color = colors.emerald)
                                }
                                Text(preset.arabic, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                                Text(preset.virtue, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTasbeehPicker = false }) {
                    Text("Close", color = colors.gold)
                }
            }
        )
    }
}
