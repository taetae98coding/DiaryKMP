package io.github.taetae98coding.diary.compose.core.listitem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
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
class DiaryCheckableSegmentedListItemTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `켜고 끄는 항목은 체크박스로 알리고 켜짐 상태를 알린다`() {
        setCheckableGroup()

        composeRule
            .onNodeWithText(FIRST_LABEL, useUnmergedTree = false)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Checkbox))
            .assertIsOn()
        composeRule.onNodeWithText(SECOND_LABEL).assertIsOff()
    }

    @Test
    fun `항목을 누르면 바뀔 켜짐 상태를 전달한다`() {
        val changes = mutableListOf<Pair<String, Boolean>>()
        setCheckableGroup(onCheckedChange = { label, checked -> changes.add(label to checked) })

        composeRule.onNodeWithText(FIRST_LABEL).performClick()
        composeRule.onNodeWithText(SECOND_LABEL).performClick()
        composeRule.waitForIdle()

        changes shouldBe listOf(FIRST_LABEL to false, SECOND_LABEL to true)
    }

    @Test
    fun `보조 문구를 함께 표시한다`() {
        setCheckableGroup()

        composeRule.onNodeWithText(SUPPORTING_TEXT, substring = true).assertExists()
    }

    @Test
    fun `체크박스는 따로 누르는 대상으로 두지 않는다`() {
        setCheckableGroup()

        composeRule.onAllNodes(hasClickAction()).fetchSemanticsNodes().size shouldBe 2
    }

    private fun setCheckableGroup(onCheckedChange: (String, Boolean) -> Unit = { _, _ -> }) {
        composeRule.setContent {
            DiaryTheme {
                Column(verticalArrangement = Arrangement.spacedBy(DiarySegmentedListItemDefaults.Gap)) {
                    DiaryCheckableSegmentedListItem(
                        checked = true,
                        onCheckedChange = { checked -> onCheckedChange(FIRST_LABEL, checked) },
                        index = 0,
                        count = 2,
                        supportingContent = { Text(text = SUPPORTING_TEXT) },
                    ) {
                        Text(text = FIRST_LABEL)
                    }
                    DiaryCheckableSegmentedListItem(
                        onCheckedChange = { checked -> onCheckedChange(SECOND_LABEL, checked) },
                        index = 1,
                        count = 2,
                    ) {
                        Text(text = SECOND_LABEL)
                    }
                }
            }
        }
    }

    public companion object {
        private const val FIRST_LABEL = "기기값"
        private const val SECOND_LABEL = "미국"
        private const val SUPPORTING_TEXT = "한국"
    }
}
