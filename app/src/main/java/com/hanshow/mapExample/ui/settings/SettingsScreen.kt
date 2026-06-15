package com.hanshow.mapExample.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var baseUrl by remember { mutableStateOf("") }
    var customerCode by remember { mutableStateOf("") }
    var storeCode by remember { mutableStateOf("") }
    var floorId by remember { mutableStateOf("") }

    // Load settings on first render
    LaunchedEffect(Unit) {
        viewModel.handleIntent(SettingsIntent.LoadSettings)
    }

    // Update local state when settings are loaded
    LaunchedEffect(uiState) {
        if (uiState is SettingsUiState.Loaded) {
            val loaded = uiState as SettingsUiState.Loaded
            baseUrl = loaded.baseUrl
            customerCode = loaded.customerCode
            storeCode = loaded.storeCode
            floorId = loaded.floorId.toString()
        }
        if (uiState is SettingsUiState.Saved) {
            snackbarHostState.showSnackbar("Settings saved. URL changes require app restart to take effect.")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    // Back button is handled by onNavigateBack in the Button below
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "API Configuration",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "AUTH_BASE_URL and MAP_BASE_URL share the same Base URL",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                label = { Text("Base URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = customerCode,
                onValueChange = { customerCode = it },
                label = { Text("Customer Code") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = storeCode,
                onValueChange = { storeCode = it },
                label = { Text("Store Code") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = floorId,
                onValueChange = { floorId = it },
                label = { Text("Floor ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Back")
                }

                Button(
                    onClick = {
                        val floorIdInt = floorId.toIntOrNull() ?: 0
                        viewModel.handleIntent(
                            SettingsIntent.SaveSettings(baseUrl, customerCode, storeCode, floorIdInt)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    enabled = baseUrl.isNotBlank()
                ) {
                    Text("Save")
                }
            }
        }
    }
}
