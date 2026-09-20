package io.github.taetae98coding.diary.work.daily.memo

import io.github.taetae98coding.diary.core.model.memo.DailyMemo

internal sealed interface DailyMemoNotificationContent {
    data class Loaded(
        val memoList: List<DailyMemo>,
    ) : DailyMemoNotificationContent

    data object Unavailable : DailyMemoNotificationContent
}

internal fun dailyMemoNotificationBody(content: DailyMemoNotificationContent): String =
    when (content) {
        is DailyMemoNotificationContent.Loaded -> content.memoList.joinToString(separator = "\n") { memo -> "- ${memo.title}" }
        DailyMemoNotificationContent.Unavailable -> ""
    }
