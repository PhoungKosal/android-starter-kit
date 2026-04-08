package com.t3r.android_starter_kit.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Card
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.starterkit.app.presentation.components.ErrorView
import com.starterkit.app.presentation.components.LoadingButton
import com.starterkit.app.presentation.components.LoadingView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    onLoggedOut: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.loggedOut) {
        if (state.loggedOut) onLoggedOut()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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

                            Text(
                                text = user.displayName,
                                style = MaterialTheme.typography.headlineSmall,
                            )

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

                        // Info section
                        ListItem(
                            headlineContent = { Text(user.email) },
                            supportingContent = { Text("Email") },
                            leadingContent = {
                                Icon(Icons.Outlined.Email, contentDescription = null)
                            },
                        )

                        user.phoneNumber?.let { phone ->
                            ListItem(
                                headlineContent = { Text(phone) },
                                supportingContent = { Text("Phone") },
                                leadingContent = {
                                    Icon(Icons.Outlined.Person, contentDescription = null)
                                },
                            )
                        }

                        ListItem(
                            headlineContent = {
                                Text(if (user.twoFactorEnabled) "Enabled" else "Disabled")
                            },
                            supportingContent = { Text("Two-Factor Authentication") },
                            leadingContent = {
                                Icon(Icons.Outlined.Security, contentDescription = null)
                            },
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Logout button
                        LoadingButton(
                            text = "Sign Out",
                            onClick = { viewModel.onEvent(ProfileEvent.Logout) },
                            isLoading = state.isLoggingOut,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                        )

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}
