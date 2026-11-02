package io.github.taetae98coding.diary.core.supabase.impl.mapper

import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.taetae98coding.diary.core.supabase.api.SupabaseSessionStatus
import io.github.taetae98coding.diary.core.supabase.api.SupabaseUser
import kotlinx.serialization.json.JsonArray
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
        providerSet = findProviderSet(),
    )

// 연결된 로그인 수단은 인증 제공자가 앱 메타데이터의 providers에 모아 두고, 사용자 정보를 다시 받으면 identities에도 담긴다.
private fun UserInfo.findProviderSet(): Set<String> =
    buildSet {
        (appMetadata?.get("providers") as? JsonArray)?.forEach { element ->
            (element as? JsonPrimitive)?.contentOrNull?.let(::add)
        }
        identities?.forEach { identity -> add(identity.provider) }
    }

private fun UserInfo.findMetadataContent(key: String): String? = (userMetadata?.get(key) as? JsonPrimitive)?.contentOrNull
