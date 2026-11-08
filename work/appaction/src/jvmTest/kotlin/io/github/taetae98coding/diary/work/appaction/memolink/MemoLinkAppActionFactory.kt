package io.github.taetae98coding.diary.work.appaction.memolink

import io.github.taetae98coding.diary.work.appaction.core.useCase

internal fun memoLinkAppActionOf(vararg useCaseArray: Any): MemoLinkAppAction {
    val useCaseList = useCaseArray.toList()

    return MemoLinkAppAction(
        addMemoTagUseCase = useCaseList.useCase(),
        removeMemoTagUseCase = useCaseList.useCase(),
        setMemoPrimaryTagUseCase = useCaseList.useCase(),
        unsetMemoPrimaryTagUseCase = useCaseList.useCase(),
        addMemoPlaceUseCase = useCaseList.useCase(),
        removeMemoPlaceUseCase = useCaseList.useCase(),
        addMemoWebUseCase = useCaseList.useCase(),
        removeMemoWebUseCase = useCaseList.useCase(),
        addMemoContactUseCase = useCaseList.useCase(),
        removeMemoContactUseCase = useCaseList.useCase(),
    )
}
