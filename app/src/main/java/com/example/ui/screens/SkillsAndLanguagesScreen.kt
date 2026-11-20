package com.example.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.content.CuratedContent
import com.example.data.model.MemberProfileDoc
import com.example.data.model.PartnerRole
import com.example.ui.components.PartnerReadOnlyBanner
import com.example.ui.components.TwoMindsCard
import com.example.ui.theme.TwoMindsThemeTokens
import java.util.Locale

@Composable
fun SkillsScreen(
    activeRole: PartnerRole,
    myProfile: MemberProfileDoc?,
    partnerProfile: MemberProfileDoc?,
    onBoostSkill: (Long) -> Unit
) {
    val colors = TwoMindsThemeTokens.colors
    val defaultTrack = if (activeRole == PartnerRole.ME) "UI/UX Design" else "Digital Marketing"
    var selectedTrack by remember { mutableStateOf(defaultTrack) }
    var completedModuleIds by remember { mutableStateOf(setOf<String>()) }

    val isPartnerTrack = (activeRole == PartnerRole.ME && selectedTrack == "Digital Marketing") ||
        (activeRole == PartnerRole.UBAID && selectedTrack == "UI/UX Design")
    val partnerName = partnerProfile?.displayName ?: activeRole.partner().defaultName
    val modules = CuratedContent.skillModules.filter { it.track == selectedTrack }
    val pct = if (isPartnerTrack) (partnerProfile?.skillsPercent ?: 85L) else (myProfile?.skillsPercent ?: 75L)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Skills Mastery",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary
            )
            Text(
                text = "UI/UX Design (You) & Digital Marketing (Ubaid)",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.card)
                    .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("UI/UX Design", "Digital Marketing").forEach { track ->
                    val selected = selectedTrack == track
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) colors.gold else Color.Transparent)
                            .clickable { selectedTrack = track }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = track,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) colors.background else colors.textSecondary
                        )
                    }
                }
            }
        }

        if (isPartnerTrack) {
            item { PartnerReadOnlyBanner(partnerName) }
        }

        item {
            TwoMindsCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.School, contentDescription = null, tint = colors.gold)
                        Column {
                            Text(selectedTrack, style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                            Text("Overall Track Mastery: $pct%", style = MaterialTheme.typography.bodySmall, color = colors.emerald)
                        }
                    }
                    if (!isPartnerTrack) {
                        Button(
                            onClick = { onBoostSkill(5L) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.gold, contentColor = colors.background)
                        ) {
                            Text("+5% Practice")
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { (pct / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)),
                    color = colors.emerald,
                    trackColor = colors.subtleSurface
                )
            }
        }

        items(modules, key = { it.id }) { mod ->
            val done = mod.id in completedModuleIds
            TwoMindsCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = if (!isPartnerTrack) {
                    {
                        completedModuleIds = if (done) completedModuleIds - mod.id else completedModuleIds + mod.id
                        if (!done) onBoostSkill(5L)
                    }
                } else null
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (done) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = mod.title,
                        tint = if (done) colors.emerald else colors.textSecondary
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(mod.title, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                }
                Spacer(Modifier.height(6.dp))
                Text(mod.description, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                Spacer(Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colors.subtleSurface
                ) {
                    Text(
                        text = "Daily Drill: ${mod.exercise}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.gold,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LanguagesScreen(
    activeRole: PartnerRole,
    myProfile: MemberProfileDoc?,
    partnerProfile: MemberProfileDoc?,
    onWordLearned: () -> Unit
) {
    val context = LocalContext.current
    val colors = TwoMindsThemeTokens.colors
    val defaultLang = if (activeRole == PartnerRole.ME) "Turkish" else "Spanish"
    var selectedLang by remember { mutableStateOf(defaultLang) }
    var revealedIds by remember { mutableStateOf(setOf<String>()) }

    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        val instance = TextToSpeech(context) { }
        tts = instance
        onDispose {
            instance.stop()
            instance.shutdown()
        }
    }

    val isPartnerLang = (activeRole == PartnerRole.ME && selectedLang == "Spanish") ||
        (activeRole == PartnerRole.UBAID && selectedLang == "Turkish")
    val partnerName = partnerProfile?.displayName ?: activeRole.partner().defaultName
    val lessons = CuratedContent.languageLessons.filter { it.language == selectedLang }
    val wordsCount = if (isPartnerLang) (partnerProfile?.languageWordsLearned ?: 135L) else (myProfile?.languageWordsLearned ?: 140L)
    val langPct = if (isPartnerLang) (partnerProfile?.languagePercent ?: 70L) else (myProfile?.languagePercent ?: 72L)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Language Lab",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary
            )
            Text(
                text = "Turkish (You) & Spanish (Ubaid) • Tap audio to hear pronunciation",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.card)
                    .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Turkish", "Spanish").forEach { lang ->
                    val selected = selectedLang == lang
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) colors.gold else Color.Transparent)
                            .clickable { selectedLang = lang }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (lang == "Turkish") "Turkish (You)" else "Spanish (Ubaid)",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) colors.background else colors.textSecondary
                        )
                    }
                }
            }
        }

        if (isPartnerLang) {
            item { PartnerReadOnlyBanner(partnerName) }
        }

        item {
            TwoMindsCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Translate, contentDescription = null, tint = colors.gold)
                        Column {
                            Text("$selectedLang Mastery", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                            Text("$wordsCount phrases mastered • $langPct%", style = MaterialTheme.typography.bodySmall, color = colors.emerald)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { (langPct / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)),
                    color = colors.gold,
                    trackColor = colors.subtleSurface
                )
            }
        }

        items(lessons, key = { it.id }) { lesson ->
            val revealed = lesson.id in revealedIds
            TwoMindsCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    revealedIds = if (revealed) revealedIds - lesson.id else revealedIds + lesson.id
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(color = colors.goldBg, shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = lesson.category,
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.gold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = {
                                tts?.language = Locale.forLanguageTag(lesson.ttsLocaleTag)
                                tts?.speak(lesson.phrase, TextToSpeech.QUEUE_FLUSH, null, lesson.id)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Pronounce phrase",
                                tint = colors.gold
                            )
                        }
                        if (!isPartnerLang) {
                            OutlinedButton(
                                onClick = onWordLearned,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Learned +1", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(lesson.phrase, style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                Text("Pronunciation: ${lesson.pronunciation}", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (revealed) "Meaning: ${lesson.english}" else "Tap card to reveal English meaning",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (revealed) colors.emerald else colors.textSecondary
                )
            }
        }
    }
}
