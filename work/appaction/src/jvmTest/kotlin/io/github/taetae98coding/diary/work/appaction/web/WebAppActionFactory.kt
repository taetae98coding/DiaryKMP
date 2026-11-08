package io.github.taetae98coding.diary.work.appaction.web

import io.github.taetae98coding.diary.work.appaction.core.useCase

internal fun webAppActionOf(vararg useCaseArray: Any): WebAppAction {
    val useCaseList = useCaseArray.toList()

    return WebAppAction(
        pageWebUseCase = useCaseList.useCase(),
        findWebUseCase = useCaseList.useCase(),
        getEntityTagUseCase = useCaseList.useCase(),
        pageWebMemoUseCase = useCaseList.useCase(),
        pageFinishedWebMemoUseCase = useCaseList.useCase(),
        addWebUseCase = useCaseList.useCase(),
        updateWebUseCase = useCaseList.useCase(),
        deleteWebUseCase = useCaseList.useCase(),
        restoreWebUseCase = useCaseList.useCase(),
        addEntityTagUseCase = useCaseList.useCase(),
        removeEntityTagUseCase = useCaseList.useCase(),
    )
}
