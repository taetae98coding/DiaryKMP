package io.github.taetae98coding.diary.data.web.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.testing.isDeletedCaseList
import io.github.taetae98coding.diary.core.testing.web.localWeb
import io.github.taetae98coding.diary.core.testing.web.web
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class WebMapperTest :
    FunSpec({
        test("domain to local") {
            isDeletedCaseList.forEach { isDeleted ->
                val domain = fixtureMonkey.web(isDeleted = isDeleted)

                domain.toLocal() shouldBe
                    WebLocalEntity(
                        id = domain.id,
                        detail = domain.detail.toLocal(),
                        isDeleted = isDeleted,
                        updatedAt = domain.updatedAt,
                        createdAt = domain.createdAt,
                    )
            }
        }

        test("local to domain") {
            isDeletedCaseList.forEach { isDeleted ->
                val local = fixtureMonkey.localWeb(isDeleted = isDeleted)

                local.toDomain() shouldBe
                    Web(
                        id = local.id,
                        detail = local.detail.toDomain(),
                        isDeleted = isDeleted,
                        updatedAt = local.updatedAt,
                        createdAt = local.createdAt,
                    )
            }
        }

        test("domain to local to domain") {
            isDeletedCaseList.forEach { isDeleted ->
                val domain = fixtureMonkey.web(isDeleted = isDeleted)

                domain.toLocal().toDomain() shouldBe domain
            }
        }

        test("local to domain to local") {
            isDeletedCaseList.forEach { isDeleted ->
                val local = fixtureMonkey.localWeb(isDeleted = isDeleted)

                local.toDomain().toLocal() shouldBe local
            }
        }
    })
