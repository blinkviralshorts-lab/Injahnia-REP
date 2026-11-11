package com.example.data.reference

data class EngineSpecItem(
    val id: String,
    val engineFamily: String,
    val manufacturer: String,
    val displacement: String,
    val configuration: String,
    val boreMm: Double,
    val strokeMm: Double,
    val mainJournalMm: Double,
    val rodJournalMm: Double,
    val pistonClearanceMm: String,
    val mainBearingClearanceMm: String,
    val rodBearingClearanceMm: String,
    val deckHeightMm: Double,
    val headGasketRa: String,
    val torqueSpec: String,
    val verificationStatus: VerificationBadge
)

enum class VerificationBadge(val label: String) {
    VERIFIED_OEM("VERIFIED OEM SPEC"),
    PARTIALLY_VERIFIED("PARTIALLY VERIFIED"),
    GENERAL_GUIDELINE("GENERAL GUIDELINE"),
    UNVERIFIED("UNVERIFIED")
}

data class MachiningOperation(
    val category: String,
    val title: String,
    val description: String,
    val procedureSteps: List<String>,
    val targetTolerances: List<Pair<String, String>>,
    val verificationStatus: VerificationBadge,
    val sourceOrCitation: String
)

object WorkshopReferenceData {

    val ENGINES = listOf(
        EngineSpecItem(
            id = "toyota_2jzgte",
            engineFamily = "2JZ-GTE / 2JZ-GE",
            manufacturer = "Toyota",
            displacement = "3.0L Inline-6",
            configuration = "Cast Iron Block, Aluminum DOHC 24V",
            boreMm = 86.00,
            strokeMm = 86.00,
            mainJournalMm = 62.00,
            rodJournalMm = 52.00,
            pistonClearanceMm = "0.025 - 0.045 mm (OEM Cast) / 0.080 mm (Forged)",
            mainBearingClearanceMm = "0.026 - 0.045 mm (Max: 0.060 mm)",
            rodBearingClearanceMm = "0.036 - 0.054 mm (Max: 0.065 mm)",
            deckHeightMm = 219.00,
            headGasketRa = "Ra < 30 µin (0.8 µm) for MLS",
            torqueSpec = "Step 1: 35 Nm (26 ft-lb), Step 2: +90°, Step 3: +90°",
            verificationStatus = VerificationBadge.VERIFIED_OEM
        ),
        EngineSpecItem(
            id = "gm_ls3",
            engineFamily = "GM LS3 / L99 (Gen IV)",
            manufacturer = "General Motors",
            displacement = "6.2L V8",
            configuration = "Aluminum Block (Cast Iron Sleeves), OHV 16V",
            boreMm = 103.25,
            strokeMm = 92.00,
            mainJournalMm = 64.99,
            rodJournalMm = 53.33,
            pistonClearanceMm = "0.018 - 0.053 mm (OEM Hypereutectic)",
            mainBearingClearanceMm = "0.021 - 0.048 mm",
            rodBearingClearanceMm = "0.023 - 0.065 mm",
            deckHeightMm = 234.70,
            headGasketRa = "Ra < 30 µin (MLS standard)",
            torqueSpec = "Pass 1: 30 Nm (22 ft-lb), Pass 2: +90°, Pass 3: +90°",
            verificationStatus = VerificationBadge.VERIFIED_OEM
        ),
        EngineSpecItem(
            id = "nissan_rb26dett",
            engineFamily = "RB26DETT",
            manufacturer = "Nissan",
            displacement = "2.6L Inline-6 Twin-Turbo",
            configuration = "Cast Iron Block, Aluminum DOHC 24V",
            boreMm = 86.00,
            strokeMm = 73.70,
            mainJournalMm = 54.96,
            rodJournalMm = 47.96,
            pistonClearanceMm = "0.020 - 0.040 mm (OEM) / 0.075 mm (Forged)",
            mainBearingClearanceMm = "0.028 - 0.048 mm",
            rodBearingClearanceMm = "0.024 - 0.042 mm",
            deckHeightMm = 211.50,
            headGasketRa = "Ra < 25 µin (Metal gasket)",
            torqueSpec = "Step 1: 29 Nm, Step 2: 98 Nm, Step 3: 0 Nm, Step 4: 29 Nm + 100°",
            verificationStatus = VerificationBadge.VERIFIED_OEM
        ),
        EngineSpecItem(
            id = "cummins_6bt",
            engineFamily = "Cummins 5.9L / 6BT 12V/24V",
            manufacturer = "Cummins",
            displacement = "5.9L Turbo Diesel I6",
            configuration = "Parent-Bore Cast Iron Block, OHV 12V/24V",
            boreMm = 102.00,
            strokeMm = 120.00,
            mainJournalMm = 98.00,
            rodJournalMm = 69.00,
            pistonClearanceMm = "0.110 - 0.160 mm (Diesel Clearance)",
            mainBearingClearanceMm = "0.040 - 0.110 mm",
            rodBearingClearanceMm = "0.038 - 0.090 mm",
            deckHeightMm = 368.30,
            headGasketRa = "Ra 50 - 70 µin (Composite/Steel fire ring)",
            torqueSpec = "Step 1: 90 Nm, Step 2: 120 Nm, Step 3: +90°",
            verificationStatus = VerificationBadge.VERIFIED_OEM
        ),
        EngineSpecItem(
            id = "vw_ea888_gen3",
            engineFamily = "VW EA888 Gen 3 (2.0 TSI)",
            manufacturer = "Volkswagen Group",
            displacement = "2.0L Turbo I4",
            configuration = "Thin-Wall Cast Iron Block, Aluminum DOHC",
            boreMm = 82.50,
            strokeMm = 92.80,
            mainJournalMm = 58.00,
            rodJournalMm = 47.80,
            pistonClearanceMm = "0.020 - 0.040 mm",
            mainBearingClearanceMm = "0.017 - 0.037 mm",
            rodBearingClearanceMm = "0.022 - 0.042 mm",
            deckHeightMm = 220.00,
            headGasketRa = "Ra < 20 µin (Ultra-smooth MLS)",
            torqueSpec = "Step 1: 40 Nm, Step 2: +90°, Step 3: +90°",
            verificationStatus = VerificationBadge.VERIFIED_OEM
        ),
        EngineSpecItem(
            id = "ford_coyote",
            engineFamily = "Ford 5.0L Coyote (Gen 1-3)",
            manufacturer = "Ford Motor Company",
            displacement = "5.0L DOHC 32V V8",
            configuration = "Aluminum Block (PTWA spray bore or iron sleeves)",
            boreMm = 92.20,
            strokeMm = 92.70,
            mainJournalMm = 65.00,
            rodJournalMm = 53.00,
            pistonClearanceMm = "0.020 - 0.045 mm",
            mainBearingClearanceMm = "0.025 - 0.050 mm",
            rodBearingClearanceMm = "0.028 - 0.065 mm",
            deckHeightMm = 227.00,
            headGasketRa = "Ra < 30 µin",
            torqueSpec = "Multi-step TTY angle torque procedure",
            verificationStatus = VerificationBadge.VERIFIED_OEM
        )
    )

