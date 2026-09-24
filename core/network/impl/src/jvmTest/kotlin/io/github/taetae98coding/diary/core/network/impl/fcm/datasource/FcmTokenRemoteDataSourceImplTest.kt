package io.github.taetae98coding.diary.core.network.impl.fcm.datasource

import io.github.taetae98coding.diary.core.network.api.fcm.entity.FcmTokenRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.TestException
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot

class FcmTokenRemoteDataSourceImplTest :
    FunSpec({
        test("TC-FCM-TOKEN-DATA-001 등록 제출은 토큰, 시간대, 언어를 담아 v1-fcm-token-submit로 보낸다") {
            val fcmToken = FcmTokenRemoteEntity(token = "token-a", timeZone = "Asia/Seoul", language = "ko-KR")
            val bodySlot = slot<Any>()
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery {
                supabaseFunction(function = "v1-fcm-token-submit", body = capture(bodySlot), typeInfo = any(), headers = any())
            } returns httpResponse()
            val dataSource = FcmTokenRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            dataSource.upsert(fcmToken = fcmToken)

            bodySlot.captured shouldBe fcmToken
        }

        test("TC-FCM-TOKEN-DATA-002 해제 제출은 토큰만 담아 v1-fcm-token-submit로 보낸다") {
            val fcmToken = FcmTokenRemoteEntity(token = "token-a")
            val bodySlot = slot<Any>()
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery {
                supabaseFunction(function = "v1-fcm-token-submit", body = capture(bodySlot), typeInfo = any(), headers = any())
            } returns httpResponse()
            val dataSource = FcmTokenRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            dataSource.upsert(fcmToken = fcmToken)

            bodySlot.captured shouldBe FcmTokenRemoteEntity(token = "token-a", timeZone = null, language = null)
        }

        test("TC-FCM-TOKEN-DOMAIN-016 요청이 실패하면 실패를 그대로 전달한다") {
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery { supabaseFunction(function = any(), body = any(), typeInfo = any(), headers = any()) } throws TestException("network")
            val dataSource = FcmTokenRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            shouldThrow<TestException> { dataSource.upsert(fcmToken = FcmTokenRemoteEntity(token = "token-a")) }
        }
    })

private suspend fun httpResponse(): HttpResponse {
    val client =
        HttpClient(
            MockEngine {
                respond(
                    content = "{}",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            },
        )

    return client.get("/")
}
