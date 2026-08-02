package io.github.taetae98coding.diary.compose.tag

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal fun previewTag(
    emoji: String,
    title: String,
    color: Long,
): Tag =
    Tag(
        id = Uuid.random(),
        detail = TagDetail(emoji = emoji, title = title, description = "", color = color),
        isFinished = false,
        isDeleted = false,
        updatedAt = Instant.DISTANT_PAST,
        createdAt = Instant.DISTANT_PAST,
    )

internal class TagListPreviewParameter : PreviewParameterProvider<List<Tag>> {
    override val values: Sequence<List<Tag>> =
        sequenceOf(
            emptyList(),
            listOf(
                previewTag(emoji = "💼", title = "업무", color = 0xFF3A7BD5),
                previewTag(emoji = "🏃", title = "운동", color = 0xFFE57373),
                previewTag(emoji = "", title = "공부", color = 0xFF81C784),
            ),
        )
}
