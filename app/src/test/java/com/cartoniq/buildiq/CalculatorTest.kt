package com.cartoniq.buildiq

import com.cartoniq.buildiq.data.ProjectEntity
import com.cartoniq.buildiq.model.CalculationItem
import com.cartoniq.buildiq.model.Calculators
import com.cartoniq.buildiq.model.UnitSystem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.sqrt

class CalculatorTest {

    private fun assertClose(expected: Double, actual: Double, delta: Double = 0.05) {
        assertTrue("Expected $expected, got $actual", abs(expected - actual) <= delta)
    }

    @Test
    fun testUnitConversions() {
        val ft = 10.0
        val m = ft * UnitSystem.FT_TO_M
        assertClose(3.048, m)
        assertClose(10.0, m * UnitSystem.M_TO_FT)

        val sqFt = 100.0
        val sqM = sqFt * UnitSystem.SQFT_TO_SQM
        assertClose(9.29, sqM)

        val gal = 5.0
        val liters = gal * UnitSystem.GAL_TO_L
        assertClose(18.927, liters)

        val cuYd = 2.0
        val cuM = cuYd * UnitSystem.CUYD_TO_CUM
        assertClose(1.529, cuM)
    }

    @Test
    fun testPaintCalculatorImperial() {
        val calc = Calculators.find("paint")
        assertNotNull(calc)

        val inputs = mapOf(
            "length" to 15.0,
            "width" to 10.0,
            "height" to 8.0,
            "doors" to 1.0,
            "windows" to 2.0,
            "coats" to 2.0,
            "waste" to 10.0,
            "coverage" to 350.0
        )
        val prices = mapOf("paint_gal" to 40.0)

        // Gross area = 2 * (15 + 10) * 8 = 400 sq ft
        // Deductions = 1*20 + 2*15 = 50 sq ft
        // Net area = 350 sq ft
        // Total coated = 350 * 2 * 1.10 = 770 sq ft
        // Gallons = 770 / 350 = 2.2 gal -> 3 gal to buy
        val result = calc!!.compute(inputs, prices, UnitSystem.IMPERIAL)
        assertEquals(3.0, result.primaryQuantity, 0.0)
        assertEquals(120.0, result.cost!!, 0.0)
    }

    @Test
    fun testFlooringCalculator() {
        val calc = Calculators.find("flooring")
        assertNotNull(calc)

        val inputs = mapOf(
            "length" to 20.0,
            "width" to 15.0,
            "waste" to 10.0,
            "box_sqft" to 25.0
        )
        val prices = mapOf("flooring_sqft" to 5.0)

        // Room Area = 300 sq ft
        // Area with 10% waste = 330 sq ft
        // Boxes needed = ceil(330 / 25) = 14 boxes
        // Purchased sq ft = 14 * 25 = 350 sq ft
        // Cost = 350 * 5.0 = 1750.0
        val result = calc!!.compute(inputs, prices, UnitSystem.IMPERIAL)
        assertEquals(14.0, result.primaryQuantity, 0.0)
        assertEquals(1750.0, result.cost!!, 0.0)
    }

    @Test
    fun testTileCalculatorWithCustomSize() {
        val calc = Calculators.find("tile")
        assertNotNull(calc)

        val inputs = mapOf(
            "length" to 10.0,
            "width" to 10.0,
            "tile_l" to 12.0,
            "tile_w" to 12.0,
            "waste" to 10.0,
            "tiles_per_box" to 10.0
        )
        val prices = mapOf("tile_sqft" to 4.0)

        // Area = 100 sq ft, 12x12 tile = 1 sq ft
        // Exact tiles = 100
        // With 10% waste = ceil(110) = 110 tiles
        // Boxes = ceil(110 / 10) = 11 boxes
        val result = calc!!.compute(inputs, prices, UnitSystem.IMPERIAL)
        assertEquals(110.0, result.primaryQuantity, 0.0)
    }

