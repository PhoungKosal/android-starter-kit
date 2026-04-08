package com.t3r.android_starter_kit.presentation.settings

import com.t3r.android_starter_kit.core.result.AppError

data class TwoFactorState(
    val isLoading: Boolean = false,
    val twoFactorEnabled: Boolean = false,

    // Enable flow
    val qrCodeUrl: String? = null,
    val secret: String? = null,
    val recoveryCodes: List<String> = emptyList(),
    val verifyCode: String = "",
    val showSetupDialog: Boolean = false,

    // Disable flow
    val disablePassword: String = "",
    val showDisableDialog: Boolean = false,

    val message: String? = null,
    val error: AppError? = null,
)

sealed interface TwoFactorEvent {
    // Enable
    data object RequestEnable : TwoFactorEvent
    data class UpdateVerifyCode(val value: String) : TwoFactorEvent
    data object ConfirmEnable : TwoFactorEvent
    data object DismissSetupDialog : TwoFactorEvent

    // Disable
    data object ShowDisableDialog : TwoFactorEvent
    data object DismissDisableDialog : TwoFactorEvent
    data class UpdateDisablePassword(val value: String) : TwoFactorEvent
    data object ConfirmDisable : TwoFactorEvent

    data object ClearMessage : TwoFactorEvent
}
