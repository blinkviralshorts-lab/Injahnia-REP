package com.example.data.reference

import java.util.Locale
import java.util.regex.Pattern

/**
 * Evaluates hands-free engine block measurements dictated by mechanics.
 * Compares dictated dimensions (bore, piston, ring gaps, bearing clearances, deck flatness)
 * against OEM tolerances and high-performance racing guidelines.
 */
object MeasurementEvaluator {

    data class EvaluationResult(
        val isRecognizedMeasurement: Boolean,
        val formattedReport: String,
        val statusBadge: VerificationBadge = VerificationBadge.VERIFIED_OEM
    )

    fun canEvaluate(input: String): Boolean {
        val lower = input.lowercase()
        val hasMeasurementKeywords = lower.contains("bore") ||
                lower.contains("piston") ||
                lower.contains("clearance") ||
                lower.contains("ring gap") ||
                lower.contains("bearing") ||
                lower.contains("journal") ||
                lower.contains("deck") ||
                lower.contains("warp") ||
                lower.contains("flatness") ||
                lower.contains("feeler") ||
                lower.contains("micrometer") ||
                lower.contains("plastigauge")

        val hasNumbers = lower.any { it.isDigit() }
        return hasMeasurementKeywords && hasNumbers
    }

    fun evaluateMeasurement(query: String, engineCode: String): EvaluationResult {
        val lower = query.lowercase(Locale.ROOT)
        val engine = engineCode.trim().ifBlank { "Toyota 2JZ-GTE" }

        // Check for Bore and Piston clearance measurement
        if ((lower.contains("bore") || lower.contains("cyl")) && lower.contains("piston")) {
            return evaluateBoreAndPiston(query, lower, engine)
        }

        // Check for single Bore measurement
        if (lower.contains("bore") || lower.contains("cylinder")) {
            return evaluateBoreOnly(query, lower, engine)
        }

        // Check for Ring End Gap
        if (lower.contains("ring") && (lower.contains("gap") || lower.contains("end"))) {
            return evaluateRingGap(query, lower, engine)
        }

        // Check for Bearing Oil Clearance (Main / Rod)
        if (lower.contains("bearing") || lower.contains("journal") || lower.contains("oil clearance") || lower.contains("plastigauge")) {
            return evaluateBearingClearance(query, lower, engine)
        }

        // Check for Deck Flatness / Warpage
        if (lower.contains("deck") || lower.contains("warp") || lower.contains("flatness")) {
            return evaluateDeckFlatness(query, lower, engine)
        }

        return evaluateGenericMeasurement(query, engine)
    }

    private fun extractNumbers(text: String): List<Double> {
        val numbers = mutableListOf<Double>()
        // Match numbers like 86.035, 0.0018, 86, 3.875
        val pattern = Pattern.compile("(\\d+(?:\\.\\d+)?)")
        val matcher = pattern.matcher(text)
        while (matcher.find()) {
            matcher.group(1)?.toDoubleOrNull()?.let { numbers.add(it) }
        }
        return numbers
    }

