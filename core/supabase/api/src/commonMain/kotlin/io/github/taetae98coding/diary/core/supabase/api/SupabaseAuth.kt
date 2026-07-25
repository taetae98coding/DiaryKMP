package io.github.taetae98coding.diary.core.supabase.api

import kotlinx.coroutines.flow.Flow

public interface SupabaseAuth {
    public suspend fun importAuthToken(
        accessToken: String,
        refreshToken: String,
    )

    public fun getSessionStatusFlow(): Flow<SupabaseSessionStatus>

    public fun getUserFlow(): Flow<SupabaseUser?>

    public suspend fun signOut()
}