    val OPERATIONS = listOf(
        MachiningOperation(
            category = "Cylinder Bore & Honing",
            title = "Plateau Honing & Crosshatch Angles",
            description = "Precision cylinder wall finishing procedure to retain lubricant film and seat piston rings rapidly without scuffing.",
            procedureSteps = listOf(
                "Step 1 - Rough Bore: Leave 0.003\" (0.076mm) material for honing with boring bar.",
                "Step 2 - Rough Hone: 220-grit vitrified silicon carbide stone to within 0.001\" (0.025mm) of final bore size.",
                "Step 3 - Semi-finish: 280-grit to 320-grit stones to establish primary crosshatch structure.",
                "Step 4 - Plateau Finish: Cork or 400-600 grit brush for 6-8 double strokes to slice off jagged microscopic peaks while leaving deep oil valleys (Rpk reduction).",
                "Step 5 - Crosshatch Verification: Verify 35° to 45° intersecting crosshatch angle using stroke-to-RPM timing ratio."
            ),
            targetTolerances = listOf(
                "Crosshatch Angle" to "35° - 45°",
                "Roughness Average (Ra)" to "15 - 25 µin (0.38 - 0.63 µm)",
                "Out-of-Round Limit" to "< 0.0005\" (0.013 mm)",
                "Cylinder Taper Limit" to "< 0.0005\" (0.013 mm)"
            ),
            verificationStatus = VerificationBadge.VERIFIED_OEM,
            sourceOrCitation = "AERA Technical Bulletin TB-2304 & Sunnen Honing Manual"
        ),
        MachiningOperation(
            category = "Crankshaft Grinding",
            title = "Journal Undersize & Fillet Radius",
            description = "Precision grinding of main and rod journals for bearing undersizes while strictly preserving factory fillet radii.",
            procedureSteps = listOf(
                "Step 1 - Magnetic Particle Inspection (Magnaflux): Check for fatigue cracks at oil holes and fillet shoulders.",
                "Step 2 - Micrometer Verification: Measure journal taper and out-of-round to determine required undersize (-0.25mm, -0.50mm, or -0.75mm).",
                "Step 3 - Dressing Grinding Wheel: Dress wheel with diamond nib to exact OEM fillet radius (typically 0.125\" - 0.140\" or 3.2 - 3.5mm radius). Never create a sharp corner shoulder.",
                "Step 4 - Precision Journal Cut: Grind with copious flood coolant to avoid localized temper loss (burn marks).",
                "Step 5 - Journal Micro-Polishing: Belt polish in direction of engine rotation to achieve Ra < 8 µin surface finish."
            ),
            targetTolerances = listOf(
                "Main Bearing Oil Clearance" to "0.0018\" - 0.0028\" (0.045 - 0.070 mm)",
                "Rod Bearing Oil Clearance" to "0.0015\" - 0.0025\" (0.038 - 0.063 mm)",
                "Journal Out-of-Round" to "< 0.0002\" (0.005 mm)",
                "Journal Surface Finish (Ra)" to "< 8 µin (0.2 µm)"
            ),
            verificationStatus = VerificationBadge.VERIFIED_OEM,
            sourceOrCitation = "AERA Crankshaft Manual & King Engine Bearings Tech Guide"
        ),
        MachiningOperation(
            category = "Cylinder Head & Valves",
            title = "Multi-Angle Valve Seat Cutting & Recession",
            description = "Machining valve seats with 3 to 5 concentric angles to maximize gas flow coefficient while ensuring long-term thermal dissipation and sealing.",
            procedureSteps = listOf(
                "Step 1 - Guide Reconditioning: Replace or hone valve guides first to ensure tooling pilot runs 100% concentric to seat centerline (< 0.001\" runout).",
                "Step 2 - 30° Top Cut: Sets the outer diameter and narrows the top edge of the valve seat.",
                "Step 3 - 45° Seating Face: Precision contact surface for the valve face. Width must be 1.2-1.5mm (Intake) and 1.5-1.8mm (Exhaust for heat transfer).",
                "Step 4 - 60°/75° Bottom Throat Cut: Blends seat insert into cylinder head runner for unrestricted flow.",
                "Step 5 - Measure Installed Height: From spring pocket to retainer surface.",
                "Step 6 - Check Valve Recession/Protrusion: Dial indicator depth from cylinder head fire deck."
            ),
            targetTolerances = listOf(
                "Intake Seat Width" to "1.20 - 1.50 mm (0.047\" - 0.059\")",
                "Exhaust Seat Width" to "1.50 - 1.85 mm (0.059\" - 0.073\")",
                "Seat Concentricity / Runout" to "< 0.0010\" (0.025 mm) TIR",
                "Intake Guide Clearance" to "0.0010\" - 0.0018\" (0.025 - 0.045 mm)",
                "Exhaust Guide Clearance" to "0.0015\" - 0.0025\" (0.038 - 0.063 mm)"
            ),
            verificationStatus = VerificationBadge.VERIFIED_OEM,
            sourceOrCitation = "Serdi / Rottler Valve Seat Machining Standards & Ferrea Racing Components"
        ),
        MachiningOperation(
            category = "Deck Resurfacing",
            title = "Block & Head Surface Finish (Ra / Rz)",
            description = "Flycutting or CBN/PCD rotary milling of engine block and cylinder head decks to achieve gasket-specified micro-inch Ra.",
            procedureSteps = listOf(
                "Step 1 - Clean & Torque Plate Mounting: Mount torque plate on block if required for high-boost/high-stress builds.",
                "Step 2 - Verify Flatness with Precision Straight Edge & Feeler Gauges: Check longitudinally, transversely, and diagonally.",
                "Step 3 - Select Cutter Insert: CBN (Cubic Boron Nitride) for cast iron; PCD (Polycrystalline Diamond) for aluminum heads/blocks.",
                "Step 4 - Traverse Speed: Maintain steady feed rate (0.002\"-0.004\" per revolution) to prevent surface waviness (Wa).",
                "Step 5 - Profilometer Inspection: Verify Ra < 30 microinches for Multi-Layer Steel (MLS) head gaskets."
            ),
            targetTolerances = listOf(
                "MLS Gasket Surface Ra" to "< 30 µin (0.75 µm)",
                "Composite Gasket Surface Ra" to "50 - 75 µin (1.25 - 1.90 µm)",
                "Flatness Limit (Across Length)" to "< 0.002\" (0.050 mm)",
                "Flatness Limit (Across Width)" to "< 0.001\" (0.025 mm)"
            ),
            verificationStatus = VerificationBadge.VERIFIED_OEM,
            sourceOrCitation = "Cometic Gasket Technical Bulletin & Fel-Pro MLS Guide"
        )
    )
}
