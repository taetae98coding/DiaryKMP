package io.github.taetae98coding.diary.compose.calendar

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary

internal fun Arb.Companion.calendarColor(): Arb<CalendarColor> =
    arbitrary {
        CalendarColor(
            sundayAndHolidayColor = Arb.color().bind(),
            saturdayColor = Arb.color().bind(),
        )
    }
