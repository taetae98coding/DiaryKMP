package io.github.taetae98coding.diary.work.appaction.qr

import io.github.taetae98coding.diary.core.model.qr.QrDetail
import io.github.taetae98coding.diary.domain.qr.usecase.AddQrUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.DeleteQrUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.FindQrUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.PageQrUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.RestoreQrUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.UpdateQrUseCase
import io.github.taetae98coding.diary.work.appaction.core.AppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.AppActionId
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.core.map
import io.github.taetae98coding.diary.work.appaction.core.readFirst
import io.github.taetae98coding.diary.work.appaction.core.readPage
import io.github.taetae98coding.diary.work.appaction.core.runAppAction
import io.github.taetae98coding.diary.work.appaction.core.toAppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.toAppActionId
import io.github.taetae98coding.diary.work.appaction.core.toPageOffset
import io.github.taetae98coding.diary.work.appaction.core.toUuidArgument
import org.koin.core.annotation.Factory

private const val QR_ID = "qrId"

@Factory
public class QrAppAction internal constructor(
    private val pageQrUseCase: PageQrUseCase,
    private val findQrUseCase: FindQrUseCase,
    private val addQrUseCase: AddQrUseCase,
    private val updateQrUseCase: UpdateQrUseCase,
    private val deleteQrUseCase: DeleteQrUseCase,
    private val restoreQrUseCase: RestoreQrUseCase,
) {
    public suspend fun list(cursor: String?): AppActionPage<AppActionQr> =
        runAppAction {
            pageQrUseCase
                .readPage(parameter = Unit, offset = cursor.toPageOffset())
                .map { qr -> qr.toAppActionQr() }
        }

    public suspend fun get(qrId: String): AppActionQr? = runAppAction { findQrUseCase.readFirst(parameter = qrId.toUuidArgument(name = QR_ID))?.toAppActionQr() }

    public suspend fun add(
        title: String,
        description: String?,
        value: String,
    ): AppActionId =
        runAppAction {
            val parameter =
                AddQrUseCase.Parameter(
                    detail = QrDetail(title = title, description = description.orEmpty(), value = value),
                    tagIdSet = emptySet(),
                )

            addQrUseCase(parameter = parameter).getOrThrow().toAppActionId()
        }

    public suspend fun update(
        qrId: String,
        title: String?,
        description: String?,
        value: String?,
    ): AppActionChangedCount =
        runAppAction {
            val id = qrId.toUuidArgument(name = QR_ID)
            val current = findQrUseCase.readFirst(parameter = id)?.detail ?: return@runAppAction 0.toAppActionChangedCount()
            val detail =
                QrDetail(
                    title = title ?: current.title,
                    description = description ?: current.description,
                    value = value ?: current.value,
                )

            updateQrUseCase(parameter = UpdateQrUseCase.Parameter(id = id, detail = detail)).getOrThrow().toAppActionChangedCount()
        }

    public suspend fun delete(qrId: String): AppActionChangedCount = runAppAction { deleteQrUseCase(parameter = qrId.toUuidArgument(name = QR_ID)).getOrThrow().toAppActionChangedCount() }

    public suspend fun restore(qrId: String): AppActionChangedCount = runAppAction { restoreQrUseCase(parameter = qrId.toUuidArgument(name = QR_ID)).getOrThrow().toAppActionChangedCount() }
}
