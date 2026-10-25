package io.github.taetae98coding.diary.feature.contact.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactFilter
import io.github.taetae98coding.diary.core.model.contact.ContactFilterExistence
import io.github.taetae98coding.diary.domain.contact.usecase.DeleteContactUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.PageContactUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.RestoreContactUseCase
import io.github.taetae98coding.diary.feature.contact.ui.refreshableList
import io.github.taetae98coding.diary.feature.contact.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class ContactHomeFilterTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-da6da799-9cd6-4577-a51a-2231c6c1a828 처음에는 세 축이 모두 전체이고 목록을 거르지 않는다`() {
        val contactList = axisContactList()
        setContactHomeScreen(contactList = contactList)

        composeRule.onNodeWithContentDescription(DEFAULT_FILTER_DESCRIPTION).performClick()

        composeRule.onNodeWithContentDescription(segmentDescription(label = DEFAULT_BIRTHDAY_LABEL, value = DEFAULT_ALL)).assertIsSelected()
        composeRule.onNodeWithContentDescription(segmentDescription(label = DEFAULT_BIRTH_YEAR_LABEL, value = DEFAULT_ALL)).assertIsSelected()
        composeRule.onNodeWithContentDescription(segmentDescription(label = DEFAULT_PHONE_NUMBER_LABEL, value = DEFAULT_ALL)).assertIsSelected()
        contactList.forEach { contact -> composeRule.onNodeWithText(contact.detail.name).assertExists() }
    }

    @Test
    fun `TC-685d992b-bc6c-475b-863c-cd81286a6cac 생일 축에서 있음을 고르면 생일이 있는 연락처만 표시되고 필터는 열린 채 남는다`() {
        assertWithSelected(case = birthdayCase())
    }

    @Test
    fun `TC-685d992b-bc6c-475b-863c-cd81286a6cac 태어난 연도 축에서 있음을 고르면 태어난 연도가 있는 연락처만 표시되고 필터는 열린 채 남는다`() {
        assertWithSelected(case = birthYearCase())
    }

    @Test
    fun `TC-685d992b-bc6c-475b-863c-cd81286a6cac 전화번호 축에서 있음을 고르면 전화번호가 있는 연락처만 표시되고 필터는 열린 채 남는다`() {
        assertWithSelected(case = phoneNumberCase())
    }

    @Test
    fun `TC-39533d6b-1110-4ba8-a5b1-fb93eb983ac7 생일 축에서 없음을 고르면 생일이 없는 연락처만 표시된다`() {
        assertWithoutSelected(case = birthdayCase())
    }

    @Test
    fun `TC-39533d6b-1110-4ba8-a5b1-fb93eb983ac7 태어난 연도 축에서 없음을 고르면 태어난 연도가 없는 연락처만 표시된다`() {
        assertWithoutSelected(case = birthYearCase())
    }

    @Test
    fun `TC-39533d6b-1110-4ba8-a5b1-fb93eb983ac7 전화번호 축에서 없음을 고르면 전화번호가 없는 연락처만 표시된다`() {
        assertWithoutSelected(case = phoneNumberCase())
    }

    @Test
    fun `TC-0fe1d98c-c869-46a3-8b3a-b0b6417a2304 축을 전체로 되돌리면 그 축으로 거르지 않는다`() {
        val withPhoneNumber = testContact(name = displayText(), phoneNumberList = listOf(displayText()))
        val withoutPhoneNumber = testContact(name = displayText())
        setContactHomeScreen(contactList = listOf(withPhoneNumber, withoutPhoneNumber))
        composeRule.onNodeWithContentDescription(DEFAULT_FILTER_DESCRIPTION).performClick()
        composeRule.onNodeWithContentDescription(segmentDescription(label = DEFAULT_PHONE_NUMBER_LABEL, value = DEFAULT_WITH)).performClick()
        waitUntilNameDoesNotExist(withoutPhoneNumber.detail.name)

        composeRule.onNodeWithContentDescription(segmentDescription(label = DEFAULT_PHONE_NUMBER_LABEL, value = DEFAULT_ALL)).performClick()
        waitUntilNameExists(withoutPhoneNumber.detail.name)

        composeRule.onNodeWithText(withPhoneNumber.detail.name).assertExists()
        composeRule.onNodeWithContentDescription(segmentDescription(label = DEFAULT_PHONE_NUMBER_LABEL, value = DEFAULT_WITH)).assertIsNotSelected()
    }

    @Test
    fun `TC-3ad9d1cc-6efd-461c-b44e-d53ffc2d4c9b 필터를 여는 동작은 필터가 적용되었는지 알린다`() {
        val caseList =
            listOf(
                ContactFilter() to false,
                ContactFilter(birthday = ContactFilterExistence.EXIST) to true,
                ContactFilter(birthYear = ContactFilterExistence.EXIST) to true,
                ContactFilter(phoneNumber = ContactFilterExistence.NOT_EXIST) to true,
            )
        var filterUiState by mutableStateOf(ContactHomeFilterUiState())
        composeRule.setContent {
            DiaryTheme {
                ContactHomeScaffold(
                    onEvent = {},
                    filterUiStateProvider = { filterUiState },
                )
            }
        }

        caseList.forEach { (filter, isApplied) ->
            composeRule.runOnIdle { filterUiState = ContactHomeFilterUiState(filter = filter) }

            composeRule.onNodeWithContentDescription(DEFAULT_FILTER_DESCRIPTION).assertIsDisplayed()
            if (isApplied) {
                composeRule.onNode(hasStateDescription(DEFAULT_FILTER_APPLIED_DESCRIPTION)).assertExists()
            } else {
                composeRule.onNode(hasStateDescription(DEFAULT_FILTER_APPLIED_DESCRIPTION)).assertDoesNotExist()
            }
        }
    }

    @Test
    fun `TC-a4e6db31-3a33-47e2-a8d0-2e51aa2e3a32 필터를 적용해 맞는 연락처가 없으면 좁힌 빈 상태를 알린다`() {
        setContactHomeScreen(contactList = listOf(testContact(name = displayText(), phoneNumberList = listOf(displayText()))))
        composeRule.onNodeWithContentDescription(DEFAULT_FILTER_DESCRIPTION).performClick()

        composeRule.onNodeWithContentDescription(segmentDescription(label = DEFAULT_PHONE_NUMBER_LABEL, value = DEFAULT_WITHOUT)).performClick()
        waitUntilTextExists(DEFAULT_FILTERED_EMPTY_TITLE)

        composeRule.onNodeWithText(DEFAULT_FILTERED_EMPTY_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-16bb3020-8cc9-4a2b-90e2-f95426a9091d 연락처가 하나도 없어도 필터를 열 수 있다`() {
        setContactHomeScreen(contactList = emptyList())
        waitUntilTextExists(DEFAULT_EMPTY_TITLE)

        composeRule.onNodeWithContentDescription(DEFAULT_FILTER_DESCRIPTION).performClick()

        composeRule.onNodeWithText(DEFAULT_FILTER_TITLE).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(segmentDescription(label = DEFAULT_BIRTHDAY_LABEL, value = DEFAULT_ALL)).assertIsSelected()
        composeRule.onNodeWithContentDescription(segmentDescription(label = DEFAULT_BIRTH_YEAR_LABEL, value = DEFAULT_ALL)).assertIsSelected()
        composeRule.onNodeWithContentDescription(segmentDescription(label = DEFAULT_PHONE_NUMBER_LABEL, value = DEFAULT_ALL)).assertIsSelected()
    }

    @Test
    fun `TC-16bb3020-8cc9-4a2b-90e2-f95426a9091d 좁힌 결과가 비어 있어도 필터를 열 수 있다`() {
        val viewModel = contactHomeViewModel(contactList = listOf(testContact(name = displayText())))
        viewModel.setBirthdayExistence(existence = ContactFilterExistence.EXIST)
        composeRule.setContent { ContactHomeScreenContent(viewModel = viewModel) }
        waitUntilTextExists(DEFAULT_FILTERED_EMPTY_TITLE)

        composeRule.onNodeWithContentDescription(DEFAULT_FILTER_DESCRIPTION).performClick()

        composeRule.onNodeWithText(DEFAULT_FILTER_TITLE).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(segmentDescription(label = DEFAULT_BIRTHDAY_LABEL, value = DEFAULT_WITH)).assertIsSelected()
        composeRule.onNodeWithContentDescription(segmentDescription(label = DEFAULT_BIRTH_YEAR_LABEL, value = DEFAULT_ALL)).assertIsSelected()
        composeRule.onNodeWithContentDescription(segmentDescription(label = DEFAULT_PHONE_NUMBER_LABEL, value = DEFAULT_ALL)).assertIsSelected()
    }

    @Test
    fun `TC-fa5369e8-175d-4636-929a-bd601f89604a 필터 축을 바꾸면 목록을 처음부터 다시 본다`() {
        val contactList = positionContactList()
        setContactHomeScreen(contactList = contactList)
        waitUntilNameExists(contactList.first().detail.name)
        composeRule.refreshableList().performScrollToIndex(POSITION_SCROLL_INDEX)
        composeRule.onNodeWithText(contactList.first().detail.name).assertDoesNotExist()

        composeRule.onNodeWithContentDescription(DEFAULT_FILTER_DESCRIPTION).performClick()
        composeRule.onNodeWithContentDescription(segmentDescription(label = DEFAULT_BIRTHDAY_LABEL, value = DEFAULT_WITHOUT)).performClick()
        waitUntilNameExists(contactList.first().detail.name)

        composeRule.onNodeWithText(contactList.first().detail.name).assertExists()
    }

    @Test
    fun `TC-baa17f81-7f27-457b-9c4d-51ad900c1b38 이미 고른 축 상태를 다시 고르면 목록 위치를 되돌리지 않는다`() {
        val contactList = positionContactList()
        setContactHomeScreen(contactList = contactList)
        waitUntilNameExists(contactList.first().detail.name)
        composeRule.refreshableList().performScrollToIndex(POSITION_SCROLL_INDEX)

        composeRule.onNodeWithContentDescription(DEFAULT_FILTER_DESCRIPTION).performClick()
        composeRule.onNodeWithContentDescription(segmentDescription(label = DEFAULT_BIRTHDAY_LABEL, value = DEFAULT_ALL)).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(contactList.first().detail.name).assertDoesNotExist()
        composeRule.onNodeWithText(contactList[POSITION_SCROLL_INDEX].detail.name).assertExists()
    }

    @Test
    fun `TC-852f29ae-df6e-48e7-9930-771159a3ee43 화면이 재생성되어도 유무 필터 선택을 유지한다`() {
        val withBirthday = testContact(name = displayText(), birthday = birthday())
        val withoutBirthday = testContact(name = displayText())
        val viewModel = contactHomeViewModel(contactList = listOf(withBirthday, withoutBirthday))
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { ContactHomeScreenContent(viewModel = viewModel) }
        composeRule.onNodeWithContentDescription(DEFAULT_FILTER_DESCRIPTION).performClick()
        composeRule.onNodeWithContentDescription(segmentDescription(label = DEFAULT_BIRTHDAY_LABEL, value = DEFAULT_WITH)).performClick()
        waitUntilNameDoesNotExist(withoutBirthday.detail.name)

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.runOnIdle { viewModel.filterUiState.value.filter.birthday shouldBe ContactFilterExistence.EXIST }
        composeRule.onNodeWithText(withBirthday.detail.name).assertExists()
        composeRule.onNodeWithText(withoutBirthday.detail.name).assertDoesNotExist()
    }

    @Test
    fun `TC-aa5ba16e-90da-45c6-aeea-05b10ad39ada 시스템이 앱을 정리했다가 다시 만들면 유무 필터가 처음 상태로 돌아간다`() {
        val withPhoneNumber = testContact(name = displayText(), phoneNumberList = listOf(displayText()))
        val withoutPhoneNumber = testContact(name = displayText())
        val contactList = listOf(withPhoneNumber, withoutPhoneNumber)
        var viewModel = contactHomeViewModel(contactList = contactList)
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { ContactHomeScreenContent(viewModel = viewModel) }
        composeRule.onNodeWithContentDescription(DEFAULT_FILTER_DESCRIPTION).performClick()
        composeRule.onNodeWithContentDescription(segmentDescription(label = DEFAULT_PHONE_NUMBER_LABEL, value = DEFAULT_WITHOUT)).performClick()
        waitUntilNameDoesNotExist(withPhoneNumber.detail.name)

        // 시스템이 앱을 정리하면 화면 상태를 보관하던 객체도 사라지므로, 되살린 화면에는 새로 만든 객체를 준다.
        viewModel = contactHomeViewModel(contactList = contactList)
        restorationTester.emulateSavedInstanceStateRestore()
        waitUntilNameExists(withPhoneNumber.detail.name)

        composeRule.runOnIdle { viewModel.filterUiState.value shouldBe ContactHomeFilterUiState() }
        composeRule.onNodeWithText(withoutPhoneNumber.detail.name).assertExists()
        composeRule.onNode(hasStateDescription(DEFAULT_FILTER_APPLIED_DESCRIPTION)).assertDoesNotExist()
    }

    @Test
    fun `TC-5aacff14-029c-4c93-a4ab-4d47f077336d 필터를 닫았다가 다시 열어도 유무 필터 상태를 표시한다`() {
        val filterSheetState = DialogState(isVisible = true)
        var filterUiState by mutableStateOf(ContactHomeFilterUiState())
        composeRule.setContent {
            DiaryTheme {
                ContactHomeScaffold(
                    onEvent = { event ->
                        when (event) {
                            is ContactHomeScaffoldEvent.ClickFilter -> filterSheetState.show()
                            is ContactHomeScaffoldEvent.SetPhoneNumberExistence -> filterUiState = ContactHomeFilterUiState(filter = filterUiState.filter.copy(phoneNumber = event.existence))
                            else -> Unit
                        }
                    },
                    filterSheetState = filterSheetState,
                    filterUiStateProvider = { filterUiState },
                )
            }
        }
        composeRule.onNodeWithContentDescription(segmentDescription(label = DEFAULT_PHONE_NUMBER_LABEL, value = DEFAULT_WITH)).performClick()

        composeRule.runOnIdle { filterSheetState.hide() }
        composeRule.onNodeWithText(DEFAULT_FILTER_TITLE).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_FILTER_DESCRIPTION).performClick()

        composeRule.onNodeWithText(DEFAULT_FILTER_TITLE).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(segmentDescription(label = DEFAULT_PHONE_NUMBER_LABEL, value = DEFAULT_WITH)).assertIsSelected()
        composeRule.onNodeWithContentDescription(segmentDescription(label = DEFAULT_BIRTHDAY_LABEL, value = DEFAULT_ALL)).assertIsSelected()
    }

    @Test
    fun `TC-e567cdd5-51a3-4088-aaf3-7e56ed6508c4 필터를 연 채 화면이 재생성되어도 필터는 열린 채 남는다`() {
        val viewModel = contactHomeViewModel(contactList = axisContactList())
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { ContactHomeScreenContent(viewModel = viewModel) }
        composeRule.onNodeWithText(DEFAULT_FILTER_TITLE).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_FILTER_DESCRIPTION).performClick()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_FILTER_TITLE).assertIsDisplayed()
    }

    @Test
    fun `TC-e567cdd5-51a3-4088-aaf3-7e56ed6508c4 필터를 연 채 시스템이 앱을 정리했다가 다시 만들어도 필터는 열린 채 남는다`() {
        val contactList = axisContactList()
        var viewModel = contactHomeViewModel(contactList = contactList)
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { ContactHomeScreenContent(viewModel = viewModel) }
        composeRule.onNodeWithContentDescription(DEFAULT_FILTER_DESCRIPTION).performClick()

        viewModel = contactHomeViewModel(contactList = contactList)
        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_FILTER_TITLE).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(segmentDescription(label = DEFAULT_BIRTHDAY_LABEL, value = DEFAULT_ALL)).assertIsSelected()
    }

    @Test
    fun `TC-41761d0a-3df3-45c7-aa52-03bc7f80c69b 필터를 바꾸지 않은 채 목록의 내용이 바뀌면 보던 위치를 유지한다`() {
        val contactList = positionContactList()
        val contactListFlow = MutableStateFlow(contactList)
        val pageContactUseCase = mockk<PageContactUseCase>()
        every { pageContactUseCase(parameter = any()) } answers {
            val filter = firstArg<PageContactUseCase.Parameter>().filter
            contactListFlow.map { list -> Result.success(contactPagingDataOf(list.filter { contact -> contact.matches(filter) })) }
        }
        val viewModel =
            ContactHomeViewModel(
                pageContactUseCase = pageContactUseCase,
                deleteContactUseCase = mockk<DeleteContactUseCase>(),
                restoreContactUseCase = mockk<RestoreContactUseCase>(),
            )
        viewModel.setBirthdayExistence(existence = ContactFilterExistence.NOT_EXIST)
        val shownList = contactList.filter { contact -> contact.detail.birthday == null }
        composeRule.setContent { ContactHomeScreenContent(viewModel = viewModel) }
        waitUntilNameExists(shownList.first().detail.name)
        composeRule.refreshableList().performScrollToIndex(POSITION_SCROLL_INDEX / 2)
        val positionName = shownList[POSITION_SCROLL_INDEX / 2].detail.name
        composeRule.onNodeWithText(positionName).assertExists()

        val added = testContact(name = "position-999-${displayText()}")
        composeRule.runOnIdle { contactListFlow.value = contactList + added }
        waitUntilNameExists(positionName)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(positionName).assertExists()
        composeRule.onNodeWithText(shownList.first().detail.name).assertDoesNotExist()
    }

    @Test
    fun `필터를 적용한 동안 조건에 맞지 않는 연락처가 조회 결과에서 빠지면 화면에도 나타나지 않는다`() {
        val shown = testContact(name = displayText())
        val contactListFlow = MutableStateFlow(listOf(shown))
        val pageContactUseCase = mockk<PageContactUseCase>()
        every { pageContactUseCase(parameter = any()) } answers {
            val filter = firstArg<PageContactUseCase.Parameter>().filter
            contactListFlow.map { list -> Result.success(contactPagingDataOf(list.filter { contact -> contact.matches(filter) })) }
        }
        val viewModel =
            ContactHomeViewModel(
                pageContactUseCase = pageContactUseCase,
                deleteContactUseCase = mockk<DeleteContactUseCase>(),
                restoreContactUseCase = mockk<RestoreContactUseCase>(),
            )
        viewModel.setBirthdayExistence(existence = ContactFilterExistence.NOT_EXIST)
        composeRule.setContent { ContactHomeScreenContent(viewModel = viewModel) }
        waitUntilNameExists(shown.detail.name)

        val added = testContact(name = displayText(), birthday = birthday())
        val matchingAdded = testContact(name = displayText())
        composeRule.runOnIdle { contactListFlow.value = listOf(shown, added, matchingAdded) }
        waitUntilNameExists(matchingAdded.detail.name)

        composeRule.onNodeWithText(added.detail.name).assertDoesNotExist()
        composeRule.onNodeWithText(shown.detail.name).assertExists()
    }

    private fun assertWithSelected(case: AxisCase) {
        val viewModel = setContactHomeScreen(contactList = case.contactList)
        composeRule.onNodeWithContentDescription(DEFAULT_FILTER_DESCRIPTION).performClick()

        composeRule.onNodeWithContentDescription(segmentDescription(label = case.label, value = DEFAULT_WITH)).performClick()
        waitUntilNameDoesNotExist(case.without.detail.name)

        composeRule.onNodeWithContentDescription(segmentDescription(label = case.label, value = DEFAULT_WITH)).assertIsSelected()
        composeRule.onNodeWithText(DEFAULT_FILTER_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(case.with.detail.name).assertExists()
        viewModel.filterUiState.value.filter shouldBe case.filter(ContactFilterExistence.EXIST)
    }

    private fun assertWithoutSelected(case: AxisCase) {
        val viewModel = setContactHomeScreen(contactList = case.contactList)
        composeRule.onNodeWithContentDescription(DEFAULT_FILTER_DESCRIPTION).performClick()

        composeRule.onNodeWithContentDescription(segmentDescription(label = case.label, value = DEFAULT_WITHOUT)).performClick()
        waitUntilNameDoesNotExist(case.with.detail.name)

        composeRule.onNodeWithText(case.without.detail.name).assertExists()
        viewModel.filterUiState.value.filter shouldBe case.filter(ContactFilterExistence.NOT_EXIST)
    }

    @Composable
    private fun ContactHomeScreenContent(viewModel: ContactHomeViewModel) {
        DiaryTheme {
            ContactHomeScreen(
                navigateUp = {},
                navigateToAdd = {},
                navigateToDetail = {},
                componentVisibleProvider = { ContactHomeScaffoldComponentVisible() },
                contactViewModel = viewModel,
                syncViewModel = syncViewModel(),
            )
        }
    }

    private fun setContactHomeScreen(contactList: List<Contact>): ContactHomeViewModel {
        val viewModel = contactHomeViewModel(contactList = contactList)
        composeRule.setContent { ContactHomeScreenContent(viewModel = viewModel) }
        composeRule.waitForIdle()

        return viewModel
    }

    private fun contactHomeViewModel(contactList: List<Contact>): ContactHomeViewModel {
        val pageContactUseCase = mockk<PageContactUseCase>()
        every { pageContactUseCase(parameter = any()) } answers {
            val filter = firstArg<PageContactUseCase.Parameter>().filter
            flowOf(Result.success(contactPagingDataOf(contactList.filter { contact -> contact.matches(filter) })))
        }

        return ContactHomeViewModel(
            pageContactUseCase = pageContactUseCase,
            deleteContactUseCase = mockk<DeleteContactUseCase>(),
            restoreContactUseCase = mockk<RestoreContactUseCase>(),
        )
    }

    private fun syncViewModel(): SyncRefreshViewModel {
        val viewModel = mockk<SyncRefreshViewModel>()
        every { viewModel.uiState } returns MutableStateFlow(SyncRefreshUiState())
        justRun { viewModel.refresh() }

        return viewModel
    }

    private fun waitUntilNameExists(name: String) = waitUntilTextExists(name)

    private fun waitUntilTextExists(text: String) {
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitUntilNameDoesNotExist(name: String) {
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(name).fetchSemanticsNodes().isEmpty()
        }
    }

    private fun segmentDescription(
        label: String,
        value: String,
    ): String = "$label $value"

    private fun hasStateDescription(value: String): SemanticsMatcher = SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, value)

    private data class AxisCase(
        val label: String,
        val with: Contact,
        val without: Contact,
        val filter: (ContactFilterExistence) -> ContactFilter,
    ) {
        val contactList: List<Contact> = listOf(with, without)
    }

    private companion object {
        const val LIST_ITEM_TIMEOUT_MILLIS = 5_000L
        const val POSITION_CONTACT_COUNT = 200
        const val POSITION_SCROLL_INDEX = 150
        const val DEFAULT_FILTER_DESCRIPTION = "Filter"
        const val DEFAULT_FILTER_APPLIED_DESCRIPTION = "Filter applied"
        const val DEFAULT_FILTER_TITLE = "Filter"
        const val DEFAULT_BIRTHDAY_LABEL = "Birthday"
        const val DEFAULT_BIRTH_YEAR_LABEL = "Birth year"
        const val DEFAULT_PHONE_NUMBER_LABEL = "Phone number"
        const val DEFAULT_WITH = "With"
        const val DEFAULT_WITHOUT = "Without"
        const val DEFAULT_ALL = "All"
        const val DEFAULT_EMPTY_TITLE = "No contacts yet"
        const val DEFAULT_FILTERED_EMPTY_TITLE = "No contacts match the filter"
        const val DEFAULT_FILTERED_EMPTY_DESCRIPTION = "Change the filter to see other contacts."

        // 카드 문구를 정확히 찾아야 하므로 공백이나 제어 문자가 섞이지 않는 값으로 만든다.
        fun displayText(): String = fixtureMonkey.giveMeOne<Uuid>().toString()

        fun birthday(): ContactBirthday = fixtureMonkey.giveMeOne<ContactBirthday>()

        fun axisContactList(): List<Contact> =
            listOf(
                testContact(name = displayText(), birthday = birthday()),
                testContact(name = displayText(), birthYear = fixtureMonkey.giveMeOne<Int>()),
                testContact(name = displayText(), phoneNumberList = listOf(displayText())),
                testContact(name = displayText()),
            )

        fun birthdayCase(): AxisCase =
            AxisCase(
                label = DEFAULT_BIRTHDAY_LABEL,
                with = testContact(name = displayText(), birthday = birthday()),
                without = testContact(name = displayText()),
                filter = { existence -> ContactFilter(birthday = existence) },
            )

        fun birthYearCase(): AxisCase =
            AxisCase(
                label = DEFAULT_BIRTH_YEAR_LABEL,
                with = testContact(name = displayText(), birthYear = fixtureMonkey.giveMeOne<Int>()),
                without = testContact(name = displayText()),
                filter = { existence -> ContactFilter(birthYear = existence) },
            )

        fun phoneNumberCase(): AxisCase =
            AxisCase(
                label = DEFAULT_PHONE_NUMBER_LABEL,
                with = testContact(name = displayText(), phoneNumberList = listOf(displayText())),
                without = testContact(name = displayText()),
                filter = { existence -> ContactFilter(phoneNumber = existence) },
            )

        // 첫 연락처와 내려가 본 연락처가 모두 생일이 없는 쪽에 남아, 위치를 되돌리지 않으면 좁힌 뒤에도 첫 연락처가 보이지 않게 한다.
        fun positionContactList(): List<Contact> =
            List(POSITION_CONTACT_COUNT) { index ->
                testContact(
                    name = "position-${index.toString().padStart(length = 3, padChar = '0')}-${fixtureMonkey.giveMeOne<Int>()}",
                    birthday = if (index % 2 == 0) null else birthday(),
                )
            }

        fun Contact.matches(filter: ContactFilter): Boolean =
            filter.birthday.matches(hasValue = detail.birthday != null) &&
                filter.birthYear.matches(hasValue = detail.birthYear != null) &&
                filter.phoneNumber.matches(hasValue = detail.phoneNumberList.isNotEmpty())

        fun ContactFilterExistence.matches(hasValue: Boolean): Boolean =
            when (this) {
                ContactFilterExistence.ALL -> true
                ContactFilterExistence.EXIST -> hasValue
                ContactFilterExistence.NOT_EXIST -> !hasValue
            }
    }
}
