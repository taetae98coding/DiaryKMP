package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.place.ui.add.placeTestTag
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class PlaceDetailTagInputTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-037 조회 중에는 태그 입력을 표시하지 않는다`() {
        composeRule.setPlaceDetailScreen(
            viewModel = screenTestViewModel(uiState = MutableStateFlow(PlaceDetailUiState.Loading)),
        )
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).assertDoesNotExist()
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-037 조회가 끝나면 태그 입력을 표시한다`() {
        setPlaceDetailScreen()

        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).assertExists()
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-039 태그를 연결해도 수정 동작의 제공 여부는 바뀌지 않는다`() {
        setPlaceDetailScreen(selectableTagList = listOf(placeTestTag(title = SELECTABLE_TAG_TITLE)))
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()

        linkTag(title = SELECTABLE_TAG_TITLE)

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-041 연결한 태그를 누르면 그 태그의 상세 이동을 한 번 요청한다`() {
        val linkedTag = placeTestTag(title = LINKED_TAG_TITLE)
        val navigatedIdList = mutableListOf<Uuid>()
        setPlaceDetailScreen(
            tagList = listOf(linkedTag),
            navigateToTagDetail = navigatedIdList::add,
        )

        composeRule.onNodeWithText(LINKED_TAG_TITLE).performClick()
        composeRule.waitForIdle()

        navigatedIdList shouldBe listOf(linkedTag.id)
    }

    @Test
    fun `TC-PLACE-DETAIL-DOMAIN-029 태그를 연결해도 입력 중인 내용은 바뀌지 않는다`() {
        setPlaceDetailScreen(selectableTagList = listOf(placeTestTag(title = SELECTABLE_TAG_TITLE)))
        composeRule.input(TITLE_INDEX).performTextReplacement(CHANGED_TITLE)
        composeRule.input(DESCRIPTION_INDEX).performTextReplacement(CHANGED_DESCRIPTION)
        composeRule.input(ADDRESS_INDEX).performTextReplacement(CHANGED_ADDRESS)
        composeRule.input(LATITUDE_INDEX).performTextReplacement(CHANGED_LATITUDE)
        composeRule.waitForIdle()

        linkTag(title = SELECTABLE_TAG_TITLE)

        composeRule.input(TITLE_INDEX).assert(hasText(CHANGED_TITLE))
        composeRule.input(DESCRIPTION_INDEX).assert(hasText(CHANGED_DESCRIPTION))
        composeRule.input(ADDRESS_INDEX).assert(hasText(CHANGED_ADDRESS))
        composeRule.input(LATITUDE_INDEX).assert(hasText(CHANGED_LATITUDE))
    }

    private fun linkTag(title: String) {
        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).performClick()
        composeRule.waitForIdle()
        awaitPickerRow(title = title)
        composeRule.onNodeWithText(title).performClick()
        composeRule.waitForIdle()
    }

    // 선택 목록은 페이지 단위로 준비되므로 항목이 나타날 때까지 프레임과 실제 시간을 함께 진행시킨다.
    private fun awaitPickerRow(title: String) {
        repeat(PICKER_WAIT_ATTEMPT_COUNT) {
            composeRule.waitForIdle()
            if (composeRule.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty()) return
            composeRule.mainClock.advanceTimeByFrame()
            @Suppress("ForbiddenMethodCall")
            Thread.sleep(PICKER_WAIT_INTERVAL_MILLIS)
        }

        error("태그 선택 목록에 $title 이 나타나지 않았다")
    }

    private fun setPlaceDetailScreen(
        tagList: List<Tag> = emptyList(),
        selectableTagList: List<Tag> = emptyList(),
        navigateToTagDetail: (Uuid) -> Unit = {},
    ) {
        composeRule.setPlaceDetailScreen(
            viewModel = screenTestViewModel(),
            navigateToTagDetail = navigateToTagDetail,
            tagViewModel =
                detailTagScreenTestViewModel(
                    tagList = tagList,
                    selectableTagList = selectableTagList,
                ),
        )
        composeRule.waitForIdle()
    }

    private companion object {
        const val PICKER_WAIT_ATTEMPT_COUNT = 500
        const val PICKER_WAIT_INTERVAL_MILLIS = 10L
        const val DEFAULT_ENTITY_TAG_LABEL = "Select tag"
        const val LINKED_TAG_TITLE = "PlaceDetailLinkedTag"
        const val SELECTABLE_TAG_TITLE = "PlaceDetailSelectableTag"
    }
}
