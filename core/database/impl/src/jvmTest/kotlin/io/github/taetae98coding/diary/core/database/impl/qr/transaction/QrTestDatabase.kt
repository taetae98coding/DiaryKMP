package io.github.taetae98coding.diary.core.database.impl.qr.transaction

import androidx.room3.useReaderConnection
import androidx.room3.useWriterConnection
import androidx.sqlite.SQLiteStatement
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.qr.entity.AccountQrLocalEntity
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal suspend fun DiaryDatabase.findQrList(): List<QrLocalEntity> =
    useReaderConnection { transactor ->
        transactor.usePrepared(
            """
            SELECT id, title, description, value, is_deleted, updated_at, created_at
            FROM qr
            ORDER BY id ASC
            """,
        ) { statement -> statement.readAll { it.toQr() } }
    }

internal suspend fun DiaryDatabase.findAccountQrList(): List<AccountQrLocalEntity> =
    useReaderConnection { transactor ->
        transactor.usePrepared(
            """
            SELECT account_id, qr_id, is_dirty
            FROM account_qr
            ORDER BY account_id ASC, qr_id ASC
            """,
        ) { statement -> statement.readAll { it.toAccountQr() } }
    }

// 수정과 삭제가 업로드 대기를 다시 세우는지 보려면 저장 직후의 대기 상태를 먼저 지워야 한다.
internal suspend fun DiaryDatabase.markQrUploaded(
    accountId: Uuid,
    qrId: Uuid,
) {
    useWriterConnection { transactor ->
        transactor.usePrepared("UPDATE account_qr SET is_dirty = 0 WHERE account_id = ? AND qr_id = ?") { statement ->
            statement.bindText(1, accountId.toString())
            statement.bindText(2, qrId.toString())
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

private fun SQLiteStatement.toQr(): QrLocalEntity =
    QrLocalEntity(
        id = Uuid.parse(getText(0)),
        detail =
            QrDetailLocalEntity(
                title = getText(1),
                description = getText(2),
                value = getText(3),
            ),
        isDeleted = getBoolean(4),
        updatedAt = Instant.fromEpochMilliseconds(getLong(5)),
        createdAt = Instant.fromEpochMilliseconds(getLong(6)),
    )

private fun SQLiteStatement.toAccountQr(): AccountQrLocalEntity =
    AccountQrLocalEntity(
        accountId = Uuid.parse(getText(0)),
        qrId = Uuid.parse(getText(1)),
        isDirty = getBoolean(2),
    )
