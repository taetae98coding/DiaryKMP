package io.github.taetae98coding.diary.compose.map

import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * 지도 제공자의 실제 표시 요소는 만들 수 없으므로, 지도가 알려온 위치를 상태에 담은 뒤 화면 재생성으로 되살린 상태를 확인한다.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryMapStateRestorationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-DIARY-MAP-DOMAIN-046 화면이 재생성되면 지도가 알려온 위치와 확대 수준을 되살린다`() {
        val camera =
            DiaryMapCamera(
                latitude = fixtureMonkey.giveMeOne<Int>() % MAX_LATITUDE / COORDINATE_SCALE,
                longitude = fixtureMonkey.giveMeOne<Int>() % MAX_LONGITUDE / COORDINATE_SCALE,
                zoom = (fixtureMonkey.giveMeOne<Int>() % MAX_ZOOM).toDouble().let { zoom -> if (zoom < 0) -zoom else zoom },
            )
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var state: DiaryMapState

        restorationTester.setContent {
            state = rememberDiaryMapState(initialProvider = DiaryMapProvider.NAVER)
        }
        // 지도가 사용자가 조작한 위치를 알려온 것과 같게 상태에 담는다.
        composeRule.runOnIdle { state.moveCamera(camera) }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle {
            state.coordinate shouldBe DiaryMapCoordinate(latitude = camera.latitude, longitude = camera.longitude)
            state.camera?.zoom shouldBe camera.zoom
        }
    }

    private companion object {
        private const val MAX_LATITUDE = 900_000
        private const val MAX_LONGITUDE = 1_800_000
        private const val MAX_ZOOM = 21
        private const val COORDINATE_SCALE = 10_000.0

        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()
    }
}
