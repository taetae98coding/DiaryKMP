package io.github.taetae98coding.diary.notification

import kotlinx.datetime.LocalTime

// iOS는 정해진 시각에 앱 코드를 깨우는 대신 알림 자체를 미리 등록하므로, 전달 시각 예약도 알림 수단이 소유한다.
public interface LocalNotificationScheduler {
    public fun scheduleDaily(
        identifier: String,
        titleLocalizationKey: String,
        time: LocalTime,
    )
}
