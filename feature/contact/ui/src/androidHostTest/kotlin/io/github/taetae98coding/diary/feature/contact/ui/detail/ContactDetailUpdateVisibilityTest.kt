package io.github.taetae98coding.diary.feature.contact.ui.detail

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.core.model.measure.Length.Companion.centimeter
import io.github.taetae98coding.diary.core.model.measure.Length.Companion.millimeter
import io.github.taetae98coding.diary.feature.contact.ui.form.ContactFormState
import io.github.taetae98coding.diary.feature.contact.ui.form.rememberContactDetailFormState
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class ContactDetailUpdateVisibilityTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-006 설명을 바꾸면 수정 동작을 제공한다`() {
        assertUpdateVisible { detail -> descriptionState.setText("${detail.description}-${fixtureMonkey.giveMeOne<String>()}") }
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-006 키를 바꾸면 수정 동작을 제공한다`() {
        assertUpdateVisible { heightState.setTextAndPlaceCursorAtEnd(OTHER_HEIGHT_TEXT) }
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-006 신발 사이즈를 바꾸면 수정 동작을 제공한다`() {
        assertUpdateVisible { footSizeState.setTextAndPlaceCursorAtEnd(OTHER_FOOT_SIZE_TEXT) }
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-006 생일을 다른 날짜로 바꾸면 수정 동작을 제공한다`() {
        assertUpdateVisible { detail -> birthdayState.selectDate(checkNotNull(detail.birthday).date.plus(1, DateTimeUnit.DAY)) }
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-006 고향을 바꾸면 수정 동작을 제공한다`() {
        assertUpdateVisible { detail -> hometownState.setTextAndPlaceCursorAtEnd("${detail.hometown}-${fixtureMonkey.giveMeOne<String>()}") }
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-006 전화번호 항목을 추가하면 수정 동작을 제공한다`() {
        assertUpdateVisible { phoneNumberState.add() }
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-006 전화번호 항목을 삭제하면 수정 동작을 제공한다`() {
        assertUpdateVisible { phoneNumberState.remove(phoneNumberState.rowList.first()) }
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-006 바꾼 값을 저장된 값으로 되돌리면 수정 동작을 제공하지 않는다`() {
        val detail = storedDetail()
        lateinit var state: ContactFormState
        setScaffold(detail = detail) { value -> state = value }

        composeRule.runOnIdle { state.nameState.textFieldState.setTextAndPlaceCursorAtEnd("${detail.name}-${fixtureMonkey.giveMeOne<String>()}") }
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assert(hasClickAction())

        composeRule.runOnIdle { state.nameState.textFieldState.setTextAndPlaceCursorAtEnd(detail.name) }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-006 즐겨찾기만 바꾸면 수정 동작을 제공하지 않는다`() {
        val detail = storedDetail()
        var uiState by mutableStateOf(ContactDetailUiState.Content(id = fixtureMonkey.giveMeOne<Uuid>(), detail = detail, isFavorite = false))
        composeRule.setContent {
            DiaryTheme {
                ContactDetailTestScaffold(
                    onEvent = {},
                    state = rememberContactDetailFormState(initialDetail = detail),
                    uiStateProvider = { uiState },
                )
            }
        }

        composeRule.runOnIdle { uiState = uiState.copy(isFavorite = true) }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    private fun assertUpdateVisible(change: ContactFormState.(ContactDetail) -> Unit) {
        val detail = storedDetail()
        lateinit var state: ContactFormState
        setScaffold(detail = detail) { value -> state = value }
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()

        composeRule.runOnIdle { state.change(detail) }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    private fun setScaffold(
        detail: ContactDetail,
        onState: (ContactFormState) -> Unit,
    ) {
        val content = ContactDetailUiState.Content(id = fixtureMonkey.giveMeOne<Uuid>(), detail = detail)
        composeRule.setContent {
            DiaryTheme {
                val state = rememberContactDetailFormState(initialDetail = detail)
                onState(state)

                ContactDetailTestScaffold(
                    onEvent = {},
                    state = state,
                    uiStateProvider = { content },
                )
            }
        }
    }

    private companion object {
        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()
        private const val DEFAULT_UPDATE_BUTTON_DESCRIPTION = "Update contact"
        private const val OTHER_HEIGHT_TEXT = "180"
        private const val OTHER_FOOT_SIZE_TEXT = "260"

        private fun storedDetail(): ContactDetail =
            testContactDetail(
                height = 175.5.centimeter,
                footSize = 250.millimeter,
                birthday = ContactBirthday(date = LocalDate(year = 1994, month = 3, day = 21), calendar = ContactBirthdayCalendar.SOLAR),
                hometown = "고향-${fixtureMonkey.giveMeOne<String>()}",
                phoneNumberList = listOf("번호-${fixtureMonkey.giveMeOne<String>()}", "번호-${fixtureMonkey.giveMeOne<String>()}"),
            )
    }
}
