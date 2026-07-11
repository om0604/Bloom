package com.bloom.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bloom.app.data.local.dao.JournalEntryDao
import com.bloom.app.data.local.dao.MoodEntryDao
import com.bloom.app.data.local.entity.JournalEntryEntity
import com.bloom.app.data.local.entity.MoodEntryEntity

// ─────────────────────────────────────────────────────────────────────────────
// BloomDatabase
//
// Single Room database for the entire app.
// Version starts at 1 — future migrations will be handled with Migration
// objects rather than destructive fallback (user data must be preserved).
// ─────────────────────────────────────────────────────────────────────────────

@Database(
    entities        = [JournalEntryEntity::class, MoodEntryEntity::class],
    version         = 1,
    exportSchema    = true,   // Export schema for migration validation
)
abstract class BloomDatabase : RoomDatabase() {

    abstract fun journalEntryDao(): JournalEntryDao
    abstract fun moodEntryDao(): MoodEntryDao

    companion object {
        private const val DATABASE_NAME = "bloom_database"

        @Volatile
        private var INSTANCE: BloomDatabase? = null

        fun getInstance(context: Context): BloomDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    BloomDatabase::class.java,
                    DATABASE_NAME,
                )
                    // Do NOT use fallbackToDestructiveMigration — user journal
                    // data is precious and must never be silently wiped.
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
