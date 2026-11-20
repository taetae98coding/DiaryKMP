package io.github.taetae98coding.diary.core.data.testing.checklist

import io.github.taetae98coding.diary.core.data.testing.checklistitem.checklistItemLocalEntity
import io.github.taetae98coding.diary.core.data.testing.checklistitem.checklistItemRemoteEntity
import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistLocalEntity
import io.github.taetae98coding.diary.core.database.api.checklistitem.entity.ChecklistItemLocalEntity
import io.github.taetae98coding.diary.core.network.api.checklist.entity.ChecklistRemoteEntity
import io.github.taetae98coding.diary.core.network.api.checklistitem.entity.ChecklistItemRemoteEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.map
import kotlin.uuid.Uuid

public fun Arb.Companion.localChecklist(isDeleted: Boolean): Arb<ChecklistLocalEntity> = Arb.checklistLocalEntity().map { checklist -> checklist.copy(isDeleted = isDeleted) }

public fun Arb.Companion.localChecklistItem(
    isChecked: Boolean,
    isDeleted: Boolean,
    checklistId: Uuid? = null,
): Arb<ChecklistItemLocalEntity> =
    arbitrary {
        Arb.checklistItemLocalEntity().bind().copy(
            checklistId = checklistId ?: Arb.kotlinUuid().bind(),
            isChecked = isChecked,
            isDeleted = isDeleted,
        )
    }

public fun Arb.Companion.remoteChecklist(isDeleted: Boolean): Arb<ChecklistRemoteEntity> = Arb.checklistRemoteEntity().map { checklist -> checklist.copy(isDeleted = isDeleted) }

public fun Arb.Companion.remoteChecklistItem(
    isChecked: Boolean,
    isDeleted: Boolean,
): Arb<ChecklistItemRemoteEntity> = Arb.checklistItemRemoteEntity().map { item -> item.copy(isChecked = isChecked, isDeleted = isDeleted) }
