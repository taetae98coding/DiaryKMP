package io.github.taetae98coding.diary.work.appaction.qr

import io.github.taetae98coding.diary.work.appaction.core.useCase

internal fun qrAppActionOf(vararg useCaseArray: Any): QrAppAction {
    val useCaseList = useCaseArray.toList()

    return QrAppAction(
        pageQrUseCase = useCaseList.useCase(),
        findQrUseCase = useCaseList.useCase(),
        addQrUseCase = useCaseList.useCase(),
        updateQrUseCase = useCaseList.useCase(),
        deleteQrUseCase = useCaseList.useCase(),
        restoreQrUseCase = useCaseList.useCase(),
    )
}
