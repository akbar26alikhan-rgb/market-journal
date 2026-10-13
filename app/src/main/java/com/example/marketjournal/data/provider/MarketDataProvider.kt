package com.example.marketjournal.data.provider

import com.example.marketjournal.model.MarketBreadthSummary
import com.example.marketjournal.model.MarketDataProviderType
import com.example.marketjournal.model.ProviderStatus
import com.example.marketjournal.model.Stock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

/**
 * Standard interface for Indian market data feeds (Quotes, technical metrics, fundamentals).
 */
interface MarketDataProvider {
    suspend fun getWatchlist(): Result<List<Stock>>
    suspend fun getStockDetails(symbol: String): Result<Stock>
    suspend fun getMarketBreadth(): Result<MarketBreadthSummary>
    fun getProviderType(): MarketDataProviderType
    fun getConfigurationStatus(): ProviderStatus
}

/**
 * High-fidelity Demo Provider with realistic Indian NSE stocks,
 * explicitly labeled with `isDemoData = true` so users never confuse demo simulation with real trades.
 */
class DemoMarketDataProvider : MarketDataProvider {

    private val demoStocks: List<Stock> = listOf(
        Stock(
            symbol = "RELIANCE",
            companyName = "Reliance Industries Ltd",
            sector = "Energy / Oil & Retail",
            currentPrice = 2942.50,
            changePercent = 1.45,
            dayHigh = 2958.00,
            dayLow = 2912.10,
            volume = 4850000,
            avgVolume20D = 3200000,
            relativeVolume = 1.52,
            deliveryPercent = 58.4,
            marketCapCr = 1990000.0,
            pe = 27.8,
            sectorPe = 24.5,
            pb = 2.4,
            roe = 10.2,
            roce = 11.4,
            debtToEquity = 0.42,
            salesGrowth3Y = 14.8,
            profitGrowth3Y = 11.2,
            rsi14 = 62.4,
            ema20 = 2910.0,
            ema50 = 2860.0,
            ema200 = 2650.0,
            vwap = 2935.40,
            atr14 = 42.5,
            isDemoData = true
        ),
        Stock(
            symbol = "TCS",
            companyName = "Tata Consultancy Services Ltd",
            sector = "Information Technology",
            currentPrice = 4120.00,
            changePercent = -0.35,
            dayHigh = 4155.00,
            dayLow = 4105.00,
            volume = 1200000,
            avgVolume20D = 1500000,
            relativeVolume = 0.80,
            deliveryPercent = 64.2,
            marketCapCr = 1490000.0,
            pe = 31.2,
            sectorPe = 30.5,
            pb = 13.8,
            roe = 49.5,
            roce = 61.2,
            debtToEquity = 0.05,
            salesGrowth3Y = 12.1,
            profitGrowth3Y = 10.8,
            rsi14 = 54.1,
            ema20 = 4110.0,
            ema50 = 4050.0,
            ema200 = 3820.0,
            vwap = 4125.00,
            atr14 = 55.0,
            isDemoData = true
        ),
        Stock(
            symbol = "HDFCBANK",
            companyName = "HDFC Bank Ltd",
            sector = "Private Banking",
            currentPrice = 1685.20,
            changePercent = 1.15,
            dayHigh = 1692.00,
            dayLow = 1668.50,
            volume = 18500000,
            avgVolume20D = 12000000,
            relativeVolume = 1.54,
            deliveryPercent = 61.5,
            marketCapCr = 1280000.0,
            pe = 19.4,
            sectorPe = 18.2,
            pb = 2.8,
            roe = 16.8,
            roce = 17.5,
            debtToEquity = 0.85,
            salesGrowth3Y = 24.5,
            profitGrowth3Y = 21.0,
            rsi14 = 64.8,
            ema20 = 1660.0,
            ema50 = 1615.0,
            ema200 = 1530.0,
            vwap = 1681.10,
            atr14 = 22.0,
            isDemoData = true
        ),
        Stock(
            symbol = "TATAMOTORS",
            companyName = "Tata Motors Ltd",
            sector = "Automobile",
            currentPrice = 965.40,
            changePercent = 2.65,
            dayHigh = 971.00,
            dayLow = 942.00,
            volume = 11200000,
            avgVolume20D = 5400000,
            relativeVolume = 2.07,
            deliveryPercent = 54.2,
            marketCapCr = 354000.0,
            pe = 16.5,
            sectorPe = 22.0,
            pb = 4.2,
            roe = 26.5,
            roce = 23.4,
            debtToEquity = 0.48,
            salesGrowth3Y = 28.5,
            profitGrowth3Y = 42.0,
            rsi14 = 68.2,
            ema20 = 940.0,
            ema50 = 912.0,
            ema200 = 820.0,
            vwap = 960.50,
            atr14 = 18.5,
            isDemoData = true
        ),
        Stock(
            symbol = "BHARTIARTL",
            companyName = "Bharti Airtel Ltd",
            sector = "Telecom",
            currentPrice = 1680.00,
            changePercent = 1.80,
            dayHigh = 1688.00,
            dayLow = 1655.00,
            volume = 4900000,
            avgVolume20D = 3100000,
            relativeVolume = 1.58,
            deliveryPercent = 67.8,
            marketCapCr = 998000.0,
            pe = 45.2,
            sectorPe = 42.0,
            pb = 8.5,
            roe = 19.2,
            roce = 18.8,
            debtToEquity = 1.20,
            salesGrowth3Y = 16.5,
            profitGrowth3Y = 32.0,
            rsi14 = 66.5,
            ema20 = 1640.0,
            ema50 = 1580.0,
            ema200 = 1390.0,
            vwap = 1674.00,
            atr14 = 26.0,
            isDemoData = true
        ),
        Stock(
            symbol = "INFY",
            companyName = "Infosys Ltd",
            sector = "Information Technology",
            currentPrice = 1890.50,
            changePercent = 0.85,
            dayHigh = 1905.00,
            dayLow = 1875.00,
            volume = 6100000,
            avgVolume20D = 5800000,
            relativeVolume = 1.05,
            deliveryPercent = 59.1,
            marketCapCr = 785000.0,
            pe = 29.5,
            sectorPe = 30.5,
            pb = 8.8,
            roe = 32.4,
            roce = 41.2,
            debtToEquity = 0.08,
            salesGrowth3Y = 11.4,
            profitGrowth3Y = 9.8,
            rsi14 = 58.7,
            ema20 = 1865.0,
            ema50 = 1820.0,
            ema200 = 1650.0,
            vwap = 1888.00,
            atr14 = 30.0,
            isDemoData = true
        ),
        Stock(
            symbol = "TRENT",
            companyName = "Trent Ltd (Westside & Zudio)",
            sector = "Retail / Consumption",
            currentPrice = 7240.00,
            changePercent = 3.45,
            dayHigh = 7290.00,
            dayLow = 7010.00,
            volume = 2400000,
            avgVolume20D = 1100000,
            relativeVolume = 2.18,
            deliveryPercent = 52.0,
            marketCapCr = 257000.0,
            pe = 142.0,
            sectorPe = 65.0,
            pb = 38.0,
            roe = 28.5,
            roce = 31.8,
            debtToEquity = 0.28,
            salesGrowth3Y = 48.0,
            profitGrowth3Y = 62.0,
            rsi14 = 71.5,
            ema20 = 6980.0,
            ema50 = 6450.0,
            ema200 = 5120.0,
            vwap = 7195.00,
            atr14 = 185.0,
            isDemoData = true
        ),
        Stock(
            symbol = "POLYCAB",
            companyName = "Polycab India Ltd",
            sector = "Cables & Electricals",
            currentPrice = 6850.00,
            changePercent = 2.10,
            dayHigh = 6890.00,
            dayLow = 6720.00,
            volume = 980000,
            avgVolume20D = 580000,
            relativeVolume = 1.69,
            deliveryPercent = 56.4,
            marketCapCr = 103000.0,
            pe = 52.5,
            sectorPe = 48.0,
            pb = 12.4,
            roe = 24.5,
            roce = 30.8,
            debtToEquity = 0.05,
            salesGrowth3Y = 22.0,
            profitGrowth3Y = 31.5,
            rsi14 = 64.2,
            ema20 = 6690.0,
            ema50 = 6420.0,
            ema200 = 5680.0,
            vwap = 6815.00,
            atr14 = 125.0,
            isDemoData = true
        ),
        Stock(
            symbol = "HAL",
            companyName = "Hindustan Aeronautics Ltd",
            sector = "Defence & Aerospace",
            currentPrice = 4620.00,
            changePercent = 1.95,
            dayHigh = 4660.00,
            dayLow = 4540.00,
            volume = 1900000,
            avgVolume20D = 1300000,
            relativeVolume = 1.46,
            deliveryPercent = 48.2,
            marketCapCr = 309000.0,
            pe = 38.4,
            sectorPe = 40.0,
            pb = 9.8,
            roe = 29.8,
            roce = 36.5,
            debtToEquity = 0.00,
            salesGrowth3Y = 14.5,
            profitGrowth3Y = 24.2,
            rsi14 = 63.8,
            ema20 = 4520.0,
            ema50 = 4380.0,
            ema200 = 3650.0,
            vwap = 4595.00,
            atr14 = 92.0,
            isDemoData = true
        ),
        Stock(
            symbol = "ITC",
            companyName = "ITC Ltd",
            sector = "FMCG / Diversified",
            currentPrice = 488.50,
            changePercent = 0.40,
            dayHigh = 492.00,
            dayLow = 485.50,
            volume = 14500000,
            avgVolume20D = 16000000,
            relativeVolume = 0.91,
            deliveryPercent = 68.5,
            marketCapCr = 610000.0,
            pe = 29.8,
            sectorPe = 42.0,
            pb = 8.5,
            roe = 29.2,
            roce = 38.6,
            debtToEquity = 0.01,
            salesGrowth3Y = 11.2,
            profitGrowth3Y = 13.5,
            rsi14 = 52.8,
            ema20 = 485.0,
            ema50 = 478.0,
            ema200 = 455.0,
            vwap = 487.60,
            atr14 = 6.5,
            isDemoData = true
        ),
        Stock(
            symbol = "VEDL",
            companyName = "Vedanta Ltd",
            sector = "Metals & Mining / Natural Resources",
            currentPrice = 492.80,
            changePercent = 1.85,
            dayHigh = 498.50,
            dayLow = 484.00,
            volume = 16800000,
            avgVolume20D = 10500000,
            relativeVolume = 1.60,
            deliveryPercent = 48.5,
            marketCapCr = 192000.0,
            pe = 14.8,
            sectorPe = 16.5,
            pb = 3.8,
            roe = 22.4,
            roce = 24.8,
            debtToEquity = 1.15,
            salesGrowth3Y = 16.2,
            profitGrowth3Y = 18.5,
            rsi14 = 61.5,
            ema20 = 482.0,
            ema50 = 465.0,
            ema200 = 410.0,
            vwap = 489.20,
            atr14 = 12.8,
            isDemoData = true
        ),
        Stock(
            symbol = "TATASTEEL",
            companyName = "Tata Steel Ltd",
            sector = "Metals & Mining",
            currentPrice = 162.40,
            changePercent = 1.35,
            dayHigh = 164.80,
            dayLow = 159.20,
            volume = 28500000,
            avgVolume20D = 22000000,
            relativeVolume = 1.30,
            deliveryPercent = 51.2,
            marketCapCr = 202000.0,
            pe = 38.5,
            sectorPe = 16.5,
            pb = 2.1,
            roe = 5.8,
            roce = 9.2,
            debtToEquity = 0.82,
            salesGrowth3Y = 14.0,
            profitGrowth3Y = 8.5,
            rsi14 = 59.4,
            ema20 = 158.0,
            ema50 = 152.0,
            ema200 = 140.0,
            vwap = 161.50,
            atr14 = 4.2,
            isDemoData = true
        ),
        Stock(
            symbol = "SBIN",
            companyName = "State Bank of India",
            sector = "Public Sector Banking",
            currentPrice = 825.60,
            changePercent = 0.95,
            dayHigh = 832.00,
            dayLow = 818.00,
            volume = 14200000,
            avgVolume20D = 12000000,
            relativeVolume = 1.18,
            deliveryPercent = 46.5,
            marketCapCr = 736000.0,
            pe = 11.2,
            sectorPe = 12.0,
            pb = 1.8,
            roe = 17.2,
            roce = 18.0,
            debtToEquity = 1.40,
            salesGrowth3Y = 19.5,
            profitGrowth3Y = 28.0,
            rsi14 = 57.8,
            ema20 = 815.0,
            ema50 = 795.0,
            ema200 = 740.0,
            vwap = 822.40,
            atr14 = 14.5,
            isDemoData = true
        ),
        Stock(
            symbol = "BAJFINANCE",
            companyName = "Bajaj Finance Ltd",
            sector = "Financial Services / NBFC",
            currentPrice = 7150.00,
            changePercent = 1.60,
            dayHigh = 7210.00,
            dayLow = 7020.00,
            volume = 1250000,
            avgVolume20D = 950000,
            relativeVolume = 1.32,
            deliveryPercent = 58.0,
            marketCapCr = 442000.0,
            pe = 29.8,
            sectorPe = 24.0,
            pb = 5.2,
            roe = 21.8,
            roce = 22.4,
            debtToEquity = 3.20,
            salesGrowth3Y = 26.5,
            profitGrowth3Y = 28.5,
            rsi14 = 63.2,
            ema20 = 7020.0,
            ema50 = 6880.0,
            ema200 = 6750.0,
            vwap = 7110.00,
            atr14 = 140.0,
            isDemoData = true
        )
    )

