package com.cartoniq.buildiq.quotation

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.cartoniq.buildiq.SettingsFragment
import com.cartoniq.buildiq.data.ProjectEntity
import com.cartoniq.buildiq.model.Calculators
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object QuoteGenerator {

    data class CompanyInfo(
        val companyName: String = "BuildIQ Contracting",
        val contractorName: String = "Estimator",
        val phone: String = "(555) 019-2834",
        val email: String = "estimates@buildiq.pro",
        val licenseNumber: String = "LIC #CON-84729"
    )

    fun getCompanyInfo(context: Context): CompanyInfo {
        val prefs = context.getSharedPreferences(SettingsFragment.PREFS, Context.MODE_PRIVATE)
        return CompanyInfo(
            companyName = prefs.getString("company_name", "BuildIQ Contracting") ?: "BuildIQ Contracting",
            contractorName = prefs.getString("contractor_name", "Estimator") ?: "Estimator",
            phone = prefs.getString("company_phone", "(555) 019-2834") ?: "(555) 019-2834",
            email = prefs.getString("company_email", "estimates@buildiq.pro") ?: "estimates@buildiq.pro",
            licenseNumber = prefs.getString("company_license", "LIC #CON-84729") ?: "LIC #CON-84729"
        )
    }

    fun generatePlainTextEstimate(context: Context, project: ProjectEntity): String {
        val company = getCompanyInfo(context)
        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
        val dateStr = dateFormat.format(Date(project.createdAt))
        val cur = project.currencySymbol

        val sb = StringBuilder()
        sb.appendLine("========================================")
        sb.appendLine(company.companyName.uppercase(Locale.US))
        sb.appendLine("${company.contractorName} | ${company.licenseNumber}")
        sb.appendLine("Tel: ${company.phone} | ${company.email}")
        sb.appendLine("========================================")
        sb.appendLine("PROJECT ESTIMATE & QUOTATION")
        sb.appendLine("Date: $dateStr")
        sb.appendLine("Project: ${project.title}")
        if (project.clientName.isNotBlank()) sb.appendLine("Client: ${project.clientName}")
        if (project.clientAddress.isNotBlank()) sb.appendLine("Site Address: ${project.clientAddress}")
        if (project.clientPhone.isNotBlank()) sb.appendLine("Phone: ${project.clientPhone}")
        if (project.clientEmail.isNotBlank()) sb.appendLine("Email: ${project.clientEmail}")
        sb.appendLine("----------------------------------------")
        sb.appendLine("ITEMIZED SCOPE & MATERIALS:")

        val items = project.getItems()
        if (items.isEmpty()) {
            sb.appendLine("• ${project.summary}")
            sb.appendLine("  Estimated Cost: ${Calculators.money(project.totalCost, cur)}")
        } else {
            items.forEachIndexed { index, item ->
                sb.appendLine("${index + 1}. ${item.title}")
                sb.appendLine("   Result: ${item.headline} (${item.label})")
                item.details.lines().filter { it.isNotBlank() }.forEach { line ->
                    sb.appendLine("   - $line")
                }
                sb.appendLine("   Material Subtotal: ${Calculators.money(item.materialCost, cur)}")
                sb.appendLine()
            }
        }

        val materialsSubtotal = items.sumOf { it.materialCost }.let { if (it > 0) it else project.totalCost }
        sb.appendLine("----------------------------------------")
        sb.appendLine("FINANCIAL BREAKDOWN:")
        sb.appendLine("Materials Subtotal:      ${Calculators.money(materialsSubtotal, cur)}")

        if (project.laborCost > 0) {
            val laborDesc = if (project.laborType.equals("HOURLY", true)) {
                " (${project.laborHours} hrs @ ${Calculators.money(project.laborRate, cur)}/hr)"
            } else " (Flat rate)"
            sb.appendLine("Labor$laborDesc: ${Calculators.money(project.laborCost, cur)}")
        }

        if (project.contingencyAmount > 0) {
            sb.appendLine("Contingency (${Calculators.f1(project.contingencyRate)}%):    ${Calculators.money(project.contingencyAmount, cur)}")
        }

        if (project.markupAmount > 0) {
            sb.appendLine("Markup / Margin (${Calculators.f1(project.markupRate)}%):${Calculators.money(project.markupAmount, cur)}")
        }

        if (project.taxAmount > 0) {
            sb.appendLine("Sales Tax (${Calculators.f1(project.taxRate)}%):        ${Calculators.money(project.taxAmount, cur)}")
        }

        sb.appendLine("========================================")
        sb.appendLine("ESTIMATED GRAND TOTAL:   ${Calculators.money(project.totalCost, cur)}")
        sb.appendLine("========================================")

        if (project.notes.isNotBlank()) {
            sb.appendLine("NOTES & TERMS:")
            sb.appendLine(project.notes)
            sb.appendLine()
        }

        sb.appendLine("Estimate valid for 30 days. Material pricing subject to market variance.")
        return sb.toString()
    }

    fun generatePdfQuote(context: Context, project: ProjectEntity): File {
        val company = getCompanyInfo(context)
        val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.US)
        val cur = project.currencySymbol

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 at 72dpi
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(25, 118, 210) // BuildIQ Blue
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val headerPaint = Paint().apply {
            color = Color.rgb(33, 33, 33)
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.rgb(66, 66, 66)
            textSize = 9.5f
            isAntiAlias = true
        }

        val mutedPaint = Paint().apply {
            color = Color.rgb(120, 120, 120)
            textSize = 8.5f
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.rgb(220, 224, 230)
            strokeWidth = 1f
        }

        val cardBgPaint = Paint().apply {
            color = Color.rgb(245, 247, 250)
        }

        var y = 45f

        // Brand Banner Header
        canvas.drawText(company.companyName, 40f, y, titlePaint)
        canvas.drawText("PROJECT ESTIMATE", 420f, y, titlePaint)
        y += 18f
        canvas.drawText("${company.contractorName} • ${company.licenseNumber}", 40f, y, mutedPaint)
        canvas.drawText("Date: ${dateFormat.format(Date(project.createdAt))}", 420f, y, mutedPaint)
        y += 14f
        canvas.drawText("Tel: ${company.phone} | ${company.email}", 40f, y, mutedPaint)
        canvas.drawText("Estimate #: EST-${project.id}", 420f, y, mutedPaint)
        y += 20f

        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 18f

        // Client & Site Card
        canvas.drawRect(40f, y, 555f, y + 54f, cardBgPaint)
        canvas.drawText("CLIENT & JOB SITE INFORMATION:", 50f, y + 16f, headerPaint)
        val clientLine1 = if (project.clientName.isNotBlank()) project.clientName else "Valued Client"
        val clientLine2 = if (project.clientAddress.isNotBlank()) "Site: ${project.clientAddress}" else "Project: ${project.title}"
        canvas.drawText(clientLine1, 50f, y + 32f, bodyPaint)
        canvas.drawText(clientLine2, 50f, y + 46f, bodyPaint)
        if (project.clientPhone.isNotBlank() || project.clientEmail.isNotBlank()) {
            canvas.drawText("Contact: ${project.clientPhone} ${project.clientEmail}", 300f, y + 32f, bodyPaint)
        }
        y += 72f

        // Items Table Header
        canvas.drawRect(40f, y, 555f, y + 20f, cardBgPaint)
        canvas.drawText("ITEM / SCOPE OF WORK", 50f, y + 14f, headerPaint)
        canvas.drawText("DETAILS / QUANTITY", 260f, y + 14f, headerPaint)
        canvas.drawText("SUBTOTAL", 480f, y + 14f, headerPaint)
        y += 26f

        val items = project.getItems()
        if (items.isEmpty()) {
            canvas.drawText(project.title, 50f, y, bodyPaint)
            canvas.drawText(project.summary, 260f, y, bodyPaint)
            canvas.drawText(Calculators.money(project.totalCost, cur), 480f, y, bodyPaint)
            y += 20f
        } else {
            for (item in items) {
                if (y > 680f) break // protect page boundary
                canvas.drawText(item.title, 50f, y, headerPaint)
                canvas.drawText("${item.headline} (${item.label})", 260f, y, bodyPaint)
                canvas.drawText(Calculators.money(item.materialCost, cur), 480f, y, bodyPaint)
                y += 14f

                // draw details snippet
                val firstDetail = item.details.lines().firstOrNull { it.isNotBlank() } ?: ""
                canvas.drawText(firstDetail, 50f, y, mutedPaint)
                y += 18f
                canvas.drawLine(40f, y - 6f, 555f, y - 6f, linePaint)
            }
        }

        y += 10f

        // Financial summary block
        val startSummaryY = y
        val materialsSubtotal = items.sumOf { it.materialCost }.let { if (it > 0) it else project.totalCost }

        canvas.drawText("Materials Subtotal:", 320f, y, bodyPaint)
        canvas.drawText(Calculators.money(materialsSubtotal, cur), 480f, y, bodyPaint)
        y += 15f

        if (project.laborCost > 0) {
            val laborLabel = if (project.laborType.equals("HOURLY", true)) "Labor (${project.laborHours}h):" else "Labor (Flat):"
            canvas.drawText(laborLabel, 320f, y, bodyPaint)
            canvas.drawText(Calculators.money(project.laborCost, cur), 480f, y, bodyPaint)
            y += 15f
        }

        if (project.contingencyAmount > 0) {
            canvas.drawText("Contingency (${Calculators.f1(project.contingencyRate)}%):", 320f, y, bodyPaint)
            canvas.drawText(Calculators.money(project.contingencyAmount, cur), 480f, y, bodyPaint)
            y += 15f
        }

        if (project.markupAmount > 0) {
            canvas.drawText("Markup / Margin (${Calculators.f1(project.markupRate)}%):", 320f, y, bodyPaint)
            canvas.drawText(Calculators.money(project.markupAmount, cur), 480f, y, bodyPaint)
            y += 15f
        }

        if (project.taxAmount > 0) {
            canvas.drawText("Tax (${Calculators.f1(project.taxRate)}%):", 320f, y, bodyPaint)
            canvas.drawText(Calculators.money(project.taxAmount, cur), 480f, y, bodyPaint)
            y += 15f
        }

        canvas.drawLine(320f, y - 2f, 555f, y - 2f, linePaint)
        y += 14f

        val totalPaint = Paint().apply {
            color = Color.rgb(46, 125, 50) // Forest green
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("ESTIMATED TOTAL:", 320f, y, totalPaint)
        canvas.drawText(Calculators.money(project.totalCost, cur), 460f, y, totalPaint)

        // Notes and terms
        y = maxOf(y + 35f, startSummaryY + 95f)
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 16f

        canvas.drawText("TERMS & ACCEPTANCE:", 40f, y, headerPaint)
        y += 14f
        val noteText = if (project.notes.isNotBlank()) project.notes else "All estimates valid for 30 days. Material pricing subject to market changes."
        canvas.drawText(noteText, 40f, y, bodyPaint)
        y += 35f

        // Signature Lines
        canvas.drawLine(40f, y + 20f, 220f, y + 20f, linePaint)
        canvas.drawText("Contractor Signature & Date", 40f, y + 34f, mutedPaint)

        canvas.drawLine(340f, y + 20f, 520f, y + 20f, linePaint)
        canvas.drawText("Client Acceptance Signature & Date", 340f, y + 34f, mutedPaint)

        document.finishPage(page)

        // Write to file in cache directory
        val dir = File(context.cacheDir, "estimates")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "Estimate_${project.id}_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
        return file
    }

    fun shareProject(context: Context, project: ProjectEntity, asPdf: Boolean = true) {
        val intent = Intent(Intent.ACTION_SEND)
        if (asPdf) {
            val file = generatePdfQuote(context, project)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            intent.type = "application/pdf"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.putExtra(Intent.EXTRA_SUBJECT, "Estimate: ${project.title}")
            intent.putExtra(Intent.EXTRA_TEXT, generatePlainTextEstimate(context, project))
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_SUBJECT, "Estimate: ${project.title}")
            intent.putExtra(Intent.EXTRA_TEXT, generatePlainTextEstimate(context, project))
        }

        val chooser = Intent.createChooser(intent, "Share Estimate via")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
