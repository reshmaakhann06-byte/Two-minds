package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.content.CuratedContent
import com.example.data.model.PriceAlertDoc
import com.example.data.model.TradeEntryDoc
import com.example.data.network.NewsArticleDto
import com.example.data.repository.CandleBar
import com.example.data.repository.LiveMarketTicker
import com.example.ui.components.TwoMindsCard
import com.example.ui.theme.TwoMindsThemeTokens

@Composable
fun TradingScreen(
    tickers: List<LiveMarketTicker>,
    selectedSymbol: String,
    selectedInterval: String,
    candles: List<CandleBar>,
    news: List<NewsArticleDto>,
    marketStatus: String,
    trades: List<TradeEntryDoc>,
    alerts: List<PriceAlertDoc>,
    feedbackMessage: String?,
    onSelectSymbol: (String) -> Unit,
    onSelectInterval: (String) -> Unit,
    onPlaceOrder: (String, Double, String?) -> Unit,
    onAddAlert: (String, Double, String) -> Unit,
    onDeleteAlert: (String) -> Unit,
    onClearFeedback: () -> Unit
) {
    val context = LocalContext.current
    val colors = TwoMindsThemeTokens.colors
    var subTab by remember { mutableIntStateOf(0) } // 0=Markets & Chart, 1=Portfolio, 2=Alerts, 3=News & Academy
    var orderAmountStr by remember { mutableStateOf("500") }
    var orderNote by remember { mutableStateOf("") }
    var showAlertDialog by remember { mutableStateOf(false) }

    val activeTicker = tickers.firstOrNull { it.symbol == selectedSymbol }

    // Portfolio valuation from paper trades
    val startingCash = 10_000.0
    val netCashSpent = trades.sumOf { if (it.side == "BUY") it.totalUsd else -it.totalUsd }
    val cashBalance = (startingCash - netCashSpent).coerceAtLeast(0.0)
    val holdingsBySymbol = trades.groupBy { it.symbol }.mapValues { (_, list) ->
        list.sumOf { if (it.side == "BUY") it.quantity else -it.quantity }
    }.filterValues { it > 0.00001 }
    val priceMap = tickers.associateBy({ it.symbol }, { it.priceUsd })
    val holdingsValue = holdingsBySymbol.entries.sumOf { (sym, qty) -> qty * (priceMap[sym] ?: 0.0) }
    val totalPortfolio = cashBalance + holdingsValue
    val pnl = totalPortfolio - startingCash

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Trading Desk & Academy",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Text(
                        text = marketStatus,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (marketStatus.contains("Live")) colors.emerald else colors.gold
                    )
                }
                Surface(color = colors.card, shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), horizontalAlignment = Alignment.End) {
                        Text("$${"%.2f".format(totalPortfolio)}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
                        Text(
                            text = "${if (pnl >= 0) "+" else ""}${"%.2f".format(pnl)} P&L",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (pnl >= 0) colors.emerald else colors.coral
                        )
                    }
                }
            }
        }

        if (feedbackMessage != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.goldBg,
                    modifier = Modifier.fillMaxWidth().clickable { onClearFeedback() }
                ) {
                    Text(
                        text = feedbackMessage,
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.gold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // Sub-navigation pills
        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Charts & Trade", "Portfolio & History", "Price Alerts", "News & Academy").forEachIndexed { idx, label ->
                    FilterChip(
                        selected = subTab == idx,
                        onClick = { subTab = idx },
                        label = { Text(label) }
                    )
                }
            }
        }

        when (subTab) {
            0 -> {
                // Live Market Ticker Strip
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (tickers.isEmpty()) {
                            TwoMindsCard { Text("Loading live market quotes...", color = colors.textSecondary) }
                        } else {
                            tickers.forEach { ticker ->
                                val selected = ticker.symbol == selectedSymbol
                                val positive = ticker.changePercent24h >= 0
                                TwoMindsCard(
                                    modifier = Modifier.width(156.dp),
                                    onClick = { onSelectSymbol(ticker.symbol) }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = ticker.symbol.removeSuffix("USDT"),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (selected) colors.gold else colors.textPrimary
                                        )
                                        Text(
                                            text = "${if (positive) "+" else ""}${"%.2f".format(ticker.changePercent24h)}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (positive) colors.emerald else colors.coral
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "$${"%.2f".format(ticker.priceUsd)}",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = colors.textPrimary
                                    )
                                    Text(ticker.displayName, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                                }
                            }
                        }
                    }
                }

                // Technical Analysis Candlestick Chart + SMA(7)
                item {
                    TwoMindsCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "$selectedSymbol • Candlestick + SMA(7)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.textPrimary
                                )
                                activeTicker?.let {
                                    Text(
                                        text = "24h H: $${"%.2f".format(it.high24h)}  L: $${"%.2f".format(it.low24h)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("15m", "1h", "4h", "1d").forEach { iv ->
                                    val sel = selectedInterval == iv
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (sel) colors.gold else colors.subtleSurface)
                                            .clickable { onSelectInterval(iv) }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = iv,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (sel) colors.background else colors.textPrimary
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        CandlestickChartCanvas(
                            candles = candles,
                            bullColor = colors.emerald,
                            bearColor = colors.coral,
                            smaColor = colors.gold,
                            gridColor = colors.border,
                            modifier = Modifier.fillMaxWidth().height(190.dp)
                        )
                    }
                }

                // Paper Order Ticket
                item {
                    TwoMindsCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Practice Paper Order ($selectedSymbol)", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                        Text("Available Virtual Cash: $${"%.2f".format(cashBalance)}", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = orderAmountStr,
                            onValueChange = { orderAmountStr = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            label = { Text("Amount (USD)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = orderNote,
                            onValueChange = { orderNote = it },
                            label = { Text("Setup Thesis / Journal Note (optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val amt = orderAmountStr.toDoubleOrNull() ?: 0.0
                                    onPlaceOrder("BUY", amt, orderNote)
                                    orderNote = ""
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.emerald, contentColor = Color.White)
                            ) {
                                Text("BUY LONG")
                            }
                            Button(
                                onClick = {
                                    val amt = orderAmountStr.toDoubleOrNull() ?: 0.0
                                    onPlaceOrder("SELL", amt, orderNote)
                                    orderNote = ""
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.coral, contentColor = Color.White)
                            ) {
                                Text("SELL")
                            }
                        }
                    }
                }
            }

            1 -> {
                // Holdings
                item {
                    TwoMindsCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Portfolio Breakdown", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                        Spacer(Modifier.height(8.dp))
                        Text("Cash Balance: $${"%.2f".format(cashBalance)}", style = MaterialTheme.typography.bodyLarge, color = colors.emerald)
                        if (holdingsBySymbol.isEmpty()) {
                            Text("No open positions yet. Execute a paper trade in Charts & Trade.", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                        } else {
                            holdingsBySymbol.forEach { (sym, qty) ->
                                val valUsd = qty * (priceMap[sym] ?: 0.0)
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("$sym (${"%.5f".format(qty)})", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                                    Text("$${"%.2f".format(valUsd)}", style = MaterialTheme.typography.labelLarge, color = colors.gold)
                                }
                            }
                        }
                    }
                }
                item {
                    Text("Transaction History", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                }
                items(trades, key = { it.id }) { trade ->
                    TwoMindsCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${trade.side} ${trade.symbol}",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (trade.side == "BUY") colors.emerald else colors.coral
                            )
                            Text("$${"%.2f".format(trade.totalUsd)}", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                        }
                        Text("${"%.5f".format(trade.quantity)} @ $${"%.2f".format(trade.priceUsd)}", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                        if (!trade.notes.isNullOrBlank()) {
                            Text("Note: ${trade.notes}", style = MaterialTheme.typography.bodySmall, color = colors.gold)
                        }
                    }
                }
            }

            2 -> {
                item {
                    Button(
                        onClick = { showAlertDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.gold, contentColor = colors.background)
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Create New Price Alert")
                    }
                }
                items(alerts, key = { it.id }) { alert ->
                    TwoMindsCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("${alert.symbol} ${alert.condition.uppercase()} $${alert.targetPrice}", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                                Text(
                                    text = if (alert.triggered) "Triggered!" else if (alert.active) "Active • Watching live feed" else "Paused",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (alert.triggered) colors.emerald else colors.gold
                                )
                            }
                            IconButton(onClick = { onDeleteAlert(alert.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete alert", tint = colors.coral)
                            }
                        }
                    }
                }
            }

            3 -> {
                item {
                    Text("Trading Academy", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                }
                items(CuratedContent.tradingLessons) { lesson ->
                    TwoMindsCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(lesson.title, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary, modifier = Modifier.weight(1f))
                            Surface(color = colors.goldBg, shape = RoundedCornerShape(8.dp)) {
                                Text(lesson.badge, style = MaterialTheme.typography.labelMedium, color = colors.gold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(lesson.summary, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                        Spacer(Modifier.height(6.dp))
                        Text("Rule: ${lesson.checklist}", style = MaterialTheme.typography.bodySmall, color = colors.emerald)
                    }
                }
                item {
                    Text("Live Market News", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                }
                items(news, key = { it.id }) { article ->
                    TwoMindsCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (article.url.isNotBlank()) {
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(article.url)))
                                } catch (_: Exception) {
                                }
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(article.source, style = MaterialTheme.typography.labelMedium, color = colors.gold)
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Read article", tint = colors.textSecondary, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(article.title, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text(article.body.take(140) + "...", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                    }
                }
            }
        }
    }

    if (showAlertDialog) {
        var targetStr by remember { mutableStateOf(activeTicker?.priceUsd?.toInt()?.toString() ?: "") }
        var condition by remember { mutableStateOf("above") }
        AlertDialog(
            onDismissRequest = { showAlertDialog = false },
            containerColor = colors.card,
            title = { Text("Price Alert ($selectedSymbol)", color = colors.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = targetStr,
                        onValueChange = { targetStr = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Target Price (USD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("above", "below").forEach { cond ->
                            FilterChip(
                                selected = condition == cond,
                                onClick = { condition = cond },
                                label = { Text(cond.replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val price = targetStr.toDoubleOrNull() ?: 0.0
                        if (price > 0.0) {
                            onAddAlert(selectedSymbol, price, condition)
                            showAlertDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.gold, contentColor = colors.background)
                ) {
                    Text("Arm Alert")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAlertDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun CandlestickChartCanvas(
    candles: List<CandleBar>,
    bullColor: Color,
    bearColor: Color,
    smaColor: Color,
    gridColor: Color,
    modifier: Modifier = Modifier
) {
    if (candles.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Loading technical chart...", style = MaterialTheme.typography.bodySmall)
        }
        return
    }
    Canvas(modifier = modifier) {
        val minPrice = candles.minOf { it.low }
        val maxPrice = candles.maxOf { it.high }
        val range = (maxPrice - minPrice).coerceAtLeast(0.0001)
        val w = size.width
        val h = size.height
        val slotW = w / candles.size.toFloat()
        val bodyW = (slotW * 0.62f).coerceAtLeast(3f)

        fun yFor(price: Double): Float {
            return h - (((price - minPrice) / range).toFloat() * h)
        }

        // Subtle grid lines
        listOf(0.25f, 0.5f, 0.75f).forEach { f ->
            drawLine(gridColor, Offset(0f, h * f), Offset(w, h * f), strokeWidth = 1.dp.toPx())
        }

        val smaPath = Path()
        candles.forEachIndexed { idx, c ->
            val cx = idx * slotW + slotW / 2f
            val bull = c.close >= c.open
            val color = if (bull) bullColor else bearColor

            // Wick
            drawLine(
                color = color,
                start = Offset(cx, yFor(c.high)),
                end = Offset(cx, yFor(c.low)),
                strokeWidth = 1.5.dp.toPx()
            )

            // Body
            val topY = yFor(maxOf(c.open, c.close))
            val botY = yFor(minOf(c.open, c.close))
            val rectH = (botY - topY).coerceAtLeast(2.dp.toPx())
            drawRect(
                color = color,
                topLeft = Offset(cx - bodyW / 2f, topY),
                size = Size(bodyW, rectH)
            )

            // 7-period SMA overlay
            val windowStart = (idx - 6).coerceAtLeast(0)
            val sma = candles.subList(windowStart, idx + 1).map { it.close }.average()
            val smaY = yFor(sma)
            if (idx == 0) smaPath.moveTo(cx, smaY) else smaPath.lineTo(cx, smaY)
        }
        drawPath(smaPath, color = smaColor, style = Stroke(width = 2.dp.toPx()))
    }
}
