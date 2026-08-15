package io.github.taetae98coding.diary.feature.web.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
public data class WebAddNavKey(
    val initialTagId: Uuid? = null,
) : NavKey
