package io.github.taetae98coding.diary.work.appaction.appfunction.routine

import androidx.appfunctions.AppFunctionSerializable
import io.github.taetae98coding.diary.work.appaction.appfunction.item.AppFunctionTag
import io.github.taetae98coding.diary.work.appaction.appfunction.item.toAppFunctionTag
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.routine.AppActionRoutine
import io.github.taetae98coding.diary.work.appaction.routine.AppActionRoutineDetail
import io.github.taetae98coding.diary.work.appaction.routine.AppActionRoutineProgress
import io.github.taetae98coding.diary.work.appaction.routine.AppActionRoutineRecord

/** The progress of a routine, counted from today. It does not depend on whether the routine is finished. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionRoutineProgress(
    /** not_started before the start date, in_progress on or after the start date. */
    val status: String,
    /** Total amount recorded in the cycle that contains today. Present only when in_progress and the routine is not finished. */
    val cycleAmount: Long?,
    /** Percent of the goal amount reached in this cycle, from 0 to 100. Present only when in_progress, the routine is not finished and it has a goal amount. */
    val achievementRate: Int?,
)

/** A routine. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionRoutine(
    /** Id of the routine. */
    val id: String,
    /** Title of the routine. */
    val title: String,
    /** Description of the routine. */
    val description: String,
    /** Color of the routine as a hex color like #4285F4. */
    val color: String,
    /** First date records can be added, like 2026-01-31. */
    val startDate: String,
    /** How often the routine repeats: FREQ=DAILY, FREQ=DAILY;BYDAY=MO,TU,WE,TH,FR, FREQ=DAILY;BYDAY=SA,SU, FREQ=WEEKLY;WKST=SU or FREQ=MONTHLY. */
    val cycle: String,
    /** Unit of the amounts as the user wrote it. Empty when the routine has no unit, and amounts are then read as plain numbers. */
    val unit: String,
    /** Amount to reach in each cycle. Null when the routine has no goal. */
    val goalAmount: Int?,
    /** Amount of one record when no amount is given. */
    val amountPerRecord: Int,
    /** Whether the routine is finished. */
    val isFinished: Boolean,
    /** When the routine was created, in ISO-8601. */
    val createdAt: String,
    /** When the routine was last updated, in ISO-8601. */
    val updatedAt: String,
    /** The progress, counted from today. */
    val progress: AppFunctionRoutineProgress,
)

/** A routine with its linked tags. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionRoutineDetail(
    /** The routine. */
    val routine: AppFunctionRoutine,
    /** Tags linked to the routine. */
    val tags: List<AppFunctionTag>,
)

/** One page of routines. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionRoutinePage(
    /** Routines in this page, at most 100. */
    val items: List<AppFunctionRoutine>,
    /** Pass this value as cursor to read the next page. Absent on the last page. */
    val nextCursor: String?,
)

/** A record of a routine. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionRoutineRecord(
    /** Id of the routine record. */
    val id: String,
    /** Id of the routine the record belongs to. */
    val routineId: String,
    /** Date of the record, like 2026-01-31. */
    val date: String,
    /** Amount of the record in the unit of the routine. */
    val amount: Int,
    /** When the record was created, in ISO-8601. */
    val createdAt: String,
    /** When the record was last updated, in ISO-8601. */
    val updatedAt: String,
)

/** One page of routine records. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionRoutineRecordPage(
    /** Routine records in this page, at most 100. */
    val items: List<AppFunctionRoutineRecord>,
    /** Pass this value as cursor to read the next page. Absent on the last page. */
    val nextCursor: String?,
)

internal fun AppActionPage<AppActionRoutine>.toAppFunctionRoutinePage(): AppFunctionRoutinePage = AppFunctionRoutinePage(items = items.map { routine -> routine.toAppFunctionRoutine() }, nextCursor = nextCursor)

internal fun AppActionPage<AppActionRoutineRecord>.toAppFunctionRoutineRecordPage(): AppFunctionRoutineRecordPage = AppFunctionRoutineRecordPage(items = items.map { record -> record.toAppFunctionRoutineRecord() }, nextCursor = nextCursor)

internal fun AppActionRoutineDetail.toAppFunctionRoutineDetail(): AppFunctionRoutineDetail = AppFunctionRoutineDetail(routine = routine.toAppFunctionRoutine(), tags = tags.map { tag -> tag.toAppFunctionTag() })

internal fun AppActionRoutine.toAppFunctionRoutine(): AppFunctionRoutine =
    AppFunctionRoutine(
        id = id,
        title = title,
        description = description,
        color = color,
        startDate = startDate,
        cycle = cycle,
        unit = unit,
        goalAmount = goalAmount,
        amountPerRecord = amountPerRecord,
        isFinished = isFinished,
        createdAt = createdAt,
        updatedAt = updatedAt,
        progress = progress.toAppFunctionRoutineProgress(),
    )

private fun AppActionRoutineProgress.toAppFunctionRoutineProgress(): AppFunctionRoutineProgress =
    AppFunctionRoutineProgress(
        status = status,
        cycleAmount = cycleAmount,
        achievementRate = achievementRate,
    )

private fun AppActionRoutineRecord.toAppFunctionRoutineRecord(): AppFunctionRoutineRecord =
    AppFunctionRoutineRecord(
        id = id,
        routineId = routineId,
        date = date,
        amount = amount,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
