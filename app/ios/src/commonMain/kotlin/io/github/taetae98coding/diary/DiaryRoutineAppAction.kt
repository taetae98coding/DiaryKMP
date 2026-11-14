package io.github.taetae98coding.diary

import io.github.taetae98coding.diary.work.appaction.core.appActionDate
import io.github.taetae98coding.diary.work.appaction.core.appActionDateEpochMillis
import io.github.taetae98coding.diary.work.appaction.core.appActionEpochMillis
import io.github.taetae98coding.diary.work.appaction.core.readNumberedPage
import io.github.taetae98coding.diary.work.appaction.routine.AppActionRoutine
import io.github.taetae98coding.diary.work.appaction.routine.AppActionRoutineCycleValue
import io.github.taetae98coding.diary.work.appaction.routine.AppActionRoutineDetail
import io.github.taetae98coding.diary.work.appaction.routine.AppActionRoutineRecord
import io.github.taetae98coding.diary.work.appaction.routine.AppActionRoutineStatusValue
import io.github.taetae98coding.diary.work.appaction.routine.RoutineAppAction
import io.github.taetae98coding.diary.work.appaction.routine.RoutineListAppAction
import io.github.taetae98coding.diary.work.appaction.routine.RoutineRecordAppAction

public enum class DiaryAppActionRoutineStatus {
    NOT_STARTED,
    IN_PROGRESS,
}

public enum class DiaryAppActionRoutineCycle {
    DAILY,
    WEEKDAYS,
    WEEKENDS,
    WEEKLY,
    MONTHLY,
}

