package io.github.taetae98coding.diary.data.core.mapper

import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceDetailLocalEntity
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.core.testing.place.placeDetail
import io.github.taetae98coding.diary.core.testing.place.placeDetailLocalEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class PlaceDetailMapperTest :
    FunSpec({
        test("domain to local") {
            val domain = Arb.placeDetail().next()

            domain.toLocal() shouldBe
                PlaceDetailLocalEntity(
                    title = domain.title,
                    description = domain.description,
                    color = domain.color,
                    latitude = domain.coordinate.latitude,
                    longitude = domain.coordinate.longitude,
                    address = domain.address,
                )
        }

        test("local to domain") {
            val local = Arb.placeDetailLocalEntity().next()

            local.toDomain() shouldBe
                PlaceDetail(
                    title = local.title,
                    description = local.description,
                    color = local.color,
                    coordinate =
                        Coordinate(
                            latitude = local.latitude,
                            longitude = local.longitude,
                        ),
                    address = local.address,
                )
        }

        test("domain to local to domain") {
            val domain = Arb.placeDetail().next()

            domain.toLocal().toDomain() shouldBe domain
        }

        test("local to domain to local") {
            val local = Arb.placeDetailLocalEntity().next()

            local.toDomain().toLocal() shouldBe local
        }
    })
