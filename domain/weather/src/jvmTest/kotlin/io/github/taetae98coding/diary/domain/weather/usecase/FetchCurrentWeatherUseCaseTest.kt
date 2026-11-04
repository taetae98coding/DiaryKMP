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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class FetchCurrentWeatherUseCaseTest :
    BehaviorSpec({
        Given("TC-89671f9e-6427-4dfe-b210-2388f68f48e6 현재 위치의 날씨 동기화가 성공하도록 준비되어 있다") {
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

        Given("TC-47671c52-4594-48cc-8861-e46521d110cb 현재 위치의 날씨 동기화가 실패하도록 준비되어 있다") {
            val failure = FetchCurrentWeatherTestException(Arb.string().next())
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
            val failure = FetchCurrentWeatherTestException(Arb.string().next())
            val repository = mockk<WeatherRepository>()
            coEvery { repository.fetch() } throws failure
            val useCase = FetchCurrentWeatherUseCase(weatherRepository = repository)

            val reportList = recordCrashlyticsLog()

            When("현재 날씨 동기화를 요청한다") {
                val result = useCase(parameter = Unit)

                Then("TC-11ea5e3e-4cc3-4063-8d4d-8b38b78a0d01 실패로 끝나고 오류 보고는 남지 않는다") {
                    result.shouldBeFailure() shouldBeSameInstanceAs failure
                    reportList.shouldBeEmpty()
                }
            }
        }

        Given("오류 보고 기록 수단이 등록되어 있고 현재 날씨 동기화가 끝나지 않도록 준비되어 있다") {
            val onStart = CompletableDeferred<Unit>()
            val repository = mockk<WeatherRepository>()
            coEvery { repository.fetch() } coAnswers {
                onStart.complete(Unit)
                awaitCancellation()
            }
            val useCase = FetchCurrentWeatherUseCase(weatherRepository = repository)

            val reportList = recordCrashlyticsLog()

            When("위치 권한을 새로 허용해 진행 중이던 동기화가 멈춘다") {
                coroutineScope {
                    val job = launch { useCase(parameter = Unit) }

                    onStart.await()
                    job.cancelAndJoin()
                }

                Then("TC-4c46a618-d3d2-43be-9b55-2c5eec821f89 멈춘 동기화에 대한 오류 보고가 남지 않는다") {
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
