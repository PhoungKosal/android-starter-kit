package com.t3r.android_starter_kit.presentation.profile

import android.net.Uri
import com.t3r.android_starter_kit.core.result.AppError
import com.t3r.android_starter_kit.domain.model.User

data class ProfileState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val error: AppError? = null,
    val isLoggingOut: Boolean = false,
    val loggedOut: Boolean = false,

    // Edit profile
    val isEditing: Boolean = false,
    val editFirstName: String = "",
    val editLastName: String = "",
    val editPhoneNumber: String = "",
    val isSaving: Boolean = false,
    val profileUpdated: Boolean = false,

    // Avatar upload
    val isUploadingAvatar: Boolean = false,

    // Delete account
    val showDeleteDialog: Boolean = false,
    val deletePassword: String = "",
    val isDeleting: Boolean = false,
    val accountDeleted: Boolean = false,
)

sealed interface ProfileEvent {
    data object LoadProfile : ProfileEvent
    data object Logout : ProfileEvent

    // Edit profile
    data object StartEditing : ProfileEvent
    data object CancelEditing : ProfileEvent
    data class UpdateFirstName(val value: String) : ProfileEvent
    data class UpdateLastName(val value: String) : ProfileEvent
    data class UpdatePhoneNumber(val value: String) : ProfileEvent
    data object SaveProfile : ProfileEvent

    // Avatar
    data class UploadAvatar(val uri: Uri) : ProfileEvent

    // Delete account
    data object ShowDeleteDialog : ProfileEvent
    data object DismissDeleteDialog : ProfileEvent
    data class UpdateDeletePassword(val value: String) : ProfileEvent
    data object ConfirmDeleteAccount : ProfileEvent
    data object ClearError : ProfileEvent
}