    override suspend fun getWatchlist(): Result<List<Stock>> {
        return Result.success(demoStocks)
    }

    override suspend fun getStockDetails(symbol: String): Result<Stock> {
        val found = demoStocks.find { it.symbol.equals(symbol, ignoreCase = true) }
        return if (found != null) {
            Result.success(found)
        } else {
            Result.failure(IllegalArgumentException("Stock $symbol not found in Demo universe"))
        }
    }

    override suspend fun getMarketBreadth(): Result<MarketBreadthSummary> {
        return Result.success(
            MarketBreadthSummary(
                nifty50Price = 25145.20,
                nifty50ChangePercent = 0.72,
                bankNiftyPrice = 52410.80,
                bankNiftyChangePercent = 0.88,
                indiaVix = 12.85,
                advancesCount = 34,
                declinesCount = 16,
                marketSentiment = "MODERATE BULLISH (Broad-based institutional participation)"
            )
        )
    }

    override fun getProviderType(): MarketDataProviderType = MarketDataProviderType.DEMO_DATA

    override fun getConfigurationStatus(): ProviderStatus = ProviderStatus.ACTIVE_DEMO
}

/**
 * Supported Indian Broker API Providers.
 */
enum class BrokerType(val displayName: String, val defaultBaseUrl: String, val accountLabel: String) {
    ANGEL_ONE("Angel One (SmartAPI)", "https://apiconnect.angelone.in", "Angel One Client Code (Account ID)"),
    ZERODHA_KITE("Zerodha (Kite Connect)", "https://api.kite.trade", "Kite User ID (e.g. AB1234)"),
    CUSTOM_REST("Custom REST Gateway", "https://api.marketjournal.ai", "Client / Account ID")
}

