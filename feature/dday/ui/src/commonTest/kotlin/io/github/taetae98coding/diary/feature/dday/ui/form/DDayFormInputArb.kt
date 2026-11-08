package io.github.taetae98coding.diary.feature.dday.ui.form

import io.github.taetae98coding.diary.core.model.dday.DDayOverflowPolicy
import io.github.taetae98coding.diary.domain.dday.DDayInput
import io.github.taetae98coding.diary.domain.dday.repeat.DDayRepeatInput
import io.github.taetae98coding.diary.domain.dday.repeat.DDayRepeatUnit
import io.github.taetae98coding.diary.library.fixture.MAX_COLLECTION_SIZE
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string

private const val MAX_REPEAT_NUMBER = 99_999

internal fun Arb.Companion.ddayRepeatInput(): Arb<DDayRepeatInput> =
    arbitrary {
        DDayRepeatInput(
            number = Arb.int(0..MAX_REPEAT_NUMBER).orNull().bind(),
            unit = Arb.enum<DDayRepeatUnit>().bind(),
        )
    }

internal fun Arb.Companion.ddayInput(): Arb<DDayInput> =
    arbitrary {
        DDayInput(
            title = Arb.string().bind(),
            description = Arb.string().bind(),
            color = Arb.long().bind(),
            baseDate = Arb.kotlinxLocalDate().bind(),
            countsBaseDay = Arb.boolean().bind(),
            repeatList = Arb.list(Arb.ddayRepeatInput(), 0..MAX_COLLECTION_SIZE).bind(),
            endDate = Arb.kotlinxLocalDate().orNull().bind(),
            overflowPolicy = Arb.enum<DDayOverflowPolicy>().bind(),
        )
    }
