package io.github.taetae98coding.diary.data.sync.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.network.api.memo.entity.MemoRemoteEntity
import io.github.taetae98coding.diary.core.testing.memo.localMemo
import io.github.taetae98coding.diary.core.testing.memo.memoCaseList
import io.github.taetae98coding.diary.core.testing.memo.remoteMemo
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class MemoRemoteMapperTest :
    FunSpec({
        // TC-DATA-SYNC-DOMAIN-006, TC-DATA-SYNC-DOMAIN-007
        test("local to remote") {
            fixtureMonkey.memoCaseList().forEach { (isFinished, isDeleted, primaryTagId) ->
                val local = fixtureMonkey.localMemo(isFinished = isFinished, isDeleted = isDeleted, primaryTagId = primaryTagId)

                local.toRemote() shouldBe
                    MemoRemoteEntity(
                        id = local.id,
                        detail = local.detail.toRemote(),
                        primaryTagId = primaryTagId,
                        isFinished = isFinished,
                        isDeleted = isDeleted,
                        updatedAt = local.updatedAt,
                        createdAt = local.createdAt,
                    )
            }
        }

        test("remote to local") {
            fixtureMonkey.memoCaseList().forEach { (isFinished, isDeleted, primaryTagId) ->
                val remote = fixtureMonkey.remoteMemo(isFinished = isFinished, isDeleted = isDeleted, primaryTagId = primaryTagId)

                remote.toLocal() shouldBe
                    MemoLocalEntity(
                        id = remote.id,
                        detail = remote.detail.toLocal(),
                        primaryTagId = primaryTagId,
                        isFinished = isFinished,
                        isDeleted = isDeleted,
                        updatedAt = remote.updatedAt,
                        createdAt = remote.createdAt,
                    )
            }
        }

        test("local to remote to local") {
            fixtureMonkey.memoCaseList().forEach { (isFinished, isDeleted, primaryTagId) ->
                val local = fixtureMonkey.localMemo(isFinished = isFinished, isDeleted = isDeleted, primaryTagId = primaryTagId)

                local.toRemote().toLocal() shouldBe local
            }
        }
    })
