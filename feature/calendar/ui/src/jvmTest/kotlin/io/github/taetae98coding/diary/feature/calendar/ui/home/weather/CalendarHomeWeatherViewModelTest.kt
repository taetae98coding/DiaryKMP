package io.github.taetae98coding.diary.feature.calendar.ui.home.weather

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.weather.CalendarWeather
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherReport
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherTemperature
import io.github.taetae98coding.diary.domain.weather.usecase.FetchCurrentWeatherUseCase
import io.github.taetae98coding.diary.domain.weather.usecase.GetCurrentCalendarWeatherUseCase
import io.github.taetae98coding.diary.domain.weather.usecase.RefreshCurrentWeatherUseCase
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

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class CalendarHomeWeatherViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-CALENDAR-HOME-DATA-020 동기화를 시작하면 현재 위치의 날씨 동기화를 한 번 요청한다") {
            runTest(mainDispatcher) {
                val useCase = successfulFetchCurrentWeatherUseCase()
                val viewModel = weatherViewModel(fetchCurrentWeatherUseCase = useCase)

                viewModel.fetch()
                advanceUntilIdle()

                coVerify(exactly = 1) { useCase(parameter = Unit) }
            }
        }

        test("TC-CALENDAR-HOME-DATA-021 TC-SYNC-REFRESH-DOMAIN-008 날씨 동기화가 진행 중이면 새 동기화를 시작하지 않는다") {
            runTest(mainDispatcher) {
                val completion = CompletableDeferred<Result<Unit>>()
                val useCase = mockk<FetchCurrentWeatherUseCase>()
                coEvery { useCase(parameter = Unit) } coAnswers { completion.await() }
                val viewModel = weatherViewModel(fetchCurrentWeatherUseCase = useCase)

                viewModel.fetch()
                viewModel.fetch()
                completion.complete(Result.success(Unit))
                advanceUntilIdle()

                coVerify(exactly = 1) { useCase(parameter = Unit) }
            }
        }

        test("TC-CALENDAR-HOME-DATA-025 위치 권한 허용 계기는 간격과 무관한 동기화를 요청한다") {
            runTest(mainDispatcher) {
                val fetchCurrentWeatherUseCase = successfulFetchCurrentWeatherUseCase()
                val useCase = successfulRefreshCurrentWeatherUseCase()
                val viewModel =
                    weatherViewModel(
                        fetchCurrentWeatherUseCase = fetchCurrentWeatherUseCase,
                        refreshCurrentWeatherUseCase = useCase,
                    )

                viewModel.refreshOnLocationPermissionGranted()
                advanceUntilIdle()

                coVerify(exactly = 1) { useCase(parameter = Unit) }
                coVerify(exactly = 0) { fetchCurrentWeatherUseCase(parameter = Unit) }
            }
        }

        test("TC-CALENDAR-HOME-DATA-027 동기화가 진행 중이면 위치 권한 허용 계기의 동기화도 시작하지 않는다") {
            runTest(mainDispatcher) {
                val completion = CompletableDeferred<Result<Unit>>()
                val fetchCurrentWeatherUseCase = mockk<FetchCurrentWeatherUseCase>()
                coEvery { fetchCurrentWeatherUseCase(parameter = Unit) } coAnswers { completion.await() }
                val useCase = successfulRefreshCurrentWeatherUseCase()
                val viewModel =
                    weatherViewModel(
                        fetchCurrentWeatherUseCase = fetchCurrentWeatherUseCase,
                        refreshCurrentWeatherUseCase = useCase,
                    )

                viewModel.fetch()
                viewModel.refreshOnLocationPermissionGranted()
                completion.complete(Result.success(Unit))
                advanceUntilIdle()

                coVerify(exactly = 0) { useCase(parameter = Unit) }
            }
        }

        test("동기화가 실행되는 동안 진행 중임을 알린다") {
            runTest(mainDispatcher) {
                val completion = CompletableDeferred<Result<Unit>>()
                val useCase = mockk<FetchCurrentWeatherUseCase>()
                coEvery { useCase(parameter = Unit) } coAnswers { completion.await() }
                val viewModel = weatherViewModel(fetchCurrentWeatherUseCase = useCase)
                viewModel.isLoading.value shouldBe false

                viewModel.fetch()
                advanceUntilIdle()
                viewModel.isLoading.value shouldBe true

                completion.complete(Result.success(Unit))
                advanceUntilIdle()

                viewModel.isLoading.value shouldBe false
            }
        }

        test("동기화가 끝난 뒤 발생한 새 동기화 조건은 처리한다") {
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

        test("동기화가 실패로 끝난 뒤 발생한 새 동기화 조건은 처리한다") {
            runTest(mainDispatcher) {
                val useCase = mockk<FetchCurrentWeatherUseCase>()
                coEvery { useCase(parameter = Unit) } returns
                    Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                val viewModel = weatherViewModel(fetchCurrentWeatherUseCase = useCase)

                viewModel.fetch()
                advanceUntilIdle()
                viewModel.fetch()
                advanceUntilIdle()

                coVerify(exactly = 2) { useCase(parameter = Unit) }
            }
        }

        test("날짜별 날씨가 조회되면 캘린더에 표시할 날씨로 제공한다") {
            runTest(mainDispatcher) {
                val calendarWeather = calendarWeather()
                val viewModel =
                    weatherViewModel(
                        getCurrentCalendarWeatherUseCase =
                            getCurrentCalendarWeatherUseCase(Result.success(CalendarWeatherReport(weatherList = listOf(calendarWeather)))),
                    )

                viewModel.weatherReport.test {
                    awaitItem().weatherList shouldBe emptyList()
                    awaitItem().weatherList shouldBe listOf(calendarWeather)
                }
            }
        }

        test("TC-CALENDAR-HOME-DATA-023 날짜별 날씨가 없거나 조회에 실패하면 날씨 없이 표시한다") {
            runTest(mainDispatcher) {
                val results =
                    listOf(
                        Result.success(CalendarWeatherReport()),
                        Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>())),
                    )

                results.forEach { result ->
                    val viewModel = weatherViewModel(getCurrentCalendarWeatherUseCase = getCurrentCalendarWeatherUseCase(result))

                    viewModel.weatherReport.test {
                        awaitItem() shouldBe CalendarWeatherReport()
                        advanceUntilIdle()
                        expectNoEvents()
                    }
                }
            }
        }

        test("지역명이 조회되면 날씨 검색에 사용할 지역명으로 제공한다") {
            runTest(mainDispatcher) {
                val locationName = fixtureMonkey.nonBlankString()
                val viewModel =
                    weatherViewModel(
                        getCurrentCalendarWeatherUseCase =
                            getCurrentCalendarWeatherUseCase(Result.success(CalendarWeatherReport(locationName = locationName))),
                    )

                viewModel.weatherReport.test {
                    awaitItem().locationName shouldBe ""
                    awaitItem().locationName shouldBe locationName
                }
            }
        }
    }
}

