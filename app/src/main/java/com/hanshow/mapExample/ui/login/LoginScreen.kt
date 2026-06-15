package com.hanshow.mapExample.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hanshow.mapExample.R
import com.hanshow.mapExample.data.api.ApiConfig

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var username by remember { mutableStateOf(ApiConfig.userName) }
    var password by remember { mutableStateOf(ApiConfig.pwd) }

    // Navigate after successful login
    LaunchedEffect(uiState) {
        when (uiState) {
            is LoginUiState.Success -> onLoginSuccess()
            is LoginUiState.Error -> snackbarHostState.showSnackbar(
                (uiState as LoginUiState.Error).message
            )
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Map Demo") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_settings_24),
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
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
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Map Demo",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = uiState !is LoginUiState.Loading
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = uiState !is LoginUiState.Loading
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState is LoginUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Settings")
                    }

                    Button(
                        onClick = {
                            if (username.isNotBlank() && password.isNotBlank()) {
                                viewModel.handleIntent(LoginIntent.Login(username, password))
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = username.isNotBlank() && password.isNotBlank()
                    ) {
                        Text("Login")
                    }
                }
            }
        }
    }
}
