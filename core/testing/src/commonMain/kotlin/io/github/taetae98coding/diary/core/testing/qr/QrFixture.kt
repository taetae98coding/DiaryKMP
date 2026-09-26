package io.github.taetae98coding.diary.core.testing.qr

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.core.model.qr.QrDetail
import io.github.taetae98coding.diary.core.network.api.qr.entity.QrRemoteEntity

public fun FixtureMonkey.qrDetail(
    title: String = "title-" + giveMeOne<String>(),
    description: String = giveMeOne<String>(),
    value: String = "value-" + giveMeOne<String>(),
): QrDetail =
    QrDetail(
        title = title,
        description = description,
        value = value,
    )

public fun FixtureMonkey.qr(
    isDeleted: Boolean,
    detail: QrDetail = qrDetail(),
): Qr =
    giveMeKotlinBuilder<Qr>()
        .setExp(Qr::isDeleted, isDeleted)
        .sample()
        .copy(detail = detail)

public fun FixtureMonkey.localQr(isDeleted: Boolean): QrLocalEntity =
    giveMeKotlinBuilder<QrLocalEntity>()
        .setExp(QrLocalEntity::isDeleted, isDeleted)
        .sample()

public fun FixtureMonkey.remoteQr(isDeleted: Boolean): QrRemoteEntity =
    giveMeKotlinBuilder<QrRemoteEntity>()
        .setExp(QrRemoteEntity::isDeleted, isDeleted)
        .sample()
