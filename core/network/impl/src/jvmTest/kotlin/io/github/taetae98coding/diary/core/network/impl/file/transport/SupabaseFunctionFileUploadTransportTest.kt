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
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

// 한 번에 보내는 덩어리(8KiB)보다 커야 보낸 양이 여러 번 알려진다.
private const val CONTENT_BYTES = 20_000

class SupabaseFunctionFileUploadTransportTest :
    FunSpec({
        test("TC-FILE-STORAGE-DATA-005 고른 파일의 내용과 크기를 그대로 보내고 서버가 돌려준 파일 정보를 돌려준다") {
            val bytes = contentBytes()
            val response = fixtureMonkey.giveMeOne<FileRemoteEntity>()
            val bodySlot = slot<Any>()
            val supabaseFunction = uploadFunction(bodySlot = bodySlot, response = response)
            val sentBytesList = mutableListOf<Long>()
            val transport = SupabaseFunctionFileUploadTransport(supabaseFunction = supabaseFunction)

            val actual =
                transport.upload(
                    name = fileName(),
                    title = title(),
                    description = description(),
                    mimeType = mimeType(),
                    contentLength = bytes.size.toLong(),
                    accountId = fixtureMonkey.giveMeOne<Uuid>(),
                    openContent = { Buffer().apply { write(bytes) } },
                    onSent = { sentBytes -> sentBytesList += sentBytes },
                )

            val content = bodySlot.captured.shouldBeInstanceOf<OutgoingContent.WriteChannelContent>()
            val written = content.writtenBytes()
            val parts = written.parseMultipart(boundary = content.boundary())
            parts.getValue("file").body shouldBe bytes
            content.contentLength shouldBe written.size.toLong()
            sentBytesList.last() shouldBe bytes.size.toLong()
            sentBytesList.zipWithNext().all { (before, after) -> before <= after } shouldBe true
            actual shouldBe response
        }

        test("TC-FILE-STORAGE-DOMAIN-002 파일 이름과 형식을 본문에 담아 보내고 헤더에는 이름을 싣지 않는다") {
            val name = "보고서 ${fixtureMonkey.giveMeOne<String>()}.pdf"
            val headersSlot = slot<Headers>()
            val bodySlot = slot<Any>()
            val supabaseFunction = uploadFunction(bodySlot = bodySlot, headersSlot = headersSlot)
            val transport = SupabaseFunctionFileUploadTransport(supabaseFunction = supabaseFunction)

            transport.upload(
                name = name,
                title = title(),
                description = description(),
                mimeType = "application/pdf",
                contentLength = 0,
                accountId = fixtureMonkey.giveMeOne<Uuid>(),
                openContent = { Buffer() },
                onSent = {},
            )

            val content = bodySlot.captured.shouldBeInstanceOf<OutgoingContent.WriteChannelContent>()
            val parts = content.writtenBytes().parseMultipart(boundary = content.boundary())
            content.contentType.shouldNotBeNull().match(ContentType.MultiPart.FormData) shouldBe true
            parts.getValue("name").body.decodeToString() shouldBe name
            parts.getValue("file").headers["content-type"] shouldBe "application/pdf"
            parts.getValue("file").body.size shouldBe 0
            if (headersSlot.isCaptured) headersSlot.captured["X-File-Name"].shouldBeNull()
        }

        test("TC-FILE-STORAGE-DOMAIN-016 사용자가 적은 제목과 설명을 그대로 함께 보낸다") {
            listOf(
                "회의록" to "첫 줄\n둘째 줄",
                " 앞뒤 공백 " to "",
                "가".repeat(1_000) to "나".repeat(10_000),
            ).forEach { (title, description) ->
                val bodySlot = slot<Any>()
                val transport = SupabaseFunctionFileUploadTransport(supabaseFunction = uploadFunction(bodySlot = bodySlot))

                transport.upload(
                    name = fileName(),
                    title = title,
                    description = description,
                    mimeType = mimeType(),
                    contentLength = 0,
                    accountId = fixtureMonkey.giveMeOne<Uuid>(),
                    openContent = { Buffer() },
                    onSent = {},
                )

                val content = bodySlot.captured.shouldBeInstanceOf<OutgoingContent.WriteChannelContent>()
                val parts = content.writtenBytes().parseMultipart(boundary = content.boundary())
                parts.getValue("title").body.decodeToString() shouldBe title
                parts.getValue("description").body.decodeToString() shouldBe description
            }
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
                title = title(),
                description = description(),
                mimeType = mimeType(),
                contentLength = 0,
                accountId = fixtureMonkey.giveMeOne<Uuid>(),
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
                transport.upload(
                    name = fileName(),
                    title = title(),
                    description = description(),
                    mimeType = mimeType(),
                    contentLength = 0,
                    accountId = fixtureMonkey.giveMeOne<Uuid>(),
                    openContent = { Buffer() },
                    onSent = {},
                )
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
                        transport.upload(
                            name = fileName(),
                            title = title(),
                            description = description(),
                            mimeType = mimeType(),
                            contentLength = 0,
                            accountId = fixtureMonkey.giveMeOne<Uuid>(),
                            openContent = { Buffer() },
                            onSent = {},
                        )
                    }

                actual shouldBeSameInstanceAs exception
            }
        }

        test("앱이 살아 있는 동안에만 보내므로 앞선 실행에서 이어지는 올리기가 없다") {
            val transport = SupabaseFunctionFileUploadTransport(supabaseFunction = mockk())

            transport.getContinuedUpload().first().shouldBeNull()
            transport.getContinuedUploadResult().toList().shouldBeEmpty()
            transport.cancelContinuedUpload(exceptAccountId = fixtureMonkey.giveMeOne<Uuid>())
        }
    })

