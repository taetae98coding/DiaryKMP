package io.github.taetae98coding.diary.feature.contact.ui.detail.tab

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.feature.contact.ui.detail.CONTACT_DETAIL_PAGER_TEST_TAG
import io.github.taetae98coding.diary.feature.contact.ui.detail.ContactDetailScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.contact.ui.detail.ContactDetailScreen
import io.github.taetae98coding.diary.feature.contact.ui.detail.ContactDetailScreenTestHost
import io.github.taetae98coding.diary.feature.contact.ui.detail.ContactDetailTestScaffold
import io.github.taetae98coding.diary.feature.contact.ui.detail.ContactDetailUiState
import io.github.taetae98coding.diary.feature.contact.ui.detail.DEFAULT_DETAIL_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.contact.ui.detail.DEFAULT_MEMO_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.contact.ui.detail.DEFAULT_UPDATE_DESCRIPTION
import io.github.taetae98coding.diary.feature.contact.ui.detail.FIRST_CONTACT_ID
import io.github.taetae98coding.diary.feature.contact.ui.detail.KOREAN_DETAIL_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.contact.ui.detail.KOREAN_MEMO_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.contact.ui.detail.contactMemo
import io.github.taetae98coding.diary.feature.contact.ui.detail.contactMemoPagingData
import io.github.taetae98coding.diary.feature.contact.ui.detail.prepareContactDetailTabViewModels
import io.github.taetae98coding.diary.feature.contact.ui.detail.screenTestViewModel
import io.github.taetae98coding.diary.feature.contact.ui.detail.selectContactDetailTab
import io.github.taetae98coding.diary.feature.contact.ui.detail.setContactDetailScreen
import io.github.taetae98coding.diary.feature.contact.ui.detail.testContactDetail
import io.github.taetae98coding.diary.feature.contact.ui.form.rememberContactDetailFormState
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class ContactDetailTabTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-033 화면에 처음 진입하면 연락처 디테일 탭이 선택된다`() {
        setScaffold()

        composeRule.onNodeWithContentDescription(DEFAULT_DETAIL_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNode(hasText(CONTACT_NAME) and hasSetTextAction()).assertExists()
        composeRule.onNodeWithText(MEMO_TITLE).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-CONTACT-DETAIL-FEATURE-033 한국어 환경 탭 접근성 이름을 표시한다`() {
        setScaffold()

        composeRule.onNodeWithContentDescription(KOREAN_DETAIL_TAB_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(KOREAN_MEMO_TAB_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-034 메모 탭을 선택하면 메모 목록을 표시하고 다시 디테일 탭으로 돌아온다`() {
        setScaffold()

        composeRule.selectContactDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()
        waitUntilMemoListExists()

        composeRule.selectContactDetailTab(DEFAULT_DETAIL_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_DETAIL_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNode(hasText(CONTACT_NAME) and hasSetTextAction()).assertExists()
        composeRule.onNodeWithText(MEMO_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-035 본문을 좌우로 밀면 옆 탭으로 전환된다`() {
        setScaffold()

        composeRule.onNodeWithTag(CONTACT_DETAIL_PAGER_TEST_TAG).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()

        composeRule.onNodeWithTag(CONTACT_DETAIL_PAGER_TEST_TAG).performTouchInput { swipeRight() }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_DETAIL_TAB_DESCRIPTION).assertIsSelected()
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-036 TC-CONTACT-DETAIL-FEATURE-042 조회 중에도 탭 행이 표시되고 메모 탭으로 전환할 수 있다`() {
        setScaffold(uiState = ContactDetailUiState.Loading)

        composeRule.onNode(hasText(CONTACT_NAME) and hasSetTextAction()).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_DETAIL_TAB_DESCRIPTION).assertIsSelected()
        composeRule.selectContactDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()
        waitUntilMemoListExists()
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-037 탭을 전환해도 수정 중이던 내용이 유지된다`() {
        setScaffold()
        composeRule.onNode(hasText(CONTACT_NAME) and hasSetTextAction()).performTextInput(EDIT_SUFFIX)

        composeRule.selectContactDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.selectContactDetailTab(DEFAULT_DETAIL_TAB_DESCRIPTION)

        composeRule.onNode(hasText(CONTACT_NAME + EDIT_SUFFIX) and hasSetTextAction()).assertExists()
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-038 수정 반영 동작은 연락처 디테일 탭에서만 제공된다`() {
        setScaffold()
        composeRule.onNode(hasText(CONTACT_NAME) and hasSetTextAction()).performTextInput(EDIT_SUFFIX)
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_DESCRIPTION).assert(hasClickAction())

        composeRule.selectContactDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_DESCRIPTION).assertDoesNotExist()

        composeRule.selectContactDetailTab(DEFAULT_DETAIL_TAB_DESCRIPTION)
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-039 즐겨찾기 변경과 삭제는 선택한 탭과 관계없이 제공된다`() {
        setScaffold()

        listOf(DEFAULT_DETAIL_TAB_DESCRIPTION, DEFAULT_MEMO_TAB_DESCRIPTION).forEach { tabDescription ->
            composeRule.selectContactDetailTab(tabDescription)

            composeRule.onNodeWithContentDescription(DEFAULT_FAVORITE_DESCRIPTION).assert(hasClickAction())
            composeRule.onNodeWithContentDescription(DEFAULT_DELETE_DESCRIPTION).assert(hasClickAction())
        }
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-040 화면 재생성 후에도 선택한 탭이 유지된다`() {
        val restorationTester = StateRestorationTester(composeRule)
        prepareContactDetailTabViewModels()

        restorationTester.setContent {
            ContactDetailScreenTestHost {
                ContactDetailScreen(
                    navigateUp = {},
                    navigateToMemoAdd = {},
                    navigateToMemoDetail = {},
                    id = FIRST_CONTACT_ID,
                    componentVisibleProvider = { ContactDetailScaffoldComponentVisible() },
                    viewModel = screenTestViewModel(uiState = MutableStateFlow(content())),
                )
            }
        }
        composeRule.selectContactDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-041 메모 탭에서 뒤로가도 진입하기 전 화면으로 돌아간다`() {
        var navigateUpCount = 0
        composeRule.setContactDetailScreen(
            viewModel = screenTestViewModel(uiState = MutableStateFlow(content())),
            navigateUp = { navigateUpCount += 1 },
        )
        composeRule.selectContactDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-CONTACT-DETAIL-DOMAIN-014 상세 대상이 다른 연락처로 바뀌면 연락처 디테일 탭으로 초기화한다`() {
        var id by mutableStateOf(FIRST_CONTACT_ID)
        prepareContactDetailTabViewModels()

        composeRule.setContent {
            ContactDetailScreenTestHost {
                ContactDetailScreen(
                    navigateUp = {},
                    navigateToMemoAdd = {},
                    navigateToMemoDetail = {},
                    id = id,
                    componentVisibleProvider = { ContactDetailScaffoldComponentVisible() },
                    viewModel = screenTestViewModel(uiState = MutableStateFlow(content())),
                )
            }
        }
        composeRule.selectContactDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        composeRule.runOnIdle { id = Uuid.random() }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_DETAIL_TAB_DESCRIPTION).assertIsSelected()
    }

    @Test
    fun `TC-CONTACT-DETAIL-DOMAIN-015 삭제 상태인 연락처에서도 두 탭을 모두 사용할 수 있다`() {
        // 삭제 상태인 연락처도 조회 결과로 표시되므로 내용 표시 상태로 관찰된다.
        setScaffold()

        composeRule.selectContactDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        waitUntilMemoListExists()

        composeRule.selectContactDetailTab(DEFAULT_DETAIL_TAB_DESCRIPTION)
        composeRule.onNode(hasText(CONTACT_NAME) and hasSetTextAction()).assertExists()
    }

    private fun waitUntilMemoListExists() {
        composeRule.waitUntil(timeoutMillis = PAGE_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(MEMO_TITLE).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun setScaffold(uiState: ContactDetailUiState = content()) {
        val memoPagingDataFlow = MutableStateFlow(contactMemoPagingData(itemList = listOf(MemoListItem.Content(memo = contactMemo(title = MEMO_TITLE)))))

        composeRule.setContent {
            ContactDetailScreenTestHost {
                ContactDetailTestScaffold(
                    onEvent = {},
                    state = rememberContactDetailFormState(initialDetail = (uiState as? ContactDetailUiState.Content)?.detail ?: ContactDetail.EMPTY),
                    uiStateProvider = { uiState },
                    memoPagingItems = memoPagingDataFlow.collectAsLazyPagingItems(),
                )
            }
        }
    }

    private companion object {
        const val CONTACT_NAME = "ContactDetailTabName"
        const val EDIT_SUFFIX = "Edited"
        const val MEMO_TITLE = "ContactDetailTabMemo"
        const val PAGE_TIMEOUT_MILLIS = 5_000L
        const val DEFAULT_FAVORITE_DESCRIPTION = "Add to favorites"
        const val DEFAULT_DELETE_DESCRIPTION = "Delete contact"
        const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"

        fun content(): ContactDetailUiState.Content = ContactDetailUiState.Content(id = FIRST_CONTACT_ID, detail = testContactDetail(name = CONTACT_NAME))
    }
}
