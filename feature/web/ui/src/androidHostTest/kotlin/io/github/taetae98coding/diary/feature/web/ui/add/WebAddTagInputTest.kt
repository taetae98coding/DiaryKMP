package io.github.taetae98coding.diary.feature.web.ui.add

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import io.github.taetae98coding.diary.feature.web.ui.TEST_TAG_ADD_REQUEST_KEY
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class WebAddTagInputTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-WEB-ADD-FEATURE-019 추가에 성공해도 고른 태그는 유지한다`() {
        val tag = webTestTag(title = SELECTED_TAG_TITLE)
        composeRule.setContent {
            WebAddScreenTestTheme {
                WebAddScreen(
                    navigateToTagAdd = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    navigateUp = {},
                    componentVisibleProvider = { WebAddScaffoldComponentVisible() },
                    addViewModel = effectViewModel(effect = WebAddEffect.AddSucceeded(id = Uuid.random())),
                    navigateToTagDetail = {},
                    tagViewModel = addTagScreenTestViewModel(tagList = listOf(tag)),
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.fillAllInput()

        composeRule.clickAdd()

        composeRule.titleInput().assert(hasText(""))
        composeRule.descriptionInput().assert(hasText(""))
        composeRule.urlInput().assert(hasText(""))
        composeRule.inputCount() shouldBe INPUT_COUNT_WITHOUT_HEADER
        composeRule.onNodeWithText(SELECTED_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-WEB-ADD-FEATURE-020 화면이 재생성되어도 고른 태그가 유지된다`() {
        val tag = webTestTag(title = SELECTED_TAG_TITLE)
        val tagViewModel = addTagScreenTestViewModel(tagList = listOf(tag))
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            WebAddScreenTestTheme {
                WebAddScreen(
                    navigateToTagAdd = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    navigateUp = {},
                    componentVisibleProvider = { WebAddScaffoldComponentVisible() },
                    addViewModel = screenTestViewModel(),
                    navigateToTagDetail = {},
                    tagViewModel = tagViewModel,
                )
            }
        }
        composeRule.titleInput().performTextInput(TYPED_TITLE)
        composeRule.urlInput().performTextInput(TYPED_URL)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(SELECTED_TAG_TITLE).assertExists()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(TYPED_TITLE))
        composeRule.urlInput().assert(hasText(TYPED_URL))
        composeRule.onNodeWithText(SELECTED_TAG_TITLE).assertExists()
    }

    private companion object {
        const val SELECTED_TAG_TITLE = "WebAddSelectedTag"
    }
}
