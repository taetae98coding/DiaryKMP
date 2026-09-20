package io.github.taetae98coding.diary.data.tag.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.testing.finishedAndDeletedCaseList
import io.github.taetae98coding.diary.core.testing.tag.localTag
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class TagMapperTest :
    FunSpec({
        test("domain to local") {
            finishedAndDeletedCaseList.forEach { (isFinished, isDeleted) ->
                val domain = fixtureMonkey.tag(isFinished = isFinished, isDeleted = isDeleted)

                domain.toLocal() shouldBe
                    TagLocalEntity(
                        id = domain.id,
                        detail = domain.detail.toLocal(),
                        isFinished = isFinished,
                        isDeleted = isDeleted,
                        updatedAt = domain.updatedAt,
                        createdAt = domain.createdAt,
                    )
            }
        }

        test("local to domain") {
            finishedAndDeletedCaseList.forEach { (isFinished, isDeleted) ->
                val local = fixtureMonkey.localTag(isFinished = isFinished, isDeleted = isDeleted)

                local.toDomain() shouldBe
                    Tag(
                        id = local.id,
                        detail = local.detail.toDomain(),
                        isFinished = isFinished,
                        isDeleted = isDeleted,
                        updatedAt = local.updatedAt,
                        createdAt = local.createdAt,
                    )
            }
        }

        test("domain to local to domain") {
            finishedAndDeletedCaseList.forEach { (isFinished, isDeleted) ->
                val domain = fixtureMonkey.tag(isFinished = isFinished, isDeleted = isDeleted)

                domain.toLocal().toDomain() shouldBe domain
            }
        }
    })
