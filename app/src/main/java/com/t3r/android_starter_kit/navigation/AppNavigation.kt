package com.t3r.android_starter_kit.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.starterkit.app.presentation.auth.AuthViewModel
import com.starterkit.app.presentation.auth.ForgotPasswordScreen
import com.starterkit.app.presentation.auth.LoginScreen
import com.starterkit.app.presentation.auth.RegisterScreen
import com.starterkit.app.presentation.auth.TwoFactorScreen
import com.starterkit.app.presentation.home.HomeScreen
import com.starterkit.app.presentation.home.HomeViewModel
import com.starterkit.app.presentation.notifications.NotificationsScreen
import com.starterkit.app.presentation.notifications.NotificationsViewModel
import com.starterkit.app.presentation.profile.ProfileScreen
import com.starterkit.app.presentation.profile.ProfileViewModel

@Composable
fun AppNavigation(
    isLoggedIn: Boolean,
) {
    val startRoute: NavKey = if (isLoggedIn) Route.Home else Route.Login
    val backStack = rememberNavBackStack(startRoute)

    NavDisplay(
        backStack = backStack,
        entryProvider = entryProvider {

            // -- Auth Flow --

            entry<Route.Login> {
                val viewModel: AuthViewModel = hiltViewModel()
                LoginScreen(
                    viewModel = viewModel,
                    onNavigateToRegister = { backStack.add(Route.Register) },
                    onNavigateToForgotPassword = { backStack.add(Route.ForgotPassword) },
                    onNavigateToHome = {
                        backStack.removeAll { true }
                        backStack.add(Route.Home)
                    },
                    onNavigateToTwoFactor = { backStack.add(Route.TwoFactor) },
                )
            }

            entry<Route.Register> {
                val viewModel: AuthViewModel = hiltViewModel()
                RegisterScreen(
                    viewModel = viewModel,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onNavigateToHome = {
                        backStack.removeAll { true }
                        backStack.add(Route.Home)
                    },
                )
            }

            entry<Route.ForgotPassword> {
                val viewModel: AuthViewModel = hiltViewModel()
                ForgotPasswordScreen(
                    viewModel = viewModel,
                    onNavigateBack = { backStack.removeLastOrNull() },
                )
            }

            entry<Route.TwoFactor> {
                val viewModel: AuthViewModel = hiltViewModel()
                TwoFactorScreen(
                    viewModel = viewModel,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onNavigateToHome = {
                        backStack.removeAll { true }
                        backStack.add(Route.Home)
                    },
                )
            }

            // -- Main App Flow --

            entry<Route.Home> {
                val viewModel: HomeViewModel = hiltViewModel()
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToProfile = { backStack.add(Route.Profile) },
                    onNavigateToNotifications = { backStack.add(Route.Notifications) },
                )
            }

            entry<Route.Profile> {
                val viewModel: ProfileViewModel = hiltViewModel()
                ProfileScreen(
                    viewModel = viewModel,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onLoggedOut = {
                        backStack.removeAll { true }
                        backStack.add(Route.Login)
                    },
                )
            }

            entry<Route.Notifications> {
                val viewModel: NotificationsViewModel = hiltViewModel()
                NotificationsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { backStack.removeLastOrNull() },
                )
            }
        },
    )
}
