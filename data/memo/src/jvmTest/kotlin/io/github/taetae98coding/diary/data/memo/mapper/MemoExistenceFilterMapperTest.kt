package io.github.taetae98coding.diary.data.memo.mapper

import io.github.taetae98coding.diary.core.database.api.memofilter.entity.MemoExistenceFilterLocalEntity
import io.github.taetae98coding.diary.core.model.memo.MemoExistenceFilter
import io.github.taetae98coding.diary.core.model.memo.MemoFilterExistence
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MemoExistenceFilterMapperTest :
    FunSpec({
        test("local to domain") {
            MemoExistenceFilterLocalEntity(
                hasDate = null,
                hasTag = true,
                hasPlace = false,
            ).toDomain() shouldBe
                MemoExistenceFilter(
                    date = MemoFilterExistence.ALL,
                    tag = MemoFilterExistence.EXIST,
                    place = MemoFilterExistence.NOT_EXIST,
                )
        }

        test("domain to local") {
            MemoFilterExistence.ALL.toLocal().shouldBe(null)
            MemoFilterExistence.EXIST.toLocal() shouldBe true
            MemoFilterExistence.NOT_EXIST.toLocal() shouldBe false
        }

        test("domain to local to domain 왕복은 값을 보존한다") {
            MemoFilterExistence.entries.forEach { date ->
                MemoFilterExistence.entries.forEach { tag ->
                    MemoFilterExistence.entries.forEach { place ->
                        MemoExistenceFilterLocalEntity(
                            hasDate = date.toLocal(),
                            hasTag = tag.toLocal(),
                            hasPlace = place.toLocal(),
                        ).toDomain() shouldBe MemoExistenceFilter(date = date, tag = tag, place = place)
                    }
                }
            }
        }
    })
