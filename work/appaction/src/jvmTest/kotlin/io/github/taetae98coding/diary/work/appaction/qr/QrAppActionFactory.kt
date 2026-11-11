package io.github.taetae98coding.diary.work.appaction.qr

import io.github.taetae98coding.diary.core.testing.qr.qr
import io.github.taetae98coding.diary.work.appaction.core.findUseCase
import io.github.taetae98coding.diary.work.appaction.core.useCase
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

internal fun qrAppActionOf(vararg useCaseArray: Any): QrAppAction {
    val useCaseList = useCaseArray.toList()

    return QrAppAction(
        pageQrUseCase = useCaseList.useCase(),
        findQrUseCase = useCaseList.findUseCase { Arb.qr().next().copy(isDeleted = false) },
        getEntityTagUseCase = useCaseList.useCase(),
        addQrUseCase = useCaseList.useCase(),
        updateQrUseCase = useCaseList.useCase(),
        deleteQrUseCase = useCaseList.useCase(),
        restoreQrUseCase = useCaseList.useCase(),
    )
}
