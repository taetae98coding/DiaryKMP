package io.github.taetae98coding.diary.core.testing.checklist

import io.github.taetae98coding.diary.core.model.checklist.Checklist
import io.github.taetae98coding.diary.core.model.checklist.ChecklistDetail
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItem
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.string

public fun Arb.Companion.checklist(): Arb<Checklist> =
    arbitrary {
        Checklist(
            id = Arb.kotlinUuid().bind(),
            detail = Arb.checklistDetail().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }

public fun Arb.Companion.checklistItem(): Arb<ChecklistItem> =
    arbitrary {
        ChecklistItem(
            id = Arb.kotlinUuid().bind(),
            checklistId = Arb.kotlinUuid().bind(),
            content = Arb.string().bind(),
            isChecked = Arb.boolean().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }
