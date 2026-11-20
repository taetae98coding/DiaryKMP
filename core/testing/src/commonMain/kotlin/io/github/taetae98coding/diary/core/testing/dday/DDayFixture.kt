package io.github.taetae98coding.diary.core.testing.dday

import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.model.dday.DDayDetail
import io.github.taetae98coding.diary.core.model.dday.DDayOverflowPolicy
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate

public fun Arb.Companion.dday(
    isDeleted: Boolean,
    detail: DDayDetail? = null,
): Arb<DDay> =
    arbitrary {
        Arb.dday().bind().copy(
            detail = detail ?: Arb.ddayDetail().bind(),
            isDeleted = isDeleted,
        )
    }

// 날짜를 정하는 값만 고정하고 나머지는 임의로 채운다. 기준일을 1일로 세기를 넘기지 않으면 결과가 그 값과 무관한 경우로 보고 임의로 정한다.
public fun Arb.Companion.ddayDetail(
    baseDate: LocalDate,
    countsBaseDay: Boolean? = null,
    repeats: Set<DateTimeUnit.DateBased> = emptySet(),
    endDate: LocalDate? = null,
    overflowPolicy: DDayOverflowPolicy = DDayOverflowPolicy.CLAMP_TO_MONTH_END,
): Arb<DDayDetail> =
    arbitrary {
        Arb.ddayDetail().bind().copy(
            baseDate = baseDate,
            countsBaseDay = countsBaseDay ?: Arb.boolean().bind(),
            repeats = repeats,
            endDate = endDate,
            overflowPolicy = overflowPolicy,
        )
    }
