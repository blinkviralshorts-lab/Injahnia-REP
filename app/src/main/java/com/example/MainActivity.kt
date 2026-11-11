package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Mic
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.WorkshopBottomBar
import com.example.ui.components.WorkshopTopBar
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ReferenceLibraryScreen
import com.example.ui.screens.SavedQueriesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.CarbonBlack
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanContainer
import com.example.ui.theme.GraphiteBorder
import com.example.ui.theme.GraphiteDarkest
import com.example.ui.theme.GraphiteSurface
import com.example.ui.theme.GraphiteSurfaceVariant
import com.example.ui.theme.InjahniaTheme
import com.example.ui.theme.WorkshopOffWhite
import com.example.ui.theme.WorkshopSlate
import com.example.ui.theme.WorkshopWhite
import com.example.viewmodel.InjahniaScreen
import com.example.viewmodel.InjahniaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: InjahniaViewModel = viewModel()
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            InjahniaTheme(darkTheme = isDarkTheme) {
                InjahniaApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun InjahniaApp(viewModel: InjahniaViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val engineCode by viewModel.engineCode.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    var showEngineDialog by remember { mutableStateOf(false) }

    // System Back Press handling
    BackHandler(enabled = currentScreen != InjahniaScreen.DASHBOARD && currentScreen != InjahniaScreen.WELCOME) {
        val handled = viewModel.handleBackPress()
        if (!handled) {
            viewModel.navigateTo(InjahniaScreen.DASHBOARD)
        }
    }

    val showTopBar = currentScreen != InjahniaScreen.WELCOME
    val showBottomBar = currentScreen != InjahniaScreen.WELCOME

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (showTopBar) {
                WorkshopTopBar(
                    engineCode = engineCode,
                    connectionStatus = connectionStatus,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { viewModel.toggleDarkTheme() },
                    onOpenSettings = { viewModel.navigateTo(InjahniaScreen.SETTINGS) },
                    onEngineClick = { showEngineDialog = true }
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                WorkshopBottomBar(
                    currentScreen = currentScreen,
                    onNavigate = { screen -> viewModel.navigateTo(screen) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                InjahniaScreen.WELCOME -> {
                    WelcomeScreen(
                        onEnterWorkshop = { viewModel.navigateTo(InjahniaScreen.DASHBOARD) },
                        onConfigureKey = { viewModel.navigateTo(InjahniaScreen.SETTINGS) }
                    )
                }

                InjahniaScreen.DASHBOARD -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigate = { screen -> viewModel.navigateTo(screen) }
                    )
                }

                InjahniaScreen.AI_CHAT -> {
                    ChatScreen(
                        viewModel = viewModel,
                        onNavigate = { screen -> viewModel.navigateTo(screen) }
                    )
                }

                InjahniaScreen.REFERENCE_LIBRARY -> {
                    ReferenceLibraryScreen(viewModel = viewModel)
                }

                InjahniaScreen.SAVED_QUERIES -> {
                    SavedQueriesScreen(
                        viewModel = viewModel,
                        onNavigate = { screen -> viewModel.navigateTo(screen) }
                    )
                }

                InjahniaScreen.ACCOUNT_SUBSCRIPTION -> {
                    AccountScreen(
                        onNavigate = { screen -> viewModel.navigateTo(screen) }
                    )
                }

                InjahniaScreen.SETTINGS -> {
                    SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Engine Selection Dialog
    if (showEngineDialog) {
        EngineSelectorDialog(
            currentEngine = engineCode,
            onDismiss = { showEngineDialog = false },
            onSelect = { selected ->
                viewModel.onEngineCodeChanged(selected)
                showEngineDialog = false
            }
        )
    }
}

@Composable
fun EngineSelectorDialog(
    currentEngine: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val context = LocalContext.current
    var customInput by remember { mutableStateOf(currentEngine) }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenMatches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = spokenMatches?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                customInput = spokenText
                Toast.makeText(context, "Voice input: $spokenText", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val onMicClick = {
        val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak engine code or car model (e.g. 2JZ-GTE, LS3, BMW B58)...")
        }
        try {
            speechLauncher.launch(speechIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Voice input is not available on this device", Toast.LENGTH_SHORT).show()
        }
    }

    val presetEngines = listOf(
        "Toyota 2JZ-GTE (3.0L I6 Twin-Turbo)",
        "GM LS3 / L99 (6.2L V8)",
        "Nissan RB26DETT (2.6L I6)",
        "Cummins 5.9L 6BT 12V/24V",
        "VW EA888 Gen 3 (2.0 TSI)",
        "Ford 5.0L Coyote (DOHC V8)",
        "BMW S58 / B58 (3.0L Turbo I6)",
        "Subaru EJ257 (2.5L Turbo Boxer)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select Focus Engine",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Type or speak any engine code or car model to ground AI clearances and machining specifications:",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = customInput,
                    onValueChange = { customInput = it },
                    label = { Text("Engine Code or Car Model") },
                    trailingIcon = {
                        IconButton(
                            onClick = onMicClick,
                            modifier = Modifier
                                .testTag("dialog_engine_mic_button")
                                .size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Speak engine code",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_engine_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Popular Workshop Architectures:",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(presetEngines) { eng ->
                        val isSelected = eng.startsWith(customInput.take(6))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { customInput = eng.substringBefore(" (") }
                        ) {
                            Text(
                                text = eng,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (customInput.isNotBlank()) {
                        onSelect(customInput.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .testTag("confirm_engine_button")
                    .height(52.dp)
            ) {
                Text(text = "Set Engine", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(52.dp)
            ) {
                Text(text = "Cancel", fontSize = 14.sp)
            }
        }
    )
}
