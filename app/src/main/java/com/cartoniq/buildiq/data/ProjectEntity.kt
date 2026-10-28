package com.cartoniq.buildiq.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cartoniq.buildiq.model.CalculationItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val summary: String,
    val totalCost: Double,
    val createdAt: Long,
    val clientName: String = "",
    val clientAddress: String = "",
    val clientPhone: String = "",
    val clientEmail: String = "",
    val notes: String = "",
    val laborType: String = "NONE",
    val laborRate: Double = 0.0,
    val laborHours: Double = 0.0,
    val laborCost: Double = 0.0,
    val taxRate: Double = 0.0,
    val taxAmount: Double = 0.0,
    val contingencyRate: Double = 0.0,
    val contingencyAmount: Double = 0.0,
    val markupRate: Double = 0.0,
    val markupAmount: Double = 0.0,
    val currencySymbol: String = "$",
    val itemsJson: String = "[]"
) {
    fun getItems(): List<CalculationItem> {
        return try {
            val type = object : TypeToken<List<CalculationItem>>() {}.type
            Gson().fromJson(itemsJson, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    companion object {
        fun createWithItems(
            id: Long = 0,
            title: String,
            summary: String,
            items: List<CalculationItem>,
            clientName: String = "",
            clientAddress: String = "",
            clientPhone: String = "",
            clientEmail: String = "",
            notes: String = "",
            laborType: String = "NONE",
            laborRate: Double = 0.0,
            laborHours: Double = 0.0,
            taxRate: Double = 0.0,
            contingencyRate: Double = 0.0,
            markupRate: Double = 0.0,
            currencySymbol: String = "$",
            createdAt: Long = System.currentTimeMillis()
        ): ProjectEntity {
            val materialsSubtotal = items.sumOf { it.materialCost }

            // Labor calculation
            val calculatedLabor = when (laborType.uppercase()) {
                "HOURLY" -> laborRate * laborHours
                "FLAT" -> laborRate
                else -> 0.0
            }

            val directCost = materialsSubtotal + calculatedLabor
            val contingencyAmt = directCost * (contingencyRate / 100.0)
            val subtotalWithContingency = directCost + contingencyAmt
            val markupAmt = subtotalWithContingency * (markupRate / 100.0)
            val taxableBase = subtotalWithContingency + markupAmt
            val taxAmt = taxableBase * (taxRate / 100.0)
            val grandTotal = taxableBase + taxAmt

            val json = Gson().toJson(items)

            return ProjectEntity(
                id = id,
                title = title,
                summary = summary,
                totalCost = grandTotal,
                createdAt = createdAt,
                clientName = clientName,
                clientAddress = clientAddress,
                clientPhone = clientPhone,
                clientEmail = clientEmail,
                notes = notes,
                laborType = laborType,
                laborRate = laborRate,
                laborHours = laborHours,
                laborCost = calculatedLabor,
                taxRate = taxRate,
                taxAmount = taxAmt,
                contingencyRate = contingencyRate,
                contingencyAmount = contingencyAmt,
                markupRate = markupRate,
                markupAmount = markupAmt,
                currencySymbol = currencySymbol,
                itemsJson = json
            )
        }
    }
}
