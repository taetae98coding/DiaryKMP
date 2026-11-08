package io.github.taetae98coding.diary.work.appaction.place

import io.github.taetae98coding.diary.work.appaction.core.useCase

internal fun placeAppActionOf(vararg useCaseArray: Any): PlaceAppAction {
    val useCaseList = useCaseArray.toList()

    return PlaceAppAction(
        pagePlaceHomeUseCase = useCaseList.useCase(),
        getPlaceListUseCase = useCaseList.useCase(),
        findPlaceUseCase = useCaseList.useCase(),
        getEntityTagUseCase = useCaseList.useCase(),
        pagePlaceMemoUseCase = useCaseList.useCase(),
        pageFinishedPlaceMemoUseCase = useCaseList.useCase(),
        addPlaceUseCase = useCaseList.useCase(),
        updatePlaceUseCase = useCaseList.useCase(),
        deletePlaceUseCase = useCaseList.useCase(),
        restorePlaceUseCase = useCaseList.useCase(),
        addEntityTagUseCase = useCaseList.useCase(),
        removeEntityTagUseCase = useCaseList.useCase(),
    )
}
