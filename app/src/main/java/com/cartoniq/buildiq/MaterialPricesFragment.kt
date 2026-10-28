package com.cartoniq.buildiq

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.cartoniq.buildiq.data.AppDatabase
import com.cartoniq.buildiq.data.PriceEntity
import com.cartoniq.buildiq.databinding.DialogAddPriceBinding
import com.cartoniq.buildiq.databinding.FragmentMaterialPricesBinding
import com.cartoniq.buildiq.databinding.ItemPriceRowBinding
import com.cartoniq.buildiq.model.Calculators
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

class MaterialPricesFragment : Fragment() {

    private var _binding: FragmentMaterialPricesBinding? = null
    private val binding get() = _binding!!

    private val standardRows = mutableMapOf<String, ItemPriceRowBinding>()
    private val customRows = mutableMapOf<String, Pair<PriceEntity, ItemPriceRowBinding>>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMaterialPricesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Populate standard price rows
        binding.pricesContainer.removeAllViews()
        standardRows.clear()

        for (def in Calculators.priceDefs) {
            val row = ItemPriceRowBinding.inflate(LayoutInflater.from(requireContext()), binding.pricesContainer, false)
            row.priceInputLayout.hint = "${def.label} (${def.unitImperial})"
            binding.pricesContainer.addView(row.root)
            standardRows[def.key] = row
        }

        binding.btnAddCustomPrice.setOnClickListener {
            showAddCustomPriceDialog()
        }

        binding.btnResetDefaults.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Reset to Defaults")
                .setMessage("Restore all standard material rates to factory defaults?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Reset") { _, _ ->
                    val defaults = Calculators.defaultPrices()
                    for ((key, row) in standardRows) {
                        val defVal = defaults[key] ?: 0.0
                        row.priceEdit.setText(String.format(Locale.US, "%.2f", defVal))
                    }
                    Toast.makeText(requireContext(), "Standard prices reset to defaults", Toast.LENGTH_SHORT).show()
                }
                .show()
        }

        loadSavedPrices()

        binding.savePricesButton.setOnClickListener {
            saveAllPrices()
        }
    }

    private fun loadSavedPrices() {
        viewLifecycleOwner.lifecycleScope.launch {
            val dao = AppDatabase.get(requireContext()).projectDao()
            val savedList = dao.getPrices()
            val savedMap = savedList.associateBy { it.key }

            // Fill standard rows
            val defaults = Calculators.defaultPrices()
            for ((key, row) in standardRows) {
                val value = savedMap[key]?.value ?: defaults[key] ?: 0.0
                row.priceEdit.setText(String.format(Locale.US, "%.2f", value))
            }

            // Fill custom rows
            binding.customPricesContainer.removeAllViews()
            customRows.clear()

            val customList = savedList.filter { it.isCustom }
            if (customList.isNotEmpty()) {
                binding.customMaterialsHeader.visibility = View.VISIBLE
                for (custom in customList) {
                    addCustomRowToUi(custom)
                }
            } else {
                binding.customMaterialsHeader.visibility = View.GONE
            }
        }
    }

    private fun addCustomRowToUi(priceEntity: PriceEntity) {
        val row = ItemPriceRowBinding.inflate(LayoutInflater.from(requireContext()), binding.customPricesContainer, false)
        row.priceInputLayout.hint = "${priceEntity.label} (${priceEntity.unit})"
        row.priceEdit.setText(String.format(Locale.US, "%.2f", priceEntity.value))
        row.btnDeletePrice.visibility = View.VISIBLE
        row.btnDeletePrice.setOnClickListener {
            deleteCustomPrice(priceEntity.key)
        }
        binding.customPricesContainer.addView(row.root)
        customRows[priceEntity.key] = Pair(priceEntity, row)
        binding.customMaterialsHeader.visibility = View.VISIBLE
    }

    private fun showAddCustomPriceDialog() {
        val dialogBinding = DialogAddPriceBinding.inflate(LayoutInflater.from(requireContext()))
        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Add") { _, _ ->
                val label = dialogBinding.addPriceLabel.text.toString().trim()
                val unit = dialogBinding.addPriceUnit.text.toString().trim().ifBlank { "unit" }
                val value = dialogBinding.addPriceValue.text.toString().toDoubleOrNull() ?: 0.0

                if (label.isBlank() || value <= 0.0) {
                    Toast.makeText(requireContext(), "Enter valid label and price", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val key = "custom_" + UUID.randomUUID().toString().take(8)
                val newEntity = PriceEntity(
                    key = key,
                    value = value,
                    label = label,
                    unit = unit,
                    category = "CUSTOM",
                    isCustom = true
                )

                viewLifecycleOwner.lifecycleScope.launch {
                    AppDatabase.get(requireContext()).projectDao().upsertPrice(newEntity)
                    addCustomRowToUi(newEntity)
                    Toast.makeText(requireContext(), "Added custom material '$label'", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun deleteCustomPrice(key: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val dao = AppDatabase.get(requireContext()).projectDao()
            dao.deletePrice(key)
            val pair = customRows.remove(key)
            if (pair != null) {
                binding.customPricesContainer.removeView(pair.second.root)
            }
            if (customRows.isEmpty()) {
                binding.customMaterialsHeader.visibility = View.GONE
            }
            Toast.makeText(requireContext(), "Custom price removed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveAllPrices() {
        viewLifecycleOwner.lifecycleScope.launch {
            val dao = AppDatabase.get(requireContext()).projectDao()

            // Save standard prices
            for ((key, row) in standardRows) {
                val value = row.priceEdit.text?.toString()?.trim()?.toDoubleOrNull()
                if (value != null && value >= 0.0) {
                    val def = Calculators.priceDefs.firstOrNull { it.key == key }
                    dao.upsertPrice(
                        PriceEntity(
                            key = key,
                            value = value,
                            label = def?.label ?: key,
                            unit = def?.unitImperial ?: "",
                            category = def?.category?.name ?: "",
                            isCustom = false
                        )
                    )
                }
            }

            // Save custom prices
            for ((key, pair) in customRows) {
                val value = pair.second.priceEdit.text?.toString()?.trim()?.toDoubleOrNull()
                if (value != null && value >= 0.0) {
                    dao.upsertPrice(pair.first.copy(value = value))
                }
            }

            Toast.makeText(requireContext(), "Price catalog saved successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        standardRows.clear()
        customRows.clear()
        _binding = null
    }
}
