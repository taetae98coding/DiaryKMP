package io.github.taetae98coding.diary.core.location.impl

import android.content.Context
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.Tasks
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.location.api.Location
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AndroidLocationProviderTest {
    private val client = mockk<FusedLocationProviderClient>()

    @Before
    fun setUp() {
        mockkStatic(LocationServices::class)
        every { LocationServices.getFusedLocationProviderClient(any<Context>()) } returns client
    }

    @After
    fun tearDown() {
        unmockkStatic(LocationServices::class)
    }

    @Test
    fun `TC-CURRENT-LOCATION-DOMAIN-002 위치 권한이 허용되어 있지 않으면 디바이스 위치를 확인하지 못한 것으로 다룬다`() {
        every { client.lastLocation } throws SecurityException()
        val locationProvider = AndroidLocationProvider(context = RuntimeEnvironment.getApplication())

        val location = runBlocking { withTimeout(TIMEOUT_MILLIS) { locationProvider.getCurrentLocation() } }

        location.shouldBeNull()
    }

    @Test
    fun `TC-CURRENT-LOCATION-DOMAIN-002 위치 권한이 허용되어 있어도 기기가 위치 서비스를 제공하지 못하면 디바이스 위치를 확인하지 못한 것으로 다룬다`() {
        every { client.lastLocation } returns Tasks.forException(ApiException(Status(CommonStatusCodes.API_NOT_CONNECTED)))
        val locationProvider = AndroidLocationProvider(context = RuntimeEnvironment.getApplication())

        val location = runBlocking { withTimeout(TIMEOUT_MILLIS) { locationProvider.getCurrentLocation() } }

        location.shouldBeNull()
    }

    @Test
    fun `TC-CURRENT-LOCATION-DOMAIN-002 확인하는 도중 위치 권한이 회수되면 디바이스 위치를 확인하지 못한 것으로 다룬다`() {
        every { client.lastLocation } returns Tasks.forResult<android.location.Location>(null)
        every { client.getCurrentLocation(any<Int>(), any<CancellationToken>()) } returns Tasks.forException(SecurityException())
        val locationProvider = AndroidLocationProvider(context = RuntimeEnvironment.getApplication())

        val location = runBlocking { withTimeout(TIMEOUT_MILLIS) { locationProvider.getCurrentLocation() } }

        location.shouldBeNull()
    }

    @Test
    fun `TC-CURRENT-LOCATION-DOMAIN-006 디바이스가 최근에 확인해 둔 위치가 있으면 새로 확인하지 않는다`() {
        val recentLocation = androidLocation(latitude = fixtureMonkey.latitude(), longitude = fixtureMonkey.longitude())
        val freshLocation = androidLocation(latitude = fixtureMonkey.latitude(), longitude = fixtureMonkey.longitude())
        every { client.lastLocation } returns Tasks.forResult(recentLocation)
        every { client.getCurrentLocation(any<Int>(), any<CancellationToken>()) } returns Tasks.forResult(freshLocation)
        val locationProvider = AndroidLocationProvider(context = RuntimeEnvironment.getApplication())

        val location = runBlocking { withTimeout(TIMEOUT_MILLIS) { locationProvider.getCurrentLocation() } }

        location shouldBe Location(latitude = recentLocation.latitude, longitude = recentLocation.longitude)
        verify(exactly = 0) { client.getCurrentLocation(any<Int>(), any<CancellationToken>()) }
    }

    @Test
    fun `최근에 확인해 둔 위치가 없으면 새로 확인한 위치를 쓴다`() {
        val freshLocation = androidLocation(latitude = fixtureMonkey.latitude(), longitude = fixtureMonkey.longitude())
        every { client.lastLocation } returns Tasks.forResult<android.location.Location>(null)
        every { client.getCurrentLocation(any<Int>(), any<CancellationToken>()) } returns Tasks.forResult(freshLocation)
        val locationProvider = AndroidLocationProvider(context = RuntimeEnvironment.getApplication())

        val location = runBlocking { withTimeout(TIMEOUT_MILLIS) { locationProvider.getCurrentLocation() } }

        location shouldBe Location(latitude = freshLocation.latitude, longitude = freshLocation.longitude)
    }

    private companion object {
        private const val TIMEOUT_MILLIS = 5_000L
        private const val MAX_LATITUDE = 90
        private const val MAX_LONGITUDE = 180

        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        private fun FixtureMonkey.latitude(): Double = giveMeOne<Int>() % MAX_LATITUDE + giveMeOne<Int>() % 1_000 / 1_000.0

        private fun FixtureMonkey.longitude(): Double = giveMeOne<Int>() % MAX_LONGITUDE + giveMeOne<Int>() % 1_000 / 1_000.0

        private fun androidLocation(
            latitude: Double,
            longitude: Double,
        ): android.location.Location =
            android.location.Location("test").apply {
                this.latitude = latitude
                this.longitude = longitude
            }
    }
}
