package io.github.taetae98coding.diary.feature.calendar.ui.timetable

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.weather.CalendarWeather
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherReport
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherTemperature
import io.github.taetae98coding.diary.domain.weather.usecase.FetchCurrentWeatherUseCase
import io.github.taetae98coding.diary.domain.weather.usecase.GetCurrentCalendarWeatherUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.library.fixturemonkey.nonBlankString
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class CalendarTimetableWeatherViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-515ac5d8-9130-45d2-be01-b7bb30eea8da 동기화를 시작하면 현재 위치의 날씨 동기화를 한 번 요청한다") {
            runTest(mainDispatcher) {
                val useCase = successfulFetchCurrentWeatherUseCase()
                val viewModel = weatherViewModel(fetchCurrentWeatherUseCase = useCase)

                viewModel.fetch()
                advanceUntilIdle()

                coVerify(exactly = 1) { useCase(parameter = Unit) }
            }
        }

        test("TC-459cf9f3-ac83-4b94-9574-b331ec46e351 날씨 동기화가 진행 중이면 새 동기화를 시작하지 않는다") {
            runTest(mainDispatcher) {
                val completion = CompletableDeferred<Result<Unit>>()
                val useCase = mockk<FetchCurrentWeatherUseCase>()
                coEvery { useCase(parameter = Unit) } coAnswers { completion.await() }
                val viewModel = weatherViewModel(fetchCurrentWeatherUseCase = useCase)

                viewModel.fetch()
                advanceUntilIdle()
                viewModel.fetch()
                completion.complete(Result.success(Unit))
                advanceUntilIdle()

                coVerify(exactly = 1) { useCase(parameter = Unit) }
            }
        }

        test("동기화가 끝나면 다음 계기의 동기화를 다시 요청한다") {
            runTest(mainDispatcher) {
                val useCase = successfulFetchCurrentWeatherUseCase()
                val viewModel = weatherViewModel(fetchCurrentWeatherUseCase = useCase)

                viewModel.fetch()
                advanceUntilIdle()
                viewModel.fetch()
                advanceUntilIdle()

                coVerify(exactly = 2) { useCase(parameter = Unit) }
            }
        }

        test("동기화가 실패해도 다음 계기의 동기화를 다시 요청한다") {
            runTest(mainDispatcher) {
                val useCase = mockk<FetchCurrentWeatherUseCase>()
                coEvery { useCase(parameter = Unit) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                val viewModel = weatherViewModel(fetchCurrentWeatherUseCase = useCase)

                viewModel.fetch()
                advanceUntilIdle()
                viewModel.fetch()
                advanceUntilIdle()

                coVerify(exactly = 2) { useCase(parameter = Unit) }
            }
        }

        test("저장된 날짜별 날씨와 지역명을 그대로 제공한다") {
            runTest(mainDispatcher) {
                val report =
                    CalendarWeatherReport(
                        weatherList =
                            listOf(
                                CalendarWeather(
                                    date = fixtureMonkey.giveMeOne<LocalDate>(),
                                    temperature = CalendarWeatherTemperature.Current(value = fixtureMonkey.giveMeOne<Double>()),
                                    weatherList = emptyList(),
                                ),
                            ),
                        locationName = fixtureMonkey.nonBlankString(),
                    )
                val getUseCase = mockk<GetCurrentCalendarWeatherUseCase>()
                every { getUseCase(parameter = Unit) } returns flowOf(Result.success(report))
                val viewModel = weatherViewModel(getCurrentCalendarWeatherUseCase = getUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe CalendarTimetableWeatherUiState()
                    awaitItem() shouldBe CalendarTimetableWeatherUiState(weatherReport = report)
                }
            }
        }

        test("TC-67e702c6-49f8-44c2-8e6a-c5add506e4f0 날짜별 날씨 조회가 실패하면 날씨 없이 제공한다") {
            runTest(mainDispatcher) {
                val getUseCase = mockk<GetCurrentCalendarWeatherUseCase>()
                every { getUseCase(parameter = Unit) } returns flowOf(Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>())))
                val viewModel = weatherViewModel(getCurrentCalendarWeatherUseCase = getUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe CalendarTimetableWeatherUiState()
                    expectNoEvents()
                }
            }
        }
    }

    private fun weatherViewModel(
        fetchCurrentWeatherUseCase: FetchCurrentWeatherUseCase = successfulFetchCurrentWeatherUseCase(),
        getCurrentCalendarWeatherUseCase: GetCurrentCalendarWeatherUseCase =
            mockk<GetCurrentCalendarWeatherUseCase>().also { useCase ->
                every { useCase(parameter = Unit) } returns flowOf(Result.success(CalendarWeatherReport()))
            },
    ): CalendarTimetableWeatherViewModel =
        CalendarTimetableWeatherViewModel(
            fetchCurrentWeatherUseCase = fetchCurrentWeatherUseCase,
            getCurrentCalendarWeatherUseCase = getCurrentCalendarWeatherUseCase,
        )

    private fun successfulFetchCurrentWeatherUseCase(): FetchCurrentWeatherUseCase =
        mockk<FetchCurrentWeatherUseCase>().also { useCase ->
            coEvery { useCase(parameter = Unit) } returns Result.success(Unit)
        }
}
