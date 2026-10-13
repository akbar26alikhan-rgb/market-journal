package com.example.marketjournal.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        TrackedPositionEntity::class,
        DailyReportEntity::class,
        JobExecutionLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MarketJournalDatabase : RoomDatabase() {

    abstract fun marketJournalDao(): MarketJournalDao

    companion object {
        @Volatile
        private var INSTANCE: MarketJournalDatabase? = null

        fun getDatabase(context: Context): MarketJournalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MarketJournalDatabase::class.java,
                    "market_journal_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
