package io.github.taetae98coding.diary.feature.timetable.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
public data class CalendarTimetableNavKey(
    val type: Type,
    val date: LocalDate,
    // 백스택의 키는 서로 달라야 하는데, 알림은 같은 형식과 날짜의 시간표가 아래에 있어도 시간표를 다시 열 수 있으므로 그때마다 새 값을 준다.
    val id: Uuid = Uuid.NIL,
) : ScreenNavKey {
    override val screenName: String get() = "CalendarTimetable"

    @Serializable
    public enum class Type {
        @SerialName("day")
        DAY,

        @SerialName("week")
        WEEK,
    }
}
