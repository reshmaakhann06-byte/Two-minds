package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.E2ECryptoManager
import com.example.data.model.MemberProfileDoc
import com.example.data.model.PartnerRole
import com.example.data.model.UserProfileDoc
import com.example.ui.components.TwoMindsCard
import com.example.ui.theme.TwoMindsThemeTokens

@Composable
fun ProfileAndSettingsScreen(
    activeRole: PartnerRole,
    userProfile: UserProfileDoc?,
    myProfile: MemberProfileDoc?,
    themeMode: String,
    onSetThemeMode: (String) -> Unit,
    onUpdateProfileInfo: (String, String) -> Unit,
    onUpdatePrivacy: (Boolean, Long, Boolean) -> Unit,
    onLockApp: () -> Unit,
    onSignOut: () -> Unit
) {
    val colors = TwoMindsThemeTokens.colors
    var nameInput by remember(myProfile?.displayName) { mutableStateOf(myProfile?.displayName ?: activeRole.defaultName) }
    var bioInput by remember(myProfile?.bio) { mutableStateOf(myProfile?.bio ?: "Grow Together") }
    var savedToast by remember { mutableStateOf(false) }

    val notificationsEnabled = userProfile?.notificationsEnabled ?: true
    val disappearingSec = userProfile?.disappearingMessagesSec ?: 0L
    val blockedPartner = userProfile?.blockedPartner ?: false

    val notifPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        onUpdatePrivacy(granted, disappearingSec, blockedPartner)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Profile & Settings",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary
            )
            Text(
                text = "Customize profile, theme, security & multi-device sync",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }

        // Profile Card
        item {
            TwoMindsCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(colors.goldBg)
                            .border(1.dp, colors.gold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = nameInput.take(1).uppercase(),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.gold
                        )
                    }
                    Column {
                        Text(nameInput, style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                        Text(
                            text = "Role: ${if (activeRole == PartnerRole.ME) "Partner 1 (UI/UX & Turkish)" else "Partner 2 (Ubaid • Marketing & Spanish)"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.emerald
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it; savedToast = false },
                    label = { Text("Display Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = bioInput,
                    onValueChange = { bioInput = it; savedToast = false },
                    label = { Text("Focus / Bio") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            onUpdateProfileInfo(nameInput, bioInput)
                            savedToast = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.gold, contentColor = colors.background)
                    ) {
                        Text("Save Profile")
                    }
                    if (savedToast) {
                        Text("Saved!", style = MaterialTheme.typography.labelLarge, color = colors.emerald)
                    }
                }
            }
        }

        // Theme Appearance (Dark / Light / System)
        item {
            TwoMindsCard(modifier = Modifier.fillMaxWidth()) {
                Text("Appearance Theme", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("dark" to "Dark Obsidian", "light" to "Light Minimal", "system" to "System").forEach { (mode, label) ->
                        val selected = themeMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) colors.gold else colors.subtleSurface)
                                .clickable { onSetThemeMode(mode) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) colors.background else colors.textPrimary
                            )
                        }
                    }
                }
            }
        }

        // Privacy, Notifications, Disappearing Messages, Contact Block
        item {
            TwoMindsCard(modifier = Modifier.fillMaxWidth()) {
                Text("Chat Privacy & Notifications", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Instant Message Notifications", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                        Text("Alert immediately on new messages & price alerts", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                    }
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { enable ->
                            if (enable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                onUpdatePrivacy(enable, disappearingSec, blockedPartner)
                            }
                        }
                    )
                }
                HorizontalDivider(color = colors.border, modifier = Modifier.padding(vertical = 10.dp))
                Text("Disappearing Messages Default", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0L to "Off", 86400L to "24 Hours", 604800L to "7 Days").forEach { (sec, label) ->
                        FilterChip(
                            selected = disappearingSec == sec,
                            onClick = { onUpdatePrivacy(notificationsEnabled, sec, blockedPartner) },
                            label = { Text(label) }
                        )
                    }
                }
                HorizontalDivider(color = colors.border, modifier = Modifier.padding(vertical = 10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Block Partner Chat", style = MaterialTheme.typography.titleMedium, color = colors.coral)
                        Text("Pause incoming messages temporarily", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                    }
                    Switch(
                        checked = blockedPartner,
                        onCheckedChange = { onUpdatePrivacy(notificationsEnabled, disappearingSec, it) }
                    )
                }
            }
        }

        // Security, Multi-Device Sync & Account Actions
        item {
            TwoMindsCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Shield, contentDescription = "E2E", tint = colors.emerald)
                    Text("End-to-End Encryption & Sync", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                }
                Spacer(Modifier.height(8.dp))
                Text("Active Device: ${userProfile?.deviceInfo ?: "Android"}", style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                Text("Multi-Device Cloud Sync: Enabled via Firestore", style = MaterialTheme.typography.bodySmall, color = colors.emerald)
                Spacer(Modifier.height(8.dp))
                Surface(color = colors.subtleSurface, shape = RoundedCornerShape(10.dp)) {
                    Text(
                        text = "Safety Code: ${E2ECryptoManager.getSafetyFingerprint()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onLockApp,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Lock Code")
                    }
                    Button(
                        onClick = onSignOut,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.coral, contentColor = androidx.compose.ui.graphics.Color.White)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Sign Out")
                    }
                }
            }
        }
    }
}
