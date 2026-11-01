package io.github.taetae98coding.diary.core.model.memo

public sealed interface MemoDraftGeneration {
    public data class InProgress(
        val progress: MemoDraftProgress,
    ) : MemoDraftGeneration

    public data class Completed(
        val draft: MemoDraft,
    ) : MemoDraftGeneration
}
