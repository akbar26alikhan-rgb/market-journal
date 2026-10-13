package com.example.marketjournal.calendar

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Handles the Indian Equity Trading Calendar (NSE / BSE).
 * Respects Indian Standard Time (IST, UTC+5:30), market hours (09:15 - 15:30),
 * weekends, and official exchange holidays.
 */
object IndianTradingCalendar {

    val IST_ZONE_ID: ZoneId = ZoneId.of("Asia/Kolkata")

    // NSE Trading Hours in IST
    val MARKET_PRE_OPEN: LocalTime = LocalTime.of(9, 0)
    val MARKET_OPEN: LocalTime = LocalTime.of(9, 15)
    val MARKET_CLOSE: LocalTime = LocalTime.of(15, 30)

    /**
     * Map of official NSE/BSE Trading Holidays for 2025 & 2026 (ISO date strings -> Holiday Name).
     */
    val OFFICIAL_HOLIDAYS: Map<String, String> = mapOf(
        // 2025 Holidays
        "2025-01-26" to "Republic Day",
        "2025-02-26" to "Mahashivratri",
        "2025-03-14" to "Holi",
        "2025-03-31" to "Id-Ul-Fitr (Ramzan Id)",
        "2025-04-10" to "Mahavir Jayanti",
        "2025-04-14" to "Dr. Baba Saheb Ambedkar Jayanti",
        "2025-04-18" to "Good Friday",
        "2025-05-01" to "Maharashtra Day",
        "2025-06-07" to "Bakri Id",
        "2025-08-15" to "Independence Day",
        "2025-08-27" to "Ganesh Chaturthi",
        "2025-10-02" to "Mahatma Gandhi Jayanti",
        "2025-10-21" to "Diwali Laxmi Pujan (Muhurat Trading only)",
        "2025-10-22" to "Diwali Balipratipada",
        "2025-11-05" to "Gurunanak Jayanti",
        "2025-12-25" to "Christmas",

        // 2026 Holidays
        "2026-01-26" to "Republic Day",
        "2026-02-16" to "Mahashivratri",
        "2026-03-04" to "Holi",
        "2026-03-20" to "Id-Ul-Fitr",
        "2026-04-03" to "Good Friday",
        "2026-04-14" to "Dr. Baba Saheb Ambedkar Jayanti",
        "2026-05-01" to "Maharashtra Day",
        "2026-05-27" to "Bakri Id",
        "2026-08-15" to "Independence Day",
        "2026-10-02" to "Mahatma Gandhi Jayanti",
        "2026-10-20" to "Dussehra",
        "2026-11-08" to "Diwali Laxmi Pujan (Muhurat Trading)",
        "2026-11-10" to "Diwali Balipratipada",
        "2026-11-24" to "Gurunanak Jayanti",
        "2026-12-25" to "Christmas"
    )

    /**
     * Returns current date-time in IST.
     */
    fun currentIstDateTime(): ZonedDateTime = ZonedDateTime.now(IST_ZONE_ID)

    /**
     * Returns current date in IST.
     */
    fun currentIstDate(): LocalDate = currentIstDateTime().toLocalDate()

    /**
     * Checks if given date is a weekend (Saturday or Sunday).
     */
    fun isWeekend(date: LocalDate): Boolean {
        return date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
    }

    /**
     * Checks if given date is an official NSE/BSE holiday.
     */
    fun getHolidayName(date: LocalDate): String? {
        val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        return OFFICIAL_HOLIDAYS[dateStr]
    }

    /**
     * True if the date is a legitimate trading day (not a weekend and not a holiday).
     */
    fun isTradingDay(date: LocalDate): Boolean {
        if (isWeekend(date)) return false
        if (getHolidayName(date) != null) return false
        return true
    }

    /**
     * Returns market status for the current moment in IST.
     */
    fun getMarketStatus(zonedDateTime: ZonedDateTime = currentIstDateTime()): MarketStatusInfo {
        val date = zonedDateTime.toLocalDate()
        val time = zonedDateTime.toLocalTime()
        val isTrading = isTradingDay(date)
        val holiday = getHolidayName(date)

        val statusDescription = when {
            isWeekend(date) -> "Closed (Weekend - ${date.dayOfWeek.name})"
            holiday != null -> "Closed (NSE Holiday - $holiday)"
            time.isBefore(MARKET_PRE_OPEN) -> "Pre-Market Closed (Opens at 09:15 IST)"
            time.isBefore(MARKET_OPEN) -> "Pre-Open Session (09:00 - 09:08 IST)"
            time.isAfter(MARKET_CLOSE) -> "Market Closed (EOD Post-Market Session)"
            else -> "Market Open (Trading Active)"
        }

        val isOpen = isTrading && !time.isBefore(MARKET_OPEN) && !time.isAfter(MARKET_CLOSE)

        return MarketStatusInfo(
            isOpen = isOpen,
            isTradingDay = isTrading,
            isWeekend = isWeekend(date),
            holidayName = holiday,
            istDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
            istTime = time.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
            description = statusDescription,
            nextTradingDate = getNextTradingDay(date).format(DateTimeFormatter.ISO_LOCAL_DATE)
        )
    }

    /**
     * Calculates the nearest previous trading day on or before the given date.
     */
    fun getLatestTradingDay(fromDate: LocalDate = currentIstDate()): LocalDate {
        var date = fromDate
        while (!isTradingDay(date)) {
            date = date.minusDays(1)
        }
        return date
    }

    /**
     * Finds the next upcoming trading day strictly after the given date.
     */
    fun getNextTradingDay(fromDate: LocalDate = currentIstDate()): LocalDate {
        var date = fromDate.plusDays(1)
        while (!isTradingDay(date)) {
            date = date.plusDays(1)
        }
        return date
    }
}

data class MarketStatusInfo(
    val isOpen: Boolean,
    val isTradingDay: Boolean,
    val isWeekend: Boolean,
    val holidayName: String?,
    val istDate: String,
    val istTime: String,
    val description: String,
    val nextTradingDate: String
)
