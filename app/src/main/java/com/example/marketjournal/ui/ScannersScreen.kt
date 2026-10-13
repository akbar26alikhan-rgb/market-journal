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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.marketjournal.model.Recommendation
import com.example.marketjournal.model.ScannerStrategy
import com.example.marketjournal.model.ScoredStock
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
fun ScannersScreen(
    state: ScannersUiState,
    onStrategySelected: (ScannerStrategy) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onMinScoreChanged: (Int) -> Unit,
    onTrackStock: (ScoredStock) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate900)
    ) {
        // Strategy Tabs
        val strategies = ScannerStrategy.values()
        val selectedIndex = strategies.indexOf(state.selectedStrategy).coerceAtLeast(0)

        ScrollableTabRow(
            selectedTabIndex = selectedIndex,
            containerColor = Slate800,
            contentColor = ProfitGreen,
            edgePadding = 12.dp
        ) {
            strategies.forEachIndexed { index, strategy ->
                Tab(
                    selected = selectedIndex == index,
                    onClick = { onStrategySelected(strategy) },
                    text = {
                        Text(
                            text = strategy.displayName,
                            fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp,
                            color = if (selectedIndex == index) ProfitGreen else Slate400
                        )
                    },
                    modifier = Modifier.testTag("strategy_tab_${strategy.name}")
                )
            }
        }

        // Strategy Objective Sub-banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Slate800.copy(alpha = 0.5f))
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Horizon: ${state.selectedStrategy.holdingHorizon}",
                    fontSize = 11.sp,
                    color = AccentSky,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Scoring: Deterministic (0-100)",
                    fontSize = 11.sp,
                    color = Slate400
                )
            }
        }

        // Filter & Search Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = { Text("Search NSE symbol or sector...", color = Slate400, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Slate400) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("search_stocks_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ProfitGreen,
                    unfocusedBorderColor = Slate700,
                    focusedContainerColor = Slate800,
                    unfocusedContainerColor = Slate800,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            // Score Threshold Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Min Score:", fontSize = 11.sp, color = Slate400)
                listOf(0 to "All", 60 to "60+", 70 to "70+", 80 to "80+ (Top)").forEach { (score, label) ->
                    val isSelected = state.minScoreFilter == score
                    FilterChip(
                        selected = isSelected,
                        onClick = { onMinScoreChanged(score) },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ProfitGreen.copy(alpha = 0.2f),
                            selectedLabelColor = ProfitGreen,
                            containerColor = Slate800,
                            labelColor = Slate400
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) ProfitGreen else Slate700
                        ),
                        modifier = Modifier.testTag("filter_chip_$score")
                    )
                }
            }
        }

        // Content List
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("scanner_loading"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = ProfitGreen)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Scanning NSE Equities with deterministic models...", color = Slate400, fontSize = 13.sp)
                }
            }
        } else {
            val filteredStocks = state.scoredStocks.filter { scored ->
                val matchesQuery = state.searchQuery.isBlank() ||
                        scored.stock.symbol.contains(state.searchQuery, ignoreCase = true) ||
                        scored.stock.companyName.contains(state.searchQuery, ignoreCase = true) ||
                        scored.stock.sector.contains(state.searchQuery, ignoreCase = true)
                val matchesScore = scored.compositeScore >= state.minScoreFilter
                matchesQuery && matchesScore
            }

            if (filteredStocks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No setups match the current filters.",
                        color = Slate400,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredStocks, key = { it.stock.symbol }) { scored ->
                        ScoredStockCard(
                            scored = scored,
                            onTrackStock = onTrackStock
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ScoredStockCard(
    scored: ScoredStock,
    onTrackStock: (ScoredStock) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var isTracked by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("stock_card_${scored.stock.symbol}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Slate800),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (scored.compositeScore >= 80) ProfitGreen.copy(alpha = 0.5f) else Slate700
            )
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Symbol, Sector, Price, Change %
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = scored.stock.symbol,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = scored.stock.exchange,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate400,
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(Slate700)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                        if (scored.stock.isDemoData) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DEMO",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarningAmber,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(WarningAmber.copy(alpha = 0.15f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = "${scored.stock.companyName} • ${scored.stock.sector}",
                        fontSize = 11.sp,
                        color = Slate400
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${scored.stock.currentPrice}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    val changeColor = if (scored.stock.changePercent >= 0) ProfitGreen else LossRed
                    Text(
                        text = "${if (scored.stock.changePercent >= 0) "+" else ""}${scored.stock.changePercent}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = changeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Score Banner & Recommendation Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Slate700.copy(alpha = 0.5f))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val scoreColor = when {
                        scored.compositeScore >= 80 -> ProfitGreen
                        scored.compositeScore >= 65 -> AccentSky
                        scored.compositeScore >= 45 -> WarningAmber
                        else -> LossRed
                    }
                    Text(
                        text = "${scored.compositeScore}",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = scoreColor
                    )
                    Text(
                        text = "/100",
                        fontSize = 12.sp,
                        color = Slate400,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(scoreColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = scored.recommendation.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = scoreColor
                        )
                    }
                }

                // Quick stats: RVOL & RSI
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "RVOL: ${String.format("%.2f", scored.stock.relativeVolume)}x",
                        fontSize = 11.sp,
                        color = if (scored.stock.relativeVolume >= 1.5) ProfitGreen else Slate400,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "RSI: ${String.format("%.1f", scored.stock.rsi14)}",
                        fontSize = 11.sp,
                        color = AccentSky,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Trade Plan Structure Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Slate900)
                    .border(1.dp, Slate700, RoundedCornerShape(6.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("ENTRY", fontSize = 10.sp, color = Slate400, fontWeight = FontWeight.Bold)
                            Text("₹${scored.entryPrice}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column {
                            Text("STOP LOSS", fontSize = 10.sp, color = LossRed, fontWeight = FontWeight.Bold)
                            Text("₹${scored.stopLoss}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LossRed)
                        }
                        Column {
                            Text("TARGET 1", fontSize = 10.sp, color = ProfitGreen, fontWeight = FontWeight.Bold)
                            Text("₹${scored.target1}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ProfitGreen)
                        }
                        Column {
                            Text("TARGET 2", fontSize = 10.sp, color = ProfitGreen, fontWeight = FontWeight.Bold)
                            Text("₹${scored.target2}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ProfitGreen)
                        }
                        Column {
                            Text("R:R", fontSize = 10.sp, color = AccentSky, fontWeight = FontWeight.Bold)
                            Text("1:${scored.riskRewardRatio}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccentSky)
                        }
                    }
                }
            }

            // Expandable Factor Explanations
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Text(
                        text = "Deterministic Scoring Reasons:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    scored.factors.forEach { f ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("• ", color = ProfitGreen, fontSize = 12.sp)
                            Text(
                                text = f.description,
                                fontSize = 11.sp,
                                color = Slate200,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clickable { expanded = !expanded }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (expanded) "Hide Scoring Factors" else "View Scoring Factors (${scored.factors.size})",
                        fontSize = 11.sp,
                        color = AccentSky,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle factors",
                        tint = AccentSky,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Button(
                    onClick = {
                        if (!isTracked) {
                            onTrackStock(scored)
                            isTracked = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTracked) Slate700 else ProfitGreen,
                        contentColor = if (isTracked) ProfitGreen else Slate900
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("track_button_${scored.stock.symbol}")
                ) {
                    Icon(
                        imageVector = if (isTracked) Icons.Default.Check else Icons.Default.AddChart,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isTracked) "Tracked in Journal" else "Track in Journal",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
