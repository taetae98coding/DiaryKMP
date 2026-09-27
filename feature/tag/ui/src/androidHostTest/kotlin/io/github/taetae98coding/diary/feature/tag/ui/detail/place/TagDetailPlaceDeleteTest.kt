package io.github.taetae98coding.diary.feature.tag.ui.detail.place

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import io.github.taetae98coding.diary.compose.place.PLACE_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.place.PlaceListEffect
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_DETAIL_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_PLACE_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_WEB_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.EDIT_SUFFIX
import io.github.taetae98coding.diary.feature.tag.ui.detail.KOREAN_PLACE_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.TAG_TITLE
import io.github.taetae98coding.diary.feature.tag.ui.detail.placeEffectFlow
import io.github.taetae98coding.diary.feature.tag.ui.detail.placePagingDataFlow
import io.github.taetae98coding.diary.feature.tag.ui.detail.placeViewModelRef
import io.github.taetae98coding.diary.feature.tag.ui.detail.screenTestViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.selectTagDetailTab
import io.github.taetae98coding.diary.feature.tag.ui.detail.setTagDetailScreen
import io.github.taetae98coding.diary.feature.tag.ui.detail.tagDetail
import io.github.taetae98coding.diary.feature.tag.ui.detail.tagDetailUiState
import io.github.taetae98coding.diary.feature.tag.ui.detail.titleInput
import io.github.taetae98coding.diary.feature.tag.ui.fixtureId
import io.github.taetae98coding.diary.feature.tag.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.tag.ui.tagEntityPagingData
import io.github.taetae98coding.diary.feature.tag.ui.tagPlace
import io.kotest.matchers.shouldBe
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class TagDetailPlaceDeleteTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-FEATURE-038 장소 카드를 삭제 방향으로 밀면 그 장소 삭제를 요청하고 기본 안내와 실행 취소를 표시한다`() {
        val place = setScreenOnPlaceTab()

        swipeCardLeft()

        verify(exactly = 1) { placeViewModel().delete(id = place.id) }
        emitPlaceEffect(PlaceListEffect.Deleted(id = place.id))

        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-TAG-DETAIL-PLACE-FEATURE-038 장소 카드를 삭제 방향으로 밀면 한국어 안내와 실행 취소를 표시한다`() {
        val place = setScreenOnPlaceTab(tabDescription = KOREAN_PLACE_TAB_DESCRIPTION)

        swipeCardLeft()
        emitPlaceEffect(PlaceListEffect.Deleted(id = place.id))

        composeRule.onNodeWithText(KOREAN_DELETED_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(KOREAN_UNDO_ACTION).assertIsDisplayed()
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-FEATURE-040 삭제를 실행 취소하면 그 장소의 삭제 되돌리기를 요청한다`() {
        val place = setScreenOnPlaceTab()

        swipeCardLeft()
        emitPlaceEffect(PlaceListEffect.Deleted(id = place.id))
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { placeViewModel().restore(id = place.id) }
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-FEATURE-044 안내가 보이는 동안 다른 장소를 삭제하면 마지막 삭제에만 실행 취소가 적용된다`() {
        val place = setScreenOnPlaceTab()
        val otherId = fixtureId()

        swipeCardLeft()
        emitPlaceEffect(PlaceListEffect.Deleted(id = place.id))
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertIsDisplayed()

        emitPlaceEffect(PlaceListEffect.Deleted(id = otherId))

        composeRule.onAllNodesWithText(DEFAULT_DELETED_MESSAGE).fetchSemanticsNodes().size shouldBe 1
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { placeViewModel().restore(id = otherId) }
        verify(exactly = 0) { placeViewModel().restore(id = place.id) }
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-FEATURE-045 실행 취소를 선택하지 않으면 안내가 잠시 뒤 사라지고 되돌릴 수 없다`() {
        val place = setScreenOnPlaceTab()

        swipeCardLeft()
        emitPlaceEffect(PlaceListEffect.Deleted(id = place.id))
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertIsDisplayed()

        composeRule.mainClock.advanceTimeBy(AFTER_UNDO_SNACKBAR_DISMISS_MILLIS)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        verify(exactly = 0) { placeViewModel().restore(id = any()) }
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-FEATURE-046 안내가 보이는 동안 다른 탭으로 바꾸면 안내가 닫히고 되돌릴 수 없다`() {
        val place = setScreenOnPlaceTab()

        swipeCardLeft()
        emitPlaceEffect(PlaceListEffect.Deleted(id = place.id))
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertIsDisplayed()

        composeRule.selectTagDetailTab(DEFAULT_WEB_TAB_DESCRIPTION)
        composeRule.mainClock.advanceTimeBy(TAB_CHANGE_SETTLE_MILLIS)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        composeRule.selectTagDetailTab(DEFAULT_PLACE_TAB_DESCRIPTION)
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        verify(exactly = 0) { placeViewModel().restore(id = any()) }
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-FEATURE-047 안내가 보이는 동안 보기 모드를 바꿔도 안내가 남고 실행 취소할 수 있다`() {
        val place = setScreenOnPlaceTab()

        swipeCardLeft()
        emitPlaceEffect(PlaceListEffect.Deleted(id = place.id))
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertIsDisplayed()

        composeRule.onNodeWithContentDescription(DEFAULT_SHOW_MAP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_SHOW_LIST_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { placeViewModel().restore(id = place.id) }
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-DOMAIN-015 장소 탭의 삭제와 실행 취소는 대상 태그와 수정 중인 입력을 바꾸지 않는다`() {
        val place = tagPlace(title = PLACE_TITLE)
        // 대상 태그의 수정이나 삭제를 요청하면 엄격한 mock이 실패하므로, 요청하지 않았음을 함께 확인한다.
        setScreen(place = place)
        composeRule.titleInput().performTextInput(EDIT_SUFFIX)
        composeRule.waitForIdle()
        composeRule.selectTagDetailTab(DEFAULT_PLACE_TAB_DESCRIPTION)
        waitForPlace()

        swipeCardLeft()
        emitPlaceEffect(PlaceListEffect.Deleted(id = place.id))
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()
        composeRule.selectTagDetailTab(DEFAULT_DETAIL_TAB_DESCRIPTION)

        verify(exactly = 1) { placeViewModel().delete(id = place.id) }
        verify(exactly = 1) { placeViewModel().restore(id = place.id) }
        composeRule.titleInput().assert(hasText(TAG_TITLE + EDIT_SUFFIX))
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-065 장소 카드 위에서 왼쪽으로 밀면 탭은 바뀌지 않고 그 장소가 삭제된다`() {
        val place = setScreenOnPlaceTab()

        swipeCardLeft()

        verify(exactly = 1) { placeViewModel().delete(id = place.id) }
        composeRule.onNodeWithContentDescription(DEFAULT_PLACE_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNodeWithContentDescription(DEFAULT_WEB_TAB_DESCRIPTION).assertIsNotSelected()
    }

    private fun swipeCardLeft() {
        composeRule.onNode(hasTestTag(PLACE_CARD_TEST_TAG) and hasText(PLACE_TITLE)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
    }

    // 목록은 스와이프 결과를 저장소 갱신으로 확인하므로, 사라진 목록과 결과 Effect를 함께 넣는다.
    private fun emitPlaceEffect(effect: PlaceListEffect) {
        placePagingDataFlow.value = tagEntityPagingData(itemList = emptyList())
        placeEffectFlow.tryEmit(effect)
        composeRule.waitForIdle()
    }

    private fun placeViewModel(): TagDetailPlaceViewModel = requireNotNull(placeViewModelRef)

    private fun waitForPlace() {
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(PLACE_TITLE).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun setScreenOnPlaceTab(tabDescription: String = DEFAULT_PLACE_TAB_DESCRIPTION): Place {
        val place = tagPlace(title = PLACE_TITLE)
        setScreen(place = place)
        composeRule.selectTagDetailTab(tabDescription)
        waitForPlace()

        return place
    }

    private fun setScreen(place: Place) {
        composeRule.setTagDetailScreen(
            viewModel = screenTestViewModel(MutableStateFlow(tagDetailUiState(detail = tagDetail(TAG_TITLE)))),
            placePagingData = tagEntityPagingData(itemList = listOf(place)),
        )
    }

    private companion object {
        const val PLACE_TITLE = "TagDetailScreenPlaceDelete"
        const val PAGING_ITEMS_TIMEOUT_MILLIS = 5_000L
        const val DEFAULT_DELETED_MESSAGE = "Place deleted."
        const val KOREAN_DELETED_MESSAGE = "장소가 삭제되었습니다."
        const val DEFAULT_UNDO_ACTION = "Undo"
        const val KOREAN_UNDO_ACTION = "실행 취소"
        const val AFTER_UNDO_SNACKBAR_DISMISS_MILLIS = 11_000L
        const val TAB_CHANGE_SETTLE_MILLIS = 1_000L
    }
}
