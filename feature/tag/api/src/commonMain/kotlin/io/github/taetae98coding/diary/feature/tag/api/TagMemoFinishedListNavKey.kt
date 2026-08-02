package io.github.taetae98coding.diary.feature.tag.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
public data class TagMemoFinishedListNavKey(
    val tagId: Uuid,
) : NavKey
