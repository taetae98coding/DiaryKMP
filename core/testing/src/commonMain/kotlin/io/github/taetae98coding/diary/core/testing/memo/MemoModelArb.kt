package io.github.taetae98coding.diary.core.testing.memo

import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.core.model.memo.MemoDraftProgress
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDateTime
import io.github.taetae98coding.diary.library.fixture.localDateRange
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string

public fun Arb.Companion.memo(): Arb<Memo> =
    arbitrary {
        Memo(
            id = Arb.kotlinUuid().bind(),
            detail = Arb.memoDetail().bind(),
            primaryTagId = Arb.kotlinUuid().orNull().bind(),
            isFinished = Arb.boolean().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }

public fun Arb.Companion.memoDateTime(): Arb<MemoDateTime> =
    Arb.choice(
        Arb.memoDateTimeAllDay(),
        Arb.memoDateTimeDateTime(),
    )

public fun Arb.Companion.memoDateTimeAllDay(): Arb<MemoDateTime.AllDay> =
    arbitrary {
        MemoDateTime.AllDay(
            dateRange = Arb.localDateRange().bind(),
        )
    }

public fun Arb.Companion.memoDateTimeDateTime(): Arb<MemoDateTime.DateTime> =
    arbitrary {
        MemoDateTime.DateTime(
            start = Arb.kotlinxLocalDateTime().bind(),
            endInclusive = Arb.kotlinxLocalDateTime().bind(),
        )
    }

public fun Arb.Companion.memoDetail(): Arb<MemoDetail> =
    arbitrary {
        MemoDetail(
            title = Arb.string().bind(),
            description = Arb.string().bind(),
            color = Arb.long().bind(),
            dateTime = Arb.memoDateTime().orNull().bind(),
        )
    }

public fun Arb.Companion.memoDraftProgress(): Arb<MemoDraftProgress> =
    arbitrary {
        MemoDraftProgress(
            title = Arb.string().bind(),
            description = Arb.string().bind(),
        )
    }
