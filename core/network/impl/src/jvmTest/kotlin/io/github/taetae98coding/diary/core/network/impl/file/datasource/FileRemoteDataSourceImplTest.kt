package io.github.taetae98coding.diary.core.network.impl.file.datasource

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.network.api.file.entity.ContinuedFileUploadRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.ContinuedFileUploadResultRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.FileCursorRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.FileRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.file.entity.FileListRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.file.entity.FileListResponseRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.file.transport.FileUploadTransport
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.io.Buffer
import kotlinx.io.RawSource
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class FileRemoteDataSourceImplTest :
    FunSpec({
        test("고른 파일의 이름, 형식, 크기, 내용과 보낸 양을 플랫폼의 전송 수단에 그대로 넘기고 그 결과를 돌려준다") {
            val name = fixtureMonkey.giveMeOne<String>()
            val mimeType = fixtureMonkey.giveMeOne<String>()
            val contentLength = fixtureMonkey.giveMeOne<Long>()
            val response = fixtureMonkey.giveMeOne<FileRemoteEntity>()
            val source = Buffer()
            val openContent: suspend () -> RawSource = { source }
            val onSent: (Long) -> Unit = {}
            val transport = mockk<FileUploadTransport>()
            coEvery {
                transport.upload(name = name, mimeType = mimeType, contentLength = contentLength, openContent = openContent, onSent = onSent)
            } returns response
            val dataSource = FileRemoteDataSourceImpl(supabaseFunction = mockk(), fileUploadTransport = transport)

            val actual =
                dataSource.upload(
                    name = name,
                    mimeType = mimeType,
                    contentLength = contentLength,
                    openContent = openContent,
                    onSent = onSent,
                )

            actual shouldBe response
        }

        test("앞선 실행에서 이어지는 올리기와 그 결과, 취소를 플랫폼의 전송 수단에 맡긴다") {
            val upload = fixtureMonkey.giveMeOne<ContinuedFileUploadRemoteEntity>()
            val result = ContinuedFileUploadResultRemoteEntity.Failed(name = fixtureMonkey.giveMeOne<String>())
            val transport = mockk<FileUploadTransport>(relaxUnitFun = true)
            every { transport.getContinuedUpload() } returns flowOf(upload)
            every { transport.getContinuedUploadResult() } returns flowOf(result)
            val dataSource = FileRemoteDataSourceImpl(supabaseFunction = mockk(), fileUploadTransport = transport)

            dataSource.getContinuedUpload().test {
                awaitItem() shouldBe upload
                awaitComplete()
            }
            dataSource.getContinuedUploadResult().test {
                awaitItem() shouldBe result
                awaitComplete()
            }
            dataSource.cancelContinuedUpload()

            coVerify(exactly = 1) { transport.cancelContinuedUpload() }
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
            val dataSource = FileRemoteDataSourceImpl(supabaseFunction = supabaseFunction, fileUploadTransport = mockk())

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
            val dataSource = FileRemoteDataSourceImpl(supabaseFunction = supabaseFunction, fileUploadTransport = mockk())

            dataSource.fetch(cursor = cursor, size = 20)

            requestSlot.captured shouldBe FileListRequestRemoteEntity(cursor = cursor, size = 20)
        }

        test("목록 요청은 서버 함수가 받는 필드 이름으로 직렬화된다") {
            val cursor = fixtureMonkey.giveMeOne<FileCursorRemoteEntity>()

            val json = Json.encodeToString(FileListRequestRemoteEntity(cursor = cursor, size = 20))

            json shouldBe """{"cursor":{"createdAt":"${cursor.createdAt}","id":"${cursor.id}"},"size":20}"""
        }
    })

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
