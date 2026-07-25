package io.github.taetae98coding.diary.core.supabase.impl.mapper

import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.taetae98coding.diary.core.supabase.api.SupabaseSessionStatus
import io.github.taetae98coding.diary.core.supabase.api.SupabaseUser
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlin.uuid.Uuid

internal fun SessionStatus.toSupabaseSessionStatus(): SupabaseSessionStatus =
    when (this) {
        is SessionStatus.Authenticated -> SupabaseSessionStatus.Authenticated
        is SessionStatus.Initializing -> SupabaseSessionStatus.Initializing
        is SessionStatus.RefreshFailure -> SupabaseSessionStatus.RefreshFailure
        is SessionStatus.NotAuthenticated -> SupabaseSessionStatus.NotAuthenticated
    }

internal fun UserInfo.toSupabaseUser(): SupabaseUser =
    SupabaseUser(
        id = Uuid.parse(id),
        email = requireNotNull(email) { "Supabase user email is null." },
        profileImage = findMetadataContent("avatar_url") ?: findMetadataContent("picture"),
    )

private fun UserInfo.findMetadataContent(key: String): String? = (userMetadata?.get(key) as? JsonPrimitive)?.contentOrNull
