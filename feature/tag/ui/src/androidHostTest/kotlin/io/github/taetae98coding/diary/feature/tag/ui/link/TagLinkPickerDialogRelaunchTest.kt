package io.github.taetae98coding.diary.feature.tag.ui.link

import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performTextInput
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.tag.ui.resetAndroidUiDispatcher
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagLinkPickerDialogRelaunchTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var dialogState: DialogState

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    // 앱을 다시 실행하면 저장해 둔 화면 상태도, 화면 상태를 들고 있던 객체도 없으므로 새 키와 새 인스턴스로 화면을 다시 구성한다.
    @Test
    fun `TC-79d2f30c-9e3a-499d-b598-61b48d4aee7f 목록이 열린 채 앱을 종료하고 새로 진입하면 목록이 닫힌 상태로 시작한다`() {
        val tagList = listOf(testTag(title = WORK_TAG_TITLE), testTag(title = EXERCISE_TAG_TITLE))
        var launchCount by mutableIntStateOf(0)
        composeRule.setContent {
            key(launchCount) {
                var query by remember { mutableStateOf("") }
                val tagPagingItems =
                    remember(query) {
                        MutableStateFlow(tagPagingDataOf(tagList.filter { tag -> tag.detail.title.contains(query, ignoreCase = true) }))
                    }.collectAsLazyPagingItems()
                dialogState = rememberDialogState()
                DiaryTheme {
                    TagLinkPickerDialogHost(
                        dialogState = dialogState,
                        onEvent = { event -> if (event is TagLinkPickerEvent.ChangeQuery) query = event.query },
                        tagPagingItems = tagPagingItems,
                    )
                }
            }
        }
        composeRule.runOnIdle { dialogState.show() }
        awaitDialogTextCount(text = EXERCISE_TAG_TITLE, count = 1)
        composeRule.dialogSearchField().performTextInput(WORK_TAG_QUERY)
        awaitDialogTextCount(text = EXERCISE_TAG_TITLE, count = 0)

        composeRule.runOnIdle { launchCount += 1 }
        composeRule.waitForIdle()

        dialogState.isVisible shouldBe false
        composeRule.onNode(isDialog()).assertDoesNotExist()

        composeRule.runOnIdle { dialogState.show() }
        awaitDialogTextCount(text = EXERCISE_TAG_TITLE, count = 1)
        composeRule.dialogSearchField().assert(hasText(""))
    }

    private fun awaitDialogTextCount(
        text: String,
        count: Int,
    ) {
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodes(hasText(text) and hasAnyAncestor(isDialog())).fetchSemanticsNodes().size == count
        }
    }

    private companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
