package io.github.taetae98coding.diary.data.place.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.testing.isDeletedCaseList
import io.github.taetae98coding.diary.core.testing.place.localPlace
import io.github.taetae98coding.diary.core.testing.place.place
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class PlaceMapperTest :
    FunSpec({
        test("domain to local") {
            isDeletedCaseList.forEach { isDeleted ->
                val domain = fixtureMonkey.place(isDeleted = isDeleted)

                domain.toLocal() shouldBe
                    PlaceLocalEntity(
                        id = domain.id,
                        detail = domain.detail.toLocal(),
                        isDeleted = isDeleted,
                        updatedAt = domain.updatedAt,
                        createdAt = domain.createdAt,
                    )
            }
        }

        test("local to domain") {
            isDeletedCaseList.forEach { isDeleted ->
                val local = fixtureMonkey.localPlace(isDeleted = isDeleted)

                local.toDomain() shouldBe
                    Place(
                        id = local.id,
                        detail = local.detail.toDomain(),
                        isDeleted = isDeleted,
                        updatedAt = local.updatedAt,
                        createdAt = local.createdAt,
                    )
            }
        }

        test("domain to local to domain") {
            isDeletedCaseList.forEach { isDeleted ->
                val domain = fixtureMonkey.place(isDeleted = isDeleted)

                domain.toLocal().toDomain() shouldBe domain
            }
        }

        test("local to domain to local") {
            isDeletedCaseList.forEach { isDeleted ->
                val local = fixtureMonkey.localPlace(isDeleted = isDeleted)

                local.toDomain().toLocal() shouldBe local
            }
        }
    })
