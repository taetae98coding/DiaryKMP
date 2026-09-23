package io.github.taetae98coding.diary.core.supabase.impl

import app.cash.turbine.test
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.minimalConfig
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.createSupabaseClient
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.uuid.Uuid

class SupabaseAuthImplTest :
    FunSpec({
        test("인증된 세션을 알리면 저장소가 아니라 알림에 담긴 프로필 이미지를 사용한다") {
            val storedUser = user(profileImage = "https://example.com/stored.jpg")
            val client = createClient(sessionManager = MemorySessionManager(session(storedUser)))

            client.auth.awaitInitialization()
            client.auth.importSession(session(storedUser.withProfileImage("https://example.com/notified.jpg")))

            SupabaseAuthImpl(client).getUserFlow().test {
                awaitItem()?.profileImage shouldBe "https://example.com/notified.jpg"
            }

            client.close()
        }

        test("인증되지 않은 세션을 알리면 저장된 프로필 이미지를 사용한다") {
            val storedUser = user(profileImage = "https://example.com/stored.jpg")
            val client = createClient(sessionManager = MemorySessionManager(session(storedUser)))

            client.auth.awaitInitialization()

            SupabaseAuthImpl(client).getUserFlow().test {
                awaitItem()?.profileImage shouldBe "https://example.com/stored.jpg"
            }

            client.close()
        }

        test("저장된 세션도 인증된 세션도 없으면 사용자 정보를 알리지 않는다") {
            val client = createClient(sessionManager = MemorySessionManager())

            client.auth.awaitInitialization()

            SupabaseAuthImpl(client).getUserFlow().test {
                awaitItem() shouldBe null
            }

            client.close()
        }
    }) {
    companion object {
        // 저장소에 쓰기 전에 상태를 먼저 알리는 순서를 테스트에서 그대로 만들기 위해, 자동 저장을 끄고
        // 저장소의 세션과 알림의 세션을 서로 다르게 둔다.
        private fun createClient(sessionManager: SessionManager): SupabaseClient =
            createSupabaseClient(
                supabaseUrl = "https://example.supabase.co",
                supabaseKey = "test-key",
            ) {
                httpEngine = MockEngine { respondOk() }
                install(Auth) {
                    minimalConfig()
                    this.sessionManager = sessionManager
                }
            }

        private fun user(profileImage: String): UserInfo =
            UserInfo(
                aud = "authenticated",
                id = Uuid.random().toString(),
                email = "diary@example.com",
            ).withProfileImage(profileImage)

        private fun UserInfo.withProfileImage(profileImage: String): UserInfo = copy(userMetadata = JsonObject(mapOf("avatar_url" to JsonPrimitive(profileImage))))

        private fun session(user: UserInfo): UserSession =
            UserSession(
                accessToken = "access-token",
                refreshToken = "refresh-token",
                expiresIn = 3600,
                tokenType = "bearer",
                user = user,
            )
    }
}
