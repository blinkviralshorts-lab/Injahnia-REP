package com.example

import com.example.data.reference.WorkshopReferenceData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InjahniaMachiningTest {

    @Test
    fun testPistonClearanceCalculation() {
        val cylinderBoreMm = 86.000
        val pistonSkirtMm = 85.920
        val clearanceMm = cylinderBoreMm - pistonSkirtMm
        val clearanceInch = clearanceMm / 25.4

        assertEquals(0.080, clearanceMm, 0.0001)
        assertTrue("Clearance in inches should be ~0.00315\"", clearanceInch > 0.0030 && clearanceInch < 0.0033)
    }

    @Test
    fun testReferenceEnginesIntegrity() {
        val engines = WorkshopReferenceData.ENGINES
        assertTrue("Should have verified engine library entries", engines.isNotEmpty())

        val jz = engines.find { it.id == "toyota_2jzgte" }
        assertNotNull("Toyota 2JZ-GTE should exist", jz)
        assertEquals(86.00, jz!!.boreMm, 0.001)
        assertEquals(62.00, jz.mainJournalMm, 0.001)
    }

    @Test
    fun testMachiningOperationsLoaded() {
        val ops = WorkshopReferenceData.OPERATIONS
        assertTrue("Should have standard machining operation guides", ops.isNotEmpty())
        val honing = ops.find { it.category == "Cylinder Bore & Honing" }
        assertNotNull(honing)
    }

    @Test
    fun testThemeStateToggling() {
        val viewModel = com.example.viewmodel.InjahniaViewModel()
        assertTrue("Default theme should be dark mode for workshop use", viewModel.isDarkTheme.value)

        viewModel.toggleDarkTheme()
        org.junit.Assert.assertFalse("Theme should switch to light mode", viewModel.isDarkTheme.value)

        viewModel.setDarkTheme(true)
        assertTrue("Theme should switch back to dark mode", viewModel.isDarkTheme.value)

        viewModel.setDarkTheme(false)
        org.junit.Assert.assertFalse("setDarkTheme(false) should switch to light mode", viewModel.isDarkTheme.value)
    }
}
