package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.PdfDocumentHelper
import com.example.ui.theme.TwoMindsThemeTokens

@Composable
fun TwoMindsCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = TwoMindsThemeTokens.colors
    val shape = RoundedCornerShape(18.dp)
    Surface(
        modifier = modifier
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = shape,
        color = colors.card,
        border = BorderStroke(1.dp, colors.border),
        tonalElevation = if (colors.isDark) 2.dp else 0.dp
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

@Composable
fun CircularProgressRing(
    progress: Float,
    color: Color,
    trackColor: Color,
    size: Dp = 96.dp,
    strokeWidth: Dp = 9.dp,
    centerContent: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val inset = strokePx / 2f
            val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }
        centerContent()
    }
}

@Composable
fun WeeklyDualLineChart(
    myScores: List<Long>,
    partnerScores: List<Long>,
    modifier: Modifier = Modifier
) {
    val colors = TwoMindsThemeTokens.colors
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val mine = if (myScores.size == 7) myScores else listOf(58L, 64L, 69L, 74L, 71L, 78L, 82L)
    val partner = if (partnerScores.size == 7) partnerScores else listOf(62L, 60L, 71L, 76L, 79L, 81L, 85L)

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            val w = size.width
            val h = size.height
            val stepX = w / 6f

            // Horizontal subtle grid lines
            listOf(0.25f, 0.5f, 0.75f).forEach { frac ->
                val y = h * frac
                drawLine(
                    color = colors.border,
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            fun drawSeries(values: List<Long>, lineColor: Color) {
                val path = Path()
                values.forEachIndexed { idx, v ->
                    val x = idx * stepX
                    val y = h - ((v.coerceIn(0L, 100L) / 100f) * h)
                    if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )
                values.forEachIndexed { idx, v ->
                    val x = idx * stepX
                    val y = h - ((v.coerceIn(0L, 100L) / 100f) * h)
                    drawCircle(color = lineColor, radius = 4.dp.toPx(), center = Offset(x, y))
                }
            }

            drawSeries(partner, colors.emerald)
            drawSeries(mine, colors.gold)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
fun PartnerReadOnlyBanner(partnerName: String) {
    val colors = TwoMindsThemeTokens.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.goldBg)
            .border(1.dp, colors.gold.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Visibility,
            contentDescription = "Read-only partner view",
            tint = colors.gold,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = "Viewing $partnerName's progress (Live Sync • Read-Only)",
            style = MaterialTheme.typography.labelLarge,
            color = colors.gold,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Locked for editing",
            tint = colors.gold,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun OnlineStatusDot(online: Boolean, modifier: Modifier = Modifier) {
    val colors = TwoMindsThemeTokens.colors
    Box(
        modifier = modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(if (online) colors.emerald else colors.textSecondary.copy(alpha = 0.5f))
    )
}

@Composable
fun PdfViewerDialog(
    source: PdfDocumentHelper.PdfSource,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val colors = TwoMindsThemeTokens.colors
    var pageCount by remember { mutableIntStateOf(0) }
    var currentPage by remember { mutableIntStateOf(0) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val title = when (source) {
        is PdfDocumentHelper.PdfSource.BundledAsset -> source.title
        is PdfDocumentHelper.PdfSource.DocumentUri -> source.title
    }

    LaunchedEffect(source) {
        pageCount = PdfDocumentHelper.getPageCount(context, source)
        currentPage = 0
    }

    LaunchedEffect(source, currentPage, pageCount) {
        if (pageCount > 0) {
            isLoading = true
            bitmap = PdfDocumentHelper.renderPage(context, source, currentPage)
            isLoading = false
        } else {
            isLoading = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            color = colors.card
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = if (pageCount > 0) "Page ${currentPage + 1} of $pageCount" else "PDF Reader",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close PDF reader",
                            tint = colors.textPrimary
                        )
                    }
                }
                HorizontalDivider(color = colors.border)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(colors.subtleSurface),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isLoading -> CircularProgressIndicator(color = colors.gold)
                        bitmap != null -> Image(
                            bitmap = bitmap!!.asImageBitmap(),
                            contentDescription = "PDF page ${currentPage + 1}",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            contentScale = ContentScale.Fit
                        )
                        else -> Text(
                            text = "Unable to render PDF page.",
                            color = colors.textSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                HorizontalDivider(color = colors.border)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { if (currentPage > 0) currentPage-- },
                        enabled = currentPage > 0
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous page")
                        Spacer(Modifier.width(6.dp))
                        Text("Prev")
                    }
                    Text(
                        text = if (pageCount > 0) "${currentPage + 1} / $pageCount" else "0 / 0",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    OutlinedButton(
                        onClick = { if (currentPage < pageCount - 1) currentPage++ },
                        enabled = currentPage < pageCount - 1
                    ) {
                        Text("Next")
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next page")
                    }
                }
            }
        }
    }
}
