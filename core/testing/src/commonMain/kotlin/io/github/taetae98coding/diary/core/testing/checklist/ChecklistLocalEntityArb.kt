package io.github.taetae98coding.diary.core.testing.checklist

import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.string

public fun Arb.Companion.checklistDetailLocalEntity(): Arb<ChecklistDetailLocalEntity> =
    arbitrary {
        ChecklistDetailLocalEntity(
            title = Arb.string().bind(),
            description = Arb.string().bind(),
        )
    }

public fun Arb.Companion.checklistLocalEntity(): Arb<ChecklistLocalEntity> =
    arbitrary {
        ChecklistLocalEntity(
            id = Arb.kotlinUuid().bind(),
            detail = Arb.checklistDetailLocalEntity().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }
