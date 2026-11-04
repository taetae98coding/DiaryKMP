package io.github.taetae98coding.diary.core.testing.checklist

import io.github.taetae98coding.diary.core.network.api.checklist.entity.ChecklistDetailRemoteEntity
import io.github.taetae98coding.diary.core.network.api.checklist.entity.ChecklistRemoteEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.string

public fun Arb.Companion.checklistDetailRemoteEntity(): Arb<ChecklistDetailRemoteEntity> =
    arbitrary {
        ChecklistDetailRemoteEntity(
            title = Arb.string().bind(),
            description = Arb.string().bind(),
        )
    }

public fun Arb.Companion.checklistRemoteEntity(): Arb<ChecklistRemoteEntity> =
    arbitrary {
        ChecklistRemoteEntity(
            id = Arb.kotlinUuid().bind(),
            detail = Arb.checklistDetailRemoteEntity().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }
