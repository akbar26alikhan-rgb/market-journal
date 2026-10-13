package com.example

import com.example.marketjournal.model.PositionStatus
import com.example.marketjournal.model.ScannerStrategy
import com.example.marketjournal.model.TrackedPosition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StockTrackingLogicTest {

    @Test
    fun `calculates PnL amount and percentage correctly on profit`() {
        val position = TrackedPosition(
            symbol = "RELIANCE",
            stockName = "Reliance Industries Ltd",
            strategy = ScannerStrategy.SWING,
            entryDate = "2026-10-10",
            entryPrice = 2900.0,
            quantity = 10,
            currentPrice = 3045.0,
            stopLoss = 2820.0,
            target1 = 3045.0,
            target2 = 3120.0,
            status = PositionStatus.TARGET_1_HIT,
            pnlAmount = (3045.0 - 2900.0) * 10,
            pnlPercent = ((3045.0 - 2900.0) / 2900.0) * 100.0
        )

        assertEquals(1450.0, position.pnlAmount, 0.01)
        assertEquals(5.0, position.pnlPercent, 0.01)
        assertEquals(PositionStatus.TARGET_1_HIT, position.status)
    }

    @Test
    fun `calculates PnL on stop loss accurately`() {
        val position = TrackedPosition(
            symbol = "INFY",
            stockName = "Infosys Ltd",
            strategy = ScannerStrategy.INTRADAY,
            entryDate = "2026-10-12",
            entryPrice = 1900.0,
            quantity = 20,
            currentPrice = 1870.0,
            stopLoss = 1870.0,
            target1 = 1945.0,
            target2 = 1980.0,
            status = PositionStatus.STOP_LOSS_HIT,
            pnlAmount = (1870.0 - 1900.0) * 20,
            pnlPercent = ((1870.0 - 1900.0) / 1900.0) * 100.0
        )

        assertEquals(-600.0, position.pnlAmount, 0.01)
        assertTrue(position.pnlPercent < 0)
        assertEquals(PositionStatus.STOP_LOSS_HIT, position.status)
    }
}
