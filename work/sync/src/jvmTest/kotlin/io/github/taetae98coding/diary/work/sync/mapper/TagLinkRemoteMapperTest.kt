package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.taglink.entity.TagLinkLocalEntity
import io.github.taetae98coding.diary.core.network.api.taglink.entity.TagLinkRemoteEntity
import io.github.taetae98coding.diary.core.testing.taglink.tagLinkLocalEntity
import io.github.taetae98coding.diary.core.testing.taglink.tagLinkRemoteEntity
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlin.time.Instant

class TagLinkRemoteMapperTest :
    FunSpec({
        test("local to remote") {
            listOf(true, false).forEach { isDeleted ->
                val local = localTagLink(isDeleted = isDeleted)

                local.toRemote() shouldBe
                    TagLinkRemoteEntity(
                        fromTagId = local.fromTagId,
                        toTagId = local.toTagId,
                        isDeleted = isDeleted,
                        updatedAt = local.updatedAt,
                        createdAt = local.createdAt,
                    )
            }
        }

        test("remote to local") {
            listOf(true, false).forEach { isDeleted ->
                val remote = remoteTagLink(isDeleted = isDeleted)

                remote.toLocal() shouldBe
                    TagLinkLocalEntity(
                        fromTagId = remote.fromTagId,
                        toTagId = remote.toTagId,
                        isDeleted = isDeleted,
                        updatedAt = remote.updatedAt,
                        createdAt = remote.createdAt,
                    )
            }
        }

        test("local to remote to local") {
            listOf(true, false).forEach { isDeleted ->
                val local = localTagLink(isDeleted = isDeleted)

                local.toRemote().toLocal() shouldBe local
            }
        }

        test("remote to local to remote") {
            listOf(true, false).forEach { isDeleted ->
                val remote = remoteTagLink(isDeleted = isDeleted)

                remote.toLocal().toRemote() shouldBe remote
            }
        }
    })

private fun localTagLink(isDeleted: Boolean): TagLinkLocalEntity =
    Arb
        .tagLinkLocalEntity()
        .next()
        .copy(
            isDeleted = isDeleted,
            updatedAt = Arb.secondPrecisionInstant().next(),
            createdAt = Arb.secondPrecisionInstant().next(),
        )

private fun remoteTagLink(isDeleted: Boolean): TagLinkRemoteEntity =
    Arb
        .tagLinkRemoteEntity()
        .next()
        .copy(
            isDeleted = isDeleted,
            updatedAt = Arb.secondPrecisionInstant().next(),
            createdAt = Arb.secondPrecisionInstant().next(),
        )
