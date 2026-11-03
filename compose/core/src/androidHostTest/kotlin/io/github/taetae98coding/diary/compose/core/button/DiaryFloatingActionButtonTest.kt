package io.github.taetae98coding.diary.compose.core.button

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.library.fixturemonkey.nonBlankString
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

private val floatingButtons: List<@Composable (onClick: () -> Unit, contentDescription: String, isInProgress: Boolean) -> Unit> =
    listOf(
        { onClick, contentDescription, isInProgress ->
            FloatingAddButton(onClick = onClick, contentDescription = contentDescription, isInProgressProvider = { isInProgress })
        },
        { onClick, contentDescription, isInProgress ->
            FloatingCheckButton(onClick = onClick, contentDescription = contentDescription, isInProgressProvider = { isInProgress })
        },
        { onClick, contentDescription, isInProgress ->
            FloatingUploadButton(onClick = onClick, contentDescription = contentDescription, isInProgressProvider = { isInProgress })
        },
    )

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryFloatingActionButtonTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `떠 있는 버튼을 누르면 동작을 한 번 실행한다`() {
        val contentDescriptions = List(floatingButtons.size) { fixtureMonkey.nonBlankString() }
        val clickCounts = IntArray(floatingButtons.size)
        composeRule.setContent {
            DiaryTheme {
                Column {
                    floatingButtons.forEachIndexed { index, button ->
                        button({ clickCounts[index] += 1 }, contentDescriptions[index], false)
                    }
                }
            }
        }

        floatingButtons.indices.forEach { index ->
            composeRule.onNodeWithContentDescription(contentDescriptions[index]).performClick()
        }

        clickCounts.toList() shouldBe List(floatingButtons.size) { 1 }
    }

    @Test
    fun `진행 중인 떠 있는 버튼은 이름을 유지하고 진행 표시를 보인다`() {
        val contentDescriptions = List(floatingButtons.size) { fixtureMonkey.nonBlankString() }
        composeRule.setContent {
            DiaryTheme {
                Column {
                    floatingButtons.forEachIndexed { index, button ->
                        button({}, contentDescriptions[index], true)
                    }
                }
            }
        }

        floatingButtons.indices.forEach { index ->
            composeRule.onNodeWithContentDescription(contentDescriptions[index]).assertHasClickAction()
            composeRule.onNode(progressIn(contentDescriptions[index]), useUnmergedTree = true).assertExists()
        }
    }

    @Test
    fun `진행 중이 아닌 떠 있는 버튼은 진행 표시를 보이지 않는다`() {
        val contentDescriptions = List(floatingButtons.size) { fixtureMonkey.nonBlankString() }
        composeRule.setContent {
            DiaryTheme {
                Column {
                    floatingButtons.forEachIndexed { index, button ->
                        button({}, contentDescriptions[index], false)
                    }
                }
            }
        }

        floatingButtons.indices.forEach { index ->
            composeRule.onNode(progressIn(contentDescriptions[index]), useUnmergedTree = true).assertDoesNotExist()
        }
    }

    private fun progressIn(contentDescription: String): SemanticsMatcher = hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate) and hasAnyAncestor(hasContentDescription(contentDescription))
}
