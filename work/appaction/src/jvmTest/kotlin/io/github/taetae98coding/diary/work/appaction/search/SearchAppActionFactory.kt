package io.github.taetae98coding.diary.work.appaction.search

import io.github.taetae98coding.diary.work.appaction.core.useCase

internal fun searchAppActionOf(vararg useCaseArray: Any): SearchAppAction {
    val useCaseList = useCaseArray.toList()

    return SearchAppAction(
        searchMemoUseCase = useCaseList.useCase(),
        searchTagUseCase = useCaseList.useCase(),
        searchPlaceUseCase = useCaseList.useCase(),
        searchWebUseCase = useCaseList.useCase(),
        searchDDayUseCase = useCaseList.useCase(),
        clock = useCaseList.useCase(),
    )
}
