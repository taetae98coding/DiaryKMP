package io.github.taetae98coding.diary.feature.contact.ui.detail

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.feature.contact.ui.form.ContactFormState
import io.github.taetae98coding.diary.feature.contact.ui.form.rememberContactDetailFormState
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class ContactDetailScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-003 화면 제목에 저장된 연락처 이름을 표시한다`() {
        val detail = testContactDetail(name = CONTACT_NAME)

        setContactDetailScaffold(uiState = content(detail = detail))

        // 이름은 상단 바 제목과 이름 입력에 함께 나타나므로 편집할 수 없는 제목 노드를 지목한다.
        composeRule.onNode(hasText(CONTACT_NAME) and SemanticsMatcher.keyNotDefined(SemanticsProperties.EditableText)).assertExists()
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-004 조회 중에는 입력과 수정 삭제 동작을 제공하지 않는다`() {
        setContactDetailScaffold(uiState = ContactDetailUiState.Loading)

        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_NAME_LABEL).assertDoesNotExist()
    }

    @Test
    fun `조회에 성공하면 입력과 삭제 동작을 제공한다`() {
        setContactDetailScaffold(uiState = content())

        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithText(DEFAULT_NAME_LABEL).assertExists()
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-006 입력이 저장 내용과 같으면 수정 동작을 제공하지 않는다`() {
        setContactDetailScaffold(uiState = content())

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-006 입력이 저장 내용과 다르면 수정 동작을 제공한다`() {
        val detail = testContactDetail(name = CONTACT_NAME)
        lateinit var state: ContactFormState

        composeRule.setContent {
            DiaryTheme {
                state = rememberContactDetailFormState(initialDetail = detail)

                ContactDetailTestScaffold(
                    onEvent = {},
                    state = state,
                    uiStateProvider = { content(detail = detail) },
                )
            }
        }

        composeRule.onNode(hasText(CONTACT_NAME) and hasSetTextAction()).performTextInput("변경")
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-006 생일의 달력 구분만 바꿔도 수정 동작을 제공한다`() {
        val detail =
            testContactDetail(
                name = CONTACT_NAME,
                birthday = ContactBirthday(date = LocalDate(1994, 3, 21), calendar = ContactBirthdayCalendar.SOLAR),
            )
        lateinit var state: ContactFormState

        composeRule.setContent {
            DiaryTheme {
                state = rememberContactDetailFormState(initialDetail = detail)

                ContactDetailTestScaffold(
                    onEvent = {},
                    state = state,
                    uiStateProvider = { content(detail = detail) },
                )
            }
        }

        composeRule.onNodeWithText(BIRTHDAY_CALENDAR_LUNAR).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-013 삭제를 선택하면 삭제 행동을 한 번 전달한다`() {
        val eventList = mutableListOf<ContactDetailScaffoldEvent>()

        setContactDetailScaffold(uiState = content(), onEvent = eventList::add)
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).performClick()

        eventList shouldBe listOf(ContactDetailScaffoldEvent.ClickDelete)
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-016 삭제를 선택해도 확인 대화상자를 표시하지 않는다`() {
        setContactDetailScaffold(uiState = content())

        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_CONFIRM_TEXT).assertDoesNotExist()
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-021 뒤로가기를 선택하면 돌아가기 행동을 한 번 전달한다`() {
        val eventList = mutableListOf<ContactDetailScaffoldEvent>()

        setContactDetailScaffold(uiState = content(), onEvent = eventList::add)
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        eventList shouldBe listOf(ContactDetailScaffoldEvent.ClickNavigateUp)
    }

    @Test
    fun `목록과 함께 표시하는 상태에서는 뒤로가기 버튼을 표시하지 않는다`() {
        setContactDetailScaffold(
            uiState = content(),
            componentVisible = ContactDetailScaffoldComponentVisible(isNavigateUpButtonVisible = false),
        )

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 삭제 버튼 이름은 연락처 삭제이다`() {
        setContactDetailScaffold(uiState = content())

        composeRule.onNodeWithContentDescription(KOREAN_DELETE_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    private fun setContactDetailScaffold(
        uiState: ContactDetailUiState = ContactDetailUiState.Loading,
        onEvent: (ContactDetailScaffoldEvent) -> Unit = {},
        componentVisible: ContactDetailScaffoldComponentVisible = ContactDetailScaffoldComponentVisible(),
    ) {
        composeRule.setContent {
            DiaryTheme {
                ContactDetailTestScaffold(
                    onEvent = onEvent,
                    state = rememberContactDetailFormState(initialDetail = (uiState as? ContactDetailUiState.Content)?.detail ?: ContactDetail.EMPTY),
                    uiStateProvider = { uiState },
                    componentVisibleProvider = { componentVisible },
                )
            }
        }
    }

    private companion object {
        private const val CONTACT_NAME = "ContactDetailName"
        private const val BIRTHDAY_CALENDAR_LUNAR = "Lunar"
        private const val DEFAULT_NAME_LABEL = "Name"
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val DEFAULT_DELETE_BUTTON_DESCRIPTION = "Delete contact"
        private const val KOREAN_DELETE_BUTTON_DESCRIPTION = "연락처 삭제"
        private const val DEFAULT_UPDATE_BUTTON_DESCRIPTION = "Update contact"
        private const val DEFAULT_CONFIRM_TEXT = "Confirm"

        private fun content(detail: ContactDetail = testContactDetail()): ContactDetailUiState.Content = ContactDetailUiState.Content(id = Uuid.random(), detail = detail)
    }
}
