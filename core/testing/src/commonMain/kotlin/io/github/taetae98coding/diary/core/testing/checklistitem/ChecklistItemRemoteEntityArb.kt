package io.github.taetae98coding.diary.core.testing.checklistitem

import io.github.taetae98coding.diary.core.network.api.checklistitem.entity.ChecklistItemRemoteEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.string

public fun Arb.Companion.checklistItemRemoteEntity(): Arb<ChecklistItemRemoteEntity> =
    arbitrary {
        ChecklistItemRemoteEntity(
            id = Arb.kotlinUuid().bind(),
            checklistId = Arb.kotlinUuid().bind(),
            content = Arb.string().bind(),
            isChecked = Arb.boolean().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }
