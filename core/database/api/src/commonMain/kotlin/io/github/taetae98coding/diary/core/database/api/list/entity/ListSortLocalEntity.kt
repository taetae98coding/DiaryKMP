package io.github.taetae98coding.diary.core.database.api.list.entity

public enum class ListSortLocalEntity(
    public val queryValue: String,
) {
    DEFAULT("default"),
    TITLE("title"),
    NAME("name"),
    RECENTLY_UPDATED("recently_updated"),
}
