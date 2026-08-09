package io.github.taetae98coding.diary.feature.place.ui.add

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.github.taetae98coding.diary.feature.place.ui.TEST_TAG_ADD_REQUEST_KEY
import io.kotest.matchers.shouldBe
import io.mockk.every
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class PlaceAddTagInputTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-PLACE-ADD-FEATURE-037 추가에 성공해도 고른 태그는 유지한다`() {
        val tag = placeTestTag(title = SELECTED_TAG_TITLE)
        val effect = Channel<PlaceAddEffect>(capacity = Channel.BUFFERED)
        val viewModel = screenTestViewModel(effect = effect.receiveAsFlow())
        every { viewModel.add(any(), tagIdSet = any()) } answers {
            effect.trySend(PlaceAddEffect.AddSucceeded(id = Uuid.random())).getOrThrow()
        }
        composeRule.setPlaceAddScreen(
            viewModel = viewModel,
            tagViewModel = addTagScreenTestViewModel(tagList = listOf(tag)),
        )
        fillInput()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(""))
        composeRule.onAllNodes(hasSetTextAction())[DESCRIPTION_INDEX].assert(hasText(""))
        composeRule.onAllNodes(hasSetTextAction())[ADDRESS_INDEX].assert(hasText(""))
        composeRule.onNodeWithText(SELECTED_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-PLACE-ADD-FEATURE-038 화면이 재생성되어도 고른 태그가 유지된다`() {
        val tag = placeTestTag(title = SELECTED_TAG_TITLE)
        val tagViewModel = addTagScreenTestViewModel(tagList = listOf(tag))
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            PlaceAddScreenTestTheme {
                PlaceAddScreen(
                    navigateToTagAdd = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    navigateUp = {},
                    initialCoordinate = null,
                    addViewModel = screenTestViewModel(),
                    searchViewModel = searchScreenTestViewModel(),
                    navigateToTagDetail = {},
                    tagViewModel = tagViewModel,
                )
            }
        }
        fillInput()
        composeRule.onNodeWithText(SELECTED_TAG_TITLE).assertExists()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(TYPED_TITLE).assertExists()
        composeRule.onAllNodes(hasSetTextAction())[DESCRIPTION_INDEX].assert(hasText(TYPED_DESCRIPTION))
        composeRule.onAllNodes(hasSetTextAction())[LATITUDE_INDEX].assert(hasText(TYPED_LATITUDE))
        composeRule.onNodeWithText(SELECTED_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-ENTITY-TAG-INPUT-FEATURE-011 고른 태그를 누르면 그 태그의 상세 이동을 한 번 요청한다`() {
        val tag = placeTestTag(title = SELECTED_TAG_TITLE)
        val navigatedIdList = mutableListOf<Uuid>()
        composeRule.setPlaceAddScreen(
            viewModel = screenTestViewModel(),
            navigateToTagDetail = navigatedIdList::add,
            tagViewModel = addTagScreenTestViewModel(tagList = listOf(tag)),
        )

        composeRule.onNodeWithText(SELECTED_TAG_TITLE).performClick()
        composeRule.waitForIdle()

        navigatedIdList shouldBe listOf(tag.id)
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
    }
}
