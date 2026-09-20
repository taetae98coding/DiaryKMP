package io.github.taetae98coding.diary.data.place.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceDetailLocalEntity
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class PlaceDetailMapperTest :
    FunSpec({
        test("domain to local") {
            val domain = fixtureMonkey.giveMeOne<PlaceDetail>()

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
            val local = fixtureMonkey.giveMeOne<PlaceDetailLocalEntity>()

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
            val domain = fixtureMonkey.giveMeOne<PlaceDetail>()

            domain.toLocal().toDomain() shouldBe domain
        }

        test("local to domain to local") {
            val local = fixtureMonkey.giveMeOne<PlaceDetailLocalEntity>()

            local.toDomain().toLocal() shouldBe local
        }
    })

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()
