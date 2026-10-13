package com.example.marketjournal.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.marketjournal.calendar.IndianTradingCalendar
import com.example.marketjournal.data.provider.BrokerApiConfig
import com.example.marketjournal.data.provider.BrokerType
import com.example.marketjournal.model.MarketDataProviderType
import com.example.marketjournal.scheduler.ServerSchedulerIntegrationSpec
import com.example.ui.theme.AccentSky
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.WarningAmber

@Composable
fun ConfigScreen(
    state: ConfigUiState,
    onSwitchProvider: (MarketDataProviderType) -> Unit,
    onSaveBrokerConfig: (BrokerApiConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedBroker by remember { mutableStateOf(state.brokerType) }
    var clientCode by remember { mutableStateOf(state.clientCode) }
    var apiKey by remember { mutableStateOf(state.liveApiKey) }
    var passwordOrMpin by remember { mutableStateOf(state.passwordOrMpin) }
    var totpSecret by remember { mutableStateOf(state.totpSecret) }
    var token by remember { mutableStateOf(state.liveAccessToken) }
    var baseUrl by remember { mutableStateOf(state.liveBaseUrl) }
    var showInstructions by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate900)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Data Provider Mode Toggle
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
                                text = "Demo Mode (Simulated NSE Equities — Labeled)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Covers top Indian stocks (VEDL, RELIANCE, TCS, TATAMOTORS, SBIN, etc.) with transparent [DEMO] labeling.",
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
                                text = "Live Broker API Feed (Angel One / Zerodha)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Connects directly to your Indian trading account for live market quotes and market depth.",
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

        // Live Broker Configuration Card
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LIVE BROKER ACCOUNT & API SETTINGS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate400,
                            letterSpacing = 0.8.sp
                        )
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = AccentSky,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Broker Selector Chips
                    Text(
                        text = "Select Indian Broker:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BrokerType.values().forEach { broker ->
                            val isSelected = selectedBroker == broker
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedBroker = broker
                                    baseUrl = broker.defaultBaseUrl
                                },
                                label = { Text(broker.displayName, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ProfitGreen.copy(alpha = 0.2f),
                                    selectedLabelColor = ProfitGreen,
                                    containerColor = Slate700,
                                    labelColor = Slate400
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) ProfitGreen else Slate600
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Status / Validation Banner
                    val statusColor = if (state.providerStatus.isOperational && state.providerType == MarketDataProviderType.LIVE_REST_API) ProfitGreen else WarningAmber
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

                    // Field 1: Account Number / Client Code (MANDATORY FOR ANGEL ONE)
                    OutlinedTextField(
                        value = clientCode,
                        onValueChange = { clientCode = it.trim().uppercase() },
                        label = { Text(selectedBroker.accountLabel) },
                        placeholder = {
                            Text(
                                if (selectedBroker == BrokerType.ANGEL_ONE) "e.g. A1234567 (Angel One Client Code)" else "e.g. AB1234 (Kite User ID)",
                                color = Slate400,
                                fontSize = 12.sp
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("client_code_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = ProfitGreen,
                            unfocusedBorderColor = Slate700
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Field 2: API Key
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it.trim() },
                        label = {
                            Text(
                                if (selectedBroker == BrokerType.ANGEL_ONE) "Angel One SmartAPI Key" else "Broker API Key"
                            )
                        },
                        placeholder = { Text("App API Key from developer portal", color = Slate400, fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("api_key_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = ProfitGreen,
                            unfocusedBorderColor = Slate700
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Angel One Specific Fields: MPIN and TOTP Secret
                    if (selectedBroker == BrokerType.ANGEL_ONE) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = passwordOrMpin,
                                onValueChange = { passwordOrMpin = it.trim() },
                                label = { Text("Angel One MPIN / Password") },
                                placeholder = { Text("4-digit MPIN", color = Slate400, fontSize = 12.sp) },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("mpin_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = ProfitGreen,
                                    unfocusedBorderColor = Slate700
                                )
                            )

                            OutlinedTextField(
                                value = totpSecret,
                                onValueChange = { totpSecret = it.trim() },
                                label = { Text("TOTP Secret / Live TOTP") },
                                placeholder = { Text("Authenticator TOTP", color = Slate400, fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("totp_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = ProfitGreen,
                                    unfocusedBorderColor = Slate700
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Field 4: JWT / Feed Token / Access Token
                    OutlinedTextField(
                        value = token,
                        onValueChange = { token = it.trim() },
                        label = {
                            Text(
                                if (selectedBroker == BrokerType.ANGEL_ONE) "Session JWT Token / Feed Token (Optional if MPIN provided)" else "Access Token / Enctoken"
                            )
                        },
                        placeholder = { Text("Paste active session token if pre-generated", color = Slate400, fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("jwt_token_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = ProfitGreen,
                            unfocusedBorderColor = Slate700
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Field 5: Base URL
                    OutlinedTextField(
                        value = baseUrl,
                        onValueChange = { baseUrl = it.trim() },
                        label = { Text("Broker API Endpoint") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = ProfitGreen,
                            unfocusedBorderColor = Slate700
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val config = BrokerApiConfig(
                                brokerType = selectedBroker,
                                clientCode = clientCode,
                                apiKey = apiKey,
                                passwordOrMpin = passwordOrMpin,
                                totpSecret = totpSecret,
                                sessionOrJwtToken = token,
                                baseUrl = baseUrl
                            )
                            onSaveBrokerConfig(config)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ProfitGreen, contentColor = Slate900),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_live_config_button")
                    ) {
                        Text("Save & Validate ${selectedBroker.displayName} Credentials", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Dedicated Step-by-Step Guide for Angel One SmartAPI Setup
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showInstructions = !showInstructions },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = null,
                                tint = AccentSky,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "HOW TO GET ANGEL ONE SMARTAPI DETAILS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentSky,
                                letterSpacing = 0.8.sp
                            )
                        }

                        Icon(
                            imageVector = if (showInstructions) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Slate400,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    AnimatedVisibility(visible = showInstructions) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "1. Account / Client Code:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Open your Angel One mobile app. Tap on your Profile (top left). Your Client Code looks like 'A1234567' or similar letters + digits.",
                                fontSize = 11.sp,
                                color = Slate400
                            )

                            Text(
                                text = "2. SmartAPI Key:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Visit smartapi.angelbroking.com. Sign in with your Angel One account. Click 'Create an app', select 'Trading API', and name it (e.g. 'MarketJournal'). Copy the generated API Key into the form above.",
                                fontSize = 11.sp,
                                color = Slate400
                            )

                            Text(
                                text = "3. MPIN & TOTP (2-Factor Authentication):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "In the Angel One app > Profile > Security > TOTP: enable Google Authenticator or copy your TOTP secret key. SmartAPI uses this along with your 4-digit MPIN for automated token handshakes.",
                                fontSize = 11.sp,
                                color = Slate400
                            )

                            Text(
                                text = "4. Endpoints & Market Hours:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Angel One live market sessions stream during NSE hours (09:15 - 15:30 IST) via gateway https://apiconnect.angelone.in.",
                                fontSize = 11.sp,
                                color = Slate400
                            )
                        }
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
                        Triple("Angel One SmartAPI Live Feeds", "Integration points configured with Client Code, MPIN, API Key, and TOTP inputs (Awaiting your credentials above)", false),
                        Triple("Cloud Run Server-Side Cron Webhook", "Specification documented (POST /api/v1/jobs/generate-eod-report)", false)
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
    }
}
