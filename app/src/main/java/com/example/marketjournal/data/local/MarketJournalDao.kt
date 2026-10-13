package com.example.marketjournal.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketJournalDao {

    // --- Tracked Positions ---
    @Query("SELECT * FROM tracked_positions ORDER BY createdAt DESC")
    fun getAllPositions(): Flow<List<TrackedPositionEntity>>

    @Query("SELECT * FROM tracked_positions WHERE status = 'ACTIVE' ORDER BY createdAt DESC")
    fun getActivePositions(): Flow<List<TrackedPositionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosition(position: TrackedPositionEntity): Long

    @Update
    suspend fun updatePosition(position: TrackedPositionEntity)

    @Query("DELETE FROM tracked_positions WHERE id = :id")
    suspend fun deletePositionById(id: Long)

    @Query("SELECT * FROM tracked_positions WHERE id = :id")
    suspend fun getPositionById(id: Long): TrackedPositionEntity?

    // --- Daily EOD Reports ---
    @Query("SELECT * FROM daily_reports ORDER BY reportDate DESC")
    fun getAllReports(): Flow<List<DailyReportEntity>>

    @Query("SELECT * FROM daily_reports WHERE reportDate = :date LIMIT 1")
    suspend fun getReportByDate(date: String): DailyReportEntity?

    @Query("SELECT * FROM daily_reports WHERE id = :id LIMIT 1")
    suspend fun getReportById(id: Long): DailyReportEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: DailyReportEntity): Long

    // --- Job Execution Audit Logs ---
    @Query("SELECT * FROM job_execution_logs ORDER BY startedAt DESC LIMIT 100")
    fun getAllJobLogs(): Flow<List<JobExecutionLogEntity>>

    @Query("SELECT * FROM job_execution_logs WHERE jobName = :jobName AND executionDate = :date ORDER BY startedAt DESC LIMIT 1")
    suspend fun getLatestJobLogForDate(jobName: String, date: String): JobExecutionLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobLog(log: JobExecutionLogEntity): Long
}
