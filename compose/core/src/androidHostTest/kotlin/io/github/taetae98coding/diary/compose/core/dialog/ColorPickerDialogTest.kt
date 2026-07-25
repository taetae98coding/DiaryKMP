package io.github.taetae98coding.diary.compose.core.dialog

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ColorPickerDialogTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-DIARY-COLOR-INPUT-FEATURE-004 슬라이더를 움직이면 Hex 입력 칸이 함께 갱신된다`() {
        setColorPickerDialog(initialColor = Color.Black)

        composeRule
            .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.SetProgress))[0]
            .performSemanticsAction(SemanticsActions.SetProgress) { it(CHANNEL_MAX) }
        composeRule.waitForIdle()

        composeRule.onNode(hasSetTextAction()).assert(hasText(RED_HEX))
    }

    @Test
    fun `TC-DIARY-COLOR-INPUT-FEATURE-008 무작위 컬러 버튼을 누르면 편집 컬러가 무작위 컬러로 갱신된다`() {
        setColorPickerDialog(initialColor = Color.Black)

        composeRule.onNodeWithContentDescription(DEFAULT_RANDOM_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        val hexText =
            composeRule
                .onNode(hasSetTextAction())
                .fetchSemanticsNode()
                .config[SemanticsProperties.EditableText]
                .text

        HEX_REGEX.matches(hexText) shouldBe true

        val rgb = hexText.removePrefix("#").toInt(radix = 16)
        assertChannelSliders(
            red = (rgb shr 16 and 0xFF).toFloat(),
            green = (rgb shr 8 and 0xFF).toFloat(),
            blue = (rgb and 0xFF).toFloat(),
        )
    }

    @Test
    fun `TC-DIARY-COLOR-INPUT-DOMAIN-001 유효한 Hex 입력만 편집 컬러에 반영한다`() {
        setColorPickerDialog(initialColor = BASE_COLOR)

        listOf(
            "#3A7BD5" to TYPED_CHANNELS,
            "3A7BD5" to TYPED_CHANNELS,
            "#3a7bd5" to TYPED_CHANNELS,
            "#3A7BD" to BASE_CHANNELS,
            "#3A7BD5F" to BASE_CHANNELS,
            "#GGGGGG" to BASE_CHANNELS,
            "" to BASE_CHANNELS,
        ).forEach { (input, channels) ->
            composeRule.onNode(hasSetTextAction()).performTextReplacement(BASE_HEX)
            composeRule.waitForIdle()

            if (input.isEmpty()) {
                composeRule.onNode(hasSetTextAction()).performTextClearance()
            } else {
                composeRule.onNode(hasSetTextAction()).performTextReplacement(input)
            }
            composeRule.waitForIdle()

            assertChannelSliders(red = channels[0], green = channels[1], blue = channels[2])
        }
    }

    private fun assertChannelSliders(
        red: Float,
        green: Float,
        blue: Float,
    ) {
        val values =
            composeRule
                .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.SetProgress))
                .fetchSemanticsNodes()
                .map { it.config[SemanticsProperties.ProgressBarRangeInfo].current }

        values shouldContainExactly listOf(red, green, blue)
    }

    private fun setColorPickerDialog(initialColor: Color) {
        composeRule.setContent {
            DiaryTheme {
                ColorPickerDialog(
                    state = rememberColorPickerState(initialColor = initialColor),
                    onDismissRequest = {},
                    onConfirm = {},
                )
            }
        }
    }

    public companion object {
        private const val CHANNEL_MAX = 255F
        private const val RED_HEX = "#FF0000"
        private const val BASE_HEX = "#102030"
        private const val DEFAULT_RANDOM_DESCRIPTION = "Random color"
        private val BASE_COLOR = Color(color = 0xFF102030.toInt())
        private val BASE_CHANNELS = listOf(16F, 32F, 48F)
        private val TYPED_CHANNELS = listOf(58F, 123F, 213F)
        private val HEX_REGEX = Regex(pattern = "#[0-9A-F]{6}")
    }
}
