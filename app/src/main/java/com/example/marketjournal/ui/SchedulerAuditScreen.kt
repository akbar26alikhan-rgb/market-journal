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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import com.example.marketjournal.calendar.MarketStatusInfo
import com.example.marketjournal.data.local.JobExecutionLogEntity
import com.example.marketjournal.model.JobExecutionStatus
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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun SchedulerAuditScreen(
    state: SchedulerUiState,
    jobLogs: List<JobExecutionLogEntity>,
    onTriggerJob: (LocalDate, Boolean) -> Unit,
    onRefreshCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate900)
            .padding(16.dp)
    ) {
        // Indian Trading Calendar Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("calendar_status_card"),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = AccentSky,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "INDIAN TRADING CALENDAR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentSky,
                            letterSpacing = 0.8.sp
                        )
                    }

                    Text(
                        text = "IST: ${state.calendarStatus.istTime}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.calendarStatus.description,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (state.calendarStatus.isOpen) ProfitGreen else WarningAmber
                )
                Text(
                    text = "Current Trading Date: ${state.calendarStatus.istDate} • Next Session: ${state.calendarStatus.nextTradingDate}",
                    fontSize = 11.sp,
                    color = Slate400
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Scheduler Controls & Execution Buttons
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
                    text = "IDEMPOTENT SERVER SCHEDULER INFRASTRUCTURE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate400,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Trigger Schedule: ${ServerSchedulerIntegrationSpec.CRON_EXPRESSION} (15:30 IST Mon-Fri)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate200
                )
                Text(
                    text = "Idempotency Key: EOD_REPORT_YYYY_MM_DD. Ensures only 1 report per session.",
                    fontSize = 11.sp,
                    color = Slate400
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onTriggerJob(LocalDate.now(), false) },
                        enabled = !state.isRunningJob,
                        colors = ButtonDefaults.buttonColors(containerColor = ProfitGreen, contentColor = Slate900),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("run_scheduled_job_button")
                    ) {
                        if (state.isRunningJob) {
                            CircularProgressIndicator(color = Slate900, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Run Scheduled Job", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { onTriggerJob(LocalDate.now(), true) },
                        enabled = !state.isRunningJob,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningAmber),
                        border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(WarningAmber)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("force_rerun_job_button")
                    ) {
                        Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Force Rerun (Override)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Audit Trail Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SCHEDULER AUDIT LOGS (${jobLogs.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Slate400,
                letterSpacing = 0.8.sp
            )
            Text(
                text = "Persistent DB Records",
                fontSize = 11.sp,
                color = Slate400
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Audit Logs List
        if (jobLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No job executions logged yet.", color = Slate400, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(jobLogs, key = { it.id }) { log ->
                    JobExecutionLogCard(log = log)
                }
            }
        }
    }
}

@Composable
fun JobExecutionLogCard(log: JobExecutionLogEntity) {
    var expanded by remember { mutableStateOf(false) }

    val statusColor = when (log.status) {
        JobExecutionStatus.SUCCESS.name -> ProfitGreen
        JobExecutionStatus.SKIPPED_WEEKEND.name, JobExecutionStatus.SKIPPED_HOLIDAY.name -> WarningAmber
        JobExecutionStatus.ALREADY_RUN_SKIPPED.name -> AccentSky
        else -> LossRed
    }

    val timestampFormatted = try {
        val dt = Instant.ofEpochMilli(log.startedAt).atZone(ZoneId.of("Asia/Kolkata"))
        dt.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
    } catch (e: Exception) {
        ""
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("job_log_card_${log.id}"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Slate800),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Slate700)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = log.status,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = log.executionDate,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Text(
                    text = "$timestampFormatted IST (${log.durationMs}ms)",
                    fontSize = 11.sp,
                    color = Slate400
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = log.message,
                fontSize = 12.sp,
                color = Slate200,
                lineHeight = 16.sp
            )

            AnimatedVisibility(visible = expanded && log.errorLog != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Slate900)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Stacktrace:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = LossRed
                    )
                    Text(
                        text = log.errorLog ?: "",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate400
                    )
                }
            }
        }
    }
}
