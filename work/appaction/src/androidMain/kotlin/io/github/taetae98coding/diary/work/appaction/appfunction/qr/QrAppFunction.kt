package io.github.taetae98coding.diary.work.appaction.appfunction.qr

import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionChangedCount
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionId
import io.github.taetae98coding.diary.work.appaction.appfunction.core.appFunctionCall
import io.github.taetae98coding.diary.work.appaction.appfunction.core.toAppFunctionChangedCount
import io.github.taetae98coding.diary.work.appaction.appfunction.core.toAppFunctionId
import io.github.taetae98coding.diary.work.appaction.qr.QrAppAction

internal class QrAppFunction(
    private val qrAppAction: QrAppAction,
) {
    suspend fun list(cursor: String?): AppFunctionQrPage = appFunctionCall { qrAppAction.list(cursor = cursor).toAppFunctionQrPage() }

    suspend fun get(qrId: String): AppFunctionQr? = appFunctionCall { qrAppAction.get(qrId = qrId)?.toAppFunctionQr() }

    suspend fun add(
        title: String,
        description: String?,
        value: String,
    ): AppFunctionId = appFunctionCall { qrAppAction.add(title = title, description = description, value = value).toAppFunctionId() }

    suspend fun update(
        qrId: String,
        title: String?,
        description: String?,
        value: String?,
    ): AppFunctionChangedCount = appFunctionCall { qrAppAction.update(qrId = qrId, title = title, description = description, value = value).toAppFunctionChangedCount() }

    suspend fun delete(qrId: String): AppFunctionChangedCount = appFunctionCall { qrAppAction.delete(qrId = qrId).toAppFunctionChangedCount() }

    suspend fun restore(qrId: String): AppFunctionChangedCount = appFunctionCall { qrAppAction.restore(qrId = qrId).toAppFunctionChangedCount() }
}