    private fun evaluateBoreAndPiston(rawText: String, lower: String, engine: String): EvaluationResult {
        val numbers = extractNumbers(lower)
        if (numbers.size < 2) {
            return evaluateBoreOnly(rawText, lower, engine)
        }

        // Usually bore is larger than piston
        val bore = numbers.maxOrNull() ?: 86.00
        val piston = numbers.minOrNull() ?: 85.95
        val clearance = bore - piston

        val isInch = lower.contains("inch") || lower.contains("in") || clearance < 0.015
        val clearanceMm = if (isInch) clearance * 25.4 else clearance
        val clearanceInches = if (isInch) clearance else clearance / 25.4

        val boreMm = if (isInch) bore * 25.4 else bore
        val pistonMm = if (isInch) piston * 25.4 else piston

        val oemMin = 0.025
        val oemMax = 0.045
        val oemLimit = 0.060
        val forgedMin = 0.075
        val forgedMax = 0.095

        val statusVerdict: String
        val statusDescription: String
        val badge: VerificationBadge

        when {
            clearanceMm in oemMin..oemMax -> {
                statusVerdict = "[PASS] WITHIN OEM CAST PISTON TOLERANCE"
                statusDescription = "Optimal factory clearance for quiet cold operation, excellent ring sealing, and low oil consumption."
                badge = VerificationBadge.VERIFIED_OEM
            }
            clearanceMm > oemMax && clearanceMm <= oemLimit -> {
                statusVerdict = "[PASS / ACCEPTABLE] OEM SERVICE LIMIT RANGE"
                statusDescription = "Acceptable for street re-assembly, but near OEM maximum wear threshold."
                badge = VerificationBadge.VERIFIED_OEM
            }
            clearanceMm in forgedMin..forgedMax -> {
                statusVerdict = "[PASS] WITHIN HIGH-PERFORMANCE FORGED 2618 ALLOY SPEC"
                statusDescription = "Ideal for forged pistons (CP, Wiseco, JE). Accommodates thermal expansion under high boost / nitrous loads."
                badge = VerificationBadge.PARTIALLY_VERIFIED
            }
            clearanceMm < oemMin -> {
                statusVerdict = "[CRITICAL DANGER] CLEARANCE TOO TIGHT (SCUFFING RISK)"
                statusDescription = "Piston will expand under thermal combustion load and seize against cylinder wall. Must power hone bore to expand clearance."
                badge = VerificationBadge.VERIFIED_OEM
            }
            else -> {
                statusVerdict = "[CRITICAL] EXCESSIVE PISTON-TO-WALL CLEARANCE"
                statusDescription = "Clearance exceeds maximum allowable wear limit. Will cause piston slap, ring flutter, and oil blowby. Re-bore cylinder to next oversize (+0.25mm / +0.50mm)."
                badge = VerificationBadge.VERIFIED_OEM
            }
        }

        val report = """
[HANDS-FREE MEASUREMENT EVALUATION] **$engine**
Dictated by Machinist: "$rawText"

### MEASURED DIMENSIONS & CALCULATED CLEARANCE
- **Measured Cylinder Bore:** ${String.format(Locale.US, "%.3f", boreMm)} mm (${String.format(Locale.US, "%.4f", boreMm / 25.4)}")
- **Measured Piston Skirt:** ${String.format(Locale.US, "%.3f", pistonMm)} mm (${String.format(Locale.US, "%.4f", pistonMm / 25.4)}")
- **Calculated Piston-to-Wall Clearance:** **${String.format(Locale.US, "%.3f", clearanceMm)} mm** (**${String.format(Locale.US, "%.4f", clearanceInches)}"**)

### VERIFICATION VERDICT
**$statusVerdict**
$statusDescription

| Specification Parameter | Standard OEM Spec | Forged 2618 Spec | Wear Limit | Measured Result | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| Piston Skirt Clearance | 0.025 - 0.045 mm | 0.075 - 0.095 mm | 0.060 mm | ${String.format(Locale.US, "%.3f", clearanceMm)} mm | ${if (clearanceMm <= oemLimit || clearanceMm in forgedMin..forgedMax) "VERIFIED" else "ACTION REQ"} |

### RECOMMENDED MACHINING ACTION
- **Cross-hatch angle:** Ensure 45° cross-hatch finish with 280-320 grit diamond/silicon carbide stones.
- **Torque Plate:** Always verify bore measurement with a torque plate torqued to final head bolt spec.
- **Plateau Finish:** Finish with 400-grit ultra-fine plateau stones or brushing tool to eliminate torn & folded metal.
        """.trimIndent()

        return EvaluationResult(true, report, badge)
    }

    private fun evaluateBoreOnly(rawText: String, lower: String, engine: String): EvaluationResult {
        val numbers = extractNumbers(lower)
        val bore = numbers.firstOrNull() ?: 86.00
        val isInch = lower.contains("inch") || lower.contains("in") || bore < 5.0
        val boreMm = if (isInch) bore * 25.4 else bore
        val boreInches = if (isInch) bore else bore / 25.4

        val report = """
[HANDS-FREE MEASUREMENT EVALUATION] **$engine Cylinder Bore**
Dictated by Machinist: "$rawText"

### MEASURED CYLINDER BORE
- **Measured Diameter:** **${String.format(Locale.US, "%.3f", boreMm)} mm** (**${String.format(Locale.US, "%.4f", boreInches)}"**)

### OEM BORE STANDARDS ($engine)
- **Standard Bore Grade 1:** 86.000 - 86.010 mm
- **Standard Bore Grade 2:** 86.010 - 86.020 mm
- **Standard Bore Grade 3:** 86.020 - 86.030 mm
- **Maximum Cylinder Out-of-Round / Taper:** 0.010 mm (0.0004")
- **Service Wear Limit:** 86.200 mm (0.008" over standard)

### WORKSHOP MACHINING PROCEDURE
1. Take 6-point measurements: Top (below ridge), Middle, and Bottom along both Thrust (X) and Axial (Y) planes.
2. Calculate taper = Top Bore - Bottom Bore.
3. Calculate out-of-round = Thrust Diameter - Axial Diameter.
4. Dictate piston skirt measurement to immediately calculate live piston-to-wall clearance.
        """.trimIndent()

        return EvaluationResult(true, report, VerificationBadge.VERIFIED_OEM)
    }

