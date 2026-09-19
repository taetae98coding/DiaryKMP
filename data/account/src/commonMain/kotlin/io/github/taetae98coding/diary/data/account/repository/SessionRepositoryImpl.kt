package io.github.taetae98coding.diary.data.account.repository

import io.github.taetae98coding.diary.core.model.authentication.AppleCredential
import io.github.taetae98coding.diary.core.model.authentication.GoogleCredential
import io.github.taetae98coding.diary.core.model.authentication.Session
import io.github.taetae98coding.diary.core.network.api.authentication.datasource.SessionRemoteDataSource
import io.github.taetae98coding.diary.core.supabase.api.SupabaseAuth
import io.github.taetae98coding.diary.core.supabase.api.SupabaseSessionStatus
import io.github.taetae98coding.diary.domain.account.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
internal class SessionRepositoryImpl(
    private val sessionRemoteDataSource: SessionRemoteDataSource,
    private val supabaseAuth: SupabaseAuth,
) : SessionRepository {
    override fun get(): Flow<Session> =
        supabaseAuth.getSessionStatusFlow().map { status ->
            when (status) {
                is SupabaseSessionStatus.Authenticated -> Session.Authenticated

                is SupabaseSessionStatus.Initializing,
                is SupabaseSessionStatus.RefreshFailure,
                is SupabaseSessionStatus.NotAuthenticated,
                -> Session.NotAuthenticated
            }
        }

    override suspend fun create(credential: GoogleCredential) {
        val sessionRemoteEntity =
            when (credential) {
                is GoogleCredential.AuthorizationCode -> {
                    sessionRemoteDataSource.createWithGoogle(
                        code = credential.code,
                        clientId = credential.clientId,
                        redirectUri = credential.redirectUri,
                        codeVerifier = credential.codeVerifier,
                    )
                }

                is GoogleCredential.IdToken -> {
                    sessionRemoteDataSource.createWithGoogle(
                        idToken = credential.idToken,
                        nonce = credential.nonce,
                    )
                }
            }

        supabaseAuth.importAuthToken(
            accessToken = sessionRemoteEntity.accessToken,
            refreshToken = sessionRemoteEntity.refreshToken,
        )
    }

    override suspend fun create(credential: AppleCredential) {
        val sessionRemoteEntity =
            sessionRemoteDataSource.createWithApple(
                idToken = credential.idToken,
                nonce = credential.nonce,
            )

        supabaseAuth.importAuthToken(
            accessToken = sessionRemoteEntity.accessToken,
            refreshToken = sessionRemoteEntity.refreshToken,
        )
    }

    override suspend fun delete() {
        supabaseAuth.signOut()
    }
}
