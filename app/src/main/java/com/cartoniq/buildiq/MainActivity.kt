package com.cartoniq.buildiq

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.cartoniq.buildiq.ads.AdManager
import com.cartoniq.buildiq.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences(SettingsFragment.PREFS, MODE_PRIVATE)
        if (prefs.contains(SettingsFragment.KEY_DARK)) {
            val dark = prefs.getBoolean(SettingsFragment.KEY_DARK, false)
            AppCompatDelegate.setDefaultNightMode(
                if (dark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Ads and UMP Consent
        AdManager.init(this)

        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        NavigationUI.setupWithNavController(binding.bottomNav, navHost.navController)
    }
}
