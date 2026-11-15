package io.github.taetae98coding.diary.work.appaction.checklist

import io.github.taetae98coding.diary.core.testing.checklist.checklist
import io.github.taetae98coding.diary.core.testing.checklist.checklistItem
import io.github.taetae98coding.diary.work.appaction.core.findUseCase
import io.github.taetae98coding.diary.work.appaction.core.useCase
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

internal fun checklistAppActionOf(vararg useCaseArray: Any): ChecklistAppAction {
    val useCaseList = useCaseArray.toList()

    return ChecklistAppAction(
        pageChecklistUseCase = useCaseList.useCase(),
        findChecklistUseCase = useCaseList.findUseCase { Arb.checklist().next().copy(isDeleted = false) },
        getEntityTagUseCase = useCaseList.useCase(),
        getChecklistItemUseCase = useCaseList.useCase(),
        addChecklistUseCase = useCaseList.useCase(),
        updateChecklistUseCase = useCaseList.useCase(),
        resetChecklistUseCase = useCaseList.useCase(),
        deleteChecklistUseCase = useCaseList.useCase(),
        restoreChecklistUseCase = useCaseList.useCase(),
    )
}

internal fun checklistItemAppActionOf(vararg useCaseArray: Any): ChecklistItemAppAction {
    val useCaseList = useCaseArray.toList()

    return ChecklistItemAppAction(
        findChecklistUseCase = useCaseList.findUseCase { Arb.checklist().next().copy(isDeleted = false) },
        findChecklistItemUseCase = useCaseList.findUseCase { Arb.checklistItem().next().copy(isDeleted = false) },
        addChecklistItemUseCase = useCaseList.useCase(),
        checkChecklistItemUseCase = useCaseList.useCase(),
        uncheckChecklistItemUseCase = useCaseList.useCase(),
        deleteChecklistItemUseCase = useCaseList.useCase(),
    )
}
