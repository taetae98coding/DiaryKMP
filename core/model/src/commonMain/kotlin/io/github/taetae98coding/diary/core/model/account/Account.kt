package io.github.taetae98coding.diary.core.model.account

import kotlin.uuid.Uuid

public sealed interface Account {
    public val id: Uuid

    public data object Guest : Account {
        override val id: Uuid = Uuid.NIL
    }

    public data class User(
        override val id: Uuid,
        val profileImage: String?,
        val email: String,
        val isSessionValid: Boolean,
    ) : Account
}
