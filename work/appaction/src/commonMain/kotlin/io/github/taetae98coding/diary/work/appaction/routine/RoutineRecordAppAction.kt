package io.github.taetae98coding.diary.work.appaction.routine

import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.core.model.routine.RoutineRecord
import io.github.taetae98coding.diary.domain.routine.ROUTINE_AMOUNT_RANGE
import io.github.taetae98coding.diary.domain.routine.progress.canRecord
import io.github.taetae98coding.diary.domain.routine.usecase.AddRoutineRecordUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.DeleteRoutineRecordUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FindRoutineRecordUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FindRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.PageRoutineRecordUseCase
import io.github.taetae98coding.diary.work.appaction.core.AppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.core.changeActive
import io.github.taetae98coding.diary.work.appaction.core.invalidArgument
import io.github.taetae98coding.diary.work.appaction.core.map
import io.github.taetae98coding.diary.work.appaction.core.pageActive
import io.github.taetae98coding.diary.work.appaction.core.readActive
import io.github.taetae98coding.diary.work.appaction.core.readPage
import io.github.taetae98coding.diary.work.appaction.core.runAppAction
import io.github.taetae98coding.diary.work.appaction.core.toAppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.toLocalDateArgumentOrNull
import io.github.taetae98coding.diary.work.appaction.core.toPageOffset
import io.github.taetae98coding.diary.work.appaction.core.toUuidArgument
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.koin.core.annotation.Factory
import kotlin.time.Clock

private const val AMOUNT = "amount"
private const val DATE = "date"
private const val RECORD_ID = "recordId"

@Factory
public class RoutineRecordAppAction internal constructor(
    private val findRoutineUseCase: FindRoutineUseCase,
    private val findRoutineRecordUseCase: FindRoutineRecordUseCase,
    private val pageRoutineRecordUseCase: PageRoutineRecordUseCase,
    private val addRoutineRecordUseCase: AddRoutineRecordUseCase,
    private val deleteRoutineRecordUseCase: DeleteRoutineRecordUseCase,
    private val clock: Clock,
) {
    public suspend fun list(
        routineId: String,
        cursor: String?,
    ): AppActionPage<AppActionRoutineRecord> =
        runAppAction {
            val id = routineId.toUuidArgument(name = ROUTINE_ID)
            val offset = cursor.toPageOffset()

            findRoutineUseCase.pageActive(id = id, isDeleted = Routine::isDeleted) {
                pageRoutineRecordUseCase.readPage(parameter = id, offset = offset).map { record -> record.toAppActionRoutineRecord() }
            }
        }

    public suspend fun add(
        routineId: String,
        date: String?,
        amount: Int?,
    ): AppActionChangedCount =
        runAppAction {
            val id = routineId.toUuidArgument(name = ROUTINE_ID)
            val newDate = date.toLocalDateArgumentOrNull(name = DATE)
            val detail = findRoutineUseCase.readActive(id = id, isDeleted = Routine::isDeleted)?.detail ?: return@runAppAction 0.toAppActionChangedCount()
            val parameter =
                AddRoutineRecordUseCase.Parameter(
                    routineId = id,
                    date = newDate ?: clock.todayIn(TimeZone.currentSystemDefault()),
                    amount = amount ?: detail.amountPerRecord,
                )

            if (parameter.amount !in ROUTINE_AMOUNT_RANGE) invalidArgument(message = "'$AMOUNT' must be between ${ROUTINE_AMOUNT_RANGE.first} and ${ROUTINE_AMOUNT_RANGE.last}.")
            if (!detail.canRecord(date = parameter.date)) invalidArgument(message = "'$DATE' must be on or after the start date of the routine.")

            addRoutineRecordUseCase(parameter = parameter).getOrThrow()
            1.toAppActionChangedCount()
        }

    public suspend fun remove(recordId: String): AppActionChangedCount =
        runAppAction {
            val id = recordId.toUuidArgument(name = RECORD_ID)
            val record = findRoutineRecordUseCase.readActive(id = id, isDeleted = RoutineRecord::isDeleted) ?: return@runAppAction 0.toAppActionChangedCount()

            findRoutineUseCase.changeActive(id = record.routineId, isDeleted = Routine::isDeleted) { deleteRoutineRecordUseCase(parameter = id) }
        }
}
