package io.github.taetae98coding.diary.feature.place.ui.home.viewmode

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.lifecycle.Lifecycle
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.feature.place.ui.home.DEFAULT_LIST_VIEW_MODE_DESCRIPTION
import io.github.taetae98coding.diary.feature.place.ui.home.DEFAULT_MAP_VIEW_MODE_DESCRIPTION
import io.github.taetae98coding.diary.feature.place.ui.home.PlaceHomeUiState
import io.github.taetae98coding.diary.feature.place.ui.home.ViewModeTestPlaceHomeScaffold
import io.github.taetae98coding.diary.feature.place.ui.home.list.PLACE_HOME_PAGING_LIST_TEST_TAG
import io.github.taetae98coding.diary.feature.place.ui.home.placePagingDataFlowOf
import io.github.taetae98coding.diary.feature.place.ui.home.rememberPlaceHomeScaffoldState
import io.github.taetae98coding.diary.feature.place.ui.home.viewModeTestPlace
import io.kotest.matchers.booleans.shouldBeFalse
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class PlaceHomeListPositionTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `TC-PLACE-HOME-DOMAIN-026 화면이 회전하거나 창 크기가 바뀌어도 목록 모드에서 보던 목록 위치를 유지한다`() {
        val placeList = placeList()
        val placePagingDataFlow = placePagingDataFlowOf(placeList)
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { PlaceHomeTestScaffold(placePagingDataFlow = placePagingDataFlow) }
        selectListModeAndScroll()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        assertScrolledPosition(placeList)
    }

    @Test
    fun `TC-PLACE-HOME-DOMAIN-026 앱의 백그라운드 진입 후 복귀해도 목록 모드에서 보던 목록 위치를 유지한다`() {
        val placeList = placeList()
        val placePagingDataFlow = placePagingDataFlowOf(placeList)
        composeRule.setContent { PlaceHomeTestScaffold(placePagingDataFlow = placePagingDataFlow) }
        selectListModeAndScroll()

        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()

        assertScrolledPosition(placeList)
    }

    @Test
    fun `TC-PLACE-HOME-DOMAIN-026 장소 추가나 장소 상세로 이동한 뒤 뒤로 돌아와도 목록 모드에서 보던 목록 위치를 유지한다`() {
        val placeList = placeList()
        val placePagingDataFlow = placePagingDataFlowOf(placeList)
        var isPlaceHomeOnTop by mutableStateOf(true)
        composeRule.setContent {
            // 내비게이션이 뒤에 쌓인 화면을 컴포지션에서 내리고 저장 상태만 보관하는 것을 그대로 따른다.
            val saveableStateHolder = rememberSaveableStateHolder()
            if (isPlaceHomeOnTop) {
                saveableStateHolder.SaveableStateProvider(key = PLACE_HOME_ENTRY_KEY) {
                    PlaceHomeTestScaffold(placePagingDataFlow = placePagingDataFlow)
                }
            }
        }
        selectListModeAndScroll()

        composeRule.runOnIdle { isPlaceHomeOnTop = false }
        composeRule.waitForIdle()
        composeRule.runOnIdle { isPlaceHomeOnTop = true }
        composeRule.waitForIdle()

        assertScrolledPosition(placeList)
    }

    @Test
    fun `TC-PLACE-HOME-DOMAIN-027 지도 모드에 다녀오면 목록 모드의 목록을 맨 위부터 보여 준다`() {
        val placeList = placeList()
        val placePagingDataFlow = placePagingDataFlowOf(placeList)
        composeRule.setContent { PlaceHomeTestScaffold(placePagingDataFlow = placePagingDataFlow) }
        selectListModeAndScroll()
        isDisplayed(placeList.first().detail.title).shouldBeFalse()

        composeRule.onNodeWithContentDescription(DEFAULT_MAP_VIEW_MODE_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_LIST_VIEW_MODE_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(placeList.first().detail.title).assertIsDisplayed()
    }

    @Composable
    private fun PlaceHomeTestScaffold(placePagingDataFlow: MutableStateFlow<PagingData<Place>>) {
        ViewModeTestPlaceHomeScaffold(
            onEvent = {},
            state = rememberPlaceHomeScaffoldState(),
            uiState = PlaceHomeUiState.Loading,
            placePagingDataFlow = placePagingDataFlow,
        )
    }

    private fun selectListModeAndScroll() {
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_LIST_VIEW_MODE_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(PLACE_HOME_PAGING_LIST_TEST_TAG).performScrollToIndex(SCROLLED_INDEX)
        composeRule.waitForIdle()
    }

    private fun assertScrolledPosition(placeList: List<Place>) {
        composeRule.onNodeWithContentDescription(DEFAULT_MAP_VIEW_MODE_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(placeList[SCROLLED_INDEX].detail.title).assertIsDisplayed()
        isDisplayed(placeList.first().detail.title).shouldBeFalse()
    }

    private fun isDisplayed(text: String): Boolean =
        runCatching { composeRule.onNodeWithText(text).assertIsDisplayed() }
            .isSuccess

    private companion object {
        private const val PLACE_HOME_ENTRY_KEY = "PlaceHome"
        private const val PLACE_COUNT = 40
        private const val SCROLLED_INDEX = 30

        fun placeList(): List<Place> = List(PLACE_COUNT) { viewModeTestPlace() }
    }
}
