package io.github.taetae98coding.diary.core.testing.dday

import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayOverflowPolicyLocalEntity
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.model.dday.DDayDetail
import io.github.taetae98coding.diary.core.model.dday.DDayOverflowPolicy
import io.github.taetae98coding.diary.core.network.api.dday.entity.DDayRemoteEntity
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.map
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

public fun Arb.Companion.localDDay(isDeleted: Boolean): Arb<DDayLocalEntity> = Arb.ddayLocalEntity().map { dday -> dday.copy(isDeleted = isDeleted) }

public fun Arb.Companion.remoteDDay(isDeleted: Boolean): Arb<DDayRemoteEntity> = Arb.ddayRemoteEntity().map { dday -> dday.copy(isDeleted = isDeleted) }

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

public fun Arb.Companion.localDDay(
    baseDate: LocalDate,
    countsBaseDay: Boolean? = null,
    repeats: Set<DateTimeUnit.DateBased> = emptySet(),
    endDate: LocalDate? = null,
    overflowPolicy: DDayOverflowPolicyLocalEntity = DDayOverflowPolicyLocalEntity.CLAMP_TO_MONTH_END,
    title: String? = null,
    isDeleted: Boolean = false,
): Arb<DDayLocalEntity> =
    arbitrary {
        val dday = Arb.ddayLocalEntity().bind()

        dday.copy(
            detail =
                dday.detail.copy(
                    title = title ?: dday.detail.title,
                    baseDate = baseDate,
                    countsBaseDay = countsBaseDay ?: Arb.boolean().bind(),
                    repeats = repeats,
                    endDate = endDate,
                    overflowPolicy = overflowPolicy,
                ),
            isDeleted = isDeleted,
        )
    }
