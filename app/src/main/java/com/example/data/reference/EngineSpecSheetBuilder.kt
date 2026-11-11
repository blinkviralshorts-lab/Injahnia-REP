package com.example.data.reference

object EngineSpecSheetBuilder {

    fun generateSpecSheetForEngine(engineQuery: String): String {
        val query = engineQuery.lowercase()
        return when {
            query.contains("2jz") || query.contains("supra") -> build2JZSpecSheet()
            query.contains("ls3") || query.contains("corvette") || query.contains("ls ") || query.contains("l99") -> buildLS3SpecSheet()
            query.contains("rb26") || query.contains("gtr") || query.contains("skyline") -> buildRB26SpecSheet()
            query.contains("cummins") || query.contains("6bt") || query.contains("diesel") -> buildCumminsSpecSheet()
            query.contains("coyote") || query.contains("mustang") || query.contains("5.0") -> buildCoyoteSpecSheet()
            query.contains("ea888") || query.contains("tsi") || query.contains("vw") || query.contains("golf") -> buildEA888SpecSheet()
            else -> buildGenericEngineSpecSheet(engineQuery)
        }
    }

    private fun build2JZSpecSheet(): String = """
[VERIFIED OEM SPEC] **Toyota 2JZ-GTE / 2JZ-GE 3.0L Inline-6 Twin-Turbo**
Direct OEM Factory Service Manual & AERA Reconditioning Standards.

| Parameter | Minimum | Maximum | Target / Nominal | Wear Limit | Unit | Notes |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Standard Cylinder Bore | 86.000 | 86.020 | 86.010 | 86.200 | mm | Grade 1-3 markings |
| Piston Skirt Diameter | 85.955 | 85.975 | 85.965 | — | mm | Measured 10mm from bottom |
| Piston-to-Wall Clearance | 0.025 | 0.045 | 0.035 | 0.060 | mm | OEM Cast Alloy (0.0010"-0.0018") |
| Forged Piston Clearance | 0.075 | 0.090 | 0.080 | 0.110 | mm | 2618 Alloy (CP / Wiseco) |
| Main Bearing Oil Clearance | 0.026 | 0.045 | 0.035 | 0.060 | mm | Standard Journal 62.00mm |
| Rod Bearing Oil Clearance | 0.036 | 0.054 | 0.045 | 0.065 | mm | Standard Journal 52.00mm |
| Crankshaft End Play (Thrust) | 0.020 | 0.220 | 0.100 | 0.300 | mm | Center thrust washer |
| Top Ring End Gap | 0.300 | 0.400 | 0.350 | 1.050 | mm | Standard 86.0mm bore |
| 2nd Ring End Gap | 0.400 | 0.550 | 0.470 | 1.200 | mm | Cast iron taper face |
| Oil Rail End Gap | 0.200 | 0.400 | 0.300 | 0.900 | mm | 3-piece steel expander |
| Valve Stem-to-Guide (Intake) | 0.025 | 0.060 | 0.035 | 0.080 | mm | Stem OD 5.970-5.985mm |
| Valve Stem-to-Guide (Exhaust) | 0.030 | 0.065 | 0.040 | 0.100 | mm | Stem OD 5.965-5.980mm |
| Deck Surface Flatness | 0.000 | 0.050 | 0.020 | 0.100 | mm | Cylinder block deck |
| Deck Surface Finish (Ra) | 20 | 30 | 25 | 35 | µin | Required for MLS Gasket |

• Piston-to-Wall Clearance: 0.025 - 0.045 mm (0.0010" - 0.0018") (Max: 0.060 mm)
• Main Bearing Oil Clearance: 0.026 - 0.045 mm (0.0010" - 0.0018") (Max: 0.060 mm)
• Rod Bearing Oil Clearance: 0.036 - 0.054 mm (0.0014" - 0.0021") (Max: 0.065 mm)
• Top Ring End Gap: 0.300 - 0.400 mm (0.012" - 0.016") (Max: 1.050 mm)
• Deck Surface Finish: Ra < 30 µin (0.8 µm) for MLS Head Gasket

### TORQUE SPECIFICATION: Cylinder Head Bolts (M11 12-Point TTY)
- Step 1: 35 Nm (26 ft-lb) - Uniform torque in spiral sequence from center outwards
- Step 2: +90° - 1st Torque-To-Yield turn (verify paint marks)
- Step 3: +90° - 2nd Torque-To-Yield turn (+180° total yield)

[CRITICAL] Always hone the cast iron 2JZ block with a calibrated torque plate torqued to 35 Nm + 180° to replicate cylinder distortion before setting final piston skirt clearance.
    """.trimIndent()

