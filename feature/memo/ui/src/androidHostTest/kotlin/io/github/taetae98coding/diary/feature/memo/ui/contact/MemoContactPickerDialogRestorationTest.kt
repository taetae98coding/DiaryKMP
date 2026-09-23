package io.github.taetae98coding.diary.feature.memo.ui.contact

import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performTextInput
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoContactPickerDialogRestorationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-CONTACT-INPUT-DOMAIN-013 화면이 재생성되어도 열려 있는 목록의 검색어가 유지된다`() {
        val contact = testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER)
        val contactPagingDataFlow = MutableStateFlow(contactPagingDataOf(listOf(contact)))
        val queryList = mutableListOf<String>()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            DiaryTheme {
                MemoContactPickerDialogHost(
                    dialogState = rememberDialogState(initialVisible = true),
                    onEvent = { event -> if (event is MemoContactPickerEvent.ChangeQuery) queryList += event.query },
                    contactPagingItems = contactPagingDataFlow.collectAsLazyPagingItems(),
                )
            }
        }
        composeRule.contactDialogSearchField().performTextInput(SEARCH_QUERY)
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        queryList.last() shouldBe SEARCH_QUERY
        // 재생성은 목록을 닫는 것이 아니므로 빈 검색어를 보고하지 않는다. 목록을 열 때 보고한 한 번만 있어야 한다.
        queryList.count { query -> query.isEmpty() } shouldBe 1
        composeRule.contactDialogNodeWithText(SEARCH_QUERY).assertExists()
        composeRule.awaitContactPickerRows()
        // 제목은 검색 입력의 값과 겹칠 수 있으므로 목록 항목은 URL로 가려 확인한다.
        composeRule.contactDialogNodeWithText(FIRST_CONTACT_PHONE_NUMBER).assertExists()
    }

    public companion object {
        private const val SEARCH_QUERY = "MemoContactRestoredQuery"
    }
}
