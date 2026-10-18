package io.github.taetae98coding.diary.data.account.mapper

import io.github.taetae98coding.diary.core.model.image.ImageFormat

internal fun ImageFormat.toMimeType(): String =
    when (this) {
        ImageFormat.JPEG -> "image/jpeg"
    }
