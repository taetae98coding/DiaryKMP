package io.github.taetae98coding.diary.compose.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * 지도 제공자의 실제 표시 요소는 만들 수 없으므로, 지도가 시작 위치로 읽는 상태가 나중에 바뀐 초기 위치를 따르지 않는지 확인한다.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryMapInitialCoordinateTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-aee62b17-5336-4d71-99af-7c63aaf2ea7e 지도가 표시된 뒤에 초기 위치가 바뀌어도 지도 위치와 확대 수준을 유지한다`() {
        val first = fixtureMonkey.giveMeOne<DiaryMapCoordinate>()
        val second = fixtureMonkey.giveMeOne<DiaryMapCoordinate>()
        var initialCoordinate by mutableStateOf(first)
        val states = mutableListOf<DiaryMapState>()

        composeRule.setContent {
            states += rememberDiaryMapState(initialProvider = DiaryMapProvider.NAVER, initialCoordinate = initialCoordinate)
        }
        composeRule.runOnIdle { initialCoordinate = second }
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            // 바뀐 초기 위치로 다시 그려졌는지부터 확인한다.
            (states.size > 1) shouldBe true
            states.last() shouldBeSameInstanceAs states.first()
            states.last().camera shouldBe
                DiaryMapCamera(
                    latitude = first.latitude,
                    longitude = first.longitude,
                    zoom = DiaryMapCamera.NEIGHBORHOOD_ZOOM,
                )
        }
    }

    private companion object {
        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()
    }
}