/**
 * Complete credentials structure for Indian Broker APIs including Angel One SmartAPI and Zerodha.
 */
data class BrokerApiConfig(
    val brokerType: BrokerType = BrokerType.ANGEL_ONE,
    val clientCode: String = "", // Angel One Client ID / Zerodha User ID
    val apiKey: String = "", // SmartAPI Key / Kite API Key
    val passwordOrMpin: String = "", // MPIN / Password
    val totpSecret: String = "", // 6-digit TOTP or base32 authenticator secret
    val sessionOrJwtToken: String = "", // JWT / Feed Token / Access Token
    val baseUrl: String = BrokerType.ANGEL_ONE.defaultBaseUrl
)

/**
 * Live Market Data Provider Interface contract.
 * Does NOT invent live prices or fake responses if unconfigured.
 * Strictly verifies API configuration and reports exact missing parameters.
 */
class LiveRestMarketDataProvider(
    private var config: BrokerApiConfig = BrokerApiConfig()
) : MarketDataProvider {

    fun updateConfig(newConfig: BrokerApiConfig) {
        this.config = newConfig
    }

    fun getConfig(): BrokerApiConfig = config

    fun updateConfig(baseUrl: String, apiKey: String, accessToken: String) {
        this.config = this.config.copy(
            baseUrl = baseUrl.trim(),
            apiKey = apiKey.trim(),
            sessionOrJwtToken = accessToken.trim()
        )
    }

    fun isConfigured(): Boolean {
        return when (config.brokerType) {
            BrokerType.ANGEL_ONE -> {
                config.clientCode.isNotBlank() &&
                config.apiKey.isNotBlank() &&
                (config.sessionOrJwtToken.isNotBlank() || (config.passwordOrMpin.isNotBlank() && config.totpSecret.isNotBlank()))
            }
            BrokerType.ZERODHA_KITE -> {
                config.clientCode.isNotBlank() && config.apiKey.isNotBlank() && config.sessionOrJwtToken.isNotBlank()
            }
            BrokerType.CUSTOM_REST -> {
                config.baseUrl.isNotBlank() && config.apiKey.isNotBlank()
            }
        }
    }

    fun getMissingParametersList(): List<String> {
        val missing = mutableListOf<String>()
        when (config.brokerType) {
            BrokerType.ANGEL_ONE -> {
                if (config.clientCode.isBlank()) missing.add("Angel One Client Code (Account ID)")
                if (config.apiKey.isBlank()) missing.add("SmartAPI Key (from smartapi.angelbroking.com)")
                if (config.sessionOrJwtToken.isBlank() && config.passwordOrMpin.isBlank()) missing.add("MPIN / Password or Session JWT Token")
                if (config.sessionOrJwtToken.isBlank() && config.totpSecret.isBlank()) missing.add("TOTP Secret / Live Authenticator code")
            }
            BrokerType.ZERODHA_KITE -> {
                if (config.clientCode.isBlank()) missing.add("Kite User ID")
                if (config.apiKey.isBlank()) missing.add("Kite Connect API Key")
                if (config.sessionOrJwtToken.isBlank()) missing.add("Access Token / Enctoken")
            }
            BrokerType.CUSTOM_REST -> {
                if (config.baseUrl.isBlank()) missing.add("Gateway Base URL")
                if (config.apiKey.isBlank()) missing.add("API Secret / Token")
            }
        }
        return missing
    }

    override suspend fun getWatchlist(): Result<List<Stock>> {
        if (!isConfigured()) {
            val missing = getMissingParametersList().joinToString(", ")
            return Result.failure(
                IllegalStateException(
                    "Live Market Provider (${config.brokerType.displayName}) is unconfigured. Missing: $missing. " +
                    "Please fill in your credentials in the Config tab."
                )
            )
        }
        return Result.failure(
            IllegalStateException("Live connection to ${config.brokerType.displayName} at '${config.baseUrl}' requires active market hours session token handshake.")
        )
    }

    override suspend fun getStockDetails(symbol: String): Result<Stock> {
        if (!isConfigured()) {
            return Result.failure(IllegalStateException("Live Market Provider is unconfigured."))
        }
        return Result.failure(IllegalStateException("Live quote for $symbol unavailable without active session."))
    }

    override suspend fun getMarketBreadth(): Result<MarketBreadthSummary> {
        if (!isConfigured()) {
            return Result.failure(IllegalStateException("Live Market Breadth is unconfigured."))
        }
        return Result.failure(IllegalStateException("Live Market Breadth requires active session."))
    }

    override fun getProviderType(): MarketDataProviderType = MarketDataProviderType.LIVE_REST_API

    override fun getConfigurationStatus(): ProviderStatus {
        return if (isConfigured()) {
            ProviderStatus.LIVE_CONNECTED
        } else {
            val missing = getMissingParametersList()
            if (missing.isNotEmpty()) {
                ProviderStatus.LIVE_UNCONFIGURED
            } else {
                ProviderStatus.LIVE_UNCONFIGURED
            }
        }
    }
}

/**
 * Coordinates between active data providers and exposes state to UI.
 */
class MarketDataProviderManager(
    private val demoProvider: DemoMarketDataProvider = DemoMarketDataProvider(),
    private val liveProvider: LiveRestMarketDataProvider = LiveRestMarketDataProvider()
) {
    private val _currentProviderType = MutableStateFlow(MarketDataProviderType.DEMO_DATA)
    val currentProviderType: StateFlow<MarketDataProviderType> = _currentProviderType.asStateFlow()

    fun getActiveProvider(): MarketDataProvider {
        return when (_currentProviderType.value) {
            MarketDataProviderType.DEMO_DATA -> demoProvider
            MarketDataProviderType.LIVE_REST_API -> liveProvider
        }
    }

    fun setProviderType(type: MarketDataProviderType) {
        _currentProviderType.value = type
    }

    fun getLiveProvider(): LiveRestMarketDataProvider = liveProvider
    fun getDemoProvider(): DemoMarketDataProvider = demoProvider
}
