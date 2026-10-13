package com.example.marketjournal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.marketjournal.model.PositionStatus
import com.example.marketjournal.model.ScannerStrategy
import com.example.marketjournal.model.TrackedPosition
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
fun JournalScreen(
    positions: List<TrackedPosition>,
    onUpdateStatus: (Long, PositionStatus, Double?) -> Unit,
    onDeletePosition: (Long) -> Unit,
    onAddCustomPosition: (String, String, ScannerStrategy, Double, Int, Double, Double, Double, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilterTab by remember { mutableIntStateOf(0) } // 0: Active, 1: Closed, 2: All
    var showAddDialog by remember { mutableStateOf(false) }

    // Calculate live portfolio analytics
    val totalTrades = positions.size
    val activeTrades = positions.count { it.status == PositionStatus.ACTIVE }
    val closedTradesList = positions.filter { it.status != PositionStatus.ACTIVE }
    val winningTrades = closedTradesList.count { it.pnlAmount > 0 }
    val winRate = if (closedTradesList.isNotEmpty()) (winningTrades.toDouble() / closedTradesList.size) * 100.0 else 0.0

    val totalInvested = positions.sumOf { it.entryPrice * it.quantity }
    val currentVal = positions.sumOf { it.currentPrice * it.quantity }
    val netPnl = positions.sumOf { it.pnlAmount }
    val netPnlPct = if (totalInvested > 0) (netPnl / totalInvested) * 100.0 else 0.0

    val filteredPositions = when (selectedFilterTab) {
        0 -> positions.filter { it.status == PositionStatus.ACTIVE }
        1 -> positions.filter { it.status != PositionStatus.ACTIVE }
        else -> positions
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Slate900)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Portfolio Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("journal_performance_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Slate800),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Slate700)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "JOURNAL PAPER TRADING LEDGER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Invested", fontSize = 11.sp, color = Slate400)
                            Text(
                                text = "₹${String.format("%,.0f", totalInvested)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Column {
                            Text("Current Value", fontSize = 11.sp, color = Slate400)
                            Text(
                                text = "₹${String.format("%,.0f", currentVal)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Net P&L", fontSize = 11.sp, color = Slate400)
                            val pnlColor = if (netPnl >= 0) ProfitGreen else LossRed
                            Text(
                                text = "${if (netPnl >= 0) "+" else ""}₹${String.format("%,.0f", netPnl)} (${String.format("%.2f", netPnlPct)}%)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = pnlColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Slate700.copy(alpha = 0.5f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Trades: $totalTrades", fontSize = 11.sp, color = Slate200)
                            Text("Active: $activeTrades", fontSize = 11.sp, color = AccentSky, fontWeight = FontWeight.Bold)
                            Text("Win Rate: ${String.format("%.1f", winRate)}%", fontSize = 11.sp, color = ProfitGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Tabs for Active / Closed / All
            TabRow(
                selectedTabIndex = selectedFilterTab,
                containerColor = Slate800,
                contentColor = ProfitGreen
            ) {
                listOf("Active Positions ($activeTrades)", "Closed Trades (${closedTradesList.size})", "All Trades ($totalTrades)").forEachIndexed { index, title ->
                    Tab(
                        selected = selectedFilterTab == index,
                        onClick = { selectedFilterTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedFilterTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedFilterTab == index) ProfitGreen else Slate400
                            )
                        },
                        modifier = Modifier.testTag("journal_tab_$index")
                    )
                }
            }

            // Positions List
            if (filteredPositions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (selectedFilterTab == 0) "No active tracked positions." else "No positions in this filter.",
                            color = Slate400,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Add trade ideas from the Scanners tab or tap '+' to enter a custom trade.",
                            color = Slate400,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredPositions, key = { it.id }) { position ->
                        JournalPositionCard(
                            position = position,
                            onUpdateStatus = onUpdateStatus,
                            onDelete = onDeletePosition
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }

        // Floating Action Button to Add Custom Trade
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = ProfitGreen,
            contentColor = Slate900,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_custom_trade_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Custom Trade")
        }
    }

    if (showAddDialog) {
        AddCustomTradeDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { symbol, name, strat, entry, qty, sl, t1, t2, notes ->
                onAddCustomPosition(symbol, name, strat, entry, qty, sl, t1, t2, notes)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun JournalPositionCard(
    position: TrackedPosition,
    onUpdateStatus: (Long, PositionStatus, Double?) -> Unit,
    onDelete: (Long) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("position_card_${position.symbol}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Slate800),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Slate700)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Symbol, Strategy, Date, Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = position.symbol,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(AccentSky.copy(alpha = 0.15f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = position.strategy.displayName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentSky
                        )
                    }
                }

                // Status Pill
                val statusColor = when (position.status) {
                    PositionStatus.ACTIVE -> AccentSky
                    PositionStatus.TARGET_1_HIT, PositionStatus.TARGET_2_HIT -> ProfitGreen
                    PositionStatus.STOP_LOSS_HIT -> LossRed
                    PositionStatus.CLOSED -> Slate400
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = position.status.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${position.stockName} • Entry: ${position.entryDate}",
                fontSize = 11.sp,
                color = Slate400
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Price & PnL Breakdown
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Slate900)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Entry (Qty: ${position.quantity})", fontSize = 10.sp, color = Slate400)
                    Text("₹${position.entryPrice}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column {
                    Text("Current / Exit", fontSize = 10.sp, color = Slate400)
                    val price = position.exitPrice ?: position.currentPrice
                    Text("₹$price", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column {
                    Text("Stop Loss", fontSize = 10.sp, color = LossRed)
                    Text("₹${position.stopLoss}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LossRed)
                }
                Column {
                    Text("Target 1", fontSize = 10.sp, color = ProfitGreen)
                    Text("₹${position.target1}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ProfitGreen)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("P&L", fontSize = 10.sp, color = Slate400)
                    val pnlColor = if (position.pnlAmount >= 0) ProfitGreen else LossRed
                    Text(
                        text = "${if (position.pnlAmount >= 0) "+" else ""}₹${position.pnlAmount}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = pnlColor
                    )
                    Text(
                        text = "(${if (position.pnlPercent >= 0) "+" else ""}${position.pnlPercent}%)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = pnlColor
                    )
                }
            }

            if (position.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Notes: ${position.notes}",
                    fontSize = 11.sp,
                    color = Slate400
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (position.status == PositionStatus.ACTIVE) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = { onUpdateStatus(position.id, PositionStatus.TARGET_1_HIT, position.target1) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ProfitGreen),
                            border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ProfitGreen)),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Hit T1", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { onUpdateStatus(position.id, PositionStatus.STOP_LOSS_HIT, position.stopLoss) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = LossRed),
                            border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(LossRed)),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Hit SL", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { onUpdateStatus(position.id, PositionStatus.CLOSED, position.currentPrice) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate200),
                            border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate700)),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Close", fontSize = 11.sp)
                        }
                    }
                } else {
                    Text(
                        text = "Outcome recorded on ${position.exitDate ?: position.entryDate}",
                        fontSize = 11.sp,
                        color = Slate400
                    )
                }

                IconButton(
                    onClick = { onDelete(position.id) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete position",
                        tint = Slate400,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddCustomTradeDialog(
    onDismiss: () -> Unit,
    onConfirm: (symbol: String, name: String, strat: ScannerStrategy, entry: Double, qty: Int, sl: Double, t1: Double, t2: Double, notes: String) -> Unit
) {
    var symbol by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var strategy by remember { mutableStateOf(ScannerStrategy.SWING) }
    var entryStr by remember { mutableStateOf("") }
    var qtyStr by remember { mutableStateOf("10") }
    var slStr by remember { mutableStateOf("") }
    var t1Str by remember { mutableStateOf("") }
    var t2Str by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Paper Trade", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = symbol,
                    onValueChange = { symbol = it.uppercase() },
                    label = { Text("NSE Symbol (e.g. INFY)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Company Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = entryStr,
                        onValueChange = { entryStr = it },
                        label = { Text("Entry Price ₹") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    OutlinedTextField(
                        value = qtyStr,
                        onValueChange = { qtyStr = it },
                        label = { Text("Quantity") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = slStr,
                        onValueChange = { slStr = it },
                        label = { Text("Stop Loss ₹") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    OutlinedTextField(
                        value = t1Str,
                        onValueChange = { t1Str = it },
                        label = { Text("Target 1 ₹") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Trade Rationale / Notes") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val entry = entryStr.toDoubleOrNull() ?: 0.0
                    val qty = qtyStr.toIntOrNull() ?: 1
                    val sl = slStr.toDoubleOrNull() ?: (entry * 0.97)
                    val t1 = t1Str.toDoubleOrNull() ?: (entry * 1.05)
                    val t2 = t2Str.toDoubleOrNull() ?: (entry * 1.10)
                    if (symbol.isNotBlank() && entry > 0) {
                        onConfirm(symbol, if (name.isBlank()) symbol else name, strategy, entry, qty, sl, t1, t2, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ProfitGreen, contentColor = Slate900)
            ) {
                Text("Save Trade", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Slate400)
            }
        },
        containerColor = Slate800
    )
}
