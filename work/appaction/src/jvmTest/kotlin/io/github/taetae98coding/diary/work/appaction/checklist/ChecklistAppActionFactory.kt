package io.github.taetae98coding.diary.work.appaction.checklist

import io.github.taetae98coding.diary.work.appaction.core.useCase

internal fun checklistAppActionOf(vararg useCaseArray: Any): ChecklistAppAction {
    val useCaseList = useCaseArray.toList()

    return ChecklistAppAction(
        pageChecklistUseCase = useCaseList.useCase(),
        findChecklistUseCase = useCaseList.useCase(),
        getChecklistItemUseCase = useCaseList.useCase(),
        addChecklistUseCase = useCaseList.useCase(),
        updateChecklistUseCase = useCaseList.useCase(),
        resetChecklistUseCase = useCaseList.useCase(),
        deleteChecklistUseCase = useCaseList.useCase(),
        restoreChecklistUseCase = useCaseList.useCase(),
        addChecklistItemUseCase = useCaseList.useCase(),
        checkChecklistItemUseCase = useCaseList.useCase(),
        uncheckChecklistItemUseCase = useCaseList.useCase(),
        deleteChecklistItemUseCase = useCaseList.useCase(),
    )
}
