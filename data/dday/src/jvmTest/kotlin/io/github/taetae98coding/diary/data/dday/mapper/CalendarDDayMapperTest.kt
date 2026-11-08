package io.github.taetae98coding.diary.data.dday.mapper

import io.github.taetae98coding.diary.core.database.api.dday.entity.CalendarDDayKindLocalEntity
import io.github.taetae98coding.diary.core.model.dday.CalendarDDay
import io.github.taetae98coding.diary.core.model.dday.CalendarDDayLabel
import io.github.taetae98coding.diary.core.testing.dday.calendarDDayLocalEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class CalendarDDayMapperTest :
    FunSpec({
        test("캘린더 디데이는 식별값, 제목, 컬러, 날짜를 옮기고 종류와 수로 문구를 정한다") {
            CalendarDDayKindLocalEntity.entries.forEach { kind ->
                val local = Arb.calendarDDayLocalEntity().next().copy(kind = kind)
                val label =
                    when (kind) {
                        CalendarDDayKindLocalEntity.BASE_DAY -> CalendarDDayLabel.BaseDay
                        CalendarDDayKindLocalEntity.YEARS -> CalendarDDayLabel.Years(years = local.amount)
                        CalendarDDayKindLocalEntity.MONTHS -> CalendarDDayLabel.Months(months = local.amount)
                        CalendarDDayKindLocalEntity.DAYS -> CalendarDDayLabel.Days(days = local.amount)
                    }

                local.toDomain() shouldBe
                    CalendarDDay(
                        ddayId = local.ddayId,
                        title = local.title,
                        color = local.color,
                        date = local.date,
                        label = label,
                    )
            }
        }
    })
