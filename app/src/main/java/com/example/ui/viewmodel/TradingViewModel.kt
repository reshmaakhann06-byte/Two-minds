package com.example.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.NotificationHelper
import com.example.data.model.PriceAlertDoc
import com.example.data.model.TradeEntryDoc
import com.example.data.network.NewsArticleDto
import com.example.data.repository.CandleBar
import com.example.data.repository.LiveMarketTicker
import com.example.data.repository.TradingMarketRepository
import com.example.data.repository.TwoMindsRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TradingViewModel(
    private val firestoreRepo: TwoMindsRepository,
    private val marketRepo: TradingMarketRepository,
    private val appContext: Context,
    private val currentUserId: String
) : ViewModel() {

    private val _tickers = MutableStateFlow<List<LiveMarketTicker>>(emptyList())
    val tickers: StateFlow<List<LiveMarketTicker>> = _tickers.asStateFlow()

    private val _selectedSymbol = MutableStateFlow("BTCUSDT")
    val selectedSymbol: StateFlow<String> = _selectedSymbol.asStateFlow()

    private val _selectedInterval = MutableStateFlow("1h")
    val selectedInterval: StateFlow<String> = _selectedInterval.asStateFlow()

    private val _candles = MutableStateFlow<List<CandleBar>>(emptyList())
    val candles: StateFlow<List<CandleBar>> = _candles.asStateFlow()

    private val _news = MutableStateFlow<List<NewsArticleDto>>(emptyList())
    val news: StateFlow<List<NewsArticleDto>> = _news.asStateFlow()

    private val _marketStatus = MutableStateFlow("Connecting to live feed...")
    val marketStatus: StateFlow<String> = _marketStatus.asStateFlow()

    private val _tradeFeedback = MutableStateFlow<String?>(null)
    val tradeFeedback: StateFlow<String?> = _tradeFeedback.asStateFlow()

    val tradesState: StateFlow<UiState<List<TradeEntryDoc>>> = firestoreRepo.observeTrades(currentUserId)
        .map<List<TradeEntryDoc>, UiState<List<TradeEntryDoc>>> { UiState.Success(it) }
        .catch { e ->
            val fe = generateSequence(e) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
            if (fe == null && e is CancellationException) throw e
            emit(UiState.Error((fe ?: e).message ?: "Failed to load trades"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val alertsState: StateFlow<UiState<List<PriceAlertDoc>>> = firestoreRepo.observePriceAlerts(currentUserId)
        .map<List<PriceAlertDoc>, UiState<List<PriceAlertDoc>>> { UiState.Success(it) }
        .catch { e ->
            val fe = generateSequence(e) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
            if (fe == null && e is CancellationException) throw e
            emit(UiState.Error((fe ?: e).message ?: "Failed to load price alerts"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    init {
        startLiveMarketPolling()
        refreshCandles()
        refreshNews()
    }

    private fun startLiveMarketPolling() {
        viewModelScope.launch {
            while (isActive) {
                val res = marketRepo.fetchLiveTickers()
                if (res.isSuccess) {
                    val list = res.getOrNull().orEmpty()
                    _tickers.value = list
                    _marketStatus.value = "Live Binance Feed"
                    checkPriceAlerts(list)
                } else {
                    Log.w("TradingVM", "Live feed retry: ${res.exceptionOrNull()?.message}")
                    _marketStatus.value = "Offline / Reconnecting..."
                }
                delay(10_000L)
            }
        }
    }

    fun selectSymbol(symbol: String) {
        _selectedSymbol.value = symbol
        refreshCandles()
    }

    fun selectInterval(interval: String) {
        _selectedInterval.value = interval
        refreshCandles()
    }

    fun refreshCandles() {
        viewModelScope.launch {
            val res = marketRepo.fetchKlines(_selectedSymbol.value, _selectedInterval.value)
            if (res.isSuccess) {
                _candles.value = res.getOrNull().orEmpty()
            }
        }
    }

    fun refreshNews() {
        viewModelScope.launch {
            val res = marketRepo.fetchMarketNews()
            if (res.isSuccess) {
                _news.value = res.getOrNull().orEmpty()
            }
        }
    }

    fun placePaperOrder(side: String, usdAmount: Double, notes: String?) {
        val ticker = _tickers.value.firstOrNull { it.symbol == _selectedSymbol.value }
        if (ticker == null || ticker.priceUsd <= 0.0) {
            _tradeFeedback.value = "Waiting for live market quote before placing order."
            return
        }
        if (usdAmount <= 0.0) {
            _tradeFeedback.value = "Enter a positive USD amount."
            return
        }
        val qty = usdAmount / ticker.priceUsd
        viewModelScope.launch {
            val res = firestoreRepo.placePaperTrade(
                symbol = ticker.symbol,
                side = side,
                quantity = qty,
                priceUsd = ticker.priceUsd,
                notes = notes
            )
            _tradeFeedback.value = if (res.isSuccess) {
                "$side ${"%.5f".format(qty)} ${ticker.symbol.removeSuffix("USDT")} @ $${"%.2f".format(ticker.priceUsd)}"
            } else {
                res.exceptionOrNull()?.localizedMessage ?: "Order failed"
            }
        }
    }

    fun addPriceAlert(symbol: String, targetPrice: Double, condition: String) {
        viewModelScope.launch {
            val res = firestoreRepo.addPriceAlert(symbol, targetPrice, condition)
            _tradeFeedback.value = if (res.isSuccess) {
                "Alert armed: $symbol $condition $$targetPrice"
            } else {
                res.exceptionOrNull()?.localizedMessage ?: "Could not save alert"
            }
        }
    }

    fun deletePriceAlert(alertId: String) {
        viewModelScope.launch {
            firestoreRepo.deletePriceAlert(alertId)
        }
    }

    fun clearFeedback() {
        _tradeFeedback.value = null
    }

    private suspend fun checkPriceAlerts(liveTickers: List<LiveMarketTicker>) {
        val alerts = (alertsState.value as? UiState.Success)?.data ?: return
        val priceMap = liveTickers.associateBy({ it.symbol }, { it.priceUsd })
        for (alert in alerts) {
            if (!alert.active || alert.triggered) continue
            val live = priceMap[alert.symbol] ?: continue
            val hit = when (alert.condition) {
                "above" -> live >= alert.targetPrice
                "below" -> live <= alert.targetPrice
                else -> false
            }
            if (hit) {
                firestoreRepo.markPriceAlertTriggered(alert.id)
                NotificationHelper.showPriceAlertNotification(
                    appContext,
                    alert.symbol,
                    "$${"%.2f".format(live)}"
                )
            }
        }
    }
}
