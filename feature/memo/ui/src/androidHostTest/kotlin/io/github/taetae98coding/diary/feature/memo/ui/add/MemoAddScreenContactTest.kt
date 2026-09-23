package io.github.taetae98coding.diary.feature.memo.ui.add

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoUseCase
import io.github.taetae98coding.diary.feature.contact.api.ContactAddedResult
import io.github.taetae98coding.diary.feature.memo.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.memo.ui.closeDialogByBack
import io.github.taetae98coding.diary.feature.memo.ui.contact.DEFAULT_CONTACT_PICKER_ADD_LABEL
import io.github.taetae98coding.diary.feature.memo.ui.contact.DEFAULT_CONTACT_PICKER_TITLE
import io.github.taetae98coding.diary.feature.memo.ui.contact.DEFAULT_CONTACT_SELECT_LABEL
import io.github.taetae98coding.diary.feature.memo.ui.contact.FIRST_CONTACT_NAME
import io.github.taetae98coding.diary.feature.memo.ui.contact.FIRST_CONTACT_PHONE_NUMBER
import io.github.taetae98coding.diary.feature.memo.ui.contact.SECOND_CONTACT_NAME
import io.github.taetae98coding.diary.feature.memo.ui.contact.SECOND_CONTACT_PHONE_NUMBER
import io.github.taetae98coding.diary.feature.memo.ui.contact.awaitContactPickerRows
import io.github.taetae98coding.diary.feature.memo.ui.contact.contactDialogNodeWithText
import io.github.taetae98coding.diary.feature.memo.ui.contact.refreshingContactPagingData
import io.github.taetae98coding.diary.feature.memo.ui.contact.testContact
import io.github.taetae98coding.diary.feature.memo.ui.gemini.screenTestGeminiViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceMapViewModel
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

private const val FIRST_ADDED_CONTACT_NAME: String = "MemoContactFirstAdded"
private const val SECOND_ADDED_CONTACT_NAME: String = "MemoContactSecondAdded"
private const val CONTACT_TEST_TYPED_TITLE: String = "MemoContactTypedTitle"
private const val CONTACT_TEST_ADD_BUTTON_DESCRIPTION: String = "Add memo"

private fun ResultEventBus.sendContactAddedResult(contact: Contact) {
    sendResult<ContactAddedResult>(result = ContactAddedResult(id = contact.id))
}

private fun ComposeContentTestRule.openContactPicker() {
    onNodeWithText(DEFAULT_CONTACT_SELECT_LABEL).performScrollTo().performClick()
    waitForIdle()
}

