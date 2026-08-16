package io.github.taetae98coding.diary.feature.search.api

import kotlinx.serialization.Serializable

@Serializable
public enum class SearchHomeType {
    MEMO,
    TAG,
    PLACE,
    WEB,
}
