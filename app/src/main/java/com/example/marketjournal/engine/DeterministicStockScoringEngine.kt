package com.example.marketjournal.engine

import com.example.marketjournal.model.Recommendation
import com.example.marketjournal.model.ScannerStrategy
import com.example.marketjournal.model.ScoreFactor
import com.example.marketjournal.model.ScoredStock
import com.example.marketjournal.model.Stock
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Deterministic mathematical stock scoring engine.
 * Fully transparent, rule-based, and auditable without arbitrary or hallucinated scores.
 */
object DeterministicStockScoringEngine {

    /**
     * Scores a given stock according to the selected strategy.
     */
    fun score(stock: Stock, strategy: ScannerStrategy): ScoredStock {
        return when (strategy) {
            ScannerStrategy.INTRADAY -> scoreIntraday(stock)
            ScannerStrategy.SWING -> scoreSwing(stock)
            ScannerStrategy.LONG_TERM_FUNDAMENTAL -> scoreFundamental(stock)
        }
    }

    /**
     * Scores an entire list of stocks and sorts them by score descending.
     */
    fun rankWatchlist(stocks: List<Stock>, strategy: ScannerStrategy): List<ScoredStock> {
        return stocks.map { score(it, strategy) }
            .sortedByDescending { it.compositeScore }
    }

