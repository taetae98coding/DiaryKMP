package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.placetag.entity.PlaceTagLocalEntity
import io.github.taetae98coding.diary.core.network.api.placetag.entity.PlaceTagRemoteEntity
import io.github.taetae98coding.diary.core.testing.placetag.placeTagLocalEntity
import io.github.taetae98coding.diary.core.testing.placetag.placeTagRemoteEntity
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
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

private fun localPlaceTag(isDeleted: Boolean): PlaceTagLocalEntity =
    Arb
        .placeTagLocalEntity()
        .next()
        .copy(
            isDeleted = isDeleted,
            updatedAt = Arb.secondPrecisionInstant().next(),
            createdAt = Arb.secondPrecisionInstant().next(),
        )

private fun remotePlaceTag(isDeleted: Boolean): PlaceTagRemoteEntity =
    Arb
        .placeTagRemoteEntity()
        .next()
        .copy(
            isDeleted = isDeleted,
            updatedAt = Arb.secondPrecisionInstant().next(),
            createdAt = Arb.secondPrecisionInstant().next(),
        )
