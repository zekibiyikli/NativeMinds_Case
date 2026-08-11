package com.zekibiyikli.nativemindscase.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SampleEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun sampleDao(): SampleDao

    companion object {
        const val NAME = "nativeminds.db"
    }
}
