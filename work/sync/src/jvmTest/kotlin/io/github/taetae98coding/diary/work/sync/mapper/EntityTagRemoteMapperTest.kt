package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.network.api.entitytag.entity.EntityTagKindRemoteEntity
import io.github.taetae98coding.diary.core.network.api.entitytag.entity.EntityTagRemoteEntity
import io.github.taetae98coding.diary.core.testing.entitytag.entityTagLocalEntity
import io.github.taetae98coding.diary.core.testing.entitytag.entityTagRemoteEntity
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlin.time.Instant

class EntityTagRemoteMapperTest :
    FunSpec({
        test("local to remote") {
            listOf(true, false).forEach { isDeleted ->
                val local = localEntityTag(isDeleted = isDeleted)

                local.toRemote() shouldBe
                    EntityTagRemoteEntity(
                        entityId = local.entityId,
                        tagId = local.tagId,
                        isDeleted = isDeleted,
                        updatedAt = local.updatedAt,
                        createdAt = local.createdAt,
                    )
            }
        }

        test("remote to local") {
            listOf(true, false).forEach { isDeleted ->
                val remote = remoteEntityTag(isDeleted = isDeleted)

                remote.toLocal() shouldBe
                    EntityTagLocalEntity(
                        entityId = remote.entityId,
                        tagId = remote.tagId,
                        isDeleted = isDeleted,
                        updatedAt = remote.updatedAt,
                        createdAt = remote.createdAt,
                    )
            }
        }

        test("종류마다 같은 원격 종류로 변환한다") {
            EntityTagKindLocalEntity.entries.map { kind -> kind.toRemote() } shouldBe
                listOf(
                    EntityTagKindRemoteEntity.WEB,
                    EntityTagKindRemoteEntity.PLACE,
                    EntityTagKindRemoteEntity.CONTACT,
                    EntityTagKindRemoteEntity.CHECKLIST,
                    EntityTagKindRemoteEntity.MUSIC,
                    EntityTagKindRemoteEntity.QR,
                    EntityTagKindRemoteEntity.DDAY,
                )
        }

        test("local to remote to local") {
            listOf(true, false).forEach { isDeleted ->
                val local = localEntityTag(isDeleted = isDeleted)

                local.toRemote().toLocal() shouldBe local
            }
        }

        test("remote to local to remote") {
            listOf(true, false).forEach { isDeleted ->
                val remote = remoteEntityTag(isDeleted = isDeleted)

                remote.toLocal().toRemote() shouldBe remote
            }
        }
    })

private fun localEntityTag(isDeleted: Boolean): EntityTagLocalEntity =
    Arb
        .entityTagLocalEntity()
        .next()
        .copy(
            isDeleted = isDeleted,
            updatedAt = Arb.secondPrecisionInstant().next(),
            createdAt = Arb.secondPrecisionInstant().next(),
        )

private fun remoteEntityTag(isDeleted: Boolean): EntityTagRemoteEntity =
    Arb
        .entityTagRemoteEntity()
        .next()
        .copy(
            isDeleted = isDeleted,
            updatedAt = Arb.secondPrecisionInstant().next(),
            createdAt = Arb.secondPrecisionInstant().next(),
        )
