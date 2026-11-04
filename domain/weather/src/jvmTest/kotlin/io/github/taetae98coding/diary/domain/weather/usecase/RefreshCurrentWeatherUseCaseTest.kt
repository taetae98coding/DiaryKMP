package io.github.taetae98coding.diary.domain.weather.usecase

import io.github.taetae98coding.diary.domain.weather.repository.WeatherRepository
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
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk

class RefreshCurrentWeatherUseCaseTest :
    BehaviorSpec({
        Given("TC-44f121e6-fb34-456f-b5c3-df7604d2fb20 현재 위치의 날씨 동기화가 성공하도록 준비되어 있다") {
            val repository = mockk<WeatherRepository>()
            coEvery { repository.refresh() } just Runs
            val useCase = RefreshCurrentWeatherUseCase(weatherRepository = repository)

            When("위치 권한 허용을 계기로 현재 날씨 동기화를 요청한다") {
                val result = useCase(parameter = Unit)

                Then("동기화 간격을 적용하지 않고 조회를 요청한다") {
                    result.shouldBeSuccess()
                    coVerify(exactly = 1) { repository.refresh() }
                    coVerify(exactly = 0) { repository.fetch() }
                }
            }
        }

        Given("현재 위치의 날씨 동기화가 실패하도록 준비되어 있다") {
            val failure = RefreshCurrentWeatherTestException(Arb.string().next())
            val repository = mockk<WeatherRepository>()
            coEvery { repository.refresh() } throws failure
            val useCase = RefreshCurrentWeatherUseCase(weatherRepository = repository)

            When("위치 권한 허용을 계기로 현재 날씨 동기화를 요청한다") {
                val result = useCase(parameter = Unit)

                Then("실패 원인을 그대로 반환한다") {
                    result.shouldBeFailure() shouldBeSameInstanceAs failure
                }
            }
        }

        Given("오류 보고 기록 수단이 등록되어 있고 현재 날씨 동기화가 특정 원인으로 실패하도록 준비되어 있다") {
            val failure = RefreshCurrentWeatherTestException(Arb.string().next())
            val repository = mockk<WeatherRepository>()
            coEvery { repository.refresh() } throws failure
            val useCase = RefreshCurrentWeatherUseCase(weatherRepository = repository)

            val reportList = recordCrashlyticsLog()

            When("위치 권한 허용을 계기로 현재 날씨 동기화를 요청한다") {
                val result = useCase(parameter = Unit)

                Then("TC-11ea5e3e-4cc3-4063-8d4d-8b38b78a0d01 실패로 끝나고 오류 보고는 남지 않는다") {
                    result.shouldBeFailure() shouldBeSameInstanceAs failure
                    reportList.shouldBeEmpty()
                }
            }
        }
    })

private class RefreshCurrentWeatherTestException(
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
