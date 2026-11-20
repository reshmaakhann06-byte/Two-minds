package com.example.data.repository

import com.example.data.network.MarketNetworkClient
import com.example.data.network.NewsArticleDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class LiveMarketTicker(
    val symbol: String,
    val displayName: String,
    val priceUsd: Double,
    val changePercent24h: Double,
    val high24h: Double,
    val low24h: Double,
    val volumeUsd: Double
)

data class CandleBar(
    val openTimeMs: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double
)

class TradingMarketRepository {
    private val trackedSymbols = listOf(
        "BTCUSDT" to "Bitcoin",
        "ETHUSDT" to "Ethereum",
        "SOLUSDT" to "Solana",
        "BNBUSDT" to "BNB",
        "XRPUSDT" to "XRP",
        "PAXGUSDT" to "Gold (PAXG)"
    )

    suspend fun fetchLiveTickers(): Result<List<LiveMarketTicker>> = withContext(Dispatchers.IO) {
        try {
            val symbolsJson = trackedSymbols.joinToString(prefix = "[", postfix = "]") { "\"${it.first}\"" }
            val dtos = MarketNetworkClient.binanceApi.getTickers(symbolsJson)
            val nameMap = trackedSymbols.toMap()
            val tickers = dtos.map { dto ->
                LiveMarketTicker(
                    symbol = dto.symbol,
                    displayName = nameMap[dto.symbol] ?: dto.symbol,
                    priceUsd = dto.lastPrice.toDoubleOrNull() ?: 0.0,
                    changePercent24h = dto.priceChangePercent.toDoubleOrNull() ?: 0.0,
                    high24h = dto.highPrice.toDoubleOrNull() ?: 0.0,
                    low24h = dto.lowPrice.toDoubleOrNull() ?: 0.0,
                    volumeUsd = dto.quoteVolume.toDoubleOrNull() ?: 0.0
                )
            }
            Result.success(tickers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchKlines(symbol: String, interval: String): Result<List<CandleBar>> = withContext(Dispatchers.IO) {
        try {
            val raw = MarketNetworkClient.binanceApi.getKlines(symbol = symbol, interval = interval, limit = 40)
            val candles = raw.mapNotNull { row ->
                if (row.size < 6) null
                else {
                    val time = (row[0] as? Number)?.toLong() ?: 0L
                    val open = row[1].toString().toDoubleOrNull() ?: 0.0
                    val high = row[2].toString().toDoubleOrNull() ?: 0.0
                    val low = row[3].toString().toDoubleOrNull() ?: 0.0
                    val close = row[4].toString().toDoubleOrNull() ?: 0.0
                    val vol = row[5].toString().toDoubleOrNull() ?: 0.0
                    CandleBar(time, open, high, low, close, vol)
                }
            }
            Result.success(candles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchMarketNews(): Result<List<NewsArticleDto>> = withContext(Dispatchers.IO) {
        try {
            val resp = MarketNetworkClient.newsApi.getLatestNews("EN")
            Result.success(resp.data.take(12))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
