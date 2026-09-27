package io.github.taetae98coding.diary.core.model.file

import io.github.taetae98coding.diary.library.kotlin.math.FULL_PERCENT

public fun uploadPercentOrNull(
    size: Long,
    sentBytes: Long,
): Int? =
    if (sentBytes <= 0 || size <= 0) {
        null
    } else {
        (sentBytes.coerceAtMost(size) * FULL_PERCENT / size).toInt()
    }
