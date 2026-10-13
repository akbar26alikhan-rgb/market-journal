package com.example.marketjournal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.marketjournal.calendar.MarketStatusInfo
import com.example.marketjournal.model.MarketBreadthSummary
import com.example.marketjournal.model.MarketDataProviderType
import com.example.ui.theme.AccentSky
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.WarningAmber

@Composable
fun MarketJournalHeader(
    calendarStatus: MarketStatusInfo,
    providerType: MarketDataProviderType,
    marketBreadth: MarketBreadthSummary?,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Slate800)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "MARKET JOURNAL AI",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Data Source Badge
                    val isDemo = providerType == MarketDataProviderType.DEMO_DATA
                    val badgeBg = if (isDemo) WarningAmber.copy(alpha = 0.2f) else ProfitGreen.copy(alpha = 0.2f)
                    val badgeBorder = if (isDemo) WarningAmber else ProfitGreen
                    val badgeText = if (isDemo) "DEMO DATA" else "LIVE NSE"

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeBg)
                            .border(1.dp, badgeBorder, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .testTag("provider_badge")
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDemo) WarningAmber else ProfitGreen
                        )
                    }
                }

                Text(
                    text = "IST: ${calendarStatus.istTime} | ${calendarStatus.istDate}",
                    fontSize = 11.sp,
                    color = Slate400
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Market Status Pill
                val statusDotColor = if (calendarStatus.isOpen) ProfitGreen else LossRed
                val statusBg = if (calendarStatus.isOpen) ProfitGreen.copy(alpha = 0.15f) else LossRed.copy(alpha = 0.15f)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("market_status_pill")
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(statusDotColor)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (calendarStatus.isOpen) "NSE OPEN" else "NSE CLOSED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusDotColor
                    )
                }

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .testTag("refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Market Data",
                        tint = AccentSky
                    )
                }
            }
        }

        // Quick Market Index Ticker Bar
        if (marketBreadth != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Slate700.copy(alpha = 0.6f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("NIFTY 50: ", fontSize = 11.sp, color = Slate400, fontWeight = FontWeight.Bold)
                    Text("₹${marketBreadth.nifty50Price} ", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    val nChangeColor = if (marketBreadth.nifty50ChangePercent >= 0) ProfitGreen else LossRed
                    Text(
                        "${if (marketBreadth.nifty50ChangePercent >= 0) "+" else ""}${marketBreadth.nifty50ChangePercent}%",
                        fontSize = 11.sp,
                        color = nChangeColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("BANK NIFTY: ", fontSize = 11.sp, color = Slate400, fontWeight = FontWeight.Bold)
                    Text("₹${marketBreadth.bankNiftyPrice} ", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    val bChangeColor = if (marketBreadth.bankNiftyChangePercent >= 0) ProfitGreen else LossRed
                    Text(
                        "${if (marketBreadth.bankNiftyChangePercent >= 0) "+" else ""}${marketBreadth.bankNiftyChangePercent}%",
                        fontSize = 11.sp,
                        color = bChangeColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("VIX: ", fontSize = 11.sp, color = Slate400, fontWeight = FontWeight.Bold)
                    Text("${marketBreadth.indiaVix}", fontSize = 11.sp, color = AccentSky, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
