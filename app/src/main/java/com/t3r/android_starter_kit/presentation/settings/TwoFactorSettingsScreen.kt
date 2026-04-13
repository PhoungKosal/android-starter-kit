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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.t3r.android_starter_kit.R
import com.t3r.android_starter_kit.presentation.components.LoadingButton
import com.t3r.android_starter_kit.presentation.components.LoadingOutlinedButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoFactorScreen(
    viewModel: TwoFactorViewModel,
    twoFactorEnabled: Boolean,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
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
            title = { Text(stringResource(R.string.two_factor_enable_title)) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                ) {
                    if (state.qrCodeUrl != null) {
                        Text(
                            text = stringResource(R.string.two_factor_scan_qr),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AsyncImage(
                            model = state.qrCodeUrl,
                            contentDescription = stringResource(R.string.two_factor_qr_content_desc),
                            modifier = Modifier
                                .size(200.dp)
                                .align(Alignment.CenterHorizontally),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    if (state.secret != null) {
                        Text(
                            text = stringResource(R.string.two_factor_manual_entry),
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
                        label = { Text(stringResource(R.string.two_factor_code_label)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                LoadingButton(
                    text = stringResource(R.string.two_factor_enable),
                    onClick = { viewModel.onEvent(TwoFactorEvent.ConfirmEnable) },
                    isLoading = state.isLoading,
                    enabled = state.verifyCode.isNotBlank(),
                )
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(TwoFactorEvent.DismissSetupDialog) }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    // Disable 2FA dialog
    if (state.showDisableDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(TwoFactorEvent.DismissDisableDialog) },
            title = { Text(stringResource(R.string.two_factor_disable_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.two_factor_disable_desc))
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = state.disablePassword,
                        onValueChange = { viewModel.onEvent(TwoFactorEvent.UpdateDisablePassword(it)) },
                        label = { Text(stringResource(R.string.password)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.disableCode,
                        onValueChange = { viewModel.onEvent(TwoFactorEvent.UpdateDisableCode(it)) },
                        label = { Text(stringResource(R.string.two_factor_authenticator_code)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                LoadingButton(
                    text = stringResource(R.string.two_factor_disable),
                    onClick = { viewModel.onEvent(TwoFactorEvent.ConfirmDisable) },
                    isLoading = state.isLoading,
                    enabled = state.disablePassword.isNotBlank() && state.disableCode.isNotBlank(),
                )
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(TwoFactorEvent.DismissDisableDialog) }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.two_factor_settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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
                        text = if (state.twoFactorEnabled) stringResource(R.string.two_factor_enabled_status) else stringResource(R.string.two_factor_disabled_status),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (state.twoFactorEnabled) {
                            stringResource(R.string.two_factor_enabled_desc)
                        } else {
                            stringResource(R.string.two_factor_disabled_desc)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (state.twoFactorEnabled) {
                        LoadingOutlinedButton(
                            text = stringResource(R.string.two_factor_disable_2fa),
                            onClick = { viewModel.onEvent(TwoFactorEvent.ShowDisableDialog) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        LoadingButton(
                            text = stringResource(R.string.two_factor_enable_2fa),
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
                            text = stringResource(R.string.two_factor_recovery_codes),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.two_factor_recovery_desc),
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
