package io.github.taetae98coding.diary.feature.place.ui.search

import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 다이얼로그 전체는 지도를 포함해 호스트 테스트에서 구성할 수 없으므로, 다이얼로그가 쓰는 지도 상태를 배치한 화면의 지도 상태에서 만들어 확인한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaceSearchDialogMapTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-PLACE-SEARCH-DIALOG-DOMAIN-021 네이버 지도를 보던 화면에서 열면 다이얼로그 지도가 네이버 지도로 같은 위치에서 시작한다`() {
        assertDialogMapStartsFromHostMap(provider = DiaryMapProvider.NAVER)
    }

    @Test
    fun `TC-PLACE-SEARCH-DIALOG-DOMAIN-021 Google 지도를 보던 화면에서 열면 다이얼로그 지도가 Google 지도로 같은 위치에서 시작한다`() {
        assertDialogMapStartsFromHostMap(provider = DiaryMapProvider.GOOGLE)
    }

    @Test
    fun `TC-PLACE-SEARCH-DIALOG-DOMAIN-022 다이얼로그에서 바꾼 제공자와 옮긴 위치는 배치한 화면의 지도에 반영하지 않는다`() {
        val hostCoordinate = fixtureMonkey.giveMeOne<DiaryMapCoordinate>()
        val dialogCoordinate = fixtureMonkey.giveMeOne<DiaryMapCoordinate>()
        val hostMapState = DiaryMapState(initialProvider = DiaryMapProvider.NAVER, initialCoordinate = hostCoordinate)
        lateinit var dialogState: PlaceSearchDialogState

        composeRule.setContent {
            dialogState = rememberPlaceSearchDialogState(hostMapState = hostMapState)
        }

        composeRule.runOnIdle {
            dialogState.mapState.select(DiaryMapProvider.GOOGLE)
            dialogState.mapState.moveTo(dialogCoordinate)
        }

        composeRule.runOnIdle {
            hostMapState.provider shouldBe DiaryMapProvider.NAVER
            hostMapState.coordinate shouldBe hostCoordinate
        }
    }

    @Test
    fun `TC-PLACE-SEARCH-DIALOG-FEATURE-019 화면이 재생성되어도 다이얼로그 지도의 제공자와 위치를 유지한다`() {
        val coordinate = fixtureMonkey.giveMeOne<DiaryMapCoordinate>()
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var dialogState: PlaceSearchDialogState

        restorationTester.setContent {
            dialogState = rememberPlaceSearchDialogState(initialProvider = DiaryMapProvider.NAVER)
        }
        composeRule.runOnIdle {
            dialogState.mapState.select(DiaryMapProvider.GOOGLE)
            dialogState.mapState.moveTo(coordinate)
        }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle {
            dialogState.mapState.provider shouldBe DiaryMapProvider.GOOGLE
            dialogState.mapState.coordinate shouldBe coordinate
        }
    }

    private fun assertDialogMapStartsFromHostMap(provider: DiaryMapProvider) {
        val coordinate = fixtureMonkey.giveMeOne<DiaryMapCoordinate>()
        val hostMapState = DiaryMapState(initialProvider = provider, initialCoordinate = coordinate)
        lateinit var dialogState: PlaceSearchDialogState

        composeRule.setContent {
            dialogState = rememberPlaceSearchDialogState(hostMapState = hostMapState)
        }

        composeRule.runOnIdle {
            dialogState.mapState.provider shouldBe provider
            dialogState.mapState.coordinate shouldBe coordinate
        }
    }

    private companion object {
        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()
    }
}
