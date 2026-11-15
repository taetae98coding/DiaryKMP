package io.github.taetae98coding.diary.work.appaction.memolink

import io.github.taetae98coding.diary.core.testing.contact.contact
import io.github.taetae98coding.diary.core.testing.memo.memo
import io.github.taetae98coding.diary.core.testing.place.place
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.core.testing.web.web
import io.github.taetae98coding.diary.work.appaction.core.findUseCase
import io.github.taetae98coding.diary.work.appaction.core.useCase
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

internal fun memoLinkAppActionOf(vararg useCaseArray: Any): MemoLinkAppAction {
    val useCaseList = useCaseArray.toList()

    return MemoLinkAppAction(
        findMemoUseCase = useCaseList.findUseCase { Arb.memo().next().copy(isDeleted = false) },
        findTagUseCase = useCaseList.findUseCase { Arb.tag().next().copy(isDeleted = false) },
        findPlaceUseCase = useCaseList.findUseCase { Arb.place().next().copy(isDeleted = false) },
        findWebUseCase = useCaseList.findUseCase { Arb.web().next().copy(isDeleted = false) },
        findContactUseCase = useCaseList.findUseCase { Arb.contact().next().copy(isDeleted = false) },
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
