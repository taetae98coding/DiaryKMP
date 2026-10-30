package io.github.taetae98coding.diary.compose.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * 지도 제공자의 실제 표시 요소는 만들 수 없으므로, 지도가 알려온 위치와 배치한 화면이 지정한 지점을 상태에 담은 뒤 되살린 상태를 확인한다.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryMapStateRestorationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-ffe3d6a4-1504-4305-b8f8-f39fd2889ecd 화면이 재생성되면 지도가 알려온 위치와 확대 수준을 되살린다`() {
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

    // 화면 재생성과 메모리 정리 뒤 복원은 모두 저장해 둔 상태로 다시 만드는 같은 경계를 지나므로 한 번의 복원으로 함께 확인한다.
    @Test
    fun `TC-bd198412-2aed-4d8b-b062-edab46cffd90 화면 재생성이나 메모리 정리 뒤 복원하면 표시하던 지점을 되살린다`() {
        val spot = fixtureCoordinate()
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var state: DiaryMapState

        restorationTester.setContent {
            state = rememberDiaryMapState(initialProvider = DiaryMapProvider.NAVER)
        }
        composeRule.runOnIdle { state.selectSpot(spot) }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle { state.spot shouldBe spot }
    }

    @Test
    fun `TC-68f5de9e-1099-4902-b584-ac84ec44293c 지점이 없던 지도는 화면 재생성이나 메모리 정리 뒤 복원해도 지점 없이 되살린다`() {
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var state: DiaryMapState

        restorationTester.setContent {
            state = rememberDiaryMapState(initialProvider = DiaryMapProvider.NAVER)
        }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle { state.spot.shouldBeNull() }
    }

    @Test
    fun `TC-d553bb2e-0558-4625-8144-263dd58fe04d DiaryMap을 새로 시작하면 이전에 표시하던 지점을 되살리지 않는다`() {
        var isVisible by mutableStateOf(true)
        var state: DiaryMapState? = null

        composeRule.setContent {
            if (isVisible) {
                state = rememberDiaryMapState(initialProvider = DiaryMapProvider.NAVER)
            }
        }
        composeRule.runOnIdle { checkNotNull(state).selectSpot(fixtureCoordinate()) }
        composeRule.runOnIdle { isVisible = false }
        composeRule.runOnIdle { isVisible = true }

        composeRule.runOnIdle { checkNotNull(state).spot.shouldBeNull() }
    }

    private fun fixtureCoordinate(): DiaryMapCoordinate =
        DiaryMapCoordinate(
            latitude = fixtureMonkey.giveMeOne<Int>() % MAX_LATITUDE / COORDINATE_SCALE,
            longitude = fixtureMonkey.giveMeOne<Int>() % MAX_LONGITUDE / COORDINATE_SCALE,
        )

    private companion object {
        private const val MAX_LATITUDE = 900_000
        private const val MAX_LONGITUDE = 1_800_000
        private const val MAX_ZOOM = 21
        private const val COORDINATE_SCALE = 10_000.0

        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()
    }
}
