package io.github.taetae98coding.diary.work.sync.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.network.api.tag.entity.TagRemoteEntity
import io.github.taetae98coding.diary.core.testing.finishedAndDeletedCaseList
import io.github.taetae98coding.diary.core.testing.tag.localTag
import io.github.taetae98coding.diary.core.testing.tag.remoteTag
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class TagRemoteMapperTest :
    FunSpec({
        test("TC-DATA-SYNC-DOMAIN-006 TC-DATA-SYNC-DOMAIN-007 local to remote") {
            finishedAndDeletedCaseList.forEach { (isFinished, isDeleted) ->
                val local = fixtureMonkey.localTag(isFinished = isFinished, isDeleted = isDeleted)

                local.toRemote() shouldBe
                    TagRemoteEntity(
                        id = local.id,
                        detail = local.detail.toRemote(),
                        isFinished = isFinished,
                        isDeleted = isDeleted,
                        updatedAt = local.updatedAt,
                        createdAt = local.createdAt,
                    )
            }
        }

        test("remote to local") {
            finishedAndDeletedCaseList.forEach { (isFinished, isDeleted) ->
                val remote = fixtureMonkey.remoteTag(isFinished = isFinished, isDeleted = isDeleted)

                remote.toLocal() shouldBe
                    TagLocalEntity(
                        id = remote.id,
                        detail = remote.detail.toLocal(),
                        isFinished = isFinished,
                        isDeleted = isDeleted,
                        updatedAt = remote.updatedAt,
                        createdAt = remote.createdAt,
                    )
            }
        }

        test("local to remote to local") {
            finishedAndDeletedCaseList.forEach { (isFinished, isDeleted) ->
                val local = fixtureMonkey.localTag(isFinished = isFinished, isDeleted = isDeleted)

                local.toRemote().toLocal() shouldBe local
            }
        }
    })
