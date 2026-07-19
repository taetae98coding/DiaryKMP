package io.github.taetae98coding.diary.core.mapper.place

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.network.api.place.entity.PlaceRemoteEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Instant

class PlaceMapperTest :
    FunSpec({
        test("domain to local") {
            listOf(true, false).forEach { isDeleted ->
                val domain = place(isDeleted = isDeleted)

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
            listOf(true, false).forEach { isDeleted ->
                val local = localPlace(isDeleted = isDeleted)

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
            listOf(true, false).forEach { isDeleted ->
                val domain = place(isDeleted = isDeleted)

                domain.toLocal().toDomain() shouldBe domain
            }
        }

        test("local to domain to local") {
            listOf(true, false).forEach { isDeleted ->
                val local = localPlace(isDeleted = isDeleted)

                local.toDomain().toLocal() shouldBe local
            }
        }

        test("local to remote") {
            listOf(true, false).forEach { isDeleted ->
                val local = localPlace(isDeleted = isDeleted)

                local.toRemote() shouldBe
                    PlaceRemoteEntity(
                        id = local.id,
                        detail = local.detail.toRemote(),
                        isDeleted = isDeleted,
                        updatedAt = local.updatedAt,
                        createdAt = local.createdAt,
                    )
            }
        }

        test("remote to local") {
            listOf(true, false).forEach { isDeleted ->
                val remote = remotePlace(isDeleted = isDeleted)

                remote.toLocal() shouldBe
                    PlaceLocalEntity(
                        id = remote.id,
                        detail = remote.detail.toLocal(),
                        isDeleted = isDeleted,
                        updatedAt = remote.updatedAt,
                        createdAt = remote.createdAt,
                    )
            }
        }

        test("local to remote to local") {
            listOf(true, false).forEach { isDeleted ->
                val local = localPlace(isDeleted = isDeleted)

                local.toRemote().toLocal() shouldBe local
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun place(isDeleted: Boolean): Place =
            fixtureMonkey
                .giveMeKotlinBuilder<Place>()
                .setExp(Place::isDeleted, isDeleted)
                .setExp(Place::updatedAt, instant())
                .setExp(Place::createdAt, instant())
                .sample()

        private fun localPlace(isDeleted: Boolean): PlaceLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<PlaceLocalEntity>()
                .setExp(PlaceLocalEntity::isDeleted, isDeleted)
                .setExp(PlaceLocalEntity::updatedAt, instant())
                .setExp(PlaceLocalEntity::createdAt, instant())
                .sample()

        private fun remotePlace(isDeleted: Boolean): PlaceRemoteEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<PlaceRemoteEntity>()
                .setExp(PlaceRemoteEntity::isDeleted, isDeleted)
                .setExp(PlaceRemoteEntity::updatedAt, instant())
                .setExp(PlaceRemoteEntity::createdAt, instant())
                .sample()

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
    }
}
