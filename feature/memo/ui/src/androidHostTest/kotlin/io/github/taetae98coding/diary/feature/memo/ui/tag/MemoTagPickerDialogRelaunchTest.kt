package io.github.taetae98coding.diary.feature.memo.ui.tag

import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performTextInput
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.tag.usecase.GetSelectedTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import io.github.taetae98coding.diary.feature.memo.ui.add.MemoAddTagViewModel
import io.github.taetae98coding.diary.feature.memo.ui.resetAndroidUiDispatcher
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoTagPickerDialogRelaunchTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var dialogState: DialogState

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    // 앱을 다시 실행하면 저장해 둔 화면 상태도, 화면 상태를 들고 있던 객체도 없으므로 새 키와 새 인스턴스로 화면을 다시 구성한다.
    @Test
    fun `TC-8f264fc1-17c4-4c95-9bf7-6964b72d6a79 목록이 열린 채 앱을 종료하고 새로 진입하면 목록이 닫힌 상태로 시작한다`() {
        val tagList = listOf(testTag(title = WORK_TAG_TITLE), testTag(title = EXERCISE_TAG_TITLE))
        var launchCount by mutableIntStateOf(0)
        composeRule.setContent {
            key(launchCount) {
                val viewModel = remember { viewModel(tagList = tagList) }
                dialogState = rememberDialogState()
                DiaryTheme {
                    MemoTagPickerDialogHost(
                        dialogState = dialogState,
                        onEvent = { event -> if (event is MemoTagPickerEvent.ChangeQuery) viewModel.updateQuery(query = event.query) },
                        tagPagingItems = viewModel.tagPagingData.collectAsLazyPagingItems(),
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

    // 검색어에 맞는 태그를 고르는 조회 자체는 저장소 테스트가 검증하므로, 검색어를 받으면 그 조건의 결과를 돌려준다.
    private fun viewModel(tagList: List<Tag>): MemoAddTagViewModel {
        val pageTagUseCase = mockk<PageTagUseCase>()
        every { pageTagUseCase(parameter = any()) } answers {
            val query = firstArg<String>()
            flowOf(Result.success(PagingData.from(tagList.filter { tag -> tag.detail.title.contains(query, ignoreCase = true) })))
        }
        val getSelectedTagUseCase = mockk<GetSelectedTagUseCase>()
        every { getSelectedTagUseCase(parameter = any()) } returns flowOf(Result.success(emptyList()))

        return MemoAddTagViewModel(
            initialPrimaryTagId = null,
            pageTagUseCase = pageTagUseCase,
            getSelectedTagUseCase = getSelectedTagUseCase,
        )
    }

    private companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
