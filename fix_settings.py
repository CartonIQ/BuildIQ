import re

file_path = "app/src/main/java/com/cartoniq/buildiq/SettingsFragment.kt"
with open(file_path, "r") as f:
    content = f.read()

# 1. Update the bindings setup (inside onCreateView or onViewCreated)
setup_code = """
        val currencyOptions = arrayOf("$", "₹")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, currencyOptions)
        binding.prefCurrencyDropdown.setAdapter(adapter)
        binding.prefCurrencyDropdown.setOnItemClickListener { _, _, position, _ ->
            val selected = currencyOptions[position]
            binding.exchangeRateLayout.visibility = if (selected == "₹") android.view.View.VISIBLE else android.view.View.GONE
            prefs.edit().putString(KEY_CURRENCY, selected).apply()
        }
        binding.prefCurrencyDropdown.setText(prefs.getString(KEY_CURRENCY, "$"), false)
        binding.exchangeRateLayout.visibility = if (prefs.getString(KEY_CURRENCY, "$") == "₹") android.view.View.VISIBLE else android.view.View.GONE
        
        binding.prefExchangeRateEdit.setText(prefs.getString(KEY_EXCHANGE_RATE, "95"))
        binding.prefExchangeRateEdit.doAfterTextChanged {
            prefs.edit().putString(KEY_EXCHANGE_RATE, it?.toString() ?: "95").apply()
        }
"""

# Find a place to insert it (after binding is initialized)
content = content.replace("super.onViewCreated(view, savedInstanceState)", "super.onViewCreated(view, savedInstanceState)\n" + setup_code)

# 2. Add the KEY_EXCHANGE_RATE constant if missing
if "KEY_EXCHANGE_RATE" not in content:
    content = content.replace('const val KEY_CURRENCY = "currency"', 'const val KEY_CURRENCY = "currency"\n    const val KEY_EXCHANGE_RATE = "exchange_rate"')

with open(file_path, "w") as f:
    f.write(content)

print("Settings updated successfully!")
