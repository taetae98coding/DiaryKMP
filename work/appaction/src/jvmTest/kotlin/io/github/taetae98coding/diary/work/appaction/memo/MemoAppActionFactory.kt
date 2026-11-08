package io.github.taetae98coding.diary.work.appaction.memo

import io.github.taetae98coding.diary.work.appaction.core.useCase

internal fun memoAppActionOf(vararg useCaseArray: Any): MemoAppAction {
    val useCaseList = useCaseArray.toList()

    return MemoAppAction(
        pageMemoHomeUseCase = useCaseList.useCase(),
        pageFinishedMemoUseCase = useCaseList.useCase(),
        findMemoUseCase = useCaseList.useCase(),
        getMemoTagUseCase = useCaseList.useCase(),
        getMemoPlaceUseCase = useCaseList.useCase(),
        getMemoWebUseCase = useCaseList.useCase(),
        getMemoContactUseCase = useCaseList.useCase(),
        addMemoUseCase = useCaseList.useCase(),
        updateMemoUseCase = useCaseList.useCase(),
        copyMemoUseCase = useCaseList.useCase(),
        finishMemoUseCase = useCaseList.useCase(),
        restartMemoUseCase = useCaseList.useCase(),
        deleteMemoUseCase = useCaseList.useCase(),
        restoreMemoUseCase = useCaseList.useCase(),
    )
}
