package com.example.data.model

import com.example.data.reference.VerificationBadge

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val engineCode: String? = null,
    val verificationBadge: VerificationBadge? = null,
    val isPinned: Boolean = false,
    val calculationSummary: String? = null
)

data class SavedBuildItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val engineCode: String,
    val title: String,
    val note: String,
    val specs: Map<String, String>,
    val timestamp: Long = System.currentTimeMillis()
)

data class ClearanceCalcResult(
    val typeName: String,
    val clearanceMm: Double,
    val clearanceInch: Double,
    val minAllowedMm: Double,
    val maxAllowedMm: Double,
    val isWithinSpec: Boolean,
    val recommendations: String
)
