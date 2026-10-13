package com.example.marketjournal.data.repository

import com.example.marketjournal.calendar.IndianTradingCalendar
import com.example.marketjournal.data.local.DailyReportEntity
import com.example.marketjournal.data.local.JobExecutionLogEntity
import com.example.marketjournal.data.local.MarketJournalDao
import com.example.marketjournal.data.local.TrackedPositionEntity
import com.example.marketjournal.data.provider.MarketDataProvider
import com.example.marketjournal.data.provider.MarketDataProviderManager
import com.example.marketjournal.engine.DeterministicStockScoringEngine
import com.example.marketjournal.model.DailyReport
import com.example.marketjournal.model.JobExecutionLog
import com.example.marketjournal.model.JobExecutionStatus
import com.example.marketjournal.model.JournalPerformanceSnapshot
import com.example.marketjournal.model.MarketBreadthSummary
import com.example.marketjournal.model.MarketDataProviderType
import com.example.marketjournal.model.PositionStatus
import com.example.marketjournal.model.ProviderStatus
import com.example.marketjournal.model.ScannerStrategy
import com.example.marketjournal.model.ScoredStock
import com.example.marketjournal.model.Stock
import com.example.marketjournal.model.TrackedPosition
import com.example.marketjournal.scheduler.ReportSchedulerEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

