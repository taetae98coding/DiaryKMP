package io.github.taetae98coding.diary.compose.tag.list

import kotlin.uuid.Uuid

public sealed interface TagListEffect {
    public data class Finished(
        val id: Uuid,
    ) : TagListEffect

    public data class Restarted(
        val id: Uuid,
    ) : TagListEffect

    public data class Deleted(
        val id: Uuid,
    ) : TagListEffect
}
