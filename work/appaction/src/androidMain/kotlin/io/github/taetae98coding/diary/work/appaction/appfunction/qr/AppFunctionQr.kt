package io.github.taetae98coding.diary.work.appaction.appfunction.qr

import androidx.appfunctions.AppFunctionSerializable
import io.github.taetae98coding.diary.work.appaction.appfunction.item.AppFunctionTag
import io.github.taetae98coding.diary.work.appaction.appfunction.item.toAppFunctionTag
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.qr.AppActionQr
import io.github.taetae98coding.diary.work.appaction.qr.AppActionQrDetail

/** A saved QR code. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionQr(
    /** Id of the QR. */
    val id: String,
    /** Title of the QR. */
    val title: String,
    /** Description of the QR. */
    val description: String,
    /** Text encoded in the QR. */
    val value: String,
    /** When the QR was created, in ISO-8601. */
    val createdAt: String,
    /** When the QR was last updated, in ISO-8601. */
    val updatedAt: String,
)

/** A saved QR code with its linked tags. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionQrDetail(
    /** The QR code. */
    val qr: AppFunctionQr,
    /** Tags linked to the QR code. */
    val tags: List<AppFunctionTag>,
)

/** One page of QR codes. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionQrPage(
    /** QR codes in this page, at most 100. */
    val items: List<AppFunctionQr>,
    /** Pass this value as cursor to read the next page. Absent on the last page. */
    val nextCursor: String?,
)

internal fun AppActionQr.toAppFunctionQr(): AppFunctionQr =
    AppFunctionQr(
        id = id,
        title = title,
        description = description,
        value = value,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

internal fun AppActionPage<AppActionQr>.toAppFunctionQrPage(): AppFunctionQrPage = AppFunctionQrPage(items = items.map { qr -> qr.toAppFunctionQr() }, nextCursor = nextCursor)

internal fun AppActionQrDetail.toAppFunctionQrDetail(): AppFunctionQrDetail = AppFunctionQrDetail(qr = qr.toAppFunctionQr(), tags = tags.map { tag -> tag.toAppFunctionTag() })
