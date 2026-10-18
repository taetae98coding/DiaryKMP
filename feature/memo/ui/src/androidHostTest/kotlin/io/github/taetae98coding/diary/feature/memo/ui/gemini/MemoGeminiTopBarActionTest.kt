package io.github.taetae98coding.diary.feature.memo.ui.gemini

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.memo.ui.add.MemoAddScaffoldEvent
import io.github.taetae98coding.diary.feature.memo.ui.add.MemoAddTopBar
import io.github.taetae98coding.diary.feature.memo.ui.detail.MemoDetailScaffoldEvent
import io.github.taetae98coding.diary.feature.memo.ui.detail.MemoDetailTopBar
import io.github.taetae98coding.diary.feature.memo.ui.detail.MemoDetailUiState
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoGeminiTopBarActionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-GEMINI-FEATURE-003 설정을 확인하지 못하면 시작 동작을 표시하지 않는다`() {
        setTopBar(isButtonVisible = false)

        composeRule.onNodeWithContentDescription(DEFAULT_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `설정을 확인하면 시작 동작을 표시한다`() {
        setTopBar(isButtonVisible = true)

        composeRule.onNodeWithContentDescription(DEFAULT_BUTTON_DESCRIPTION).assertExists()
    }

    @Test
    fun `설정이 갖춰지지 않아도 누를 수 있다`() {
        var clickCount = 0
        setTopBar(
            isButtonVisible = true,
            onEvent = { event ->
                if (event == MemoAddScaffoldEvent.ClickGemini) {
                    clickCount += 1
                }
            },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        clickCount shouldBe 1
    }

    @Test
    fun `TC-MEMO-GEMINI-FEATURE-003 MemoDetail에서 설정을 확인하지 못하면 시작 동작을 표시하지 않는다`() {
        setDetailTopBar(isButtonVisible = false)

        composeRule.onNodeWithContentDescription(DEFAULT_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `MemoDetail에서 설정을 확인하면 시작 동작을 표시한다`() {
        setDetailTopBar(isButtonVisible = true)

        composeRule.onNodeWithContentDescription(DEFAULT_BUTTON_DESCRIPTION).assertExists()
    }

    @Test
    fun `MemoDetail에서 설정이 갖춰지지 않아도 누를 수 있다`() {
        var clickCount = 0
        setDetailTopBar(
            isButtonVisible = true,
            onEvent = { event ->
                if (event == MemoDetailScaffoldEvent.ClickGemini) {
                    clickCount += 1
                }
            },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        clickCount shouldBe 1
    }

    private fun setDetailTopBar(
        isButtonVisible: Boolean,
        onEvent: (MemoDetailScaffoldEvent) -> Unit = {},
    ) {
        val content = fixtureMonkey.giveMeOne<MemoDetailUiState.Content>()
        composeRule.setContent {
            DiaryTheme {
                MemoDetailTopBar(
                    onEvent = onEvent,
                    uiStateProvider = { content },
                    geminiUiStateProvider = { MemoGeminiUiState(isButtonVisible = isButtonVisible) },
                )
            }
        }
    }

    private fun setTopBar(
        isButtonVisible: Boolean,
        onEvent: (MemoAddScaffoldEvent) -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                MemoAddTopBar(
                    onEvent = onEvent,
                    geminiUiStateProvider = { MemoGeminiUiState(isButtonVisible = isButtonVisible) },
                )
            }
        }
    }

    private companion object {
        private const val DEFAULT_BUTTON_DESCRIPTION = "Writing assistant"
    }
}
