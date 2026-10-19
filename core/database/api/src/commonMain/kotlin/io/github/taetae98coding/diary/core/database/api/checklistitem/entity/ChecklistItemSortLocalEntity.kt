package io.github.taetae98coding.diary.core.database.api.checklistitem.entity

// 상수 이름을 바꿔도 SQL이 비교하는 문자열은 함께 바뀌지 않으므로 질의에 넣는 값을 이름과 끊어 둔다.
public enum class ChecklistItemSortLocalEntity(
    public val queryValue: String,
) {
    ADDED("added"),
    UNCHECKED_FIRST("unchecked_first"),
}
