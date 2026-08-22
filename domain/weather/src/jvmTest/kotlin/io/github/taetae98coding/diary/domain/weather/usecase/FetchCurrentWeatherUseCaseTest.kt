package io.github.taetae98coding.diary.domain.weather.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.domain.weather.repository.WeatherRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.logger.core.DiaryLog
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.logger.core.DiaryLoggerDelegate
import io.github.taetae98coding.diary.logger.crashlytics.api.CrashlyticsLog
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class FetchCurrentWeatherUseCaseTest :
    BehaviorSpec({
        Given("TC-WEATHER-FETCH-DATA-008 현재 위치의 날씨 동기화가 성공하도록 준비되어 있다") {
            val repository = mockk<WeatherRepository>()
            coEvery { repository.fetch() } just Runs
            val useCase = FetchCurrentWeatherUseCase(weatherRepository = repository)

            When("현재 날씨 동기화를 요청한다") {
                val result = useCase(parameter = Unit)

                Then("성공 결과를 반환한다") {
                    result.shouldBeSuccess()
                    coVerify(exactly = 1) { repository.fetch() }
                }
            }
        }

        Given("TC-WEATHER-FETCH-DATA-013 현재 위치의 날씨 동기화가 실패하도록 준비되어 있다") {
            val failure = FetchCurrentWeatherTestException(fixtureMonkey.giveMeOne())
            val repository = mockk<WeatherRepository>()
            coEvery { repository.fetch() } throws failure
            val useCase = FetchCurrentWeatherUseCase(weatherRepository = repository)

            When("현재 날씨 동기화를 요청한다") {
                val result = useCase(parameter = Unit)

                Then("실패 원인을 그대로 반환한다") {
                    result.shouldBeFailure() shouldBeSameInstanceAs failure
                }
            }
        }

        Given("오류 보고 기록 수단이 등록되어 있고 현재 날씨 동기화가 특정 원인으로 실패하도록 준비되어 있다") {
            val failure = FetchCurrentWeatherTestException(fixtureMonkey.giveMeOne())
            val repository = mockk<WeatherRepository>()
            coEvery { repository.fetch() } throws failure
            val useCase = FetchCurrentWeatherUseCase(weatherRepository = repository)

            val reportList = recordCrashlyticsLog()

            When("현재 날씨 동기화를 요청한다") {
                useCase(parameter = Unit)

                Then("TC-WEATHER-FETCH-DOMAIN-016 그 원인을 담은 오류 보고가 한 번 남는다") {
                    val report = reportList.single()

                    report.throwable shouldBeSameInstanceAs failure
                    report.message shouldContain "FetchCurrentWeatherUseCase"
                }
            }
        }

        Given("오류 보고 기록 수단이 등록되어 있고 현재 날씨 동기화가 성공하도록 준비되어 있다") {
            val repository = mockk<WeatherRepository>()
            coEvery { repository.fetch() } just Runs
            val useCase = FetchCurrentWeatherUseCase(weatherRepository = repository)

            val reportList = recordCrashlyticsLog()

            When("현재 날씨 동기화를 요청한다") {
                useCase(parameter = Unit)

                Then("TC-WEATHER-FETCH-DOMAIN-017 오류 보고가 남지 않는다") {
                    reportList.shouldBeEmpty()
                }
            }
        }
    })

private class FetchCurrentWeatherTestException(
    message: String,
) : RuntimeException(message)

private fun recordCrashlyticsLog(): List<CrashlyticsLog> {
    val reportList = mutableListOf<CrashlyticsLog>()
    val delegate = mockk<DiaryLoggerDelegate>()

    every { delegate.log(log = any()) } answers {
        val log = firstArg<DiaryLog>()
        if (log is CrashlyticsLog) {
            reportList += log
        }
    }

    DiaryLogger.add(delegate = delegate)

    return reportList
}
