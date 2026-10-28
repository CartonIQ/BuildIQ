package com.cartoniq.buildiq

import android.content.Context
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.cartoniq.buildiq.ads.AdManager
import com.cartoniq.buildiq.data.AppDatabase
import com.cartoniq.buildiq.data.ProjectEntity
import com.cartoniq.buildiq.databinding.FragmentCalcBinding
import com.cartoniq.buildiq.databinding.ItemInputBinding
import com.cartoniq.buildiq.model.CalcDef
import com.cartoniq.buildiq.model.CalcResult
import com.cartoniq.buildiq.model.CalculationItem
import com.cartoniq.buildiq.model.Calculators
import com.cartoniq.buildiq.model.UnitSystem
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.util.Locale

class CalculatorFragment : Fragment() {

    private var _binding: FragmentCalcBinding? = null
    private val binding get() = _binding!!

    private val projectsViewModel: ProjectsViewModel by viewModels()

    private val inputs = mutableMapOf<String, ItemInputBinding>()
    private var calc: CalcDef? = null
    private var lastResult: CalcResult? = null
    private var lastInputMap: Map<String, Double> = emptyMap()
    private var prices: Map<String, Double> = Calculators.defaultPrices()

    private var currentUnit: UnitSystem = UnitSystem.IMPERIAL
    private var currencySymbol: String = "$"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalcBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences(SettingsFragment.PREFS, Context.MODE_PRIVATE)
        val defaultUnitStr = prefs.getString("pref_unit_system", UnitSystem.IMPERIAL.name)
        currentUnit = UnitSystem.fromString(defaultUnitStr)
        currencySymbol = prefs.getString("pref_currency", "$") ?: "$"

        val key = arguments?.getString("calcType") ?: "paint"
        val def = Calculators.find(key) ?: Calculators.all.first()
        calc = def

        binding.calcTitle.text = def.title
        binding.calcSubtitle.text = def.subtitle

        if (currentUnit == UnitSystem.METRIC) {
            binding.unitToggleGroup.check(R.id.btnMetric)
        } else {
            binding.unitToggleGroup.check(R.id.btnImperial)
        }

        binding.unitToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                currentUnit = if (checkedId == R.id.btnMetric) UnitSystem.METRIC else UnitSystem.IMPERIAL
                updateInputLabels()
                if (lastResult != null) {
                    calculate(binding.calculateButton, silent = true)
                }
            }
        }

        binding.btnMeasurementHints.setOnClickListener {
            showMeasurementTipsDialog(def)
        }

        buildInputs(def)

        AdManager.loadBanner(binding.adView)

        viewLifecycleOwner.lifecycleScope.launch {
            prices = Calculators.loadPrices(requireContext())
        }

        binding.calculateButton.setOnClickListener { calculate(it) }
        binding.saveButton.setOnClickListener { showSaveNewProjectDialog() }
        binding.btnAddToProject.setOnClickListener { showAddToExistingProjectDialog() }
    }

    private fun buildInputs(def: CalcDef) {
        binding.inputContainer.removeAllViews()
        inputs.clear()

        for (field in def.fields) {
            val row = ItemInputBinding.inflate(LayoutInflater.from(requireContext()), binding.inputContainer, false)
            row.inputLayout.hint = field.getLabel(currentUnit)
            if (field.defaultValue > 0.0) {
                val formatted = if (field.isInteger) field.defaultValue.toInt().toString() else Calculators.f1(field.defaultValue)
                row.inputEdit.setText(formatted)
            }
            binding.inputContainer.addView(row.root)
            inputs[field.key] = row
        }
    }

    private fun updateInputLabels() {
        val def = calc ?: return
        for (field in def.fields) {
            val row = inputs[field.key] ?: continue
            row.inputLayout.hint = field.getLabel(currentUnit)
        }
    }

    private fun showMeasurementTipsDialog(def: CalcDef) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(def.title)
            .setMessage(def.measurementTips)
            .setPositiveButton("Got It", null)
            .show()
    }

    private fun calculate(button: View, silent: Boolean = false) {
        val def = calc ?: return
        val values = mutableMapOf<String, Double>()
        var valid = true

        for (field in def.fields) {
            val row = inputs[field.key] ?: continue
            val text = row.inputEdit.text?.toString()?.trim()
            val number = text?.toDoubleOrNull()
            if (number == null || number < field.min || number > field.max) {
                row.inputLayout.error = "Enter a valid number between ${field.min.toInt()} and ${field.max.toInt()}"
                valid = false
            } else {
                row.inputLayout.error = null
                values[field.key] = number
            }
        }
        if (!valid) return

        lastInputMap = values

        if (!silent) {
            button.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(button.windowToken, 0)
        }

        val result = def.compute(values, prices, currentUnit)
        lastResult = result

        binding.resultHeadline.text = result.headline
        binding.resultLabel.text = result.label
        binding.resultDetails.text = result.details
        val cost = result.cost
        if (cost != null) {
            binding.resultCost.visibility = View.VISIBLE
            binding.resultCost.text = "Estimated Material Cost: " + Calculators.money(cost, currencySymbol)
        } else {
            binding.resultCost.visibility = View.GONE
        }

        val card = binding.resultCard
        if (card.visibility != View.VISIBLE) {
            card.alpha = 0f
            card.translationY = 20f
            card.visibility = View.VISIBLE
            card.animate().alpha(1f).translationY(0f).setDuration(300).start()
            card.doOnLayout { binding.scrollView.smoothScrollTo(0, it.top) }
        }
    }

    private fun createCalculationItem(): CalculationItem? {
        val def = calc ?: return null
        val result = lastResult ?: return null
        return CalculationItem(
            calcKey = def.key,
            title = def.title,
            subtitle = def.subtitle,
            inputs = lastInputMap,
            unitSystem = currentUnit.name,
            headline = result.headline,
            label = result.label,
            details = result.details,
            materialCost = result.cost ?: 0.0
        )
    }

    private fun showSaveNewProjectDialog() {
        val def = calc ?: return
        val result = lastResult ?: return

        val input = EditText(requireContext()).apply {
            hint = "Project Name"
            setText(def.title)
            setSelection(text.length)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Save to Project")
            .setMessage("Enter project name to save this calculation:")
            .setView(input)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                val title = input.text.toString().trim().ifBlank { def.title }
                val item = createCalculationItem() ?: return@setPositiveButton

                viewLifecycleOwner.lifecycleScope.launch {
                    val project = ProjectEntity.createWithItems(
                        title = title,
                        summary = "${result.headline} (${result.label})",
                        items = listOf(item),
                        currencySymbol = currencySymbol
                    )
                    AppDatabase.get(requireContext()).projectDao().insertProject(project)
                    Toast.makeText(requireContext(), "Project '$title' saved!", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun showAddToExistingProjectDialog() {
        val item = createCalculationItem() ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            val allProjects = AppDatabase.get(requireContext()).projectDao().getAllProjects()
            if (allProjects.isEmpty()) {
                Toast.makeText(requireContext(), "No existing projects found. Save as a new project first!", Toast.LENGTH_LONG).show()
                return@launch
            }

            val projectTitles = allProjects.map { "${it.title} (${it.getItems().size} items)" }.toTypedArray()

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Combine with Project")
                .setItems(projectTitles) { _, which ->
                    val selected = allProjects[which]
                    projectsViewModel.addCalculationToProject(selected.id, item) {
                        Toast.makeText(requireContext(), "Added to '${selected.title}'!", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onDestroyView() {
        _binding?.adView?.destroy()
        super.onDestroyView()
        _binding = null
    }
}