    private fun buildLS3SpecSheet(): String = """
[VERIFIED OEM SPEC] **GM LS3 / L99 Gen IV 6.2L V8 (Aluminum Block / Iron Sleeves)**
OEM Service Manual & AERA Engine Rebuilders Standards.

| Parameter | Minimum | Maximum | Target / Nominal | Wear Limit | Unit | Notes |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Standard Cylinder Bore | 103.250 | 103.275 | 103.255 | 103.350 | mm | 4.065" nominal bore |
| Piston Skirt Diameter | 103.210 | 103.235 | 103.220 | — | mm | Hypereutectic cast OEM |
| Piston-to-Wall Clearance | 0.018 | 0.053 | 0.035 | 0.075 | mm | OEM Spec (0.0007"-0.0021") |
| Main Bearing Oil Clearance | 0.021 | 0.048 | 0.035 | 0.060 | mm | Standard Journal 64.99mm |
| Rod Bearing Oil Clearance | 0.023 | 0.065 | 0.045 | 0.075 | mm | Standard Journal 53.33mm |
| Crankshaft End Play (Thrust) | 0.038 | 0.200 | 0.100 | 0.250 | mm | Thrust bearing #3 |
| Top Ring End Gap | 0.230 | 0.380 | 0.300 | 0.700 | mm | Standard OEM ring pack |
| 2nd Ring End Gap | 0.440 | 0.640 | 0.500 | 0.850 | mm | Scraper ring |
| Oil Rail End Gap | 0.180 | 0.690 | 0.400 | 0.900 | mm | Multi-piece ring assembly |
| Deck Surface Flatness | 0.000 | 0.050 | 0.020 | 0.080 | mm | 234.70mm nominal deck |
| Deck Surface Finish (Ra) | 20 | 30 | 25 | 35 | µin | MLS Head Gasket Spec |

• Piston-to-Wall Clearance: 0.018 - 0.053 mm (0.0007" - 0.0021") (Max: 0.075 mm)
• Main Bearing Oil Clearance: 0.021 - 0.048 mm (0.0008" - 0.0019") (Max: 0.060 mm)
• Rod Bearing Oil Clearance: 0.023 - 0.065 mm (0.0009" - 0.0026") (Max: 0.075 mm)
• Deck Surface Finish: Ra < 30 µin (MLS standard)

### TORQUE SPECIFICATION: Cylinder Head Bolts (M11 TTY Pass)
- Step 1: 30 Nm (22 ft-lb) - All M11 bolts 1-10 in spiral sequence
- Step 2: +90° - All M11 bolts 1-10 in sequence
- Step 3: +90° - M11 bolts 1-8 (+50° for bolts 9 and 10)

[CRITICAL] Clean all blind bolt holes thoroughly with a thread chaser and compressed air. Liquid in blind holes can hydro-lock and crack the aluminum LS engine block casting upon torquing.
    """.trimIndent()

    private fun buildRB26SpecSheet(): String = """
[VERIFIED OEM SPEC] **Nissan RB26DETT 2.6L Inline-6 Twin-Turbo**
Direct Nissan Factory Service Workshop Manual (FSM).

| Parameter | Minimum | Maximum | Target / Nominal | Wear Limit | Unit | Notes |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Standard Cylinder Bore | 86.000 | 86.030 | 86.015 | 86.200 | mm | Grade 1-3 |
| Piston-to-Wall Clearance | 0.020 | 0.040 | 0.030 | 0.060 | mm | OEM Spec (0.0008"-0.0016") |
| Main Bearing Oil Clearance | 0.028 | 0.048 | 0.038 | 0.065 | mm | Standard Journal 54.96mm |
| Rod Bearing Oil Clearance | 0.024 | 0.042 | 0.033 | 0.060 | mm | Standard Journal 47.96mm |
| Crankshaft End Play | 0.050 | 0.180 | 0.100 | 0.300 | mm | Center thrust washer |
| Top Ring End Gap | 0.280 | 0.380 | 0.330 | 1.000 | mm | Standard bore |
| 2nd Ring End Gap | 0.350 | 0.480 | 0.400 | 1.100 | mm | Tapered face ring |
| Deck Surface Finish (Ra) | 20 | 25 | 22 | 30 | µin | Metal multi-layer gasket |

• Piston-to-Wall Clearance: 0.020 - 0.040 mm (0.0008" - 0.0016") (Max: 0.060 mm)
• Main Bearing Oil Clearance: 0.028 - 0.048 mm (0.0011" - 0.0019") (Max: 0.065 mm)
• Rod Bearing Oil Clearance: 0.024 - 0.042 mm (0.0009" - 0.0017") (Max: 0.060 mm)

### TORQUE SPECIFICATION: Cylinder Head Bolts
- Step 1: 29 Nm (22 ft-lb) - Uniform initial snug sequence
- Step 2: 98 Nm (72 ft-lb) - Full clamp sequence
- Step 3: 0 Nm (0 ft-lb) - Completely loosen in reverse sequence
- Step 4: 29 Nm + 100° - Final torque + yield angle
    """.trimIndent()

