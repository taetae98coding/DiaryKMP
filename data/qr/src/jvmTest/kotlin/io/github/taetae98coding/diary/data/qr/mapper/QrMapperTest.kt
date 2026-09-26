package io.github.taetae98coding.diary.data.qr.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.core.model.qr.QrDetail
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Instant

class QrMapperTest :
    FunSpec({
        test("TC-QR-ADD-DATA-005 QR 내용을 로컬 모델로 바꿀 때 제목, 설명과 QR 값을 그대로 옮긴다") {
            val domain = fixtureMonkey.giveMeOne<QrDetail>()

            domain.toLocal() shouldBe
                QrDetailLocalEntity(
                    title = domain.title,
                    description = domain.description,
                    value = domain.value,
                )
        }

        test("TC-QR-ADD-DATA-005 로컬 QR 내용을 도메인 모델로 바꿀 때 제목, 설명과 QR 값을 그대로 옮긴다") {
            val local = fixtureMonkey.giveMeOne<QrDetailLocalEntity>()

            local.toDomain() shouldBe
                QrDetail(
                    title = local.title,
                    description = local.description,
                    value = local.value,
                )
        }

        test("TC-QR-ADD-DOMAIN-010 줄바꿈과 앞뒤 공백이 있는 QR 값은 로컬 모델을 거쳐도 바뀌지 않는다") {
            val value = "  ${fixtureMonkey.giveMeOne<String>()}\n${fixtureMonkey.giveMeOne<String>()}  "
            val domain = fixtureMonkey.giveMeOne<QrDetail>().copy(value = value)

            domain.toLocal().toDomain().value shouldBe value
        }

        test("QR은 로컬 모델을 거쳐도 바뀌지 않는다") {
            val domain =
                fixtureMonkey
                    .giveMeKotlinBuilder<Qr>()
                    .setExp(Qr::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                    .setExp(Qr::createdAt, fixtureMonkey.giveMeOne<Instant>())
                    .sample()

            domain.toLocal().toDomain() shouldBe domain
        }

        test("로컬 QR은 도메인 모델을 거쳐도 바뀌지 않는다") {
            val local =
                fixtureMonkey
                    .giveMeKotlinBuilder<QrLocalEntity>()
                    .setExp(QrLocalEntity::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                    .setExp(QrLocalEntity::createdAt, fixtureMonkey.giveMeOne<Instant>())
                    .sample()

            local.toDomain().toLocal() shouldBe local
        }
    })

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()