    @Test
    fun testRoofingPitchMultiplierAndBundles() {
        val calc = Calculators.find("roofing")
        assertNotNull(calc)

        val inputs = mapOf(
            "length" to 40.0,
            "width" to 30.0,
            "pitch" to 6.0, // sqrt(1 + (6/12)^2) = sqrt(1.25) ~ 1.118
            "overhang" to 12.0, // 1 ft each side -> 42 x 32
            "waste" to 10.0
        )
        val prices = mapOf("roofing_bundle" to 35.0, "underlayment_roll" to 50.0)

        val result = calc!!.compute(inputs, prices, UnitSystem.IMPERIAL)
        assertTrue(result.primaryQuantity > 0)
        assertTrue(result.cost!! > 0)
    }

    @Test
    fun testFencingCalculations() {
        val calc = Calculators.find("fencing")
        assertNotNull(calc)

        val inputs = mapOf(
            "length" to 80.0,
            "post_spacing" to 8.0,
            "height" to 6.0,
            "picket_w" to 5.5,
            "picket_gap" to 0.5,
            "waste" to 10.0
        )
        val prices = mapOf(
            "fence_picket" to 3.0,
            "fence_post" to 15.0,
            "concrete_bag" to 6.0
        )

        // Sections = 80 / 8 = 10 sections -> 11 posts
        // Total inches = 80 * 12 = 960 in / 6 in (5.5 + 0.5) = 160 pickets * 1.10 = 176 pickets
        val result = calc!!.compute(inputs, prices, UnitSystem.IMPERIAL)
        assertEquals(176.0, result.primaryQuantity, 0.0)
        assertTrue(result.cost!! > 0)
    }

    @Test
    fun testPricingEngineFullFinancialBreakdown() {
        val item1 = CalculationItem(
            calcKey = "paint",
            title = "Living Room Paint",
            headline = "3 gal",
            label = "Walls",
            details = "Detailed breakdown",
            materialCost = 120.0
        )
        val item2 = CalculationItem(
            calcKey = "flooring",
            title = "Hallway Flooring",
            headline = "14 boxes",
            label = "Planks",
            details = "Detailed breakdown",
            materialCost = 880.0
        )

        // Materials = 1000.0
        // Labor = 10 hrs @ 50/hr = 500.0
        // Direct cost = 1500.0
        // Contingency = 10% of 1500 = 150.0 -> Subtotal = 1650.0
        // Markup = 10% of 1650 = 165.0 -> Taxable Base = 1815.0
        // Sales Tax = 10% of 1815.0 = 181.50
        // Grand Total = 1815.0 + 181.50 = 1996.50
        val project = ProjectEntity.createWithItems(
            title = "Whole Home Refresh",
            summary = "2 calculations",
            items = listOf(item1, item2),
            laborType = "HOURLY",
            laborRate = 50.0,
            laborHours = 10.0,
            contingencyRate = 10.0,
            markupRate = 10.0,
            taxRate = 10.0
        )

        assertEquals(500.0, project.laborCost, 0.01)
        assertEquals(150.0, project.contingencyAmount, 0.01)
        assertEquals(165.0, project.markupAmount, 0.01)
        assertEquals(181.50, project.taxAmount, 0.01)
        assertEquals(1996.50, project.totalCost, 0.01)
    }

    @Test
    fun testProjectItemDeserialization() {
        val item = CalculationItem(
            calcKey = "decking",
            title = "Backyard Deck",
            headline = "350 linear ft",
            label = "Boards",
            details = "20x14 ft deck",
            materialCost = 1330.0
        )
        val project = ProjectEntity.createWithItems(
            title = "Deck Construction",
            summary = "Decking calculation",
            items = listOf(item)
        )

        val restoredItems = project.getItems()
        assertEquals(1, restoredItems.size)
        assertEquals("decking", restoredItems[0].calcKey)
        assertEquals(1330.0, restoredItems[0].materialCost, 0.0)
    }
}
