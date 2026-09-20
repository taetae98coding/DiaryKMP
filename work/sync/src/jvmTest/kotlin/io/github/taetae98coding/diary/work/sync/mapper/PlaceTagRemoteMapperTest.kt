package io.github.taetae98coding.diary.work.sync.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.placetag.entity.PlaceTagLocalEntity
import io.github.taetae98coding.diary.core.network.api.placetag.entity.PlaceTagRemoteEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Instant

class PlaceTagRemoteMapperTest :
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
    })

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

private fun localPlaceTag(isDeleted: Boolean): PlaceTagLocalEntity =
    fixtureMonkey
        .giveMeKotlinBuilder<PlaceTagLocalEntity>()
        .setExp(PlaceTagLocalEntity::isDeleted, isDeleted)
        .setExp(PlaceTagLocalEntity::updatedAt, fixtureMonkey.giveMeOne<Instant>())
        .setExp(PlaceTagLocalEntity::createdAt, fixtureMonkey.giveMeOne<Instant>())
        .sample()

private fun remotePlaceTag(isDeleted: Boolean): PlaceTagRemoteEntity =
    fixtureMonkey
        .giveMeKotlinBuilder<PlaceTagRemoteEntity>()
        .setExp(PlaceTagRemoteEntity::isDeleted, isDeleted)
        .setExp(PlaceTagRemoteEntity::updatedAt, fixtureMonkey.giveMeOne<Instant>())
        .setExp(PlaceTagRemoteEntity::createdAt, fixtureMonkey.giveMeOne<Instant>())
        .sample()