    private fun scoreIntraday(stock: Stock): ScoredStock {
        val factors = mutableListOf<ScoreFactor>()

        // 1. Momentum & RSI-14 (Max 25 pts)
        var momentumScore = 0.0
        when {
            stock.rsi14 in 56.0..72.0 -> {
                momentumScore += 16.0
                factors.add(ScoreFactor("Momentum", 16.0, 16.0, "RSI-14 (${stock.rsi14}) is in sweet spot (56-72) showing fresh bullish expansion."))
            }
            stock.rsi14 in 50.0..55.9 -> {
                momentumScore += 10.0
                factors.add(ScoreFactor("Momentum", 10.0, 16.0, "RSI-14 (${stock.rsi14}) is bullish above centerline (50-55)."))
            }
            stock.rsi14 > 72.0 -> {
                momentumScore += 8.0
                factors.add(ScoreFactor("Momentum", 8.0, 16.0, "RSI-14 (${stock.rsi14}) is elevated (>72); potential exhaustion risk."))
            }
            else -> {
                momentumScore += 2.0
                factors.add(ScoreFactor("Momentum", 2.0, 16.0, "RSI-14 (${stock.rsi14}) lacks strong bullish momentum."))
            }
        }

        val distanceFromHighPct = if (stock.dayHigh > 0) ((stock.dayHigh - stock.currentPrice) / stock.dayHigh) * 100 else 10.0
        if (distanceFromHighPct <= 0.8) {
            momentumScore += 9.0
            factors.add(ScoreFactor("Breakout Proximity", 9.0, 9.0, "Trading within 0.8% of Day High (High: ₹${stock.dayHigh}). Clean breakout zone."))
        } else if (distanceFromHighPct <= 1.8) {
            momentumScore += 5.0
            factors.add(ScoreFactor("Breakout Proximity", 5.0, 9.0, "Within 1.8% of Day High."))
        } else {
            factors.add(ScoreFactor("Breakout Proximity", 1.0, 9.0, "Trading ${String.format("%.1f", distanceFromHighPct)}% below Day High."))
        }

        // 2. Relative Volume & Participation (Max 25 pts)
        var volumeScore = 0.0
        when {
            stock.relativeVolume >= 2.0 -> {
                volumeScore += 25.0
                factors.add(ScoreFactor("Volume Confirmation", 25.0, 25.0, "RVOL is ${String.format("%.2f", stock.relativeVolume)}x (>2.0x). Massive institutional volume surge."))
            }
            stock.relativeVolume >= 1.4 -> {
                volumeScore += 18.0
                factors.add(ScoreFactor("Volume Confirmation", 18.0, 25.0, "RVOL is ${String.format("%.2f", stock.relativeVolume)}x (1.4-2.0x). Good volume participation."))
            }
            stock.relativeVolume >= 1.0 -> {
                volumeScore += 10.0
                factors.add(ScoreFactor("Volume Confirmation", 10.0, 25.0, "RVOL is ${String.format("%.2f", stock.relativeVolume)}x. At or slightly above 20D average."))
            }
            else -> {
                volumeScore += 2.0
                factors.add(ScoreFactor("Volume Confirmation", 2.0, 25.0, "RVOL is below 1.0x (${String.format("%.2f", stock.relativeVolume)}x). Weak volume interest."))
            }
        }

        // 3. Intraday Trend & VWAP (Max 25 pts)
        var trendScore = 0.0
        if (stock.currentPrice > stock.vwap) {
            trendScore += 15.0
            factors.add(ScoreFactor("VWAP Alignment", 15.0, 15.0, "Price (₹${stock.currentPrice}) above VWAP (₹${stock.vwap}). Buyers controlling intraday benchmark."))
        } else {
            factors.add(ScoreFactor("VWAP Alignment", 0.0, 15.0, "Price is below VWAP (₹${stock.vwap}). Selling pressure present."))
        }

        if (stock.currentPrice > stock.ema20) {
            trendScore += 10.0
            factors.add(ScoreFactor("EMA20 Trend", 10.0, 10.0, "Price holding firmly above 20 EMA."))
        } else {
            factors.add(ScoreFactor("EMA20 Trend", 2.0, 10.0, "Price trading under short-term 20 EMA."))
        }

        // 4. Fundamental & Quality gate (Max 10 pts)
        var fundamentalScore = 0.0
        if (stock.roce >= 15.0) {
            fundamentalScore += 5.0
            factors.add(ScoreFactor("Quality Base", 5.0, 5.0, "Solid ROCE (${stock.roce}%) ensures liquid institutional participation."))
        }
        if (stock.debtToEquity <= 0.8) {
            fundamentalScore += 5.0
            factors.add(ScoreFactor("Balance Sheet", 5.0, 5.0, "Manageable Debt/Equity (${stock.debtToEquity})."))
        }

        // 5. Risk / Reward Structure (Max 15 pts)
        val atr = max(stock.atr14, stock.currentPrice * 0.008)
        val entry = stock.currentPrice
        val stopLoss = roundToTwoDecimals(entry - (1.0 * atr))
        val target1 = roundToTwoDecimals(entry + (1.5 * atr))
        val target2 = roundToTwoDecimals(entry + (2.5 * atr))
        val risk = entry - stopLoss
        val reward = target1 - entry
        val rrRatio = if (risk > 0) roundToTwoDecimals(reward / risk) else 1.0

        var riskRewardScore = 0.0
        if (rrRatio >= 1.5) {
            riskRewardScore += 15.0
            factors.add(ScoreFactor("Risk/Reward", 15.0, 15.0, "Risk:Reward is 1:${rrRatio} with defined SL at ₹$stopLoss and T1 at ₹$target1."))
        } else {
            riskRewardScore += 8.0
            factors.add(ScoreFactor("Risk/Reward", 8.0, 15.0, "Risk:Reward is 1:${rrRatio}."))
        }

        val totalPoints = momentumScore + volumeScore + trendScore + fundamentalScore + riskRewardScore
        val composite = min(100, max(0, totalPoints.roundToInt()))
        val recommendation = tierRecommendation(composite)

        return ScoredStock(
            stock = stock,
            strategy = ScannerStrategy.INTRADAY,
            compositeScore = composite,
            momentumScore = momentumScore,
            volumeScore = volumeScore,
            trendScore = trendScore,
            fundamentalScore = fundamentalScore,
            riskRewardScore = riskRewardScore,
            recommendation = recommendation,
            entryPrice = entry,
            stopLoss = stopLoss,
            target1 = target1,
            target2 = target2,
            riskRewardRatio = rrRatio,
            factors = factors
        )
    }

