package io.github.taetae98coding.diary.data.weather.usecase

import dev.zacsweers.metro.createGraphFactory
import io.github.taetae98coding.diary.core.data.testing.ip.ipRemoteEntity
import io.github.taetae98coding.diary.core.data.testing.location.location
import io.github.taetae98coding.diary.core.data.testing.weather.currentWeatherRemoteEntity
import io.github.taetae98coding.diary.core.data.testing.weather.forecastRemoteEntity
import io.github.taetae98coding.diary.core.ip.network.api.datasource.IpRemoteDataSource
import io.github.taetae98coding.diary.core.ip.network.api.entity.IpRemoteEntity
import io.github.taetae98coding.diary.core.location.api.Location
import io.github.taetae98coding.diary.core.location.api.LocationProvider
import io.github.taetae98coding.diary.core.weather.network.api.datasource.WeatherRemoteDataSource
import io.github.taetae98coding.diary.data.weather.WeatherDataTestGraph
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.weather.usecase.FetchCurrentWeatherUseCase
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class FetchCurrentWeatherUseCaseLocationTest :
    BehaviorSpec({
        Given("디바이스 위치와 공인 IP 기준 위치가 서로 다른 위도와 경도로 확인된다") {
            val deviceLocation = Arb.location().next()
            val ipRemoteDataSource = ipRemoteDataSource(location = Arb.ipRemoteEntity().next())
            val weatherRemoteDataSource = weatherRemoteDataSource()
            val useCase =
                useCase(
                    deviceLocation = deviceLocation,
                    ipRemoteDataSource = ipRemoteDataSource,
                    weatherRemoteDataSource = weatherRemoteDataSource,
                )

            When("현재 날씨 동기화를 요청한다") {
                val result = useCase()

                Then("TC-3253502b-5d0a-45c2-af8c-f7c7806ee83e 디바이스 위치로 조회하고 공인 IP 기준 위치는 사용하지 않는다") {
                    result.isSuccess shouldBe true
                    coVerify(exactly = 1) {
                        weatherRemoteDataSource.readCurrentWeather(latitude = deviceLocation.latitude, longitude = deviceLocation.longitude)
                    }
                    coVerify(exactly = 1) {
                        weatherRemoteDataSource.readForecast(latitude = deviceLocation.latitude, longitude = deviceLocation.longitude)
                    }
                    coVerify(exactly = 0) { ipRemoteDataSource.read() }
                }
            }
        }

        Given("디바이스에서 위치를 확인하지 못하고 공인 IP 기준 위치는 확인된다") {
            val ipLocation = Arb.ipRemoteEntity().next()
            val weatherRemoteDataSource = weatherRemoteDataSource()
            val useCase =
                useCase(
                    deviceLocation = null,
                    ipRemoteDataSource = ipRemoteDataSource(location = ipLocation),
                    weatherRemoteDataSource = weatherRemoteDataSource,
                )

            When("현재 날씨 동기화를 요청한다") {
                val result = useCase()

                Then("TC-65566469-303b-41e1-b25d-323c6b8b8eb3 동기화가 성공하고 공인 IP 기준 위치로 조회한다") {
                    result.isSuccess shouldBe true
                    coVerify(exactly = 1) {
                        weatherRemoteDataSource.readCurrentWeather(latitude = ipLocation.latitude, longitude = ipLocation.longitude)
                    }
                    coVerify(exactly = 1) {
                        weatherRemoteDataSource.readForecast(latitude = ipLocation.latitude, longitude = ipLocation.longitude)
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
    every { clock.now() } returns Arb.secondPrecisionInstant().next()

    return createGraphFactory<WeatherDataTestGraph.Factory>()
        .create(
            locationProvider = locationProvider,
            ipRemoteDataSource = ipRemoteDataSource,
            weatherRemoteDataSource = weatherRemoteDataSource,
            clock = clock,
        ).fetchCurrentWeatherUseCase
}

private fun ipRemoteDataSource(location: IpRemoteEntity): IpRemoteDataSource =
    mockk<IpRemoteDataSource>().also { dataSource ->
        coEvery { dataSource.read() } returns location
    }

private fun weatherRemoteDataSource(): WeatherRemoteDataSource =
    mockk<WeatherRemoteDataSource>().also { dataSource ->
        coEvery { dataSource.readLocationName(any(), any()) } returns emptyList()
        every { dataSource.forecastInterval } returns 3.hours
        coEvery { dataSource.readCurrentWeather(any(), any()) } returns Arb.currentWeatherRemoteEntity().next()
        coEvery { dataSource.readForecast(any(), any()) } returns Arb.forecastRemoteEntity().next()
    }
