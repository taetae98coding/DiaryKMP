package io.github.taetae98coding.diary.data.weather.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.ip.network.api.datasource.IpRemoteDataSource
import io.github.taetae98coding.diary.core.ip.network.api.entity.IpRemoteEntity
import io.github.taetae98coding.diary.core.location.api.Location
import io.github.taetae98coding.diary.core.location.api.LocationProvider
import io.github.taetae98coding.diary.core.weather.network.api.datasource.WeatherRemoteDataSource
import io.github.taetae98coding.diary.data.weather.WeatherDataTestKoinApplication
import io.github.taetae98coding.diary.domain.weather.usecase.FetchCurrentWeatherUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import org.koin.dsl.module
import org.koin.plugin.module.dsl.koinApplication
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class FetchCurrentWeatherUseCaseLocationTest :
    BehaviorSpec({
        Given("디바이스 위치와 공인 IP 기준 위치가 서로 다른 위도와 경도로 확인된다") {
            val deviceLocation = fixtureMonkey.giveMeOne<Location>()
            val ipRemoteDataSource = ipRemoteDataSource(location = fixtureMonkey.giveMeOne())
            val weatherRemoteDataSource = weatherRemoteDataSource()
            val useCase =
                useCase(
                    deviceLocation = deviceLocation,
                    ipRemoteDataSource = ipRemoteDataSource,
                    weatherRemoteDataSource = weatherRemoteDataSource,
                )

            When("현재 날씨 동기화를 요청한다") {
                val result = useCase(parameter = Unit)

                Then("TC-WEATHER-FETCH-DOMAIN-008 디바이스 위치로 조회하고 공인 IP 기준 위치는 사용하지 않는다") {
                    result.isSuccess shouldBe true
                    coVerify(exactly = 1) {
                        weatherRemoteDataSource.getCurrentWeather(latitude = deviceLocation.latitude, longitude = deviceLocation.longitude)
                    }
                    coVerify(exactly = 1) {
                        weatherRemoteDataSource.getForecast(latitude = deviceLocation.latitude, longitude = deviceLocation.longitude)
                    }
                    coVerify(exactly = 0) { ipRemoteDataSource.get() }
                }
            }
        }

        Given("디바이스에서 위치를 확인하지 못하고 공인 IP 기준 위치는 확인된다") {
            val ipLocation = fixtureMonkey.giveMeOne<IpRemoteEntity>()
            val weatherRemoteDataSource = weatherRemoteDataSource()
            val useCase =
                useCase(
                    deviceLocation = null,
                    ipRemoteDataSource = ipRemoteDataSource(location = ipLocation),
                    weatherRemoteDataSource = weatherRemoteDataSource,
                )

            When("현재 날씨 동기화를 요청한다") {
                val result = useCase(parameter = Unit)

                Then("TC-WEATHER-FETCH-DOMAIN-009 동기화가 성공하고 공인 IP 기준 위치로 조회한다") {
                    result.isSuccess shouldBe true
                    coVerify(exactly = 1) {
                        weatherRemoteDataSource.getCurrentWeather(latitude = ipLocation.latitude, longitude = ipLocation.longitude)
                    }
                    coVerify(exactly = 1) {
                        weatherRemoteDataSource.getForecast(latitude = ipLocation.latitude, longitude = ipLocation.longitude)
                    }
                }
            }
        }
    })

private fun useCase(
    deviceLocation: Location?,
    ipRemoteDataSource: IpRemoteDataSource,
    weatherRemoteDataSource: WeatherRemoteDataSource,
): FetchCurrentWeatherUseCase {
    val locationProvider = mockk<LocationProvider>()
    coEvery { locationProvider.getCurrentLocation() } returns deviceLocation
    val clock = mockk<Clock>()
    every { clock.now() } returns fixtureMonkey.giveMeOne<Instant>()

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

private fun ipRemoteDataSource(location: IpRemoteEntity): IpRemoteDataSource =
    mockk<IpRemoteDataSource>().also { dataSource ->
        coEvery { dataSource.get() } returns location
    }

private fun weatherRemoteDataSource(): WeatherRemoteDataSource =
    mockk<WeatherRemoteDataSource>().also { dataSource ->
        coEvery { dataSource.getLocationName(any(), any()) } returns emptyList()
        every { dataSource.forecastInterval } returns 3.hours
        coEvery { dataSource.getCurrentWeather(any(), any()) } returns fixtureMonkey.giveMeOne()
        coEvery { dataSource.getForecast(any(), any()) } returns fixtureMonkey.giveMeOne()
    }
