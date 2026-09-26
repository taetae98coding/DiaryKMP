package io.github.taetae98coding.diary.data.memo.mapper

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
        test("종일 여부에 따라 종일 기간 또는 시각이 있는 기간으로 변환한다") {
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
    })

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()
