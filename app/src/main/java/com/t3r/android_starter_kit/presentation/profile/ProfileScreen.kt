package com.t3r.android_starter_kit.presentation.profile

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
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
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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
        }
    }

    // Delete account dialog
    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(ProfileEvent.DismissDeleteDialog) },
            title = { Text("Delete Account") },
            text = {
                Column {
                    Text("This action is permanent. Enter your password to confirm.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = state.deletePassword,
                        onValueChange = { viewModel.onEvent(ProfileEvent.UpdateDeletePassword(it)) },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                LoadingButton(
                    text = "Delete",
                    onClick = { viewModel.onEvent(ProfileEvent.ConfirmDeleteAccount) },
                    isLoading = state.isDeleting,
                    enabled = state.deletePassword.isNotBlank(),
                )
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(ProfileEvent.DismissDeleteDialog) }) {
                    Text("Cancel")
                }
            },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!state.isEditing && state.user != null) {
                        IconButton(onClick = { viewModel.onEvent(ProfileEvent.StartEditing) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
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
                            if (user.avatar != null) {
                                AsyncImage(
                                    model = user.avatar,
                                    contentDescription = "Avatar",
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                )
                            } else {
                                Card(
                                    modifier = Modifier.size(96.dp),
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
                            supportingContent = { Text("Email") },
                            leadingContent = { Icon(Icons.Outlined.Email, contentDescription = null) },
                        )
                        user.phoneNumber?.let { phone ->
                            ListItem(
                                headlineContent = { Text(phone) },
                                supportingContent = { Text("Phone") },
                                leadingContent = { Icon(Icons.Outlined.Person, contentDescription = null) },
                            )
                        }
                        ListItem(
                            headlineContent = {
                                Text(if (user.twoFactorEnabled) "Enabled" else "Disabled")
                            },
                            supportingContent = { Text("Two-Factor Authentication") },
                            leadingContent = { Icon(Icons.Outlined.Security, contentDescription = null) },
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        LoadingButton(
                            text = "Sign Out",
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
                            Text("Delete Account")
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
            label = { Text("First Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.editLastName,
            onValueChange = { onEvent(ProfileEvent.UpdateLastName(it)) },
            label = { Text("Last Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.editPhoneNumber,
            onValueChange = { onEvent(ProfileEvent.UpdatePhoneNumber(it)) },
            label = { Text("Phone Number") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(24.dp))

        LoadingButton(
            text = "Save Changes",
            onClick = { onEvent(ProfileEvent.SaveProfile) },
            isLoading = state.isSaving,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = { onEvent(ProfileEvent.CancelEditing) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Cancel")
        }
    }
}
