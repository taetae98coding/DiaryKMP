package io.github.taetae98coding.diary.core.supabase.impl

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.SignOutScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.taetae98coding.diary.core.supabase.api.SupabaseAuth
import io.github.taetae98coding.diary.core.supabase.api.SupabaseSessionStatus
import io.github.taetae98coding.diary.core.supabase.api.SupabaseUser
import io.github.taetae98coding.diary.core.supabase.impl.mapper.toSupabaseSessionStatus
import io.github.taetae98coding.diary.core.supabase.impl.mapper.toSupabaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
internal class SupabaseAuthImpl(
    private val client: SupabaseClient,
) : SupabaseAuth {
    override suspend fun importAuthToken(
        accessToken: String,
        refreshToken: String,
    ) {
        client.auth.importAuthToken(
            accessToken = accessToken,
            refreshToken = refreshToken,
            retrieveUser = true,
            autoRefresh = true,
        )
    }

    override fun getSessionStatusFlow(): Flow<SupabaseSessionStatus> =
        client.auth.sessionStatus
            .map { status -> status.toSupabaseSessionStatus() }

    // 인증 제공자는 사용자 정보를 갱신할 때 새 상태를 먼저 알리고 저장은 그 뒤에 하므로,
    // 알림을 받고 저장소를 다시 읽으면 갱신 직전 값을 읽는다. 알림이 담아 온 세션을 그대로 쓴다.
    override fun getUserFlow(): Flow<SupabaseUser?> =
        client.auth.sessionStatus
            .map { status ->
                (status as? SessionStatus.Authenticated)?.session?.user?.toSupabaseUser() ?: loadStoredUser()
            }

    override suspend fun retrieveUserForCurrentSession() {
        client.auth.retrieveUserForCurrentSession(updateSession = true)
    }

    override suspend fun signOut() {
        client.auth.signOut(scope = SignOutScope.LOCAL)
    }

    private suspend fun loadStoredUser(): SupabaseUser? =
        client.auth.sessionManager
            .loadSessionOrNull()
            ?.user
            ?.toSupabaseUser()
}
