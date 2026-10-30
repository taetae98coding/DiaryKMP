package io.github.taetae98coding.diary.feature.memo.ui.contact

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
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.domain.contact.usecase.GetSelectedContactUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageMemoSelectableContactUseCase
import io.github.taetae98coding.diary.feature.memo.ui.add.MemoAddContactViewModel
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
class MemoContactPickerDialogBoundaryTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val requestedQueryList = mutableListOf<MutableList<String>>()

    private lateinit var dialogState: DialogState

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-f6951ae2-267d-4a58-8689-c2562dd77d1f 메모리 정리 뒤 복원하면 목록이 다시 열리고 검색어가 다시 나타나 입력 정지 대기 시간 뒤 그 검색어로 좁힌 목록을 보여 준다`() {
        val contactList = listOf(testContact(name = FIRST_CONTACT_NAME), testContact(name = SECOND_CONTACT_NAME))
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            // 메모리 정리 뒤에는 검색어를 반영하던 상태 객체도 새로 만들어지므로, 복원할 때마다 새 인스턴스를 쓴다.
            val viewModel = remember { viewModel(contactList = contactList, queryList = mutableListOf<String>().also(requestedQueryList::add)) }
            ContactPickerDialogHost(viewModel = viewModel)
        }
        openDialogAndSearch()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        requestedQueryList.size shouldBe 2
        dialogState.isVisible shouldBe true
        composeRule.contactDialogNodeWithText(CONTACT_QUERY).assertExists()
        awaitDialogTextCount(text = FIRST_CONTACT_NAME, count = 1)
        awaitDialogTextCount(text = SECOND_CONTACT_NAME, count = 0)
        requestedQueryList.last().last() shouldBe CONTACT_QUERY
    }

    @Test
    fun `TC-fb76eeeb-23a5-4408-be80-351a8a04876e 목록이 열린 채 다른 앱에 다녀와도 목록과 검색어가 유지된다`() {
        val contactList = listOf(testContact(name = FIRST_CONTACT_NAME), testContact(name = SECOND_CONTACT_NAME))
        val viewModel = viewModel(contactList = contactList, queryList = mutableListOf<String>().also(requestedQueryList::add))
        composeRule.setContent { ContactPickerDialogHost(viewModel = viewModel) }
        openDialogAndSearch()

        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()

        dialogState.isVisible shouldBe true
        composeRule.contactDialogNodeWithText(CONTACT_QUERY).assertExists()
        awaitDialogTextCount(text = FIRST_CONTACT_NAME, count = 1)
        awaitDialogTextCount(text = SECOND_CONTACT_NAME, count = 0)
    }

    @Composable
    private fun ContactPickerDialogHost(viewModel: MemoAddContactViewModel) {
        dialogState = rememberDialogState()
        DiaryTheme {
            MemoContactPickerDialogHost(
                dialogState = dialogState,
                onEvent = { event -> if (event is MemoContactPickerEvent.ChangeQuery) viewModel.updateQuery(query = event.query) },
                contactPagingItems = viewModel.contactPagingData.collectAsLazyPagingItems(),
            )
        }
    }

    private fun openDialogAndSearch() {
        composeRule.runOnIdle { dialogState.show() }
        awaitDialogTextCount(text = SECOND_CONTACT_NAME, count = 1)
        composeRule.contactDialogSearchField().performTextInput(CONTACT_QUERY)
        awaitDialogTextCount(text = SECOND_CONTACT_NAME, count = 0)
    }

    private fun awaitDialogTextCount(
        text: String,
        count: Int,
    ) {
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodes(hasText(text) and hasAnyAncestor(isDialog())).fetchSemanticsNodes().size == count
        }
    }

    // 검색어에 맞는 연락처를 고르는 조회 자체는 저장소 테스트가 검증하므로, 검색어를 받으면 그 조건의 결과를 돌려준다.
    private fun viewModel(
        contactList: List<Contact>,
        queryList: MutableList<String>,
    ): MemoAddContactViewModel {
        val pageMemoSelectableContactUseCase = mockk<PageMemoSelectableContactUseCase>()
        every { pageMemoSelectableContactUseCase(parameter = any()) } answers {
            val query = firstArg<String>()
            queryList.add(query)
            flowOf(Result.success(PagingData.from(contactList.filter { contact -> contact.detail.name.contains(query, ignoreCase = true) })))
        }
        val getSelectedContactUseCase = mockk<GetSelectedContactUseCase>()
        every { getSelectedContactUseCase(parameter = any()) } returns flowOf(Result.success(emptyList()))

        return MemoAddContactViewModel(
            initialContactId = null,
            pageMemoSelectableContactUseCase = pageMemoSelectableContactUseCase,
            getSelectedContactUseCase = getSelectedContactUseCase,
        )
    }

    private companion object {
        private const val CONTACT_QUERY = "Ki"
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
