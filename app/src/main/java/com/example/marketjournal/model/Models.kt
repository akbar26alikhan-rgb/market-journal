package com.example.marketjournal.model

/**
 * Strategy types supported by the deterministic scanning & scoring engine.
 */
enum class ScannerStrategy(val displayName: String, val holdingHorizon: String) {
    INTRADAY("Intraday Momentum", "Same Day (Close by 15:15 IST)"),
    SWING("Swing Trend Pullback", "3 - 15 Trading Days"),
    LONG_TERM_FUNDAMENTAL("Fundamental Compounder", "6 - 24 Months")
}

/**
 * Trade recommendation tier generated from composite score.
 */
enum class Recommendation(val label: String) {
    STRONG_BUY("Strong Buy"),
    BUY("Buy"),
    NEUTRAL("Neutral / Watch"),
    AVOID("Avoid")
}

/**
 * Position status for stock-selection journal tracking.
 */
enum class PositionStatus(val label: String) {
    ACTIVE("Active"),
    TARGET_1_HIT("Target 1 Hit"),
    TARGET_2_HIT("Target 2 Hit"),
    STOP_LOSS_HIT("Stop Loss Hit"),
    CLOSED("Closed Manually")
}

/**
 * Market data provider execution mode.
 */
enum class MarketDataProviderType {
    DEMO_DATA,
    LIVE_REST_API
}

/**
 * Status of the market data provider connection.
 */
enum class ProviderStatus(val isOperational: Boolean, val message: String) {
    ACTIVE_DEMO(true, "Demo Data Mode (Simulated NSE Universe with clear labels)"),
    LIVE_CONNECTED(true, "Live NSE/BSE Rest API Connected"),
    LIVE_UNCONFIGURED(false, "Live Market Provider Unconfigured (Missing Endpoint/API Key)"),
    CONNECTION_ERROR(false, "Connection Error to Live Market Endpoint")
}

/**
 * Scheduled job execution status for the audit trail.
 */
enum class JobExecutionStatus(val label: String) {
    SUCCESS("Success"),
    SKIPPED_WEEKEND("Skipped (Weekend)"),
    SKIPPED_HOLIDAY("Skipped (NSE Holiday)"),
    ALREADY_RUN_SKIPPED("Skipped (Idempotent: Already Generated)"),
    FAILED("Failed")
}

/**
 * Core Stock entity with technical and fundamental indicators.
 */
data class Stock(
    val symbol: String,
    val companyName: String,
    val exchange: String = "NSE",
    val sector: String,
    val currentPrice: Double,
    val changePercent: Double,
    val dayHigh: Double,
    val dayLow: Double,
    val volume: Long,
    val avgVolume20D: Long,
    val relativeVolume: Double, // RVOL: volume / avgVolume20D
    val deliveryPercent: Double, // Institutional delivery accumulation %
    val marketCapCr: Double, // Market Cap in INR Crores
    val pe: Double,
    val sectorPe: Double,
    val pb: Double,
    val roe: Double, // Return on Equity %
    val roce: Double, // Return on Capital Employed %
    val debtToEquity: Double,
    val salesGrowth3Y: Double, // 3-year CAGR sales growth %
    val profitGrowth3Y: Double, // 3-year CAGR profit growth %
    val rsi14: Double,
    val ema20: Double,
    val ema50: Double,
    val ema200: Double,
    val vwap: Double,
    val atr14: Double,
    val isDemoData: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Detailed scoring breakdown across deterministic dimensions.
 */
data class ScoreFactor(
    val category: String,
    val pointsAwarded: Double,
    val maxPoints: Double,
    val description: String
)

/**
 * Evaluated stock with deterministic score and clear trade plan.
 */
data class ScoredStock(
    val stock: Stock,
    val strategy: ScannerStrategy,
    val compositeScore: Int, // 0 - 100
    val momentumScore: Double,
    val volumeScore: Double,
    val trendScore: Double,
    val fundamentalScore: Double,
    val riskRewardScore: Double,
    val recommendation: Recommendation,
    val entryPrice: Double,
    val stopLoss: Double,
    val target1: Double,
    val target2: Double,
    val riskRewardRatio: Double,
    val factors: List<ScoreFactor>,
    val scoredAt: Long = System.currentTimeMillis()
)

/**
 * Stock position tracked in user's journal / paper trading ledger.
 */
data class TrackedPosition(
    val id: Long = 0,
    val symbol: String,
    val stockName: String,
    val strategy: ScannerStrategy,
    val entryDate: String, // YYYY-MM-DD
    val entryPrice: Double,
    val quantity: Int,
    val currentPrice: Double,
    val stopLoss: Double,
    val target1: Double,
    val target2: Double,
    val status: PositionStatus = PositionStatus.ACTIVE,
    val exitPrice: Double? = null,
    val exitDate: String? = null,
    val pnlAmount: Double = 0.0,
    val pnlPercent: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Summary of a scored stock for reports.
 */
data class ScoredStockSummary(
    val symbol: String,
    val companyName: String,
    val currentPrice: Double,
    val compositeScore: Int,
    val recommendation: String,
    val entryPrice: Double,
    val stopLoss: Double,
    val target1: Double,
    val riskReward: String,
    val keyHighlight: String
)

/**
 * Snapshot of overall journal / paper portfolio performance.
 */
data class JournalPerformanceSnapshot(
    val totalTrades: Int,
    val activeTrades: Int,
    val closedTrades: Int,
    val winningTrades: Int,
    val winRatePercent: Double,
    val totalInvested: Double,
    val currentPortfolioValue: Double,
    val totalRealizedPnl: Double,
    val totalUnrealizedPnl: Double,
    val netPnlPercent: Double
)

/**
 * Indian Market breadth summary.
 */
data class MarketBreadthSummary(
    val nifty50Price: Double,
    val nifty50ChangePercent: Double,
    val bankNiftyPrice: Double,
    val bankNiftyChangePercent: Double,
    val indiaVix: Double,
    val advancesCount: Int,
    val declinesCount: Int,
    val marketSentiment: String
)

/**
 * Daily End-of-Day (EOD) Market Journal Report.
 */
data class DailyReport(
    val id: Long = 0,
    val reportDate: String, // YYYY-MM-DD (idempotent key)
    val title: String,
    val marketSummary: MarketBreadthSummary,
    val topIntradayPicks: List<ScoredStockSummary>,
    val topSwingPicks: List<ScoredStockSummary>,
    val topFundamentalPicks: List<ScoredStockSummary>,
    val journalSnapshot: JournalPerformanceSnapshot,
    val fullMarkdownText: String,
    val generatedAt: Long = System.currentTimeMillis(),
    val idempotentKey: String
)

/**
 * Scheduled job execution log entity for auditability.
 */
data class JobExecutionLog(
    val id: Long = 0,
    val jobName: String,
    val executionDate: String, // YYYY-MM-DD
    val status: JobExecutionStatus,
    val startedAt: Long,
    val finishedAt: Long,
    val durationMs: Long,
    val message: String,
    val errorLog: String? = null,
    val isIdempotentSkip: Boolean = false
)
