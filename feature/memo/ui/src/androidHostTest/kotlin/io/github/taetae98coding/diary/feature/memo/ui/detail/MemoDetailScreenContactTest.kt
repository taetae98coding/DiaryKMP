package io.github.taetae98coding.diary.feature.memo.ui.detail

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.feature.memo.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.memo.ui.closeDialogByBack
import io.github.taetae98coding.diary.feature.memo.ui.contact.DEFAULT_CONTACT_PICKER_ADD_LABEL
import io.github.taetae98coding.diary.feature.memo.ui.contact.DEFAULT_CONTACT_PICKER_TITLE
import io.github.taetae98coding.diary.feature.memo.ui.contact.DEFAULT_CONTACT_SELECT_LABEL
import io.github.taetae98coding.diary.feature.memo.ui.contact.FIRST_CONTACT_NAME
import io.github.taetae98coding.diary.feature.memo.ui.contact.FIRST_CONTACT_PHONE_NUMBER
import io.github.taetae98coding.diary.feature.memo.ui.contact.MemoContactInputUiState
import io.github.taetae98coding.diary.feature.memo.ui.contact.MemoContactViewModel
import io.github.taetae98coding.diary.feature.memo.ui.contact.SECOND_CONTACT_NAME
import io.github.taetae98coding.diary.feature.memo.ui.contact.SECOND_CONTACT_PHONE_NUMBER
import io.github.taetae98coding.diary.feature.memo.ui.contact.awaitContactPickerRows
import io.github.taetae98coding.diary.feature.memo.ui.contact.contactDialogNodeWithText
import io.github.taetae98coding.diary.feature.memo.ui.contact.contactDialogSearchField
import io.github.taetae98coding.diary.feature.memo.ui.contact.contactPagingDataOf
import io.github.taetae98coding.diary.feature.memo.ui.contact.screenTestContactViewModel
import io.github.taetae98coding.diary.feature.memo.ui.contact.testContact
import io.github.taetae98coding.diary.feature.memo.ui.gemini.screenTestGeminiViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceInputUiState
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceViewModel
import io.github.taetae98coding.diary.feature.memo.ui.web.screenTestWebViewModel
import io.kotest.matchers.shouldBe
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class MemoDetailScreenContactTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-066 연락처 입력에 저장된 연락처 연결이 칩으로 표시된다`() {
        val contact = testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER)
        composeRule.setMemoDetailScreenWithContact(
            contactViewModel = memoDetailContactViewModel(contactList = listOf(contact), selectedContactList = listOf(contact)),
        )

        composeRule.onNodeWithText(DEFAULT_CONTACT_SELECT_LABEL).performScrollTo()

        composeRule.onNodeWithText(FIRST_CONTACT_NAME).assertExists()
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-067 다른 경로로 저장된 연락처 연결이 바뀌면 연락처 입력에 반영된다`() {
        val contact = testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER)
        val uiState = MutableStateFlow(MemoContactInputUiState())
        composeRule.setMemoDetailScreenWithContact(
            contactViewModel = screenTestContactViewModel(uiState = uiState, contactPagingDataFlow = MutableStateFlow(contactPagingDataOf(listOf(contact)))),
        )
        composeRule.onNodeWithText(DEFAULT_CONTACT_SELECT_LABEL).performScrollTo()
        composeRule.onNodeWithText(FIRST_CONTACT_NAME).assertDoesNotExist()

        composeRule.runOnIdle { uiState.value = MemoContactInputUiState(selectedContactList = listOf(contact)) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(FIRST_CONTACT_NAME).assertExists()
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-069 연락처를 선택하면 즉시 연결 변경을 요청하고 성공 안내는 표시하지 않는다`() {
        val contact = testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER)
        val contactViewModel = memoDetailContactViewModel(contactList = listOf(contact))
        composeRule.setMemoDetailScreenWithContact(contactViewModel = contactViewModel)

        composeRule.openMemoDetailContactPicker()
        composeRule.awaitContactPickerRows()
        composeRule.contactDialogNodeWithText(FIRST_CONTACT_NAME).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { contactViewModel.selectContact(contactId = contact.id) }
        composeRule.onNodeWithText(DEFAULT_UPDATE_SUCCEEDED_MESSAGE).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-069 연락처 선택을 해제하면 즉시 연결 해제를 요청한다`() {
        val contact = testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER)
        val contactViewModel = memoDetailContactViewModel(contactList = listOf(contact), selectedContactList = listOf(contact))
        composeRule.setMemoDetailScreenWithContact(contactViewModel = contactViewModel)

        composeRule.openMemoDetailContactPicker()
        composeRule.awaitContactPickerRows()
        composeRule.contactDialogNodeWithText(FIRST_CONTACT_PHONE_NUMBER).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { contactViewModel.unselectContact(contactId = contact.id) }
    }

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-016 연락처 칩을 누르면 ContactDetail 이동을 요청한다`() {
        val contact = testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER)
        val clickedIdList = mutableListOf<Uuid>()
        composeRule.setMemoDetailScreenWithContact(
            contactViewModel = memoDetailContactViewModel(contactList = listOf(contact), selectedContactList = listOf(contact)),
            navigateToContactDetail = { id -> clickedIdList += id },
        )

        composeRule.onNodeWithText(FIRST_CONTACT_NAME).performScrollTo().performClick()
        composeRule.waitForIdle()

        clickedIdList shouldBe listOf(contact.id)
    }

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-003 선택할 수 있는 연락처가 없으면 추가 항목이 ContactAdd 이동을 요청한다`() {
        var contactAddCount = 0
        composeRule.setMemoDetailScreenWithContact(
            contactViewModel = memoDetailContactViewModel(contactList = emptyList()),
            navigateToContactAdd = { contactAddCount += 1 },
        )

        composeRule.openMemoDetailContactPicker()
        composeRule.waitForIdle()

        contactAddCount shouldBe 1
        composeRule.onNodeWithText(DEFAULT_CONTACT_PICKER_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-007 목록의 연락처 추가 항목을 누르면 목록이 닫히고 ContactAdd 이동을 요청한다`() {
        val contact = testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER)
        var contactAddCount = 0
        composeRule.setMemoDetailScreenWithContact(
            contactViewModel = memoDetailContactViewModel(contactList = listOf(contact)),
            navigateToContactAdd = { contactAddCount += 1 },
        )

        composeRule.openMemoDetailContactPicker()
        composeRule.awaitContactPickerRows()
        composeRule.contactDialogNodeWithText(DEFAULT_CONTACT_PICKER_ADD_LABEL).performClick()
        composeRule.waitForIdle()

        contactAddCount shouldBe 1
        composeRule.onNodeWithText(DEFAULT_CONTACT_PICKER_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-DETAIL-DOMAIN-013 삭제된 연락처는 연락처 입력과 선택 목록에 나타나지 않는다`() {
        val keptContact = testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER)
        val deletedContact = testContact(name = SECOND_CONTACT_NAME, phoneNumber = SECOND_CONTACT_PHONE_NUMBER)
        // 조회는 삭제된 연락처를 제외하므로 선택 목록과 칩 모두에서 빠진다.
        composeRule.setMemoDetailScreenWithContact(
            contactViewModel = memoDetailContactViewModel(contactList = listOf(keptContact), selectedContactList = listOf(keptContact)),
        )

        composeRule.openMemoDetailContactPicker()
        composeRule.awaitContactPickerRows()

        composeRule.contactDialogNodeWithText(FIRST_CONTACT_NAME).assertExists()
        composeRule.contactDialogNodeWithText(SECOND_CONTACT_NAME).assertDoesNotExist()
        composeRule.closeDialogByBack()
        composeRule.onNodeWithText(SECOND_CONTACT_NAME).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-CONTACT-INPUT-DOMAIN-015 상세 대상이 바뀌면 열려 있던 연락처 선택 목록과 검색어를 유지하지 않는다`() {
        val contact = testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER)
        val detailUiState = MutableStateFlow<MemoDetailUiState>(memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(MEMO_TITLE)))
        composeRule.setMemoDetailScreenWithContact(
            contactViewModel = memoDetailContactViewModel(contactList = listOf(contact)),
            detailUiState = detailUiState,
        )
        composeRule.openMemoDetailContactPicker()
        composeRule.awaitContactPickerRows()
        composeRule.contactDialogSearchField().performTextInput(FIRST_CONTACT_NAME)
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            detailUiState.value = memoDetailUiState(id = Uuid.parse("00000000-0000-0000-0000-000000000002"), detail = memoDetail(MEMO_TITLE))
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_CONTACT_PICKER_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-070 연락처를 선택해도 수정 버튼이 나타나지 않는다`() {
        val contact = testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER)
        composeRule.setMemoDetailScreenWithContact(contactViewModel = memoDetailContactViewModel(contactList = listOf(contact)))

        composeRule.openMemoDetailContactPicker()
        composeRule.awaitContactPickerRows()
        composeRule.contactDialogNodeWithText(FIRST_CONTACT_NAME).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-070 연락처 선택을 해제해도 수정 버튼이 나타나지 않는다`() {
        val contact = testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER)
        composeRule.setMemoDetailScreenWithContact(contactViewModel = memoDetailContactViewModel(contactList = listOf(contact), selectedContactList = listOf(contact)))

        composeRule.openMemoDetailContactPicker()
        composeRule.awaitContactPickerRows()
        composeRule.contactDialogNodeWithText(FIRST_CONTACT_PHONE_NUMBER).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-071 다른 메모를 선택하면 연락처 입력이 새 메모의 연락처 연결로 바뀐다`() {
        val firstContact = testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER)
        val secondContact = testContact(name = SECOND_CONTACT_NAME, phoneNumber = SECOND_CONTACT_PHONE_NUMBER)
        val detailUiState = MutableStateFlow<MemoDetailUiState>(memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(MEMO_TITLE)))
        val contactUiState = MutableStateFlow(MemoContactInputUiState(selectedContactList = listOf(firstContact)))
        composeRule.setMemoDetailScreenWithContact(
            contactViewModel = screenTestContactViewModel(uiState = contactUiState, contactPagingDataFlow = MutableStateFlow(contactPagingDataOf(listOf(firstContact, secondContact)))),
            detailUiState = detailUiState,
        )
        composeRule.onNodeWithText(DEFAULT_CONTACT_SELECT_LABEL).performScrollTo()
        composeRule.onNodeWithText(FIRST_CONTACT_NAME).assertExists()

        composeRule.runOnIdle {
            detailUiState.value = memoDetailUiState(id = SECOND_CONTACT_MEMO_ID, detail = memoDetail(MEMO_TITLE))
            contactUiState.value = MemoContactInputUiState(selectedContactList = listOf(secondContact))
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_CONTACT_SELECT_LABEL).performScrollTo()

        composeRule.onNodeWithText(SECOND_CONTACT_NAME).assertExists()
        composeRule.onNodeWithText(FIRST_CONTACT_NAME).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-072 화면이 재생성되면 연락처 입력은 저장된 연락처 연결을 다시 표시한다`() {
        val contact = testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER)
        val contactViewModel = memoDetailContactViewModel(contactList = listOf(contact), selectedContactList = listOf(contact))
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            MemoDetailScreenTestTheme {
                MemoDetailScreen(
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    detailViewModel = screenTestViewModel(uiState = MutableStateFlow(memoDetailUiState(detail = memoDetail(MEMO_TITLE)))),
                    tagViewModel = screenTestTagViewModel(),
                    webViewModel = screenTestWebViewModel(),
                    contactViewModel = contactViewModel,
                    placeViewModel = screenTestPlaceViewModel(uiState = MutableStateFlow(MemoPlaceInputUiState(isSelectedPlaceLoaded = true))),
                    placeMapViewModel = screenTestPlaceMapViewModel(),
                    geminiViewModel = screenTestGeminiViewModel(),
                    navigateUp = {},
                    navigateToCopiedMemo = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    navigateToWebAdd = {},
                    navigateToWebDetail = {},
                    navigateToContactAdd = {},
                    navigateToContactDetail = {},
                    navigateToPlaceAdd = {},
                    navigateToPlaceDetail = {},
                    componentVisibleProvider = { MemoDetailScaffoldComponentVisible() },
                    isStandalone = true,
                )
            }
        }
        composeRule.onNodeWithText(DEFAULT_CONTACT_SELECT_LABEL).performScrollTo()
        composeRule.onNodeWithText(FIRST_CONTACT_NAME).assertExists()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.onNodeWithText(DEFAULT_CONTACT_SELECT_LABEL).performScrollTo()

        composeRule.onNodeWithText(FIRST_CONTACT_NAME).assertExists()
    }

    private fun ComposeContentTestRule.setMemoDetailScreenWithContact(
        contactViewModel: MemoContactViewModel,
        navigateToContactAdd: () -> Unit = {},
        navigateToContactDetail: (Uuid) -> Unit = {},
        detailUiState: MutableStateFlow<MemoDetailUiState> = MutableStateFlow(memoDetailUiState(detail = memoDetail(MEMO_TITLE))),
    ) {
        setContent {
            MemoDetailScreenTestTheme {
                MemoDetailScreen(
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    detailViewModel = screenTestViewModel(uiState = detailUiState),
                    tagViewModel = screenTestTagViewModel(),
                    webViewModel = screenTestWebViewModel(),
                    contactViewModel = contactViewModel,
                    placeViewModel = screenTestPlaceViewModel(uiState = MutableStateFlow(MemoPlaceInputUiState(isSelectedPlaceLoaded = true))),
                    placeMapViewModel = screenTestPlaceMapViewModel(),
                    geminiViewModel = screenTestGeminiViewModel(),
                    navigateUp = {},
                    navigateToCopiedMemo = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    navigateToWebAdd = {},
                    navigateToWebDetail = {},
                    navigateToContactAdd = navigateToContactAdd,
                    navigateToContactDetail = navigateToContactDetail,
                    navigateToPlaceAdd = {},
                    navigateToPlaceDetail = {},
                    componentVisibleProvider = { MemoDetailScaffoldComponentVisible() },
                    isStandalone = true,
                )
            }
        }
    }

    private fun ComposeContentTestRule.openMemoDetailContactPicker() {
        onNodeWithText(DEFAULT_CONTACT_SELECT_LABEL).performScrollTo().performClick()
        waitForIdle()
    }

    private fun memoDetailContactViewModel(
        contactList: List<Contact>,
        selectedContactList: List<Contact> = emptyList(),
    ): MemoContactViewModel =
        screenTestContactViewModel(
            uiState = MutableStateFlow(MemoContactInputUiState(selectedContactList = selectedContactList)),
            contactPagingDataFlow = MutableStateFlow<PagingData<Contact>>(contactPagingDataOf(contactList)),
        )

    private companion object {
        val SECOND_CONTACT_MEMO_ID: Uuid = Uuid.parse("00000000-0000-0000-0000-000000000031")
    }
}
