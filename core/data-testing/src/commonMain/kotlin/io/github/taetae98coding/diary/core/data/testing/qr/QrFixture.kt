package io.github.taetae98coding.diary.core.data.testing.qr

import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import io.github.taetae98coding.diary.core.network.api.qr.entity.QrRemoteEntity
import io.kotest.property.Arb
import io.kotest.property.arbitrary.map

public fun Arb.Companion.localQr(isDeleted: Boolean): Arb<QrLocalEntity> = Arb.qrLocalEntity().map { qr -> qr.copy(isDeleted = isDeleted) }

public fun Arb.Companion.remoteQr(isDeleted: Boolean): Arb<QrRemoteEntity> = Arb.qrRemoteEntity().map { qr -> qr.copy(isDeleted = isDeleted) }
