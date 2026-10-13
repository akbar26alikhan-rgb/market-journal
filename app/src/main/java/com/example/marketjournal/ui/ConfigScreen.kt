package com.example.marketjournal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.marketjournal.calendar.IndianTradingCalendar
import com.example.marketjournal.model.MarketDataProviderType
import com.example.marketjournal.scheduler.ServerSchedulerIntegrationSpec
import com.example.ui.theme.AccentSky
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.WarningAmber

@Composable
fun ConfigScreen(
    state: ConfigUiState,
    onSwitchProvider: (MarketDataProviderType) -> Unit,
    onSaveLiveConfig: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var baseUrl by remember { mutableStateOf(state.liveBaseUrl) }
    var apiKey by remember { mutableStateOf(state.liveApiKey) }
    var token by remember { mutableStateOf(state.liveAccessToken) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate900)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Data Provider Selector Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("provider_selector_card"),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Slate800),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Slate700)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "MARKET DATA PROVIDER MODE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Option 1: Demo Provider
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (state.providerType == MarketDataProviderType.DEMO_DATA) Slate700.copy(alpha = 0.6f) else Color.Transparent)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = state.providerType == MarketDataProviderType.DEMO_DATA,
                            onClick = { onSwitchProvider(MarketDataProviderType.DEMO_DATA) },
                            colors = RadioButtonDefaults.colors(selectedColor = ProfitGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Demo Data Provider (NSE Equities — Labeled)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "High-fidelity simulated NSE tickers (RELIANCE, TCS, HDFCBANK, etc.) with transparent [DEMO] labeling.",
                                fontSize = 11.sp,
                                color = Slate400
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Option 2: Live Provider
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (state.providerType == MarketDataProviderType.LIVE_REST_API) Slate700.copy(alpha = 0.6f) else Color.Transparent)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = state.providerType == MarketDataProviderType.LIVE_REST_API,
                            onClick = { onSwitchProvider(MarketDataProviderType.LIVE_REST_API) },
                            colors = RadioButtonDefaults.colors(selectedColor = ProfitGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Live REST Market Feed (Zerodha Kite / Angel One)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Connects to official broker / NSE gateway with authenticated API keys.",
                                fontSize = 11.sp,
                                color = Slate400
                            )
                        }
                    }

                    if (state.saveMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.saveMessage,
                            fontSize = 11.sp,
                            color = AccentSky,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Live API Configuration Form
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("live_api_form_card"),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Slate800),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Slate700)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "LIVE MARKET API INTEGRATION SETTINGS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Status pill
                    val statusColor = if (state.providerStatus.isOperational) ProfitGreen else WarningAmber
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (state.providerStatus.isOperational) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = state.providerStatus.message,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = baseUrl,
                        onValueChange = { baseUrl = it },
                        label = { Text("Broker API Gateway Base URL") },
                        placeholder = { Text("https://api.kite.trade") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("API Key (e.g. Kite API Key)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = token,
                        onValueChange = { token = it },
                        label = { Text("Access Token / Session Secret") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { onSaveLiveConfig(baseUrl, apiKey, token) },
                        colors = ButtonDefaults.buttonColors(containerColor = ProfitGreen, contentColor = Slate900),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_live_config_button")
                    ) {
                        Text("Save & Validate Live Credentials", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Feature Implementation & Integration Checklist
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("integration_checklist_card"),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Slate800),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Slate700)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "FEATURE IMPLEMENTATION & DEPLOYMENT CHECKLIST",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val checklistItems = listOf(
                        Triple("Deterministic Stock-Scoring Engine", "Fully operational (RSI, RVOL, EMA20/50/200, ROCE, D/E, Target/SL calculation)", true),
                        Triple("Intraday Momentum Scanner", "Fully operational with RVOL >= 1.5x and Day High breakout algorithms", true),
                        Triple("Swing Trend Pullback Scanner", "Fully operational with 20 EMA pullbacks & delivery volume accumulation", true),
                        Triple("Long-Term Fundamental Scanner", "Fully operational with ROCE > 18%, D/E < 0.5, 3Y growth filters", true),
                        Triple("Persistent Room Database", "Fully operational (Tracked trades, daily reports, execution audit logs)", true),
                        Triple("Stock-Selection Tracking Ledger", "Fully operational with real-time P&L calculation, Target/SL triggers, and win rate", true),
                        Triple("Daily Report-Generation Pipeline", "Fully operational (Markdown report generation with market breadth)", true),
                        Triple("Indian Trading Calendar & Holidays", "Fully operational with IST timezone (09:15-15:30) and NSE/BSE holiday list", true),
                        Triple("Idempotent Server Scheduler Engine", "Fully operational (skips weekends/holidays, prevents duplicate runs, logs audit trail)", true),
                        Triple("Live Broker Market-Data Feed", "Blocked by external credentials (requires Zerodha Kite / Angel One API Key & Token)", false),
                        Triple("Cloud Run Server-Side Cron Webhook", "Blocked by external cloud deployment (API endpoint specification documented)", false)
                    )

                    checklistItems.forEach { (name, desc, isWorking) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = if (isWorking) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isWorking) ProfitGreen else WarningAmber,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isWorking) Color.White else WarningAmber
                                )
                                Text(
                                    text = desc,
                                    fontSize = 11.sp,
                                    color = Slate400,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Server Webhook Contract Documentation
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Slate800),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Slate700)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "SERVER-SIDE WEBHOOK SPECIFICATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "POST ${ServerSchedulerIntegrationSpec.WEBHOOK_ENDPOINT_PATH}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = AccentSky
                    )
                    Text(
                        text = "Cron: ${ServerSchedulerIntegrationSpec.CRON_EXPRESSION} (Mon-Fri 15:30 IST)",
                        fontSize = 11.sp,
                        color = Slate400
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = ServerSchedulerIntegrationSpec.SAMPLE_REQUEST_BODY,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate200,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Slate900)
                            .padding(8.dp)
                    )
                }
            }
        }

        // Official NSE/BSE Market Holidays Reference
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Slate800),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Slate700)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "NSE / BSE TRADING HOLIDAYS (2025 - 2026)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    IndianTradingCalendar.OFFICIAL_HOLIDAYS.entries.take(10).forEach { (date, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(date, fontSize = 11.sp, color = AccentSky, fontFamily = FontFamily.Monospace)
                            Text(name, fontSize = 11.sp, color = Slate200)
                        }
                    }
                }
            }
        }
    }
}
