package io.github.taetae98coding.diary.core.network.impl.authentication.datasource

import io.github.taetae98coding.diary.core.network.api.authentication.datasource.SessionRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.authentication.entity.SessionRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.authentication.entity.AppleIdTokenRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.authentication.entity.GoogleAuthorizationCodeRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.authentication.entity.GoogleIdTokenRequestRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.supabase.api.invoke
import io.ktor.client.call.body
import org.koin.core.annotation.Factory

@Factory
internal class SessionRemoteDataSourceImpl(
    private val supabaseFunction: SupabaseFunction,
) : SessionRemoteDataSource {
    override suspend fun createWithGoogle(
        code: String,
        clientId: String,
        redirectUri: String,
        codeVerifier: String?,
    ): SessionRemoteEntity =
        supabaseFunction(
            function = "v1-session-google-authorization-code",
            body =
                GoogleAuthorizationCodeRequestRemoteEntity(
                    authorizationCode = code,
                    clientId = clientId,
                    redirectUri = redirectUri,
                    codeVerifier = codeVerifier,
                ),
        ).body()

    override suspend fun createWithGoogle(
        idToken: String,
        nonce: String,
    ): SessionRemoteEntity =
        supabaseFunction(
            function = "v1-session-google-id-token",
            body = GoogleIdTokenRequestRemoteEntity(idToken = idToken, nonce = nonce),
        ).body()

    override suspend fun createWithApple(
        idToken: String,
        nonce: String,
    ): SessionRemoteEntity =
        supabaseFunction(
            function = "v1-session-apple-id-token",
            body = AppleIdTokenRequestRemoteEntity(idToken = idToken, nonce = nonce),
        ).body()
}
