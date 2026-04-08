package com.t3r.android_starter_kit.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.t3r.android_starter_kit.presentation.components.ErrorView
import com.t3r.android_starter_kit.presentation.components.LoadingView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToTwoFactor: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.isLoading -> LoadingView(modifier = Modifier.padding(padding))
            state.error != null -> ErrorView(
                message = state.error!!.message,
                onRetry = { viewModel.onEvent(SettingsEvent.Load) },
                modifier = Modifier.padding(padding),
            )
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Text(
                        text = "Security",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )

                    ListItem(
                        headlineContent = { Text("Two-Factor Authentication") },
                        supportingContent = {
                            Text(
                                if (state.user?.twoFactorEnabled == true) "Enabled" else "Disabled"
                            )
                        },
                        leadingContent = { Icon(Icons.Outlined.Security, contentDescription = null) },
                        modifier = Modifier.clickable { onNavigateToTwoFactor() },
                    )

                    HorizontalDivider()

                    Text(
                        text = "Preferences",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )

                    ListItem(
                        headlineContent = { Text("Theme") },
                        supportingContent = {
                            Text(state.user?.theme?.replaceFirstChar { it.uppercase() } ?: "System")
                        },
                        leadingContent = { Icon(Icons.Outlined.Palette, contentDescription = null) },
                    )

                    ListItem(
                        headlineContent = { Text("Language") },
                        supportingContent = {
                            Text(state.user?.language?.uppercase() ?: "EN")
                        },
                        leadingContent = { Icon(Icons.Outlined.Language, contentDescription = null) },
                    )
                }
            }
        }
    }
}
