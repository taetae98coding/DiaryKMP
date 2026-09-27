package io.github.taetae98coding.diary.data.weather.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.ipnetwork.api.datasource.IpRemoteDataSource
import io.github.taetae98coding.diary.core.location.api.LocationProvider
import io.github.taetae98coding.diary.core.weather.network.api.datasource.WeatherRemoteDataSource
import io.github.taetae98coding.diary.data.weather.WeatherDataTestKoinApplication
import io.github.taetae98coding.diary.domain.weather.usecase.FetchCurrentWeatherUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.logger.core.DiaryLog
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.logger.core.DiaryLoggerDelegate
import io.github.taetae98coding.diary.logger.crashlytics.api.CrashlyticsLog
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import org.koin.dsl.module
import org.koin.plugin.module.dsl.koinApplication
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class FetchCurrentWeatherUseCaseReportTest :
    BehaviorSpec({
        Given("오류 보고 기록 수단이 등록되어 있고 위치 확인과 현재 날씨·시간대별 예보 조회는 성공하며 지역명 조회만 실패하도록 제어되어 있다") {
            val weatherRemoteDataSource = weatherRemoteDataSource()
            coEvery { weatherRemoteDataSource.getLocationName(any(), any()) } throws
                FetchCurrentWeatherUseCaseReportTestException(fixtureMonkey.giveMeOne())
            val useCase =
                useCase(
                    weatherRemoteDataSource = weatherRemoteDataSource,
                    clock = clock { fixtureMonkey.giveMeOne<Instant>() },
                )
            val reportList = recordCrashlyticsLog()

            When("현재 날씨 동기화를 요청한다") {
                val result = useCase(parameter = Unit)

                Then("TC-WEATHER-FETCH-DOMAIN-023 동기화가 성공으로 끝나고 오류 보고가 남지 않는다") {
                    result.isSuccess shouldBe true
                    reportList.shouldBeEmpty()
                }
            }
        }

        Given("오류 보고 기록 수단이 등록되어 있고 날씨 동기화가 성공한 뒤 아직 1시간이 지나지 않았다") {
            val weatherRemoteDataSource = weatherRemoteDataSource()
            var now = fixtureMonkey.giveMeOne<Instant>()
            val useCase =
                useCase(
                    weatherRemoteDataSource = weatherRemoteDataSource,
                    clock = clock { now },
                )
            useCase(parameter = Unit).isSuccess shouldBe true
            val reportList = recordCrashlyticsLog()
            now += 59.minutes

            When("위치 권한 허용이 아닌 계기로 현재 날씨 동기화를 다시 요청한다") {
                val result = useCase(parameter = Unit)

                Then("TC-WEATHER-FETCH-DOMAIN-024 동기화가 성공으로 끝나고 조회 없이 오류 보고가 남지 않는다") {
                    result.isSuccess shouldBe true
                    coVerify(exactly = 1) { weatherRemoteDataSource.getCurrentWeather(any(), any()) }
                    coVerify(exactly = 1) { weatherRemoteDataSource.getForecast(any(), any()) }
                    reportList.shouldBeEmpty()
                }
            }
        }
    })

private class FetchCurrentWeatherUseCaseReportTestException(
    message: String,
) : RuntimeException(message)

private fun useCase(
    weatherRemoteDataSource: WeatherRemoteDataSource,
    clock: Clock,
): FetchCurrentWeatherUseCase {
    val locationProvider = mockk<LocationProvider>()
    coEvery { locationProvider.getCurrentLocation() } returns null
    val ipRemoteDataSource = mockk<IpRemoteDataSource>()
    coEvery { ipRemoteDataSource.get() } returns fixtureMonkey.giveMeOne()

    return koinApplication<WeatherDataTestKoinApplication> {
        modules(
            module {
                single<LocationProvider> { locationProvider }
                single<IpRemoteDataSource> { ipRemoteDataSource }
                single<WeatherRemoteDataSource> { weatherRemoteDataSource }
                single<Clock> { clock }
            },
        )
    }.koin.get()
}

private fun weatherRemoteDataSource(): WeatherRemoteDataSource =
    mockk<WeatherRemoteDataSource>().also { dataSource ->
        coEvery { dataSource.getLocationName(any(), any()) } returns emptyList()
        every { dataSource.forecastInterval } returns 3.hours
        coEvery { dataSource.getCurrentWeather(any(), any()) } returns fixtureMonkey.giveMeOne()
        coEvery { dataSource.getForecast(any(), any()) } returns fixtureMonkey.giveMeOne()
    }

private fun clock(now: () -> Instant): Clock =
    mockk<Clock>().also { clock ->
        every { clock.now() } answers { now() }
    }

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
