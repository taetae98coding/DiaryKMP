package io.github.taetae98coding.diary.core.model.memo

public sealed interface DailyMemoNotificationContent {
    public data class Loaded(
        val memoList: List<DailyMemo>,
    ) : DailyMemoNotificationContent

    public data object Unavailable : DailyMemoNotificationContent
}
