package com.t3r.android_starter_kit.presentation.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.t3r.android_starter_kit.R
import com.t3r.android_starter_kit.presentation.components.ErrorView
import com.t3r.android_starter_kit.presentation.components.LoadingButton
import com.t3r.android_starter_kit.presentation.components.LoadingView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    onLoggedOut: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let { viewModel.onEvent(ProfileEvent.UploadAvatar(it)) }
    }

    LaunchedEffect(state.loggedOut) {
        if (state.loggedOut) onLoggedOut()
    }

    LaunchedEffect(Unit) {
        viewModel.navigation.collect { event ->
            when (event) {
                ProfileNavigationEvent.AccountDeleted -> onLoggedOut()
            }
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it.message)
            viewModel.onEvent(ProfileEvent.ClearError)
        }
    }

    // Delete account dialog
    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(ProfileEvent.DismissDeleteDialog) },
            title = { Text(stringResource(R.string.profile_delete_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.profile_delete_description))
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = state.deletePassword,
                        onValueChange = { viewModel.onEvent(ProfileEvent.UpdateDeletePassword(it)) },
                        label = { Text(stringResource(R.string.password)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                LoadingButton(
                    text = stringResource(R.string.delete),
                    onClick = { viewModel.onEvent(ProfileEvent.ConfirmDeleteAccount) },
                    isLoading = state.isDeleting,
                    enabled = state.deletePassword.isNotBlank(),
                )
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(ProfileEvent.DismissDeleteDialog) }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if (!state.isEditing && state.user != null) {
                        IconButton(onClick = { viewModel.onEvent(ProfileEvent.StartEditing) }) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.profile_edit))
                        }
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.isLoading && state.user == null -> {
                LoadingView(modifier = Modifier.padding(padding))
            }
            state.error != null && state.user == null -> {
                ErrorView(
                    message = state.error!!.message,
                    onRetry = { viewModel.onEvent(ProfileEvent.LoadProfile) },
                    modifier = Modifier.padding(padding),
                )
            }
            state.isEditing -> {
                EditProfileContent(
                    state = state,
                    onEvent = viewModel::onEvent,
                    modifier = Modifier.padding(padding),
                )
            }
            else -> {
                state.user?.let { user ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        // Avatar & name section
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape)
                                    .clickable(enabled = !state.isUploadingAvatar) {
                                        imagePickerLauncher.launch("image/*")
                                    },
                            ) {
                                if (user.avatar != null) {
                                    AsyncImage(
                                        model = user.avatar,
                                        contentDescription = stringResource(R.string.profile_avatar),
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                    )
                                } else {
                                    Card(
                                        modifier = Modifier.fillMaxSize(),
                                        shape = CircleShape,
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                        ) {
                                            Text(
                                                text = user.initials,
                                                style = MaterialTheme.typography.headlineMedium,
                                            )
                                        }
                                    }
                                }
                                if (state.isUploadingAvatar) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(48.dp),
                                        strokeWidth = 3.dp,
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            TextButton(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                enabled = !state.isUploadingAvatar,
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.size(4.dp))
                                Text(stringResource(R.string.profile_change_photo), style = MaterialTheme.typography.labelSmall)
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = user.displayName, style = MaterialTheme.typography.headlineSmall)
                            Text(
                                text = "@${user.username}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            user.role?.let {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = it.name.replaceFirstChar { c -> c.uppercase() },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }

                        HorizontalDivider()

                        ListItem(
                            headlineContent = { Text(user.email) },
                            supportingContent = { Text(stringResource(R.string.profile_email_label)) },
                            leadingContent = { Icon(Icons.Outlined.Email, contentDescription = null) },
                        )
                        user.phoneNumber?.let { phone ->
                            ListItem(
                                headlineContent = { Text(phone) },
                                supportingContent = { Text(stringResource(R.string.profile_phone_label)) },
                                leadingContent = { Icon(Icons.Outlined.Person, contentDescription = null) },
                            )
                        }
                        ListItem(
                            headlineContent = {
                                Text(if (user.twoFactorEnabled) stringResource(R.string.enabled) else stringResource(R.string.disabled))
                            },
                            supportingContent = { Text(stringResource(R.string.profile_2fa_label)) },
                            leadingContent = { Icon(Icons.Outlined.Security, contentDescription = null) },
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        LoadingButton(
                            text = stringResource(R.string.profile_sign_out),
                            onClick = { viewModel.onEvent(ProfileEvent.Logout) },
                            isLoading = state.isLoggingOut,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        TextButton(
                            onClick = { viewModel.onEvent(ProfileEvent.ShowDeleteDialog) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error,
                            ),
                        ) {
                            Text(stringResource(R.string.profile_delete_account))
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun EditProfileContent(
    state: ProfileState,
    onEvent: (ProfileEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.editFirstName,
            onValueChange = { onEvent(ProfileEvent.UpdateFirstName(it)) },
            label = { Text(stringResource(R.string.profile_first_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.editLastName,
            onValueChange = { onEvent(ProfileEvent.UpdateLastName(it)) },
            label = { Text(stringResource(R.string.profile_last_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.editPhoneNumber,
            onValueChange = { onEvent(ProfileEvent.UpdatePhoneNumber(it)) },
            label = { Text(stringResource(R.string.profile_phone_number)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(24.dp))

        LoadingButton(
            text = stringResource(R.string.profile_save_changes),
            onClick = { onEvent(ProfileEvent.SaveProfile) },
            isLoading = state.isSaving,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = { onEvent(ProfileEvent.CancelEditing) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.cancel))
        }
    }
}
