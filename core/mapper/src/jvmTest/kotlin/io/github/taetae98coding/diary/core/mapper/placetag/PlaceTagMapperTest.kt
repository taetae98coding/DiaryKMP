package io.github.taetae98coding.diary.core.mapper.placetag

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.placetag.entity.PlaceTagLocalEntity
import io.github.taetae98coding.diary.core.network.api.placetag.entity.PlaceTagRemoteEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Instant

class PlaceTagMapperTest :
    FunSpec({
        test("local to remote") {
            listOf(true, false).forEach { isDeleted ->
                val local = localPlaceTag(isDeleted = isDeleted)

                local.toRemote() shouldBe
                    PlaceTagRemoteEntity(
                        placeId = local.placeId,
                        tagId = local.tagId,
                        isDeleted = isDeleted,
                        updatedAt = local.updatedAt,
                        createdAt = local.createdAt,
                    )
            }
        }

        test("remote to local") {
            listOf(true, false).forEach { isDeleted ->
                val remote = remotePlaceTag(isDeleted = isDeleted)

                remote.toLocal() shouldBe
                    PlaceTagLocalEntity(
                        placeId = remote.placeId,
                        tagId = remote.tagId,
                        isDeleted = isDeleted,
                        updatedAt = remote.updatedAt,
                        createdAt = remote.createdAt,
                    )
            }
        }

        test("local to remote to local") {
            listOf(true, false).forEach { isDeleted ->
                val local = localPlaceTag(isDeleted = isDeleted)

                local.toRemote().toLocal() shouldBe local
            }
        }

        test("remote to local to remote") {
            listOf(true, false).forEach { isDeleted ->
                val remote = remotePlaceTag(isDeleted = isDeleted)

                remote.toLocal().toRemote() shouldBe remote
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun localPlaceTag(isDeleted: Boolean): PlaceTagLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<PlaceTagLocalEntity>()
                .setExp(PlaceTagLocalEntity::isDeleted, isDeleted)
                .setExp(PlaceTagLocalEntity::updatedAt, instant())
                .setExp(PlaceTagLocalEntity::createdAt, instant())
                .sample()

        private fun remotePlaceTag(isDeleted: Boolean): PlaceTagRemoteEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<PlaceTagRemoteEntity>()
                .setExp(PlaceTagRemoteEntity::isDeleted, isDeleted)
                .setExp(PlaceTagRemoteEntity::updatedAt, instant())
                .setExp(PlaceTagRemoteEntity::createdAt, instant())
                .sample()

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
    }
}
