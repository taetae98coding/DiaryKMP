package io.github.taetae98coding.diary.feature.contact.ui.detail

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.compose.core.share.ContentSharer
import io.github.taetae98coding.diary.compose.core.share.ShareContent
import io.github.taetae98coding.diary.compose.core.share.ShareFileType
import io.github.taetae98coding.diary.compose.core.share.ShareResult
import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.feature.contact.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.contact.ui.add.DEFAULT_BIRTHDAY_CLEAR_DESCRIPTION
import io.github.taetae98coding.diary.feature.contact.ui.add.DEFAULT_PHONE_NUMBER_ADD_BUTTON_DESCRIPTION
import io.github.taetae98coding.diary.feature.contact.ui.add.DEFAULT_PHONE_NUMBER_BLANK_MESSAGE
import io.github.taetae98coding.diary.feature.contact.ui.add.nameInput
import io.github.taetae98coding.diary.feature.contact.ui.add.phoneNumberInput
import io.github.taetae98coding.diary.feature.contact.ui.add.selectBirthday
import io.github.taetae98coding.diary.feature.contact.ui.add.today
import io.github.taetae98coding.diary.feature.contact.ui.detailTagScreenTestViewModel
import io.github.taetae98coding.diary.feature.contact.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class ContactDetailShareTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-2dfcd9ff-bf0b-4b76-8967-3e878d1cf14c 조회 중에는 공유를 실행할 수 없다`() {
        composeRule.setContactDetailScreen(viewModel = screenTestViewModel(), contentSharerFactory = testContentSharerFactory(contentSharer()))

        composeRule.onNodeWithContentDescription(DEFAULT_SHARE_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-02d030ea-1b3a-43b5-852e-2eb67c1543e1 조회 중이면 연락처 파일 미지원 환경에서 공유가 없다`() {
        composeRule.setContactDetailScreen(viewModel = screenTestViewModel(), contentSharerFactory = testContentSharerFactory(contentSharer(isVCardSupported = false)))

        composeRule.onNodeWithContentDescription(DEFAULT_SHARE_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-02d030ea-1b3a-43b5-852e-2eb67c1543e1 내용 표시면 연락처 파일 미지원 환경에서 공유가 없다`() {
        composeRule.setContactDetailScreen(viewModel = contentViewModel(stored = storedDetail()), contentSharerFactory = testContentSharerFactory(contentSharer(isVCardSupported = false)))

        composeRule.onNodeWithContentDescription(DEFAULT_FAVORITE_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_SHARE_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-3e995da9-8832-4395-93b2-abd02793e929 지금 입력한 값으로 만든 연락처 파일을 요청한다`() {
        val sharer = contentSharer()
        val name = inputName()
        val phoneNumber = inputPhoneNumber()
        composeRule.setContactDetailScreen(viewModel = contentViewModel(stored = storedDetail()), contentSharerFactory = testContentSharerFactory(sharer))
        composeRule.nameInput().performTextReplacement(name)
        composeRule.phoneNumberInput().performTextReplacement(phoneNumber)
        composeRule.onNodeWithContentDescription(DEFAULT_BIRTHDAY_CLEAR_DESCRIPTION).performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()
        composeRule.selectBirthday()

        val content = composeRule.shareAndCapture(sharer = sharer)

        content.fileName shouldBe "$name.vcf"
        content.name shouldBe name
        content.type shouldBe ShareFileType.VCARD
        content.title shouldBe name
        content.text shouldBe ""
        content.bytes.decodeToString() shouldBe vCardOf(name = name, phoneNumberList = listOf(phoneNumber), birthday = today())
    }

    @Test
    fun `TC-a84fae3f-e541-4891-8b9f-b6d6d966a1dc 빈 전화번호 항목이 있어도 알리지 않고 공유한다`() {
        val sharer = contentSharer()
        val stored = storedDetail()
        composeRule.setContactDetailScreen(viewModel = contentViewModel(stored = stored), contentSharerFactory = testContentSharerFactory(sharer))
        composeRule.onNodeWithContentDescription(DEFAULT_PHONE_NUMBER_ADD_BUTTON_DESCRIPTION).performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()

        val content = composeRule.shareAndCapture(sharer = sharer)

        composeRule.onNodeWithText(DEFAULT_PHONE_NUMBER_BLANK_MESSAGE).assertDoesNotExist()
        content.bytes.decodeToString() shouldBe vCardOf(name = stored.name, phoneNumberList = stored.phoneNumberList.map { it.number }, birthday = requireNotNull(stored.birthday).date)
    }

    @Test
    fun `TC-4628220d-e926-40da-8f8b-b10f1644f141 공유하지 못하면 안내하고 화면을 유지한다`() {
        val sharer = contentSharer(result = ShareResult.FAILED)
        val name = inputName()
        composeRule.setContactDetailScreen(viewModel = contentViewModel(stored = storedDetail()), contentSharerFactory = testContentSharerFactory(sharer))
        composeRule.nameInput().performTextReplacement(name)
        composeRule.waitForIdle()

        composeRule.clickShare()

        composeRule.onNodeWithText(DEFAULT_SHARE_FAILED_MESSAGE).assertExists()
        composeRule.nameInput().assert(hasText(name))
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-b05d425a-f7da-4816-b083-418eca63080d 공유를 마치면 안내하지 않는다`() {
        assertNoFeedbackAfterShare(result = ShareResult.COMPLETED)
    }

    @Test
    fun `TC-b05d425a-f7da-4816-b083-418eca63080d 공유 시트를 닫으면 안내하지 않는다`() {
        assertNoFeedbackAfterShare(result = ShareResult.DISMISSED)
    }

    @Test
    fun `TC-d95f14af-9b49-4975-ac54-5ad298ec6325 공유해도 입력과 수정 동작이 남고 수정하지 않는다`() {
        val sharer = contentSharer()
        val viewModel = contentViewModel(stored = storedDetail())
        val name = inputName()
        val phoneNumber = inputPhoneNumber()
        composeRule.setContactDetailScreen(viewModel = viewModel, contentSharerFactory = testContentSharerFactory(sharer))
        composeRule.nameInput().performTextReplacement(name)
        composeRule.phoneNumberInput().performTextReplacement(phoneNumber)
        composeRule.waitForIdle()

        composeRule.shareAndCapture(sharer = sharer)

        composeRule.nameInput().assert(hasText(name))
        composeRule.phoneNumberInput().assert(hasText(phoneNumber))
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_DESCRIPTION).assert(hasClickAction())
        verify(exactly = 0) { viewModel.update(detail = any()) }
    }

    @Test
    fun `TC-05489736-ebb6-42bb-be2c-205facca809d 수정 중에도 공유한다`() {
        assertShareDuringProgress { content -> content.copy(isUpdateInProgress = true) }
    }

    @Test
    fun `TC-05489736-ebb6-42bb-be2c-205facca809d 즐겨찾기 변경 중에도 공유한다`() {
        assertShareDuringProgress { content -> content.copy(isFavoriteInProgress = true) }
    }

    @Test
    fun `TC-05489736-ebb6-42bb-be2c-205facca809d 삭제 중에도 공유한다`() {
        assertShareDuringProgress { content -> content.copy(isDeleteInProgress = true) }
    }

    @Test
    fun `TC-e6086d68-ec6e-400e-b03c-8d71934cdea4 메모 탭에서도 입력 중인 이름으로 공유하고 탭을 유지한다`() {
        val sharer = contentSharer()
        val name = inputName()
        composeRule.setContactDetailScreen(viewModel = contentViewModel(stored = storedDetail()), contentSharerFactory = testContentSharerFactory(sharer))
        composeRule.nameInput().performTextReplacement(name)
        composeRule.waitForIdle()
        composeRule.selectContactDetailTab(contentDescription = DEFAULT_MEMO_TAB_DESCRIPTION)

        val content = composeRule.shareAndCapture(sharer = sharer)

        content.name shouldBe name
        content.bytes
            .decodeToString()
            .split("\r\n")
            .filter { line -> line.startsWith("FN:") } shouldBe listOf("FN:$name")
        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()
    }

    @Test
    fun `TC-c4bf9d12-8685-49ae-bc8a-78f610c1dfcc 재생성되어도 공유를 다시 시작하지 않는다`() {
        val sharer = contentSharer()
        coEvery { sharer.share(content = any()) } coAnswers { awaitCancellation() }
        val viewModel = contentViewModel(stored = storedDetail())
        val contentSharerFactory = testContentSharerFactory(sharer)
        val restorationTester = StateRestorationTester(composeRule)
        prepareContactDetailTabViewModels()
        restorationTester.setContent {
            ContactDetailScreenTestHost {
                ContactDetailScreen(
                    contentSharerFactory = contentSharerFactory,
                    navigateUp = {},
                    navigateToMemoAdd = {},
                    navigateToMemoDetail = {},
                    navigateToMemoFinishedList = {},
                    id = FIRST_CONTACT_ID,
                    componentVisibleProvider = { ContactDetailScaffoldComponentVisible() },
                    contactViewModel = viewModel,
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    tagViewModel = detailTagScreenTestViewModel(),
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.clickShare()
        coVerify(exactly = 1) { sharer.share(content = any()) }

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        coVerify(exactly = 1) { sharer.share(content = any()) }
        composeRule.onNodeWithContentDescription(DEFAULT_SHARE_DESCRIPTION).assertIsEnabled()
    }

    private fun assertNoFeedbackAfterShare(result: ShareResult) {
        val sharer = contentSharer(result = result)
        composeRule.setContactDetailScreen(viewModel = contentViewModel(stored = storedDetail()), contentSharerFactory = testContentSharerFactory(sharer))

        composeRule.shareAndCapture(sharer = sharer)

        composeRule.onNodeWithText(DEFAULT_SHARE_FAILED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_PHONE_NUMBER_BLANK_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UPDATE_SUCCEEDED_MESSAGE).assertDoesNotExist()
    }

    private fun assertShareDuringProgress(progress: (ContactDetailUiState.Content) -> ContactDetailUiState.Content) {
        val sharer = contentSharer()
        val content = progress(ContactDetailUiState.Content(id = FIRST_CONTACT_ID, detail = storedDetail()))
        composeRule.setContactDetailScreen(viewModel = screenTestViewModel(uiState = MutableStateFlow(content)), contentSharerFactory = testContentSharerFactory(sharer))

        composeRule.onNodeWithContentDescription(DEFAULT_SHARE_DESCRIPTION).assertIsEnabled()
        composeRule.clickShare()

        coVerify(exactly = 1) { sharer.share(content = any()) }
    }

    private fun ComposeContentTestRule.clickShare() {
        onNodeWithContentDescription(DEFAULT_SHARE_DESCRIPTION).performClick()
        waitForIdle()
    }

    private fun ComposeContentTestRule.shareAndCapture(sharer: ContentSharer): ShareContent.File {
        val slot = slot<ShareContent>()

        clickShare()

        coVerify(exactly = 1) { sharer.share(content = capture(slot)) }
        return slot.captured.shouldBeInstanceOf<ShareContent.File>()
    }

    private companion object {
        private const val DEFAULT_SHARE_DESCRIPTION = "Share"
        private const val DEFAULT_FAVORITE_DESCRIPTION = "Add to favorites"
        private const val DEFAULT_SHARE_FAILED_MESSAGE = "Couldn't share."
        private const val DEFAULT_UPDATE_SUCCEEDED_MESSAGE = "Contact updated."

        private fun contentSharer(
            isVCardSupported: Boolean = true,
            result: ShareResult = ShareResult.COMPLETED,
        ): ContentSharer =
            mockk<ContentSharer> {
                every { isTextSupported } returns isVCardSupported
                every { isFileSupported(type = any()) } answers { isVCardSupported && firstArg<ShareFileType>() == ShareFileType.VCARD }
                coEvery { share(content = any()) } returns result
            }

        private fun contentViewModel(stored: ContactDetail): ContactDetailViewModel = screenTestViewModel(uiState = MutableStateFlow(ContactDetailUiState.Content(id = FIRST_CONTACT_ID, detail = stored)))

        private fun storedDetail(): ContactDetail =
            testContactDetail(
                name = "저장-${Arb.int().next()}",
                birthday = ContactBirthday(date = Arb.kotlinxLocalDate().next(), calendar = ContactBirthdayCalendar.SOLAR),
                phoneNumberList = listOf("010-${Arb.int().next()}"),
            )

        private fun inputName(): String = "이름-${Arb.int().next()}"

        private fun inputPhoneNumber(): String = "02-${Arb.int().next()}"

        private fun vCardOf(
            name: String,
            phoneNumberList: List<String>,
            birthday: LocalDate,
        ): String =
            buildString {
                append("BEGIN:VCARD\r\nVERSION:3.0\r\n")
                append("N:$name\r\nFN:$name\r\n")
                phoneNumberList.forEach { number -> append("TEL:$number\r\n") }
                append("BDAY:${birthday.year}-${birthday.month.number.toString().padStart(2, '0')}-${birthday.day.toString().padStart(2, '0')}\r\n")
                append("END:VCARD\r\n")
            }
    }
}
