package com.cartoniq.buildiq

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cartoniq.buildiq.databinding.FragmentDashboardBinding
import com.cartoniq.buildiq.databinding.ItemDashboardCardBinding
import com.cartoniq.buildiq.model.CalcCategory
import com.cartoniq.buildiq.model.CalcDef
import com.cartoniq.buildiq.model.Calculators

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private var selectedCategory: CalcCategory? = null
    private var searchQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup Onboarding dismiss
        val prefs = requireContext().getSharedPreferences(SettingsFragment.PREFS, Context.MODE_PRIVATE)
        val dismissedOnboarding = prefs.getBoolean("dismissed_onboarding", false)
        binding.onboardingCard.visibility = if (dismissedOnboarding) View.GONE else View.VISIBLE
        binding.btnDismissOnboarding.setOnClickListener {
            prefs.edit().putBoolean("dismissed_onboarding", true).apply()
            binding.onboardingCard.visibility = View.GONE
        }

        binding.btnQuickPrices.setOnClickListener {
            findNavController().navigate(R.id.materialPricesFragment)
        }

        // Set column count based on screen size
        val cols = resources.getInteger(R.integer.dashboard_grid_columns)
        binding.calcGrid.columnCount = cols

        // Search edit listener
        binding.searchEdit.doAfterTextChanged {
            searchQuery = it?.toString()?.trim() ?: ""
            refreshGrid()
        }

        // Chip group listener
        binding.categoryChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            selectedCategory = when (checkedIds.firstOrNull()) {
                R.id.chipSurfaces -> CalcCategory.SURFACES
                R.id.chipFlooring -> CalcCategory.FLOORING
                R.id.chipFraming -> CalcCategory.FRAMING
                R.id.chipRoofing -> CalcCategory.ROOFING_DECK
                R.id.chipLandscaping -> CalcCategory.LANDSCAPING
                else -> null
            }
            refreshGrid()
        }

        refreshGrid()
    }

    private fun refreshGrid() {
        binding.calcGrid.removeAllViews()
        val margin = (8 * resources.displayMetrics.density).toInt()

        val filtered = Calculators.all.filter { def ->
            val matchCategory = selectedCategory == null || def.category == selectedCategory
            val matchSearch = searchQuery.isBlank() ||
                    def.title.contains(searchQuery, ignoreCase = true) ||
                    def.subtitle.contains(searchQuery, ignoreCase = true) ||
                    def.key.contains(searchQuery, ignoreCase = true)
            matchCategory && matchSearch
        }

        for (def in filtered) {
            addCard(def) {
                findNavController().navigate(R.id.calculatorFragment, bundleOf("calcType" to def.key))
            }
        }
    }

    private fun addCard(def: CalcDef, onClick: () -> Unit) {
        val card = ItemDashboardCardBinding.inflate(LayoutInflater.from(requireContext()), binding.calcGrid, false)
        card.cardTitle.text = def.title
        card.cardSubtitle.text = def.subtitle
        card.cardIcon.setImageResource(def.iconRes)
        card.root.setOnClickListener { onClick() }

        val lp = GridLayout.LayoutParams(
            GridLayout.spec(GridLayout.UNDEFINED, 1f),
            GridLayout.spec(GridLayout.UNDEFINED, 1f)
        ).apply {
            width = 0
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            val margin = (6 * resources.displayMetrics.density).toInt()
            setMargins(margin, margin, margin, margin)
        }
        binding.calcGrid.addView(card.root, lp)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
