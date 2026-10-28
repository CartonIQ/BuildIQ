package com.cartoniq.buildiq.backup

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.cartoniq.buildiq.SettingsFragment
import com.cartoniq.buildiq.data.AppDatabase
import com.cartoniq.buildiq.data.PriceEntity
import com.cartoniq.buildiq.data.ProjectEntity
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupPayload(
    val version: Int = 2,
    val app: String = "BuildIQ",
    val exportedAt: Long = System.currentTimeMillis(),
    val projects: List<ProjectEntity> = emptyList(),
    val prices: List<PriceEntity> = emptyList(),
    val settings: Map<String, String> = emptyMap()
)

data class ImportResult(
    val success: Boolean,
    val message: String,
    val projectsImported: Int = 0,
    val pricesImported: Int = 0
)

object BackupManager {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun createBackupPayload(context: Context): BackupPayload {
        val dao = AppDatabase.get(context).projectDao()
        val projects = dao.getAllProjects()
        val prices = dao.getPrices()

        val prefs = context.getSharedPreferences(SettingsFragment.PREFS, Context.MODE_PRIVATE)
        val settingsMap = mutableMapOf<String, String>()
        for ((k, v) in prefs.all) {
            settingsMap[k] = v?.toString() ?: ""
        }

        return BackupPayload(
            version = 2,
            app = "BuildIQ",
            exportedAt = System.currentTimeMillis(),
            projects = projects,
            prices = prices,
            settings = settingsMap
        )
    }

    suspend fun exportBackupFile(context: Context): File {
        val payload = createBackupPayload(context)
        val json = gson.toJson(payload)

        val dir = File(context.cacheDir, "backups")
        if (!dir.exists()) dir.mkdirs()

        val dateTag = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(dir, "BuildIQ_Backup_$dateTag.json")
        FileOutputStream(file).use { it.write(json.toByteArray(Charsets.UTF_8)) }
        return file
    }

    fun shareBackup(context: Context, backupFile: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", backupFile)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "BuildIQ Data Backup")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Export Backup to")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    suspend fun restoreFromJson(context: Context, json: String, replaceExisting: Boolean = false): ImportResult {
        return try {
            val payload = gson.fromJson(json, BackupPayload::class.java)
                ?: return ImportResult(false, "Invalid or corrupted backup file")

            if (payload.app != "BuildIQ") {
                return ImportResult(false, "Not a valid BuildIQ backup file")
            }

            val dao = AppDatabase.get(context).projectDao()

            if (replaceExisting) {
                dao.clearProjects()
                dao.clearPrices()
            }

            var projectCount = 0
            for (p in payload.projects) {
                // If merging, avoid duplicating exact ID by inserting with new ID if conflict
                val toInsert = if (replaceExisting) p else p.copy(id = 0)
                dao.insertProject(toInsert)
                projectCount++
            }

            var priceCount = 0
            for (pr in payload.prices) {
                dao.upsertPrice(pr)
                priceCount++
            }

            // Restore settings if present
            if (payload.settings.isNotEmpty()) {
                val editor = context.getSharedPreferences(SettingsFragment.PREFS, Context.MODE_PRIVATE).edit()
                for ((k, v) in payload.settings) {
                    if (k == SettingsFragment.KEY_DARK) {
                        editor.putBoolean(k, v.toBoolean())
                    } else {
                        editor.putString(k, v)
                    }
                }
                editor.apply()
            }

            ImportResult(
                success = true,
                message = "Successfully restored $projectCount projects and $priceCount prices",
                projectsImported = projectCount,
                pricesImported = priceCount
            )
        } catch (e: Exception) {
            ImportResult(false, "Failed to restore backup: ${e.localizedMessage ?: "Unknown error"}")
        }
    }
}
