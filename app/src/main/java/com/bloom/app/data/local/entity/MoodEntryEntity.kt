package com.bloom.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_entries")
data class MoodEntryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id         : Long   = 0,

    @ColumnInfo(name = "mood")
    val mood       : String,

    @ColumnInfo(name = "note")
    val note       : String? = null,

    @ColumnInfo(name = "recorded_at")
    val recordedAt : Long,
)
