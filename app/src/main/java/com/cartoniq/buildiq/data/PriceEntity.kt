package com.cartoniq.buildiq.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prices")
data class PriceEntity(
    @PrimaryKey val key: String,
    val value: Double,
    val label: String = "",
    val unit: String = "",
    val category: String = "",
    val isCustom: Boolean = false
)