    private fun scoreSwing(stock: Stock): ScoredStock {
        val factors = mutableListOf<ScoreFactor>()

        // 1. Moving Average Alignment (Max 30 pts)
        var trendScore = 0.0
        val isBullishStack = stock.ema20 > stock.ema50 && stock.ema50 > stock.ema200
        if (isBullishStack && stock.currentPrice >= stock.ema20) {
            trendScore += 25.0
            factors.add(ScoreFactor("Trend Alignment", 25.0, 25.0, "Perfect Stage-2 moving average alignment: EMA20 > EMA50 > EMA200."))
        } else if (stock.ema20 > stock.ema50) {
            trendScore += 16.0
            factors.add(ScoreFactor("Trend Alignment", 16.0, 25.0, "Short-term bullish cross (EMA20 > EMA50)."))
        } else {
            trendScore += 4.0
            factors.add(ScoreFactor("Trend Alignment", 4.0, 25.0, "Mixed or lagging moving average structure."))
        }

        // Pullback near 20 EMA
        val diffFromEma20Pct = kotlin.math.abs((stock.currentPrice - stock.ema20) / stock.ema20) * 100
        if (diffFromEma20Pct <= 3.0 && stock.currentPrice >= stock.ema20 * 0.98) {
            trendScore += 5.0
            factors.add(ScoreFactor("Pullback Entry", 5.0, 5.0, "Trading within 3% of 20 EMA. Favorable swing entry cushion."))
        }

        // 2. Volume & Delivery Accumulation (Max 25 pts)
        var volumeScore = 0.0
        if (stock.deliveryPercent >= 55.0) {
            volumeScore += 18.0
            factors.add(ScoreFactor("Delivery Accumulation", 18.0, 18.0, "High Delivery % (${stock.deliveryPercent}%). Institutional hands taking delivery."))
        } else if (stock.deliveryPercent >= 40.0) {
            volumeScore += 12.0
            factors.add(ScoreFactor("Delivery Accumulation", 12.0, 18.0, "Healthy Delivery % (${stock.deliveryPercent}%)."))
        } else {
            volumeScore += 4.0
            factors.add(ScoreFactor("Delivery Accumulation", 4.0, 18.0, "Low delivery % (${stock.deliveryPercent}%), mostly speculative intraday volume."))
        }

        if (stock.relativeVolume >= 1.25) {
            volumeScore += 7.0
            factors.add(ScoreFactor("Volume Spike", 7.0, 7.0, "RVOL is ${String.format("%.2f", stock.relativeVolume)}x."))
        }

        // 3. Momentum Runway (Max 15 pts)
        var momentumScore = 0.0
        when {
            stock.rsi14 in 52.0..64.0 -> {
                momentumScore += 15.0
                factors.add(ScoreFactor("Swing Momentum", 15.0, 15.0, "RSI-14 (${stock.rsi14}) provides healthy expansion runway without being overbought."))
            }
            stock.rsi14 in 64.1..72.0 -> {
                momentumScore += 10.0
                factors.add(ScoreFactor("Swing Momentum", 10.0, 15.0, "RSI-14 (${stock.rsi14}) strong but approaching upper momentum band."))
            }
            else -> {
                momentumScore += 4.0
                factors.add(ScoreFactor("Swing Momentum", 4.0, 15.0, "RSI-14 (${stock.rsi14}) sub-optimal for swing breakout."))
            }
        }

        // 4. Fundamental Quality Cushion (Max 20 pts)
        var fundamentalScore = 0.0
        if (stock.roce >= 18.0) {
            fundamentalScore += 10.0
            factors.add(ScoreFactor("ROCE", 10.0, 10.0, "High ROCE (${stock.roce}%) indicates strong compounding power."))
        } else if (stock.roce >= 12.0) {
            fundamentalScore += 6.0
            factors.add(ScoreFactor("ROCE", 6.0, 10.0, "Moderate ROCE (${stock.roce}%)."))
        }

        if (stock.debtToEquity <= 0.4) {
            fundamentalScore += 10.0
            factors.add(ScoreFactor("Debt to Equity", 10.0, 10.0, "Virtually debt-free / Low leverage (D/E: ${stock.debtToEquity})."))
        } else if (stock.debtToEquity <= 0.9) {
            fundamentalScore += 6.0
            factors.add(ScoreFactor("Debt to Equity", 6.0, 10.0, "Manageable leverage (D/E: ${stock.debtToEquity})."))
        }

        // 5. Risk / Reward Structure (Max 10 pts)
        val atr = max(stock.atr14, stock.currentPrice * 0.015)
        val entry = stock.currentPrice
        val structuralSL = min(stock.ema20 * 0.985, entry - (1.5 * atr))
        val stopLoss = roundToTwoDecimals(structuralSL)
        val target1 = roundToTwoDecimals(entry + (3.0 * atr))
        val target2 = roundToTwoDecimals(entry + (5.0 * atr))
        val risk = entry - stopLoss
        val reward = target1 - entry
        val rrRatio = if (risk > 0) roundToTwoDecimals(reward / risk) else 1.0

        var riskRewardScore = 0.0
        if (rrRatio >= 1.8) {
            riskRewardScore += 10.0
            factors.add(ScoreFactor("Risk/Reward", 10.0, 10.0, "Attractive 1:${rrRatio} R:R. Defined SL at ₹$stopLoss, Target 1 at ₹$target1."))
        } else {
            riskRewardScore += 6.0
            factors.add(ScoreFactor("Risk/Reward", 6.0, 10.0, "R:R is 1:${rrRatio}."))
        }

        val totalPoints = trendScore + volumeScore + momentumScore + fundamentalScore + riskRewardScore
        val composite = min(100, max(0, totalPoints.roundToInt()))
        val recommendation = tierRecommendation(composite)

        return ScoredStock(
            stock = stock,
            strategy = ScannerStrategy.SWING,
            compositeScore = composite,
            momentumScore = momentumScore,
            volumeScore = volumeScore,
            trendScore = trendScore,
            fundamentalScore = fundamentalScore,
            riskRewardScore = riskRewardScore,
            recommendation = recommendation,
            entryPrice = entry,
            stopLoss = stopLoss,
            target1 = target1,
            target2 = target2,
            riskRewardRatio = rrRatio,
            factors = factors
        )
    }

