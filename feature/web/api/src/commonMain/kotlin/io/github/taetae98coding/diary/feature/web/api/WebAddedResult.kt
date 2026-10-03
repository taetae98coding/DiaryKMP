package io.github.taetae98coding.diary.feature.web.api

import kotlin.uuid.Uuid

public data class WebAddedResult(
    val id: Uuid,
)

public fun webAddedResultKey(requestKey: Uuid): String = "${WebAddedResult::class}:$requestKey"
