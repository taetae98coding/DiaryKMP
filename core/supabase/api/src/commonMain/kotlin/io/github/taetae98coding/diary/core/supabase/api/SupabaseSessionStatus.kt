package io.github.taetae98coding.diary.core.supabase.api

public sealed interface SupabaseSessionStatus {
    public data object Authenticated : SupabaseSessionStatus

    public data object Initializing : SupabaseSessionStatus

    public data object RefreshFailure : SupabaseSessionStatus

    public data object NotAuthenticated : SupabaseSessionStatus
}
