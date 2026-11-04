package io.github.taetae98coding.diary.data.core.mapper

import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.testing.finishedAndDeletedCaseList
import io.github.taetae98coding.diary.core.testing.tag.localTag
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class TagMapperTest :
    FunSpec({
        test("domain to local") {
            finishedAndDeletedCaseList.forEach { (isFinished, isDeleted) ->
                val domain = Arb.tag(isFinished = isFinished, isDeleted = isDeleted).next()

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
                val local = Arb.localTag(isFinished = isFinished, isDeleted = isDeleted).next()

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
                val domain = Arb.tag(isFinished = isFinished, isDeleted = isDeleted).next()

                domain.toLocal().toDomain() shouldBe domain
            }
        }
    })
