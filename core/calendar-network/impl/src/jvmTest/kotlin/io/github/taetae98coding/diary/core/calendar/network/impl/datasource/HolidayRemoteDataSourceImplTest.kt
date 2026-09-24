package io.github.taetae98coding.diary.core.calendar.network.impl.datasource

import io.github.taetae98coding.diary.core.calendar.network.api.datasource.HolidayRemoteDataSource
import io.github.taetae98coding.diary.core.calendar.network.api.entity.HolidayCountryRemoteEntity
import io.github.taetae98coding.diary.core.calendar.network.api.entity.HolidayRemoteEntity
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

class HolidayRemoteDataSourceImplTest :
    FunSpec({
        test("공휴일 응답 JSON을 HolidayRemoteEntity 목록으로 파싱한다") {
            val engine = createMockEngine()
            val dataSource = createDataSource(engine)

            val actual = dataSource.get(country = HolidayCountryRemoteEntity.KOREA, year = 2026)

            actual shouldHaveSize 129
            actual.first() shouldBe
                HolidayRemoteEntity(
                    name = "신정",
                    isHoliday = true,
                    start = LocalDate(2026, 1, 1),
                    endInclusive = LocalDate(2026, 1, 1),
                )
            actual.first { it.name == "소한" } shouldBe
                HolidayRemoteEntity(
                    name = "소한",
                    isHoliday = false,
                    start = LocalDate(2026, 1, 5),
                    endInclusive = LocalDate(2026, 1, 5),
                )
            actual.first { it.name == "설날" } shouldBe
                HolidayRemoteEntity(
                    name = "설날",
                    isHoliday = true,
                    start = LocalDate(2026, 2, 16),
                    endInclusive = LocalDate(2026, 2, 18),
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
            val dataSource = createDataSource(engine)

            dataSource.get(country = HolidayCountryRemoteEntity.KOREA, year = 2026) shouldBe emptyList()
        }

        test("TC-HOLIDAY-FETCH-DATA-011 요청한 국가와 연도의 공휴일을 원격에 요청한다") {
            val caseMap =
                mapOf(
                    (HolidayCountryRemoteEntity.KOREA to 2026) to "https://taetae98coding.github.io/CalendarApi/holiday/kr/2026.json",
                    (HolidayCountryRemoteEntity.UNITED_STATES to 2027) to "https://taetae98coding.github.io/CalendarApi/holiday/us/2027.json",
                )

            caseMap.forEach { (request, expectedUrl) ->
                val engine = createMockEngine()
                val dataSource = createDataSource(engine)

                dataSource.get(country = request.first, year = request.second)

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
                    content = readResource("holiday-2026-response.json"),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            }

        private fun createDataSource(engine: HttpClientEngine): HolidayRemoteDataSource =
            koinApplication<CalendarNetworkTestKoinApplication> {
                modules(
                    module {
                        single<HttpClientEngine>(qualifier = named<CalendarHttpClientEngine>()) { engine }
                    },
                )
            }.koin.get()

        private fun readResource(name: String): String = checkNotNull(HolidayRemoteDataSourceImplTest::class.java.classLoader.getResource(name)).readText()
    }
}