class MarketJournalRepository(
    private val dao: MarketJournalDao,
    val providerManager: MarketDataProviderManager
) {
    private val schedulerEngine = ReportSchedulerEngine(dao, providerManager.getActiveProvider())

    // --- Active Stocks & Scored Watchlists ---
    suspend fun getWatchlistStocks(): Result<List<Stock>> {
        return providerManager.getActiveProvider().getWatchlist()
    }

    suspend fun getMarketBreadth(): Result<MarketBreadthSummary> {
        return providerManager.getActiveProvider().getMarketBreadth()
    }

    suspend fun scanStocks(strategy: ScannerStrategy): Result<List<ScoredStock>> {
        val watchlistResult = getWatchlistStocks()
        return watchlistResult.map { stocks ->
            DeterministicStockScoringEngine.rankWatchlist(stocks, strategy)
        }
    }

    // --- Tracked Positions (Journal / Paper Trading) ---
    val allPositions: Flow<List<TrackedPosition>> = dao.getAllPositions().map { list ->
        list.map { it.toDomain() }
    }

    val activePositions: Flow<List<TrackedPosition>> = dao.getActivePositions().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun addPositionFromScored(
        scored: ScoredStock,
        quantity: Int = 10,
        notes: String = ""
    ): Long {
        val dateStr = IndianTradingCalendar.currentIstDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val entity = TrackedPositionEntity(
            symbol = scored.stock.symbol,
            stockName = scored.stock.companyName,
            strategy = scored.strategy.name,
            entryDate = dateStr,
            entryPrice = scored.entryPrice,
            quantity = quantity,
            currentPrice = scored.stock.currentPrice,
            stopLoss = scored.stopLoss,
            target1 = scored.target1,
            target2 = scored.target2,
            status = PositionStatus.ACTIVE.name,
            pnlAmount = 0.0,
            pnlPercent = 0.0,
            notes = if (notes.isBlank()) "Automated entry via ${scored.strategy.displayName} (Score: ${scored.compositeScore}/100)" else notes,
            createdAt = System.currentTimeMillis()
        )
        return dao.insertPosition(entity)
    }

    suspend fun addCustomPosition(
        symbol: String,
        stockName: String,
        strategy: ScannerStrategy,
        entryPrice: Double,
        quantity: Int,
        stopLoss: Double,
        target1: Double,
        target2: Double,
        notes: String
    ): Long {
        val dateStr = IndianTradingCalendar.currentIstDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val entity = TrackedPositionEntity(
            symbol = symbol.uppercase(),
            stockName = stockName,
            strategy = strategy.name,
            entryDate = dateStr,
            entryPrice = entryPrice,
            quantity = quantity,
            currentPrice = entryPrice,
            stopLoss = stopLoss,
            target1 = target1,
            target2 = target2,
            status = PositionStatus.ACTIVE.name,
            pnlAmount = 0.0,
            pnlPercent = 0.0,
            notes = notes,
            createdAt = System.currentTimeMillis()
        )
        return dao.insertPosition(entity)
    }

    suspend fun updatePositionStatus(id: Long, newStatus: PositionStatus, exitPrice: Double? = null) {
        val existing = dao.getPositionById(id) ?: return
        val effectiveExit = exitPrice ?: existing.currentPrice
        val pnl = (effectiveExit - existing.entryPrice) * existing.quantity
        val pnlPct = ((effectiveExit - existing.entryPrice) / existing.entryPrice) * 100.0
        val dateStr = IndianTradingCalendar.currentIstDate().format(DateTimeFormatter.ISO_LOCAL_DATE)

        dao.updatePosition(
            existing.copy(
                status = newStatus.name,
                exitPrice = effectiveExit,
                exitDate = dateStr,
                pnlAmount = roundToTwoDecimals(pnl),
                pnlPercent = roundToTwoDecimals(pnlPct)
            )
        )
    }

    suspend fun deletePosition(id: Long) {
        dao.deletePositionById(id)
    }

    suspend fun refreshPositionsWithLatestPrices() {
        val stocks = getWatchlistStocks().getOrNull() ?: return
        val currentEntities = dao.getAllPositions().first()
        currentEntities.forEach { pos ->
            val matchingStock = stocks.find { it.symbol.equals(pos.symbol, ignoreCase = true) }
            if (matchingStock != null && pos.status == PositionStatus.ACTIVE.name) {
                val newPrice = matchingStock.currentPrice
                val pnl = (newPrice - pos.entryPrice) * pos.quantity
                val pnlPct = ((newPrice - pos.entryPrice) / pos.entryPrice) * 100.0

                var autoStatus = pos.status
                if (newPrice >= pos.target2) {
                    autoStatus = PositionStatus.TARGET_2_HIT.name
                } else if (newPrice >= pos.target1) {
                    autoStatus = PositionStatus.TARGET_1_HIT.name
                } else if (newPrice <= pos.stopLoss) {
                    autoStatus = PositionStatus.STOP_LOSS_HIT.name
                }

                dao.updatePosition(
                    pos.copy(
                        currentPrice = newPrice,
                        status = autoStatus,
                        pnlAmount = roundToTwoDecimals(pnl),
                        pnlPercent = roundToTwoDecimals(pnlPct)
                    )
                )
            }
        }
    }

    // --- Reports ---
    val allReports: Flow<List<DailyReportEntity>> = dao.getAllReports()

    suspend fun getReportByDate(date: String): DailyReportEntity? = dao.getReportByDate(date)

    suspend fun runScheduledEodReport(
        date: LocalDate = IndianTradingCalendar.currentIstDate(),
        force: Boolean = false
    ): JobExecutionLog {
        val engine = ReportSchedulerEngine(dao, providerManager.getActiveProvider())
        return engine.executeDailyEodJob(date, force)
    }

    // --- Audit Logs ---
    val allJobLogs: Flow<List<JobExecutionLogEntity>> = dao.getAllJobLogs()

    // --- Seed Demo Positions if Database is Empty ---
    suspend fun seedInitialJournalIfEmpty() {
        val existing = dao.getAllPositions().first()
        if (existing.isEmpty()) {
            val samplePositions = listOf(
                TrackedPositionEntity(
                    symbol = "TATAMOTORS",
                    stockName = "Tata Motors Ltd",
                    strategy = ScannerStrategy.INTRADAY.name,
                    entryDate = "2026-10-10",
                    entryPrice = 945.0,
                    quantity = 25,
                    currentPrice = 965.4,
                    stopLoss = 932.0,
                    target1 = 960.0,
                    target2 = 975.0,
                    status = PositionStatus.TARGET_1_HIT.name,
                    exitPrice = 962.0,
                    exitDate = "2026-10-10",
                    pnlAmount = 425.0,
                    pnlPercent = 1.8,
                    notes = "Opening Range Breakout with 2.1x volume confirmation.",
                    createdAt = System.currentTimeMillis() - 86400000L * 2
                ),
                TrackedPositionEntity(
                    symbol = "BHARTIARTL",
                    stockName = "Bharti Airtel Ltd",
                    strategy = ScannerStrategy.SWING.name,
                    entryDate = "2026-10-08",
                    entryPrice = 1630.0,
                    quantity = 15,
                    currentPrice = 1680.0,
                    stopLoss = 1605.0,
                    target1 = 1675.0,
                    target2 = 1720.0,
                    status = PositionStatus.ACTIVE.name,
                    pnlAmount = 750.0,
                    pnlPercent = 3.07,
                    notes = "20 EMA bounce with 68% delivery accumulation.",
                    createdAt = System.currentTimeMillis() - 86400000L * 4
                ),
                TrackedPositionEntity(
                    symbol = "TRENT",
                    stockName = "Trent Ltd",
                    strategy = ScannerStrategy.SWING.name,
                    entryDate = "2026-10-05",
                    entryPrice = 6920.0,
                    quantity = 5,
                    currentPrice = 7240.0,
                    stopLoss = 6750.0,
                    target1 = 7180.0,
                    target2 = 7450.0,
                    status = PositionStatus.TARGET_1_HIT.name,
                    pnlAmount = 1600.0,
                    pnlPercent = 4.62,
                    notes = "Zudio expansion momentum + all-time high consolidation breakout.",
                    createdAt = System.currentTimeMillis() - 86400000L * 7
                ),
                TrackedPositionEntity(
                    symbol = "POLYCAB",
                    stockName = "Polycab India Ltd",
                    strategy = ScannerStrategy.LONG_TERM_FUNDAMENTAL.name,
                    entryDate = "2026-09-25",
                    entryPrice = 6500.0,
                    quantity = 8,
                    currentPrice = 6850.0,
                    stopLoss = 6150.0,
                    target1 = 7200.0,
                    target2 = 8000.0,
                    status = PositionStatus.ACTIVE.name,
                    pnlAmount = 2800.0,
                    pnlPercent = 5.38,
                    notes = "31% ROCE + clean balance sheet compounder.",
                    createdAt = System.currentTimeMillis() - 86400000L * 17
                )
            )
            samplePositions.forEach { dao.insertPosition(it) }

            // Also seed one EOD report so the user sees immediate value in the Reports tab
            val latestTradingDay = IndianTradingCalendar.getLatestTradingDay()
            runScheduledEodReport(latestTradingDay, force = true)
        }
    }

    private fun roundToTwoDecimals(value: Double): Double {
        return (value * 100.0).roundToInt() / 100.0
    }

    private fun TrackedPositionEntity.toDomain(): TrackedPosition {
        val strat = try {
            ScannerStrategy.valueOf(strategy)
        } catch (e: Exception) {
            ScannerStrategy.INTRADAY
        }
        val stat = try {
            PositionStatus.valueOf(status)
        } catch (e: Exception) {
            PositionStatus.ACTIVE
        }
        return TrackedPosition(
            id = id,
            symbol = symbol,
            stockName = stockName,
            strategy = strat,
            entryDate = entryDate,
            entryPrice = entryPrice,
            quantity = quantity,
            currentPrice = currentPrice,
            stopLoss = stopLoss,
            target1 = target1,
            target2 = target2,
            status = stat,
            exitPrice = exitPrice,
            exitDate = exitDate,
            pnlAmount = pnlAmount,
            pnlPercent = pnlPercent,
            notes = notes,
            createdAt = createdAt
        )
    }
}
