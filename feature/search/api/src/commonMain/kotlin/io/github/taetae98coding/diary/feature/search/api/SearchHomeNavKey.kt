package io.github.taetae98coding.diary.feature.search.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
public data class SearchHomeNavKey(
    val initialType: SearchHomeType = SearchHomeType.MEMO,
) : NavKey
