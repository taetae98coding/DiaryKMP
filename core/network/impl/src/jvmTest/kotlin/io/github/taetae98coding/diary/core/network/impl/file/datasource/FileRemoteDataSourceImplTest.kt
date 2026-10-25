package io.github.taetae98coding.diary.core.network.impl.file.datasource

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.network.api.file.entity.ContinuedFileUploadRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.ContinuedFileUploadResultRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.FileCursorRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.FileRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.exception.FileNotFoundRemoteException
import io.github.taetae98coding.diary.core.network.impl.file.entity.FileDownloadResponseRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.file.entity.FileIdRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.file.entity.FileListRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.file.entity.FileListResponseRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.file.entity.FileUpdateRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.file.transport.FileUploadTransport
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunctionException
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowAny
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
import kotlinx.io.readByteArray
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class FileRemoteDataSourceImplTest :
    FunSpec({
        test("고른 파일의 이름, 제목, 설명, 형식, 크기, 시작한 계정, 내용과 보낸 양을 플랫폼의 전송 수단에 그대로 넘기고 그 결과를 돌려준다") {
            val name = fixtureMonkey.giveMeOne<String>()
            val title = fixtureMonkey.giveMeOne<String>()
            val description = fixtureMonkey.giveMeOne<String>()
            val mimeType = fixtureMonkey.giveMeOne<String>()
            val contentLength = fixtureMonkey.giveMeOne<Long>()
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val response = fixtureMonkey.giveMeOne<FileRemoteEntity>()
            val source = Buffer()
            val openContent: suspend () -> RawSource = { source }
            val onSent: (Long) -> Unit = {}
            val transport = mockk<FileUploadTransport>()
            coEvery {
                transport.upload(name = name, title = title, description = description, mimeType = mimeType, contentLength = contentLength, accountId = accountId, openContent = openContent, onSent = onSent)
            } returns response
            val dataSource = FileRemoteDataSourceImpl(supabaseFunction = mockk(), fileUploadTransport = transport, httpClient = mockk())

            val actual =
                dataSource.upload(
                    name = name,
                    title = title,
                    description = description,
                    mimeType = mimeType,
                    contentLength = contentLength,
                    accountId = accountId,
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
            val dataSource = FileRemoteDataSourceImpl(supabaseFunction = mockk(), fileUploadTransport = transport, httpClient = mockk())

            dataSource.getContinuedUpload().test {
                awaitItem() shouldBe upload
                awaitComplete()
            }
            dataSource.getContinuedUploadResult().test {
                awaitItem() shouldBe result
                awaitComplete()
            }
            val exceptAccountId = fixtureMonkey.giveMeOne<Uuid>()
            dataSource.cancelContinuedUpload(exceptAccountId = exceptAccountId)

            coVerify(exactly = 1) { transport.cancelContinuedUpload(exceptAccountId = exceptAccountId) }
        }

        test("TC-c3c9a856-e7b9-41c7-b35b-025cbe78c827 처음 불러올 때 마지막 파일 없이 가져올 개수를 보내고 돌려받은 목록을 읽는다") {
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
            val dataSource = FileRemoteDataSourceImpl(supabaseFunction = supabaseFunction, fileUploadTransport = mockk(), httpClient = mockk())

            val actual = dataSource.readList(cursor = null, size = size)

            requestSlot.captured shouldBe FileListRequestRemoteEntity(cursor = null, size = size)
            actual shouldBe fileList
        }

        test("TC-e83464f1-ff45-45d4-b832-57f4b4f82143 이어서 불러올 때 마지막 파일의 올린 시각과 식별자를 보낸다") {
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
            val dataSource = FileRemoteDataSourceImpl(supabaseFunction = supabaseFunction, fileUploadTransport = mockk(), httpClient = mockk())

            dataSource.readList(cursor = cursor, size = 20)

            requestSlot.captured shouldBe FileListRequestRemoteEntity(cursor = cursor, size = 20)
        }

        test("목록 요청은 서버 함수가 받는 필드 이름으로 직렬화된다") {
            val cursor = fixtureMonkey.giveMeOne<FileCursorRemoteEntity>()

            val json = Json.encodeToString(FileListRequestRemoteEntity(cursor = cursor, size = 20))

            json shouldBe """{"cursor":{"createdAt":"${cursor.createdAt}","id":"${cursor.id}"},"size":20}"""
        }
        test("TC-2209b4c9-9d1b-41ca-a863-6c755564b60c 파일 하나의 정보를 식별자로 요청하고, 찾을 수 없으면 찾을 수 없음으로 알린다") {
            val id = fixtureMonkey.giveMeOne<Uuid>()
            val file = fixtureMonkey.giveMeOne<FileRemoteEntity>()
            val requestSlot = slot<FileIdRequestRemoteEntity>()
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery {
                supabaseFunction(function = "v1-file-get", body = capture(requestSlot), typeInfo = any(), headers = any(), requestTimeout = any())
            } returns httpResponse(body = Json.encodeToString(file))
            val dataSource = FileRemoteDataSourceImpl(supabaseFunction = supabaseFunction, fileUploadTransport = mockk(), httpClient = mockk())

            dataSource.read(id = id) shouldBe file
            requestSlot.captured shouldBe FileIdRequestRemoteEntity(id = id)

            coEvery {
                supabaseFunction(function = "v1-file-get", body = any<FileIdRequestRemoteEntity>(), typeInfo = any(), headers = any(), requestTimeout = any())
            } throws SupabaseFunctionException(statusCode = 404)
            shouldThrow<FileNotFoundRemoteException> { dataSource.read(id = id) }
        }

        test("TC-cd6be59f-06d7-4587-ab0a-6668ae6dd1f4 정보 고치기는 식별자와 이름, 제목, 설명을 함께 보낸다") {
            val request = fixtureMonkey.giveMeOne<FileUpdateRequestRemoteEntity>()
            val file = fixtureMonkey.giveMeOne<FileRemoteEntity>()
            val requestSlot = slot<FileUpdateRequestRemoteEntity>()
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery {
                supabaseFunction(function = "v1-file-update", body = capture(requestSlot), typeInfo = any(), headers = any(), requestTimeout = any())
            } returns httpResponse(body = Json.encodeToString(file))
            val dataSource = FileRemoteDataSourceImpl(supabaseFunction = supabaseFunction, fileUploadTransport = mockk(), httpClient = mockk())

            dataSource.update(id = request.id, name = request.name, title = request.title, description = request.description) shouldBe file
            requestSlot.captured shouldBe request
            Json.encodeToString(request) shouldBe """{"id":"${request.id}","name":${Json.encodeToString(request.name)},"title":${Json.encodeToString(request.title)},"description":${Json.encodeToString(request.description)}}"""
        }

        test("TC-c8dace87-717c-4c9c-a6af-7c9edef184b1 서버가 내준 임시 주소에서 받은 내용을 그대로 쓴다") {
            val id = fixtureMonkey.giveMeOne<Uuid>()
            val content = fixtureMonkey.giveMeOne<ByteArray>()
            val url = "https://storage.example/${fixtureMonkey.giveMeOne<Int>()}"
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery {
                supabaseFunction(function = "v1-file-download", body = FileIdRequestRemoteEntity(id = id), typeInfo = any(), headers = any(), requestTimeout = any())
            } returns httpResponse(body = Json.encodeToString(FileDownloadResponseRemoteEntity(url = url)))
            val httpClient = HttpClient(MockEngine { request -> if (request.url.toString() == url) respond(content = content) else respond(content = "", status = HttpStatusCode.NotFound) })
            val sink = Buffer()

            FileRemoteDataSourceImpl(supabaseFunction = supabaseFunction, fileUploadTransport = mockk(), httpClient = httpClient).download(id = id, sink = sink)

            sink.readByteArray() shouldBe content
        }

        test("TC-eff813ca-20ff-4336-a492-fa49b3a3374e 임시 주소가 실패로 응답하거나 받은 내용이 알린 크기보다 짧으면 실패로 끝난다") {
            listOf(
                MockEngine { respond(content = "", status = HttpStatusCode.Forbidden) },
                MockEngine { respond(content = byteArrayOf(1, 2), headers = headersOf(HttpHeaders.ContentLength, "5")) },
            ).forEach { engine ->
                val supabaseFunction = mockk<SupabaseFunction>()
                coEvery {
                    supabaseFunction(function = "v1-file-download", body = any<FileIdRequestRemoteEntity>(), typeInfo = any(), headers = any(), requestTimeout = any())
                } returns httpResponse(body = Json.encodeToString(FileDownloadResponseRemoteEntity(url = "https://storage.example/file")))

                shouldThrowAny {
                    FileRemoteDataSourceImpl(supabaseFunction = supabaseFunction, fileUploadTransport = mockk(), httpClient = HttpClient(engine))
                        .download(id = fixtureMonkey.giveMeOne<Uuid>(), sink = Buffer())
                }
            }
        }

        test("내려받기 주소를 찾을 수 없으면 찾을 수 없음으로 알린다") {
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery {
                supabaseFunction(function = "v1-file-download", body = any<FileIdRequestRemoteEntity>(), typeInfo = any(), headers = any(), requestTimeout = any())
            } throws SupabaseFunctionException(statusCode = 404)

            shouldThrow<FileNotFoundRemoteException> {
                FileRemoteDataSourceImpl(supabaseFunction = supabaseFunction, fileUploadTransport = mockk(), httpClient = mockk()).download(id = fixtureMonkey.giveMeOne<Uuid>(), sink = Buffer())
            }
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
