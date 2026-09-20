package io.github.taetae98coding.diary.core.datastore.impl

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
internal data class SyncTimeData(
    val syncedAtMap: Map<Uuid, Instant> = emptyMap(),
)
