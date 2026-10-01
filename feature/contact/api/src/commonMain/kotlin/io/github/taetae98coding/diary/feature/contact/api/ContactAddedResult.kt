package io.github.taetae98coding.diary.feature.contact.api

import kotlin.uuid.Uuid

public data class ContactAddedResult(
    val id: Uuid,
)

public fun contactAddedResultKey(requestKey: Uuid): String = "${ContactAddedResult::class}:$requestKey"
