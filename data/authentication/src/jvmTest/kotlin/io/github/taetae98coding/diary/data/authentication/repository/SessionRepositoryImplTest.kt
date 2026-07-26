package io.github.taetae98coding.diary.data.authentication.repository

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.authentication.AppleCredential
import io.github.taetae98coding.diary.core.model.authentication.GoogleCredential
import io.github.taetae98coding.diary.core.model.authentication.Session
import io.github.taetae98coding.diary.core.network.api.authentication.datasource.SessionRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.authentication.entity.SessionRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseAuth
import io.github.taetae98coding.diary.core.supabase.api.SupabaseSessionStatus
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf

class SessionRepositoryImplTest :
    FunSpec({
        test("TC-LOGIN-DATA-001 authorization code 매핑") {
            val credential =
                GoogleCredential.AuthorizationCode(
                    code = fixtureMonkey.giveMeOne<String>(),
                    clientId = fixtureMonkey.giveMeOne<String>(),
                    redirectUri = fixtureMonkey.giveMeOne<String>(),
                    codeVerifier = fixtureMonkey.giveMeOne<String>(),
                )
            val response = fixtureMonkey.giveMeOne<SessionRemoteEntity>()
            val remoteDataSource = mockk<SessionRemoteDataSource>()
            val supabaseAuth = mockk<SupabaseAuth>()
            coEvery {
                remoteDataSource.createWithGoogle(
                    code = credential.code,
                    clientId = credential.clientId,
                    redirectUri = credential.redirectUri,
                    codeVerifier = credential.codeVerifier,
                )
            } returns response
            coEvery {
                supabaseAuth.importAuthToken(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken,
                )
            } returns Unit
            val repository =
                SessionRepositoryImpl(
                    sessionRemoteDataSource = remoteDataSource,
                    supabaseAuth = supabaseAuth,
                )

            repository.create(credential = credential)

            coVerify(exactly = 1) {
                remoteDataSource.createWithGoogle(
                    code = credential.code,
                    clientId = credential.clientId,
                    redirectUri = credential.redirectUri,
                    codeVerifier = credential.codeVerifier,
                )
            }
            coVerify(exactly = 0) {
                remoteDataSource.createWithGoogle(
                    idToken = any(),
                    nonce = any(),
                )
            }
            coVerify(exactly = 1) {
                supabaseAuth.importAuthToken(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken,
                )
            }
        }

        test("TC-LOGIN-DATA-002 idToken 매핑") {
            val credential = fixtureMonkey.giveMeOne<GoogleCredential.IdToken>()
            val response = fixtureMonkey.giveMeOne<SessionRemoteEntity>()
            val remoteDataSource = mockk<SessionRemoteDataSource>()
            val supabaseAuth = mockk<SupabaseAuth>()
            coEvery {
                remoteDataSource.createWithGoogle(
                    idToken = credential.idToken,
                    nonce = credential.nonce,
                )
            } returns response
            coEvery {
                supabaseAuth.importAuthToken(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken,
                )
            } returns Unit
            val repository =
                SessionRepositoryImpl(
                    sessionRemoteDataSource = remoteDataSource,
                    supabaseAuth = supabaseAuth,
                )

            repository.create(credential = credential)

            coVerify(exactly = 0) {
                remoteDataSource.createWithGoogle(
                    code = any(),
                    clientId = any(),
                    redirectUri = any(),
                    codeVerifier = any(),
                )
            }
            coVerify(exactly = 1) {
                remoteDataSource.createWithGoogle(
                    idToken = credential.idToken,
                    nonce = credential.nonce,
                )
            }
            coVerify(exactly = 1) {
                supabaseAuth.importAuthToken(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken,
                )
            }
        }

        test("TC-LOGIN-DATA-007 Apple idToken 매핑") {
            val credential = fixtureMonkey.giveMeOne<AppleCredential>()
            val response = fixtureMonkey.giveMeOne<SessionRemoteEntity>()
            val remoteDataSource = mockk<SessionRemoteDataSource>()
            val supabaseAuth = mockk<SupabaseAuth>()
            coEvery {
                remoteDataSource.createWithApple(
                    idToken = credential.idToken,
                    nonce = credential.nonce,
                )
            } returns response
            coEvery {
                supabaseAuth.importAuthToken(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken,
                )
            } returns Unit
            val repository =
                SessionRepositoryImpl(
                    sessionRemoteDataSource = remoteDataSource,
                    supabaseAuth = supabaseAuth,
                )

            repository.create(credential = credential)

            coVerify(exactly = 1) {
                remoteDataSource.createWithApple(
                    idToken = credential.idToken,
                    nonce = credential.nonce,
                )
            }
            coVerify(exactly = 0) {
                remoteDataSource.createWithGoogle(
                    idToken = any(),
                    nonce = any(),
                )
            }
            coVerify(exactly = 0) {
                remoteDataSource.createWithGoogle(
                    code = any(),
                    clientId = any(),
                    redirectUri = any(),
                    codeVerifier = any(),
                )
            }
            coVerify(exactly = 1) {
                supabaseAuth.importAuthToken(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken,
                )
            }
        }

        test("TC-LOGIN-DATA-003 remote 실패 전파") {
            val credential = fixtureMonkey.giveMeOne<GoogleCredential.IdToken>()
            val failure = TestException("remote failure")
            val remoteDataSource = mockk<SessionRemoteDataSource>()
            val supabaseAuth = mockk<SupabaseAuth>()
            coEvery {
                remoteDataSource.createWithGoogle(
                    idToken = credential.idToken,
                    nonce = credential.nonce,
                )
            } throws failure
            val repository =
                SessionRepositoryImpl(
                    sessionRemoteDataSource = remoteDataSource,
                    supabaseAuth = supabaseAuth,
                )

            val actual =
                shouldThrowExactly<TestException> {
                    repository.create(credential = credential)
                }

            actual.shouldBeSameInstanceAs(failure)
            coVerify(exactly = 1) {
                remoteDataSource.createWithGoogle(
                    idToken = credential.idToken,
                    nonce = credential.nonce,
                )
            }
            coVerify(exactly = 0) {
                supabaseAuth.importAuthToken(
                    accessToken = any(),
                    refreshToken = any(),
                )
            }
        }

        test("TC-LOGIN-DATA-004 token import 실패 전파") {
            val credential = fixtureMonkey.giveMeOne<GoogleCredential.AuthorizationCode>()
            val response = fixtureMonkey.giveMeOne<SessionRemoteEntity>()
            val failure = TestException("token import failure")
            val remoteDataSource = mockk<SessionRemoteDataSource>()
            val supabaseAuth = mockk<SupabaseAuth>()
            coEvery {
                remoteDataSource.createWithGoogle(
                    code = credential.code,
                    clientId = credential.clientId,
                    redirectUri = credential.redirectUri,
                    codeVerifier = credential.codeVerifier,
                )
            } returns response
            coEvery {
                supabaseAuth.importAuthToken(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken,
                )
            } throws failure
            val repository =
                SessionRepositoryImpl(
                    sessionRemoteDataSource = remoteDataSource,
                    supabaseAuth = supabaseAuth,
                )

            val actual =
                shouldThrowExactly<TestException> {
                    repository.create(credential = credential)
                }

            actual.shouldBeSameInstanceAs(failure)
            coVerify(exactly = 1) {
                remoteDataSource.createWithGoogle(
                    code = credential.code,
                    clientId = credential.clientId,
                    redirectUri = credential.redirectUri,
                    codeVerifier = credential.codeVerifier,
                )
            }
            coVerify(exactly = 1) {
                supabaseAuth.importAuthToken(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken,
                )
            }
        }

        test("TC-LOGIN-DATA-003 Apple remote 실패 전파") {
            val credential = fixtureMonkey.giveMeOne<AppleCredential>()
            val failure = TestException("apple remote failure")
            val remoteDataSource = mockk<SessionRemoteDataSource>()
            val supabaseAuth = mockk<SupabaseAuth>()
            coEvery {
                remoteDataSource.createWithApple(
                    idToken = credential.idToken,
                    nonce = credential.nonce,
                )
            } throws failure
            val repository =
                SessionRepositoryImpl(
                    sessionRemoteDataSource = remoteDataSource,
                    supabaseAuth = supabaseAuth,
                )

            val actual =
                shouldThrowExactly<TestException> {
                    repository.create(credential = credential)
                }

            actual.shouldBeSameInstanceAs(failure)
            coVerify(exactly = 0) {
                supabaseAuth.importAuthToken(
                    accessToken = any(),
                    refreshToken = any(),
                )
            }
        }

        test("TC-LOGIN-DATA-004 Apple token import 실패 전파") {
            val credential = fixtureMonkey.giveMeOne<AppleCredential>()
            val response = fixtureMonkey.giveMeOne<SessionRemoteEntity>()
            val failure = TestException("apple token import failure")
            val remoteDataSource = mockk<SessionRemoteDataSource>()
            val supabaseAuth = mockk<SupabaseAuth>()
            coEvery {
                remoteDataSource.createWithApple(
                    idToken = credential.idToken,
                    nonce = credential.nonce,
                )
            } returns response
            coEvery {
                supabaseAuth.importAuthToken(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken,
                )
            } throws failure
            val repository =
                SessionRepositoryImpl(
                    sessionRemoteDataSource = remoteDataSource,
                    supabaseAuth = supabaseAuth,
                )

            val actual =
                shouldThrowExactly<TestException> {
                    repository.create(credential = credential)
                }

            actual.shouldBeSameInstanceAs(failure)
            coVerify(exactly = 1) {
                remoteDataSource.createWithApple(
                    idToken = credential.idToken,
                    nonce = credential.nonce,
                )
            }
            coVerify(exactly = 1) {
                supabaseAuth.importAuthToken(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken,
                )
            }
        }

        listOf(
            SupabaseSessionStatus.Authenticated to Session.Authenticated,
            SupabaseSessionStatus.Initializing to Session.NotAuthenticated,
            SupabaseSessionStatus.RefreshFailure to Session.NotAuthenticated,
            SupabaseSessionStatus.NotAuthenticated to Session.NotAuthenticated,
        ).forEach { (supabaseStatus, session) ->
            test("TC-LOGIN-DATA-006 $supabaseStatus 세션 상태를 $session 로 판단한다") {
                val remoteDataSource = mockk<SessionRemoteDataSource>()
                val supabaseAuth = mockk<SupabaseAuth>()
                every { supabaseAuth.getSessionStatusFlow() } returns flowOf(supabaseStatus)
                val repository =
                    SessionRepositoryImpl(
                        sessionRemoteDataSource = remoteDataSource,
                        supabaseAuth = supabaseAuth,
                    )

                repository.get().test {
                    awaitItem() shouldBe session
                    awaitComplete()
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}

private class TestException(
    message: String,
) : RuntimeException(message)
