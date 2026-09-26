package io.github.taetae98coding.diary.feature.contact.ui.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.core.model.measure.Length.Companion.centimeter
import io.github.taetae98coding.diary.core.model.measure.Length.Companion.millimeter
import io.github.taetae98coding.diary.feature.contact.ui.add.DEFAULT_BIRTHDAY_CLEAR_DESCRIPTION
import io.github.taetae98coding.diary.feature.contact.ui.add.DEFAULT_BIRTHDAY_NOT_SET
import io.github.taetae98coding.diary.feature.contact.ui.add.DEFAULT_PHONE_NUMBER_BLANK_MESSAGE
import io.github.taetae98coding.diary.feature.contact.ui.add.descriptionInput
import io.github.taetae98coding.diary.feature.contact.ui.add.footSizeInput
import io.github.taetae98coding.diary.feature.contact.ui.add.heightInput
import io.github.taetae98coding.diary.feature.contact.ui.add.hometownInput
import io.github.taetae98coding.diary.feature.contact.ui.add.nameInput
import io.github.taetae98coding.diary.feature.contact.ui.add.phoneNumberInput
import io.github.taetae98coding.diary.feature.contact.ui.add.phoneNumberRowCount
import io.github.taetae98coding.diary.feature.contact.ui.detail.memo.CONTACT_DETAIL_MEMO_LIST_TEST_TAG
import io.github.taetae98coding.diary.feature.contact.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.datetime.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class ContactDetailScreenEditTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-019 화면이 재생성되어도 바꾼 모든 입력을 유지한다`() {
        val stored = storedDetail()
        val input = editedInput()
        val viewModel = screenTestViewModel(uiState = MutableStateFlow(ContactDetailUiState.Content(id = FIRST_CONTACT_ID, detail = stored)))
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
                    viewModel = viewModel,
                )
            }
        }
        composeRule.editAll(input = input)

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.assertEdited(input = input)
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-020 화면을 떠난 뒤 같은 연락처에 다시 들어오면 저장된 내용으로 다시 시작한다`() {
        val stored = storedDetail()
        val input = editedInput()
        val viewModel = screenTestViewModel(uiState = MutableStateFlow(ContactDetailUiState.Content(id = FIRST_CONTACT_ID, detail = stored)))
        var isShown by mutableStateOf(true)
        prepareContactDetailTabViewModels()
        composeRule.setContent {
            ContactDetailScreenTestHost {
                if (isShown) {
                    ContactDetailScreen(
                        navigateUp = {},
                        navigateToMemoAdd = {},
                        navigateToMemoDetail = {},
                        id = FIRST_CONTACT_ID,
                        componentVisibleProvider = { ContactDetailScaffoldComponentVisible() },
                        viewModel = viewModel,
                    )
                }
            }
        }
        composeRule.editAll(input = input)

        composeRule.runOnIdle { isShown = false }
        composeRule.waitForIdle()
        composeRule.runOnIdle { isShown = true }
        composeRule.waitForIdle()

        composeRule.nameInput().assert(hasText(stored.name))
        composeRule.descriptionInput().assert(hasText(stored.description))
        composeRule.heightInput().assert(hasText(STORED_HEIGHT_TEXT))
        composeRule.footSizeInput().assert(hasText(STORED_FOOT_SIZE_TEXT))
        composeRule.hometownInput().assert(hasText(stored.hometown))
        composeRule.onNodeWithText(STORED_BIRTHDAY_TEXT).assertExists()
        composeRule.phoneNumberRowCount() shouldBe stored.phoneNumberList.size
        composeRule.phoneNumberInput().assert(hasText(stored.phoneNumberList.first().number))
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-009 수정에 실패하면 안내 없이 입력을 유지하고 다시 수정할 수 있다`() {
        val stored = storedDetail()
        val input = editedInput()
        val viewModel = screenTestViewModel(uiState = MutableStateFlow(ContactDetailUiState.Content(id = FIRST_CONTACT_ID, detail = stored)))
        composeRule.setContactDetailScreen(viewModel = viewModel)
        composeRule.editAll(input = input)

        composeRule.clickUpdate()

        composeRule.assertEdited(input = input)
        composeRule.onNodeWithText(DEFAULT_UPDATE_SUCCEEDED_MESSAGE).assertDoesNotExist()

        composeRule.clickUpdate()

        verify(exactly = 2) { viewModel.update(detail = any()) }
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-010 이름을 비운 채 수정하면 비운 이름과 바꾼 설명으로 수정하고 성공을 알린다`() {
        val stored = storedDetail()
        val description = "설명-${fixtureMonkey.giveMeOne<String>()}"
        val viewModel = effectViewModel(stored = stored, effect = ContactDetailEffect.UpdateSucceeded)
        composeRule.setContactDetailScreen(viewModel = viewModel)
        composeRule.nameInput().performTextReplacement("")
        composeRule.descriptionInput().performTextReplacement(description)
        composeRule.waitForIdle()

        composeRule.clickUpdate()

        verify(exactly = 1) { viewModel.update(detail = stored.copy(name = "", description = description)) }
        composeRule.onNodeWithText(DEFAULT_UPDATE_SUCCEEDED_MESSAGE).assertExists()
        composeRule.onNode(hasText(stored.name) and SemanticsMatcher.keyNotDefined(SemanticsProperties.EditableText)).assertExists()
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-046 이름을 비운 채 수정해도 수정 동작이 계속 제공된다`() {
        val stored = storedDetail()
        val viewModel = effectViewModel(stored = stored, effect = ContactDetailEffect.UpdateSucceeded)
        composeRule.setContactDetailScreen(viewModel = viewModel)
        composeRule.nameInput().performTextReplacement("")
        composeRule.waitForIdle()

        composeRule.clickUpdate()

        composeRule.onNodeWithText(DEFAULT_UPDATE_SUCCEEDED_MESSAGE).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-012 번호가 비어 있는 전화번호 항목이 있으면 전화번호 입력을 알리고 입력을 유지한다`() {
        val stored = storedDetail()
        val input = editedInput()
        val viewModel = effectViewModel(stored = stored, effect = ContactDetailEffect.PhoneNumberBlank)
        composeRule.setContactDetailScreen(viewModel = viewModel)
        composeRule.editAll(input = input)
        composeRule.addPhoneNumberRowOnScreen()

        composeRule.clickUpdate()

        composeRule.onNodeWithText(DEFAULT_PHONE_NUMBER_BLANK_MESSAGE).assertExists()
        composeRule.assertEdited(input = input, blankPhoneNumberRowCount = 1)
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-045 전화번호 미입력으로 수정하면 보고 있던 입력에 초점을 그대로 둔다`() {
        val stored = storedDetail()
        val viewModel = effectViewModel(stored = stored, effect = ContactDetailEffect.PhoneNumberBlank)
        composeRule.setContactDetailScreen(viewModel = viewModel)
        composeRule.addPhoneNumberRowOnScreen()
        composeRule.heightInput().performTextReplacement(EDITED_HEIGHT_TEXT)
        composeRule.heightInput().assertIsFocused()

        composeRule.clickUpdate()

        composeRule.onNodeWithText(DEFAULT_PHONE_NUMBER_BLANK_MESSAGE).assertExists()
        composeRule.heightInput().assertIsFocused()
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-021 입력을 바꾼 채 뒤로가면 수정 없이 진입하기 전 화면으로 돌아간다`() {
        val stored = storedDetail()
        val viewModel = screenTestViewModel(uiState = MutableStateFlow(ContactDetailUiState.Content(id = FIRST_CONTACT_ID, detail = stored)))
        var navigateUpCount = 0
        composeRule.setContactDetailScreen(viewModel = viewModel, navigateUp = { navigateUpCount++ })
        composeRule.nameInput().performTextReplacement("이름-${fixtureMonkey.giveMeOne<String>()}")
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
        verify(exactly = 0) { viewModel.update(detail = any()) }
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-047 즐겨찾기인 연락처는 화면이 재생성되어도 즐겨찾기 표시를 유지한다`() {
        assertFavoriteRestored(isFavorite = true, expectedDescription = DEFAULT_UNFAVORITE_DESCRIPTION)
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-047 즐겨찾기가 아닌 연락처는 화면이 재생성되어도 즐겨찾기가 아닌 표시를 유지한다`() {
        assertFavoriteRestored(isFavorite = false, expectedDescription = DEFAULT_FAVORITE_DESCRIPTION)
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-048 탭을 전환해도 메모 탭에서 보던 목록 위치가 유지된다`() {
        val titleList = List(MEMO_COUNT) { index -> "메모-$index-${fixtureMonkey.giveMeOne<Int>()}" }
        val viewModel = screenTestViewModel(uiState = MutableStateFlow(ContactDetailUiState.Content(id = FIRST_CONTACT_ID, detail = storedDetail())))
        composeRule.setContactDetailScreen(
            viewModel = viewModel,
            memoPagingData = contactMemoPagingData(itemList = titleList.map { title -> MemoListItem.Content(memo = contactMemo(title = title)) }),
        )
        composeRule.selectContactDetailTab(contentDescription = DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.waitUntil { composeRule.onAllNodesWithText(titleList.first()).fetchSemanticsNodes().isNotEmpty() }
        composeRule.onNodeWithTag(CONTACT_DETAIL_MEMO_LIST_TEST_TAG).performScrollToNode(hasText(titleList.last()))
        composeRule.waitForIdle()
        composeRule.onNodeWithText(titleList.last()).assertIsDisplayed()

        composeRule.selectContactDetailTab(contentDescription = DEFAULT_DETAIL_TAB_DESCRIPTION)
        composeRule.selectContactDetailTab(contentDescription = DEFAULT_MEMO_TAB_DESCRIPTION)

        composeRule.onNodeWithText(titleList.last()).assertIsDisplayed()
        composeRule.onNodeWithText(titleList.first()).assertIsNotDisplayed()
    }

    private fun assertFavoriteRestored(
        isFavorite: Boolean,
        expectedDescription: String,
    ) {
        val viewModel =
            screenTestViewModel(
                uiState = MutableStateFlow(ContactDetailUiState.Content(id = FIRST_CONTACT_ID, detail = storedDetail(), isFavorite = isFavorite)),
            )
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
                    viewModel = viewModel,
                )
            }
        }
        composeRule.onNodeWithContentDescription(expectedDescription).assertExists()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(expectedDescription).assertExists()
    }

    private fun effectViewModel(
        stored: ContactDetail,
        effect: ContactDetailEffect,
    ): ContactDetailViewModel {
        val channel = Channel<ContactDetailEffect>(capacity = Channel.BUFFERED)
        val viewModel =
            screenTestViewModel(
                uiState = MutableStateFlow(ContactDetailUiState.Content(id = FIRST_CONTACT_ID, detail = stored)),
                effect = channel.receiveAsFlow(),
            )
        every { viewModel.update(detail = any()) } answers { channel.trySend(effect).getOrThrow() }
        return viewModel
    }

    // 이 창 높이에서는 전화번호 추가 버튼이 떠 있는 수정 버튼과 겹치는 자리에 놓이므로 좌표 대신 누름 동작으로 누른다.
    private fun ComposeContentTestRule.addPhoneNumberRowOnScreen() {
        onNodeWithContentDescription(DEFAULT_PHONE_NUMBER_ADD_DESCRIPTION).performSemanticsAction(SemanticsActions.OnClick)
        waitForIdle()
    }

    private fun ComposeContentTestRule.clickUpdate() {
        onNodeWithContentDescription(DEFAULT_UPDATE_DESCRIPTION).performClick()
        waitForIdle()
    }

    private fun ComposeContentTestRule.editAll(input: EditedInput) {
        nameInput().performTextReplacement(input.name)
        descriptionInput().performTextReplacement(input.description)
        heightInput().performTextReplacement(EDITED_HEIGHT_TEXT)
        footSizeInput().performTextReplacement(EDITED_FOOT_SIZE_TEXT)
        hometownInput().performTextReplacement(input.hometown)
        onNodeWithContentDescription(DEFAULT_BIRTHDAY_CLEAR_DESCRIPTION).performSemanticsAction(SemanticsActions.OnClick)
        waitForIdle()
        addPhoneNumberRowOnScreen()
        phoneNumberInput(row = 1).performTextInput(input.phoneNumber)
        waitForIdle()
    }

    private fun ComposeContentTestRule.assertEdited(
        input: EditedInput,
        blankPhoneNumberRowCount: Int = 0,
    ) {
        nameInput().assert(hasText(input.name))
        descriptionInput().assert(hasText(input.description))
        heightInput().assert(hasText(EDITED_HEIGHT_TEXT))
        footSizeInput().assert(hasText(EDITED_FOOT_SIZE_TEXT))
        hometownInput().assert(hasText(input.hometown))
        onNodeWithText(DEFAULT_BIRTHDAY_NOT_SET).assertExists()
        phoneNumberRowCount() shouldBe STORED_PHONE_NUMBER_COUNT + 1 + blankPhoneNumberRowCount
        phoneNumberInput(row = 1).assert(hasText(input.phoneNumber))
    }

    private data class EditedInput(
        val name: String,
        val description: String,
        val hometown: String,
        val phoneNumber: String,
    )

    private companion object {
        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()
        private const val STORED_HEIGHT_TEXT = "175.5"
        private const val STORED_FOOT_SIZE_TEXT = "250"
        private const val EDITED_HEIGHT_TEXT = "180"
        private const val EDITED_FOOT_SIZE_TEXT = "260"
        private const val STORED_BIRTHDAY_TEXT = "Mar 21, 1994"
        private const val STORED_PHONE_NUMBER_COUNT = 1
        private const val MEMO_COUNT = 40
        private const val DEFAULT_UPDATE_SUCCEEDED_MESSAGE = "Contact updated."
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val DEFAULT_PHONE_NUMBER_ADD_DESCRIPTION = "Add phone number"
        private const val DEFAULT_FAVORITE_DESCRIPTION = "Add to favorites"
        private const val DEFAULT_UNFAVORITE_DESCRIPTION = "Remove from favorites"

        private fun storedDetail(): ContactDetail =
            testContactDetail(
                name = "이름-${fixtureMonkey.giveMeOne<Int>()}",
                description = "설명-${fixtureMonkey.giveMeOne<Int>()}",
                height = 175.5.centimeter,
                footSize = 250.millimeter,
                birthday = ContactBirthday(date = LocalDate(year = 1994, month = 3, day = 21), calendar = ContactBirthdayCalendar.SOLAR),
                hometown = "고향-${fixtureMonkey.giveMeOne<Int>()}",
                phoneNumberList = List(STORED_PHONE_NUMBER_COUNT) { "010-${fixtureMonkey.giveMeOne<Int>()}" },
            )

        private fun editedInput(): EditedInput =
            EditedInput(
                name = "이름-${fixtureMonkey.giveMeOne<Int>()}",
                description = "설명-${fixtureMonkey.giveMeOne<Int>()}",
                hometown = "고향-${fixtureMonkey.giveMeOne<Int>()}",
                phoneNumber = "02-${fixtureMonkey.giveMeOne<Int>()}",
            )
    }
}
