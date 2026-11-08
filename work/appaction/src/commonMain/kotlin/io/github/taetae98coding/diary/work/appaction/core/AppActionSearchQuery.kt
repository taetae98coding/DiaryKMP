package io.github.taetae98coding.diary.work.appaction.core

// 검색 동작이 없는 종류의 항목 선택 목록을 검색어로 좁힌다. 저장소의 검색(SQLite LIKE)과 같이 ASCII 영문자만 대소문자를 맞춘다.
public fun <T> List<T>.filterBySearchQuery(
    query: String,
    values: (T) -> List<String>,
): List<T> {
    val keyword = query.trim().lowercaseAscii()
    if (keyword.isEmpty()) return emptyList()

    return filter { item -> values(item).any { value -> value.lowercaseAscii().contains(keyword) } }
}

private fun String.lowercaseAscii(): String = map { char -> if (char in 'A'..'Z') char.lowercaseChar() else char }.joinToString(separator = "")