private suspend fun uploadFunction(
    bodySlot: io.mockk.CapturingSlot<Any>,
    headersSlot: io.mockk.CapturingSlot<Headers> = slot(),
    response: FileRemoteEntity = fixtureMonkey.giveMeOne<FileRemoteEntity>(),
): SupabaseFunction {
    val httpResponse = httpResponse(body = Json.encodeToString(response))
    val supabaseFunction = mockk<SupabaseFunction>()

    coEvery {
        supabaseFunction(
            function = "v1-file-upload",
            body = capture(bodySlot),
            typeInfo = any(),
            headers = capture(headersSlot),
            requestTimeout = any(),
        )
    } returns httpResponse

    return supabaseFunction
}

internal class MultipartPart(
    val headers: Map<String, String>,
    val body: ByteArray,
)

internal fun OutgoingContent.WriteChannelContent.boundary(): String = contentType.shouldNotBeNull().parameter("boundary").shouldNotBeNull()

internal suspend fun OutgoingContent.WriteChannelContent.writtenBytes(): ByteArray {
    val channel = ByteChannel(autoFlush = true)

    writeTo(channel)
    channel.flushAndClose()

    return channel.readRemaining().readByteArray()
}

// 본문을 바이트 그대로 나눠, 파일 내용에 줄바꿈이나 대시가 섞여 있어도 경계로만 자른다.
internal fun ByteArray.parseMultipart(boundary: String): Map<String, MultipartPart> {
    val delimiter = "\r\n--$boundary".encodeToByteArray()
    val body = "\r\n".encodeToByteArray() + this
    val headerEnd = "\r\n\r\n".encodeToByteArray()
    val parts = mutableMapOf<String, MultipartPart>()
    var start = body.indexOf(delimiter, from = 0) + delimiter.size

    while (true) {
        if (body.copyOfRange(start, start + 2).decodeToString() == "--") break

        val partStart = start + 2
        val next = body.indexOf(delimiter, from = partStart)
        val headerIndex = body.indexOf(headerEnd, from = partStart)
        val headers =
            body
                .copyOfRange(partStart, headerIndex)
                .decodeToString()
                .split("\r\n")
                .associate { line -> line.substringBefore(":").trim().lowercase() to line.substringAfter(":").trim() }
        val name = headers.getValue("content-disposition").substringAfter("name=\"").substringBefore("\"")

        parts[name] = MultipartPart(headers = headers, body = body.copyOfRange(headerIndex + headerEnd.size, next))
        start = next + delimiter.size
    }

    return parts
}

private fun ByteArray.indexOf(
    target: ByteArray,
    from: Int,
): Int = (from..size - target.size).first { index -> target.indices.all { offset -> this[index + offset] == target[offset] } }

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

// 파일 내용에 경계 구분에 쓰이는 줄바꿈과 대시를 섞어, 내용이 경계로 오인되지 않는지도 함께 확인한다.
private fun contentBytes(): ByteArray =
    generateSequence { "\r\n--chunk-${fixtureMonkey.giveMeOne<String>()}" }
        .flatMap { value -> value.encodeToByteArray().asSequence() }
        .take(CONTENT_BYTES)
        .toList()
        .toByteArray()

private fun fileName(): String = "file-${fixtureMonkey.giveMeOne<Int>()}.txt"

private fun title(): String = "title-${fixtureMonkey.giveMeOne<String>()}"

private fun description(): String = fixtureMonkey.giveMeOne<String>()

private fun mimeType(): String = "application/x-${fixtureMonkey.giveMeOne<Int>().absoluteValue}"
