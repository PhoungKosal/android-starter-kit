package com.t3r.android_starter_kit.data.mapper

import com.t3r.android_starter_kit.data.remote.dto.auth.PermissionDto
import com.t3r.android_starter_kit.data.remote.dto.auth.RoleDto
import com.t3r.android_starter_kit.data.remote.dto.auth.RuleDto
import com.t3r.android_starter_kit.data.remote.dto.auth.UserDto
import com.t3r.android_starter_kit.data.remote.dto.auth.UserSettingsDto
import com.t3r.android_starter_kit.data.remote.dto.auth.Enable2faResponseDto
import com.t3r.android_starter_kit.data.remote.dto.auth.Setup2faResponseDto
import com.t3r.android_starter_kit.data.remote.dto.files.FileDto
import com.t3r.android_starter_kit.data.remote.dto.files.UploadUrlResponseDto
import com.t3r.android_starter_kit.data.remote.dto.notifications.NotificationDto
import com.t3r.android_starter_kit.domain.model.CaslRule
import com.t3r.android_starter_kit.domain.model.FileInfo
import com.t3r.android_starter_kit.domain.model.Notification
import com.t3r.android_starter_kit.domain.model.TwoFactorSetup
import com.t3r.android_starter_kit.domain.model.NotificationType
import com.t3r.android_starter_kit.domain.model.Permission
import com.t3r.android_starter_kit.domain.model.Role
import com.t3r.android_starter_kit.domain.model.UploadUrl
import com.t3r.android_starter_kit.domain.model.User
import com.t3r.android_starter_kit.domain.model.UserSettings

// -- User mapping --

fun UserDto.toDomain(): User = User(
    id = id,
    email = email,
    username = username,
    isActive = isActive,
    isEmailVerified = isEmailVerified,
    twoFactorEnabled = twoFactorEnabled,
    firstName = profile?.firstName,
    lastName = profile?.lastName,
    phoneNumber = profile?.phoneNumber,
    avatar = avatarUrl ?: profile?.avatar,
    bio = profile?.bio,
    location = profile?.location,
    role = role?.toDomain(),
    language = settings?.language,
    theme = settings?.theme,
    notificationsEnabled = settings?.emailNotifications ?: true,
    createdAt = createdAt,
)

fun UserSettingsDto.toDomain(): UserSettings = UserSettings(
    theme = theme ?: "light",
    language = language ?: "en",
    timezone = timezone ?: "UTC",
    dateFormat = dateFormat ?: "MM/DD/YYYY",
    primaryColor = primaryColor ?: "blue",
    neutralColor = neutralColor ?: "slate",
    emailNotifications = emailNotifications,
    pushNotifications = pushNotifications,
)

fun RoleDto.toDomain(): Role = Role(
    id = id,
    name = name,
    description = description,
    permissions = permissions?.map { it.toDomain() } ?: emptyList(),
)

fun PermissionDto.toDomain(): Permission = Permission(
    id = id,
    action = action,
    subject = subject,
    key = key,
)

fun RuleDto.toDomain(): CaslRule = CaslRule(
    action = action,
    subject = subject,
    conditions = conditions,
    inverted = inverted,
)

// -- 2FA mapping --

fun Setup2faResponseDto.toDomain(): TwoFactorSetup = TwoFactorSetup(
    qrCode = qrCode,
    manualEntryKey = manualEntryKey,
)

fun Enable2faResponseDto.toDomain(): List<String> = recoveryCodes

// -- Notification mapping --

fun NotificationDto.toDomain(): Notification = Notification(
    id = id,
    key = key,
    data = data,
    title = title,
    message = message,
    type = NotificationType.fromString(type),
    isRead = isRead,
    link = link,
    createdAt = createdAt,
)

// -- File mapping --

fun FileDto.toDomain(): FileInfo = FileInfo(
    id = id,
    originalName = originalName,
    mimeType = mimeType,
    size = size,
    url = url,
    createdAt = createdAt,
)

fun UploadUrlResponseDto.toDomain(): UploadUrl = UploadUrl(
    uploadToken = uploadToken,
    uploadUrl = uploadUrl,
    key = key,
    expiresIn = expiresIn,
)