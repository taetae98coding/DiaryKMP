package io.github.taetae98coding.diary.core.calendar.network.impl.datasource

import io.github.taetae98coding.diary.core.calendar.network.api.datasource.LunarRemoteDataSource
import io.github.taetae98coding.diary.core.calendar.network.api.entity.LunarDateRemoteEntity
import io.github.taetae98coding.diary.core.calendar.network.impl.CalendarNetworkTestKoinApplication
import io.github.taetae98coding.diary.core.calendar.network.impl.di.CalendarHttpClientEngine
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.datetime.LocalDate
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.plugin.module.dsl.koinApplication

class LunarRemoteDataSourceImplTest :
    FunSpec({
        test("TC-LUNAR-FETCH-DATA-010 원격 응답을 양력 날짜별 음력 날짜로 읽는다") {
            val dataSource = createDataSource(createMockEngine())

            val actual = dataSource.get(year = 2026)

            actual shouldHaveSize 365
            actual.first() shouldBe
                LunarDateRemoteEntity(
                    solar = LocalDate(2026, 1, 1),
                    year = 2025,
                    month = 11,
                    day = 13,
                    isLeapMonth = false,
                )
            actual.first { lunarDate -> lunarDate.solar == LocalDate(2026, 2, 17) } shouldBe
                LunarDateRemoteEntity(
                    solar = LocalDate(2026, 2, 17),
                    year = 2026,
                    month = 1,
                    day = 1,
                    isLeapMonth = false,
                )
        }

        test("해당 연도 자료가 없으면 빈 목록을 제공한다") {
            val engine =
                MockEngine {
                    respond(
                        content = "",
                        status = HttpStatusCode.NotFound,
                    )
                }

            createDataSource(engine).get(year = 1997) shouldBe emptyList()
        }

        test("TC-LUNAR-FETCH-DATA-009 요청한 연도의 한국 음력 자료를 원격에 요청한다") {
            val caseMap =
                mapOf(
                    2026 to "https://taetae98coding.github.io/CalendarApi/lunar/kr/2026.json",
                    2027 to "https://taetae98coding.github.io/CalendarApi/lunar/kr/2027.json",
                )

            caseMap.forEach { (year, expectedUrl) ->
                val engine = createMockEngine()
                val dataSource = createDataSource(engine)

                dataSource.get(year = year)

                engine.requestHistory
                    .single()
                    .url
                    .toString() shouldBe expectedUrl
            }
        }
    }) {
    public companion object {
        private fun createMockEngine(): MockEngine =
            MockEngine {
                respond(
                    content = readResource("lunar-2026-response.json"),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            }

        private fun createDataSource(engine: HttpClientEngine): LunarRemoteDataSource =
            koinApplication<CalendarNetworkTestKoinApplication> {
                modules(
                    module {
                        single<HttpClientEngine>(qualifier = named<CalendarHttpClientEngine>()) { engine }
                    },
                )
            }.koin.get()

        private fun readResource(name: String): String = checkNotNull(LunarRemoteDataSourceImplTest::class.java.classLoader.getResource(name)).readText()
    }
}
