package io.github.taetae98coding.diary.core.network.impl.file.transport

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.network.api.file.entity.FileRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.exception.FileTooLargeRemoteException
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunctionException
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.decodeURLQueryComponent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.readRemaining
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.absoluteValue
import kotlin.time.Duration

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

// 한 번에 보내는 덩어리(8KiB)보다 커야 보낸 양이 여러 번 알려진다.
private const val CONTENT_BYTES = 20_000

class SupabaseFunctionFileUploadTransportTest :
    FunSpec({
        test("TC-FILE-STORAGE-DATA-005 고른 파일의 내용과 크기를 그대로 보내고 서버가 돌려준 파일 정보를 돌려준다") {
            // 한 번에 보내는 덩어리보다 크게 만들어 보낸 양이 여러 번 알려지게 한다.
            val bytes =
                generateSequence { "chunk-${fixtureMonkey.giveMeOne<String>()}" }
                    .flatMap { value -> value.encodeToByteArray().asSequence() }
                    .take(CONTENT_BYTES)
                    .toList()
                    .toByteArray()
            val response = fixtureMonkey.giveMeOne<FileRemoteEntity>()
            val bodySlot = slot<Any>()
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery {
                supabaseFunction(
                    function = "v1-file-upload",
                    body = capture(bodySlot),
                    typeInfo = any(),
                    headers = any(),
                    requestTimeout = any(),
                )
            } returns httpResponse(body = Json.encodeToString(response))
            val sentBytesList = mutableListOf<Long>()
            val transport = SupabaseFunctionFileUploadTransport(supabaseFunction = supabaseFunction)

            val mimeType = mimeType()
            val actual =
                transport.upload(
                    name = fileName(),
                    mimeType = mimeType,
                    contentLength = bytes.size.toLong(),
                    openContent = { Buffer().apply { write(bytes) } },
                    onSent = { sentBytes -> sentBytesList += sentBytes },
                )

            val content = bodySlot.captured.shouldBeInstanceOf<OutgoingContent.WriteChannelContent>()
            content.contentType shouldBe ContentType.parse(mimeType)
            content.contentLength shouldBe bytes.size.toLong()
            content.writtenBytes() shouldBe bytes
            sentBytesList.last() shouldBe bytes.size.toLong()
            sentBytesList.zipWithNext().all { (before, after) -> before <= after } shouldBe true
            actual shouldBe response
        }

        test("TC-FILE-STORAGE-DOMAIN-002 파일 이름을 헤더에 실을 수 있게 인코딩하고 형식을 함께 보낸다") {
            val name = "보고서 ${fixtureMonkey.giveMeOne<String>()}.pdf"
            val headersSlot = slot<Headers>()
            val bodySlot = slot<Any>()
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery {
                supabaseFunction(
                    function = "v1-file-upload",
                    body = capture(bodySlot),
                    typeInfo = any(),
                    headers = capture(headersSlot),
                    requestTimeout = any(),
                )
            } returns httpResponse(body = Json.encodeToString(fixtureMonkey.giveMeOne<FileRemoteEntity>()))
            val transport = SupabaseFunctionFileUploadTransport(supabaseFunction = supabaseFunction)

            transport.upload(
                name = name,
                mimeType = "application/pdf",
                contentLength = 0,
                openContent = { Buffer() },
                onSent = {},
            )

            val encodedName = headersSlot.captured["X-File-Name"].shouldNotBeNull()
            encodedName.all { char -> char.code < 128 } shouldBe true
            encodedName.decodeURLQueryComponent() shouldBe name
            bodySlot.captured.shouldBeInstanceOf<OutgoingContent.WriteChannelContent>().contentType shouldBe ContentType.Application.Pdf
        }

        test("TC-FILE-STORAGE-DATA-013 올리기 요청에는 시간 제한을 두지 않는다") {
            var requestTimeout: Duration? = null
            val response = httpResponse(body = Json.encodeToString(fixtureMonkey.giveMeOne<FileRemoteEntity>()))
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery {
                supabaseFunction(
                    function = "v1-file-upload",
                    body = any(),
                    typeInfo = any(),
                    headers = any(),
                    requestTimeout = any(),
                )
            } answers {
                requestTimeout = invocation.args[4] as Duration?
                response
            }
            val transport = SupabaseFunctionFileUploadTransport(supabaseFunction = supabaseFunction)

            transport.upload(
                name = fileName(),
                mimeType = mimeType(),
                contentLength = 0,
                openContent = { Buffer() },
                onSent = {},
            )

            requestTimeout shouldBe Duration.INFINITE
        }

        test("TC-FILE-STORAGE-DATA-006 서버가 크기 초과로 거절하면 크기 초과 실패로 구분한다") {
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery {
                supabaseFunction(
                    function = "v1-file-upload",
                    body = any(),
                    typeInfo = any(),
                    headers = any(),
                    requestTimeout = any(),
                )
            } throws SupabaseFunctionException(statusCode = HttpStatusCode.PayloadTooLarge.value)
            val transport = SupabaseFunctionFileUploadTransport(supabaseFunction = supabaseFunction)

            shouldThrow<FileTooLargeRemoteException> {
                transport.upload(name = fileName(), mimeType = mimeType(), contentLength = 0, openContent = { Buffer() }, onSent = {})
            }
        }

        test("TC-FILE-STORAGE-DATA-006 이름 거절과 그 밖의 서버 실패는 크기 초과가 아닌 실패로 그대로 전달한다") {
            listOf(HttpStatusCode.BadRequest, HttpStatusCode.InternalServerError).forEach { status ->
                val exception = SupabaseFunctionException(statusCode = status.value)
                val supabaseFunction = mockk<SupabaseFunction>()
                coEvery {
                    supabaseFunction(
                        function = "v1-file-upload",
                        body = any(),
                        typeInfo = any(),
                        headers = any(),
                        requestTimeout = any(),
                    )
                } throws exception
                val transport = SupabaseFunctionFileUploadTransport(supabaseFunction = supabaseFunction)

                val actual =
                    shouldThrow<SupabaseFunctionException> {
                        transport.upload(name = fileName(), mimeType = mimeType(), contentLength = 0, openContent = { Buffer() }, onSent = {})
                    }

                actual shouldBeSameInstanceAs exception
            }
        }

        test("앱이 살아 있는 동안에만 보내므로 앞선 실행에서 이어지는 올리기가 없다") {
            val transport = SupabaseFunctionFileUploadTransport(supabaseFunction = mockk())

            transport.getContinuedUpload().first().shouldBeNull()
            transport.getContinuedUploadResult().toList().shouldBeEmpty()
            transport.cancelContinuedUpload()
        }
    })

private suspend fun OutgoingContent.WriteChannelContent.writtenBytes(): ByteArray {
    val channel = ByteChannel(autoFlush = true)

    writeTo(channel)
    channel.flushAndClose()

    return channel.readRemaining().readByteArray()
}

private suspend fun httpResponse(body: String): HttpResponse {
    val client =
        HttpClient(
            MockEngine {
                respond(
                    content = body,
                    status = HttpStatusCode.OK,
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

private fun fileName(): String = "file-${fixtureMonkey.giveMeOne<Int>()}.txt"

private fun mimeType(): String = "application/x-${fixtureMonkey.giveMeOne<Int>().absoluteValue}"
