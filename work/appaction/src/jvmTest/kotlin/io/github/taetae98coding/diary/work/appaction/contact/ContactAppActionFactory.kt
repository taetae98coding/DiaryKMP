package io.github.taetae98coding.diary.work.appaction.contact

import io.github.taetae98coding.diary.core.testing.contact.contact
import io.github.taetae98coding.diary.work.appaction.core.findUseCase
import io.github.taetae98coding.diary.work.appaction.core.useCase
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

internal fun contactAppActionOf(vararg useCaseArray: Any): ContactAppAction {
    val useCaseList = useCaseArray.toList()

    return ContactAppAction(
        pageContactUseCase = useCaseList.useCase(),
        findContactUseCase = useCaseList.findUseCase { Arb.contact().next().copy(isDeleted = false) },
        getEntityTagUseCase = useCaseList.useCase(),
        addContactUseCase = useCaseList.useCase(),
        updateContactUseCase = useCaseList.useCase(),
        favoriteContactUseCase = useCaseList.useCase(),
        unfavoriteContactUseCase = useCaseList.useCase(),
        deleteContactUseCase = useCaseList.useCase(),
        restoreContactUseCase = useCaseList.useCase(),
    )
}
