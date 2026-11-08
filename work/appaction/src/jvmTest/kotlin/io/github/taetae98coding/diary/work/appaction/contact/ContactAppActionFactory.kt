package io.github.taetae98coding.diary.work.appaction.contact

import io.github.taetae98coding.diary.work.appaction.core.useCase

internal fun contactAppActionOf(vararg useCaseArray: Any): ContactAppAction {
    val useCaseList = useCaseArray.toList()

    return ContactAppAction(
        pageContactUseCase = useCaseList.useCase(),
        findContactUseCase = useCaseList.useCase(),
        pageContactMemoUseCase = useCaseList.useCase(),
        pageFinishedContactMemoUseCase = useCaseList.useCase(),
        addContactUseCase = useCaseList.useCase(),
        updateContactUseCase = useCaseList.useCase(),
        favoriteContactUseCase = useCaseList.useCase(),
        unfavoriteContactUseCase = useCaseList.useCase(),
        deleteContactUseCase = useCaseList.useCase(),
        restoreContactUseCase = useCaseList.useCase(),
    )
}
