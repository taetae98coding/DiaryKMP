package io.github.taetae98coding.diary.core.data.testing.dday

import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayOverflowPolicyLocalEntity
import io.github.taetae98coding.diary.core.network.api.dday.entity.DDayRemoteEntity
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate

public fun Arb.Companion.localDDay(isDeleted: Boolean): Arb<DDayLocalEntity> = Arb.ddayLocalEntity().map { dday -> dday.copy(isDeleted = isDeleted) }

public fun Arb.Companion.remoteDDay(isDeleted: Boolean): Arb<DDayRemoteEntity> = Arb.ddayRemoteEntity().map { dday -> dday.copy(isDeleted = isDeleted) }

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
