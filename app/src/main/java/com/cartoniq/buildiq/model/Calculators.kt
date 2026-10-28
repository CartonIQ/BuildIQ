package com.cartoniq.buildiq.model

import android.content.Context
import com.cartoniq.buildiq.R
import com.cartoniq.buildiq.data.AppDatabase
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.sqrt

enum class CalcCategory(val title: String) {
    SURFACES("Surfaces & Paint"),
    FLOORING("Flooring & Tile"),
    FRAMING("Structure & Framing"),
    ROOFING_DECK("Roofing & Exterior"),
    LANDSCAPING("Landscaping & Earthwork")
}

data class InputField(
    val key: String,
    val labelImperial: String,
    val labelMetric: String,
    val hint: String = "",
    val defaultValue: Double = 0.0,
    val isInteger: Boolean = false,
    val min: Double = 0.0,
    val max: Double = 100000.0
) {
    fun getLabel(unitSystem: UnitSystem): String =
        if (unitSystem == UnitSystem.METRIC) labelMetric else labelImperial
}

data class PriceDef(
    val key: String,
    val label: String,
    val default: Double,
    val unitImperial: String,
    val unitMetric: String,
    val category: CalcCategory
)

data class CalcResult(
    val headline: String,
    val label: String,
    val details: String,
    val cost: Double?,
    val primaryQuantity: Double = 0.0,
    val primaryUnit: String = ""
)

class CalcDef(
    val key: String,
    val title: String,
    val subtitle: String,
    val category: CalcCategory,
    val iconRes: Int,
    val fields: List<InputField>,
    val measurementTips: String,
    val compute: (Map<String, Double>, Map<String, Double>, UnitSystem) -> CalcResult
)

object Calculators {

    fun f1(v: Double): String = String.format(Locale.US, "%.1f", v)
    fun f2(v: Double): String = String.format(Locale.US, "%.2f", v)

    fun money(v: Double, currency: String = "$"): String {
        return String.format(Locale.US, "%s%,.2f", currency, v)
    }

    private fun Map<String, Double>.v(key: String, fallback: Double = 0.0): Double =
        this[key] ?: fallback

    val priceDefs: List<PriceDef> = listOf(
        PriceDef("paint_gal", "Interior Paint", 38.0, "per gal", "per L", CalcCategory.SURFACES),
        PriceDef("primer_gal", "Primer / Sealer", 26.0, "per gal", "per L", CalcCategory.SURFACES),
        PriceDef("ceiling_paint_gal", "Ceiling Paint", 32.0, "per gal", "per L", CalcCategory.SURFACES),
        PriceDef("trim_molding_ft", "Trim / Casing Molding", 2.25, "per linear ft", "per linear m", CalcCategory.SURFACES),
        PriceDef("wallpaper_roll", "Wallpaper", 45.0, "per roll", "per roll", CalcCategory.SURFACES),
        PriceDef("flooring_sqft", "Hardwood / Laminate Flooring", 4.50, "per sq ft", "per sq m", CalcCategory.FLOORING),
        PriceDef("tile_sqft", "Ceramic / Porcelain Tile", 5.25, "per sq ft", "per sq m", CalcCategory.FLOORING),
        PriceDef("grout_bag", "Tile Grout (25 lb / 10 kg bag)", 22.0, "per bag", "per bag", CalcCategory.FLOORING),
        PriceDef("drywall_sheet", "Drywall Sheet (4x8 ft / 1.2x2.4 m)", 16.50, "per sheet", "per sheet", CalcCategory.FRAMING),
        PriceDef("board_foot", "Framing Lumber / Studs", 3.75, "per board ft", "per board ft", CalcCategory.FRAMING),
        PriceDef("roofing_bundle", "Asphalt Shingles (Bundle)", 36.0, "per bundle", "per bundle", CalcCategory.ROOFING_DECK),
        PriceDef("underlayment_roll", "Roofing Underlayment (Felt)", 48.0, "per roll", "per roll", CalcCategory.ROOFING_DECK),
        PriceDef("fence_picket", "Fence Picket (6 ft / 1.8 m)", 3.20, "each", "each", CalcCategory.ROOFING_DECK),
        PriceDef("fence_post", "Fence Post (4x4 8ft / 2.4 m)", 14.50, "each", "each", CalcCategory.ROOFING_DECK),
        PriceDef("deck_board_ft", "Decking Board (5/4x6 or Composite)", 3.80, "per linear ft", "per linear m", CalcCategory.ROOFING_DECK),
        PriceDef("mulch_cuyd", "Landscaping Mulch", 42.0, "per cu yd", "per cu m", CalcCategory.LANDSCAPING),
        PriceDef("gravel_cuyd", "Crushed Gravel / Stone", 58.0, "per cu yd", "per cu m", CalcCategory.LANDSCAPING),
        PriceDef("soil_cuyd", "Topsoil / Garden Soil", 36.0, "per cu yd", "per cu m", CalcCategory.LANDSCAPING),
        PriceDef("concrete_cuyd", "Ready-Mix Concrete", 145.0, "per cu yd", "per cu m", CalcCategory.LANDSCAPING),
        PriceDef("concrete_bag", "Quick-Set Concrete (50 lb / 25 kg bag)", 6.50, "per bag", "per bag", CalcCategory.LANDSCAPING)
    )

    fun defaultPrices(): Map<String, Double> = priceDefs.associate { it.key to it.default }

    suspend fun loadPrices(context: Context): Map<String, Double> {
        val result = defaultPrices().toMutableMap()
        try {
            for (saved in AppDatabase.get(context).projectDao().getPrices()) {
                result[saved.key] = saved.value
            }
        } catch (_: Exception) {
        }
        return result
    }

