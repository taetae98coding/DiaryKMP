package io.github.taetae98coding.diary.core.youtubenetwork.impl.datasource

import io.github.taetae98coding.diary.core.youtubenetwork.api.datasource.YoutubeVideoRemoteDataSource
import io.github.taetae98coding.diary.core.youtubenetwork.api.entity.YoutubeVideoRemoteEntity
import io.github.taetae98coding.diary.core.youtubenetwork.impl.YoutubeNetworkTestKoinApplication
import io.github.taetae98coding.diary.core.youtubenetwork.impl.di.YoutubeHttpClientEngine
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.plugin.module.dsl.koinApplication

private const val YOUTUBE_LINK: String = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"

class YoutubeVideoRemoteDataSourceImplTest :
    FunSpec({
        test("TC-MUSIC-ADD-DATA-008 oEmbed 응답의 영상 제목과 채널 이름을 그대로 제공한다") {
            val dataSource = createDataSource(createSuccessEngine())

            val actual = dataSource.fetch(link = YOUTUBE_LINK)

            actual shouldBe
                YoutubeVideoRemoteEntity(
                    title = "NewJeans (뉴진스) 'Super Shy' Official MV",
                    authorName = "HYBE LABELS",
                )
        }

        test("TC-MUSIC-ADD-DATA-008 입력한 링크를 담은 oEmbed URL로 요청한다") {
            val engine = createSuccessEngine()
            val dataSource = createDataSource(engine)

            dataSource.fetch(link = YOUTUBE_LINK)

            engine.requestHistory
                .single()
                .url
                .toString() shouldBe "https://www.youtube.com/oembed?url=https%3A%2F%2Fwww.youtube.com%2Fwatch%3Fv%3DdQw4w9WgXcQ&format=json"
        }

        listOf(
            HttpStatusCode.NotFound,
            HttpStatusCode.Unauthorized,
            HttpStatusCode.InternalServerError,
        ).forEach { status ->
            test("TC-MUSIC-ADD-DATA-009 조회할 수 없는 응답은 실패로 전달한다: $status") {
                val dataSource = createDataSource(MockEngine { respondError(status) })

                shouldThrowAny { dataSource.fetch(link = YOUTUBE_LINK) }
            }
        }

        test("TC-MUSIC-ADD-DATA-009 네트워크를 사용할 수 없으면 실패로 전달한다") {
            val dataSource = createDataSource(MockEngine { throw java.io.IOException("network unavailable") })

            shouldThrowAny { dataSource.fetch(link = YOUTUBE_LINK) }
        }
    }) {
    public companion object {
        private fun createSuccessEngine(): MockEngine =
            MockEngine {
                respond(
                    content = readResource("youtube-oembed-response.json"),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            }

        private fun createDataSource(engine: HttpClientEngine): YoutubeVideoRemoteDataSource =
            koinApplication<YoutubeNetworkTestKoinApplication> {
                modules(
                    module {
                        single<HttpClientEngine>(qualifier = named<YoutubeHttpClientEngine>()) { engine }
                    },
                )
            }.koin.get()

        private fun readResource(name: String): String = checkNotNull(YoutubeVideoRemoteDataSourceImplTest::class.java.classLoader.getResource(name)).readText()
    }
}
