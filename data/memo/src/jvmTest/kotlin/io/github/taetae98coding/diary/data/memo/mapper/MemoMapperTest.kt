package io.github.taetae98coding.diary.data.memo.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.testing.memo.localMemo
import io.github.taetae98coding.diary.core.testing.memo.memo
import io.github.taetae98coding.diary.core.testing.memo.memoCaseList
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class MemoMapperTest :
    FunSpec({
        test("domain to local") {
            fixtureMonkey.memoCaseList().forEach { (isFinished, isDeleted, primaryTagId) ->
                val domain = fixtureMonkey.memo(isFinished = isFinished, isDeleted = isDeleted, primaryTagId = primaryTagId)

                domain.toLocal() shouldBe
                    MemoLocalEntity(
                        id = domain.id,
                        detail = domain.detail.toLocal(),
                        primaryTagId = primaryTagId,
                        isFinished = isFinished,
                        isDeleted = isDeleted,
                        updatedAt = domain.updatedAt,
                        createdAt = domain.createdAt,
                    )
            }
        }

        test("local to domain") {
            fixtureMonkey.memoCaseList().forEach { (isFinished, isDeleted, primaryTagId) ->
                val local = fixtureMonkey.localMemo(isFinished = isFinished, isDeleted = isDeleted, primaryTagId = primaryTagId)

                local.toDomain() shouldBe
                    Memo(
                        id = local.id,
                        detail = local.detail.toDomain(),
                        primaryTagId = primaryTagId,
                        isFinished = isFinished,
                        isDeleted = isDeleted,
                        updatedAt = local.updatedAt,
                        createdAt = local.createdAt,
                    )
            }
        }

        test("domain to local to domain") {
            fixtureMonkey.memoCaseList().forEach { (isFinished, isDeleted, primaryTagId) ->
                val domain = fixtureMonkey.memo(isFinished = isFinished, isDeleted = isDeleted, primaryTagId = primaryTagId)

                domain.toLocal().toDomain() shouldBe domain
            }
        }
    })
