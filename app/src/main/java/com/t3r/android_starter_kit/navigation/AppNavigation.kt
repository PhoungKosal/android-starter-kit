package com.t3r.android_starter_kit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.t3r.android_starter_kit.presentation.auth.AuthViewModel
import com.t3r.android_starter_kit.presentation.auth.ForgotPasswordScreen
import com.t3r.android_starter_kit.presentation.auth.LoginScreen
import com.t3r.android_starter_kit.presentation.auth.RegisterScreen
import com.t3r.android_starter_kit.presentation.auth.ResetPasswordScreen
import com.t3r.android_starter_kit.presentation.auth.ResetPasswordViewModel
import com.t3r.android_starter_kit.presentation.auth.TwoFactorScreen
import com.t3r.android_starter_kit.presentation.auth.VerifyEmailScreen
import com.t3r.android_starter_kit.presentation.auth.VerifyEmailViewModel
import com.t3r.android_starter_kit.presentation.home.HomeScreen
import com.t3r.android_starter_kit.presentation.home.HomeViewModel
import com.t3r.android_starter_kit.presentation.notifications.NotificationsScreen
import com.t3r.android_starter_kit.presentation.notifications.NotificationsViewModel
import com.t3r.android_starter_kit.presentation.profile.ProfileScreen
import com.t3r.android_starter_kit.presentation.profile.ProfileViewModel
import com.t3r.android_starter_kit.presentation.settings.SettingsScreen
import com.t3r.android_starter_kit.presentation.settings.SettingsViewModel
import com.t3r.android_starter_kit.presentation.settings.TwoFactorScreen as TwoFactorSettingsScreen
import com.t3r.android_starter_kit.presentation.settings.TwoFactorViewModel

@Composable
fun AppNavigation(
    isLoggedIn: Boolean,
    openNotifications: Boolean = false,
    pendingDeepLink: String? = null,
    onNotificationsOpened: () -> Unit = {},
) {
    val startRoute: NavKey = if (isLoggedIn) Route.Home else Route.Login
    val backStack = rememberNavBackStack(startRoute)

    // React to auth state changes (login, logout, session expiry)
    LaunchedEffect(isLoggedIn) {
        val targetRoute: NavKey = if (isLoggedIn) Route.Home else Route.Login
        if (backStack.lastOrNull() != targetRoute) {
            backStack.removeAll { true }
            backStack.add(targetRoute)
        }
    }

    // Navigate to Notifications when the user taps a push notification.
    // Key on BOTH values so it re-fires when isLoggedIn becomes true after cold start.
    LaunchedEffect(openNotifications, isLoggedIn) {
        if (openNotifications && isLoggedIn) {
            // Ensure Home is the base, then push Notifications on top
            if (backStack.lastOrNull() != Route.Notifications) {
                backStack.removeAll { true }
                backStack.add(Route.Home)
                backStack.add(Route.Notifications)
            }
            onNotificationsOpened()
        }
    }

    NavDisplay(
        backStack = backStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
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
                    onNavigateToTwoFactor = { challengeToken -> backStack.add(Route.TwoFactor(challengeToken)) },
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
                    onNavigateToVerifyEmail = { email ->
                        backStack.add(Route.VerifyEmail(email))
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

            entry<Route.TwoFactor> { route ->
                val viewModel: AuthViewModel = hiltViewModel()
                TwoFactorScreen(
                    viewModel = viewModel,
                    challengeToken = route.challengeToken,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onNavigateToHome = {
                        backStack.removeAll { true }
                        backStack.add(Route.Home)
                    },
                )
            }

            entry<Route.VerifyEmail> {
                val viewModel: VerifyEmailViewModel = hiltViewModel()
                VerifyEmailScreen(
                    viewModel = viewModel,
                    onNavigateBack = { backStack.removeLastOrNull() },
                )
            }

            entry<Route.ResetPassword> {
                val viewModel: ResetPasswordViewModel = hiltViewModel()
                ResetPasswordScreen(
                    viewModel = viewModel,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onNavigateToLogin = {
                        backStack.removeAll { true }
                        backStack.add(Route.Login)
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
                    onNavigateToSettings = { backStack.add(Route.Settings) },
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

            // -- Settings --

            entry<Route.Settings> {
                val viewModel: SettingsViewModel = hiltViewModel()
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onNavigateToTwoFactor = {
                        val twoFactorEnabled = viewModel.state.value.user?.twoFactorEnabled ?: false
                        backStack.add(Route.TwoFactorSettings(twoFactorEnabled))
                    },
                )
            }

            entry<Route.TwoFactorSettings> { route ->
                val viewModel: TwoFactorViewModel = hiltViewModel()
                TwoFactorSettingsScreen(
                    viewModel = viewModel,
                    twoFactorEnabled = route.twoFactorEnabled,
                    onNavigateBack = { backStack.removeLastOrNull() },
                )
            }
        },
    )
}
