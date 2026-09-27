package io.github.taetae98coding.diary.feature.qr.ui

import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.core.model.qr.QrDetail
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal fun previewQr(
    title: String,
    value: String = "https://example.com",
): Qr =
    Qr(
        id = Uuid.random(),
        detail =
            QrDetail(
                title = title,
                description = "",
                value = value,
            ),
        isDeleted = false,
        updatedAt = Instant.DISTANT_PAST,
        createdAt = Instant.DISTANT_PAST,
    )
