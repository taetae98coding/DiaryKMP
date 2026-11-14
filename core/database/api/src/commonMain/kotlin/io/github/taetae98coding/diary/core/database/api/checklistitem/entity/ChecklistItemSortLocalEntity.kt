package io.github.taetae98coding.diary.core.database.api.checklistitem.entity

public enum class ChecklistItemSortLocalEntity(
    public val queryValue: String,
) {
    ADDED("added"),
    UNCHECKED_FIRST("unchecked_first"),
}
