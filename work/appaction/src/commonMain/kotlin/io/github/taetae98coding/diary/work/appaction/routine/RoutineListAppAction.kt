package io.github.taetae98coding.diary.work.appaction.routine

import io.github.taetae98coding.diary.domain.routine.usecase.PageFinishedRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.PageRoutineUseCase
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.core.map
import io.github.taetae98coding.diary.work.appaction.core.readPage
import io.github.taetae98coding.diary.work.appaction.core.runAppAction
import io.github.taetae98coding.diary.work.appaction.core.toPageOffset
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.koin.core.annotation.Factory
import kotlin.time.Clock

@Factory
public class RoutineListAppAction internal constructor(
    private val pageRoutineUseCase: PageRoutineUseCase,
    private val pageFinishedRoutineUseCase: PageFinishedRoutineUseCase,
    private val clock: Clock,
) {
    public suspend fun list(cursor: String?): AppActionPage<AppActionRoutine> =
        runAppAction {
            val today = clock.todayIn(TimeZone.currentSystemDefault())

            pageRoutineUseCase
                .readPage(parameter = today, offset = cursor.toPageOffset())
                .map { summary -> summary.toAppActionRoutine(today = today) }
        }

    public suspend fun finishedList(cursor: String?): AppActionPage<AppActionRoutine> =
        runAppAction {
            val today = clock.todayIn(TimeZone.currentSystemDefault())

            pageFinishedRoutineUseCase
                .readPage(parameter = today, offset = cursor.toPageOffset())
                .map { summary -> summary.toAppActionRoutine(today = today) }
        }
}
