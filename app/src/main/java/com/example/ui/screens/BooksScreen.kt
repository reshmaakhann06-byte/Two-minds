package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.PdfDocumentHelper
import com.example.data.content.CuratedContent
import com.example.data.model.BookNoteDoc
import com.example.data.model.CustomBookDoc
import com.example.data.model.MemberProfileDoc
import com.example.ui.components.PdfViewerDialog
import com.example.ui.components.TwoMindsCard
import com.example.ui.theme.TwoMindsThemeTokens
import kotlinx.coroutines.launch

@Composable
fun BooksScreen(
    myProfile: MemberProfileDoc?,
    bookNotes: List<BookNoteDoc>,
    customBooks: List<CustomBookDoc>,
    onUpdateReadingProgress: (String, Long) -> Unit,
    onAddBookNote: (String, String, Long?) -> Unit,
    onAddCustomBook: (String, String, String, String, Long) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val colors = TwoMindsThemeTokens.colors
    var selectedTab by remember { mutableIntStateOf(0) } // 0=Your Books, 1=Notes, 2=Discussion
    var selectedBook by remember { mutableStateOf(CuratedContent.builtInBooks.first()) }
    var noteInput by remember { mutableStateOf("") }
    var activePdfSource by remember { mutableStateOf<PdfDocumentHelper.PdfSource?>(null) }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }
            var displayName = "Psychology / Book PDF"
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) displayName = cursor.getString(idx) ?: displayName
                }
            }
            scope.launch {
                val pages = PdfDocumentHelper.getPageCount(
                    context,
                    PdfDocumentHelper.PdfSource.DocumentUri(uri, displayName)
                ).coerceAtLeast(1)
                onAddCustomBook(displayName.removeSuffix(".pdf"), "Attached PDF", uri.toString(), "both", pages.toLong())
            }
        }
    }

    val dailyRead = myProfile?.dailyPagesRead ?: 12L
    val dailyTarget = (myProfile?.dailyPagesTarget ?: 20L).coerceAtLeast(1L)

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
                Column {
                    Text(
                        text = "Books & Psychology",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Shared reading & attached PDF library",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
                Button(
                    onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_book_pdf_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.gold,
                        contentColor = colors.background
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add PDF")
                }
            }
        }

        // Tabs: Your Books | Notes | Discussion
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
                listOf("Your Books", "Key Insights", "Discussion").forEachIndexed { idx, label ->
                    val selected = selectedTab == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) colors.gold else Color.Transparent)
                            .clickable { selectedTab = idx }
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

        when (selectedTab) {
            0 -> {
                // Core Curated Books
                items(CuratedContent.builtInBooks, key = { it.id }) { book ->
                    val isSelected = selectedBook.id == book.id
                    TwoMindsCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { selectedBook = book }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 54.dp, height = 74.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) colors.goldBg else colors.subtleSurface)
                                    .border(1.dp, if (isSelected) colors.gold else colors.border, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = book.title,
                                    tint = if (isSelected) colors.gold else colors.textSecondary
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = book.title,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = colors.textPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Surface(
                                        color = if (book.assignedTo == "Ubaid") colors.emeraldBg else colors.goldBg,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = book.assignedTo,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (book.assignedTo == "Ubaid") colors.emerald else colors.gold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "${book.author} • ${book.category}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                                Spacer(Modifier.height(6.dp))
                                val pct = if (myProfile?.currentBookTitle == book.title) {
                                    myProfile.currentBookProgress.toInt()
                                } else book.defaultProgress
                                LinearProgressIndicator(
                                    progress = { pct / 100f },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
                                    color = colors.emerald,
                                    trackColor = colors.subtleSurface
                                )
                                Text(
                                    text = "$pct% Complete",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textSecondary,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Imported Custom PDFs
                if (customBooks.isNotEmpty()) {
                    item {
                        Text(
                            text = "Attached PDF Library",
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.textPrimary
                        )
                    }
                    items(customBooks, key = { it.id }) { pdfBook ->
                        TwoMindsCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                activePdfSource = PdfDocumentHelper.PdfSource.DocumentUri(
                                    Uri.parse(pdfBook.uriString),
                                    pdfBook.title
                                )
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = "PDF book",
                                    tint = colors.coral,
                                    modifier = Modifier.size(32.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(pdfBook.title, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                                    Text("${pdfBook.totalPages} pages • Tap to read PDF", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                                }
                                Text("Open ->", style = MaterialTheme.typography.labelLarge, color = colors.gold)
                            }
                        }
                    }
                }

                // Reading Goals & Key Takeaways
                item {
                    TwoMindsCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Reading Goals", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                                Text("Daily Target: $dailyTarget Pages • Progress: $dailyRead / $dailyTarget", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                            }
                            Button(
                                onClick = { onUpdateReadingProgress(selectedBook.title, 5L) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.gold, contentColor = colors.background)
                            ) {
                                Text("+5 Pages")
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { (dailyRead.toFloat() / dailyTarget).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)),
                            color = colors.gold,
                            trackColor = colors.subtleSurface
                        )
                    }
                }
            }

            1 -> {
                // Key Insights for Selected Book
                item {
                    TwoMindsCard(modifier = Modifier.fillMaxWidth()) {
                        Text(selectedBook.title, style = MaterialTheme.typography.titleLarge, color = colors.gold)
                        Text("Core Takeaways", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                        Spacer(Modifier.height(10.dp))
                        selectedBook.keyInsights.forEach { insight ->
                            Row(
                                modifier = Modifier.padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("•", color = colors.emerald, style = MaterialTheme.typography.titleMedium)
                                Text(insight, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                            }
                        }
                    }
                }
            }

            2 -> {
                // Shared Book Notes & Discussion
                item {
                    TwoMindsCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Add Key Takeaway (${selectedBook.title})", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = noteInput,
                            onValueChange = { noteInput = it },
                            placeholder = { Text("Write a takeaway for you & Ubaid...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (noteInput.isNotBlank()) {
                                    onAddBookNote(selectedBook.id, noteInput, null)
                                    noteInput = ""
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.gold, contentColor = colors.background)
                        ) {
                            Text("Save Note")
                        }
                    }
                }

                items(bookNotes, key = { it.id }) { note ->
                    TwoMindsCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (note.authorRole == "ubaid") "Ubaid" else "You",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (note.authorRole == "ubaid") colors.emerald else colors.gold
                            )
                            Text(note.bookId, style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(note.content, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                    }
                }
            }
        }
    }

    activePdfSource?.let { source ->
        PdfViewerDialog(source = source, onDismiss = { activePdfSource = null })
    }
}
