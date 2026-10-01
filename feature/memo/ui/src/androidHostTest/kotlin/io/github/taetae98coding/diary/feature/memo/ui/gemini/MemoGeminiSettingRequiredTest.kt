package io.github.taetae98coding.diary.feature.memo.ui.gemini

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.feature.memo.ui.TEST_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.memo.ui.add.MemoAddScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.memo.ui.add.MemoAddScreen
import io.github.taetae98coding.diary.feature.memo.ui.add.MemoAddScreenTestTheme
import io.github.taetae98coding.diary.feature.memo.ui.add.screenTestViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceMapViewModel
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoGeminiSettingRequiredTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-GEMINI-FEATURE-002 기본 환경에서 설정이 갖춰지지 않으면 열지 않고 설정 필요를 알린다`() {
        assertSettingRequired(buttonDescription = DEFAULT_BUTTON_DESCRIPTION, expectedMessage = DEFAULT_SETTING_REQUIRED_MESSAGE, dialogTitle = DEFAULT_DIALOG_TITLE)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-MEMO-GEMINI-FEATURE-002 한국어 환경에서 설정이 갖춰지지 않으면 열지 않고 설정 필요를 알린다`() {
        assertSettingRequired(buttonDescription = KOREAN_BUTTON_DESCRIPTION, expectedMessage = KOREAN_SETTING_REQUIRED_MESSAGE, dialogTitle = KOREAN_DIALOG_TITLE)
    }

    private fun assertSettingRequired(
        buttonDescription: String,
        expectedMessage: String,
        dialogTitle: String,
    ) {
        val effect = Channel<MemoGeminiEffect>(capacity = Channel.BUFFERED)
        val geminiViewModel = screenTestGeminiViewModel(uiState = MutableStateFlow(MemoGeminiUiState(isButtonVisible = true)))
        every { geminiViewModel.effect } returns effect.receiveAsFlow()
        every { geminiViewModel.open() } answers { effect.trySend(MemoGeminiEffect.SettingRequired).getOrThrow() }
        val viewModels = screenTestViewModel()
        composeRule.setContent {
            MemoAddScreenTestTheme {
                MemoAddScreen(
                    navigateUp = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    navigateToWebAdd = {},
                    navigateToWebDetail = {},
                    navigateToContactAdd = {},
                    navigateToContactDetail = {},
                    navigateToPlaceAdd = {},
                    navigateToPlaceDetail = {},
                    initialDateTime = null,
                    addRequestKey = TEST_ADD_REQUEST_KEY,
                    componentVisibleProvider = { MemoAddScaffoldComponentVisible() },
                    isStandalone = true,
                    addViewModel = viewModels.viewModel,
                    tagViewModel = viewModels.tagViewModel,
                    webViewModel = viewModels.webViewModel,
                    contactViewModel = viewModels.contactViewModel,
                    placeViewModel = viewModels.placeViewModel,
                    placeMapViewModel = screenTestPlaceMapViewModel(),
                    geminiViewModel = geminiViewModel,
                )
            }
        }

        composeRule.onNodeWithContentDescription(buttonDescription).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { geminiViewModel.open() }
        verify(exactly = 0) { geminiViewModel.generate(any()) }
        composeRule.onNodeWithText(expectedMessage).assertExists()
        composeRule.onNodeWithText(dialogTitle).assertDoesNotExist()
    }

    private companion object {
        private const val DEFAULT_BUTTON_DESCRIPTION = "Writing assistant"
        private const val DEFAULT_SETTING_REQUIRED_MESSAGE = "Please finish Gemini settings first."
        private const val DEFAULT_DIALOG_TITLE = "Writing Assistant"
        private const val KOREAN_BUTTON_DESCRIPTION = "작성 도우미"
        private const val KOREAN_SETTING_REQUIRED_MESSAGE = "Gemini 설정을 먼저 완료해 주세요."
        private const val KOREAN_DIALOG_TITLE = "작성 도우미"
    }
}