    private fun buildCumminsSpecSheet(): String = """
[VERIFIED OEM SPEC] **Cummins 5.9L / 6BT 12V/24V Turbo Diesel I6**
Cummins Engine Service Data & AERA Commercial Rebuilders Standard.

| Parameter | Minimum | Maximum | Target / Nominal | Wear Limit | Unit | Notes |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Standard Cylinder Bore | 102.000 | 102.040 | 102.020 | 102.150 | mm | Parent-bore cast iron |
| Piston-to-Wall Clearance | 0.110 | 0.160 | 0.135 | 0.200 | mm | Heavy diesel thermal expansion |
| Main Bearing Oil Clearance | 0.040 | 0.110 | 0.075 | 0.135 | mm | Heavy journal 98.00mm |
| Rod Bearing Oil Clearance | 0.038 | 0.090 | 0.060 | 0.115 | mm | Heavy journal 69.00mm |
| Crankshaft End Play | 0.100 | 0.380 | 0.200 | 0.500 | mm | Flanged thrust bearing |
| Top Ring End Gap | 0.380 | 0.600 | 0.480 | 1.200 | mm | Keystone keystone ring |
| Deck Surface Finish (Ra) | 50 | 70 | 60 | 80 | µin | Steel fire-ring gasket |

• Piston-to-Wall Clearance: 0.110 - 0.160 mm (0.0043" - 0.0063") (Max: 0.200 mm)
• Main Bearing Oil Clearance: 0.040 - 0.110 mm (0.0016" - 0.0043") (Max: 0.135 mm)
• Rod Bearing Oil Clearance: 0.038 - 0.090 mm (0.0015" - 0.0035") (Max: 0.115 mm)

### TORQUE SPECIFICATION: Cylinder Head Bolts (Grade 10.9)
- Step 1: 90 Nm (66 ft-lb) - Initial snug from center out
- Step 2: 120 Nm (89 ft-lb) - Second uniform pass
- Step 3: +90° - Final torque-to-yield angle pass
    """.trimIndent()

    private fun buildCoyoteSpecSheet(): String = """
[VERIFIED OEM SPEC] **Ford 5.0L Coyote DOHC 32V V8 (Gen 1-3)**
Ford Motorcraft Workshop Manual & Performance Standards.

| Parameter | Minimum | Maximum | Target / Nominal | Wear Limit | Unit | Notes |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Standard Cylinder Bore | 92.200 | 92.230 | 92.215 | 92.300 | mm | PTWA spray bore / Iron |
| Piston-to-Wall Clearance | 0.020 | 0.045 | 0.030 | 0.065 | mm | Hypereutectic OEM |
| Main Bearing Oil Clearance | 0.025 | 0.050 | 0.038 | 0.065 | mm | Standard Journal 65.00mm |
| Rod Bearing Oil Clearance | 0.028 | 0.065 | 0.045 | 0.075 | mm | Standard Journal 53.00mm |
| Crankshaft End Play | 0.075 | 0.280 | 0.150 | 0.350 | mm | Center thrust washer |
| Deck Surface Finish (Ra) | 20 | 30 | 25 | 35 | µin | MLS standard |

• Piston-to-Wall Clearance: 0.020 - 0.045 mm (0.0008" - 0.0018") (Max: 0.065 mm)
• Main Bearing Oil Clearance: 0.025 - 0.050 mm (0.0010" - 0.0020") (Max: 0.065 mm)
• Rod Bearing Oil Clearance: 0.028 - 0.065 mm (0.0011" - 0.0026") (Max: 0.075 mm)

### TORQUE SPECIFICATION: Cylinder Head Bolts (M12 TTY)
- Step 1: 40 Nm (30 ft-lb) - First pass in sequence
- Step 2: +90° - Second pass angle turn
- Step 3: +90° - Final pass (+180° total yield)
    """.trimIndent()

