package io.github.taetae98coding.diary.feature.web.ui.add

import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    fun `TC-fc3b2fe6-cb88-475f-88e5-4d9467a16e5d 화면이 재생성되어도 작성 중이던 내용을 유지한다`() {
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            WebAddScreenTestTheme {
                WebAddScreen(
                    addedResultRequestKey = null,
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

    // 앱을 다시 실행하면 저장해 둔 화면 상태도, 화면 상태를 들고 있던 객체도 없으므로 새 키와 새 인스턴스로 화면을 다시 구성한다.
    @Test
    fun `TC-9a468a94-1014-476a-9a23-b8a95f3556d8 앱을 다시 실행해 진입하면 새 입력 상태로 시작한다`() {
        var launchCount by mutableIntStateOf(0)
        composeRule.setContent {
            key(launchCount) {
                WebAddScreenTestTheme {
                    WebAddScreen(
                        addedResultRequestKey = null,
                        navigateToTagAdd = {},
                        tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                        navigateUp = {},
                        componentVisibleProvider = { WebAddScaffoldComponentVisible() },
                        addViewModel = remember { screenTestViewModel() },
                        navigateToTagDetail = {},
                        tagViewModel = remember { addTagScreenTestViewModel() },
                    )
                }
            }
        }
        composeRule.fillAllInput()
        composeRule.inputCount() shouldBe INPUT_COUNT_WITHOUT_HEADER + INPUT_COUNT_PER_HEADER * 2

        composeRule.runOnIdle { launchCount += 1 }
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(""))
        composeRule.descriptionInput().assert(hasText(""))
        composeRule.urlInput().assert(hasText(""))
        composeRule.inputCount() shouldBe INPUT_COUNT_WITHOUT_HEADER
    }
}
