package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryColorInputTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-DIARY-COLOR-INPUT-FEATURE-001 카드에 초기 컬러의 Hex 코드를 표시한다`() {
        setDiaryColorInput(initialColor = TYPED_COLOR)

        composeRule.onNode(hasText(TYPED_HEX, substring = true) and hasClickAction()).assertExists()
    }

    @Test
    fun `TC-DIARY-COLOR-INPUT-FEATURE-009 카드에 초기 컬러의 RGB 값을 둘째 줄에 표시한다`() {
        setDiaryColorInput(initialColor = TYPED_COLOR)

        composeRule.onNode(hasText(TYPED_HEX) and hasText(TYPED_RGB) and hasClickAction()).assertExists()
    }

    @Test
    fun `TC-DIARY-COLOR-INPUT-FEATURE-007 초기 컬러를 지정하지 않으면 무작위 컬러의 Hex 코드를 표시한다`() {
        composeRule.setContent {
            DiaryTheme {
                Surface {
                    DiaryColorInput(state = rememberDiaryColorInputState())
                }
            }
        }

        composeRule.onNode(hasHexText() and hasClickAction()).assertExists()
    }

    @Test
    fun `TC-DIARY-COLOR-INPUT-FEATURE-002 카드를 누르면 현재 컬러가 반영된 컬러 선택 다이얼로그가 열린다`() {
        setDiaryColorInput(initialColor = TYPED_COLOR)

        composeRule.onNode(hasText(TYPED_HEX, substring = true) and hasClickAction()).performClick()
        composeRule.waitForIdle()

        composeRule.onNode(hasSetTextAction()).assert(hasText(TYPED_HEX))
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo(current = TYPED_RED, range = 0F..255F, steps = 0))).assertExists()
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo(current = TYPED_GREEN, range = 0F..255F, steps = 0))).assertExists()
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo(current = TYPED_BLUE, range = 0F..255F, steps = 0))).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_RANDOM_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(DEFAULT_CANCEL).assertExists()
        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertExists()
        composeRule.onNodeWithText(REMOVED_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-DIARY-COLOR-INPUT-FEATURE-005 확인을 누르면 다이얼로그가 닫히고 카드 컬러가 바뀐다`() {
        setDiaryColorInput(initialColor = Color.Black)

        composeRule.onNode(hasText(BLACK_HEX, substring = true) and hasClickAction()).performClick()
        composeRule.onNode(hasSetTextAction()).performTextReplacement(TYPED_HEX)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_CONFIRM).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertDoesNotExist()
        composeRule.onNode(hasText(TYPED_HEX) and hasText(TYPED_RGB) and hasClickAction()).assertExists()
    }

    @Test
    fun `TC-DIARY-COLOR-INPUT-FEATURE-006 취소하면 카드는 기존 컬러를 유지한다`() {
        setDiaryColorInput(initialColor = TYPED_COLOR)

        composeRule.onNode(hasText(TYPED_HEX, substring = true) and hasClickAction()).performClick()
        composeRule.onNode(hasSetTextAction()).performTextReplacement(RED_HEX)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_CANCEL).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_CANCEL).assertDoesNotExist()
        composeRule.onNodeWithText(RED_HEX).assertDoesNotExist()
        composeRule.onNode(hasText(TYPED_HEX, substring = true) and hasClickAction()).assertExists()

        composeRule.onNode(hasText(TYPED_HEX, substring = true) and hasClickAction()).performClick()
        composeRule.waitForIdle()

        composeRule.onNode(hasSetTextAction()).assert(hasText(TYPED_HEX))
    }

    @Test
    fun `TC-DIARY-COLOR-INPUT-DOMAIN-002 유효하지 않은 Hex 입력이 남아 있어도 확인 시 마지막 유효 컬러가 적용된다`() {
        setDiaryColorInput(initialColor = Color.Black)

        composeRule.onNode(hasText(BLACK_HEX, substring = true) and hasClickAction()).performClick()
        composeRule.onNode(hasSetTextAction()).performTextReplacement(TYPED_HEX)
        composeRule.waitForIdle()
        composeRule.onNode(hasSetTextAction()).performTextReplacement(INVALID_HEX)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_CONFIRM).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertDoesNotExist()
        composeRule.onNode(hasText(TYPED_HEX, substring = true) and hasClickAction()).assertExists()
    }

    @Test
    fun `TC-DIARY-COLOR-INPUT-DOMAIN-003 화면이 재생성되어도 선택된 컬러가 유지된다`() {
        val restorationTester = StateRestorationTester(composeRule)
        setDiaryColorInput(restorationTester = restorationTester, initialColor = Color.Black)

        composeRule.onNode(hasText(BLACK_HEX, substring = true) and hasClickAction()).performClick()
        composeRule.onNode(hasSetTextAction()).performTextReplacement(TYPED_HEX)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_CONFIRM).performClick()
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNode(hasText(TYPED_HEX, substring = true) and hasClickAction()).assertExists()
    }

    @Test
    fun `TC-DIARY-COLOR-INPUT-DOMAIN-004 화면이 재생성되어도 열린 다이얼로그와 편집 컬러가 유지된다`() {
        val restorationTester = StateRestorationTester(composeRule)
        setDiaryColorInput(restorationTester = restorationTester, initialColor = Color.Black)

        composeRule.onNode(hasText(BLACK_HEX, substring = true) and hasClickAction()).performClick()
        composeRule.onNode(hasSetTextAction()).performTextReplacement(TYPED_HEX)
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertExists()
        composeRule.onNode(hasSetTextAction()).assert(hasText(TYPED_HEX))
    }

    private fun setDiaryColorInput(
        restorationTester: StateRestorationTester? = null,
        initialColor: Color,
    ) {
        val content: @Composable () -> Unit = {
            DiaryTheme {
                Surface {
                    DiaryColorInput(state = rememberDiaryColorInputState(initialColor = initialColor))
                }
            }
        }

        if (restorationTester == null) {
            composeRule.setContent(content)
        } else {
            restorationTester.setContent(content)
        }
    }

    public companion object {
        private const val TYPED_RED = 58F
        private const val TYPED_GREEN = 123F
        private const val TYPED_BLUE = 213F
        private const val TYPED_HEX = "#3A7BD5"
        private const val TYPED_RGB = "RGB(58, 123, 213)"
        private const val BLACK_HEX = "#000000"
        private const val RED_HEX = "#FF0000"
        private const val INVALID_HEX = "#GGGGGG"
        private const val DEFAULT_CANCEL = "Cancel"
        private const val DEFAULT_CONFIRM = "Confirm"
        private const val DEFAULT_RANDOM_DESCRIPTION = "Random color"
        private const val REMOVED_TITLE = "Select Color"

        private val TYPED_COLOR = Color(color = 0xFF3A7BD5.toInt())
        private val HEX_REGEX = Regex(pattern = "#[0-9A-F]{6}")

        private fun hasHexText(): SemanticsMatcher =
            SemanticsMatcher("has #RRGGBB text") { node ->
                node.config.getOrNull(SemanticsProperties.Text)?.any { HEX_REGEX.containsMatchIn(it.text) } == true
            }
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryColorInputTextTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `기본 환경 다이얼로그 문구를 표시한다`() {
        assertDialogText(cancel = DEFAULT_CANCEL, confirm = DEFAULT_CONFIRM, random = DEFAULT_RANDOM_DESCRIPTION)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 다이얼로그 문구를 표시한다`() {
        assertDialogText(cancel = KOREAN_CANCEL, confirm = KOREAN_CONFIRM, random = KOREAN_RANDOM_DESCRIPTION)
    }

    private fun assertDialogText(
        cancel: String,
        confirm: String,
        random: String,
    ) {
        composeRule.setContent {
            DiaryTheme {
                Surface {
                    DiaryColorInput(state = rememberDiaryColorInputState(initialColor = TYPED_COLOR))
                }
            }
        }

        composeRule.onNode(hasText(TYPED_HEX, substring = true) and hasClickAction()).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(cancel).assertExists()
        composeRule.onNodeWithText(confirm).assertExists()
        composeRule.onNodeWithContentDescription(random).assertExists()
    }

    public companion object {
        private const val TYPED_HEX = "#3A7BD5"
        private const val DEFAULT_CANCEL = "Cancel"
        private const val DEFAULT_CONFIRM = "Confirm"
        private const val DEFAULT_RANDOM_DESCRIPTION = "Random color"
        private const val KOREAN_CANCEL = "취소"
        private const val KOREAN_CONFIRM = "확인"
        private const val KOREAN_RANDOM_DESCRIPTION = "무작위 컬러"

        private val TYPED_COLOR = Color(color = 0xFF3A7BD5.toInt())
    }
}
