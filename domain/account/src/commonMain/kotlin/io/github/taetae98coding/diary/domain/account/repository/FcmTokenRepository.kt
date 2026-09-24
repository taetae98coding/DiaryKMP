package io.github.taetae98coding.diary.domain.account.repository

public interface FcmTokenRepository {
    public suspend fun upsert()

    public suspend fun delete()
}
