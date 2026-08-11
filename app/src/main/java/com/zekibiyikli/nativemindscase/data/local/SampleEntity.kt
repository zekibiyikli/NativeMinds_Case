package com.zekibiyikli.nativemindscase.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room kurulumunu ayakta tutan ornek tablo. Gercek domain modeli
 * belli oldugunda bu entity/DAO ikilisi onunla degistirilecek.
 */
@Entity(tableName = "sample_items")
data class SampleEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val imageUrl: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
