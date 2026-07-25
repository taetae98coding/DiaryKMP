package io.github.taetae98coding.diary.compose.core.listitem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiarySegmentedListItemTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `항목 전체를 눌러 클릭을 전달한다`() {
        var clickCount = 0
        composeRule.setContent {
            DiaryTheme {
                DiarySegmentedListItem(onClick = { clickCount += 1 }) {
                    Text(text = FIRST_LABEL)
                }
            }
        }

        composeRule.onNodeWithText(FIRST_LABEL).performClick()
        composeRule.waitForIdle()

        clickCount shouldBe 1
    }

    @Test
    fun `하나만 고르는 묶음은 선택한 항목만 선택 상태로 알린다`() {
        setSelectableGroup()

        composeRule.onNodeWithText(FIRST_LABEL).assertIsSelected()
        composeRule.onNodeWithText(SECOND_LABEL).assertIsNotSelected()
    }

    @Test
    fun `하나만 고르는 묶음의 항목은 단일 선택 항목으로 알린다`() {
        setSelectableGroup()

        composeRule
            .onNodeWithText(FIRST_LABEL)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton))
    }

    @Test
    fun `선택 컨트롤은 따로 누르는 대상으로 두지 않는다`() {
        setSelectableGroup()

        composeRule.onAllNodes(hasClickAction()).fetchSemanticsNodes().size shouldBe 2
    }

    @Test
    fun `선택되어 있지 않은 항목을 누르면 그 항목의 선택을 요청한다`() {
        val clickedLabels = mutableListOf<String>()
        setSelectableGroup(onClick = { label -> clickedLabels.add(label) })

        composeRule.onNodeWithText(SECOND_LABEL).performClick()
        composeRule.waitForIdle()

        clickedLabels shouldBe listOf(SECOND_LABEL)
    }

    private fun setSelectableGroup(onClick: (String) -> Unit = {}) {
        composeRule.setContent {
            DiaryTheme {
                Column(
                    modifier = Modifier.selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(DiarySegmentedListItemDefaults.Gap),
                ) {
                    listOf(FIRST_LABEL, SECOND_LABEL).forEachIndexed { index, label ->
                        DiarySelectableSegmentedListItem(
                            onClick = { onClick(label) },
                            selected = index == 0,
                            index = index,
                            count = 2,
                        ) {
                            Text(text = label)
                        }
                    }
                }
            }
        }
    }

    public companion object {
        private const val FIRST_LABEL = "네이버"
        private const val SECOND_LABEL = "Google"
    }
}
