package com.cartoniq.buildiq.model

enum class UnitSystem(val displayName: String) {
    IMPERIAL("Imperial (ft/in/gal)"),
    METRIC("Metric (m/cm/L)");

    companion object {
        fun fromString(str: String?): UnitSystem {
            return if (str.equals("METRIC", ignoreCase = true)) METRIC else IMPERIAL
        }

        // Unit conversion factors
        const val FT_TO_M = 0.3048
        const val M_TO_FT = 3.28084
        const val IN_TO_CM = 2.54
        const val CM_TO_IN = 0.393701
        const val SQFT_TO_SQM = 0.092903
        const val SQM_TO_SQFT = 10.7639
        const val CUFT_TO_CUM = 0.0283168
        const val CUM_TO_CUFT = 35.3147
        const val CUYD_TO_CUM = 0.764555
        const val CUM_TO_CUYD = 1.30795
        const val GAL_TO_L = 3.78541
        const val L_TO_GAL = 0.264172
        const val LBS_TO_KG = 0.453592
        const val KG_TO_LBS = 2.20462
    }
}
