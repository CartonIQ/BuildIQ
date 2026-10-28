package com.cartoniq.buildiq

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.lifecycleScope
import com.cartoniq.buildiq.backup.BackupManager
import com.cartoniq.buildiq.data.AppDatabase
import com.cartoniq.buildiq.databinding.FragmentSettingsBinding
import com.cartoniq.buildiq.model.UnitSystem
import com.cartoniq.buildiq.quotation.QuoteGenerator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    companion object {
        const val PREFS = "buildiq_prefs"
        const val KEY_DARK = "dark_mode"
        const val KEY_UNIT_SYSTEM = "pref_unit_system"
        const val KEY_CURRENCY = "pref_currency"
    }

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        // Dark mode setup
        val uiMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        binding.darkModeSwitch.isChecked = uiMode == Configuration.UI_MODE_NIGHT_YES
        binding.darkModeSwitch.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean(KEY_DARK, checked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (checked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        // Unit system preference
        val unitSystem = prefs.getString(KEY_UNIT_SYSTEM, UnitSystem.IMPERIAL.name)
        if (unitSystem == UnitSystem.METRIC.name) {
            binding.defaultUnitToggleGroup.check(R.id.btnPrefMetric)
        } else {
            binding.defaultUnitToggleGroup.check(R.id.btnPrefImperial)
        }

        binding.defaultUnitToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val selected = if (checkedId == R.id.btnPrefMetric) UnitSystem.METRIC.name else UnitSystem.IMPERIAL.name
                prefs.edit().putString(KEY_UNIT_SYSTEM, selected).apply()
                Toast.makeText(requireContext(), "Default units updated", Toast.LENGTH_SHORT).show()
            }
        }

        // Currency
        val cur = prefs.getString(KEY_CURRENCY, "$") ?: "$"
        binding.prefCurrencyEdit.setText(cur)

        // Company Profile
        val company = QuoteGenerator.getCompanyInfo(requireContext())
        binding.prefCompanyName.setText(company.companyName)
        binding.prefContractorName.setText(company.contractorName)
        binding.prefCompanyPhone.setText(company.phone)
        binding.prefCompanyLicense.setText(company.licenseNumber)
        binding.prefCompanyEmail.setText(company.email)

        binding.btnSaveCompanyProfile.setOnClickListener {
            val newCur = binding.prefCurrencyEdit.text.toString().trim().ifBlank { "$" }
            prefs.edit()
                .putString(KEY_CURRENCY, newCur)
                .putString("company_name", binding.prefCompanyName.text.toString().trim())
                .putString("contractor_name", binding.prefContractorName.text.toString().trim())
                .putString("company_phone", binding.prefCompanyPhone.text.toString().trim())
                .putString("company_license", binding.prefCompanyLicense.text.toString().trim())
                .putString("company_email", binding.prefCompanyEmail.text.toString().trim())
                .apply()
            Toast.makeText(requireContext(), "Preferences & Company profile saved", Toast.LENGTH_SHORT).show()
        }

        // Export Data
        binding.btnExportBackup.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val file = BackupManager.exportBackupFile(requireContext())
                    BackupManager.shareBackup(requireContext(), file)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Import Data
        binding.btnImportBackup.setOnClickListener {
            showImportBackupDialog()
        }

        // Clear History
        binding.clearHistoryButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Clear History")
                .setMessage("Delete all saved projects? This cannot be undone.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete All") { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        AppDatabase.get(requireContext()).projectDao().clearProjects()
                        Toast.makeText(requireContext(), "Project history cleared", Toast.LENGTH_SHORT).show()
                    }
                }
                .show()
        }

        binding.rateButton.setOnClickListener {
            val pkg = requireContext().packageName
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")))
            } catch (_: Exception) {
                openUrl("https://play.google.com/store/apps/details?id=$pkg")
            }
        }

        binding.privacyButton.setOnClickListener {
            
            
            val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            dialog.setTitle("Privacy Policy")
            val policyText = "Last updated: October 2026\n\n" +
                "CartoniQ (\"we,\" \"our,\" or \"us\") operates the BuildIQ mobile application. This Privacy Policy explains how we handle your information when you use our App.\n\n" +
                "1. INFORMATION WE COLLECT\n" +
                "BuildIQ is a 100% offline tool. We do NOT collect, store, or share any personal information (such as your name, email, phone number, or location) that you enter into the app. All project data, calculations, and custom prices are stored locally on your device and are never transmitted to our servers.\n\n" +
                "2. THIRD-PARTY SERVICES (ADVERTISEMENTS)\n" +
                "The App uses Google AdMob to display advertisements. Google AdMob may collect certain information used to identify you, such as your device's advertising ID, to serve personalized ads. You can learn more about how Google uses your data by visiting: https://policies.google.com/technologies/ads\n\n" +
                "3. DATA STORAGE AND SECURITY\n" +
                "All data you create (projects, quotes, prices) is stored locally on your device using an encrypted local database. We do not have access to this data. If you export a PDF quote, you are solely responsible for where you share it (e.g., via email or WhatsApp).\n\n" +
                "4. CHILDREN'S PRIVACY\n" +
                "Our App does not address anyone under the age of 13. We do not knowingly collect personally identifiable information from children under 13. If you are a parent or guardian and you are aware that your child has provided us with personal information, please contact us so that we can take necessary actions.\n\n" +
                "5. LIMITATION OF LIABILITY\n" +
                "To the maximum extent permitted by applicable law, CartoniQ shall NOT be liable for any indirect, incidental, special, consequential, or punitive damages, or any loss of profits or revenues, whether incurred directly or indirectly, or any loss of data, use, goodwill, or other intangible losses, resulting from (a) your use or inability to use the App; (b) any inaccuracies in calculations or estimates provided by the App; or (c) any unauthorized access, use, or alteration of your transmissions or content. The App is provided \"AS IS\" without warranty of any kind.\n\n" +
                "6. CHANGES TO THIS POLICY\n" +
                "We may update our Privacy Policy from time to time. Thus, you are advised to review this screen periodically for any changes. We will notify you of any changes by posting the new Privacy Policy on this screen.\n\n" +
                "7. CONTACT US\n" +
                "If you have any questions or suggestions about our Privacy Policy, do not hesitate to contact us at: cartoniq.business@gmail.com"

            dialog.setMessage(policyText)
            dialog.setPositiveButton("OK") { d, _ -> d.dismiss() }
            dialog.show()


        }
    }

    private fun showImportBackupDialog() {
        val input = EditText(requireContext()).apply {
            hint = "Paste JSON backup content here"
            isSingleLine = false
            val lines = 5
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Import Data Backup")
            .setMessage("Paste exported JSON data below. You can choose to merge with existing data or replace all.")
            .setView(input)
            .setNeutralButton("Cancel", null)
            .setNegativeButton("Merge") { _, _ ->
                val json = input.text.toString().trim()
                performRestore(json, replace = false)
            }
            .setPositiveButton("Replace All") { _, _ ->
                val json = input.text.toString().trim()
                performRestore(json, replace = true)
            }
            .show()
    }

    private fun performRestore(json: String, replace: Boolean) {
        if (json.isBlank()) {
            Toast.makeText(requireContext(), "Please paste backup JSON", Toast.LENGTH_SHORT).show()
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            val result = BackupManager.restoreFromJson(requireContext(), json, replaceExisting = replace)
            if (result.success) {
                Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), "Error: ${result.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: Exception) {
            Toast.makeText(requireContext(), "No browser application found to open link", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
