package io.github.taetae98coding.diary.core.mapper.memo

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.memo.entity.CalendarMemoLocalEntity
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class CalendarMemoMapperTest :
    FunSpec({
        // TC-CALENDAR-MEMO-DOMAIN-007
        test("local to domain") {
            listOf(true, false).forEach { isAllDay ->
                val local = fixtureMonkey.giveMeOne<CalendarMemoLocalEntity>().copy(isAllDay = isAllDay)

                local.toDomain() shouldBe
                    CalendarMemo(
                        id = local.id,
                        title = local.title,
                        color = local.color,
                        dateTime =
                            if (isAllDay) {
                                MemoDateTime.AllDay(dateRange = local.start.date..local.endInclusive.date)
                            } else {
                                MemoDateTime.DateTime(
                                    start = local.start,
                                    endInclusive = local.endInclusive,
                                )
                            },
                    )
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
