package io.github.taetae98coding.diary.feature.qr.ui.add.form

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import io.github.taetae98coding.diary.domain.qr.content.QrFormat

internal data class QrUndoRecord(
    val format: QrFormat,
    val raw: String,
)

internal val QrUndoRecordListSaver: Saver<SnapshotStateList<QrUndoRecord>, Any> =
    listSaver(
        save = { recordList -> recordList.flatMap { record -> listOf(record.format.name, record.raw) } },
        restore = { saved ->
            saved
                .chunked(2)
                .map { (format, raw) -> QrUndoRecord(format = QrFormat.valueOf(format), raw = raw) }
                .toMutableStateList()
        },
    )
