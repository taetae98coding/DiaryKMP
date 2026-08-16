package io.github.taetae98coding.diary.feature.contact.ui.form

import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate

class ContactBirthdayInputStateTest :
    FunSpec({
        test("TC-CONTACT-ADD-FEATURE-022 생일을 고르면 달력 구분이 양력으로 시작한다") {
            val state = ContactBirthdayInputState(birthday = null)

            state.selectDate(FIRST_DATE)

            state.birthday shouldBe ContactBirthday(date = FIRST_DATE, calendar = ContactBirthdayCalendar.SOLAR)
        }

        test("TC-CONTACT-ADD-FEATURE-023 고른 생일의 달력 구분을 바꿀 수 있다") {
            calendarChangeCaseList.forEach { (current, selected) ->
                val state = ContactBirthdayInputState(birthday = ContactBirthday(date = FIRST_DATE, calendar = current))

                state.selectCalendar(selected)

                state.birthday shouldBe ContactBirthday(date = FIRST_DATE, calendar = selected)
            }
        }

        test("TC-CONTACT-ADD-FEATURE-024 생일을 지우면 달력 구분도 함께 사라진다") {
            val state = ContactBirthdayInputState(birthday = ContactBirthday(date = FIRST_DATE, calendar = ContactBirthdayCalendar.LUNAR))

            state.clear()

            state.birthday.shouldBeNull()
        }

        test("TC-CONTACT-ADD-FEATURE-025 생일을 지운 뒤 다시 고르면 달력 구분이 양력으로 시작한다") {
            val state = ContactBirthdayInputState(birthday = ContactBirthday(date = FIRST_DATE, calendar = ContactBirthdayCalendar.LUNAR))

            state.clear()
            state.selectDate(SECOND_DATE)

            state.birthday shouldBe ContactBirthday(date = SECOND_DATE, calendar = ContactBirthdayCalendar.SOLAR)
        }

        test("TC-CONTACT-ADD-FEATURE-027 생일 날짜만 다시 골라도 달력 구분은 유지된다") {
            val state = ContactBirthdayInputState(birthday = ContactBirthday(date = FIRST_DATE, calendar = ContactBirthdayCalendar.LUNAR))

            state.selectDate(SECOND_DATE)

            state.birthday shouldBe ContactBirthday(date = SECOND_DATE, calendar = ContactBirthdayCalendar.LUNAR)
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

        private val calendarChangeCaseList: List<Pair<ContactBirthdayCalendar, ContactBirthdayCalendar>> =
            listOf(
                ContactBirthdayCalendar.SOLAR to ContactBirthdayCalendar.LUNAR,
                ContactBirthdayCalendar.LUNAR to ContactBirthdayCalendar.SOLAR,
            )
    }
}
