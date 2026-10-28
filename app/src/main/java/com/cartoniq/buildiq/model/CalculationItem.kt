package com.cartoniq.buildiq.model

import java.util.UUID

data class CalculationItem(
    val id: String = UUID.randomUUID().toString(),
    val calcKey: String,
    val title: String,
    val subtitle: String = "",
    val inputs: Map<String, Double> = emptyMap(),
    val unitSystem: String = UnitSystem.IMPERIAL.name,
    val headline: String,
    val label: String,
    val details: String,
    val materialCost: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
