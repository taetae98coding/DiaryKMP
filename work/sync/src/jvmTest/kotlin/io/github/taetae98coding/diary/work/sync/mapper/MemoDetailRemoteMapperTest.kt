package io.github.taetae98coding.diary.work.sync.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoDetailLocalEntity
import io.github.taetae98coding.diary.core.network.api.memo.entity.MemoDetailRemoteEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDateTime

class MemoDetailRemoteMapperTest :
    FunSpec({
        test("local to remote") {
            localCases().forEach { local ->
                local.toRemote() shouldBe
                    MemoDetailRemoteEntity(
                        title = local.title,
                        description = local.description,
                        color = local.color,
                        isAllDay = local.isAllDay,
                        start = local.start,
                        endInclusive = local.endInclusive,
                    )
            }
        }

        test("remote to local") {
            remoteCases().forEach { remote ->
                remote.toLocal() shouldBe
                    MemoDetailLocalEntity(
                        title = remote.title,
                        description = remote.description,
                        color = remote.color,
                        isAllDay = remote.isAllDay,
                        start = remote.start,
                        endInclusive = remote.endInclusive,
                    )
            }
        }

        test("local to remote to local") {
            localCases().forEach { local ->
                local.toRemote().toLocal() shouldBe local
            }
        }
    })

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

private fun localCases(): List<MemoDetailLocalEntity> {
    val local =
        fixtureMonkey
            .giveMeOne<MemoDetailLocalEntity>()
            .copy(
                start = fixtureMonkey.giveMeOne<LocalDateTime>(),
                endInclusive = fixtureMonkey.giveMeOne<LocalDateTime>(),
            )

    return listOf(
        local.copy(isAllDay = true),
        local.copy(isAllDay = false),
        local.copy(isAllDay = null, start = null, endInclusive = null),
    )
}

private fun remoteCases(): List<MemoDetailRemoteEntity> {
    val remote =
        fixtureMonkey
            .giveMeOne<MemoDetailRemoteEntity>()
            .copy(
                start = fixtureMonkey.giveMeOne<LocalDateTime>(),
                endInclusive = fixtureMonkey.giveMeOne<LocalDateTime>(),
            )

    return listOf(
        remote.copy(isAllDay = true),
        remote.copy(isAllDay = false),
        remote.copy(isAllDay = null, start = null, endInclusive = null),
    )
}