public data class DiaryAppActionRoutine(
    val id: String,
    val title: String,
    val routineDescription: String,
    val color: String,
    val startDateEpochMillis: Long,
    val cycle: DiaryAppActionRoutineCycle,
    val unit: String,
    val goalAmount: Int?,
    val amountPerRecord: Int,
    val isFinished: Boolean,
    val status: DiaryAppActionRoutineStatus,
    val cycleAmount: Long?,
    val achievementRate: Int?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

public data class DiaryAppActionRoutinePage(
    val items: List<DiaryAppActionRoutine>,
    val nextPageNumber: Int?,
)

public data class DiaryAppActionRoutineDetail(
    val routine: DiaryAppActionRoutine,
    val tags: List<DiaryAppActionTag>,
)

// 양 문구는 루틴의 단위로 읽으므로 기록마다 그 루틴의 단위를 함께 담는다.
public data class DiaryAppActionRoutineRecord(
    val id: String,
    val routineId: String,
    val dateEpochMillis: Long,
    val amount: Int,
    val unit: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

public data class DiaryAppActionRoutineRecordPage(
    val items: List<DiaryAppActionRoutineRecord>,
    val nextPageNumber: Int?,
)

public object DiaryRoutineAppAction {
    public suspend fun list(pageNumber: Int): DiaryAppActionRoutinePage {
        val page = readNumberedPage(pageNumber = pageNumber) { cursor -> routineListAppAction().list(cursor = cursor) }

        return DiaryAppActionRoutinePage(items = page.items.map { routine -> routine.toDiary() }, nextPageNumber = page.nextPageNumber)
    }

    public suspend fun finishedList(pageNumber: Int): DiaryAppActionRoutinePage {
        val page = readNumberedPage(pageNumber = pageNumber) { cursor -> routineListAppAction().finishedList(cursor = cursor) }

        return DiaryAppActionRoutinePage(items = page.items.map { routine -> routine.toDiary() }, nextPageNumber = page.nextPageNumber)
    }

    public suspend fun get(routineId: String): DiaryAppActionRoutineDetail? = routineAppAction().get(routineId = routineId)?.toDiary()

    public suspend fun getIncludingDeleted(routineId: String): DiaryAppActionRoutineDetail? = routineAppAction().getIncludingDeleted(routineId = routineId)?.toDiary()

    public suspend fun add(
        title: String,
        routineDescription: String?,
        startDateEpochMillis: Long?,
        cycle: DiaryAppActionRoutineCycle?,
        unit: String?,
        goalAmount: Int?,
        amountPerRecord: Int?,
        tagIds: List<String>,
    ): String =
        routineAppAction()
            .add(
                title = title,
                description = routineDescription,
                startDate = startDateEpochMillis?.let { millis -> appActionDate(epochMillis = millis) },
                cycle = cycle?.toValue(),
                unit = unit,
                goalAmount = goalAmount,
                amountPerRecord = amountPerRecord,
                tagIds = tagIds,
            ).id

    public suspend fun update(
        routineId: String,
        title: String?,
        routineDescription: String?,
        color: String?,
        startDateEpochMillis: Long?,
        cycle: DiaryAppActionRoutineCycle?,
        unit: String?,
        goalAmount: Int?,
        clearGoalAmount: Boolean?,
        amountPerRecord: Int?,
    ): Int =
        routineAppAction()
            .update(
                routineId = routineId,
                title = title,
                description = routineDescription,
                color = color,
                startDate = startDateEpochMillis?.let { millis -> appActionDate(epochMillis = millis) },
                cycle = cycle?.toValue(),
                unit = unit,
                goalAmount = goalAmount,
                clearGoalAmount = clearGoalAmount,
                amountPerRecord = amountPerRecord,
            ).changedCount

    public suspend fun finish(routineId: String): Int = routineAppAction().finish(routineId = routineId).changedCount

    public suspend fun restart(routineId: String): Int = routineAppAction().restart(routineId = routineId).changedCount

    public suspend fun delete(routineId: String): Int = routineAppAction().delete(routineId = routineId).changedCount

    public suspend fun restore(routineId: String): Int = routineAppAction().restore(routineId = routineId).changedCount
}

public object DiaryRoutineRecordAppAction {
    public suspend fun list(
        routineId: String,
        pageNumber: Int,
    ): DiaryAppActionRoutineRecordPage {
        val unit =
            routineAppAction()
                .get(routineId = routineId)
                ?.routine
                ?.unit
        val page = readNumberedPage(pageNumber = pageNumber) { cursor -> routineRecordAppAction().list(routineId = routineId, cursor = cursor) }

        return DiaryAppActionRoutineRecordPage(
            items = if (unit == null) emptyList() else page.items.map { record -> record.toDiary(unit = unit) },
            nextPageNumber = page.nextPageNumber,
        )
    }

    // 기록 하나만 찾는 조회가 없어 기록 선택 입력이 고른 기록을 다시 찾을 때 그 루틴의 기록을 페이지 순서대로 훑는다.
    public suspend fun find(
        routineId: String,
        recordId: String,
    ): DiaryAppActionRoutineRecord? {
        var pageNumber: Int? = SEARCH_PAGE_NUMBER

        while (pageNumber != null) {
            val page = list(routineId = routineId, pageNumber = pageNumber)

            page.items.firstOrNull { record -> record.id == recordId }?.let { record -> return record }
            pageNumber = page.nextPageNumber
        }

        return null
    }

    public suspend fun add(
        routineId: String,
        dateEpochMillis: Long?,
        amount: Int?,
    ): Int =
        routineRecordAppAction()
            .add(
                routineId = routineId,
                date = dateEpochMillis?.let { millis -> appActionDate(epochMillis = millis) },
                amount = amount,
            ).changedCount

    public suspend fun remove(recordId: String): Int = routineRecordAppAction().remove(recordId = recordId).changedCount
}

private fun routineAppAction(): RoutineAppAction = diaryAppAction()

private fun routineListAppAction(): RoutineListAppAction = diaryAppAction()

private fun routineRecordAppAction(): RoutineRecordAppAction = diaryAppAction()

private fun AppActionRoutineDetail.toDiary(): DiaryAppActionRoutineDetail = DiaryAppActionRoutineDetail(routine = routine.toDiary(), tags = tags.map { tag -> tag.toDiary() })

internal fun AppActionRoutine.toDiary(): DiaryAppActionRoutine =
    DiaryAppActionRoutine(
        id = id,
        title = title,
        routineDescription = description,
        color = color,
        startDateEpochMillis = appActionDateEpochMillis(date = startDate),
        cycle = cycle.toDiaryCycle(),
        unit = unit,
        goalAmount = goalAmount,
        amountPerRecord = amountPerRecord,
        isFinished = isFinished,
        status = progress.status.toDiaryStatus(),
        cycleAmount = progress.cycleAmount,
        achievementRate = progress.achievementRate,
        createdAtEpochMillis = appActionEpochMillis(instant = createdAt),
        updatedAtEpochMillis = appActionEpochMillis(instant = updatedAt),
    )

private fun AppActionRoutineRecord.toDiary(unit: String): DiaryAppActionRoutineRecord =
    DiaryAppActionRoutineRecord(
        id = id,
        routineId = routineId,
        dateEpochMillis = appActionDateEpochMillis(date = date),
        amount = amount,
        unit = unit,
        createdAtEpochMillis = appActionEpochMillis(instant = createdAt),
        updatedAtEpochMillis = appActionEpochMillis(instant = updatedAt),
    )

private fun DiaryAppActionRoutineCycle.toValue(): String =
    when (this) {
        DiaryAppActionRoutineCycle.DAILY -> AppActionRoutineCycleValue.DAILY
        DiaryAppActionRoutineCycle.WEEKDAYS -> AppActionRoutineCycleValue.WEEKDAYS
        DiaryAppActionRoutineCycle.WEEKENDS -> AppActionRoutineCycleValue.WEEKENDS
        DiaryAppActionRoutineCycle.WEEKLY -> AppActionRoutineCycleValue.WEEKLY
        DiaryAppActionRoutineCycle.MONTHLY -> AppActionRoutineCycleValue.MONTHLY
    }

private fun String.toDiaryCycle(): DiaryAppActionRoutineCycle =
    when (this) {
        AppActionRoutineCycleValue.DAILY -> DiaryAppActionRoutineCycle.DAILY
        AppActionRoutineCycleValue.WEEKDAYS -> DiaryAppActionRoutineCycle.WEEKDAYS
        AppActionRoutineCycleValue.WEEKENDS -> DiaryAppActionRoutineCycle.WEEKENDS
        AppActionRoutineCycleValue.WEEKLY -> DiaryAppActionRoutineCycle.WEEKLY
        AppActionRoutineCycleValue.MONTHLY -> DiaryAppActionRoutineCycle.MONTHLY
        else -> error("Unknown routine cycle: $this")
    }

private fun String.toDiaryStatus(): DiaryAppActionRoutineStatus =
    when (this) {
        AppActionRoutineStatusValue.NOT_STARTED -> DiaryAppActionRoutineStatus.NOT_STARTED
        AppActionRoutineStatusValue.IN_PROGRESS -> DiaryAppActionRoutineStatus.IN_PROGRESS
        else -> error("Unknown routine status: $this")
    }