    val all: List<CalcDef> = listOf(
        // 1. Paint Calculator
        CalcDef(
            key = "paint",
            title = "Paint Calculator",
            subtitle = "Gallons/liters of wall paint with door & window deductions",
            category = CalcCategory.SURFACES,
            iconRes = R.drawable.ic_paint,
            measurementTips = "Measure room perimeter and ceiling height. Standard door is 20 sq ft (1.86 sq m), window is 15 sq ft (1.39 sq m). Standard gallon covers ~350 sq ft (8.6 sq m/L).",
            fields = listOf(
                InputField("length", "Room length (ft)", "Room length (m)", "e.g. 15", defaultValue = 14.0),
                InputField("width", "Room width (ft)", "Room width (m)", "e.g. 12", defaultValue = 12.0),
                InputField("height", "Wall height (ft)", "Wall height (m)", "e.g. 8 or 9", defaultValue = 8.0),
                InputField("doors", "Number of doors", "Number of doors", "Count", isInteger = true, defaultValue = 1.0),
                InputField("windows", "Number of windows", "Number of windows", "Count", isInteger = true, defaultValue = 2.0),
                InputField("coats", "Coats of paint", "Coats of paint", "1 or 2 coats", isInteger = true, defaultValue = 2.0),
                InputField("waste", "Waste allowance (%)", "Waste allowance (%)", "Default 10%", defaultValue = 10.0),
                InputField("coverage", "Coverage (sq ft/gal)", "Coverage (sq m/L)", "Default 350 sq ft / 8.6 sq m", defaultValue = 350.0)
            )
        ) { i, p, unit ->
            val l = i.v("length")
            val w = i.v("width")
            val h = i.v("height")
            val doors = i.v("doors")
            val windows = i.v("windows")
            val coats = max(1.0, i.v("coats", 2.0))
            val wastePct = i.v("waste", 10.0)
            val coverage = if (i.v("coverage") > 0) i.v("coverage") else if (unit == UnitSystem.METRIC) 8.6 else 350.0

            val isMetric = unit == UnitSystem.METRIC
            val doorArea = if (isMetric) 1.86 else 20.0
            val windowArea = if (isMetric) 1.39 else 15.0

            val grossArea = 2.0 * (l + w) * h
            val openingDeductions = doors * doorArea + windows * windowArea
            val netArea = max(0.0, grossArea - openingDeductions)
            val coatedArea = netArea * coats * (1.0 + wastePct / 100.0)

            val volume = coatedArea / coverage
            val buy = ceil(volume)
            val unitStr = if (isMetric) "liters" else "gal"
            val areaUnit = if (isMetric) "sq m" else "sq ft"

            val pricePerUnit = p.v("paint_gal", 38.0)
            val cost = buy * pricePerUnit

            CalcResult(
                headline = "${f1(volume)} $unitStr",
                label = "Paint needed (${buy.toInt()} $unitStr to buy)",
                details = "Gross Wall Area: ${f1(grossArea)} $areaUnit\n" +
                        "Opening Deductions: ${f1(openingDeductions)} $areaUnit\n" +
                        "Net Paintable Area: ${f1(netArea)} $areaUnit\n" +
                        "Coats: ${coats.toInt()} coats\n" +
                        "Waste Allowance: ${f1(wastePct)}%\n" +
                        "Total Coated Surface: ${f1(coatedArea)} $areaUnit\n" +
                        "Coverage Rate: ${f1(coverage)} $areaUnit/$unitStr\n" +
                        "Containers to Buy: ${buy.toInt()} $unitStr",
                cost = cost,
                primaryQuantity = buy,
                primaryUnit = unitStr
            )
        },

        // 2. Primer Calculator
        CalcDef(
            key = "primer",
            title = "Primer Calculator",
            subtitle = "Primer/sealer volume for bare walls, drywall & color shifts",
            category = CalcCategory.SURFACES,
            iconRes = R.drawable.ic_primer,
            measurementTips = "Primer provides crucial adhesion and stains sealing. One coat is standard; 2 coats for bare drywall or dark-to-light transitions. Standard coverage is ~300 sq ft/gal (7.4 sq m/L).",
            fields = listOf(
                InputField("length", "Room length (ft)", "Room length (m)", defaultValue = 14.0),
                InputField("width", "Room width (ft)", "Room width (m)", defaultValue = 12.0),
                InputField("height", "Wall height (ft)", "Wall height (m)", defaultValue = 8.0),
                InputField("doors", "Number of doors", "Number of doors", isInteger = true, defaultValue = 1.0),
                InputField("windows", "Number of windows", "Number of windows", isInteger = true, defaultValue = 2.0),
                InputField("coats", "Coats of primer", "Coats of primer", defaultValue = 1.0, isInteger = true),
                InputField("waste", "Waste allowance (%)", "Waste allowance (%)", defaultValue = 10.0),
                InputField("coverage", "Coverage (sq ft/gal)", "Coverage (sq m/L)", defaultValue = 300.0)
            )
        ) { i, p, unit ->
            val l = i.v("length")
            val w = i.v("width")
            val h = i.v("height")
            val doors = i.v("doors")
            val windows = i.v("windows")
            val coats = max(1.0, i.v("coats", 1.0))
            val wastePct = i.v("waste", 10.0)

            val isMetric = unit == UnitSystem.METRIC
            val coverage = if (i.v("coverage") > 0) i.v("coverage") else if (isMetric) 7.4 else 300.0
            val doorArea = if (isMetric) 1.86 else 20.0
            val windowArea = if (isMetric) 1.39 else 15.0

            val grossArea = 2.0 * (l + w) * h
            val openingDeductions = doors * doorArea + windows * windowArea
            val netArea = max(0.0, grossArea - openingDeductions)
            val coatedArea = netArea * coats * (1.0 + wastePct / 100.0)

            val volume = coatedArea / coverage
            val buy = ceil(volume)
            val unitStr = if (isMetric) "liters" else "gal"
            val areaUnit = if (isMetric) "sq m" else "sq ft"
            val cost = buy * p.v("primer_gal", 26.0)

            CalcResult(
                headline = "${f1(volume)} $unitStr",
                label = "Primer needed (${buy.toInt()} $unitStr to buy)",
                details = "Net Wall Surface: ${f1(netArea)} $areaUnit\n" +
                        "Primer Coats: ${coats.toInt()}\n" +
                        "Total Primer Surface: ${f1(coatedArea)} $areaUnit\n" +
                        "Coverage Rate: ${f1(coverage)} $areaUnit/$unitStr\n" +
                        "Recommended Container Count: ${buy.toInt()} $unitStr",
                cost = cost,
                primaryQuantity = buy,
                primaryUnit = unitStr
            )
        },

        // 3. Ceiling Paint & Texture
        CalcDef(
            key = "ceiling",
            title = "Ceiling Paint & Texture",
            subtitle = "Paint or texture for flat, tray, or textured ceilings",
            category = CalcCategory.SURFACES,
            iconRes = R.drawable.ic_ceiling,
            measurementTips = "Measure room length and width. Textured ceilings (popcorn/stipple) require 15-25% more paint coverage than flat ceilings.",
            fields = listOf(
                InputField("length", "Ceiling length (ft)", "Ceiling length (m)", defaultValue = 15.0),
                InputField("width", "Ceiling width (ft)", "Ceiling width (m)", defaultValue = 12.0),
                InputField("coats", "Number of coats", "Number of coats", defaultValue = 2.0, isInteger = true),
                InputField("waste", "Texture / waste factor (%)", "Texture / waste factor (%)", defaultValue = 15.0),
                InputField("coverage", "Coverage (sq ft/gal)", "Coverage (sq m/L)", defaultValue = 350.0)
            )
        ) { i, p, unit ->
            val l = i.v("length")
            val w = i.v("width")
            val coats = max(1.0, i.v("coats", 2.0))
            val waste = i.v("waste", 15.0)

            val isMetric = unit == UnitSystem.METRIC
            val coverage = if (i.v("coverage") > 0) i.v("coverage") else if (isMetric) 8.6 else 350.0

            val area = l * w
            val totalSurface = area * coats * (1.0 + waste / 100.0)
            val volume = totalSurface / coverage
            val buy = ceil(volume)
            val unitStr = if (isMetric) "liters" else "gal"
            val areaUnit = if (isMetric) "sq m" else "sq ft"

            CalcResult(
                headline = "${f1(volume)} $unitStr",
                label = "Ceiling paint (${buy.toInt()} $unitStr to buy)",
                details = "Ceiling Area: ${f1(area)} $areaUnit\n" +
                        "Coats: ${coats.toInt()}\n" +
                        "Texture/Waste Allowance: ${f1(waste)}%\n" +
                        "Total Surface to Paint: ${f1(totalSurface)} $areaUnit\n" +
                        "Gallons/Liters to buy: ${buy.toInt()} $unitStr",
                cost = buy * p.v("ceiling_paint_gal", 32.0),
                primaryQuantity = buy,
                primaryUnit = unitStr
            )
        },

        // 4. Doors & Windows (Trim & Deductions)
        CalcDef(
            key = "doors_windows",
            title = "Doors & Windows",
            subtitle = "Molding trim casing lengths and wall opening deductions",
            category = CalcCategory.SURFACES,
            iconRes = R.drawable.ic_door_window,
            measurementTips = "Door trim covers 2 sides + header (2H + W). Window trim covers entire perimeter (2H + 2W). Standard casing stick is 8 ft (2.4 m).",
            fields = listOf(
                InputField("doors", "Door count", "Door count", isInteger = true, defaultValue = 2.0),
                InputField("door_w", "Door width (in)", "Door width (cm)", defaultValue = 36.0),
                InputField("door_h", "Door height (in)", "Door height (cm)", defaultValue = 80.0),
                InputField("windows", "Window count", "Window count", isInteger = true, defaultValue = 3.0),
                InputField("win_w", "Window width (in)", "Window width (cm)", defaultValue = 36.0),
                InputField("win_h", "Window height (in)", "Window height (cm)", defaultValue = 60.0),
                InputField("waste", "Miter waste allowance (%)", "Miter waste allowance (%)", defaultValue = 15.0)
            )
        ) { i, p, unit ->
            val doors = i.v("doors")
            val dw = i.v("door_w", 36.0)
            val dh = i.v("door_h", 80.0)
            val windows = i.v("windows")
            val ww = i.v("win_w", 36.0)
            val wh = i.v("win_h", 60.0)
            val waste = i.v("waste", 15.0)

            val isMetric = unit == UnitSystem.METRIC
            // convert to feet or meters
            val dwFt = if (isMetric) dw / 100.0 else dw / 12.0
            val dhFt = if (isMetric) dh / 100.0 else dh / 12.0
            val wwFt = if (isMetric) ww / 100.0 else ww / 12.0
            val whFt = if (isMetric) wh / 100.0 else wh / 12.0

            val doorTrimLinear = (2.0 * dhFt + dwFt) * doors
            val winTrimLinear = (2.0 * (whFt + wwFt)) * windows
            val totalLinear = (doorTrimLinear + winTrimLinear) * (1.0 + waste / 100.0)

            val pieceLength = if (isMetric) 2.4 else 8.0 // 8 ft or 2.4 m sticks
            val pieces = ceil(totalLinear / pieceLength)

            val totalDeductionArea = (doors * dwFt * dhFt) + (windows * wwFt * whFt)
            val linUnit = if (isMetric) "m" else "linear ft"
            val areaUnit = if (isMetric) "sq m" else "sq ft"

            CalcResult(
                headline = "${f1(totalLinear)} $linUnit",
                label = "Trim casing needed (${pieces.toInt()} pieces)",
                details = "Door Trim: ${f1(doorTrimLinear)} $linUnit ($doors doors)\n" +
                        "Window Trim: ${f1(winTrimLinear)} $linUnit ($windows windows)\n" +
                        "Miter Cuts Waste: ${f1(waste)}%\n" +
                        "Casing Sticks to Buy (${if (isMetric) "2.4m" else "8ft"}): ${pieces.toInt()} pcs\n" +
                        "Total Wall Deduction Area: ${f1(totalDeductionArea)} $areaUnit",
                cost = totalLinear * p.v("trim_molding_ft", 2.25),
                primaryQuantity = pieces,
                primaryUnit = "pieces"
            )
        },

        // 5. Flooring (Hardwood / Laminate / Vinyl)
        CalcDef(
            key = "flooring",
            title = "Flooring (Hardwood/Laminate)",
            subtitle = "Boxes of plank flooring with waste & expansion allowance",
            category = CalcCategory.FLOORING,
            iconRes = R.drawable.ic_flooring,
            measurementTips = "Order 10% extra for straight layout, or 15% for diagonal/herringbone. Flooring is sold in boxes; enter box coverage (usually 20-25 sq ft or 1.8-2.3 sq m).",
            fields = listOf(
                InputField("length", "Room length (ft)", "Room length (m)", defaultValue = 18.0),
                InputField("width", "Room width (ft)", "Room width (m)", defaultValue = 14.0),
                InputField("waste", "Waste allowance (%)", "Waste allowance (%)", defaultValue = 10.0),
                InputField("box_sqft", "Coverage per box (sq ft)", "Coverage per box (sq m)", defaultValue = 22.0)
            )
        ) { i, p, unit ->
            val l = i.v("length")
            val w = i.v("width")
            val waste = i.v("waste", 10.0)

            val isMetric = unit == UnitSystem.METRIC
            val boxCov = if (i.v("box_sqft") > 0) i.v("box_sqft") else if (isMetric) 2.0 else 22.0

            val roomArea = l * w
            val totalNeeded = roomArea * (1.0 + waste / 100.0)
            val boxes = ceil(totalNeeded / boxCov)
            val totalPurchased = boxes * boxCov

            val areaUnit = if (isMetric) "sq m" else "sq ft"
            val cost = totalPurchased * p.v("flooring_sqft", 4.50)

            CalcResult(
                headline = "${boxes.toInt()} boxes",
                label = "${f1(totalNeeded)} $areaUnit to buy",
                details = "Room Area: ${f1(roomArea)} $areaUnit\n" +
                        "Waste Allowance: ${f1(waste)}%\n" +
                        "Total Required: ${f1(totalNeeded)} $areaUnit\n" +
                        "Box Coverage: ${f1(boxCov)} $areaUnit/box\n" +
                        "Total Purchased: ${f1(totalPurchased)} $areaUnit (${boxes.toInt()} boxes)",
                cost = cost,
                primaryQuantity = boxes,
                primaryUnit = "boxes"
            )
        },

        // 6. Tiles (Floor & Wall)
        CalcDef(
            key = "tile",
            title = "Tiles (Floor & Wall)",
            subtitle = "Tile pieces and cartons based on custom tile dimensions",
            category = CalcCategory.FLOORING,
            iconRes = R.drawable.ic_tile,
            measurementTips = "Enter tile length & width (e.g. 12x12 or 12x24). Standard waste is 10% straight, 15% diagonal, 20% herringbone.",
            fields = listOf(
                InputField("length", "Surface length (ft)", "Surface length (m)", defaultValue = 12.0),
                InputField("width", "Surface width (ft)", "Surface width (m)", defaultValue = 10.0),
                InputField("tile_l", "Tile length (in)", "Tile length (cm)", defaultValue = 12.0),
                InputField("tile_w", "Tile width (in)", "Tile width (cm)", defaultValue = 12.0),
                InputField("waste", "Cutting waste (%)", "Cutting waste (%)", defaultValue = 10.0),
                InputField("tiles_per_box", "Tiles per box", "Tiles per box", defaultValue = 10.0, isInteger = true)
            )
        ) { i, p, unit ->
            val l = i.v("length")
            val w = i.v("width")
            val tl = i.v("tile_l", 12.0)
            val tw = i.v("tile_w", 12.0)
            val waste = i.v("waste", 10.0)
            val perBox = max(1.0, i.v("tiles_per_box", 10.0))

            val isMetric = unit == UnitSystem.METRIC
            val area = l * w
            val tileAreaSqFt = if (isMetric) (tl * tw) / 10000.0 else (tl * tw) / 144.0

            val netTiles = if (tileAreaSqFt > 0) area / tileAreaSqFt else 0.0
            val totalTiles = ceil(netTiles * (1.0 + waste / 100.0))
            val boxes = ceil(totalTiles / perBox)

            val areaUnit = if (isMetric) "sq m" else "sq ft"
            val totalSqFtPurchased = totalTiles * tileAreaSqFt

            CalcResult(
                headline = "${totalTiles.toInt()} tiles",
                label = "${boxes.toInt()} boxes (${f1(totalSqFtPurchased)} $areaUnit)",
                details = "Surface Area: ${f1(area)} $areaUnit\n" +
                        "Tile Size: ${f1(tl)} x ${f1(tw)} ${if (isMetric) "cm" else "in"}\n" +
                        "Exact Tiles: ${f1(netTiles)}\n" +
                        "Waste Allowance: ${f1(waste)}%\n" +
                        "Tiles with Waste: ${totalTiles.toInt()} tiles\n" +
                        "Boxes to Buy (${perBox.toInt()}/box): ${boxes.toInt()} boxes",
                cost = totalSqFtPurchased * p.v("tile_sqft", 5.25),
                primaryQuantity = totalTiles,
                primaryUnit = "tiles"
            )
        },

        // 7. Tile Grout
        CalcDef(
            key = "grout",
            title = "Tile Grout",
            subtitle = "Bags of sanded or unsanded grout based on joint width",
            category = CalcCategory.FLOORING,
            iconRes = R.drawable.ic_grout,
            measurementTips = "Grout amount depends on tile dimensions and grout line width. Sanded grout is used for joints >= 1/8\" (3mm), unsanded for smaller.",
            fields = listOf(
                InputField("area", "Tiled area (sq ft)", "Tiled area (sq m)", defaultValue = 120.0),
                InputField("tile_l", "Tile length (in)", "Tile length (cm)", defaultValue = 12.0),
                InputField("tile_w", "Tile width (in)", "Tile width (cm)", defaultValue = 12.0),
                InputField("tile_t", "Tile thickness (in)", "Tile thickness (mm)", defaultValue = 0.375),
                InputField("joint_w", "Grout joint width (in)", "Grout joint width (mm)", defaultValue = 0.125),
                InputField("bag_size", "Bag size (lbs)", "Bag size (kg)", defaultValue = 25.0),
                InputField("waste", "Waste allowance (%)", "Waste allowance (%)", defaultValue = 10.0)
            )
        ) { i, p, unit ->
            val area = i.v("area", 100.0)
            val tl = i.v("tile_l", 12.0)
            val tw = i.v("tile_w", 12.0)
            val isMetric = unit == UnitSystem.METRIC

            // Convert to consistent inches or mm
            val tlIn = if (isMetric) tl * 0.3937 else tl
            val twIn = if (isMetric) tw * 0.3937 else tw
            val ttIn = if (isMetric) i.v("tile_t", 9.5) * 0.03937 else i.v("tile_t", 0.375)
            val jwIn = if (isMetric) i.v("joint_w", 3.0) * 0.03937 else i.v("joint_w", 0.125)
            val bagSize = max(1.0, i.v("bag_size", if (isMetric) 10.0 else 25.0))
            val waste = i.v("waste", 10.0)

            val areaSqFt = if (isMetric) area * 10.7639 else area

            // Standard grout formula: lbs = (L + W) / (L * W) * Joint * Thick * Area * 1.5 * waste
            val groutLbs = if (tlIn > 0 && twIn > 0) {
                ((tlIn + twIn) / (tlIn * twIn)) * jwIn * ttIn * areaSqFt * 1.55 * (1.0 + waste / 100.0)
            } else 0.0

            val weightDisplay = if (isMetric) groutLbs * 0.453592 else groutLbs
            val weightUnit = if (isMetric) "kg" else "lbs"
            val bags = ceil(weightDisplay / bagSize)

            CalcResult(
                headline = "${f1(weightDisplay)} $weightUnit",
                label = "Grout needed (${bags.toInt()} bags)",
                details = "Tiled Area: ${f1(area)} ${if (isMetric) "sq m" else "sq ft"}\n" +
                        "Tile Dimensions: ${f1(tl)} x ${f1(tw)}\n" +
                        "Joint Width: ${f2(if (isMetric) i.v("joint_w", 3.0) else jwIn)} ${if (isMetric) "mm" else "in"}\n" +
                        "Calculated Grout: ${f1(weightDisplay)} $weightUnit (incl. ${f1(waste)}% waste)\n" +
                        "Bag Size: ${f1(bagSize)} $weightUnit\n" +
                        "Bags to Buy: ${bags.toInt()} bags",
                cost = bags * p.v("grout_bag", 22.0),
                primaryQuantity = bags,
                primaryUnit = "bags"
            )
        },

        // 8. Drywall (Gypsum Board)
        CalcDef(
            key = "drywall",
            title = "Drywall & Supplies",
            subtitle = "Sheets, joint compound, screws, and seam tape",
            category = CalcCategory.FRAMING,
            iconRes = R.drawable.ic_drywall,
            measurementTips = "Standard panels are 4x8 ft (32 sq ft) or 4x12 ft (48 sq ft). Ceilings are hung first, followed by wall boards.",
            fields = listOf(
                InputField("length", "Room length (ft)", "Room length (m)", defaultValue = 16.0),
                InputField("width", "Room width (ft)", "Room width (m)", defaultValue = 12.0),
                InputField("height", "Ceiling height (ft)", "Ceiling height (m)", defaultValue = 8.0),
                InputField("sheet_type", "Sheet size (1 = 4x8, 2 = 4x12)", "Sheet size (1 = 1.2x2.4m, 2 = 1.2x3.6m)", defaultValue = 1.0, isInteger = true),
                InputField("ceiling", "Include ceiling (1 = Yes, 0 = No)", "Include ceiling (1 = Yes, 0 = No)", defaultValue = 1.0, isInteger = true),
                InputField("waste", "Waste allowance (%)", "Waste allowance (%)", defaultValue = 10.0)
            )
        ) { i, p, unit ->
            val l = i.v("length")
            val w = i.v("width")
            val h = i.v("height")
            val sheetType = i.v("sheet_type", 1.0).toInt()
            val incCeiling = i.v("ceiling", 1.0) >= 0.5
            val waste = i.v("waste", 10.0)

            val isMetric = unit == UnitSystem.METRIC
            val wallArea = 2.0 * (l + w) * h
            val ceilingArea = if (incCeiling) l * w else 0.0
            val totalArea = wallArea + ceilingArea

            val sheetArea = if (isMetric) {
                if (sheetType == 2) 1.2 * 3.6 else 1.2 * 2.4
            } else {
                if (sheetType == 2) 48.0 else 32.0
            }

            val sheets = ceil(totalArea * (1.0 + waste / 100.0) / sheetArea)

            // Drywall accessories
            val totalAreaSqFt = if (isMetric) totalArea * 10.7639 else totalArea
            val compoundGal = ceil(totalAreaSqFt * 0.053) // approx 0.053 gal per sq ft
            val screws = ceil(totalAreaSqFt * 1.1) // 1.1 screws per sq ft
            val tapeRolls = ceil(totalAreaSqFt / 250.0) // 250 linear ft tape per roll

            val areaUnit = if (isMetric) "sq m" else "sq ft"
            val cost = sheets * p.v("drywall_sheet", 16.50)

            CalcResult(
                headline = "${sheets.toInt()} sheets",
                label = "Drywall panels (${f1(totalArea)} $areaUnit)",
                details = "Wall Surface Area: ${f1(wallArea)} $areaUnit\n" +
                        "Ceiling Surface: ${if (incCeiling) f1(ceilingArea) + " " + areaUnit else "Not included"}\n" +
                        "Total Surface: ${f1(totalArea)} $areaUnit\n" +
                        "Panel Size: ${if (sheetType == 2) (if (isMetric) "1.2x3.6m" else "4x12 ft") else (if (isMetric) "1.2x2.4m" else "4x8 ft")}\n" +
                        "Joint Compound Needed: ${compoundGal.toInt()} gal (${f1(compoundGal * 3.785)} L)\n" +
                        "Drywall Screws: ~${screws.toInt()} screws\n" +
                        "Joint Tape: ${tapeRolls.toInt()} rolls (250 ft/roll)",
                cost = cost,
                primaryQuantity = sheets,
                primaryUnit = "sheets"
            )
        },

        // 9. Roofing
        CalcDef(
            key = "roofing",
            title = "Roofing (Shingles & Underlayment)",
            subtitle = "Roof squares, shingle bundles, and underlayment rolls",
            category = CalcCategory.ROOFING_DECK,
            iconRes = R.drawable.ic_roofing,
            measurementTips = "1 roofing square = 100 sq ft (9.29 sq m). Standard pitch is 4/12 to 8/12. Standard shingle bundle = 1/3 square (3 bundles per square).",
            fields = listOf(
                InputField("length", "Roof footprint length (ft)", "Roof footprint length (m)", defaultValue = 40.0),
                InputField("width", "Roof footprint width (ft)", "Roof footprint width (m)", defaultValue = 30.0),
                InputField("pitch", "Roof pitch (rise over 12, e.g. 6)", "Roof pitch (rise over 12, e.g. 6)", defaultValue = 6.0),
                InputField("overhang", "Eave overhang (in)", "Eave overhang (cm)", defaultValue = 12.0),
                InputField("waste", "Waste factor (%)", "Waste factor (%)", defaultValue = 15.0)
            )
        ) { i, p, unit ->
            val l = i.v("length")
            val w = i.v("width")
            val pitch = i.v("pitch", 6.0)
            val overhang = i.v("overhang", 12.0)
            val waste = i.v("waste", 15.0)

            val isMetric = unit == UnitSystem.METRIC
            val overhangFt = if (isMetric) overhang / 100.0 else overhang / 12.0

            val pitchFactor = sqrt(1.0 + (pitch / 12.0) * (pitch / 12.0))
            val trueArea = (l + 2.0 * overhangFt) * (w + 2.0 * overhangFt) * pitchFactor
            val trueAreaSqFt = if (isMetric) trueArea * 10.7639 else trueArea

            val squares = trueAreaSqFt / 100.0
            val squaresWithWaste = squares * (1.0 + waste / 100.0)
            val bundles = ceil(squaresWithWaste * 3.0)
            val underlaymentRolls = ceil(trueAreaSqFt * (1.0 + waste / 100.0) / 400.0) // 400 sq ft per roll

            val areaUnit = if (isMetric) "sq m" else "sq ft"
            val cost = (bundles * p.v("roofing_bundle", 36.0)) + (underlaymentRolls * p.v("underlayment_roll", 48.0))

            CalcResult(
                headline = "${bundles.toInt()} bundles",
                label = "${f1(squaresWithWaste)} squares (${f1(trueArea)} $areaUnit)",
                details = "Footprint Area: ${f1(l * w)} $areaUnit\n" +
                        "Pitch Multiplier (${f1(pitch)}/12): ${f2(pitchFactor)}x\n" +
                        "Total Sloped Roof Surface: ${f1(trueArea)} $areaUnit\n" +
                        "Roofing Squares: ${f1(squaresWithWaste)} squares (incl. ${f1(waste)}% waste)\n" +
                        "Shingle Bundles (3/square): ${bundles.toInt()} bundles\n" +
                        "Underlayment Rolls (400 sq ft/roll): ${underlaymentRolls.toInt()} rolls",
                cost = cost,
                primaryQuantity = bundles,
                primaryUnit = "bundles"
            )
        },

        // 10. Fencing
        CalcDef(
            key = "fencing",
            title = "Fencing Calculator",
            subtitle = "Posts, horizontal rails, vertical pickets, and post concrete",
            category = CalcCategory.ROOFING_DECK,
            iconRes = R.drawable.ic_fencing,
            measurementTips = "Standard post spacing is 8 ft (2.4 m). Use 2 rails for 4ft fence, 3 rails for 6ft fence. Plan 1-2 concrete bags per post hole.",
            fields = listOf(
                InputField("length", "Total fence length (ft)", "Total fence length (m)", defaultValue = 120.0),
                InputField("post_spacing", "Post spacing (ft)", "Post spacing (m)", defaultValue = 8.0),
                InputField("height", "Fence height (ft)", "Fence height (m)", defaultValue = 6.0),
                InputField("picket_w", "Picket width (in)", "Picket width (cm)", defaultValue = 5.5),
                InputField("picket_gap", "Picket spacing gap (in)", "Picket spacing gap (cm)", defaultValue = 0.0),
                InputField("waste", "Waste factor (%)", "Waste factor (%)", defaultValue = 10.0)
            )
        ) { i, p, unit ->
            val len = i.v("length")
            val spacing = max(1.0, i.v("post_spacing", if (unit == UnitSystem.METRIC) 2.4 else 8.0))
            val height = i.v("height", if (unit == UnitSystem.METRIC) 1.8 else 6.0)
            val pw = i.v("picket_w", if (unit == UnitSystem.METRIC) 14.0 else 5.5)
            val gap = i.v("picket_gap", 0.0)
            val waste = i.v("waste", 10.0)

            val isMetric = unit == UnitSystem.METRIC
            val sections = ceil(len / spacing)
            val posts = sections + 1.0

            val railsPerSection = if ((if (isMetric) height >= 1.8 else height >= 6.0)) 3.0 else 2.0
            val rails = sections * railsPerSection

            val totalLengthIn = if (isMetric) len * 100.0 / 2.54 else len * 12.0
            val effectivePicketIn = if (isMetric) (pw + gap) / 2.54 else (pw + gap)
            val pickets = if (effectivePicketIn > 0) {
                ceil((totalLengthIn / effectivePicketIn) * (1.0 + waste / 100.0))
            } else 0.0

            val concreteBags = posts * 1.5 // 1.5 bags per post hole
            val cost = (pickets * p.v("fence_picket", 3.20)) +
                    (posts * p.v("fence_post", 14.50)) +
                    (concreteBags * p.v("concrete_bag", 6.50))

            val lenUnit = if (isMetric) "m" else "ft"

            CalcResult(
                headline = "${posts.toInt()} posts, ${pickets.toInt()} pickets",
                label = "Total Fence Length: ${f1(len)} $lenUnit",
                details = "Fence Sections: ${sections.toInt()} sections (${f1(spacing)} $lenUnit span)\n" +
                        "Posts Required: ${posts.toInt()} posts\n" +
                        "Horizontal Rails: ${rails.toInt()} rails (${railsPerSection.toInt()} per section)\n" +
                        "Vertical Pickets: ${pickets.toInt()} pickets (incl. ${f1(waste)}% waste)\n" +
                        "Concrete for Posts: ${ceil(concreteBags).toInt()} bags (50 lb / 25 kg)",
                cost = cost,
                primaryQuantity = pickets,
                primaryUnit = "pickets"
            )
        },

        // 11. Decking
        CalcDef(
            key = "decking",
            title = "Decking Calculator",
            subtitle = "Linear feet of deck boards, joists, and fastener screws",
            category = CalcCategory.ROOFING_DECK,
            iconRes = R.drawable.ic_decking,
            measurementTips = "Standard deck board width is 5.5 inches (14 cm). Joist spacing is usually 16 inches (40 cm) on center.",
            fields = listOf(
                InputField("length", "Deck length (ft)", "Deck length (m)", defaultValue = 20.0),
                InputField("width", "Deck width (ft)", "Deck width (m)", defaultValue = 14.0),
                InputField("board_w", "Board width (in)", "Board width (cm)", defaultValue = 5.5),
                InputField("gap", "Board gap (in)", "Board gap (cm)", defaultValue = 0.25),
                InputField("joist_spacing", "Joist spacing (in)", "Joist spacing (cm)", defaultValue = 16.0),
                InputField("waste", "Cutting waste (%)", "Cutting waste (%)", defaultValue = 10.0)
            )
        ) { i, p, unit ->
            val l = i.v("length")
            val w = i.v("width")
            val bw = i.v("board_w", 5.5)
            val gap = i.v("gap", 0.25)
            val joistSpacing = i.v("joist_spacing", 16.0)
            val waste = i.v("waste", 10.0)

            val isMetric = unit == UnitSystem.METRIC
            val area = l * w

            val widthInches = if (isMetric) w * 39.3701 else w * 12.0
            val lengthInches = if (isMetric) l * 39.3701 else l * 12.0
            val boardSpanIn = if (isMetric) (bw + gap) / 2.54 else (bw + gap)

            val boardRuns = if (boardSpanIn > 0) ceil(widthInches / boardSpanIn) else 0.0
            val linearFeet = if (isMetric) {
                (boardRuns * l) * (1.0 + waste / 100.0)
            } else {
                (boardRuns * l) * (1.0 + waste / 100.0)
            }

            val joistSpacingIn = if (isMetric) joistSpacing / 2.54 else joistSpacing
            val joists = if (joistSpacingIn > 0) ceil(lengthInches / joistSpacingIn) + 1.0 else 0.0

            val areaSqFt = if (isMetric) area * 10.7639 else area
            val screws = ceil(areaSqFt * 3.5) // approx 350 screws per 100 sq ft

            val linUnit = if (isMetric) "linear m" else "linear ft"
            val areaUnit = if (isMetric) "sq m" else "sq ft"
            val cost = linearFeet * p.v("deck_board_ft", 3.80)

            CalcResult(
                headline = "${f1(linearFeet)} $linUnit",
                label = "Decking boards (${boardRuns.toInt()} parallel rows)",
                details = "Deck Area: ${f1(area)} $areaUnit\n" +
                        "Board Runs: ${boardRuns.toInt()} rows along ${f1(l)} ${if (isMetric) "m" else "ft"}\n" +
                        "Total Board Length: ${f1(linearFeet)} $linUnit (incl. ${f1(waste)}% waste)\n" +
                        "Joists (at ${f1(joistSpacing)} ${if (isMetric) "cm" else "in"} o.c.): ${joists.toInt()} joists\n" +
                        "Fasteners / Deck Screws: ~${screws.toInt()} screws",
                cost = cost,
                primaryQuantity = linearFeet,
                primaryUnit = linUnit
            )
        },

        // 12. Mulch
        CalcDef(
            key = "mulch",
            title = "Mulch Calculator",
            subtitle = "Bulk cubic yards/meters or 2 cu ft / 50L bagged mulch",
            category = CalcCategory.LANDSCAPING,
            iconRes = R.drawable.ic_mulch,
            measurementTips = "Recommended mulch depth is 3 inches (7.5 cm) for weed suppression and moisture retention.",
            fields = listOf(
                InputField("length", "Garden bed length (ft)", "Garden bed length (m)", defaultValue = 30.0),
                InputField("width", "Garden bed width (ft)", "Garden bed width (m)", defaultValue = 6.0),
                InputField("depth", "Mulch depth (in)", "Mulch depth (cm)", defaultValue = 3.0),
                InputField("waste", "Settling factor (%)", "Settling factor (%)", defaultValue = 5.0)
            )
        ) { i, p, unit ->
            val l = i.v("length")
            val w = i.v("width")
            val d = i.v("depth", 3.0)
            val waste = i.v("waste", 5.0)

            val isMetric = unit == UnitSystem.METRIC
            val volCuFt = if (isMetric) {
                l * w * (d / 100.0) * 35.3147 * (1.0 + waste / 100.0)
            } else {
                l * w * (d / 12.0) * (1.0 + waste / 100.0)
            }

            val cuYd = volCuFt / 27.0
            val cuM = volCuFt * 0.0283168
            val bags2CuFt = ceil(volCuFt / 2.0)

            val volDisplay = if (isMetric) f2(cuM) + " cu m" else f2(cuYd) + " cu yd"
            val cost = (if (isMetric) cuM else cuYd) * p.v("mulch_cuyd", 42.0)

            CalcResult(
                headline = volDisplay,
                label = "Mulch needed (${bags2CuFt.toInt()} bags)",
                details = "Bed Coverage Area: ${f1(l * w)} ${if (isMetric) "sq m" else "sq ft"}\n" +
                        "Depth: ${f1(d)} ${if (isMetric) "cm" else "in"}\n" +
                        "Volume (cu ft): ${f1(volCuFt)} cu ft\n" +
                        "Bulk Volume: ${volDisplay}\n" +
                        "Standard Bags (2 cu ft / 56L): ${bags2CuFt.toInt()} bags",
                cost = cost,
                primaryQuantity = if (isMetric) cuM else cuYd,
                primaryUnit = if (isMetric) "cu m" else "cu yd"
            )
        },

        // 13. Gravel & Crushed Stone
        CalcDef(
            key = "gravel",
            title = "Gravel & Crushed Stone",
            subtitle = "Volume and weight in tons for driveways, paths & drainage",
            category = CalcCategory.LANDSCAPING,
            iconRes = R.drawable.ic_gravel,
            measurementTips = "Driveways require 4-6 inches (10-15 cm) depth. Crushed stone weighs ~1.4 tons per cubic yard (1.65 tons/cu m).",
            fields = listOf(
                InputField("length", "Area length (ft)", "Area length (m)", defaultValue = 40.0),
                InputField("width", "Area width (ft)", "Area width (m)", defaultValue = 10.0),
                InputField("depth", "Gravel depth (in)", "Gravel depth (cm)", defaultValue = 4.0),
                InputField("compaction", "Compaction factor (%)", "Compaction factor (%)", defaultValue = 15.0)
            )
        ) { i, p, unit ->
            val l = i.v("length")
            val w = i.v("width")
            val d = i.v("depth", 4.0)
            val comp = i.v("compaction", 15.0)

            val isMetric = unit == UnitSystem.METRIC
            val volCuFt = if (isMetric) {
                l * w * (d / 100.0) * 35.3147 * (1.0 + comp / 100.0)
            } else {
                l * w * (d / 12.0) * (1.0 + comp / 100.0)
            }

            val cuYd = volCuFt / 27.0
            val cuM = volCuFt * 0.0283168
            val tonsImperial = cuYd * 1.4 // approx 2800 lbs per cu yd
            val metricTonnes = cuM * 1.65

            val volDisplay = if (isMetric) f2(cuM) + " cu m" else f2(cuYd) + " cu yd"
            val weightDisplay = if (isMetric) f1(metricTonnes) + " tonnes" else f1(tonsImperial) + " tons"
            val cost = (if (isMetric) cuM else cuYd) * p.v("gravel_cuyd", 58.0)

            CalcResult(
                headline = weightDisplay,
                label = "Gravel needed ($volDisplay)",
                details = "Footprint Area: ${f1(l * w)} ${if (isMetric) "sq m" else "sq ft"}\n" +
                        "Depth: ${f1(d)} ${if (isMetric) "cm" else "in"}\n" +
                        "Compaction Allowance: ${f1(comp)}%\n" +
                        "Bulk Volume: $volDisplay\n" +
                        "Estimated Weight: $weightDisplay",
                cost = cost,
                primaryQuantity = if (isMetric) cuM else cuYd,
                primaryUnit = if (isMetric) "cu m" else "cu yd"
            )
        },

        // 14. Soil & Topsoil
        CalcDef(
            key = "soil",
            title = "Soil & Topsoil",
            subtitle = "Bulk cubic yards/meters or 1 cu ft / 40 lb bags",
            category = CalcCategory.LANDSCAPING,
            iconRes = R.drawable.ic_soil,
            measurementTips = "Lawn topdressing is typically 0.5-1 inch. Raised garden beds are typically 6-12 inches (15-30 cm).",
            fields = listOf(
                InputField("length", "Bed length (ft)", "Bed length (m)", defaultValue = 20.0),
                InputField("width", "Bed width (ft)", "Bed width (m)", defaultValue = 5.0),
                InputField("depth", "Soil depth (in)", "Soil depth (cm)", defaultValue = 6.0),
                InputField("waste", "Compaction / settling (%)", "Compaction / settling (%)", defaultValue = 10.0)
            )
        ) { i, p, unit ->
            val l = i.v("length")
            val w = i.v("width")
            val d = i.v("depth", 6.0)
            val waste = i.v("waste", 10.0)

            val isMetric = unit == UnitSystem.METRIC
            val volCuFt = if (isMetric) {
                l * w * (d / 100.0) * 35.3147 * (1.0 + waste / 100.0)
            } else {
                l * w * (d / 12.0) * (1.0 + waste / 100.0)
            }

            val cuYd = volCuFt / 27.0
            val cuM = volCuFt * 0.0283168
            val bags1CuFt = ceil(volCuFt)

            val volDisplay = if (isMetric) f2(cuM) + " cu m" else f2(cuYd) + " cu yd"
            val cost = (if (isMetric) cuM else cuYd) * p.v("soil_cuyd", 36.0)

            CalcResult(
                headline = volDisplay,
                label = "Soil needed (${bags1CuFt.toInt()} bags)",
                details = "Bed Area: ${f1(l * w)} ${if (isMetric) "sq m" else "sq ft"}\n" +
                        "Depth: ${f1(d)} ${if (isMetric) "cm" else "in"}\n" +
                        "Volume: $volDisplay\n" +
                        "Settling Allowance: ${f1(waste)}%\n" +
                        "1 cu ft / 28L Bag Count: ${bags1CuFt.toInt()} bags",
                cost = cost,
                primaryQuantity = if (isMetric) cuM else cuYd,
                primaryUnit = if (isMetric) "cu m" else "cu yd"
            )
        },

        // 15. Concrete / Slab
        CalcDef(
            key = "concrete",
            title = "Concrete Slab & Footings",
            subtitle = "Cubic yards/meters and 80 lb / 60 lb / 25 kg bags",
            category = CalcCategory.LANDSCAPING,
            iconRes = R.drawable.ic_concrete,
            measurementTips = "Standard patio or driveway slab thickness is 4 inches (10 cm). Always add 10% for uneven subgrade or spillage.",
            fields = listOf(
                InputField("length", "Slab length (ft)", "Slab length (m)", defaultValue = 20.0),
                InputField("width", "Slab width (ft)", "Slab width (m)", defaultValue = 10.0),
                InputField("depth", "Thickness (in)", "Thickness (cm)", defaultValue = 4.0),
                InputField("waste", "Over-pour allowance (%)", "Over-pour allowance (%)", defaultValue = 10.0)
            )
        ) { i, p, unit ->
            val l = i.v("length")
            val w = i.v("width")
            val d = i.v("depth", 4.0)
            val waste = i.v("waste", 10.0)

            val isMetric = unit == UnitSystem.METRIC
            val volCuFt = if (isMetric) {
                l * w * (d / 100.0) * 35.3147 * (1.0 + waste / 100.0)
            } else {
                l * w * (d / 12.0) * (1.0 + waste / 100.0)
            }

            val cuYd = volCuFt / 27.0
            val cuM = volCuFt * 0.0283168
            val bags80lb = ceil(volCuFt / 0.60) // 80 lb bag = 0.6 cu ft
            val bags60lb = ceil(volCuFt / 0.45) // 60 lb bag = 0.45 cu ft

            val volDisplay = if (isMetric) f2(cuM) + " cu m" else f2(cuYd) + " cu yd"
            val cost = (if (isMetric) cuM else cuYd) * p.v("concrete_cuyd", 145.0)

            CalcResult(
                headline = volDisplay,
                label = "Concrete volume (incl. ${f1(waste)}% extra)",
                details = "Slab Dimensions: ${f1(l)} x ${f1(w)} (${f1(d)} ${if (isMetric) "cm" else "in"} thick)\n" +
                        "Volume: $volDisplay\n" +
                        "80 lb (36 kg) Pre-mix Bags: ${bags80lb.toInt()} bags\n" +
                        "60 lb (27 kg) Pre-mix Bags: ${bags60lb.toInt()} bags",
                cost = cost,
                primaryQuantity = if (isMetric) cuM else cuYd,
                primaryUnit = if (isMetric) "cu m" else "cu yd"
            )
        },

        // 16. Area & Dimensions Utility
        CalcDef(
            key = "area",
            title = "Area & Dimensions",
            subtitle = "Area, perimeter, and conversion between sq ft, sq yd, and sq m",
            category = CalcCategory.SURFACES,
            iconRes = R.drawable.ic_area,
            measurementTips = "Convenient utility for sizing lots, floors, and general rectangular surfaces.",
            fields = listOf(
                InputField("length", "Length (ft)", "Length (m)", defaultValue = 25.0),
                InputField("width", "Width (ft)", "Width (m)", defaultValue = 20.0)
            )
        ) { i, _, unit ->
            val l = i.v("length")
            val w = i.v("width")
            val isMetric = unit == UnitSystem.METRIC

            val area = l * w
            val perimeter = 2.0 * (l + w)

            val sqFt = if (isMetric) area * 10.7639 else area
            val sqM = if (isMetric) area else area * 0.092903
            val sqYd = sqFt / 9.0

            val mainDisplay = if (isMetric) f1(sqM) + " sq m" else f1(sqFt) + " sq ft"

            CalcResult(
                headline = mainDisplay,
                label = "Total Area",
                details = "Perimeter: ${f1(perimeter)} ${if (isMetric) "meters" else "feet"}\n" +
                        "Square Feet: ${f1(sqFt)} sq ft\n" +
                        "Square Yards: ${f2(sqYd)} sq yd\n" +
                        "Square Meters: ${f2(sqM)} sq m",
                cost = null,
                primaryQuantity = if (isMetric) sqM else sqFt,
                primaryUnit = if (isMetric) "sq m" else "sq ft"
            )
        },

        // 17. Wallpaper
        CalcDef(
            key = "wallpaper",
            title = "Wallpaper Calculator",
            subtitle = "Rolls of wallpaper with pattern repeat matching allowance",
            category = CalcCategory.SURFACES,
            iconRes = R.drawable.ic_wallpaper,
            measurementTips = "Standard single roll covers ~28 sq ft; double roll covers ~56 sq ft. Add 15-20% for pattern repeat matching.",
            fields = listOf(
                InputField("length", "Room length (ft)", "Room length (m)", defaultValue = 14.0),
                InputField("width", "Room width (ft)", "Room width (m)", defaultValue = 12.0),
                InputField("height", "Wall height (ft)", "Wall height (m)", defaultValue = 8.0),
                InputField("openings", "Doors and windows", "Doors and windows", defaultValue = 2.0, isInteger = true),
                InputField("waste", "Pattern repeat factor (%)", "Pattern repeat factor (%)", defaultValue = 15.0)
            )
        ) { i, p, unit ->
            val l = i.v("length")
            val w = i.v("width")
            val h = i.v("height")
            val openings = i.v("openings")
            val waste = i.v("waste", 15.0)

            val isMetric = unit == UnitSystem.METRIC
            val openingArea = if (isMetric) 1.67 else 18.0
            val gross = 2.0 * (l + w) * h
            val net = max(0.0, gross - openings * openingArea)

            val netSqFt = if (isMetric) net * 10.7639 else net
            val rolls = ceil(netSqFt * (1.0 + waste / 100.0) / 56.0) // 56 sq ft per double roll

            CalcResult(
                headline = "${rolls.toInt()} rolls",
                label = "Wallpaper needed (${f1(net)} ${if (isMetric) "sq m" else "sq ft"})",
                details = "Net Wall Area: ${f1(net)} ${if (isMetric) "sq m" else "sq ft"}\n" +
                        "Pattern Match Allowance: ${f1(waste)}%\n" +
                        "Standard Double Roll (56 sq ft / 5.2 sq m)\n" +
                        "Rolls to Buy: ${rolls.toInt()} rolls",
                cost = rolls * p.v("wallpaper_roll", 45.0),
                primaryQuantity = rolls,
                primaryUnit = "rolls"
            )
        },

        // 18. Lumber / Board Feet
        CalcDef(
            key = "boardfeet",
            title = "Lumber & Board Feet",
            subtitle = "Lumber volume calculation (1 BF = 144 cubic inches)",
            category = CalcCategory.FRAMING,
            iconRes = R.drawable.ic_board,
            measurementTips = "1 Board Foot = 1 inch thick x 12 inches wide x 1 foot long (144 cu in). Enter nominal dimensions.",
            fields = listOf(
                InputField("thickness", "Thickness (in)", "Thickness (in)", defaultValue = 2.0),
                InputField("width", "Width (in)", "Width (in)", defaultValue = 4.0),
                InputField("length", "Length (ft)", "Length (ft)", defaultValue = 8.0),
                InputField("qty", "Number of pieces", "Number of pieces", defaultValue = 24.0, isInteger = true)
            )
        ) { i, p, _ ->
            val t = i.v("thickness", 2.0)
            val w = i.v("width", 4.0)
            val l = i.v("length", 8.0)
            val qty = max(1.0, i.v("qty", 1.0))

            val bfPerPiece = (t * w * l) / 12.0
            val totalBf = bfPerPiece * qty
            val cost = totalBf * p.v("board_foot", 3.75)

            CalcResult(
                headline = "${f1(totalBf)} BF",
                label = "Total board feet (${qty.toInt()} boards)",
                details = "Piece Size: ${f1(t)}\" x ${f1(w)}\" x ${f1(l)}'\n" +
                        "Board Feet per Piece: ${f2(bfPerPiece)} BF\n" +
                        "Piece Count: ${qty.toInt()}\n" +
                        "Total Board Feet: ${f2(totalBf)} BF",
                cost = cost,
                primaryQuantity = totalBf,
                primaryUnit = "BF"
            )
        }
    )

    fun find(key: String): CalcDef? = all.firstOrNull { it.key == key }
}
