package io.github.taetae98coding.diary.work.appaction.web

import io.github.taetae98coding.diary.core.testing.web.web
import io.github.taetae98coding.diary.work.appaction.core.findUseCase
import io.github.taetae98coding.diary.work.appaction.core.useCase
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

internal fun webAppActionOf(vararg useCaseArray: Any): WebAppAction {
    val useCaseList = useCaseArray.toList()

    return WebAppAction(
        pageWebUseCase = useCaseList.useCase(),
        findWebUseCase = useCaseList.findUseCase { Arb.web().next().copy(isDeleted = false) },
        getEntityTagUseCase = useCaseList.useCase(),
        addWebUseCase = useCaseList.useCase(),
        updateWebUseCase = useCaseList.useCase(),
        deleteWebUseCase = useCaseList.useCase(),
        restoreWebUseCase = useCaseList.useCase(),
    )
}
