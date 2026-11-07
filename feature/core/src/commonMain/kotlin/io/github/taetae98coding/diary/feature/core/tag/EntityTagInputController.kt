package io.github.taetae98coding.diary.feature.core.tag

import kotlin.uuid.Uuid

public interface EntityTagInputController {
    public fun updateQuery(query: String)

    public fun add(tagId: Uuid)

    public fun remove(tagId: Uuid)
}
