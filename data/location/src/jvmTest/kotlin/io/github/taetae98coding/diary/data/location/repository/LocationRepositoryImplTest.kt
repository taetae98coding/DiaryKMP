package io.github.taetae98coding.diary.data.location.repository

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.ipnetwork.api.datasource.IpRemoteDataSource
import io.github.taetae98coding.diary.core.ipnetwork.api.entity.IpRemoteEntity
import io.github.taetae98coding.diary.core.location.api.Location
import io.github.taetae98coding.diary.core.location.api.LocationProvider
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class LocationRepositoryImplTest :
    FunSpec({
        test("TC-CURRENT-LOCATION-DOMAIN-001 디바이스 위치를 우선 사용한다") {
            val deviceLocation = fixtureMonkey.giveMeOne<Location>()
            val ipRemoteDataSource = ipRemoteDataSource(entity = fixtureMonkey.giveMeOne())
            val repository =
                repository(
                    locationProvider = locationProvider(location = deviceLocation),
                    ipRemoteDataSource = ipRemoteDataSource,
                )

            repository.fetch() shouldBe
                Coordinate(latitude = deviceLocation.latitude, longitude = deviceLocation.longitude)
            coVerify(exactly = 0) { ipRemoteDataSource.get() }
        }

        test("TC-CURRENT-LOCATION-DOMAIN-002 디바이스 위치를 확인하지 못한 모든 조건에서 공인 IP 기준 위치를 사용한다") {
            val ipEntity = fixtureMonkey.giveMeOne<IpRemoteEntity>()
            val repository =
                repository(
                    locationProvider = locationProvider(location = null),
                    ipRemoteDataSource = ipRemoteDataSource(entity = ipEntity),
                )

            repository.fetch() shouldBe
                Coordinate(latitude = ipEntity.latitude, longitude = ipEntity.longitude)
        }

        test("TC-CURRENT-LOCATION-DOMAIN-003 공인 IP 기준 위치 조회에 실패하면 현재 위치를 확인하지 못한다") {
            val failure = LocationRepositoryTestException(fixtureMonkey.giveMeOne())
            val ipRemoteDataSource = mockk<IpRemoteDataSource>()
            coEvery { ipRemoteDataSource.get() } throws failure
            val repository =
                repository(
                    locationProvider = locationProvider(location = null),
                    ipRemoteDataSource = ipRemoteDataSource,
                )

            shouldThrowExactly<LocationRepositoryTestException> {
                repository.fetch()
            } shouldBeSameInstanceAs failure
        }

        test("디바이스 위치를 확인할 때마다 공인 IP 기준 위치 조회를 한 번만 요청한다") {
            val ipRemoteDataSource = ipRemoteDataSource(entity = fixtureMonkey.giveMeOne())
            val repository =
                repository(
                    locationProvider = locationProvider(location = null),
                    ipRemoteDataSource = ipRemoteDataSource,
                )

            repository.fetch()

            coVerify(exactly = 1) { ipRemoteDataSource.get() }
        }
    })

private class LocationRepositoryTestException(
    message: String,
) : RuntimeException(message)

private fun locationProvider(location: Location?): LocationProvider {
    val locationProvider = mockk<LocationProvider>()
    coEvery { locationProvider.getCurrentLocation() } returns location

    return locationProvider
}

private fun ipRemoteDataSource(entity: IpRemoteEntity): IpRemoteDataSource {
    val ipRemoteDataSource = mockk<IpRemoteDataSource>()
    coEvery { ipRemoteDataSource.get() } returns entity

    return ipRemoteDataSource
}

private fun repository(
    locationProvider: LocationProvider,
    ipRemoteDataSource: IpRemoteDataSource,
): LocationRepositoryImpl =
    LocationRepositoryImpl(
        locationProvider = locationProvider,
        ipRemoteDataSource = ipRemoteDataSource,
    )
