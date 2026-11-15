package io.github.taetae98coding.diary.work.appaction.memo

import io.github.taetae98coding.diary.core.testing.contact.contact
import io.github.taetae98coding.diary.core.testing.memo.memo
import io.github.taetae98coding.diary.core.testing.place.place
import io.github.taetae98coding.diary.core.testing.web.web
import io.github.taetae98coding.diary.work.appaction.core.findUseCase
import io.github.taetae98coding.diary.work.appaction.core.useCase
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

internal fun memoAppActionOf(vararg useCaseArray: Any): MemoAppAction {
    val useCaseList = useCaseArray.toList()

    return MemoAppAction(
        findMemoUseCase = useCaseList.findUseCase { Arb.memo().next().copy(isDeleted = false) },
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

internal fun memoListAppActionOf(vararg useCaseArray: Any): MemoListAppAction {
    val useCaseList = useCaseArray.toList()

    return MemoListAppAction(
        findPlaceUseCase = useCaseList.findUseCase { Arb.place().next().copy(isDeleted = false) },
        findWebUseCase = useCaseList.findUseCase { Arb.web().next().copy(isDeleted = false) },
        findContactUseCase = useCaseList.findUseCase { Arb.contact().next().copy(isDeleted = false) },
        pageMemoHomeUseCase = useCaseList.useCase(),
        pageFinishedMemoUseCase = useCaseList.useCase(),
        pagePlaceMemoUseCase = useCaseList.useCase(),
        pageFinishedPlaceMemoUseCase = useCaseList.useCase(),
        pageWebMemoUseCase = useCaseList.useCase(),
        pageFinishedWebMemoUseCase = useCaseList.useCase(),
        pageContactMemoUseCase = useCaseList.useCase(),
        pageFinishedContactMemoUseCase = useCaseList.useCase(),
    )
}
