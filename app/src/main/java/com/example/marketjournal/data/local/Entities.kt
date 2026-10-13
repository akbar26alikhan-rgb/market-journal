package com.example.marketjournal.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "tracked_positions")
data class TrackedPositionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val symbol: String,
    val stockName: String,
    val strategy: String,
    val entryDate: String,
    val entryPrice: Double,
    val quantity: Int,
    val currentPrice: Double,
    val stopLoss: Double,
    val target1: Double,
    val target2: Double,
    val status: String,
    val exitPrice: Double? = null,
    val exitDate: String? = null,
    val pnlAmount: Double = 0.0,
    val pnlPercent: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "daily_reports",
    indices = [Index(value = ["reportDate"], unique = true)]
)
data class DailyReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reportDate: String, // e.g. 2026-10-12
    val title: String,
    val nifty50Price: Double,
    val nifty50ChangePercent: Double,
    val bankNiftyPrice: Double,
    val bankNiftyChangePercent: Double,
    val indiaVix: Double,
    val marketSentiment: String,
    val fullMarkdownText: String,
    val generatedAt: Long = System.currentTimeMillis(),
    val idempotentKey: String
)

@Entity(tableName = "job_execution_logs")
data class JobExecutionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val jobName: String,
    val executionDate: String, // YYYY-MM-DD
    val status: String,
    val startedAt: Long,
    val finishedAt: Long,
    val durationMs: Long,
    val message: String,
    val errorLog: String? = null,
    val isIdempotentSkip: Boolean = false
)
