package io.github.taetae98coding.diary.feature.memo.ui.web

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.Lifecycle
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.domain.memo.usecase.PageMemoSelectableWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.GetSelectedWebUseCase
import io.github.taetae98coding.diary.feature.memo.ui.add.MemoAddWebViewModel
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
class MemoWebPickerDialogBoundaryTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val requestedQueryList = mutableListOf<MutableList<String>>()

    private lateinit var dialogState: DialogState

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-a3b2c5f7-4665-46a0-af16-1126dc013c17 메모리 정리 뒤 복원하면 목록이 다시 열리고 검색어가 다시 나타나 입력 정지 대기 시간 뒤 그 검색어로 좁힌 목록을 보여 준다`() {
        val webList = listOf(testWeb(title = WIKI_WEB_TITLE), testWeb(title = DOCS_WEB_TITLE))
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            // 메모리 정리 뒤에는 검색어를 반영하던 상태 객체도 새로 만들어지므로, 복원할 때마다 새 인스턴스를 쓴다.
            val viewModel = remember { viewModel(webList = webList, queryList = mutableListOf<String>().also(requestedQueryList::add)) }
            WebPickerDialogHost(viewModel = viewModel)
        }
        openDialogAndSearch()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        requestedQueryList.size shouldBe 2
        dialogState.isVisible shouldBe true
        composeRule.webDialogNodeWithText(WEB_QUERY).assertExists()
        awaitDialogTextCount(text = WIKI_WEB_TITLE, count = 1)
        awaitDialogTextCount(text = DOCS_WEB_TITLE, count = 0)
        requestedQueryList.last().last() shouldBe WEB_QUERY
    }

    @Test
    fun `TC-85f71797-05ed-4890-a3b5-264a18347107 목록이 열린 채 다른 앱에 다녀와도 목록과 검색어가 유지된다`() {
        val webList = listOf(testWeb(title = WIKI_WEB_TITLE), testWeb(title = DOCS_WEB_TITLE))
        val viewModel = viewModel(webList = webList, queryList = mutableListOf<String>().also(requestedQueryList::add))
        composeRule.setContent { WebPickerDialogHost(viewModel = viewModel) }
        openDialogAndSearch()

        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()

        dialogState.isVisible shouldBe true
        composeRule.webDialogNodeWithText(WEB_QUERY).assertExists()
        awaitDialogTextCount(text = WIKI_WEB_TITLE, count = 1)
        awaitDialogTextCount(text = DOCS_WEB_TITLE, count = 0)
    }

    @Composable
    private fun WebPickerDialogHost(viewModel: MemoAddWebViewModel) {
        dialogState = rememberDialogState()
        DiaryTheme {
            MemoWebPickerDialogHost(
                dialogState = dialogState,
                onEvent = { event -> if (event is MemoWebPickerEvent.ChangeQuery) viewModel.updateQuery(query = event.query) },
                webPagingItems = viewModel.webPagingData.collectAsLazyPagingItems(),
            )
        }
    }

    private fun openDialogAndSearch() {
        composeRule.runOnIdle { dialogState.show() }
        awaitDialogTextCount(text = DOCS_WEB_TITLE, count = 1)
        composeRule.webDialogSearchField().performTextInput(WEB_QUERY)
        awaitDialogTextCount(text = DOCS_WEB_TITLE, count = 0)
    }

    private fun awaitDialogTextCount(
        text: String,
        count: Int,
    ) {
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodes(hasText(text) and hasAnyAncestor(isDialog())).fetchSemanticsNodes().size == count
        }
    }

    // 검색어에 맞는 웹 항목을 고르는 조회 자체는 저장소 테스트가 검증하므로, 검색어를 받으면 그 조건의 결과를 돌려준다.
    private fun viewModel(
        webList: List<Web>,
        queryList: MutableList<String>,
    ): MemoAddWebViewModel {
        val pageMemoSelectableWebUseCase = mockk<PageMemoSelectableWebUseCase>()
        every { pageMemoSelectableWebUseCase(parameter = any()) } answers {
            val query = firstArg<String>()
            queryList.add(query)
            flowOf(Result.success(PagingData.from(webList.filter { web -> web.detail.title.contains(query, ignoreCase = true) })))
        }
        val getSelectedWebUseCase = mockk<GetSelectedWebUseCase>()
        every { getSelectedWebUseCase(parameter = any()) } returns flowOf(Result.success(emptyList()))

        return MemoAddWebViewModel(
            initialWebId = null,
            pageMemoSelectableWebUseCase = pageMemoSelectableWebUseCase,
            getSelectedWebUseCase = getSelectedWebUseCase,
        )
    }

    private companion object {
        private const val WEB_QUERY = "Wi"
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
