package io.github.taetae98coding.diary.compose.core.appbar

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasStateDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryTopBarTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `아이콘 버튼을 누르면 동작을 한 번 실행한다`() {
        val contentDescription = fixtureMonkey.nonBlankString()
        var clickCount = 0
        composeRule.setContent {
            DiaryTheme {
                DiaryTopBar(
                    actions =
                        listOf(
                            DiaryTopBarAction.Icon(
                                icon = DiaryTopBarIcon.DELETE,
                                contentDescription = contentDescription,
                                onClick = { clickCount += 1 },
                            ),
                        ),
                )
            }
        }

        composeRule.onNodeWithContentDescription(contentDescription).performClick()
        composeRule.waitForIdle()

        clickCount shouldBe 1
    }

    @Test
    fun `진행 중인 아이콘 버튼은 진행 표시로 바뀌어도 버튼 이름을 유지한다`() {
        val contentDescription = fixtureMonkey.nonBlankString()
        composeRule.setContent {
            DiaryTheme {
                DiaryTopBar(
                    actions =
                        listOf(
                            DiaryTopBarAction.Icon(
                                icon = DiaryTopBarIcon.DELETE,
                                contentDescription = contentDescription,
                                onClick = {},
                                isInProgress = true,
                            ),
                        ),
                )
            }
        }

        composeRule
            .onNode(
                hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate) and hasAnyAncestor(hasContentDescription(contentDescription)),
                useUnmergedTree = true,
            ).assertExists()
    }

    @Test
    fun `비활성 아이콘 버튼은 누를 수 없다`() {
        val contentDescription = fixtureMonkey.nonBlankString()
        var isEnabled by mutableStateOf(false)
        composeRule.setContent {
            DiaryTheme {
                DiaryTopBar(
                    actions =
                        listOf(
                            DiaryTopBarAction.Icon(
                                icon = DiaryTopBarIcon.UNDO,
                                contentDescription = contentDescription,
                                onClick = {},
                                isEnabled = isEnabled,
                            ),
                        ),
                )
            }
        }

        composeRule.onNodeWithContentDescription(contentDescription).assertIsNotEnabled()

        composeRule.runOnIdle { isEnabled = true }

        composeRule.onNodeWithContentDescription(contentDescription).assertIsEnabled()
    }

    @Test
    fun `표시하지 않기로 한 아이콘 버튼은 사라진다`() {
        val contentDescription = fixtureMonkey.nonBlankString()
        var isVisible by mutableStateOf(true)
        composeRule.setContent {
            DiaryTheme {
                DiaryTopBar(
                    actions =
                        listOf(
                            DiaryTopBarAction.Icon(
                                icon = DiaryTopBarIcon.OPEN_IN_NEW,
                                contentDescription = contentDescription,
                                onClick = {},
                                isVisible = isVisible,
                            ),
                        ),
                )
            }
        }

        composeRule.onNodeWithContentDescription(contentDescription).assertExists()

        composeRule.runOnIdle { isVisible = false }

        composeRule.onNodeWithContentDescription(contentDescription).assertDoesNotExist()
    }

    @Test
    fun `켬끔 버튼은 켜짐 여부를 켬끔 상태로 알린다`() {
        val contentDescription = fixtureMonkey.nonBlankString()
        var isChecked by mutableStateOf(false)
        composeRule.setContent {
            DiaryTheme {
                DiaryTopBar(
                    actions =
                        listOf(
                            DiaryTopBarAction.Toggle(
                                icon = DiaryTopBarIcon.FINISH,
                                contentDescription = contentDescription,
                                isChecked = isChecked,
                                onClick = {},
                            ),
                        ),
                )
            }
        }

        composeRule.onNodeWithContentDescription(contentDescription).assertIsOff()

        composeRule.runOnIdle { isChecked = true }

        composeRule.onNodeWithContentDescription(contentDescription).assertIsOn()
    }

    @Test
    fun `적용 상태 버튼은 적용되었을 때만 적용 상태 문구를 알린다`() {
        val contentDescription = fixtureMonkey.nonBlankString()
        val appliedStateDescription = fixtureMonkey.nonBlankString()
        var isApplied by mutableStateOf(false)
        composeRule.setContent {
            DiaryTheme {
                DiaryTopBar(
                    actions =
                        listOf(
                            DiaryTopBarAction.Applied(
                                icon = DiaryTopBarIcon.FILTER,
                                contentDescription = contentDescription,
                                isApplied = isApplied,
                                appliedStateDescription = appliedStateDescription,
                                onClick = {},
                            ),
                        ),
                )
            }
        }

        composeRule
            .onNodeWithContentDescription(contentDescription)
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.StateDescription))

        composeRule.runOnIdle { isApplied = true }

        composeRule.onNode(hasContentDescription(contentDescription) and hasStateDescription(appliedStateDescription)).assertExists()
    }

    @Test
    fun `글자 버튼은 보이는 글자와 함께 접근성 이름으로 알린다`() {
        val text = fixtureMonkey.nonBlankString()
        val contentDescription = fixtureMonkey.nonBlankString()
        var clickCount = 0
        composeRule.setContent {
            DiaryTheme {
                DiaryTopBar(
                    actions =
                        listOf(
                            DiaryTopBarAction.Text(
                                text = text,
                                contentDescription = contentDescription,
                                onClick = { clickCount += 1 },
                            ),
                        ),
                )
            }
        }

        composeRule.onNodeWithText(text).assertExists()
        composeRule.onNodeWithContentDescription(contentDescription).performClick()
        composeRule.waitForIdle()

        clickCount shouldBe 1
    }

    @Test
    fun `글자 제목은 보조 제목과 함께 표시한다`() {
        val title = fixtureMonkey.nonBlankString()
        val subtitle = fixtureMonkey.nonBlankString()
        composeRule.setContent {
            DiaryTheme {
                DiaryTopBar(title = DiaryTopBarTitle.Text(text = title, subtitle = subtitle, isMarquee = true))
            }
        }

        composeRule.onNodeWithText(title).assertExists()
        composeRule.onNodeWithText(subtitle).assertExists()
    }

    @Test
    fun `누를 수 있는 제목은 펼침 여부에 맞는 화살표 이름을 알리고 누르면 동작을 실행한다`() {
        val title = fixtureMonkey.nonBlankString()
        val expandContentDescription = fixtureMonkey.nonBlankString()
        val collapseContentDescription = fixtureMonkey.nonBlankString()
        var isExpanded by mutableStateOf(false)
        var clickCount = 0
        composeRule.setContent {
            DiaryTheme {
                DiaryTopBar(
                    title =
                        DiaryTopBarTitle.Button(
                            text = title,
                            isExpanded = isExpanded,
                            expandContentDescription = expandContentDescription,
                            collapseContentDescription = collapseContentDescription,
                            onClick = { clickCount += 1 },
                        ),
                )
            }
        }

        composeRule.onNodeWithContentDescription(expandContentDescription, useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText(title, useUnmergedTree = true).performClick()
        composeRule.runOnIdle { isExpanded = true }

        composeRule.onNodeWithContentDescription(collapseContentDescription, useUnmergedTree = true).assertExists()
        clickCount shouldBe 1
    }

    @Test
    fun `검색 제목에 입력한 글자는 검색어 상태에 반영된다`() {
        val placeholder = fixtureMonkey.nonBlankString()
        val query = fixtureMonkey.nonBlankString()
        val state = TextFieldState()
        composeRule.setContent {
            DiaryTheme {
                DiaryTopBar(
                    title =
                        DiaryTopBarTitle.Search(
                            state = state,
                            placeholder = placeholder,
                            focusRequester = remember { FocusRequester() },
                        ),
                )
            }
        }

        composeRule.onNode(hasSetTextAction()).performTextInput(query)
        composeRule.waitForIdle()

        state.text.toString() shouldBe query
    }
}
