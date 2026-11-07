package io.github.taetae98coding.diary.core.testing.qr

import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.core.model.qr.QrDetail
import io.github.taetae98coding.diary.core.network.api.qr.entity.QrRemoteEntity
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string

// 제목과 값은 비어 있으면 저장하지 않으므로 앞에 고정 문자열을 붙인다.
public fun Arb.Companion.qrDetail(
    title: String? = null,
    description: String? = null,
    value: String? = null,
): Arb<QrDetail> =
    arbitrary {
        QrDetail(
            title = title ?: "title-${Arb.string().bind()}",
            description = description ?: Arb.string().bind(),
            value = value ?: "value-${Arb.string().bind()}",
        )
    }

public fun Arb.Companion.qr(
    isDeleted: Boolean,
    detail: QrDetail? = null,
): Arb<Qr> =
    arbitrary {
        Arb.qr().bind().copy(
            detail = detail ?: Arb.qrDetail().bind(),
            isDeleted = isDeleted,
        )
    }

public fun Arb.Companion.localQr(isDeleted: Boolean): Arb<QrLocalEntity> = Arb.qrLocalEntity().map { qr -> qr.copy(isDeleted = isDeleted) }

public fun Arb.Companion.remoteQr(isDeleted: Boolean): Arb<QrRemoteEntity> = Arb.qrRemoteEntity().map { qr -> qr.copy(isDeleted = isDeleted) }
