package io.github.taetae98coding.diary.core.network.api.authentication.datasource

import io.github.taetae98coding.diary.core.network.api.authentication.entity.SessionRemoteEntity

public interface SessionRemoteDataSource {
    public suspend fun createWithGoogle(
        code: String,
        clientId: String,
        redirectUri: String,
        codeVerifier: String? = null,
    ): SessionRemoteEntity

    public suspend fun createWithGoogle(
        idToken: String,
        nonce: String,
    ): SessionRemoteEntity

    public suspend fun createWithApple(
        idToken: String,
        nonce: String,
    ): SessionRemoteEntity
}
