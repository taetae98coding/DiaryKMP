package io.github.taetae98coding.diary.core.network.impl.datasource

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.network.api.profile.entity.ProfileImageRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.readRemaining
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class ProfileImageRemoteDataSourceImplTest :
    FunSpec({
        test("이미지 내용을 미리 읽지 않고 전송하는 동안 그대로 흘려보낸다") {
            val bytes = "profile-image-${fixtureMonkey.giveMeOne<String>()}".encodeToByteArray()
            val response = fixtureMonkey.giveMeOne<ProfileImageRemoteEntity>()
            val bodySlot = slot<Any>()
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery {
                supabaseFunction(
                    function = "v1-profile-upload",
                    body = capture(bodySlot),
                    typeInfo = any(),
                    headers = any(),
                )
            } returns httpResponse(body = Json.encodeToString(response))
            val dataSource = ProfileImageRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            val actual =
                dataSource.upload(
                    mimeType = "image/png",
                    contentLength = bytes.size.toLong(),
                    openContent = { Buffer().apply { write(bytes) } },
                )

            val content = bodySlot.captured.shouldBeInstanceOf<OutgoingContent.WriteChannelContent>()
            content.contentType shouldBe ContentType.Image.PNG
            content.contentLength shouldBe bytes.size.toLong()
            content.writtenBytes() shouldBe bytes
            actual shouldBe response
        }
    })

private suspend fun OutgoingContent.WriteChannelContent.writtenBytes(): ByteArray {
    val channel = ByteChannel(autoFlush = true)

    writeTo(channel)
    channel.flushAndClose()

    return channel.readRemaining().readByteArray()
}

private suspend fun httpResponse(
    body: String,
    status: HttpStatusCode = HttpStatusCode.OK,
): HttpResponse {
    val client =
        HttpClient(
            MockEngine {
                respond(
                    content = body,
                    status = status,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            },
        ) {
            install(ContentNegotiation) {
                json()
            }
        }

    return client.get("/")
}
