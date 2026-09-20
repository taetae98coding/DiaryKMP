package io.github.taetae98coding.diary.core.model.memo

import kotlinx.datetime.LocalDate

public data class UpcomingDailyMemoNotification(
    val date: LocalDate,
    val content: DailyMemoNotificationContent,
)
