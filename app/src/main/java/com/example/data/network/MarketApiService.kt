package com.example.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class BinanceTickerDto(
    val symbol: String = "",
    val lastPrice: String = "0",
    val priceChangePercent: String = "0",
    val highPrice: String = "0",
    val lowPrice: String = "0",
    val quoteVolume: String = "0"
)

@JsonClass(generateAdapter = true)
data class CryptoCompareNewsResponse(
    @Json(name = "Data") val data: List<NewsArticleDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class NewsArticleDto(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val source: String = "",
    val url: String = "",
    @Json(name = "published_on") val publishedOn: Long = 0L
)

interface BinancePublicApi {
    @GET("api/v3/ticker/24hr")
    suspend fun getTickers(@Query("symbols") symbolsJson: String): List<BinanceTickerDto>

    @GET("api/v3/klines")
    suspend fun getKlines(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String,
        @Query("limit") limit: Int = 40
    ): List<List<Any>>
}

interface CryptoCompareNewsApi {
    @GET("data/v2/news/")
    suspend fun getLatestNews(@Query("lang") lang: String = "EN"): CryptoCompareNewsResponse
}

object MarketNetworkClient {
    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val binanceApi: BinancePublicApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.binance.com/")
            .client(okHttp)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(BinancePublicApi::class.java)
    }

    val newsApi: CryptoCompareNewsApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://min-api.cryptocompare.com/")
            .client(okHttp)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(CryptoCompareNewsApi::class.java)
    }
}
