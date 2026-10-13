package com.example

import com.example.marketjournal.engine.DeterministicStockScoringEngine
import com.example.marketjournal.model.Recommendation
import com.example.marketjournal.model.ScannerStrategy
import com.example.marketjournal.model.Stock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeterministicStockScoringEngineTest {

    private val sampleBullishStock = Stock(
        symbol = "TESTBULL",
        companyName = "Test Bullish Industries Ltd",
        exchange = "NSE",
        sector = "Technology",
        currentPrice = 1000.0,
        changePercent = 2.5,
        dayHigh = 1005.0,
        dayLow = 980.0,
        volume = 2000000,
        avgVolume20D = 1000000,
        relativeVolume = 2.0, // 2.0x volume surge
        deliveryPercent = 62.0, // strong accumulation
        marketCapCr = 50000.0,
        pe = 22.0,
        sectorPe = 25.0,
        pb = 4.0,
        roe = 24.0,
        roce = 28.0,
        debtToEquity = 0.1,
        salesGrowth3Y = 18.0,
        profitGrowth3Y = 22.0,
        rsi14 = 64.0, // sweet spot
        ema20 = 980.0,
        ema50 = 950.0,
        ema200 = 880.0,
        vwap = 992.0,
        atr14 = 20.0,
        isDemoData = true
    )

    @Test
    fun `scoreIntraday awards high score for strong momentum, RVOL and VWAP alignment`() {
        val result = DeterministicStockScoringEngine.score(sampleBullishStock, ScannerStrategy.INTRADAY)

        assertTrue("Composite score should be >= 75 for pristine setup", result.compositeScore >= 75)
        assertEquals(Recommendation.STRONG_BUY, result.recommendation)
        assertTrue("Entry price should equal current price", result.entryPrice == 1000.0)
        assertTrue("Stop loss should be below entry", result.stopLoss < 1000.0)
        assertTrue("Target 1 should be above entry", result.target1 > 1000.0)
        assertTrue("Target 2 should be above Target 1", result.target2 > result.target1)
        assertTrue("Risk Reward ratio should be at least 1:1.5", result.riskRewardRatio >= 1.5)
        assertTrue("Factors list should explain scoring rationale", result.factors.isNotEmpty())
    }

    @Test
    fun `scoreSwing checks Stage 2 moving averages and delivery percentage`() {
        val result = DeterministicStockScoringEngine.score(sampleBullishStock, ScannerStrategy.SWING)

        assertTrue("Swing score should be high for moving average alignment and delivery %", result.compositeScore >= 70)
        assertTrue("Stop Loss should be structural", result.stopLoss <= 980.0)
        assertTrue("Target 1 should provide swing target", result.target1 >= 1050.0)
    }

    @Test
    fun `scoreFundamental rewards high ROCE and low debt to equity`() {
        val result = DeterministicStockScoringEngine.score(sampleBullishStock, ScannerStrategy.LONG_TERM_FUNDAMENTAL)

        assertTrue("Fundamental compounder score should be >= 80", result.compositeScore >= 80)
        assertTrue("Target 1 is +25% expansion", result.target1 == 1250.0)
        assertTrue("Target 2 is +50% expansion", result.target2 == 1500.0)
    }

    @Test
    fun `rankWatchlist sorts scored stocks in descending order`() {
        val lowerStock = sampleBullishStock.copy(
            symbol = "WEAKSTOCK",
            relativeVolume = 0.5,
            rsi14 = 35.0,
            currentPrice = 900.0,
            vwap = 920.0,
            ema20 = 930.0,
            roce = 6.0,
            debtToEquity = 2.5
        )

        val ranked = DeterministicStockScoringEngine.rankWatchlist(
            listOf(lowerStock, sampleBullishStock),
            ScannerStrategy.INTRADAY
        )

        assertEquals("TESTBULL", ranked.first().stock.symbol)
        assertEquals("WEAKSTOCK", ranked.last().stock.symbol)
        assertTrue(ranked.first().compositeScore > ranked.last().compositeScore)
    }
}
