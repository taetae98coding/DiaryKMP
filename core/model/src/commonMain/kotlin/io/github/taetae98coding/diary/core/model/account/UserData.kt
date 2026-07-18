package io.github.taetae98coding.diary.core.model.account

import kotlin.uuid.Uuid

public data class UserData(
    val id: Uuid,
    val email: String,
    val profileImage: String?,
)
