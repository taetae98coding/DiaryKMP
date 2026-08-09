package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.place.ui.TEST_TAG_ADD_REQUEST_KEY
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaceDetailScreenRetentionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-015 화면이 재생성되어도 수정 중인 제목 설명 좌표를 유지한다`() {
        val restorationTester = StateRestorationTester(composeRule)
        val viewModel = screenTestViewModel(uiState = MutableStateFlow(content(detail = placeDetail())))
        restorationTester.setContent {
            PlaceDetailScreenTestTheme {
                PlaceDetailScreen(
                    navigateToTagAdd = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    detailViewModel = viewModel,
                    searchViewModel = searchScreenTestViewModel(),
                    navigateUp = {},
                    navigateToTagDetail = {},
                    tagViewModel = detailTagScreenTestViewModel(),
                )
            }
        }

        composeRule.input(TITLE_INDEX).performTextReplacement(CHANGED_TITLE)
        composeRule.input(DESCRIPTION_INDEX).performTextReplacement(CHANGED_DESCRIPTION)
        composeRule.input(ADDRESS_INDEX).performTextReplacement(CHANGED_ADDRESS)
        composeRule.input(LATITUDE_INDEX).performTextReplacement(CHANGED_LATITUDE)
        composeRule.input(LONGITUDE_INDEX).performTextReplacement(CHANGED_LONGITUDE)
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.input(TITLE_INDEX).assert(hasText(CHANGED_TITLE))
        composeRule.input(DESCRIPTION_INDEX).assert(hasText(CHANGED_DESCRIPTION))
        composeRule.input(ADDRESS_INDEX).assert(hasText(CHANGED_ADDRESS))
        composeRule.input(LATITUDE_INDEX).assert(hasText(CHANGED_LATITUDE))
        composeRule.input(LONGITUDE_INDEX).assert(hasText(CHANGED_LONGITUDE))
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-016 화면을 떠난 뒤 다시 들어오면 저장된 내용으로 시작한다`() {
        val detail = placeDetail()
        val uiState = MutableStateFlow(content(detail = detail))
        var isDisplayed by mutableStateOf(true)

        composeRule.setContent {
            PlaceDetailScreenTestTheme {
                if (isDisplayed) {
                    PlaceDetailScreen(
                        navigateToTagAdd = {},
                        tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                        detailViewModel = screenTestViewModel(uiState = uiState),
                        searchViewModel = searchScreenTestViewModel(),
                        navigateUp = {},
                        navigateToTagDetail = {},
                        tagViewModel = detailTagScreenTestViewModel(),
                    )
                }
            }
        }

        composeRule.input(TITLE_INDEX).performTextReplacement(CHANGED_TITLE)
        composeRule.waitForIdle()

        composeRule.runOnIdle { isDisplayed = false }
        composeRule.waitForIdle()
        composeRule.runOnIdle { isDisplayed = true }
        composeRule.waitForIdle()

        composeRule.input(TITLE_INDEX).assert(hasText(detail.title))
        composeRule.input(DESCRIPTION_INDEX).assert(hasText(detail.description))
    }

    @Test
    @Config(qualifiers = SCROLLABLE_QUALIFIERS)
    fun `TC-PLACE-DETAIL-FEATURE-028 수정을 처리하는 동안 보고 있던 화면 위치를 유지한다`() {
        val id = Uuid.random()
        val detail = placeDetail()
        val uiState = MutableStateFlow<PlaceDetailUiState>(content(id = id, detail = detail))
        composeRule.setPlaceDetailScreen(viewModel = screenTestViewModel(uiState = uiState))

        val scrolledTop = composeRule.scrollToBottomInput()

        uiState.value = content(id = id, detail = detail, isUpdateInProgress = true)
        composeRule.waitForIdle()
        composeRule.titleTop() shouldBe scrolledTop

        uiState.value = content(id = id, detail = detail, isUpdateInProgress = false)
        composeRule.waitForIdle()
        composeRule.titleTop() shouldBe scrolledTop
    }

    @Test
    @Config(qualifiers = SCROLLABLE_QUALIFIERS)
    fun `TC-PLACE-DETAIL-FEATURE-029 저장된 내용이 갱신되어도 보고 있던 화면 위치를 유지한다`() {
        val id = Uuid.random()
        val detail = placeDetail()
        val uiState = MutableStateFlow<PlaceDetailUiState>(content(id = id, detail = detail))
        composeRule.setPlaceDetailScreen(viewModel = screenTestViewModel(uiState = uiState))

        val scrolledTop = composeRule.scrollToBottomInput()

        uiState.value = content(id = id, detail = detail.copy(title = CHANGED_TITLE))
        composeRule.waitForIdle()

        composeRule.titleTop() shouldBe scrolledTop
    }
}

private const val SCROLLABLE_QUALIFIERS = "w480dp-h320dp"

private fun ComposeContentTestRule.titleTop(): Float = input(TITLE_INDEX).fetchSemanticsNode().positionInRoot.y

// 입력 목록을 끝까지 스크롤하고, 실제로 화면 위치가 옮겨졌는지 확인한 뒤 옮겨진 위치를 돌려준다.
private fun ComposeContentTestRule.scrollToBottomInput(): Float {
    val top = titleTop()
    input(LONGITUDE_INDEX).performScrollTo()
    waitForIdle()

    return titleTop().also { scrolledTop -> scrolledTop shouldBeLessThan top }
}
