package io.github.taetae98coding.diary.work.appaction.tag

import io.github.taetae98coding.diary.work.appaction.core.useCase

internal fun tagAppActionOf(vararg useCaseArray: Any): TagAppAction {
    val useCaseList = useCaseArray.toList()

    return TagAppAction(
        pageTagHomeUseCase = useCaseList.useCase(),
        pageFinishedTagUseCase = useCaseList.useCase(),
        findTagUseCase = useCaseList.useCase(),
        getLinkedTagUseCase = useCaseList.useCase(),
        addTagUseCase = useCaseList.useCase(),
        updateTagUseCase = useCaseList.useCase(),
        finishTagUseCase = useCaseList.useCase(),
        restartTagUseCase = useCaseList.useCase(),
        deleteTagUseCase = useCaseList.useCase(),
        restoreTagUseCase = useCaseList.useCase(),
    )
}

internal fun tagLinkAppActionOf(vararg useCaseArray: Any): TagLinkAppAction {
    val useCaseList = useCaseArray.toList()

    return TagLinkAppAction(
        addTagLinkUseCase = useCaseList.useCase(),
        removeTagLinkUseCase = useCaseList.useCase(),
    )
}

internal fun tagItemAppActionOf(vararg useCaseArray: Any): TagItemAppAction {
    val useCaseList = useCaseArray.toList()

    return TagItemAppAction(
        pageTagMemoUseCase = useCaseList.useCase(),
        pageFinishedTagMemoUseCase = useCaseList.useCase(),
        pageTagWebUseCase = useCaseList.useCase(),
        pageTagPlaceUseCase = useCaseList.useCase(),
    )
}
