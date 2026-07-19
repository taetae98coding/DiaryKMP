package io.github.taetae98coding.diary.core.database.api.tag.entity

// 상수 이름을 바꿔도 SQL이 비교하는 문자열은 함께 바뀌지 않으므로 질의에 넣는 값을 이름과 끊어 둔다.
public enum class TagScopeLocalEntity(
    public val queryValue: String,
) {
    SELF("self"),
    CHILD("child"),
    DESCENDANT("descendant"),
}
