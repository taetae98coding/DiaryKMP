package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.qr.entity.QrDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import io.github.taetae98coding.diary.core.network.api.qr.entity.QrDetailRemoteEntity
import io.github.taetae98coding.diary.core.network.api.qr.entity.QrRemoteEntity
import io.github.taetae98coding.diary.core.testing.qr.qrDetailLocalEntity
import io.github.taetae98coding.diary.core.testing.qr.qrDetailRemoteEntity
import io.github.taetae98coding.diary.core.testing.qr.qrLocalEntity
import io.github.taetae98coding.diary.core.testing.qr.qrRemoteEntity
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlin.time.Instant

class QrRemoteMapperTest :
    FunSpec({
        test("QR 내용을 원격 모델로 바꿀 때 제목, 설명과 QR 값을 그대로 옮긴다") {
            val local = Arb.qrDetailLocalEntity().next()

            local.toRemote() shouldBe
                QrDetailRemoteEntity(
                    title = local.title,
                    description = local.description,
                    value = local.value,
                )
        }

        test("원격 QR 내용을 로컬 모델로 바꿀 때 제목, 설명과 QR 값을 그대로 옮긴다") {
            val remote = Arb.qrDetailRemoteEntity().next()

            remote.toLocal() shouldBe
                QrDetailLocalEntity(
                    title = remote.title,
                    description = remote.description,
                    value = remote.value,
                )
        }

        test("로컬 QR은 원격 모델을 거쳐도 바뀌지 않는다") {
            val local =
                Arb
                    .qrLocalEntity()
                    .next()
                    .copy(
                        updatedAt = Arb.secondPrecisionInstant().next(),
                        createdAt = Arb.secondPrecisionInstant().next(),
                    )

            local.toRemote().toLocal() shouldBe local
        }

        test("원격 QR은 로컬 모델을 거쳐도 바뀌지 않는다") {
            val remote =
                Arb
                    .qrRemoteEntity()
                    .next()
                    .copy(
                        updatedAt = Arb.secondPrecisionInstant().next(),
                        createdAt = Arb.secondPrecisionInstant().next(),
                    )

            remote.toLocal().toRemote() shouldBe remote
        }
    })
