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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.PdfDocumentHelper
import com.example.data.content.CuratedContent
import com.example.data.model.MemberProfileDoc
import com.example.ui.components.PdfViewerDialog
import com.example.ui.components.TwoMindsCard
import com.example.ui.theme.TwoMindsThemeTokens

@Composable
fun FitnessScreen(
    myProfile: MemberProfileDoc?,
    onToggleWorkoutCompleted: () -> Unit,
    onUpdateWater: (Long) -> Unit,
    onUpdateSleep: (Double) -> Unit
) {
    val colors = TwoMindsThemeTokens.colors
    var selectedSection by remember { mutableIntStateOf(0) } // 0=Workout, 1=Diet & Meals, 2=Protein Guide
    var selectedDayIndex by remember { mutableIntStateOf(0) }
    var checkedExercises by remember { mutableStateOf(setOf<String>()) }
    var showFullPdf by remember { mutableStateOf(false) }

    val water = myProfile?.waterGlasses ?: 5L
    val sleep = myProfile?.sleepHours ?: 7.5
    val workoutCompleted = myProfile?.workoutCompleted ?: true
    val activeDay = CuratedContent.weeklyWorkouts[selectedDayIndex]

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Fitness & Diet Planner",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Home training & affordable Indian meal plan",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
                OutlinedButton(
                    onClick = { showFullPdf = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = "Open PDF", tint = colors.gold, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("PDF Plan", color = colors.gold)
                }
            }
        }

        // Section Tabs: Workout | Diet & Meals | Protein Guide
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
                listOf("Workout", "Diet & Meals", "Protein Guide").forEachIndexed { idx, label ->
                    val selected = selectedSection == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) colors.gold else Color.Transparent)
                            .clickable { selectedSection = idx }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) colors.background else colors.textSecondary
                        )
                    }
                }
            }
        }

        when (selectedSection) {
            0 -> {
                // Mon - Sun Day Selector (matches mockup)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CuratedContent.weeklyWorkouts.forEachIndexed { idx, day ->
                            val selected = selectedDayIndex == idx
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (selected) colors.gold else colors.card)
                                    .border(1.dp, if (selected) colors.gold else colors.border, RoundedCornerShape(14.dp))
                                    .clickable { selectedDayIndex = idx }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = day.dayShort,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = if (selected) colors.background else colors.textPrimary
                                )
                                Text(
                                    text = day.focus.split(" ").first(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (selected) colors.background.copy(alpha = 0.85f) else colors.textSecondary
                                )
                            }
                        }
                    }
                }

                // Active Workout Session Card
                item {
                    TwoMindsCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${activeDay.dayFull}: ${activeDay.focus}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Warm-up 5-7 min • Cool-down 3-5 min",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                            Image(
                                painter = painterResource(id = R.drawable.img_workout_pushup),
                                contentDescription = "Workout form guide",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        activeDay.exercises.forEach { ex ->
                            val key = "${activeDay.dayShort}_${ex.name}"
                            val done = key in checkedExercises
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.subtleSurface)
                                    .clickable {
                                        checkedExercises = if (done) checkedExercises - key else checkedExercises + key
                                    }
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (done) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = ex.name,
                                        tint = if (done) colors.emerald else colors.textSecondary
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(ex.name, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                                        Text(ex.dose, style = MaterialTheme.typography.labelMedium, color = colors.gold)
                                    }
                                }
                                Spacer(Modifier.height(6.dp))
                                Text("Form: ${ex.howTo}", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                                Text("Avoid: ${ex.avoid}", style = MaterialTheme.typography.bodySmall, color = colors.coral)
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                        Button(
                            onClick = onToggleWorkoutCompleted,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (workoutCompleted) colors.emerald else colors.gold,
                                contentColor = if (workoutCompleted) Color.White else colors.background
                            )
                        ) {
                            Icon(if (workoutCompleted) Icons.Default.CheckCircle else Icons.Default.FitnessCenter, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (workoutCompleted) "Today's Workout Completed" else "Mark Workout Completed")
                        }
                    }
                }

                // Water Intake & Sleep Cards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TwoMindsCard(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.WaterDrop, contentDescription = "Water", tint = colors.infoBlue)
                                Text("Water Intake", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("$water / 8 Glasses", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { (water / 8f).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
                                color = colors.infoBlue,
                                trackColor = colors.subtleSurface
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { onUpdateWater(-1L) }, contentPadding = PaddingValues(horizontal = 12.dp)) { Text("-") }
                                Button(
                                    onClick = { onUpdateWater(1L) },
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.infoBlue, contentColor = Color.White),
                                    contentPadding = PaddingValues(horizontal = 12.dp)
                                ) { Text("+1") }
                            }
                        }
                        TwoMindsCard(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Bedtime, contentDescription = "Sleep", tint = colors.gold)
                                Text("Sleep", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("${"%.1f".format(sleep)} / 8 hrs", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { (sleep.toFloat() / 8f).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
                                color = colors.gold,
                                trackColor = colors.subtleSurface
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { onUpdateSleep(sleep - 0.5) }, contentPadding = PaddingValues(horizontal = 10.dp)) { Text("-0.5") }
                                Button(
                                    onClick = { onUpdateSleep(sleep + 0.5) },
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.gold, contentColor = colors.background),
                                    contentPadding = PaddingValues(horizontal = 10.dp)
                                ) { Text("+0.5") }
                            }
                        }
                    }
                }
            }

            1 -> {
                items(CuratedContent.dailyMeals) { meal ->
                    TwoMindsCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(meal.timeMeal, style = MaterialTheme.typography.titleMedium, color = colors.gold)
                            Text(meal.proteinGuide, style = MaterialTheme.typography.labelMedium, color = colors.emerald)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(meal.whatToEat, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                    }
                }
            }

            2 -> {
                item {
                    TwoMindsCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Cheap Protein Quick Guide", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                        Spacer(Modifier.height(10.dp))
                        CuratedContent.cheapProteinGuide.forEach { (food, protein) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(food, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
                                Text(protein, style = MaterialTheme.typography.labelLarge, color = colors.emerald)
                            }
                            HorizontalDivider(color = colors.border)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "Safety Note: Stop immediately for sharp pain, chest pain, or dizziness. Good technique matters more than high reps.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }
    }

    if (showFullPdf) {
        PdfViewerDialog(
            source = PdfDocumentHelper.PdfSource.BundledAsset(
                "home_fitness_weekly_plan.pdf",
                "The Home Fitness Planner (PDF)"
            ),
            onDismiss = { showFullPdf = false }
        )
    }
}
