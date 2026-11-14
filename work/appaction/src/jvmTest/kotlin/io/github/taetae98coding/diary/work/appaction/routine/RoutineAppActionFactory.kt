package io.github.taetae98coding.diary.work.appaction.routine

import io.github.taetae98coding.diary.core.testing.routine.routine
import io.github.taetae98coding.diary.core.testing.routine.routineRecord
import io.github.taetae98coding.diary.work.appaction.core.AppActionRandomColor
import io.github.taetae98coding.diary.work.appaction.core.findUseCase
import io.github.taetae98coding.diary.work.appaction.core.useCase
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

internal fun routineAppActionOf(vararg useCaseArray: Any): RoutineAppAction {
    val useCaseList = useCaseArray.toList()

    return RoutineAppAction(
        findRoutineUseCase = useCaseList.findUseCase { Arb.routine().next().copy(isDeleted = false) },
        findRoutineSummaryUseCase = useCaseList.useCase(),
        getEntityTagUseCase = useCaseList.useCase(),
        addRoutineUseCase = useCaseList.useCase(),
        updateRoutineUseCase = useCaseList.useCase(),
        finishRoutineUseCase = useCaseList.useCase(),
        restartRoutineUseCase = useCaseList.useCase(),
        deleteRoutineUseCase = useCaseList.useCase(),
        restoreRoutineUseCase = useCaseList.useCase(),
        randomColor = useCaseList.filterIsInstance<AppActionRandomColor>().firstOrNull() ?: AppActionRandomColor(),
        clock = useCaseList.useCase(),
    )
}

internal fun routineListAppActionOf(vararg useCaseArray: Any): RoutineListAppAction {
    val useCaseList = useCaseArray.toList()

    return RoutineListAppAction(
        pageRoutineUseCase = useCaseList.useCase(),
        pageFinishedRoutineUseCase = useCaseList.useCase(),
        clock = useCaseList.useCase(),
    )
}

internal fun routineRecordAppActionOf(vararg useCaseArray: Any): RoutineRecordAppAction {
    val useCaseList = useCaseArray.toList()

    return RoutineRecordAppAction(
        findRoutineUseCase = useCaseList.findUseCase { Arb.routine().next().copy(isDeleted = false) },
        findRoutineRecordUseCase = useCaseList.findUseCase { Arb.routineRecord().next().copy(isDeleted = false) },
        pageRoutineRecordUseCase = useCaseList.useCase(),
        addRoutineRecordUseCase = useCaseList.useCase(),
        deleteRoutineRecordUseCase = useCaseList.useCase(),
        clock = useCaseList.useCase(),
    )
}
