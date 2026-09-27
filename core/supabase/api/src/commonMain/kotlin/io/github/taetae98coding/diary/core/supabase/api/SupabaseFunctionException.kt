package io.github.taetae98coding.diary.core.supabase.api

public class SupabaseFunctionException(
    public val statusCode: Int,
    override val message: String? = null,
    override val cause: Throwable? = null,
) : Exception(message, cause)
