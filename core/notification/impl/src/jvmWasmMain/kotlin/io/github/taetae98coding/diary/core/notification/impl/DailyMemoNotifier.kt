package io.github.taetae98coding.diary.core.notification.impl

internal fun interface DailyMemoNotifier {
    suspend fun notifyDailyMemo()
}
