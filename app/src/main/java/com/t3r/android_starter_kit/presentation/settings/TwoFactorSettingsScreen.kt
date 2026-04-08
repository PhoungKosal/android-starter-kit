package com.t3r.android_starter_kit.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.t3r.android_starter_kit.presentation.components.LoadingButton
import com.t3r.android_starter_kit.presentation.components.LoadingOutlinedButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoFactorScreen(
    viewModel: TwoFactorViewModel,
    twoFactorEnabled: Boolean,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(twoFactorEnabled) {
        viewModel.setInitialEnabled(twoFactorEnabled)
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(TwoFactorEvent.ClearMessage)
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it.message)
            viewModel.onEvent(TwoFactorEvent.ClearMessage)
        }
    }

    // Enable 2FA dialog
    if (state.showSetupDialog && !state.twoFactorEnabled) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(TwoFactorEvent.DismissSetupDialog) },
            title = { Text("Enable Two-Factor Auth") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                ) {
                    if (state.qrCodeUrl != null) {
                        Text(
                            text = "Scan this QR code with your authenticator app:",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AsyncImage(
                            model = state.qrCodeUrl,
                            contentDescription = "QR Code",
                            modifier = Modifier
                                .size(200.dp)
                                .align(Alignment.CenterHorizontally),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    if (state.secret != null) {
                        Text(
                            text = "Or enter this key manually:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = state.secret!!,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    OutlinedTextField(
                        value = state.verifyCode,
                        onValueChange = { viewModel.onEvent(TwoFactorEvent.UpdateVerifyCode(it)) },
                        label = { Text("Verification Code") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                LoadingButton(
                    text = "Enable",
                    onClick = { viewModel.onEvent(TwoFactorEvent.ConfirmEnable) },
                    isLoading = state.isLoading,
                    enabled = state.verifyCode.isNotBlank(),
                )
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(TwoFactorEvent.DismissSetupDialog) }) {
                    Text("Cancel")
                }
            },
        )
    }

    // Disable 2FA dialog
    if (state.showDisableDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(TwoFactorEvent.DismissDisableDialog) },
            title = { Text("Disable Two-Factor Auth") },
            text = {
                Column {
                    Text("Enter your password and a 6-digit code to disable 2FA.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = state.disablePassword,
                        onValueChange = { viewModel.onEvent(TwoFactorEvent.UpdateDisablePassword(it)) },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.disableCode,
                        onValueChange = { viewModel.onEvent(TwoFactorEvent.UpdateDisableCode(it)) },
                        label = { Text("Authenticator Code") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                LoadingButton(
                    text = "Disable",
                    onClick = { viewModel.onEvent(TwoFactorEvent.ConfirmDisable) },
                    isLoading = state.isLoading,
                    enabled = state.disablePassword.isNotBlank() && state.disableCode.isNotBlank(),
                )
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(TwoFactorEvent.DismissDisableDialog) }) {
                    Text("Cancel")
                }
            },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Two-Factor Authentication") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (state.twoFactorEnabled) "2FA is Enabled" else "2FA is Disabled",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (state.twoFactorEnabled) {
                            "Your account is protected with two-factor authentication."
                        } else {
                            "Add an extra layer of security to your account."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (state.twoFactorEnabled) {
                        LoadingOutlinedButton(
                            text = "Disable 2FA",
                            onClick = { viewModel.onEvent(TwoFactorEvent.ShowDisableDialog) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        LoadingButton(
                            text = "Enable 2FA",
                            onClick = { viewModel.onEvent(TwoFactorEvent.RequestEnable) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            // Show recovery codes if just enabled
            if (state.recoveryCodes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Recovery Codes",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Save these codes in a safe place. Each code can only be used once.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        state.recoveryCodes.forEach { code ->
                            Text(
                                text = code,
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            )
                        }
                    }
                }
            }
        }
    }
}
