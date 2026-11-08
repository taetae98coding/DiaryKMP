package io.github.taetae98coding.diary.work.appaction.qr

import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.work.appaction.core.filterBySearchQuery

public data class AppActionQr(
    val id: String,
    val title: String,
    val description: String,
    val value: String,
    val createdAt: String,
    val updatedAt: String,
)

internal fun Qr.toAppActionQr(): AppActionQr =
    AppActionQr(
        id = id.toString(),
        title = detail.title,
        description = detail.description,
        value = detail.value,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )

public fun List<AppActionQr>.filterBySearchQuery(query: String): List<AppActionQr> = filterBySearchQuery(query = query) { qr -> listOf(qr.title, qr.description) }
