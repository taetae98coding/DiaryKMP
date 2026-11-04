package io.github.taetae98coding.diary.data.qr.mapper

import io.github.taetae98coding.diary.core.database.api.qr.entity.QrDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.core.model.qr.QrDetail
import io.github.taetae98coding.diary.core.testing.qr.qr
import io.github.taetae98coding.diary.core.testing.qr.qrDetail
import io.github.taetae98coding.diary.core.testing.qr.qrDetailLocalEntity
import io.github.taetae98coding.diary.core.testing.qr.qrLocalEntity
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import kotlin.time.Instant

class QrMapperTest :
    FunSpec({
        test("TC-a1c010f9-3096-4d12-8614-480db602f88a QR 내용을 로컬 모델로 바꿀 때 제목, 설명과 QR 값을 그대로 옮긴다") {
            val domain = Arb.qrDetail().next()

            domain.toLocal() shouldBe
                QrDetailLocalEntity(
                    title = domain.title,
                    description = domain.description,
                    value = domain.value,
                )
        }

        test("TC-a1c010f9-3096-4d12-8614-480db602f88a 로컬 QR 내용을 도메인 모델로 바꿀 때 제목, 설명과 QR 값을 그대로 옮긴다") {
            val local = Arb.qrDetailLocalEntity().next()

            local.toDomain() shouldBe
                QrDetail(
                    title = local.title,
                    description = local.description,
                    value = local.value,
                )
        }

        test("TC-aa007f02-aa4e-44be-92b8-1964418944c9 줄바꿈과 앞뒤 공백이 있는 QR 값은 로컬 모델을 거쳐도 바뀌지 않는다") {
            val value = "  ${Arb.string().next()}\n${Arb.string().next()}  "
            val domain = Arb.qrDetail().next().copy(value = value)

            domain.toLocal().toDomain().value shouldBe value
        }

        test("QR은 로컬 모델을 거쳐도 바뀌지 않는다") {
            val domain =
                Arb
                    .qr()
                    .next()
                    .copy(
                        updatedAt = Arb.secondPrecisionInstant().next(),
                        createdAt = Arb.secondPrecisionInstant().next(),
                    )

            domain.toLocal().toDomain() shouldBe domain
        }

        test("로컬 QR은 도메인 모델을 거쳐도 바뀌지 않는다") {
            val local =
                Arb
                    .qrLocalEntity()
                    .next()
                    .copy(
                        updatedAt = Arb.secondPrecisionInstant().next(),
                        createdAt = Arb.secondPrecisionInstant().next(),
                    )

            local.toDomain().toLocal() shouldBe local
        }
    })
