package com.t3r.android_starter_kit.data.mapper

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
    avatar = profile?.avatar,
    bio = profile?.bio,
    location = profile?.location,
    role = role?.toDomain(),
    language = settings?.language,
    theme = settings?.theme,
    notificationsEnabled = settings?.notificationsEnabled ?: true,
    createdAt = createdAt,
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
    expiresAt = expiresAt,
)