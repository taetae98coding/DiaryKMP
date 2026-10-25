package io.github.taetae98coding.diary.feature.contact.ui.form

import androidx.compose.foundation.text.input.TextFieldState
import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate

class ContactBirthdayInputStateTest :
    FunSpec({
        test("TC-f9703b6d-e2ce-49a0-8f26-62e04f62ce88 생일을 고르면 달력 구분이 양력으로 시작한다") {
            val state = ContactBirthdayInputState(birthday = null)

            state.selectDate(FIRST_DATE)

            state.birthday shouldBe ContactBirthday(date = FIRST_DATE, calendar = ContactBirthdayCalendar.SOLAR)
        }

        test("TC-2ec1bd13-5fec-4878-a5d0-20b3897acdf8 고른 생일의 달력 구분을 바꿀 수 있다") {
            calendarChangeCaseList.forEach { (current, selected) ->
                val state = ContactBirthdayInputState(birthday = ContactBirthday(date = FIRST_DATE, calendar = current))

                state.selectCalendar(selected)

                state.birthday shouldBe ContactBirthday(date = FIRST_DATE, calendar = selected)
            }
        }

        test("TC-34577e79-b042-4b35-a57c-cb9844ce4715 생일을 지우면 달력 구분도 함께 사라진다") {
            val state = ContactBirthdayInputState(birthday = ContactBirthday(date = FIRST_DATE, calendar = ContactBirthdayCalendar.LUNAR))

            state.clear()

            state.birthday.shouldBeNull()
        }

        test("TC-ea8e375a-09fd-4c7f-b252-34e5728b6f27 생일을 지운 뒤 다시 고르면 달력 구분이 양력으로 시작한다") {
            val state = ContactBirthdayInputState(birthday = ContactBirthday(date = FIRST_DATE, calendar = ContactBirthdayCalendar.LUNAR))

            state.clear()
            state.selectDate(SECOND_DATE)

            state.birthday shouldBe ContactBirthday(date = SECOND_DATE, calendar = ContactBirthdayCalendar.SOLAR)
        }

        test("TC-13b450a4-db32-4a85-ad4b-f8d6102a596c 생일 날짜만 다시 골라도 달력 구분은 유지된다") {
            val state = ContactBirthdayInputState(birthday = ContactBirthday(date = FIRST_DATE, calendar = ContactBirthdayCalendar.LUNAR))

            state.selectDate(SECOND_DATE)

            state.birthday shouldBe ContactBirthday(date = SECOND_DATE, calendar = ContactBirthdayCalendar.LUNAR)
        }

        test("TC-740164fd-5d79-4e55-92c2-c031d227546e 생일을 고르면 태어난 연도가 생일의 연도로 채워진다") {
            listOf("", OTHER_BIRTH_YEAR).forEach { typed ->
                val state = ContactBirthdayInputState(birthday = null, birthYearState = TextFieldState(initialText = typed))

                state.selectDate(BIRTHDAY_1990)

                state.birthYearState.text.toString() shouldBe "1990"
                state.birthYear shouldBe 1990
            }
        }

        test("TC-9c2bf7e1-0400-412d-bc9a-36e8b828358f 생일의 날짜를 다시 고르면 태어난 연도도 다시 고른 연도로 바뀐다") {
            val state = ContactBirthdayInputState(birthday = null)
            state.selectDate(BIRTHDAY_1990)

            state.selectDate(BIRTHDAY_1992)

            state.birthYearState.text.toString() shouldBe "1992"
            state.birthYear shouldBe 1992
        }

        test("TC-1d552830-040d-430a-bc68-69c925828c85 생일의 달력 구분을 바꿔도 태어난 연도는 그대로다") {
            val state = ContactBirthdayInputState(birthday = null)
            state.selectDate(LocalDate(year = 1990, month = 12, day = 20))

            state.selectCalendar(ContactBirthdayCalendar.LUNAR)

            state.birthYear shouldBe 1990
        }

        test("TC-66993555-f993-40be-8bbd-6900a1d37651 생일을 지우면 태어난 연도도 지워진다") {
            val state = ContactBirthdayInputState(birthday = null)
            state.selectDate(BIRTHDAY_1990)

            state.clear()

            state.birthYearState.text.toString() shouldBe ""
            state.birthYear.shouldBeNull()
        }

        test("생일 없이 입력한 태어난 연도는 그 수로 읽는다") {
            listOf("0990" to 990, "2999" to 2999, "" to null).forEach { (typed, expected) ->
                val state = ContactBirthdayInputState(birthday = null, birthYearState = TextFieldState(initialText = typed))

                state.birthYear shouldBe expected
            }
        }

        test("생일을 고르지 않은 동안에는 달력 구분을 바꿔도 생일이 생기지 않는다") {
            val state = ContactBirthdayInputState(birthday = null)

            state.selectCalendar(ContactBirthdayCalendar.LUNAR)

            state.birthday.shouldBeNull()
        }
    }) {
    public companion object {
        private val FIRST_DATE: LocalDate = LocalDate(year = 1998, month = 5, day = 12)
        private val SECOND_DATE: LocalDate = LocalDate(year = 1994, month = 3, day = 21)
        private val BIRTHDAY_1990: LocalDate = LocalDate(year = 1990, month = 5, day = 5)
        private val BIRTHDAY_1992: LocalDate = LocalDate(year = 1992, month = 3, day = 1)
        private const val OTHER_BIRTH_YEAR = "1985"

        private val calendarChangeCaseList: List<Pair<ContactBirthdayCalendar, ContactBirthdayCalendar>> =
            listOf(
                ContactBirthdayCalendar.SOLAR to ContactBirthdayCalendar.LUNAR,
                ContactBirthdayCalendar.LUNAR to ContactBirthdayCalendar.SOLAR,
            )
    }
}
