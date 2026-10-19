package io.github.taetae98coding.diary.core.testing.checklist

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistLocalEntity
import io.github.taetae98coding.diary.core.database.api.checklistitem.entity.ChecklistItemLocalEntity
import io.github.taetae98coding.diary.core.model.checklist.Checklist
import io.github.taetae98coding.diary.core.model.checklist.ChecklistDetail
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItem
import io.github.taetae98coding.diary.core.network.api.checklist.entity.ChecklistRemoteEntity
import io.github.taetae98coding.diary.core.network.api.checklistitem.entity.ChecklistItemRemoteEntity
import kotlin.uuid.Uuid

public fun FixtureMonkey.checklistDetail(
    title: String = "title-" + giveMeOne<String>(),
    description: String = giveMeOne<String>(),
): ChecklistDetail =
    ChecklistDetail(
        title = title,
        description = description,
    )

public fun FixtureMonkey.checklist(
    isDeleted: Boolean,
    detail: ChecklistDetail = checklistDetail(),
): Checklist =
    giveMeKotlinBuilder<Checklist>()
        .setExp(Checklist::isDeleted, isDeleted)
        .sample()
        .copy(detail = detail)

public fun FixtureMonkey.checklistItem(
    isChecked: Boolean,
    isDeleted: Boolean,
    checklistId: Uuid = giveMeOne<Uuid>(),
    content: String = "content-" + giveMeOne<String>(),
): ChecklistItem =
    giveMeKotlinBuilder<ChecklistItem>()
        .setExp(ChecklistItem::isChecked, isChecked)
        .setExp(ChecklistItem::isDeleted, isDeleted)
        .sample()
        .copy(checklistId = checklistId, content = content)

public fun FixtureMonkey.localChecklist(isDeleted: Boolean): ChecklistLocalEntity =
    giveMeKotlinBuilder<ChecklistLocalEntity>()
        .setExp(ChecklistLocalEntity::isDeleted, isDeleted)
        .sample()

public fun FixtureMonkey.localChecklistItem(
    isChecked: Boolean,
    isDeleted: Boolean,
    checklistId: Uuid = giveMeOne<Uuid>(),
): ChecklistItemLocalEntity =
    giveMeKotlinBuilder<ChecklistItemLocalEntity>()
        .setExp(ChecklistItemLocalEntity::isChecked, isChecked)
        .setExp(ChecklistItemLocalEntity::isDeleted, isDeleted)
        .sample()
        .copy(checklistId = checklistId)

public fun FixtureMonkey.remoteChecklist(isDeleted: Boolean): ChecklistRemoteEntity =
    giveMeKotlinBuilder<ChecklistRemoteEntity>()
        .setExp(ChecklistRemoteEntity::isDeleted, isDeleted)
        .sample()

public fun FixtureMonkey.remoteChecklistItem(
    isChecked: Boolean,
    isDeleted: Boolean,
): ChecklistItemRemoteEntity =
    giveMeKotlinBuilder<ChecklistItemRemoteEntity>()
        .setExp(ChecklistItemRemoteEntity::isChecked, isChecked)
        .setExp(ChecklistItemRemoteEntity::isDeleted, isDeleted)
        .sample()
