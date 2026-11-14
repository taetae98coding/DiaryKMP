package io.github.taetae98coding.diary.feature.place.ui.detail.tab

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.lifecycle.Lifecycle
import io.github.taetae98coding.diary.feature.place.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.place.ui.detail.DEFAULT_DETAIL_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.place.ui.detail.DEFAULT_MEMO_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.place.ui.detail.FIRST_PLACE_ID
import io.github.taetae98coding.diary.feature.place.ui.detail.PlaceDetailScreen
import io.github.taetae98coding.diary.feature.place.ui.detail.PlaceDetailScreenTestTheme
import io.github.taetae98coding.diary.feature.place.ui.detail.content
import io.github.taetae98coding.diary.feature.place.ui.detail.contentSharerFactoryOf
import io.github.taetae98coding.diary.feature.place.ui.detail.detailTagScreenTestViewModel
import io.github.taetae98coding.diary.feature.place.ui.detail.preparePlaceDetailTabViewModels
import io.github.taetae98coding.diary.feature.place.ui.detail.screenTestViewModel
import io.github.taetae98coding.diary.feature.place.ui.detail.searchScreenTestViewModel
import io.github.taetae98coding.diary.feature.place.ui.detail.selectPlaceDetailTab
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 선택한 탭이 백그라운드 복귀와 앱 재실행의 경계를 지날 때 유지되는지, 처음 선택으로 돌아가는지를 검증한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaceDetailTabBoundaryTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `TC-98a339fa-8512-4b09-9578-c4f1f639737e 앱이 백그라운드에 다녀와도 선택한 탭이 유지된다`() {
        preparePlaceDetailTabViewModels()
        composeRule.setContent { DetailScreen() }
        composeRule.waitForIdle()
        composeRule.selectPlaceDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()

        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()
    }

    @Test
    fun `TC-78b60d4e-07fc-4bf9-8ef9-be7bfd62ae64 앱을 다시 실행해 들어오면 장소 디테일 탭으로 시작한다`() {
        var launchCount by mutableIntStateOf(0)
        preparePlaceDetailTabViewModels()
        // 앱을 다시 실행하면 저장해 둔 화면 상태와 ViewModel이 없으므로, 새 키로 저장 상태 없이 화면과 ViewModel을 다시 구성한다.
        composeRule.setContent {
            key(launchCount) { DetailScreen() }
        }
        composeRule.waitForIdle()
        composeRule.selectPlaceDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()

        composeRule.runOnIdle { launchCount += 1 }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_DETAIL_TAB_DESCRIPTION).assertIsSelected()
    }

    @Composable
    private fun DetailScreen() {
        PlaceDetailScreenTestTheme {
            PlaceDetailScreen(
                navigateUp = {},
                navigateToTagAdd = {},
                navigateToTagDetail = {},
                navigateToMemoAdd = {},
                navigateToMemoDetail = {},
                navigateToMemoFinishedList = {},
                id = FIRST_PLACE_ID,
                tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                detailViewModel = remember { screenTestViewModel(uiState = MutableStateFlow(content(id = FIRST_PLACE_ID))) },
                searchViewModel = remember { searchScreenTestViewModel() },
                tagViewModel = remember { detailTagScreenTestViewModel() },
                contentSharerFactory = contentSharerFactoryOf(),
            )
        }
    }
}
