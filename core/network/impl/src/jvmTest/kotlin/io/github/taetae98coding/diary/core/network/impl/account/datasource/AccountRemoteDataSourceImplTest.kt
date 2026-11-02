package io.github.taetae98coding.diary.core.network.impl.account.datasource

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.network.api.account.entity.AppleAuthorizationRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.TestException
import io.github.taetae98coding.diary.core.network.impl.account.entity.AccountDeleteRequestRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
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
import kotlinx.serialization.json.Json

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class AccountRemoteDataSourceImplTest :
    FunSpec({
        test("TC-8f644268-bf99-49ba-9d69-db161bfe2ed8 Apple 재인증이 없으면 Apple 정보 없이 v1-account-delete로 보낸다") {
            val bodySlot = slot<Any>()
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery { supabaseFunction(function = "v1-account-delete", body = capture(bodySlot), typeInfo = any(), headers = any()) } returns httpResponse()

            AccountRemoteDataSourceImpl(supabaseFunction = supabaseFunction).delete(appleAuthorization = null)

            val body = bodySlot.captured as AccountDeleteRequestRemoteEntity
            body.apple shouldBe null
            Json.encodeToString(AccountDeleteRequestRemoteEntity.serializer(), body) shouldBe """{"apple":null}"""
        }

        test("TC-8f644268-bf99-49ba-9d69-db161bfe2ed8 Apple 재인증을 거쳤으면 인가 코드와 식별 값을 담아 보낸다") {
            val authorization = fixtureMonkey.giveMeOne<AppleAuthorizationRemoteEntity>()
            val bodySlot = slot<Any>()
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery { supabaseFunction(function = "v1-account-delete", body = capture(bodySlot), typeInfo = any(), headers = any()) } returns httpResponse()

            AccountRemoteDataSourceImpl(supabaseFunction = supabaseFunction).delete(appleAuthorization = authorization)

            val body = bodySlot.captured as AccountDeleteRequestRemoteEntity
            body.apple shouldBe authorization
            Json.encodeToString(AccountDeleteRequestRemoteEntity.serializer(), body) shouldBe
                """{"apple":{"authorizationCode":${Json.encodeToString(authorization.authorizationCode)},"clientId":${Json.encodeToString(authorization.clientId)}}}"""
        }

        test("TC-8f644268-bf99-49ba-9d69-db161bfe2ed8 인가 코드와 식별 값이 비어 있어도 두 키를 담아 보낸다") {
            val body = AccountDeleteRequestRemoteEntity(apple = AppleAuthorizationRemoteEntity(authorizationCode = "", clientId = ""))

            Json.encodeToString(AccountDeleteRequestRemoteEntity.serializer(), body) shouldBe
                """{"apple":{"authorizationCode":"","clientId":""}}"""
        }

        test("요청이 실패하면 실패를 그대로 전달한다") {
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery { supabaseFunction(function = any(), body = any(), typeInfo = any(), headers = any()) } throws TestException("network")

            shouldThrow<TestException> { AccountRemoteDataSourceImpl(supabaseFunction = supabaseFunction).delete(appleAuthorization = null) }
        }
    })

private suspend fun httpResponse(): HttpResponse =
    HttpClient(
        MockEngine {
            respond(
                content = "{}",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        },
    ).get("/")
