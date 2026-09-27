package io.github.taetae98coding.diary.core.testing.memo

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime

public fun FixtureMonkey.calendarMemo(
    dateTime: MemoDateTime,
    title: String = giveMeOne(),
): CalendarMemo =
    CalendarMemo(
        id = giveMeOne(),
        title = title,
        color = giveMeOne(),
        dateTime = dateTime,
    )
