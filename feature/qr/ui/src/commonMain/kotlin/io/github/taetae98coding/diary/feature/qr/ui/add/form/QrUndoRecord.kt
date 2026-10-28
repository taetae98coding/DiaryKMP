package io.github.taetae98coding.diary.feature.qr.ui.add.form

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import io.github.taetae98coding.diary.domain.qr.content.QrFormat

internal data class QrUndoRecord(
    val format: QrFormat,
    val raw: String,
)

internal val QrUndoRecordSaver: Saver<QrUndoRecord?, Any> =
    listSaver(
        save = { record -> listOfNotNull(record?.format?.name, record?.raw) },
        restore = { saved -> saved.takeIf { list -> list.isNotEmpty() }?.let { (format, raw) -> QrUndoRecord(format = QrFormat.valueOf(format), raw = raw) } },
    )
