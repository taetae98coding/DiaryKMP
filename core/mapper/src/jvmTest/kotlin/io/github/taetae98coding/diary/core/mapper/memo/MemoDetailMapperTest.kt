package io.github.taetae98coding.diary.core.mapper.memo

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoDetailLocalEntity
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.core.network.api.memo.entity.MemoDetailRemoteEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

class MemoDetailMapperTest :
    FunSpec({
        test("domain to local") {
            val domain = fixtureMonkey.giveMeOne<MemoDetail>()
            val allDay = fixtureMonkey.giveMeOne<MemoDateTime.AllDay>()
            val dateTime = fixtureMonkey.giveMeOne<MemoDateTime.DateTime>()

            listOf(
                domain.copy(dateTime = allDay) to
                    domain.expectedLocal(
                        isAllDay = true,
                        start = LocalDateTime(date = allDay.dateRange.start, time = Midnight),
                        endInclusive = LocalDateTime(date = allDay.dateRange.endInclusive, time = Midnight),
                    ),
                domain.copy(dateTime = dateTime) to
                    domain.expectedLocal(
                        isAllDay = false,
                        start = dateTime.start,
                        endInclusive = dateTime.endInclusive,
                    ),
                domain.copy(dateTime = null) to
                    domain.expectedLocal(
                        isAllDay = null,
                        start = null,
                        endInclusive = null,
                    ),
            ).forEach { (case, expected) ->
                case.toLocal() shouldBe expected
            }
        }

        test("local to domain") {
            val start = fixtureMonkey.giveMeOne<LocalDateTime>()
            val endInclusive = fixtureMonkey.giveMeOne<LocalDateTime>()
            val local =
                fixtureMonkey
                    .giveMeOne<MemoDetailLocalEntity>()
                    .copy(start = start, endInclusive = endInclusive)

            listOf(
                local.copy(isAllDay = true) to
                    MemoDateTime.AllDay(dateRange = start.date..endInclusive.date),
                local.copy(isAllDay = false) to
                    MemoDateTime.DateTime(start = start, endInclusive = endInclusive),
                local.copy(isAllDay = null) to null,
                local.copy(isAllDay = true, start = null) to null,
                local.copy(isAllDay = true, endInclusive = null) to null,
                local.copy(isAllDay = null, start = null, endInclusive = null) to null,
            ).forEach { (case, expected) ->
                case.toDomain() shouldBe case.expectedDomain(dateTime = expected)
            }
        }

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

        test("domain to local to domain") {
            val domain = fixtureMonkey.giveMeOne<MemoDetail>()

            listOf(
                fixtureMonkey.giveMeOne<MemoDateTime.AllDay>(),
                fixtureMonkey.giveMeOne<MemoDateTime.DateTime>(),
                null,
            ).forEach { dateTime ->
                val expected = domain.copy(dateTime = dateTime)

                expected.toLocal().toDomain() shouldBe expected
            }
        }

        test("local to remote to local") {
            localCases().forEach { local ->
                local.toRemote().toLocal() shouldBe local
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private val Midnight = LocalTime(hour = 0, minute = 0)

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

        private fun MemoDetail.expectedLocal(
            isAllDay: Boolean?,
            start: LocalDateTime?,
            endInclusive: LocalDateTime?,
        ): MemoDetailLocalEntity =
            MemoDetailLocalEntity(
                title = title,
                description = description,
                color = color,
                isAllDay = isAllDay,
                start = start,
                endInclusive = endInclusive,
            )

        private fun MemoDetailLocalEntity.expectedDomain(dateTime: MemoDateTime?): MemoDetail =
            MemoDetail(
                title = title,
                description = description,
                color = color,
                dateTime = dateTime,
            )
    }
}
