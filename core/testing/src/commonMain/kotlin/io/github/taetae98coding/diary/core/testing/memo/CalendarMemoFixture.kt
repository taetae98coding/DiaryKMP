package io.github.taetae98coding.diary.core.testing.memo

import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string

public fun Arb.Companion.calendarMemo(
    dateTime: MemoDateTime,
    title: String? = null,
): Arb<CalendarMemo> =
    arbitrary {
        CalendarMemo(
            id = Arb.kotlinUuid().bind(),
            title = title ?: Arb.string().bind(),
            color = Arb.long().bind(),
            dateTime = dateTime,
        )
    }
