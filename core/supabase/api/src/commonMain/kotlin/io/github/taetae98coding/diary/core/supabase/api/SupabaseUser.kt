package io.github.taetae98coding.diary.core.supabase.api

import kotlin.uuid.Uuid

public data class SupabaseUser(
    val id: Uuid,
    val email: String,
    val profileImage: String?,
)
