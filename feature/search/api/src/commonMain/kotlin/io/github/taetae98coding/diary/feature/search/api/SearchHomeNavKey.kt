package io.github.taetae98coding.diary.feature.search.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.Serializable

@Serializable
public data class SearchHomeNavKey(
    val initialType: SearchHomeType = SearchHomeType.MEMO,
) : ScreenNavKey {
    override val screenName: String get() = "SearchHome"
}
