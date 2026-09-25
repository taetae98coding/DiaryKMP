package io.github.taetae98coding.diary.feature.contact.ui.detail

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.feature.contact.ui.add.DEFAULT_BIRTHDAY_CLEAR_DESCRIPTION
import io.github.taetae98coding.diary.feature.contact.ui.add.DEFAULT_BIRTHDAY_NOT_SET
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.plus
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class ContactDetailScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-017 저장된 이름이 바뀌면 화면 제목만 갱신하고 입력은 덮어쓰지 않는다`() {
        val id = Uuid.random()
        val stored = testContactDetail(name = CONTACT_NAME)
        val uiState = MutableStateFlow<ContactDetailUiState>(ContactDetailUiState.Content(id = id, detail = stored))

        setContactDetailScreen(uiState = uiState)
        composeRule.onNode(hasText(CONTACT_NAME) and hasSetTextAction()).performTextReplacement(EDITING_NAME)
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            uiState.value = ContactDetailUiState.Content(id = id, detail = stored.copy(name = CHANGED_NAME))
        }
        composeRule.waitForIdle()

        composeRule
            .onNode(hasText(CHANGED_NAME) and SemanticsMatcher.keyNotDefined(SemanticsProperties.EditableText))
            .assertExists()
        composeRule.onNode(hasText(EDITING_NAME) and hasSetTextAction()).assertExists()
    }

    @Test
    fun `TC-CONTACT-DETAIL-DOMAIN-002 같은 연락처의 저장 내용이 바뀌어도 입력을 다시 채우지 않는다`() {
        val id = Uuid.random()
        val stored = testContactDetail(name = CONTACT_NAME)
        val uiState = MutableStateFlow<ContactDetailUiState>(ContactDetailUiState.Content(id = id, detail = stored))

        setContactDetailScreen(uiState = uiState)
        composeRule.onNode(hasText(CONTACT_NAME) and hasSetTextAction()).performTextReplacement(EDITING_NAME)
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            uiState.value = ContactDetailUiState.Content(id = id, detail = stored.copy(name = CHANGED_NAME))
        }
        composeRule.waitForIdle()

        composeRule.onNode(hasText(EDITING_NAME) and hasSetTextAction()).assertExists()
        composeRule.onNode(hasText(CHANGED_NAME) and hasSetTextAction()).assertDoesNotExist()
    }

    @Test
    fun `TC-CONTACT-DETAIL-DOMAIN-002 저장된 생일이 다른 경로에서 바뀌어도 입력 중인 생일은 덮어쓰지 않는다`() {
        val id = Uuid.random()
        val birthdayRange = fixtureMonkey.giveMeOne<LocalDateRange>()
        val stored = testContactDetail(name = CONTACT_NAME, birthday = ContactBirthday(date = birthdayRange.start, calendar = ContactBirthdayCalendar.SOLAR))
        val uiState = MutableStateFlow<ContactDetailUiState>(ContactDetailUiState.Content(id = id, detail = stored))

        setContactDetailScreen(uiState = uiState)
        composeRule.onNodeWithContentDescription(DEFAULT_BIRTHDAY_CLEAR_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            uiState.value =
                ContactDetailUiState.Content(
                    id = id,
                    detail = stored.copy(birthday = ContactBirthday(date = birthdayRange.endInclusive.plus(1, DateTimeUnit.DAY), calendar = ContactBirthdayCalendar.LUNAR)),
                )
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_NOT_SET).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_BIRTHDAY_CLEAR_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-019 화면이 재생성되어도 입력 중이던 내용을 유지한다`() {
        val id = Uuid.random()
        val stored = testContactDetail(name = CONTACT_NAME)
        val uiState = MutableStateFlow<ContactDetailUiState>(ContactDetailUiState.Content(id = id, detail = stored))
        val restorationTester = StateRestorationTester(composeRule)

        restorationTester.setContent {
            DiaryTheme {
                ContactDetailScreen(
                    navigateUp = {},
                    navigateToMemoAdd = {},
                    navigateToMemoDetail = {},
                    id = id,
                    componentVisibleProvider = { ContactDetailScaffoldComponentVisible() },
                    viewModel = viewModel(uiState = uiState),
                )
            }
        }

        composeRule.onNode(hasText(CONTACT_NAME) and hasSetTextAction()).performTextReplacement(EDITING_NAME)
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNode(hasText(EDITING_NAME) and hasSetTextAction()).assertExists()
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-020 다른 연락처로 바뀌면 저장된 내용으로 다시 시작한다`() {
        val stored = testContactDetail(name = CONTACT_NAME)
        val other = testContactDetail(name = CHANGED_NAME)
        val uiState = MutableStateFlow<ContactDetailUiState>(ContactDetailUiState.Content(id = Uuid.random(), detail = stored))

        setContactDetailScreen(uiState = uiState)
        composeRule.onNode(hasText(CONTACT_NAME) and hasSetTextAction()).performTextReplacement(EDITING_NAME)
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            uiState.value = ContactDetailUiState.Content(id = Uuid.random(), detail = other)
        }
        composeRule.waitForIdle()

        composeRule.onNode(hasText(CHANGED_NAME) and hasSetTextAction()).assertExists()
        composeRule.onNodeWithText(EDITING_NAME).assertDoesNotExist()
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-013 삭제에 성공하면 화면을 떠난다`() {
        val effect = MutableStateFlow<ContactDetailEffect?>(null)
        var navigateUpCount = 0

        setContactDetailScreen(
            uiState = MutableStateFlow(ContactDetailUiState.Content(id = Uuid.random(), detail = testContactDetail(name = CONTACT_NAME))),
            effect = kotlinx.coroutines.flow.flow { effect.collect { value -> value?.let { emit(it) } } },
            navigateUp = { navigateUpCount++ },
        )

        composeRule.runOnIdle { effect.value = ContactDetailEffect.DeleteSucceeded }
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
    }

    private fun setContactDetailScreen(
        uiState: MutableStateFlow<ContactDetailUiState>,
        effect: Flow<ContactDetailEffect> = emptyFlow(),
        navigateUp: () -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                ContactDetailScreen(
                    navigateUp = navigateUp,
                    navigateToMemoAdd = {},
                    navigateToMemoDetail = {},
                    id = FIRST_CONTACT_ID,
                    componentVisibleProvider = { ContactDetailScaffoldComponentVisible() },
                    viewModel = viewModel(uiState = uiState, effect = effect),
                )
            }
        }
    }

    private companion object {
        private const val CONTACT_NAME = "ContactDetailScreenName"
        private const val CHANGED_NAME = "ContactDetailScreenChanged"
        private const val EDITING_NAME = "ContactDetailScreenEditing"
        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        private fun viewModel(
            uiState: MutableStateFlow<ContactDetailUiState>,
            effect: Flow<ContactDetailEffect> = emptyFlow(),
        ): ContactDetailViewModel {
            val viewModel = mockk<ContactDetailViewModel>()
            every { viewModel.uiState } returns uiState
            every { viewModel.effect } returns effect
            justRun { viewModel.update(detail = any<ContactDetail>()) }
            justRun { viewModel.delete() }

            return viewModel
        }
    }
}
