package io.github.taetae98coding.diary.core.database.impl.checklist.transaction

import androidx.room3.useReaderConnection
import androidx.room3.useWriterConnection
import androidx.sqlite.SQLiteStatement
import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistLocalEntity
import io.github.taetae98coding.diary.core.database.api.checklistitem.entity.ChecklistItemLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.checklist.entity.AccountChecklistLocalEntity
import io.github.taetae98coding.diary.core.database.impl.checklistitem.entity.AccountChecklistItemLocalEntity
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal suspend fun DiaryDatabase.findChecklistList(): List<ChecklistLocalEntity> =
    useReaderConnection { transactor ->
        transactor.usePrepared(
            """
            SELECT id, title, description, is_deleted, updated_at, created_at
            FROM checklist
            ORDER BY id ASC
            """,
        ) { statement -> statement.readAll { it.toChecklist() } }
    }

internal suspend fun DiaryDatabase.findAccountChecklistList(): List<AccountChecklistLocalEntity> =
    useReaderConnection { transactor ->
        transactor.usePrepared(
            """
            SELECT account_id, checklist_id, is_dirty
            FROM account_checklist
            ORDER BY account_id ASC, checklist_id ASC
            """,
        ) { statement ->
            statement.readAll {
                AccountChecklistLocalEntity(
                    accountId = Uuid.parse(it.getText(0)),
                    checklistId = Uuid.parse(it.getText(1)),
                    isDirty = it.getBoolean(2),
                )
            }
        }
    }

internal suspend fun DiaryDatabase.findChecklistItemList(): List<ChecklistItemLocalEntity> =
    useReaderConnection { transactor ->
        transactor.usePrepared(
            """
            SELECT id, checklist_id, content, is_checked, is_deleted, updated_at, created_at
            FROM checklist_item
            ORDER BY id ASC
            """,
        ) { statement -> statement.readAll { it.toChecklistItem() } }
    }

internal suspend fun DiaryDatabase.findAccountChecklistItemList(): List<AccountChecklistItemLocalEntity> =
    useReaderConnection { transactor ->
        transactor.usePrepared(
            """
            SELECT account_id, checklist_item_id, is_dirty
            FROM account_checklist_item
            ORDER BY account_id ASC, checklist_item_id ASC
            """,
        ) { statement ->
            statement.readAll {
                AccountChecklistItemLocalEntity(
                    accountId = Uuid.parse(it.getText(0)),
                    checklistItemId = Uuid.parse(it.getText(1)),
                    isDirty = it.getBoolean(2),
                )
            }
        }
    }

// 변경이 업로드 대기를 다시 세우는지 보려면 저장 직후의 대기 상태를 먼저 지워야 한다.
internal suspend fun DiaryDatabase.markChecklistUploaded(accountId: Uuid) {
    useWriterConnection { transactor ->
        transactor.usePrepared("UPDATE account_checklist SET is_dirty = 0 WHERE account_id = ?") { statement ->
            statement.bindText(1, accountId.toString())
            statement.step()
        }
        transactor.usePrepared("UPDATE account_checklist_item SET is_dirty = 0 WHERE account_id = ?") { statement ->
            statement.bindText(1, accountId.toString())
            statement.step()
        }
    }
}

private fun <T> SQLiteStatement.readAll(read: (SQLiteStatement) -> T): List<T> =
    buildList {
        while (step()) {
            add(read(this@readAll))
        }
    }

private fun SQLiteStatement.toChecklist(): ChecklistLocalEntity =
    ChecklistLocalEntity(
        id = Uuid.parse(getText(0)),
        detail =
            ChecklistDetailLocalEntity(
                title = getText(1),
                description = getText(2),
            ),
        isDeleted = getBoolean(3),
        updatedAt = Instant.fromEpochMilliseconds(getLong(4)),
        createdAt = Instant.fromEpochMilliseconds(getLong(5)),
    )

private fun SQLiteStatement.toChecklistItem(): ChecklistItemLocalEntity =
    ChecklistItemLocalEntity(
        id = Uuid.parse(getText(0)),
        checklistId = Uuid.parse(getText(1)),
        content = getText(2),
        isChecked = getBoolean(3),
        isDeleted = getBoolean(4),
        updatedAt = Instant.fromEpochMilliseconds(getLong(5)),
        createdAt = Instant.fromEpochMilliseconds(getLong(6)),
    )