private fun ComposeContentTestRule.setMemoAddScreenForContact(
    viewModels: MemoAddScreenViewModels,
    navigateToContactAdd: () -> Unit = {},
    navigateToContactDetail: (Uuid) -> Unit = {},
    resultEventBus: ResultEventBus = ResultEventBus(),
) {
    setContent {
        MemoAddScreenTestTheme(resultEventBus = resultEventBus) {
            MemoAddScreen(
                tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                addViewModel = viewModels.viewModel,
                tagViewModel = viewModels.tagViewModel,
                webViewModel = viewModels.webViewModel,
                contactViewModel = viewModels.contactViewModel,
                placeViewModel = viewModels.placeViewModel,
                placeMapViewModel = screenTestPlaceMapViewModel(),
                geminiViewModel = screenTestGeminiViewModel(),
                navigateUp = {},
                navigateToTagAdd = {},
                navigateToTagDetail = {},
                navigateToWebAdd = {},
                navigateToWebDetail = {},
                navigateToContactAdd = navigateToContactAdd,
                navigateToContactDetail = navigateToContactDetail,
                navigateToPlaceAdd = {},
                navigateToPlaceDetail = {},
                initialDateRange = null,
                componentVisibleProvider = { MemoAddScaffoldComponentVisible() },
                isStandalone = true,
            )
        }
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class MemoAddScreenContactTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-001 추가 항목을 누르면 선택할 수 있는 연락처 목록이 열린다`() {
        val contactList = listOf(testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER), testContact(name = SECOND_CONTACT_NAME, phoneNumber = SECOND_CONTACT_PHONE_NUMBER))
        var contactAddCount = 0
        composeRule.setMemoAddScreenForContact(
            viewModels = screenTestRealViewModel(contactList = contactList),
            navigateToContactAdd = { contactAddCount += 1 },
        )

        composeRule.openContactPicker()
        composeRule.awaitContactPickerRows()

        contactAddCount shouldBe 0
        composeRule.contactDialogNodeWithText(FIRST_CONTACT_NAME).assertExists()
        composeRule.contactDialogNodeWithText(SECOND_CONTACT_NAME).assertExists()
    }

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-003 선택할 수 있는 연락처가 없으면 추가 항목이 ContactAdd 이동을 요청한다`() {
        var contactAddCount = 0
        composeRule.setMemoAddScreenForContact(
            viewModels = screenTestRealViewModel(contactList = emptyList()),
            navigateToContactAdd = { contactAddCount += 1 },
        )

        composeRule.openContactPicker()

        contactAddCount shouldBe 1
        composeRule.onNodeWithText(DEFAULT_CONTACT_PICKER_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-004 목록의 대상을 확인하는 중에는 추가 항목이 목록을 연다`() {
        var contactAddCount = 0
        composeRule.setMemoAddScreenForContact(
            viewModels = screenTestViewModel(contactPagingData = MutableStateFlow(refreshingContactPagingData())),
            navigateToContactAdd = { contactAddCount += 1 },
        )

        composeRule.openContactPicker()

        contactAddCount shouldBe 0
        composeRule.onNodeWithText(DEFAULT_CONTACT_PICKER_TITLE).assertExists()
    }

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-011 목록에서 고른 연락처가 연락처 입력의 칩으로 나타난다`() {
        val contact = testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER)
        composeRule.setMemoAddScreenForContact(viewModels = screenTestRealViewModel(contactList = listOf(contact)))

        composeRule.selectContact(name = FIRST_CONTACT_NAME)

        composeRule.onNodeWithText(FIRST_CONTACT_NAME).assertExists()
    }

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-014 목록을 닫아도 반영한 선택이 유지된다`() {
        val contactList = listOf(testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER), testContact(name = SECOND_CONTACT_NAME, phoneNumber = SECOND_CONTACT_PHONE_NUMBER))
        composeRule.setMemoAddScreenForContact(viewModels = screenTestRealViewModel(contactList = contactList))

        composeRule.selectContact(name = FIRST_CONTACT_NAME)

        composeRule.onNodeWithText(DEFAULT_CONTACT_PICKER_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(FIRST_CONTACT_NAME).assertExists()
        composeRule.onNodeWithText(SECOND_CONTACT_NAME).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-007 목록의 웹 추가 항목을 누르면 목록이 닫히고 ContactAdd 이동을 요청한다`() {
        val contactList = listOf(testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER))
        var contactAddCount = 0
        composeRule.setMemoAddScreenForContact(
            viewModels = screenTestRealViewModel(contactList = contactList),
            navigateToContactAdd = { contactAddCount += 1 },
        )

        composeRule.openContactPicker()
        composeRule.awaitContactPickerRows()
        composeRule.contactDialogNodeWithText(DEFAULT_CONTACT_PICKER_ADD_LABEL).performClick()
        composeRule.waitForIdle()

        contactAddCount shouldBe 1
        composeRule.onNodeWithText(DEFAULT_CONTACT_PICKER_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-016 연락처 칩을 누르면 ContactDetail 이동을 요청한다`() {
        val contact = testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER)
        val clickedIdList = mutableListOf<Uuid>()
        composeRule.setMemoAddScreenForContact(
            viewModels = screenTestRealViewModel(contactList = listOf(contact)),
            navigateToContactDetail = { id -> clickedIdList += id },
        )
        composeRule.selectContact(name = FIRST_CONTACT_NAME)

        composeRule.onNodeWithText(FIRST_CONTACT_NAME).performScrollTo().performClick()
        composeRule.waitForIdle()

        clickedIdList shouldBe listOf(contact.id)
    }

    @Test
    fun `TC-MEMO-ADD-DATA-021 연락처를 선택하는 것만으로는 저장된 메모가 바뀌지 않는다`() {
        val contact = testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER)
        val addMemoUseCase = mockk<AddMemoUseCase>()
        composeRule.setMemoAddScreenForContact(viewModels = screenTestRealViewModel(contactList = listOf(contact), addMemoUseCase = addMemoUseCase))

        composeRule.selectContact(name = FIRST_CONTACT_NAME)

        coVerify(exactly = 0) { addMemoUseCase(any<AddMemoUseCase.Parameter>()) }
    }

    @Test
    fun `TC-MEMO-ADD-FEATURE-060 추가에 성공해도 선택한 연락처가 유지된다`() {
        val contact = testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER)
        val addMemoUseCase = mockk<AddMemoUseCase>()
        coEvery { addMemoUseCase(any<AddMemoUseCase.Parameter>()) } returns Result.success(Uuid.random())
        composeRule.setMemoAddScreenForContact(viewModels = screenTestRealViewModel(contactList = listOf(contact), addMemoUseCase = addMemoUseCase))
        composeRule.selectContact(name = FIRST_CONTACT_NAME)
        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(CONTACT_TEST_TYPED_TITLE)

        composeRule.onNodeWithContentDescription(CONTACT_TEST_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        coVerify(exactly = 1) { addMemoUseCase(any<AddMemoUseCase.Parameter>()) }
        composeRule.onNodeWithText(FIRST_CONTACT_NAME).performScrollTo().assertExists()
    }

    private fun ComposeContentTestRule.selectContact(name: String) {
        openContactPicker()
        awaitContactPickerRows()
        contactDialogNodeWithText(name).performClick()
        closeDialogByBack()
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class MemoAddScreenContactAddedResultTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-008 ContactAdd 화면에서 추가한 연락처 하나가 돌아왔을 때 선택된다`() {
        val selectedContact = testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER)
        val addedContact = testContact(name = SECOND_CONTACT_NAME, phoneNumber = SECOND_CONTACT_PHONE_NUMBER)
        val resultEventBus = ResultEventBus()
        composeRule.setMemoAddScreenForContact(
            viewModels = screenTestRealViewModel(contactList = listOf(selectedContact, addedContact)),
            resultEventBus = resultEventBus,
        )
        composeRule.selectContact(name = FIRST_CONTACT_NAME)

        resultEventBus.sendContactAddedResult(addedContact)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(FIRST_CONTACT_NAME).assertExists()
        composeRule.onNodeWithText(SECOND_CONTACT_NAME).assertExists()
    }

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-008 ContactAdd 화면에서 추가한 연락처 여러 개가 돌아왔을 때 모두 선택된다`() {
        val selectedContact = testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER)
        val firstAddedContact = testContact(name = FIRST_ADDED_CONTACT_NAME)
        val secondAddedContact = testContact(name = SECOND_ADDED_CONTACT_NAME)
        val resultEventBus = ResultEventBus()
        composeRule.setMemoAddScreenForContact(
            viewModels = screenTestRealViewModel(contactList = listOf(selectedContact, firstAddedContact, secondAddedContact)),
            resultEventBus = resultEventBus,
        )
        composeRule.selectContact(name = FIRST_CONTACT_NAME)

        resultEventBus.sendContactAddedResult(firstAddedContact)
        resultEventBus.sendContactAddedResult(secondAddedContact)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(FIRST_CONTACT_NAME).assertExists()
        composeRule.onNodeWithText(FIRST_ADDED_CONTACT_NAME).assertExists()
        composeRule.onNodeWithText(SECOND_ADDED_CONTACT_NAME).assertExists()
    }

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-009 연락처를 하나도 추가하지 않고 돌아오면 선택이 그대로 유지된다`() {
        val contactList = listOf(testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER), testContact(name = SECOND_CONTACT_NAME, phoneNumber = SECOND_CONTACT_PHONE_NUMBER))
        var contactAddCount = 0
        composeRule.setMemoAddScreenForContact(
            viewModels = screenTestRealViewModel(contactList = contactList),
            navigateToContactAdd = { contactAddCount += 1 },
        )
        composeRule.selectContact(name = FIRST_CONTACT_NAME)

        composeRule.openContactPicker()
        composeRule.awaitContactPickerRows()
        composeRule.contactDialogNodeWithText(DEFAULT_CONTACT_PICKER_ADD_LABEL).performClick()
        composeRule.waitForIdle()

        contactAddCount shouldBe 1
        composeRule.onNodeWithText(FIRST_CONTACT_NAME).assertExists()
        composeRule.onNodeWithText(SECOND_CONTACT_NAME).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-010 ContactAdd 화면에서 돌아와도 연락처 선택 목록이 저절로 열리지 않는다`() {
        val addedContact = testContact(name = SECOND_CONTACT_NAME, phoneNumber = SECOND_CONTACT_PHONE_NUMBER)
        val resultEventBus = ResultEventBus()
        composeRule.setMemoAddScreenForContact(
            viewModels = screenTestRealViewModel(contactList = listOf(addedContact)),
            resultEventBus = resultEventBus,
        )
        composeRule.openContactPicker()
        composeRule.awaitContactPickerRows()
        composeRule.contactDialogNodeWithText(DEFAULT_CONTACT_PICKER_ADD_LABEL).performClick()
        composeRule.waitForIdle()

        resultEventBus.sendContactAddedResult(addedContact)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_CONTACT_PICKER_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(SECOND_CONTACT_NAME).assertIsDisplayed()
    }

    private fun ComposeContentTestRule.selectContact(name: String) {
        openContactPicker()
        awaitContactPickerRows()
        contactDialogNodeWithText(name).performClick()
        closeDialogByBack()
    }
}
