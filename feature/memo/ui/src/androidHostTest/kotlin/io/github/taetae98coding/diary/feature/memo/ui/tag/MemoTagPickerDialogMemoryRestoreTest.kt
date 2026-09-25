package io.github.taetae98coding.diary.feature.memo.ui.tag

import androidx.compose.runtime.remember
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performTextInput
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
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
class MemoTagPickerDialogMemoryRestoreTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val requestedQueryList = mutableListOf<MutableList<String>>()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-DOMAIN-022 메모리 정리 뒤 복원하면 목록과 검색어가 다시 나타나고 기다리지 않고 그 검색어로 좁힌 목록을 보여 준다`() {
        val workTag = testTag(title = WORK_TAG_TITLE)
        val exerciseTag = testTag(title = EXERCISE_TAG_TITLE)
        val tagList = listOf(workTag, exerciseTag)
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            // 메모리 정리 뒤에는 검색어를 반영하던 상태 객체도 새로 만들어지므로, 복원할 때마다 새 인스턴스를 쓴다.
            val viewModel = remember { viewModel(tagList = tagList, queryList = mutableListOf<String>().also(requestedQueryList::add)) }

            DiaryTheme {
                MemoTagPickerDialogHost(
                    dialogState = rememberDialogState(initialVisible = true),
                    onEvent = { event -> if (event is MemoTagPickerEvent.ChangeQuery) viewModel.updateQuery(query = event.query) },
                    tagPagingItems = viewModel.tagPagingData.collectAsLazyPagingItems(),
                )
            }
        }
        awaitDialogTextCount(text = EXERCISE_TAG_TITLE, count = 1)
        composeRule.dialogSearchField().performTextInput(WORK_TAG_QUERY)
        awaitDialogTextCount(text = EXERCISE_TAG_TITLE, count = 0)

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        requestedQueryList.size shouldBe 2
        composeRule.dialogNodeWithText(WORK_TAG_QUERY).assertExists()
        awaitDialogTextCount(text = WORK_TAG_TITLE, count = 1)
        composeRule.dialogNodeWithText(EXERCISE_TAG_TITLE).assertDoesNotExist()
        // 새로 만든 상태 객체는 좁히지 않은 대상 전체를 조회하지 않고 처음부터 복원된 검색어로 조회한다.
        requestedQueryList.last() shouldBe listOf(WORK_TAG_QUERY)
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
    private fun viewModel(
        tagList: List<Tag>,
        queryList: MutableList<String>,
    ): MemoAddTagViewModel {
        val pageTagUseCase = mockk<PageTagUseCase>()
        every { pageTagUseCase(parameter = any()) } answers {
            val query = firstArg<String>()
            queryList.add(query)
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
