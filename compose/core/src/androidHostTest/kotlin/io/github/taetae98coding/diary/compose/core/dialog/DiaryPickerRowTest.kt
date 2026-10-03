package io.github.taetae98coding.diary.compose.core.dialog

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryPickerRowTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `항목은 체크박스 역할의 요소 하나로 읽히고 선택 여부를 상태로 알린다`() {
        var isSelected by mutableStateOf(false)
        composeRule.setContent {
            DiaryTheme {
                DiaryPickerRow(
                    onSelectedChange = { value -> isSelected = value },
                    label = LABEL,
                    modifier = Modifier.testTag(ROW_TEST_TAG),
                    isSelected = isSelected,
                )
            }
        }

        listOf(ToggleableState.Off, ToggleableState.On).forEach { expected ->
            val config = composeRule.onNodeWithTag(ROW_TEST_TAG).fetchSemanticsNode().config

            config.getOrNull(SemanticsProperties.Role) shouldBe Role.Checkbox
            config.getOrNull(SemanticsProperties.ToggleableState) shouldBe expected
            composeRule.onAllNodes(isToggleable()).assertCountEquals(1)

            composeRule.onNodeWithTag(ROW_TEST_TAG).performClick()
            composeRule.waitForIdle()
        }
    }

    @Test
    fun `접근성 이름에 생략하지 않은 전체 이름과 전체 보조 줄을 제공한다`() {
        setPickerRow(label = LONG_LABEL, description = LONG_DESCRIPTION, modifier = Modifier.width(NARROW_WIDTH))

        val textList =
            composeRule
                .onNodeWithTag(ROW_TEST_TAG)
                .fetchSemanticsNode()
                .config
                .getOrNull(SemanticsProperties.Text)
                .orEmpty()
                .map { text -> text.text }

        textList shouldContainExactly listOf(LONG_LABEL, LONG_DESCRIPTION)
    }

    @Test
    fun `보조 줄을 두지 않으면 이름만 읽는다`() {
        setPickerRow(label = LABEL)

        val textList =
            composeRule
                .onNodeWithTag(ROW_TEST_TAG)
                .fetchSemanticsNode()
                .config
                .getOrNull(SemanticsProperties.Text)
                .orEmpty()
                .map { text -> text.text }

        textList shouldContainExactly listOf(LABEL)
    }

    @Test
    fun `자리 표시 항목은 접근성 요소로 두지 않는다`() {
        setPickerRow(label = "", description = "", enabled = false)

        val config = composeRule.onNodeWithTag(ROW_TEST_TAG).fetchSemanticsNode().config

        config.getOrNull(SemanticsProperties.Role).shouldBeNull()
        config.getOrNull(SemanticsProperties.ToggleableState).shouldBeNull()
        config.getOrNull(SemanticsProperties.Text).shouldBeNull()
        composeRule.onAllNodes(isToggleable()).assertCountEquals(0)
        composeRule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text)).assertCountEquals(0)
        composeRule.onAllNodesWithText(LABEL).assertCountEquals(0)
    }

    private fun setPickerRow(
        label: String,
        modifier: Modifier = Modifier,
        description: String? = null,
        enabled: Boolean = true,
    ) {
        composeRule.setContent {
            DiaryTheme {
                DiaryPickerRow(
                    onSelectedChange = {},
                    label = label,
                    modifier = modifier.testTag(ROW_TEST_TAG),
                    description = description,
                    enabled = enabled,
                )
            }
        }
    }

    public companion object {
        private const val ROW_TEST_TAG: String = "DiaryPickerRow"
        private const val LABEL: String = "업무"
        private const val LONG_LABEL: String = "아주 길어서 한 줄에 다 담기지 않고 끝이 생략되는 선택 목록 항목의 이름"
        private const val LONG_DESCRIPTION: String = "아주 길어서 한 줄에 다 담기지 않고 끝이 생략되는 선택 목록 항목의 보조 줄"
        private val NARROW_WIDTH = 120.dp
    }
}
