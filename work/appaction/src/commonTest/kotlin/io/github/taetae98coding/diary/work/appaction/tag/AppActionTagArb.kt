package io.github.taetae98coding.diary.work.appaction.tag

import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.work.appaction.core.appActionColor
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.string

internal fun Arb.Companion.appActionTag(): Arb<AppActionTag> =
    arbitrary {
        AppActionTag(
            id = Arb.kotlinUuid().bind().toString(),
            emoji = Arb.string().bind(),
            title = Arb.string().bind(),
            description = Arb.string().bind(),
            color = Arb.appActionColor().bind(),
            isFinished = Arb.boolean().bind(),
            createdAt = Arb.string().bind(),
            updatedAt = Arb.string().bind(),
        )
    }
