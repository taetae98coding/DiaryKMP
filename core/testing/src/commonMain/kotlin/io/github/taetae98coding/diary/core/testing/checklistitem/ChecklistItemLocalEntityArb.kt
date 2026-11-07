package io.github.taetae98coding.diary.core.testing.checklistitem

import io.github.taetae98coding.diary.core.database.api.checklistitem.entity.ChecklistItemLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.string

public fun Arb.Companion.checklistItemLocalEntity(): Arb<ChecklistItemLocalEntity> =
    arbitrary {
        ChecklistItemLocalEntity(
            id = Arb.kotlinUuid().bind(),
            checklistId = Arb.kotlinUuid().bind(),
            content = Arb.string().bind(),
            isChecked = Arb.boolean().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }
