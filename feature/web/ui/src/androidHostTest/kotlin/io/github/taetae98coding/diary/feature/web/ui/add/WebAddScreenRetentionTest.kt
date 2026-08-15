package io.github.taetae98coding.diary.feature.web.ui.add

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.github.taetae98coding.diary.feature.web.ui.TEST_TAG_ADD_REQUEST_KEY
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class WebAddScreenRetentionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-WEB-ADD-FEATURE-010 화면이 재생성되어도 작성 중이던 내용을 유지한다`() {
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
                    tagViewModel = addTagScreenTestViewModel(),
                )
            }
        }
        composeRule.fillAllInput()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.inputCount() shouldBe INPUT_COUNT_WITHOUT_HEADER + INPUT_COUNT_PER_HEADER * 2
        composeRule.titleInput().assert(hasText(TYPED_TITLE))
        composeRule.descriptionInput().assert(hasText(TYPED_DESCRIPTION))
        composeRule.urlInput().assert(hasText(TYPED_URL))
        composeRule.headerNameInput().assert(hasText(TYPED_FIRST_HEADER_NAME))
        composeRule.headerValueInput().assert(hasText(TYPED_FIRST_HEADER_VALUE))
        composeRule.headerNameInput(row = 1).assert(hasText(TYPED_SECOND_HEADER_NAME))
        composeRule.headerValueInput(row = 1).assert(hasText(TYPED_SECOND_HEADER_VALUE))
    }
}
