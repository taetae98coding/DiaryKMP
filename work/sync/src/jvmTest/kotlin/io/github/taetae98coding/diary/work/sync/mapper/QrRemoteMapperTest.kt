package io.github.taetae98coding.diary.work.sync.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import io.github.taetae98coding.diary.core.network.api.qr.entity.QrDetailRemoteEntity
import io.github.taetae98coding.diary.core.network.api.qr.entity.QrRemoteEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Instant

class QrRemoteMapperTest :
    FunSpec({
        test("QR 내용을 원격 모델로 바꿀 때 제목, 설명과 QR 값을 그대로 옮긴다") {
            val local = fixtureMonkey.giveMeOne<QrDetailLocalEntity>()

            local.toRemote() shouldBe
                QrDetailRemoteEntity(
                    title = local.title,
                    description = local.description,
                    value = local.value,
                )
        }

        test("원격 QR 내용을 로컬 모델로 바꿀 때 제목, 설명과 QR 값을 그대로 옮긴다") {
            val remote = fixtureMonkey.giveMeOne<QrDetailRemoteEntity>()

            remote.toLocal() shouldBe
                QrDetailLocalEntity(
                    title = remote.title,
                    description = remote.description,
                    value = remote.value,
                )
        }

        test("로컬 QR은 원격 모델을 거쳐도 바뀌지 않는다") {
            val local =
                fixtureMonkey
                    .giveMeKotlinBuilder<QrLocalEntity>()
                    .setExp(QrLocalEntity::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                    .setExp(QrLocalEntity::createdAt, fixtureMonkey.giveMeOne<Instant>())
                    .sample()

            local.toRemote().toLocal() shouldBe local
        }

        test("원격 QR은 로컬 모델을 거쳐도 바뀌지 않는다") {
            val remote =
                fixtureMonkey
                    .giveMeKotlinBuilder<QrRemoteEntity>()
                    .setExp(QrRemoteEntity::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                    .setExp(QrRemoteEntity::createdAt, fixtureMonkey.giveMeOne<Instant>())
                    .sample()

            remote.toLocal().toRemote() shouldBe remote
        }
    })

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()