    private fun scoreFundamental(stock: Stock): ScoredStock {
        val factors = mutableListOf<ScoreFactor>()

        // 1. Capital Efficiency & Returns (Max 30 pts)
        var fundamentalScore = 0.0
        if (stock.roce >= 20.0) {
            fundamentalScore += 18.0
            factors.add(ScoreFactor("ROCE", 18.0, 18.0, "Elite ROCE of ${stock.roce}% (Benchmark >= 20%)."))
        } else if (stock.roce >= 15.0) {
            fundamentalScore += 12.0
            factors.add(ScoreFactor("ROCE", 12.0, 18.0, "Healthy ROCE of ${stock.roce}%."))
        } else {
            fundamentalScore += 3.0
            factors.add(ScoreFactor("ROCE", 3.0, 18.0, "Sub-par ROCE (${stock.roce}%)."))
        }

        if (stock.roe >= 18.0) {
            fundamentalScore += 12.0
            factors.add(ScoreFactor("ROE", 12.0, 12.0, "Excellent ROE of ${stock.roe}% (Benchmark >= 18%)."))
        } else if (stock.roe >= 13.0) {
            fundamentalScore += 8.0
            factors.add(ScoreFactor("ROE", 8.0, 12.0, "Moderate ROE of ${stock.roe}%."))
        }

        // 2. Solvency & Balance Sheet (Max 25 pts)
        if (stock.debtToEquity <= 0.3) {
            fundamentalScore += 20.0
            factors.add(ScoreFactor("Solvency", 20.0, 20.0, "Minimal debt (D/E: ${stock.debtToEquity}). Safe balance sheet."))
        } else if (stock.debtToEquity <= 0.7) {
            fundamentalScore += 12.0
            factors.add(ScoreFactor("Solvency", 12.0, 20.0, "Acceptable leverage (D/E: ${stock.debtToEquity})."))
        } else {
            fundamentalScore += 2.0
            factors.add(ScoreFactor("Solvency", 2.0, 20.0, "High debt burden (D/E: ${stock.debtToEquity})."))
        }

        // 3. Multi-Year Growth Track Record (Max 25 pts)
        var growthScore = 0.0
        if (stock.profitGrowth3Y >= 15.0) {
            growthScore += 15.0
            factors.add(ScoreFactor("3Y Profit Growth", 15.0, 15.0, "Robust 3-year profit CAGR of ${stock.profitGrowth3Y}%."))
        } else if (stock.profitGrowth3Y >= 10.0) {
            growthScore += 9.0
            factors.add(ScoreFactor("3Y Profit Growth", 9.0, 15.0, "Moderate 3-year profit CAGR of ${stock.profitGrowth3Y}%."))
        }

        if (stock.salesGrowth3Y >= 12.0) {
            growthScore += 10.0
            factors.add(ScoreFactor("3Y Sales Growth", 10.0, 10.0, "Consistent 3-year sales CAGR of ${stock.salesGrowth3Y}%."))
        } else if (stock.salesGrowth3Y >= 8.0) {
            growthScore += 6.0
            factors.add(ScoreFactor("3Y Sales Growth", 6.0, 10.0, "Moderate sales growth of ${stock.salesGrowth3Y}%."))
        }

        // 4. Valuation vs Sector Benchmark (Max 15 pts)
        var valuationScore = 0.0
        if (stock.pe in 1.0..stock.sectorPe) {
            valuationScore += 15.0
            factors.add(ScoreFactor("Valuation", 15.0, 15.0, "Attractive P/E (${stock.pe}) trading at discount to Sector P/E (${stock.sectorPe})."))
        } else if (stock.pe <= stock.sectorPe * 1.3) {
            valuationScore += 9.0
            factors.add(ScoreFactor("Valuation", 9.0, 15.0, "Fair P/E (${stock.pe}) vs Sector P/E (${stock.sectorPe})."))
        } else {
            valuationScore += 3.0
            factors.add(ScoreFactor("Valuation", 3.0, 15.0, "Premium valuation (P/E: ${stock.pe} vs Sector: ${stock.sectorPe})."))
        }

        // 5. Technical Safety Base (Max 5 pts)
        var trendScore = 0.0
        if (stock.currentPrice >= stock.ema200) {
            trendScore += 5.0
            factors.add(ScoreFactor("Long-Term Trend", 5.0, 5.0, "Trading above 200 EMA (₹${stock.ema200})."))
        }

        val entry = stock.currentPrice
        val stopLoss = roundToTwoDecimals(stock.ema200 * 0.94)
        val target1 = roundToTwoDecimals(entry * 1.25) // +25%
        val target2 = roundToTwoDecimals(entry * 1.50) // +50%
        val risk = entry - stopLoss
        val reward = target1 - entry
        val rrRatio = if (risk > 0) roundToTwoDecimals(reward / risk) else 2.0

        val totalPoints = fundamentalScore + growthScore + valuationScore + trendScore
        val composite = min(100, max(0, totalPoints.roundToInt()))
        val recommendation = tierRecommendation(composite)

        return ScoredStock(
            stock = stock,
            strategy = ScannerStrategy.LONG_TERM_FUNDAMENTAL,
            compositeScore = composite,
            momentumScore = 0.0,
            volumeScore = 0.0,
            trendScore = trendScore,
            fundamentalScore = fundamentalScore + growthScore + valuationScore,
            riskRewardScore = 10.0,
            recommendation = recommendation,
            entryPrice = entry,
            stopLoss = stopLoss,
            target1 = target1,
            target2 = target2,
            riskRewardRatio = rrRatio,
            factors = factors
        )
    }

    private fun tierRecommendation(score: Int): Recommendation {
        return when {
            score >= 80 -> Recommendation.STRONG_BUY
            score >= 65 -> Recommendation.BUY
            score >= 45 -> Recommendation.NEUTRAL
            else -> Recommendation.AVOID
        }
    }

    private fun roundToTwoDecimals(value: Double): Double {
        return (value * 100.0).roundToInt() / 100.0
    }
}
