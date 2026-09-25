package io.github.taetae98coding.diary.core.network.impl.file.datasource

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.network.api.file.entity.FileCursorRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.FileRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.exception.FileTooLargeRemoteException
import io.github.taetae98coding.diary.core.network.impl.file.entity.FileListRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.file.entity.FileListResponseRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunctionException
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
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
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Duration

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class FileRemoteDataSourceImplTest :
    FunSpec({
        test("TC-FILE-STORAGE-DATA-005 고른 파일의 내용을 그대로 흘려보내고 서버가 돌려준 파일 정보를 읽는다") {
            val bytes = "file-${fixtureMonkey.giveMeOne<String>()}".encodeToByteArray()
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
            val dataSource = FileRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            val actual =
                dataSource.upload(
                    name = "memo.txt",
                    mimeType = "text/plain",
                    contentLength = bytes.size.toLong(),
                    openContent = { Buffer().apply { write(bytes) } },
                )

            val content = bodySlot.captured.shouldBeInstanceOf<OutgoingContent.WriteChannelContent>()
            content.contentType shouldBe ContentType.Text.Plain
            content.contentLength shouldBe bytes.size.toLong()
            content.writtenBytes() shouldBe bytes
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
            val dataSource = FileRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            dataSource.upload(
                name = name,
                mimeType = "application/pdf",
                contentLength = 0,
                openContent = { Buffer() },
            )

            val encodedName = headersSlot.captured["X-File-Name"].shouldNotBeNull()
            encodedName.all { char -> char.code < 128 } shouldBe true
            encodedName.decodeURLQueryComponent() shouldBe name
            bodySlot.captured.shouldBeInstanceOf<OutgoingContent.WriteChannelContent>().contentType shouldBe ContentType.Application.Pdf
        }

        test("올리기 요청에는 기본보다 긴 요청 시간 제한을 둔다") {
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
            val dataSource = FileRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            dataSource.upload(
                name = "memo.txt",
                mimeType = "text/plain",
                contentLength = 0,
                openContent = { Buffer() },
            )

            (requestTimeout.shouldNotBeNull() > Duration.ZERO) shouldBe true
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
            val dataSource = FileRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            shouldThrow<FileTooLargeRemoteException> {
                dataSource.upload(name = "memo.txt", mimeType = "text/plain", contentLength = 0, openContent = { Buffer() })
            }
        }

        test("TC-FILE-STORAGE-DATA-006 크기 초과가 아닌 서버 실패는 그대로 전달한다") {
            val exception = SupabaseFunctionException(statusCode = HttpStatusCode.InternalServerError.value)
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
            val dataSource = FileRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            shouldThrow<SupabaseFunctionException> {
                dataSource.upload(name = "memo.txt", mimeType = "text/plain", contentLength = 0, openContent = { Buffer() })
            } shouldBeSameInstanceAs exception
        }

        test("TC-FILE-STORAGE-DATA-001 처음 불러올 때 마지막 파일 없이 가져올 개수를 보내고 돌려받은 목록을 읽는다") {
            val size = fixtureMonkey.giveMeOne<Int>()
            val fileList = List(2) { fixtureMonkey.giveMeOne<FileRemoteEntity>() }
            val requestSlot = slot<FileListRequestRemoteEntity>()
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery {
                supabaseFunction(
                    function = "v1-file-list",
                    body = capture(requestSlot),
                    typeInfo = any(),
                    headers = any(),
                    requestTimeout = any(),
                )
            } returns httpResponse(body = Json.encodeToString(FileListResponseRemoteEntity(fileList = fileList)))
            val dataSource = FileRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            val actual = dataSource.fetch(cursor = null, size = size)

            requestSlot.captured shouldBe FileListRequestRemoteEntity(cursor = null, size = size)
            actual shouldBe fileList
        }

        test("TC-FILE-STORAGE-DATA-002 이어서 불러올 때 마지막 파일의 올린 시각과 식별자를 보낸다") {
            val cursor = fixtureMonkey.giveMeOne<FileCursorRemoteEntity>()
            val requestSlot = slot<FileListRequestRemoteEntity>()
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery {
                supabaseFunction(
                    function = "v1-file-list",
                    body = capture(requestSlot),
                    typeInfo = any(),
                    headers = any(),
                    requestTimeout = any(),
                )
            } returns httpResponse(body = Json.encodeToString(FileListResponseRemoteEntity(fileList = emptyList())))
            val dataSource = FileRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            dataSource.fetch(cursor = cursor, size = 20)

            requestSlot.captured shouldBe FileListRequestRemoteEntity(cursor = cursor, size = 20)
        }

        test("목록 요청은 서버 함수가 받는 필드 이름으로 직렬화된다") {
            val cursor = fixtureMonkey.giveMeOne<FileCursorRemoteEntity>()

            val json = Json.encodeToString(FileListRequestRemoteEntity(cursor = cursor, size = 20))

            json shouldBe """{"cursor":{"createdAt":"${cursor.createdAt}","id":"${cursor.id}"},"size":20}"""
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
