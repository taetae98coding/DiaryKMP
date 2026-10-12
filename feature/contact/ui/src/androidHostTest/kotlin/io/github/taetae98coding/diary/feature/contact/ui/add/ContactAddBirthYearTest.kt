package io.github.taetae98coding.diary.feature.contact.ui.add

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class ContactAddBirthYearTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CONTACT-ADD-FEATURE-039 생일 없이 태어난 연도를 입력하고 지울 수 있다`() {
        composeRule.setContactAddScreen()

        composeRule.birthYearInput().performTextInput(TYPED_BIRTH_YEAR)
        composeRule.waitForIdle()

        composeRule.birthYearInput().assert(hasText(TYPED_BIRTH_YEAR))

        composeRule.onNodeWithContentDescription(DEFAULT_CLEAR_TEXT_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.birthYearInput().assert(hasText(""))
    }

    @Test
    fun `TC-CONTACT-ADD-FEATURE-040 태어난 연도는 반각 숫자를 네 자리까지만 반영한다`() {
        val caseList =
            listOf(
                "19a90" to "1990",
                "19.90" to "1990",
                "19901" to "1990",
                "１９９０" to "",
                "19９0" to "190",
            )
        composeRule.setContactAddScreen()

        caseList.forEach { (typed, expected) ->
            composeRule.birthYearInput().performTextInput(typed)
            composeRule.waitForIdle()

            composeRule.birthYearInput().assert(hasText(expected))

            composeRule.birthYearInput().performTextClearance()
            composeRule.waitForIdle()
        }
    }

    @Test
    fun `TC-CONTACT-ADD-FEATURE-041 생일을 고르면 태어난 연도가 생일의 연도로 채워진다`() {
        composeRule.setContactAddScreen()

        listOf("", OTHER_BIRTH_YEAR).forEach { typed ->
            composeRule.birthYearInput().performTextInput(typed)

            composeRule.selectBirthday()

            composeRule.birthYearInput().assert(hasText(today().year.toString()))

            composeRule.onNodeWithContentDescription(DEFAULT_BIRTHDAY_CLEAR_DESCRIPTION).performClick()
            composeRule.waitForIdle()
        }
    }

    @Test
    fun `TC-CONTACT-ADD-FEATURE-042 생일을 고른 동안에는 태어난 연도를 바꾸거나 지울 수 없다`() {
        composeRule.setContactAddScreen()

        composeRule.selectBirthday()

        composeRule.birthYearInput().assert(hasText(today().year.toString()))
        composeRule.birthYearInput().assertIsNotEnabled()
        composeRule.onAllNodesWithContentDescription(DEFAULT_CLEAR_TEXT_DESCRIPTION).fetchSemanticsNodes().size shouldBe 0
        composeRule.onNodeWithText(DEFAULT_BIRTH_YEAR_FROM_BIRTHDAY).assertExists()
    }

    @Test
    fun `TC-CONTACT-ADD-FEATURE-044 생일의 달력 구분을 바꿔도 태어난 연도는 그대로다`() {
        composeRule.setContactAddScreen()
        composeRule.selectBirthday()

        composeRule.selectBirthdayCalendar(DEFAULT_BIRTHDAY_CALENDAR_LUNAR)

        composeRule.birthYearInput().assert(hasText(today().year.toString()))
    }

    @Test
    fun `TC-CONTACT-ADD-FEATURE-045 생일을 지우면 태어난 연도도 지워지고 다시 입력할 수 있다`() {
        composeRule.setContactAddScreen()
        composeRule.selectBirthday()

        composeRule.onNodeWithContentDescription(DEFAULT_BIRTHDAY_CLEAR_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.birthYearInput().assert(hasText(""))
        composeRule.birthYearInput().assertIsEnabled()
        composeRule.onNodeWithText(DEFAULT_BIRTH_YEAR_FROM_BIRTHDAY).assertDoesNotExist()

        composeRule.birthYearInput().performTextInput(OTHER_BIRTH_YEAR)
        composeRule.waitForIdle()

        composeRule.birthYearInput().assert(hasText(OTHER_BIRTH_YEAR))
    }

    private companion object {
        private const val DEFAULT_CLEAR_TEXT_DESCRIPTION = "Clear text"
        private const val OTHER_BIRTH_YEAR = "1985"
    }
}
