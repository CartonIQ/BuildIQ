package com.cartoniq.buildiq.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

object AdManager {
    private const val TAG = "BuildIQ_Ads"
    const val BANNER_UNIT_ID = "ca-app-pub-9771662959477951/5442547210"

    private var initialized = false
    private var consentInformation: ConsentInformation? = null

    fun init(activity: Activity) {
        if (initialized) return

        // Set up UMP consent request parameters
        val debugSettings = ConsentDebugSettings.Builder(activity)
            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_DISABLED)
            .build()

        val params = ConsentRequestParameters.Builder()
            .setConsentDebugSettings(debugSettings)
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation?.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.w(TAG, "Consent form error: ${formError.message}")
                    }
                    if (consentInformation?.canRequestAds() == true) {
                        initializeMobileAds(activity)
                    }
                }
            },
            { requestConsentError ->
                Log.w(TAG, "Consent request error: ${requestConsentError.message}")
                initializeMobileAds(activity)
            }
        )

        // If consent was previously granted, init MobileAds immediately
        if (consentInformation?.canRequestAds() == true) {
            initializeMobileAds(activity)
        }
    }

    private fun initializeMobileAds(context: Context) {
        if (!initialized) {
            MobileAds.initialize(context.applicationContext) {
                initialized = true
                Log.d(TAG, "MobileAds initialized")
            }
        }
    }

    fun loadBanner(adView: AdView?) {
        if (adView == null) return
        try {
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load banner: ${e.message}")
        }
    }
}
