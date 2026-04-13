package com.t3r.android_starter_kit.presentation.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t3r.android_starter_kit.R
import com.t3r.android_starter_kit.core.locale.LocaleManager
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

    // Language picker bottom sheet
    if (state.showLanguagePicker) {
        LanguagePickerSheet(
            currentLocale = state.currentLocale,
            onLocaleSelected = { viewModel.onEvent(SettingsEvent.ChangeLocale(it)) },
            onDismiss = { viewModel.onEvent(SettingsEvent.DismissLanguagePicker) },
        )
    }

    // Theme picker bottom sheet
    if (state.showThemePicker) {
        ThemePickerSheet(
            currentTheme = state.currentTheme,
            onThemeSelected = { viewModel.onEvent(SettingsEvent.ChangeTheme(it)) },
            onDismiss = { viewModel.onEvent(SettingsEvent.DismissThemePicker) },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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
                        text = stringResource(R.string.settings_security),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )

                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_two_factor)) },
                        supportingContent = {
                            Text(
                                if (state.user?.twoFactorEnabled == true) {
                                    stringResource(R.string.enabled)
                                } else {
                                    stringResource(R.string.disabled)
                                }
                            )
                        },
                        leadingContent = { Icon(Icons.Outlined.Security, contentDescription = null) },
                        modifier = Modifier.clickable { onNavigateToTwoFactor() },
                    )

                    HorizontalDivider()

                    Text(
                        text = stringResource(R.string.settings_preferences),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )

                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_theme)) },
                        supportingContent = {
                            Text("${state.currentTheme.icon} ${stringResource(state.currentTheme.displayNameRes)}")
                        },
                        leadingContent = { Icon(Icons.Outlined.Palette, contentDescription = null) },
                        modifier = Modifier.clickable {
                            viewModel.onEvent(SettingsEvent.ShowThemePicker)
                        },
                    )

                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_language)) },
                        supportingContent = {
                            Text("${state.currentLocale.flag} ${stringResource(state.currentLocale.displayNameRes)}")
                        },
                        leadingContent = { Icon(Icons.Outlined.Language, contentDescription = null) },
                        modifier = Modifier.clickable {
                            viewModel.onEvent(SettingsEvent.ShowLanguagePicker)
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguagePickerSheet(
    currentLocale: LocaleManager.AppLocale,
    onLocaleSelected: (LocaleManager.AppLocale) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.language_label),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp),
            )

            LocaleManager.AppLocale.entries.forEach { locale ->
                val isSelected = currentLocale == locale
                val borderColor = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(12.dp),
                        )
                        .clickable { onLocaleSelected(locale) }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(text = locale.flag, fontSize = 24.sp)

                    Text(
                        text = stringResource(locale.displayNameRes),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemePickerSheet(
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.theme_label),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp),
            )

            AppTheme.entries.forEach { theme ->
                val isSelected = currentTheme == theme
                val borderColor = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(12.dp),
                        )
                        .clickable { onThemeSelected(theme) }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(text = theme.icon, fontSize = 24.sp)

                    Text(
                        text = stringResource(theme.displayNameRes),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
