package io.github.taetae98coding.diary.data.sync.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.network.api.place.entity.PlaceRemoteEntity
import io.github.taetae98coding.diary.core.testing.isDeletedCaseList
import io.github.taetae98coding.diary.core.testing.place.localPlace
import io.github.taetae98coding.diary.core.testing.place.remotePlace
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class PlaceRemoteMapperTest :
    FunSpec({
        test("local to remote") {
            isDeletedCaseList.forEach { isDeleted ->
                val local = fixtureMonkey.localPlace(isDeleted = isDeleted)

                local.toRemote() shouldBe
                    PlaceRemoteEntity(
                        id = local.id,
                        detail = local.detail.toRemote(),
                        isDeleted = isDeleted,
                        updatedAt = local.updatedAt,
                        createdAt = local.createdAt,
                    )
            }
        }

        test("remote to local") {
            isDeletedCaseList.forEach { isDeleted ->
                val remote = fixtureMonkey.remotePlace(isDeleted = isDeleted)

                remote.toLocal() shouldBe
                    PlaceLocalEntity(
                        id = remote.id,
                        detail = remote.detail.toLocal(),
                        isDeleted = isDeleted,
                        updatedAt = remote.updatedAt,
                        createdAt = remote.createdAt,
                    )
            }
        }

        test("local to remote to local") {
            isDeletedCaseList.forEach { isDeleted ->
                val local = fixtureMonkey.localPlace(isDeleted = isDeleted)

                local.toRemote().toLocal() shouldBe local
            }
        }
    })
