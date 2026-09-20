package io.github.taetae98coding.diary.core.model.memo

import kotlin.uuid.Uuid

public data class DailyMemo(
    val id: Uuid,
    val title: String,
)
