package io.github.taetae98coding.diary.core.file.impl.image

import io.github.taetae98coding.diary.core.model.image.ImageFormat

internal fun ImageFormat.extension(): String =
    when (this) {
        ImageFormat.JPEG -> "jpg"
    }
