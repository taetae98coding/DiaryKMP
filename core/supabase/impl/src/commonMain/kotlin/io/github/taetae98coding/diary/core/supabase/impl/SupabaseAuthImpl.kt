package io.github.taetae98coding.diary.core.supabase.impl

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.SignOutScope
import io.github.jan.supabase.auth.auth
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

    override fun getUserFlow(): Flow<SupabaseUser?> =
        client.auth.sessionStatus
            .map { loadStoredUser() }

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