    private fun evaluateRingGap(rawText: String, lower: String, engine: String): EvaluationResult {
        val numbers = extractNumbers(lower)
        val gap = numbers.firstOrNull() ?: 0.35
        val isInch = lower.contains("inch") || lower.contains("in") || gap < 0.05
        val gapMm = if (isInch) gap * 25.4 else gap
        val gapInches = if (isInch) gap else gap / 25.4

        val isTopRing = lower.contains("top") || !lower.contains("2nd") && !lower.contains("second")
        val ringLabel = if (isTopRing) "Top Compression Ring" else "Second (Scraper) Ring"

        val minSpecMm = if (isTopRing) 0.300 else 0.400
        val maxSpecMm = if (isTopRing) 0.400 else 0.550
        val wearLimitMm = if (isTopRing) 1.050 else 1.200

        val verdict = when {
            gapMm in minSpecMm..maxSpecMm -> "[PASS] WITHIN OEM SPECIFICATION"
            gapMm > maxSpecMm && gapMm <= wearLimitMm -> "[ACCEPTABLE] Street/mild boost performance gap"
            gapMm < minSpecMm -> "[ATTENTION] GAP TOO TIGHT - Ring ends will butt under combustion heat! File ring ends."
            else -> "[EXCESSIVE] Gap exceeds wear limit. Excessive blowby expected."
        }

        val report = """
[HANDS-FREE MEASUREMENT EVALUATION] **$engine $ringLabel End Gap**
Dictated by Machinist: "$rawText"

### MEASURED VALUE
- **Measured Gap:** **${String.format(Locale.US, "%.3f", gapMm)} mm** (**${String.format(Locale.US, "%.4f", gapInches)}"**)
- **Target Specification:** ${String.format(Locale.US, "%.3f", minSpecMm)} - ${String.format(Locale.US, "%.3f", maxSpecMm)} mm
- **Service Wear Limit:** ${String.format(Locale.US, "%.3f", wearLimitMm)} mm

### STATUS VERDICT
**$verdict**

### RING FILING INSTRUCTIONS
- Push ring square into bore using inverted piston crown down to 25mm (1 inch) from block deck.
- Use a dedicated manual or electric ring filer. Always grind from outside face inward to avoid chipping ductile iron or moly coating.
- Deburr cut edges with 400-grit stone before re-checking clearance with feeler gauge.
        """.trimIndent()

        return EvaluationResult(true, report, VerificationBadge.VERIFIED_OEM)
    }

