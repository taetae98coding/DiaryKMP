package io.github.taetae98coding.diary.feature.tag.api

import kotlin.uuid.Uuid

public data class TagAddedResult(
    val id: Uuid,
)

public fun tagAddedResultKey(requestKey: Uuid): String = "${TagAddedResult::class}:$requestKey"
