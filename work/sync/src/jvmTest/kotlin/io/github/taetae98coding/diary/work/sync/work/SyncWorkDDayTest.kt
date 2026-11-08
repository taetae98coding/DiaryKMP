package io.github.taetae98coding.diary.work.sync.work

import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import io.github.taetae98coding.diary.core.network.api.dday.entity.DDayRemoteEntity
import io.github.taetae98coding.diary.core.network.api.dday.entity.DDayRepeatRemoteEntity
import io.github.taetae98coding.diary.core.testing.dday.localDDay
import io.github.taetae98coding.diary.work.sync.mapper.toRemote
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import java.util.TimeZone

class SyncWorkDDayTest :
    FunSpec({
        suspend fun pushedDDay(stored: DDayLocalEntity): DDayRemoteEntity {
            val context = context(ddayList = listOf(stored))
            val requestList = mutableListOf<List<DDayRemoteEntity>>()
            coEvery { context.ddayRemoteDataSource.push(any()) } answers { requestList += firstArg<List<DDayRemoteEntity>>() }

            context.subject.doWork()

            return requestList.single().single()
        }

        suspend fun pulledDDay(response: DDayRemoteEntity): DDayLocalEntity {
            val context = context()
            val pull = ddayPulls(usnList = listOf(1L)).single().copy(dday = response)
            coEvery { context.ddayRemoteDataSource.pull(usn = 0L) } returns listOf(pull)
            coEvery { context.ddayRemoteDataSource.pull(usn = 1L) } returns emptyList()
            val savedList = mutableListOf<List<DDayLocalEntity>>()
            coEvery { context.accountDDaySyncTransaction.upsert(any(), any(), any()) } answers { savedList += secondArg<List<DDayLocalEntity>>() }

            context.subject.doWork()

            return savedList.single().single()
        }

        listOf(LocalDate(2027, 3, 1), null).forEach { endDate ->
            test("TC-4db2e680-d3fd-4b84-aa40-5cec0f9a2c53 종료일이 ${endDate ?: "없는"} 디데이 업로드에는 전체 상태와 연·월·일 날짜가 담긴다") {
                withTimeZone("Asia/Seoul") {
                    val stored =
                        Arb
                            .localDDay(
                                baseDate = LocalDate(2026, 3, 1),
                                repeats = setOf(DateTimeUnit.DayBased(days = 100)),
                                endDate = endDate,
                                isDeleted = Arb.boolean().next(),
                            ).next()

                    val pushed = pushedDDay(stored = stored)

                    pushed shouldBe stored.toRemote()
                    pushed.detail.title shouldBe stored.detail.title
                    pushed.detail.description shouldBe stored.detail.description
                    pushed.detail.color shouldBe stored.detail.color
                    pushed.detail.countsBaseDay shouldBe stored.detail.countsBaseDay
                    pushed.detail.overflowPolicy shouldBe stored.detail.overflowPolicy.toRemote()
                    pushed.isDeleted shouldBe stored.isDeleted
                    pushed.detail.baseDate shouldBe LocalDate(2026, 3, 1)
                    pushed.detail.endDate shouldBe endDate
                }
            }
        }

        listOf(
            setOf(DateTimeUnit.DayBased(days = 100), DateTimeUnit.MonthBased(months = 24)) to
                listOf(DDayRepeatRemoteEntity.Days(days = 100), DDayRepeatRemoteEntity.Months(months = 24)),
            emptySet<DateTimeUnit.DateBased>() to emptyList(),
        ).forEach { (repeats, expected) ->
            test("TC-a17f601a-80cd-4c92-8809-966700ddc88d 반복 간격 ${repeats.size}개 디데이 업로드에는 간격마다 일 수나 개월 수 하나만 담긴다") {
                val stored = Arb.localDDay(baseDate = LocalDate(2026, 3, 1), repeats = repeats).next()

                pushedDDay(stored = stored).detail.repeats shouldContainExactlyInAnyOrder expected
            }
        }

        listOf(
            listOf(DDayRepeatRemoteEntity.Months(months = 12), DDayRepeatRemoteEntity.Days(days = 1000)) to
                setOf(DateTimeUnit.MonthBased(months = 12), DateTimeUnit.DayBased(days = 1000)),
            emptyList<DDayRepeatRemoteEntity>() to emptySet(),
        ).forEach { (responseRepeats, expected) ->
            test("TC-1940bbd8-f6e9-4cd9-8845-d0c028854f0e 응답의 반복 간격 ${responseRepeats.size}개가 기기의 반복 간격이 된다") {
                val stored = Arb.localDDay(baseDate = LocalDate(2026, 3, 1), repeats = setOf(DateTimeUnit.DayBased(days = 100), DateTimeUnit.MonthBased(months = 12))).next()
                val response = stored.toRemote().let { remote -> remote.copy(detail = remote.detail.copy(repeats = responseRepeats)) }

                pulledDDay(response = response).detail.repeats shouldBe expected
            }
        }

        test("TC-d3c9e994-62b9-432e-8218-9b2570678f20 종료일이 비어 있는 디데이를 내려받으면 기기의 디데이는 종료일이 없어진다") {
            val stored = Arb.localDDay(baseDate = LocalDate(2026, 3, 1), endDate = LocalDate(2027, 3, 1)).next()
            val response = stored.toRemote().let { remote -> remote.copy(detail = remote.detail.copy(endDate = null)) }

            pulledDDay(response = response).detail.endDate.shouldBeNull()
        }

        listOf("UTC", "Asia/Seoul", "Pacific/Honolulu").forEach { timeZoneId ->
            test("TC-d7fa68b7-e666-44f9-b23a-87aca127608c $timeZoneId 시간대에서도 내려받은 기준일과 종료일이 같은 연·월·일로 저장된다") {
                withTimeZone(timeZoneId) {
                    val response =
                        Arb
                            .localDDay(baseDate = LocalDate(2026, 3, 1), endDate = LocalDate(2026, 12, 31))
                            .next()
                            .toRemote()

                    val saved = pulledDDay(response = response)

                    saved.detail.baseDate shouldBe LocalDate(2026, 3, 1)
                    saved.detail.endDate shouldBe LocalDate(2026, 12, 31)
                }
            }
        }
    })

private suspend fun withTimeZone(
    timeZoneId: String,
    block: suspend () -> Unit,
) {
    val defaultTimeZone = TimeZone.getDefault()
    try {
        TimeZone.setDefault(TimeZone.getTimeZone(timeZoneId))
        block()
    } finally {
        TimeZone.setDefault(defaultTimeZone)
    }
}
