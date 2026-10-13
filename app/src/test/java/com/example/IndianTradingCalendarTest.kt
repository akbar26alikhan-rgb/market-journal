package com.example

import com.example.marketjournal.calendar.IndianTradingCalendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class IndianTradingCalendarTest {

    @Test
    fun `isWeekend identifies Saturday and Sunday accurately`() {
        // 2026-10-10 was a Saturday, 2026-10-11 was a Sunday, 2026-10-12 was a Monday
        val saturday = LocalDate.of(2026, 10, 10)
        val sunday = LocalDate.of(2026, 10, 11)
        val monday = LocalDate.of(2026, 10, 12)

        assertTrue(IndianTradingCalendar.isWeekend(saturday))
        assertTrue(IndianTradingCalendar.isWeekend(sunday))
        assertFalse(IndianTradingCalendar.isWeekend(monday))
    }

    @Test
    fun `getHolidayName identifies official Indian stock exchange holidays`() {
        val republicDay = LocalDate.of(2026, 1, 26)
        val independenceDay = LocalDate.of(2026, 8, 15)
        val gandhiJayanti = LocalDate.of(2026, 10, 2)
        val regularDay = LocalDate.of(2026, 10, 14)

        assertEquals("Republic Day", IndianTradingCalendar.getHolidayName(republicDay))
        assertEquals("Independence Day", IndianTradingCalendar.getHolidayName(independenceDay))
        assertEquals("Mahatma Gandhi Jayanti", IndianTradingCalendar.getHolidayName(gandhiJayanti))
        assertNull(IndianTradingCalendar.getHolidayName(regularDay))
    }

    @Test
    fun `isTradingDay returns false on weekends and holidays`() {
        val sunday = LocalDate.of(2026, 10, 11)
        val holiday = LocalDate.of(2026, 1, 26) // Republic Day
        val normalTuesday = LocalDate.of(2026, 10, 13) // Assuming regular working day

        assertFalse(IndianTradingCalendar.isTradingDay(sunday))
        assertFalse(IndianTradingCalendar.isTradingDay(holiday))
        assertTrue(IndianTradingCalendar.isTradingDay(normalTuesday))
    }

    @Test
    fun `getNextTradingDay skips over weekends to Monday`() {
        // Friday 2026-10-09 -> Next trading day should be Monday 2026-10-12
        val friday = LocalDate.of(2026, 10, 9)
        val nextTradingDay = IndianTradingCalendar.getNextTradingDay(friday)

        assertEquals(LocalDate.of(2026, 10, 12), nextTradingDay)
    }
}
