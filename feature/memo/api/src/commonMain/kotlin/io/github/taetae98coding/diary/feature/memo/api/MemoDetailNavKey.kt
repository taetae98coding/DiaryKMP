package io.github.taetae98coding.diary.feature.memo.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
public data class MemoDetailNavKey(
    val id: Uuid,
) : NavKey
