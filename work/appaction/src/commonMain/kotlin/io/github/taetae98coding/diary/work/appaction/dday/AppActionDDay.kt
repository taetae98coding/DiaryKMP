package io.github.taetae98coding.diary.work.appaction.dday

import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.model.dday.DDayCount
import io.github.taetae98coding.diary.domain.dday.count.count
import io.github.taetae98coding.diary.work.appaction.core.toColorHex
import io.github.taetae98coding.diary.work.appaction.tag.AppActionTag
import kotlinx.datetime.LocalDate

public object AppActionDDayStatusValue {
    public const val UPCOMING: String = "upcoming"
    public const val TODAY: String = "today"
    public const val PASSED: String = "passed"
}

public data class AppActionDDayCount(
    val status: String,
    val remainingDays: Int?,
    val passedDays: Int?,
    val years: Int?,
    val months: Int?,
)

public data class AppActionDDay(
    val id: String,
    val title: String,
    val description: String,
    val color: String,
    val baseDate: String,
    val countsBaseDay: Boolean,
    val repeats: List<String>,
    val endDate: String?,
    val overflowPolicy: String,
    val createdAt: String,
    val updatedAt: String,
    val count: AppActionDDayCount,
)

public data class AppActionDDayDetail(
    val dday: AppActionDDay,
    val tags: List<AppActionTag>,
)

// 데스크톱 MCP 연결도 이 값으로 결과를 만들어 두 경로의 디데이 결과가 같게 한다.
public fun DDay.toAppActionDDay(today: LocalDate): AppActionDDay =
    AppActionDDay(
        id = id.toString(),
        title = detail.title,
        description = detail.description,
        color = detail.color.toColorHex(),
        baseDate = detail.baseDate.toString(),
        countsBaseDay = detail.countsBaseDay,
        repeats = detail.repeats.toDDayRepeatValueList(),
        endDate = detail.endDate?.toString(),
        overflowPolicy = detail.overflowPolicy.toDDayOverflowPolicyValue(),
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
        count = detail.count(today = today).toAppActionDDayCount(),
    )

private fun DDayCount.toAppActionDDayCount(): AppActionDDayCount =
    when (this) {
        is DDayCount.Upcoming ->
            AppActionDDayCount(
                status = AppActionDDayStatusValue.UPCOMING,
                remainingDays = remainingDays,
                passedDays = null,
                years = period.years,
                months = period.months,
            )

        DDayCount.Today ->
            AppActionDDayCount(
                status = AppActionDDayStatusValue.TODAY,
                remainingDays = null,
                passedDays = null,
                years = null,
                months = null,
            )

        is DDayCount.Passed ->
            AppActionDDayCount(
                status = AppActionDDayStatusValue.PASSED,
                remainingDays = null,
                passedDays = passedDays,
                years = period.years,
                months = period.months,
            )
    }
