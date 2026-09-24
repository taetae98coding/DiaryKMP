package io.github.taetae98coding.diary.core.model.playlist

import kotlin.uuid.Uuid

public data class MusicDownloadTarget(
    val id: Uuid,
    val link: String,
)