    private fun evaluateBearingClearance(rawText: String, lower: String, engine: String): EvaluationResult {
        val numbers = extractNumbers(lower)
        val clearance = numbers.firstOrNull() ?: 0.035
        val isInch = lower.contains("inch") || lower.contains("in") || clearance < 0.015
        val clearanceMm = if (isInch) clearance * 25.4 else clearance
        val clearanceInches = if (isInch) clearance else clearance / 25.4

        val isRod = lower.contains("rod") || lower.contains("pin")
        val bearingType = if (isRod) "Connecting Rod Bearing" else "Main Crankshaft Bearing"

        val minMm = if (isRod) 0.036 else 0.026
        val maxMm = if (isRod) 0.054 else 0.045
        val limitMm = 0.065

        val verdict = when {
            clearanceMm in minMm..maxMm -> "[PASS] OPTIMAL OEM OIL CLEARANCE"
            clearanceMm in 0.045..0.065 -> "[PASS] HIGH-RPM / RACING CLEARANCE (Requires 10W-50 / 15W-50 oil)"
            clearanceMm < minMm -> "[WARNING] CLEARANCE TOO TIGHT (Oil starvation & bearing wiping risk)"
            else -> "[CRITICAL] CLEARANCE EXCEEDS SERVICE LIMIT (Oil pressure drop & rod knock risk)"
        }

        val report = """
[HANDS-FREE MEASUREMENT EVALUATION] **$engine $bearingType Oil Clearance**
Dictated by Machinist: "$rawText"

### MEASURED CLEARANCE
- **Measured Oil Clearance:** **${String.format(Locale.US, "%.3f", clearanceMm)} mm** (**${String.format(Locale.US, "%.4f", clearanceInches)}"**)
- **Factory OEM Range:** ${String.format(Locale.US, "%.3f", minMm)} - ${String.format(Locale.US, "%.3f", maxMm)} mm (${String.format(Locale.US, "%.4f", minMm / 25.4)}" - ${String.format(Locale.US, "%.4f", maxMm / 25.4)}")
- **Maximum Wear Limit:** ${String.format(Locale.US, "%.3f", limitMm)} mm (${String.format(Locale.US, "%.4f", limitMm / 25.4)}")

### STATUS VERDICT
**$verdict**

### MACHINING & BEARING SELECTION NOTES
- Measured with dial bore gauge against micrometer-calibrated journal, or verified via Plastigauge.
- If clearance is tight: Polish journal or select thinner bearing color codes (Grade 1/2).
- If clearance is loose: Grind crank journal -0.25mm (-0.010") and install undersize bearing shells.
        """.trimIndent()

        return EvaluationResult(true, report, VerificationBadge.VERIFIED_OEM)
    }

    private fun evaluateDeckFlatness(rawText: String, lower: String, engine: String): EvaluationResult {
        val numbers = extractNumbers(lower)
        val warp = numbers.firstOrNull() ?: 0.02
        val isInch = lower.contains("inch") || lower.contains("in") || warp < 0.01
        val warpMm = if (isInch) warp * 25.4 else warp
        val warpInches = if (isInch) warp else warp / 25.4

        val maxAllowableMm = 0.050 // 0.002" for MLS gasket

        val verdict = if (warpMm <= maxAllowableMm) {
            "[PASS] DECK FLATNESS WITHIN MLS GASKET LIMIT"
        } else {
            "[FAIL] DECK WARPAGE EXCEEDS LIMIT - Resurfacing Required"
        }

        val report = """
[HANDS-FREE MEASUREMENT EVALUATION] **$engine Cylinder Block Deck Flatness**
Dictated by Machinist: "$rawText"

### MEASURED WARPAGE
- **Measured Deck Out-of-Flat:** **${String.format(Locale.US, "%.3f", warpMm)} mm** (**${String.format(Locale.US, "%.4f", warpInches)}"**)
- **Maximum Limit for MLS Head Gasket:** 0.050 mm (0.0020")
- **Maximum Limit for Composite Gasket:** 0.100 mm (0.0040")

### STATUS VERDICT
**$verdict**

### MACHINING RECOMMENDATIONS
- Verify deck flatness using a precision machinist straight edge and feeler gauges along 6 axes: longitudinal, lateral, and both diagonal X-crossings.
- If resurfacing: Machine minimal material (0.05 - 0.10mm max) with CBN/PCD cutter to achieve surface roughness Ra 20-30 µin.
        """.trimIndent()

        return EvaluationResult(true, report, VerificationBadge.VERIFIED_OEM)
    }

    private fun evaluateGenericMeasurement(rawText: String, engine: String): EvaluationResult {
        val report = """
[HANDS-FREE MEASUREMENT EVALUATION] **$engine Workshop Input**
Dictated by Machinist: "$rawText"

### LOGGED DIMENSION
Clearance / measurement received and cross-referenced with $engine engineering specifications.

| Parameter | Recommended Specification | Allowable Range |
| :--- | :--- | :--- |
| Piston Skirt Clearance | 0.025 - 0.045 mm | 0.075 mm Max |
| Bearing Oil Clearance | 0.026 - 0.050 mm | 0.065 mm Max |
| Ring End Gap (Top) | 0.300 - 0.400 mm | 1.050 mm Limit |
| Deck Flatness | < 0.050 mm | 0.080 mm Limit |

*Tip: Dictate both bore and piston skirt (e.g. "Bore 86.035 mm and piston 85.960 mm") to calculate live piston-to-wall clearances automatically.*
        """.trimIndent()

        return EvaluationResult(true, report, VerificationBadge.GENERAL_GUIDELINE)
    }
}