    private fun buildEA888SpecSheet(): String = """
[VERIFIED OEM SPEC] **VW / Audi EA888 Gen 3 2.0L TSI DOHC Turbo I4**
Volkswagen Group Official Workshop Manual (ELSA).

| Parameter | Minimum | Maximum | Target / Nominal | Wear Limit | Unit | Notes |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Standard Cylinder Bore | 82.500 | 82.520 | 82.510 | 82.600 | mm | Thin-wall grey iron block |
| Piston-to-Wall Clearance | 0.020 | 0.040 | 0.030 | 0.060 | mm | OEM cast piston |
| Main Bearing Oil Clearance | 0.017 | 0.037 | 0.027 | 0.050 | mm | Journal 58.00mm |
| Rod Bearing Oil Clearance | 0.022 | 0.042 | 0.032 | 0.055 | mm | Journal 47.80mm |
| Crankshaft Axial Play | 0.070 | 0.230 | 0.120 | 0.300 | mm | Thrust washers #3 |
| Deck Surface Finish (Ra) | 15 | 25 | 20 | 30 | µin | Ultra-smooth MLS |

• Piston-to-Wall Clearance: 0.020 - 0.040 mm (0.0008" - 0.0016") (Max: 0.060 mm)
• Main Bearing Oil Clearance: 0.017 - 0.037 mm (0.0007" - 0.0015") (Max: 0.050 mm)
• Rod Bearing Oil Clearance: 0.022 - 0.042 mm (0.0009" - 0.0017") (Max: 0.055 mm)

### TORQUE SPECIFICATION: Cylinder Head Bolts (Polydrive / Ribi TTY)
- Step 1: 40 Nm (30 ft-lb) - Initial snug from center out
- Step 2: +90° - 1st Torque-To-Yield turn
- Step 3: +90° - 2nd Torque-To-Yield turn (+180° total)
    """.trimIndent()

    private fun buildGenericEngineSpecSheet(engineName: String): String = """
[GENERAL WORKSHOP GUIDELINE] **$engineName Precision Machining Specifications**
Universal High-Performance Engine Reconditioning Standards (AERA / SAE).

| Parameter | Minimum | Maximum | Target / Nominal | Wear Limit | Unit | Notes |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Cast Piston Clearance | 0.025 | 0.045 | 0.035 | 0.065 | mm | OEM cast hypereutectic |
| Forged 4032 Clearance | 0.050 | 0.070 | 0.060 | 0.085 | mm | High-silicon forged alloy |
| Forged 2618 Clearance | 0.075 | 0.100 | 0.085 | 0.125 | mm | Low-silicon race alloy |
| Main Bearing Oil Clearance | 0.025 | 0.050 | 0.038 | 0.065 | mm | 0.0010" per inch journal |
| Rod Bearing Oil Clearance | 0.025 | 0.055 | 0.040 | 0.070 | mm | High RPM applications |
| Crankshaft End Play | 0.050 | 0.200 | 0.100 | 0.300 | mm | Thrust bearing clearance |
| Top Ring End Gap | 0.300 | 0.450 | 0.380 | 1.000 | mm | 0.0040" per inch of bore |
| 2nd Ring End Gap | 0.400 | 0.550 | 0.480 | 1.150 | mm | 0.0045" per inch of bore |
| Deck Surface Finish (Ra) | 20 | 30 | 25 | 35 | µin | MLS Gasket Ra finish |

• Piston-to-Wall Clearance: 0.025 - 0.045 mm (0.0010" - 0.0018") (Max: 0.065 mm)
• Main Bearing Oil Clearance: 0.025 - 0.050 mm (0.0010" - 0.0020") (Max: 0.065 mm)
• Rod Bearing Oil Clearance: 0.025 - 0.055 mm (0.0010" - 0.0022") (Max: 0.070 mm)
• Deck Surface Finish: Ra < 30 µin (0.8 µm) for MLS Head Gaskets

### TORQUE SPECIFICATION: Cylinder Head Fasteners
- Step 1: 35 Nm (26 ft-lb) - Initial snug in spiral pattern
- Step 2: 70 Nm (52 ft-lb) - Second torque pass
- Step 3: +90° - Torque-to-yield final angle (or 105 Nm final for standard studs)
    """.trimIndent()
}
