package io.github.taetae98coding.diary.core.database.api.tag.entity

public enum class TagScopeLocalEntity(
    public val queryValue: String,
) {
    SELF("self"),
    CHILD("child"),
    DESCENDANT("descendant"),
}
