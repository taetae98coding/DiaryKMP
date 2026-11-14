package io.github.taetae98coding.diary.work.appaction.place

import io.github.taetae98coding.diary.core.testing.place.place
import io.github.taetae98coding.diary.work.appaction.core.findUseCase
import io.github.taetae98coding.diary.work.appaction.core.useCase
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

internal fun placeAppActionOf(vararg useCaseArray: Any): PlaceAppAction {
    val useCaseList = useCaseArray.toList()

    return PlaceAppAction(
        pagePlaceHomeUseCase = useCaseList.useCase(),
        getPlaceListUseCase = useCaseList.useCase(),
        findPlaceUseCase = useCaseList.findUseCase { Arb.place().next().copy(isDeleted = false) },
        getEntityTagUseCase = useCaseList.useCase(),
        addPlaceUseCase = useCaseList.useCase(),
        updatePlaceUseCase = useCaseList.useCase(),
        deletePlaceUseCase = useCaseList.useCase(),
        restorePlaceUseCase = useCaseList.useCase(),
    )
}
