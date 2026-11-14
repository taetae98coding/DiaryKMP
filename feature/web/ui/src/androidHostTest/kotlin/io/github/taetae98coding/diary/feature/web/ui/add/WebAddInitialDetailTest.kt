package io.github.taetae98coding.diary.feature.web.ui.add

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.feature.web.api.WebAddNavKey
import io.github.taetae98coding.diary.feature.web.ui.TEST_TAG_ADD_REQUEST_KEY
import io.kotest.matchers.shouldBe
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class WebAddInitialDetailTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `공유로 연 화면은 받은 제목과 URL을 채우고 설명과 요청 헤더는 비운다`() {
        setWebAddScreen(key = WebAddNavKey(initialTitle = SHARED_TITLE, initialUrl = SHARED_URL, isStandalone = true))

        composeRule.titleInput().assert(hasText(SHARED_TITLE))
        composeRule.descriptionInput().assert(hasText(""))
        composeRule.urlInput().assert(hasText(SHARED_URL))
        composeRule.inputCount() shouldBe INPUT_COUNT_WITHOUT_HEADER
    }

    @Test
    fun `공유 제목이 없으면 제목은 비우고 URL만 채운다`() {
        setWebAddScreen(key = WebAddNavKey(initialUrl = SHARED_URL, isStandalone = true))

        composeRule.titleInput().assert(hasText(""))
        composeRule.urlInput().assert(hasText(SHARED_URL))
    }

    @Test
    fun `공유로 연 화면도 처음 진입할 때와 같이 제목 입력에 초점이 있다`() {
        setWebAddScreen(key = WebAddNavKey(initialTitle = SHARED_TITLE, initialUrl = SHARED_URL, isStandalone = true))

        composeRule.titleInput().assertIsFocused()
    }

    @Test
    fun `TC-713137bd-b0dc-4b28-ac7f-6fbbe6fae8d4 공유로 화면을 열기만 해서는 웹 항목을 추가하지 않는다`() {
        val viewModel = screenTestViewModel()

        setWebAddScreen(key = WebAddNavKey(initialUrl = SHARED_URL, isStandalone = true), viewModel = viewModel)

        verify(exactly = 0) { viewModel.add(any(), tagIdSet = any()) }
    }

    @Test
    fun `공유로 채운 값을 고친 뒤 화면이 재생성되면 고친 값을 유지한다`() {
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            WebAddScreenTestTheme {
                WebAddScreen(
                    navigateUp = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    addedResultRequestKey = null,
                    initialDetail = WebAddNavKey(initialTitle = SHARED_TITLE, initialUrl = SHARED_URL, isStandalone = true).initialDetail(),
                    componentVisibleProvider = { WebAddScaffoldComponentVisible() },
                    addViewModel = screenTestViewModel(),
                    tagViewModel = addTagScreenTestViewModel(),
                )
            }
        }
        composeRule.titleInput().performTextReplacement(TYPED_TITLE)
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(TYPED_TITLE))
        composeRule.urlInput().assert(hasText(SHARED_URL))
    }

    private fun setWebAddScreen(
        key: WebAddNavKey,
        viewModel: WebAddViewModel = screenTestViewModel(),
    ) {
        composeRule.setContent {
            WebAddScreenTestTheme {
                WebAddScreen(
                    navigateUp = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    addedResultRequestKey = key.requestKey,
                    initialDetail = key.initialDetail(),
                    componentVisibleProvider = { WebAddScaffoldComponentVisible(isNavigateUpButtonVisible = true) },
                    addViewModel = viewModel,
                    tagViewModel = addTagScreenTestViewModel(),
                )
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        const val SHARED_TITLE = "공유한 기사"
        const val SHARED_URL = "https://example.com"
    }
}
