package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.webtag.entity.WebTagLocalEntity
import io.github.taetae98coding.diary.core.network.api.webtag.entity.WebTagRemoteEntity
import io.github.taetae98coding.diary.core.testing.webtag.webTagLocalEntity
import io.github.taetae98coding.diary.core.testing.webtag.webTagRemoteEntity
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlin.time.Instant

class WebTagRemoteMapperTest :
    FunSpec({
        test("local to remote") {
            listOf(true, false).forEach { isDeleted ->
                val local = localWebTag(isDeleted = isDeleted)

                local.toRemote() shouldBe
                    WebTagRemoteEntity(
                        webId = local.webId,
                        tagId = local.tagId,
                        isDeleted = isDeleted,
                        updatedAt = local.updatedAt,
                        createdAt = local.createdAt,
                    )
            }
        }

        test("remote to local") {
            listOf(true, false).forEach { isDeleted ->
                val remote = remoteWebTag(isDeleted = isDeleted)

                remote.toLocal() shouldBe
                    WebTagLocalEntity(
                        webId = remote.webId,
                        tagId = remote.tagId,
                        isDeleted = isDeleted,
                        updatedAt = remote.updatedAt,
                        createdAt = remote.createdAt,
                    )
            }
        }

        test("local to remote to local") {
            listOf(true, false).forEach { isDeleted ->
                val local = localWebTag(isDeleted = isDeleted)

                local.toRemote().toLocal() shouldBe local
            }
        }

        test("remote to local to remote") {
            listOf(true, false).forEach { isDeleted ->
                val remote = remoteWebTag(isDeleted = isDeleted)

                remote.toLocal().toRemote() shouldBe remote
            }
        }
    })

private fun localWebTag(isDeleted: Boolean): WebTagLocalEntity =
    Arb
        .webTagLocalEntity()
        .next()
        .copy(
            isDeleted = isDeleted,
            updatedAt = Arb.secondPrecisionInstant().next(),
            createdAt = Arb.secondPrecisionInstant().next(),
        )

private fun remoteWebTag(isDeleted: Boolean): WebTagRemoteEntity =
    Arb
        .webTagRemoteEntity()
        .next()
        .copy(
            isDeleted = isDeleted,
            updatedAt = Arb.secondPrecisionInstant().next(),
            createdAt = Arb.secondPrecisionInstant().next(),
        )