private fun weatherViewModel(
    fetchCurrentWeatherUseCase: FetchCurrentWeatherUseCase = successfulFetchCurrentWeatherUseCase(),
    refreshCurrentWeatherUseCase: RefreshCurrentWeatherUseCase =
        successfulRefreshCurrentWeatherUseCase(),
    getCurrentCalendarWeatherUseCase: GetCurrentCalendarWeatherUseCase =
        getCurrentCalendarWeatherUseCase(Result.success(CalendarWeatherReport())),
): CalendarHomeWeatherViewModel =
    CalendarHomeWeatherViewModel(
        fetchCurrentWeatherUseCase = fetchCurrentWeatherUseCase,
        refreshCurrentWeatherUseCase = refreshCurrentWeatherUseCase,
        getCurrentCalendarWeatherUseCase = getCurrentCalendarWeatherUseCase,
    )

private fun successfulFetchCurrentWeatherUseCase(): FetchCurrentWeatherUseCase =
    mockk<FetchCurrentWeatherUseCase>().also { useCase ->
        coEvery { useCase(parameter = Unit) } returns Result.success(Unit)
    }

private fun successfulRefreshCurrentWeatherUseCase(): RefreshCurrentWeatherUseCase =
    mockk<RefreshCurrentWeatherUseCase>().also { useCase ->
        coEvery { useCase(parameter = Unit) } returns Result.success(Unit)
    }

private fun getCurrentCalendarWeatherUseCase(result: Result<CalendarWeatherReport>): GetCurrentCalendarWeatherUseCase =
    mockk<GetCurrentCalendarWeatherUseCase>().also { useCase ->
        every { useCase(parameter = Unit) } returns flowOf(result)
    }

private fun calendarWeather(): CalendarWeather =
    CalendarWeather(
        date = randomDate(),
        temperature = CalendarWeatherTemperature.Current(value = fixtureMonkey.giveMeOne()),
        weatherList = emptyList(),
    )

private fun randomDate(): LocalDate =
    LocalDate(
        year = 2_000 + (fixtureMonkey.giveMeOne<Int>().toUInt() % 1_000u).toInt(),
        month = 1 + (fixtureMonkey.giveMeOne<Int>().toUInt() % 12u).toInt(),
        day = 1 + (fixtureMonkey.giveMeOne<Int>().toUInt() % 28u).toInt(),
    )
