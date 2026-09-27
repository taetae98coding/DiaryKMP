package io.github.taetae98coding.diary.feature.tag.ui.detail.place

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
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_MEMO_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_PLACE_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.TAG_TITLE
import io.github.taetae98coding.diary.feature.tag.ui.detail.screenTestViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.selectTagDetailTab
import io.github.taetae98coding.diary.feature.tag.ui.detail.setTagDetailScreen
import io.github.taetae98coding.diary.feature.tag.ui.detail.tagDetail
import io.github.taetae98coding.diary.feature.tag.ui.detail.tagDetailUiState
import io.github.taetae98coding.diary.feature.tag.ui.fixtureText
import io.github.taetae98coding.diary.feature.tag.ui.tagEntityPagingData
import io.github.taetae98coding.diary.feature.tag.ui.tagPlace
import io.kotest.matchers.booleans.shouldBeFalse
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class TagDetailPlaceListPositionTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `TC-TAG-DETAIL-PLACE-DOMAIN-013 화면이 회전하거나 창 크기가 바뀌어도 목록 모드에서 보던 목록 위치를 유지한다`() {
        val placeList = placeList()
        val placePagingDataFlow = MutableStateFlow(tagEntityPagingData(itemList = placeList))
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { PlaceTab(placePagingDataFlow = placePagingDataFlow) }
        scrollList()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        assertScrolledPosition(placeList)
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-DOMAIN-013 앱이 종료되지 않은 채 백그라운드에 갔다가 돌아와도 목록 모드에서 보던 목록 위치를 유지한다`() {
        val placeList = placeList()
        val placePagingDataFlow = MutableStateFlow(tagEntityPagingData(itemList = placeList))
        composeRule.setContent { PlaceTab(placePagingDataFlow = placePagingDataFlow) }
        scrollList()

        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()

        assertScrolledPosition(placeList)
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-DOMAIN-013 장소 추가나 장소 상세로 이동한 뒤 뒤로 돌아와도 목록 모드에서 보던 목록 위치를 유지한다`() {
        val placeList = placeList()
        val placePagingDataFlow = MutableStateFlow(tagEntityPagingData(itemList = placeList))
        var isTagDetailOnTop by mutableStateOf(true)
        composeRule.setContent {
            // 내비게이션이 뒤에 쌓인 화면을 컴포지션에서 내리고 저장 상태만 보관하는 것을 그대로 따른다.
            val saveableStateHolder = rememberSaveableStateHolder()
            if (isTagDetailOnTop) {
                saveableStateHolder.SaveableStateProvider(key = TAG_DETAIL_ENTRY_KEY) {
                    PlaceTab(placePagingDataFlow = placePagingDataFlow)
                }
            }
        }
        scrollList()

        composeRule.runOnIdle { isTagDetailOnTop = false }
        composeRule.waitForIdle()
        composeRule.runOnIdle { isTagDetailOnTop = true }
        composeRule.waitForIdle()

        assertScrolledPosition(placeList)
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-DOMAIN-013 다른 탭을 선택한 뒤 장소 탭으로 돌아와도 목록 모드에서 보던 목록 위치를 유지한다`() {
        val placeList = placeList()
        composeRule.setTagDetailScreen(
            viewModel = screenTestViewModel(MutableStateFlow(tagDetailUiState(detail = tagDetail(TAG_TITLE)))),
            placePagingData = tagEntityPagingData(itemList = placeList),
        )
        composeRule.selectTagDetailTab(DEFAULT_PLACE_TAB_DESCRIPTION)
        scrollList()

        composeRule.selectTagDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.selectTagDetailTab(DEFAULT_PLACE_TAB_DESCRIPTION)

        assertScrolledPosition(placeList)
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-DOMAIN-014 지도 모드에 다녀오면 목록 모드의 목록을 맨 위부터 보여 준다`() {
        val placeList = placeList()
        val placePagingDataFlow = MutableStateFlow(tagEntityPagingData(itemList = placeList))
        composeRule.setContent { PlaceTab(placePagingDataFlow = placePagingDataFlow) }
        scrollList()
        isDisplayed(placeList.first().detail.title).shouldBeFalse()

        composeRule.onNodeWithContentDescription(DEFAULT_SHOW_MAP_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_SHOW_LIST_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(placeList.first().detail.title).assertIsDisplayed()
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-DOMAIN-014 화면을 떠난 뒤 다시 들어오면 목록 모드의 목록을 맨 위부터 보여 준다`() {
        val placeList = placeList()
        val placePagingDataFlow = MutableStateFlow(tagEntityPagingData(itemList = placeList))
        var isTagDetailVisible by mutableStateOf(true)
        composeRule.setContent {
            // 전환 이력에서 빠진 화면은 저장 상태도 함께 지워지므로, 다시 들어온 화면은 저장 상태 없이 새로 구성된다.
            if (isTagDetailVisible) PlaceTab(placePagingDataFlow = placePagingDataFlow)
        }
        scrollList()

        composeRule.runOnIdle { isTagDetailVisible = false }
        composeRule.waitForIdle()
        composeRule.runOnIdle { isTagDetailVisible = true }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(placeList.first().detail.title).assertIsDisplayed()
    }

    @Composable
    private fun PlaceTab(placePagingDataFlow: MutableStateFlow<PagingData<Place>>) {
        ViewModeTestTagDetailPlaceTab(
            onEvent = {},
            state = rememberTagDetailPlaceState(),
            placePagingDataFlow = placePagingDataFlow,
        )
    }

    private fun scrollList() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TAG_DETAIL_PLACE_LIST_TEST_TAG).performScrollToIndex(SCROLLED_INDEX)
        composeRule.waitForIdle()
    }

    private fun assertScrolledPosition(placeList: List<Place>) {
        composeRule.onNodeWithContentDescription(DEFAULT_SHOW_MAP_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(placeList[SCROLLED_INDEX].detail.title).assertIsDisplayed()
        isDisplayed(placeList.first().detail.title).shouldBeFalse()
    }

    private fun isDisplayed(text: String): Boolean =
        runCatching { composeRule.onNodeWithText(text).assertIsDisplayed() }
            .isSuccess

    private companion object {
        private const val TAG_DETAIL_ENTRY_KEY = "TagDetail"
        private const val PLACE_COUNT = 40
        private const val SCROLLED_INDEX = 30

        fun placeList(): List<Place> {
            val titlePrefix = fixtureText(prefix = "TagDetailPlace")
            return List(PLACE_COUNT) { index -> tagPlace(title = "${titlePrefix}Index$index") }
        }
    }
}
