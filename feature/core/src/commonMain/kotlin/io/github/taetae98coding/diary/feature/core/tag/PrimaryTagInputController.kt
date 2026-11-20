package io.github.taetae98coding.diary.feature.core.tag

import kotlin.uuid.Uuid

public interface PrimaryTagInputController {
    public fun updateQuery(query: String)

    public fun selectTag(tagId: Uuid)

    public fun unselectTag(tagId: Uuid)

    public fun selectPrimaryTag(tagId: Uuid)

    public fun unselectPrimaryTag()
}
