package io.github.taetae98coding.diary.core.webnetwork.impl.datasource

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.webnetwork.api.datasource.WebPageRemoteDataSource
import io.github.taetae98coding.diary.core.webnetwork.api.entity.WebPageHeaderRemoteEntity
import io.github.taetae98coding.diary.core.webnetwork.impl.WebNetworkTestKoinApplication
import io.github.taetae98coding.diary.core.webnetwork.impl.di.WebPageHttpClientEngine
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.plugin.module.dsl.koinApplication

class WebPageRemoteDataSourceImplTest :
    FunSpec({
        test("TC-WEB-DETAIL-DATA-004 저장된 URL로 요청하고 응답 본문을 그대로 전달한다") {
            val body = "<html><body>${fixtureMonkey.giveMeOne<String>()}</body></html>"
            val engine = createMockEngine(body = body)
            val dataSource = createDataSource(engine)

            val actual = dataSource.get(url = URL, headerList = emptyList())

            engine.requestHistory
                .single()
                .url
                .toString() shouldBe URL
            actual.url shouldBe URL
            actual.body shouldBe body
        }

        test("TC-WEB-DETAIL-DOMAIN-008 TC-WEB-DETAIL-DATA-004 저장된 요청 헤더를 순서대로 모두 요청에 담는다") {
            val headerList =
                listOf(
                    WebPageHeaderRemoteEntity(name = "X-Diary", value = "first"),
                    WebPageHeaderRemoteEntity(name = "X-Diary", value = "second"),
                    WebPageHeaderRemoteEntity(name = "Accept-Language", value = "ko-KR"),
                )
            val engine = createMockEngine()
            val dataSource = createDataSource(engine)

            dataSource.get(url = URL, headerList = headerList)

            val requestHeaders = engine.requestHistory.single().headers
            requestHeaders.getAll("X-Diary").shouldContainExactly("first", "second")
            requestHeaders.getAll("Accept-Language").shouldContainExactly("ko-KR")
        }

        test("TC-WEB-DETAIL-DATA-005 저장된 요청 헤더 외에 앱의 인증 정보를 요청에 담지 않는다") {
            val engine = createMockEngine()
            val dataSource = createDataSource(engine)

            dataSource.get(url = URL, headerList = listOf(WebPageHeaderRemoteEntity(name = "X-Diary", value = "value")))

            val requestHeaders = engine.requestHistory.single().headers
            requestHeaders[HttpHeaders.Authorization].shouldBeNull()
            requestHeaders[HttpHeaders.Cookie].shouldBeNull()
        }

        test("TC-WEB-DETAIL-DOMAIN-009 2xx가 아닌 응답도 실패로 다루지 않고 본문을 전달한다") {
            listOf(HttpStatusCode.NotFound, HttpStatusCode.InternalServerError).forEach { status ->
                val body = "<html><body>${status.value}</body></html>"
                val dataSource = createDataSource(createMockEngine(body = body, status = status))

                dataSource.get(url = URL, headerList = emptyList()).body shouldBe body
            }
        }

        test("TC-WEB-DETAIL-DOMAIN-049 다른 주소로 옮겨 가라는 응답을 따라가 마지막 응답과 그 주소를 결과로 삼는다") {
            val body = "<html><body>${fixtureMonkey.giveMeOne<String>()}</body></html>"
            val engine =
                MockEngine { request ->
                    if (request.url.toString() == URL) {
                        respond(content = "", status = HttpStatusCode.Found, headers = headersOf(HttpHeaders.Location, REDIRECTED_URL))
                    } else {
                        respond(
                            content = body,
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, ContentType.Text.Html.toString()),
                        )
                    }
                }
            val dataSource = createDataSource(engine)

            val actual = dataSource.get(url = URL, headerList = emptyList())

            engine.requestHistory.map { request -> request.url.toString() } shouldBe listOf(URL, REDIRECTED_URL)
            actual.url shouldBe REDIRECTED_URL
            actual.body shouldBe body
        }
    }) {
    public companion object {
        private const val URL = "https://developer.android.com/"
        private const val REDIRECTED_URL = "https://developer.android.com/redirected/"

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun createMockEngine(
            body: String = "<html></html>",
            status: HttpStatusCode = HttpStatusCode.OK,
        ): MockEngine =
            MockEngine {
                respond(
                    content = body,
                    status = status,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Text.Html.toString()),
                )
            }

        private fun createDataSource(engine: HttpClientEngine): WebPageRemoteDataSource =
            koinApplication<WebNetworkTestKoinApplication> {
                modules(
                    module {
                        single<HttpClientEngine>(qualifier = named<WebPageHttpClientEngine>()) { engine }
                    },
                )
            }.koin.get()
    }
}
