package io.github.taetae98coding.diary.feature.contact.ui.form

import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder

class ContactBirthdayCalendarListTest :
    FunSpec({
        test("기본값인 양력을 앞에 두고 음력을 뒤에 둔다") {
            contactBirthdayCalendarList shouldContainExactly
                listOf(
                    ContactBirthdayCalendar.SOLAR,
                    ContactBirthdayCalendar.LUNAR,
                )
        }

        test("모든 달력 구분을 한 번씩만 담는다") {
            contactBirthdayCalendarList shouldContainExactlyInAnyOrder ContactBirthdayCalendar.entries
        }
    })
