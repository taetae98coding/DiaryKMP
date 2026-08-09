package io.github.taetae98coding.diary.core.location.impl

import io.kotest.matchers.nulls.shouldBeNull
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AndroidLocationProviderTest {
    @Test
    fun `위치 권한이 허용되어 있지 않으면 위치 확인 불가로 null을 반환한다`() {
        val locationProvider = AndroidLocationProvider(context = RuntimeEnvironment.getApplication())

        val location = runBlocking { locationProvider.getCurrentLocation() }

        location.shouldBeNull()
    }
}
