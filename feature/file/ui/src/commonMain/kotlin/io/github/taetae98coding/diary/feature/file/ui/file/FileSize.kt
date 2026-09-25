package io.github.taetae98coding.diary.feature.file.ui.file

import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.feature.file.ui.Res
import io.github.taetae98coding.diary.feature.file.ui.file_size_byte
import io.github.taetae98coding.diary.feature.file.ui.file_size_kilobyte
import io.github.taetae98coding.diary.feature.file.ui.file_size_megabyte
import org.jetbrains.compose.resources.stringResource

private const val UNIT_BYTES = 1024L
private const val DECIMAL_BASE = 10

internal enum class FileSizeUnit {
    BYTE,
    KILOBYTE,
    MEGABYTE,
}

internal data class FileSize(
    val value: String,
    val unit: FileSizeUnit,
)

internal fun Long.toFileSize(): FileSize =
    when {
        this < UNIT_BYTES -> FileSize(value = toString(), unit = FileSizeUnit.BYTE)
        this < UNIT_BYTES * UNIT_BYTES -> FileSize(value = truncatedTenths(unitBytes = UNIT_BYTES), unit = FileSizeUnit.KILOBYTE)
        else -> FileSize(value = truncatedTenths(unitBytes = UNIT_BYTES * UNIT_BYTES), unit = FileSizeUnit.MEGABYTE)
    }

private fun Long.truncatedTenths(unitBytes: Long): String {
    val tenths = this * DECIMAL_BASE / unitBytes

    return "${tenths / DECIMAL_BASE}.${tenths % DECIMAL_BASE}"
}

@Composable
internal fun FileSize.toDisplayText(): String =
    when (unit) {
        FileSizeUnit.BYTE -> stringResource(Res.string.file_size_byte, value)
        FileSizeUnit.KILOBYTE -> stringResource(Res.string.file_size_kilobyte, value)
        FileSizeUnit.MEGABYTE -> stringResource(Res.string.file_size_megabyte, value)
    }
