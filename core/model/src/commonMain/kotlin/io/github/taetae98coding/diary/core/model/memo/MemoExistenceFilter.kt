package io.github.taetae98coding.diary.core.model.memo

public data class MemoExistenceFilter(
    val date: MemoFilterExistence = MemoFilterExistence.ALL,
    val tag: MemoFilterExistence = MemoFilterExistence.ALL,
    val place: MemoFilterExistence = MemoFilterExistence.ALL,
)
