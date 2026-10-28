package com.cartoniq.buildiq

import android.content.Context
import com.cartoniq.buildiq.data.AppDatabase
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.max

data class InputField(val key: String, val label: String)

data class PriceDef(val key: String, val label: String, val default: Double)

data class CalcResult(
    val headline: String,
    val label: String,
    val details: String,
    val cost: Double?
)

class CalcDef(
    val key: String,
    val title: String,
    val subtitle: String,
    val iconRes: Int,
    val fields: List<InputField>,
    val compute: (Map<String, Double>, Map<String, Double>) -> CalcResult
)

object Calculators {

    private fun f1(v: Double): String = String.format(Locale.US, "%.1f", v)

    private fun f2(v: Double): String = String.format(Locale.US, "%.2f", v)

    fun money(v: Double): String = String.format(Locale.US, "\$%,.2f", v)

    private fun Map<String, Double>.v(key: String): Double = this[key] ?: 0.0

    val priceDefs: List<PriceDef> = listOf(
        PriceDef("paint_gal", "Paint - per gallon", 35.0),
        PriceDef("tile_sqft", "Flooring/Tile - per sq ft", 4.5),
        PriceDef("concrete_cuyd", "Concrete - per cubic yard", 150.0),
        PriceDef("wallpaper_roll", "Wallpaper - per roll", 40.0),
        PriceDef("mulch_cuyd", "Mulch - per cubic yard", 45.0),
        PriceDef("gravel_cuyd", "Gravel - per cubic yard", 60.0),
        PriceDef("board_foot", "Lumber - per board foot", 4.0)
    )

    fun defaultPrices(): Map<String, Double> = priceDefs.associate { it.key to it.default }

    suspend fun loadPrices(context: Context): Map<String, Double> {
        val result = defaultPrices().toMutableMap()
        for (saved in AppDatabase.get(context).projectDao().getPrices()) {
            result[saved.key] = saved.value
        }
        return result
    }

    val all: List<CalcDef> = listOf(
        CalcDef(
            "paint", "Paint Calculator", "Gallons of paint for a room", R.drawable.ic_paint,
            listOf(
                InputField("length", "Room length (ft)"),
                InputField("width", "Room width (ft)"),
                InputField("height", "Wall height (ft)"),
                InputField("doors", "Number of doors"),
                InputField("windows", "Number of windows"),
                InputField("coats", "Coats of paint")
            )
        ) { i, p ->
            val area = max(0.0, 2.0 * (i.v("length") + i.v("width")) * i.v("height") - i.v("doors") * 20.0 - i.v("windows") * 15.0)
            val coats = if (i.v("coats") < 1.0) 1.0 else i.v("coats")
            val gallons = area * coats / 350.0
            val buy = ceil(gallons)
            CalcResult(
                f1(gallons) + " gal",
                "Paint needed",
                "Paintable wall area: " + f1(area) + " sq ft\nCoats: " + coats.toInt() + "\nGallons to buy: " + buy.toInt() + "\nCoverage: 350 sq ft per gallon",
                buy * p.v("paint_gal")
            )
        },
        CalcDef(
            "tile", "Flooring/Tile", "Flooring or tile for a room", R.drawable.ic_tile,
            listOf(
                InputField("length", "Room length (ft)"),
                InputField("width", "Room width (ft)"),
                InputField("waste", "Waste allowance (%)")
            )
        ) { i, p ->
            val area = i.v("length") * i.v("width")
            val total = area * (1.0 + i.v("waste") / 100.0)
            CalcResult(
                f1(total) + " sq ft",
                "Material to buy",
                "Room area: " + f1(area) + " sq ft\nWaste allowance: " + f1(i.v("waste")) + "%",
                total * p.v("tile_sqft")
            )
        },
        CalcDef(
            "concrete", "Concrete/Volume", "Concrete for a slab", R.drawable.ic_concrete,
            listOf(
                InputField("length", "Slab length (ft)"),
                InputField("width", "Slab width (ft)"),
                InputField("depth", "Thickness (in)")
            )
        ) { i, p ->
            val cuFt = i.v("length") * i.v("width") * i.v("depth") / 12.0
            val cuYd = cuFt / 27.0 * 1.1
            val bags = ceil(cuFt * 1.1 / 0.6)
            CalcResult(
                f2(cuYd) + " cu yd",
                "Concrete needed (incl. 10% extra)",
                "Volume: " + f1(cuFt) + " cu ft\n80 lb bags (0.6 cu ft each): " + bags.toInt(),
                cuYd * p.v("concrete_cuyd")
            )
        },
        CalcDef(
            "area", "Area/Dimensions", "Area and perimeter of a rectangle", R.drawable.ic_area,
            listOf(
                InputField("length", "Length (ft)"),
                InputField("width", "Width (ft)")
            )
        ) { i, _ ->
            val area = i.v("length") * i.v("width")
            val perimeter = 2.0 * (i.v("length") + i.v("width"))
            CalcResult(
                f1(area) + " sq ft",
                "Total area",
                "Square yards: " + f2(area / 9.0) + "\nSquare meters: " + f2(area * 0.092903) + "\nPerimeter: " + f1(perimeter) + " ft",
                null
            )
        },
        CalcDef(
            "wallpaper", "Wallpaper", "Rolls of wallpaper for a room", R.drawable.ic_wallpaper,
            listOf(
                InputField("length", "Room length (ft)"),
                InputField("width", "Room width (ft)"),
                InputField("height", "Wall height (ft)"),
                InputField("openings", "Doors and windows")
            )
        ) { i, p ->
            val area = max(0.0, 2.0 * (i.v("length") + i.v("width")) * i.v("height") - i.v("openings") * 18.0)
            val rolls = ceil(area * 1.15 / 56.0)
            CalcResult(
                rolls.toInt().toString() + " rolls",
                "Wallpaper needed",
                "Wall area: " + f1(area) + " sq ft\nIncludes 15% for pattern match\nAssumes 56 sq ft per roll",
                rolls * p.v("wallpaper_roll")
            )
        },
        CalcDef(
            "landscaping", "Landscaping", "Mulch or gravel for a bed", R.drawable.ic_landscape,
            listOf(
                InputField("length", "Bed length (ft)"),
                InputField("width", "Bed width (ft)"),
                InputField("depth", "Depth (in)")
            )
        ) { i, p ->
            val cuYd = i.v("length") * i.v("width") * (i.v("depth") / 12.0) / 27.0
            val mulch = cuYd * p.v("mulch_cuyd")
            val gravel = cuYd * p.v("gravel_cuyd")
            CalcResult(
                f2(cuYd) + " cu yd",
                "Mulch or gravel needed",
                "Mulch estimate: " + money(mulch) + "\nGravel estimate: " + money(gravel),
                mulch
            )
        },
        CalcDef(
            "boardfeet", "Board Feet", "Lumber volume in board feet", R.drawable.ic_board,
            listOf(
                InputField("thickness", "Thickness (in)"),
                InputField("width", "Width (in)"),
                InputField("length", "Length (ft)"),
                InputField("qty", "Number of boards")
            )
        ) { i, p ->
            val qty = if (i.v("qty") < 1.0) 1.0 else i.v("qty")
            val bf = i.v("thickness") * i.v("width") * i.v("length") / 12.0 * qty
            CalcResult(
                f2(bf) + " BF",
                "Total board feet",
                "Boards: " + qty.toInt() + "\nBoard feet per board: " + f2(bf / qty),
                bf * p.v("board_foot")
            )
        }
    )

    fun find(key: String): CalcDef? = all.firstOrNull { it.key == key }
}
