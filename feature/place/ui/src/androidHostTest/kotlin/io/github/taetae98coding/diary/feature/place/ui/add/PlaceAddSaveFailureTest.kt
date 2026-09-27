package io.github.taetae98coding.diary.feature.place.ui.add

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class PlaceAddSaveFailureTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-PLACE-ADD-FEATURE-039 저장에 실패하면 안내 없이 작성 내용과 고른 태그를 유지하고 같은 내용으로 다시 추가할 수 있다`() {
        val tag = placeTestTag(title = SELECTED_TAG_TITLE)
        val detailList = mutableListOf<PlaceDetail>()
        val tagIdSetList = mutableListOf<Set<Uuid>>()
        val viewModel = screenTestViewModel()
        every { viewModel.add(capture(detailList), tagIdSet = capture(tagIdSetList)) } just runs
        composeRule.setPlaceAddScreen(
            viewModel = viewModel,
            tagViewModel = addTagScreenTestViewModel(tagList = listOf(tag)),
        )
        fillInput()
        val colorHex = composeRule.currentColorHex()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(ADD_SUCCEEDED_MESSAGE).assertDoesNotExist()
        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(TYPED_TITLE))
        composeRule.onAllNodes(hasSetTextAction())[DESCRIPTION_INDEX].assert(hasText(TYPED_DESCRIPTION))
        composeRule.onAllNodes(hasSetTextAction())[ADDRESS_INDEX].assert(hasText(TYPED_ADDRESS))
        composeRule.onAllNodes(hasSetTextAction())[LATITUDE_INDEX].assert(hasText(TYPED_LATITUDE))
        composeRule.onAllNodes(hasSetTextAction())[LONGITUDE_INDEX].assert(hasText(TYPED_LONGITUDE))
        composeRule.onNodeWithText(colorHex, substring = true).assertExists()
        composeRule.onNodeWithText(SELECTED_TAG_TITLE).assertExists()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        detailList shouldHaveSize 2
        detailList[1] shouldBe detailList[0]
        tagIdSetList shouldBe listOf(setOf(tag.id), setOf(tag.id))
    }

    private fun fillInput() {
        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(TYPED_TITLE)
        composeRule.onAllNodes(hasSetTextAction())[DESCRIPTION_INDEX].performTextInput(TYPED_DESCRIPTION)
        composeRule.onAllNodes(hasSetTextAction())[ADDRESS_INDEX].performTextInput(TYPED_ADDRESS)
        composeRule.onAllNodes(hasSetTextAction())[LATITUDE_INDEX].performTextInput(TYPED_LATITUDE)
        composeRule.onAllNodes(hasSetTextAction())[LONGITUDE_INDEX].performTextInput(TYPED_LONGITUDE)
        composeRule.waitForIdle()
    }

    private companion object {
        const val SELECTED_TAG_TITLE = "PlaceAddSelectedTag"
        const val ADD_SUCCEEDED_MESSAGE = "Place added."
    }
}
