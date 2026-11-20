package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.BusinessItemDoc
import com.example.ui.components.TwoMindsCard
import com.example.ui.theme.TwoMindsThemeTokens

@Composable
fun BusinessScreen(
    businessItems: List<BusinessItemDoc>,
    onAddBusinessItem: (String, String, String?, String?, String) -> Unit,
    onToggleBusinessItem: (BusinessItemDoc) -> Unit
) {
    val context = LocalContext.current
    val colors = TwoMindsThemeTokens.colors
    var showAddDialog by remember { mutableStateOf(false) }

    fun openUrl(url: String) {
        try {
            val formatted = if (url.startsWith("http")) url else "https://$url"
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(formatted)))
        } catch (_: Exception) {
        }
    }

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
                        text = "Business & Google Drive",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Shared workflows, tasks & Drive files",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.gold, contentColor = colors.background)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Item")
                }
            }
        }

        // Google Drive Integration Hub
        item {
            TwoMindsCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.CloudQueue, contentDescription = "Google Drive", tint = colors.gold)
                    Column {
                        Text("Google Drive Workspace", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                        Text("Launch Drive folders, Docs, and Sheets directly", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DriveShortcutButton(
                        modifier = Modifier.weight(1f),
                        label = "My Drive",
                        icon = Icons.Default.FolderShared,
                        onClick = { openUrl("https://drive.google.com/drive/my-drive") }
                    )
                    DriveShortcutButton(
                        modifier = Modifier.weight(1f),
                        label = "Docs",
                        icon = Icons.Default.Description,
                        onClick = { openUrl("https://docs.google.com/") }
                    )
                    DriveShortcutButton(
                        modifier = Modifier.weight(1f),
                        label = "Sheets",
                        icon = Icons.Default.TableChart,
                        onClick = { openUrl("https://sheets.google.com/") }
                    )
                }
            }
        }

        item {
            Text(
                text = "Shared Business Tasks & Drive Links",
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary
            )
        }

        if (businessItems.isEmpty()) {
            item {
                TwoMindsCard(modifier = Modifier.fillMaxWidth()) {
                    Text("No business tasks added yet.", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text("Tap '+ Add Item' to add a shared milestone or attach a Google Drive link for you and Ubaid.", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                }
            }
        } else {
            items(businessItems, key = { it.id }) { item ->
                TwoMindsCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onToggleBusinessItem(item) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (item.completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = item.title,
                            tint = if (item.completed) colors.emerald else colors.textSecondary
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.title, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                            Text("${item.category} • Assigned: ${item.assigneeRole.uppercase()}", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                        }
                        if (!item.driveUrl.isNullOrBlank()) {
                            IconButton(onClick = { openUrl(item.driveUrl) }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = "Open Drive Link",
                                    tint = colors.gold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Operations") }
        var driveUrl by remember { mutableStateOf("") }
        var assignee by remember { mutableStateOf("both") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = colors.card,
            title = { Text("New Business Task / Drive Link", color = colors.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Task or Document Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category (e.g. Sales, Marketing, Ops)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = driveUrl,
                        onValueChange = { driveUrl = it },
                        label = { Text("Google Drive URL (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("both", "me", "ubaid").forEach { roleOpt ->
                            FilterChip(
                                selected = assignee == roleOpt,
                                onClick = { assignee = roleOpt },
                                label = { Text(roleOpt.replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onAddBusinessItem(title, category.ifBlank { "Operations" }, driveUrl, if (driveUrl.isNotBlank()) "Drive" else null, assignee)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.gold, contentColor = colors.background)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun DriveShortcutButton(
    modifier: Modifier = Modifier,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val colors = TwoMindsThemeTokens.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colors.subtleSurface)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = label, tint = colors.gold, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, color = colors.textPrimary)
    }
}
