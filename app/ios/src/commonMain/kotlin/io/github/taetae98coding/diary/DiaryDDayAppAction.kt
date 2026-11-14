package io.github.taetae98coding.diary

import io.github.taetae98coding.diary.work.appaction.core.appActionDate
import io.github.taetae98coding.diary.work.appaction.core.appActionDateEpochMillis
import io.github.taetae98coding.diary.work.appaction.core.appActionEpochMillis
import io.github.taetae98coding.diary.work.appaction.core.readNumberedPage
import io.github.taetae98coding.diary.work.appaction.dday.AppActionDDay
import io.github.taetae98coding.diary.work.appaction.dday.AppActionDDayDetail
import io.github.taetae98coding.diary.work.appaction.dday.AppActionDDayOverflowPolicyValue
import io.github.taetae98coding.diary.work.appaction.dday.AppActionDDayStatusValue
import io.github.taetae98coding.diary.work.appaction.dday.DDayAppAction

public enum class DiaryAppActionDDayStatus {
    UPCOMING,
    TODAY,
    PASSED,
}

public enum class DiaryAppActionDDayOverflowPolicy {
    CLAMP_TO_MONTH_END,
    SKIP,
    NEXT_MONTH_FIRST_DAY,
}

// 반복 간격은 단축어가 데스크톱 연결과 같은 ISO 8601 기간 글로 넣고 꺼내도록 그대로 넘긴다.
public data class DiaryAppActionDDay(
    val id: String,
    val title: String,
    val ddayDescription: String,
    val color: String,
    val baseDateEpochMillis: Long,
    val countsBaseDay: Boolean,
    val repeats: List<String>,
    val endDateEpochMillis: Long?,
    val overflowPolicy: DiaryAppActionDDayOverflowPolicy,
    val status: DiaryAppActionDDayStatus,
    val remainingDays: Int?,
    val passedDays: Int?,
    val years: Int?,
    val months: Int?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

public data class DiaryAppActionDDayPage(
    val items: List<DiaryAppActionDDay>,
    val nextPageNumber: Int?,
)

public data class DiaryAppActionDDayDetail(
    val dday: DiaryAppActionDDay,
    val tags: List<DiaryAppActionTag>,
)

public object DiaryDDayAppAction {
    public suspend fun list(pageNumber: Int): DiaryAppActionDDayPage {
        val page = readNumberedPage(pageNumber = pageNumber) { cursor -> ddayAppAction().list(cursor = cursor) }

        return DiaryAppActionDDayPage(items = page.items.map { dday -> dday.toDiary() }, nextPageNumber = page.nextPageNumber)
    }

    public suspend fun get(ddayId: String): DiaryAppActionDDayDetail? = ddayAppAction().get(ddayId = ddayId)?.toDiary()

    public suspend fun getIncludingDeleted(ddayId: String): DiaryAppActionDDayDetail? = ddayAppAction().getIncludingDeleted(ddayId = ddayId)?.toDiary()

    public suspend fun add(
        title: String,
        ddayDescription: String?,
        color: String?,
        baseDateEpochMillis: Long?,
        countsBaseDay: Boolean?,
        repeats: List<String>,
        endDateEpochMillis: Long?,
        overflowPolicy: DiaryAppActionDDayOverflowPolicy?,
        tagIds: List<String>,
    ): String =
        ddayAppAction()
            .add(
                title = title,
                description = ddayDescription,
                color = color,
                baseDate = baseDateEpochMillis?.let { millis -> appActionDate(epochMillis = millis) },
                countsBaseDay = countsBaseDay,
                repeats = repeats,
                endDate = endDateEpochMillis?.let { millis -> appActionDate(epochMillis = millis) },
                overflowPolicy = overflowPolicy?.toValue(),
                tagIds = tagIds,
            ).id

    public suspend fun update(
        ddayId: String,
        title: String?,
        ddayDescription: String?,
        color: String?,
        baseDateEpochMillis: Long?,
        countsBaseDay: Boolean?,
        repeats: List<String>?,
        endDateEpochMillis: Long?,
        clearEndDate: Boolean?,
        overflowPolicy: DiaryAppActionDDayOverflowPolicy?,
    ): Int =
        ddayAppAction()
            .update(
                ddayId = ddayId,
                title = title,
                description = ddayDescription,
                color = color,
                baseDate = baseDateEpochMillis?.let { millis -> appActionDate(epochMillis = millis) },
                countsBaseDay = countsBaseDay,
                repeats = repeats,
                endDate = endDateEpochMillis?.let { millis -> appActionDate(epochMillis = millis) },
                clearEndDate = clearEndDate,
                overflowPolicy = overflowPolicy?.toValue(),
            ).changedCount

    public suspend fun delete(ddayId: String): Int = ddayAppAction().delete(ddayId = ddayId).changedCount

    public suspend fun restore(ddayId: String): Int = ddayAppAction().restore(ddayId = ddayId).changedCount
}

private fun ddayAppAction(): DDayAppAction = diaryAppAction()

private fun AppActionDDayDetail.toDiary(): DiaryAppActionDDayDetail = DiaryAppActionDDayDetail(dday = dday.toDiary(), tags = tags.map { tag -> tag.toDiary() })

internal fun AppActionDDay.toDiary(): DiaryAppActionDDay =
    DiaryAppActionDDay(
        id = id,
        title = title,
        ddayDescription = description,
        color = color,
        baseDateEpochMillis = appActionDateEpochMillis(date = baseDate),
        countsBaseDay = countsBaseDay,
        repeats = repeats,
        endDateEpochMillis = endDate?.let { date -> appActionDateEpochMillis(date = date) },
        overflowPolicy = overflowPolicy.toDiaryOverflowPolicy(),
        status = count.status.toDiaryStatus(),
        remainingDays = count.remainingDays,
        passedDays = count.passedDays,
        years = count.years,
        months = count.months,
        createdAtEpochMillis = appActionEpochMillis(instant = createdAt),
        updatedAtEpochMillis = appActionEpochMillis(instant = updatedAt),
    )

private fun DiaryAppActionDDayOverflowPolicy.toValue(): String =
    when (this) {
        DiaryAppActionDDayOverflowPolicy.CLAMP_TO_MONTH_END -> AppActionDDayOverflowPolicyValue.CLAMP_TO_MONTH_END
        DiaryAppActionDDayOverflowPolicy.SKIP -> AppActionDDayOverflowPolicyValue.SKIP
        DiaryAppActionDDayOverflowPolicy.NEXT_MONTH_FIRST_DAY -> AppActionDDayOverflowPolicyValue.NEXT_MONTH_FIRST_DAY
    }

private fun String.toDiaryOverflowPolicy(): DiaryAppActionDDayOverflowPolicy =
    when (this) {
        AppActionDDayOverflowPolicyValue.CLAMP_TO_MONTH_END -> DiaryAppActionDDayOverflowPolicy.CLAMP_TO_MONTH_END
        AppActionDDayOverflowPolicyValue.SKIP -> DiaryAppActionDDayOverflowPolicy.SKIP
        AppActionDDayOverflowPolicyValue.NEXT_MONTH_FIRST_DAY -> DiaryAppActionDDayOverflowPolicy.NEXT_MONTH_FIRST_DAY
        else -> error("Unknown D-Day overflow policy: $this")
    }

private fun String.toDiaryStatus(): DiaryAppActionDDayStatus =
    when (this) {
        AppActionDDayStatusValue.UPCOMING -> DiaryAppActionDDayStatus.UPCOMING
        AppActionDDayStatusValue.TODAY -> DiaryAppActionDDayStatus.TODAY
        AppActionDDayStatusValue.PASSED -> DiaryAppActionDDayStatus.PASSED
        else -> error("Unknown D-Day status: $this")
    }
