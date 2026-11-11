package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.ApiConnectionStatus
import com.example.data.api.GeminiRepository
import com.example.data.model.ChatMessage
import com.example.data.model.ClearanceCalcResult
import com.example.data.model.SavedBuildItem
import com.example.data.reference.EngineSpecItem
import com.example.data.reference.EngineSpecSheetBuilder
import com.example.data.reference.MachiningOperation
import com.example.data.reference.MeasurementEvaluator
import com.example.data.reference.VerificationBadge
import com.example.data.reference.WorkshopReferenceData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class InjahniaScreen {
    WELCOME,
    DASHBOARD,
    AI_CHAT,
    SAVED_QUERIES,
    REFERENCE_LIBRARY,
    ACCOUNT_SUBSCRIPTION,
    SETTINGS
}

enum class UnitSystem {
    METRIC_MM,
    IMPERIAL_INCH
}

class InjahniaViewModel(
    val repository: GeminiRepository = GeminiRepository()
) : ViewModel() {

    constructor() : this(GeminiRepository())

    // Navigation & Screen Stack
    private val _currentScreen = MutableStateFlow(InjahniaScreen.DASHBOARD)
    val currentScreen: StateFlow<InjahniaScreen> = _currentScreen.asStateFlow()

    private val screenBackStack = mutableListOf<InjahniaScreen>()

    fun navigateTo(screen: InjahniaScreen) {
        if (_currentScreen.value != screen) {
            screenBackStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun handleBackPress(): Boolean {
        if (screenBackStack.isNotEmpty()) {
            _currentScreen.value = screenBackStack.removeAt(screenBackStack.size - 1)
            return true
        }
        return false
    }

    // BYOK Key Management
    private val _keyInput = MutableStateFlow("")
    val keyInput: StateFlow<String> = _keyInput.asStateFlow()

    private val _connectionStatus = MutableStateFlow<ApiConnectionStatus>(ApiConnectionStatus.Untested)
    val connectionStatus: StateFlow<ApiConnectionStatus> = _connectionStatus.asStateFlow()

    private val _isTestingConnection = MutableStateFlow(false)
    val isTestingConnection: StateFlow<Boolean> = _isTestingConnection.asStateFlow()

    fun onKeyInputChanged(newKey: String) {
        _keyInput.value = newKey
        repository.inMemoryKey = newKey
        _connectionStatus.value = ApiConnectionStatus.Untested
    }

    fun clearKey() {
        _keyInput.value = ""
        repository.clearKey()
        _connectionStatus.value = ApiConnectionStatus.Untested
    }

    fun testConnection() {
        val key = _keyInput.value.trim()
        if (key.isBlank()) {
            _connectionStatus.value = ApiConnectionStatus.Failed("Please enter a Gemini API key first.")
            return
        }

        viewModelScope.launch {
            _isTestingConnection.value = true
            val result = repository.testConnection(key)
            _isTestingConnection.value = false
            result.onSuccess { successResult ->
                _connectionStatus.value = ApiConnectionStatus.Connected(
                    latencyMs = successResult.latencyMs,
                    message = successResult.message
                )
            }.onFailure { error ->
                val isQuota = error.message?.contains("429") == true || error.message?.contains("Quota") == true
                _connectionStatus.value = ApiConnectionStatus.Failed(
                    error = error.localizedMessage ?: "Connection failed",
                    isQuotaError = isQuota
                )
            }
        }
    }

    // Chat Management
    private val _engineCode = MutableStateFlow("Toyota 2JZ-GTE")
    val engineCode: StateFlow<String> = _engineCode.asStateFlow()

    private val _chatInput = MutableStateFlow("")
    val chatInput: StateFlow<String> = _chatInput.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _chatError = MutableStateFlow<String?>(null)
    val chatError: StateFlow<String?> = _chatError.asStateFlow()

    fun onEngineCodeChanged(code: String) {
        _engineCode.value = code
    }

    fun onChatInputChanged(input: String) {
        _chatInput.value = input
    }

    fun loadFullEngineSpecSheet(engine: String = _engineCode.value) {
        val target = engine.trim().ifBlank { _engineCode.value }
        val userMsg = ChatMessage(
            isUser = true,
            text = "Full Workshop Spec Sheet & Tolerances: $target",
            engineCode = target
        )
        val specSheetText = EngineSpecSheetBuilder.generateSpecSheetForEngine(target)
        val assistantMsg = ChatMessage(
            isUser = false,
            text = specSheetText,
            engineCode = target,
            verificationBadge = VerificationBadge.VERIFIED_OEM
        )
        _messages.value = _messages.value + userMsg + assistantMsg
    }

    fun sendChatMessage(customPrompt: String? = null) {
        val query = customPrompt ?: _chatInput.value.trim()
        if (query.isBlank()) return

        _chatError.value = null
        val currentEngine = _engineCode.value.trim()

        val userMessage = ChatMessage(
            isUser = true,
            text = query,
            engineCode = currentEngine.takeIf { it.isNotBlank() }
        )
        val currentList = _messages.value.toMutableList()
        currentList.add(userMessage)
        _messages.value = currentList
        if (customPrompt == null) _chatInput.value = ""

        val hasApiKey = repository.getEffectiveKey().isNotBlank()

        if (!hasApiKey) {
            // Provide verified engineering response from engine reference database
            viewModelScope.launch {
                _isGenerating.value = true
                kotlinx.coroutines.delay(400) // Brief realistic calculation delay
                _isGenerating.value = false

                val (responseText, badge) = if (MeasurementEvaluator.canEvaluate(query)) {
                    val eval = MeasurementEvaluator.evaluateMeasurement(query, currentEngine)
                    eval.formattedReport to eval.statusBadge
                } else {
                    val specText = EngineSpecSheetBuilder.generateSpecSheetForEngine("$currentEngine $query")
                    val b = if (specText.contains("[VERIFIED OEM SPEC]")) {
                        VerificationBadge.VERIFIED_OEM
                    } else {
                        VerificationBadge.GENERAL_GUIDELINE
                    }
                    specText to b
                }

                val assistantMessage = ChatMessage(
                    isUser = false,
                    text = responseText,
                    engineCode = currentEngine.takeIf { it.isNotBlank() },
                    verificationBadge = badge
                )
                _messages.value = _messages.value + assistantMessage
            }
            return
        }

        viewModelScope.launch {
            _isGenerating.value = true
            val history = _messages.value
                .takeLast(6)
                .chunked(2)
                .mapNotNull { pair ->
                    if (pair.size == 2 && pair[0].isUser && !pair[1].isUser) {
                        pair[0].text to pair[1].text
                    } else null
                }

            val result = repository.queryMachiningAssistant(
                prompt = query,
                engineContext = currentEngine.takeIf { it.isNotBlank() },
                conversationHistory = history
            )
            _isGenerating.value = false

            result.onSuccess { responseText ->
                val badge = when {
                    responseText.contains("[VERIFIED OEM SPEC]") -> VerificationBadge.VERIFIED_OEM
                    responseText.contains("[PARTIALLY VERIFIED]") -> VerificationBadge.PARTIALLY_VERIFIED
                    responseText.contains("[UNVERIFIED]") -> VerificationBadge.UNVERIFIED
                    else -> VerificationBadge.GENERAL_GUIDELINE
                }

                val assistantMessage = ChatMessage(
                    isUser = false,
                    text = responseText,
                    engineCode = currentEngine.takeIf { it.isNotBlank() },
                    verificationBadge = badge
                )
                _messages.value = _messages.value + assistantMessage
            }.onFailure { err ->
                if (MeasurementEvaluator.canEvaluate(query)) {
                    val eval = MeasurementEvaluator.evaluateMeasurement(query, currentEngine)
                    val assistantMessage = ChatMessage(
                        isUser = false,
                        text = eval.formattedReport,
                        engineCode = currentEngine.takeIf { it.isNotBlank() },
                        verificationBadge = eval.statusBadge
                    )
                    _messages.value = _messages.value + assistantMessage
                } else {
                    _chatError.value = err.localizedMessage ?: "Failed to generate answer."
                }
            }
        }
    }

    fun togglePinMessage(messageId: String) {
        _messages.value = _messages.value.map { msg ->
            if (msg.id == messageId) {
                val updated = msg.copy(isPinned = !msg.isPinned)
                if (updated.isPinned) {
                    savePinnedBuild(updated)
                }
                updated
            } else msg
        }
    }

    private fun savePinnedBuild(message: ChatMessage) {
        val engine = message.engineCode ?: "General Reconditioning"
        val firstLine = message.text.lines().firstOrNull { it.isNotBlank() } ?: "Machining Specification"
        val item = SavedBuildItem(
            engineCode = engine,
            title = firstLine.take(50),
            note = message.text,
            specs = mapOf("Timestamp" to "Saved Build", "Source" to (message.verificationBadge?.label ?: "General"))
        )
        _savedBuilds.value = listOf(item) + _savedBuilds.value
    }

    fun clearChat() {
        _messages.value = emptyList()
        _chatError.value = null
    }

    // Saved Builds & Recent Queries
    private val _savedBuilds = MutableStateFlow<List<SavedBuildItem>>(emptyList())
    val savedBuilds: StateFlow<List<SavedBuildItem>> = _savedBuilds.asStateFlow()

    fun removeSavedBuild(id: String) {
        _savedBuilds.value = _savedBuilds.value.filter { it.id != id }
    }

    // Technical Reference Library Search & Filtering
    private val _librarySearchQuery = MutableStateFlow("")
    val librarySearchQuery: StateFlow<String> = _librarySearchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    fun onLibrarySearchQueryChanged(q: String) {
        _librarySearchQuery.value = q
    }

    fun onCategorySelected(cat: String) {
        _selectedCategory.value = cat
    }

    // Clearance Calculator
    private val _calcPistonBore = MutableStateFlow("86.000")
    val calcPistonBore: StateFlow<String> = _calcPistonBore.asStateFlow()

    private val _calcPistonDiameter = MutableStateFlow("85.920")
    val calcPistonDiameter: StateFlow<String> = _calcPistonDiameter.asStateFlow()

    private val _calcResult = MutableStateFlow<ClearanceCalcResult?>(null)
    val calcResult: StateFlow<ClearanceCalcResult?> = _calcResult.asStateFlow()

    fun updateCalcInputs(bore: String, piston: String) {
        _calcPistonBore.value = bore
        _calcPistonDiameter.value = piston
        computePistonClearance()
    }

    fun computePistonClearance() {
        val bore = _calcPistonBore.value.toDoubleOrNull() ?: return
        val piston = _calcPistonDiameter.value.toDoubleOrNull() ?: return

        val clearanceMm = bore - piston
        val clearanceInch = clearanceMm / 25.4

        // Standard forged 4032 piston clearance: 0.060mm - 0.090mm (0.0024" - 0.0035")
        val minAllowedMm = 0.060
        val maxAllowedMm = 0.090
        val isOk = clearanceMm in minAllowedMm..maxAllowedMm

        val rec = when {
            clearanceMm < minAllowedMm -> "CRITICAL TIGHT: Clearance of ${"%.3f".format(clearanceMm)}mm will cause piston scuffing or seizure under thermal expansion. Additional hone pass required."
            clearanceMm > maxAllowedMm -> "LOOSE: Clearance of ${"%.3f".format(clearanceMm)}mm exceeds tolerance. May cause cold piston slap and high oil consumption."
            else -> "OPTIMAL TOLERANCE: Clearance of ${"%.3f".format(clearanceMm)}mm is in target specification."
        }

        _calcResult.value = ClearanceCalcResult(
            typeName = "Piston-to-Cylinder Wall Clearance",
            clearanceMm = clearanceMm,
            clearanceInch = clearanceInch,
            minAllowedMm = minAllowedMm,
            maxAllowedMm = maxAllowedMm,
            isWithinSpec = isOk,
            recommendations = rec
        )
    }

    // Workshop Settings
    private val _unitSystem = MutableStateFlow(UnitSystem.METRIC_MM)
    val unitSystem: StateFlow<UnitSystem> = _unitSystem.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleUnitSystem() {
        _unitSystem.value = if (_unitSystem.value == UnitSystem.METRIC_MM) {
            UnitSystem.IMPERIAL_INCH
        } else {
            UnitSystem.METRIC_MM
        }
    }

    fun toggleDarkTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun setDarkTheme(enabled: Boolean) {
        _isDarkTheme.value = enabled
    }

    init {
        val existingKey = repository.getEffectiveKey()
        if (existingKey.isNotBlank()) {
            _keyInput.value = existingKey
            _connectionStatus.value = ApiConnectionStatus.Connected(
                latencyMs = 0,
                message = "Pre-configured environment key detected. You can test or replace it anytime."
            )
        }
        initSampleData()
    }

    private fun initSampleData() {
        val initialSpecSheet = EngineSpecSheetBuilder.generateSpecSheetForEngine(_engineCode.value)
        _messages.value = listOf(
            ChatMessage(
                isUser = false,
                text = initialSpecSheet,
                verificationBadge = VerificationBadge.VERIFIED_OEM,
                engineCode = _engineCode.value
            )
        )

        _savedBuilds.value = listOf(
            SavedBuildItem(
                engineCode = "Toyota 2JZ-GTE",
                title = "CP-Carrillo 86.5mm Forged Clearance",
                note = "Bore finished to 86.500mm with torque plate. Skirt measurement at 86.415mm. Measured clearance: 0.085mm (0.0033\"). Plateau honed with 400-grit Sunnen brushes.",
                specs = mapOf(
                    "Bore" to "86.500 mm",
                    "Piston Skirt" to "86.415 mm",
                    "Clearance" to "0.085 mm (0.0033\")",
                    "Crosshatch" to "40°",
                    "Status" to "Verified Optimal"
                )
            ),
            SavedBuildItem(
                engineCode = "GM LS3 6.2L",
                title = "King Racing Main Bearing Oil Clearance",
                note = "Main journals ground to -0.25mm (-0.010\"). King MB5013XP bearings installed. Housing bore torqued to OEM 30 Nm + 90°. Measured clearance: 0.052mm (0.0020\").",
                specs = mapOf(
                    "Crank Journal" to "64.740 mm",
                    "Bearing Clearance" to "0.052 mm (0.0020\")",
                    "Fillet Radius" to "3.4 mm",
                    "Deck Finish" to "Ra 22 µin"
                )
            )
        )

        computePistonClearance()
    }
}
