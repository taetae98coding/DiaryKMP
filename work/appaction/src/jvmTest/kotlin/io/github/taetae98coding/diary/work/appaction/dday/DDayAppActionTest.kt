package io.github.taetae98coding.diary.work.appaction.dday

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.model.dday.DDayDetail
import io.github.taetae98coding.diary.core.model.dday.DDayOverflowPolicy
import io.github.taetae98coding.diary.core.testing.dday.dday
import io.github.taetae98coding.diary.core.testing.dday.ddayDetail
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.domain.dday.DDayInput
import io.github.taetae98coding.diary.domain.dday.repeat.toRepeatSetOrNull
import io.github.taetae98coding.diary.domain.dday.usecase.AddDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.DeleteDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.FindDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.PageDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.RestoreDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.UpdateDDayUseCase
import io.github.taetae98coding.diary.domain.tag.EntityTagKind
import io.github.taetae98coding.diary.domain.tag.usecase.AddEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.GetEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RemoveEntityTagUseCase
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.work.appaction.core.AppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.AppActionDone
import io.github.taetae98coding.diary.work.appaction.core.AppActionException
import io.github.taetae98coding.diary.work.appaction.core.AppActionId
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.tag.AppActionTag
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate

class DDayAppActionTest :
    FunSpec({
        test("목록은 오늘로 계산한 남은 날과 채운 기간을 담아 MCP와 같은 항목을 돌려준다") {
            runTest {
                val dday = Arb.dday().next().copy(detail = Arb.ddayDetail(baseDate = LocalDate(2026, 4, 29)).next())
                val pageDDayUseCase = mockk<PageDDayUseCase>()
                every { pageDDayUseCase(parameter = Unit) } returns flowOf(Result.success(PagingData.from(listOf(dday))))
                val action = ddayAppActionOf(pageDDayUseCase, clockAt(date = LocalDate(2026, 3, 20)))

                action.list(cursor = null) shouldBe
                    AppActionPage(
                        items = listOf(dday.expected(count = AppActionDDayCount(status = "upcoming", remainingDays = 40, passedDays = null, years = 0, months = 1))),
                        nextCursor = null,
                    )
            }
        }

        test("상세는 연결된 태그와 함께 돌려주고, 당일이면 상태만 담고, 없는 디데이는 null을 돌려준다") {
            runTest {
                val dday = Arb.dday().next().copy(detail = Arb.ddayDetail(baseDate = LocalDate(2026, 3, 20)).next())
                val missingId = Arb.kotlinUuid().next()
                val tagList = Arb.list(Arb.tag(), 0..3).next()
                val findDDayUseCase = mockk<FindDDayUseCase>()
                val getEntityTagUseCase = mockk<GetEntityTagUseCase>()
                every { findDDayUseCase(parameter = dday.id) } returns flowOf(Result.success(dday))
                every { findDDayUseCase(parameter = missingId) } returns flowOf(Result.success(null))
                every { getEntityTagUseCase(parameter = GetEntityTagUseCase.Parameter(kind = EntityTagKind.DDAY, entityId = dday.id)) } returns flowOf(Result.success(tagList))
                val action = ddayAppActionOf(findDDayUseCase, getEntityTagUseCase, clockAt(date = LocalDate(2026, 3, 20)))

                action.get(ddayId = dday.id.toString()) shouldBe
                    AppActionDDayDetail(
                        dday = dday.expected(count = AppActionDDayCount(status = "today", remainingDays = null, passedDays = null, years = null, months = null)),
                        tags =
                            tagList.map { tag ->
                                AppActionTag(
                                    id = tag.id.toString(),
                                    emoji = tag.detail.emoji,
                                    title = tag.detail.title,
                                    description = tag.detail.description,
                                    color = tag.detail.color.expectedHex(),
                                    isFinished = tag.isFinished,
                                    createdAt = tag.createdAt.toString(),
                                    updatedAt = tag.updatedAt.toString(),
                                )
                            },
                    )
                action.get(ddayId = missingId.toString()) shouldBe null
            }
        }

        test("결과의 반복 간격은 일, 개월, 년 순서로 12의 배수인 개월을 년으로 적는다") {
            runTest {
                val repeats = setOf(DateTimeUnit.MonthBased(months = 24), DateTimeUnit.DayBased(days = 100), DateTimeUnit.MonthBased(months = 18))
                val dday = Arb.dday().next().copy(detail = Arb.ddayDetail(baseDate = LocalDate(2026, 1, 1), repeats = repeats).next())
                val findDDayUseCase = mockk<FindDDayUseCase>()
                val getEntityTagUseCase = mockk<GetEntityTagUseCase>()
                every { findDDayUseCase(parameter = dday.id) } returns flowOf(Result.success(dday))
                every { getEntityTagUseCase(parameter = any()) } returns flowOf(Result.success(emptyList()))
                val action = ddayAppActionOf(findDDayUseCase, getEntityTagUseCase, clockAt(date = LocalDate(2026, 3, 20)))

                action.get(ddayId = dday.id.toString())?.dday?.repeats shouldBe listOf("P100D", "P18M", "P2Y")
            }
        }

        test("추가는 담지 않은 값을 처음 값으로 채우고 담은 값과 태그를 그대로 넘겨 id를 돌려준다") {
            runTest {
                val title = Arb.nonBlankString().next()
                val tagId = Arb.kotlinUuid().next()
                val addedId = Arb.kotlinUuid().next()
                val parameterList = mutableListOf<AddDDayUseCase.Parameter>()
                val addDDayUseCase = mockk<AddDDayUseCase>()
                coEvery { addDDayUseCase(parameter = capture(parameterList)) } returns Result.success(addedId)
                val action = ddayAppActionOf(addDDayUseCase, clockAt(date = LocalDate(2026, 3, 20)))

                action.add(
                    title = title,
                    description = null,
                    color = null,
                    baseDate = null,
                    countsBaseDay = null,
                    repeats = emptyList(),
                    endDate = null,
                    overflowPolicy = null,
                    tagIds = emptyList(),
                ) shouldBe AppActionId(id = addedId.toString())
                action.add(
                    title = title,
                    description = "설명",
                    color = "#4285F4",
                    baseDate = "2026-01-31",
                    countsBaseDay = false,
                    repeats = listOf("P1Y6M", "P18M", "P100D"),
                    endDate = "2026-12-31",
                    overflowPolicy = "skip",
                    tagIds = listOf(tagId.toString()),
                )

                val (defaultParameter, filledParameter) = parameterList
                defaultParameter.tagIdSet shouldBe emptySet()
                defaultParameter.input.recordedDetail() shouldBe
                    DDayDetail(
                        title = title,
                        description = "",
                        color = defaultParameter.input.color,
                        baseDate = LocalDate(2026, 3, 20),
                        countsBaseDay = true,
                        repeats = emptySet(),
                        endDate = null,
                        overflowPolicy = DDayOverflowPolicy.CLAMP_TO_MONTH_END,
                    )
                filledParameter.tagIdSet shouldBe setOf(tagId)
                filledParameter.input.recordedDetail() shouldBe
                    DDayDetail(
                        title = title,
                        description = "설명",
                        color = 0xFF4285F4.toInt().toLong(),
                        baseDate = LocalDate(2026, 1, 31),
                        countsBaseDay = false,
                        repeats = setOf(DateTimeUnit.MonthBased(months = 18), DateTimeUnit.DayBased(days = 100)),
                        endDate = LocalDate(2026, 12, 31),
                        overflowPolicy = DDayOverflowPolicy.SKIP,
                    )
            }
        }

        test("받을 수 없는 값은 앱 기능을 실행하지 않고 MCP와 같은 메시지로 알린다") {
            runTest {
                val addDDayUseCase = mockk<AddDDayUseCase>()
                val action = ddayAppActionOf(addDDayUseCase, clockAt(date = LocalDate(2026, 3, 20)))
                val title = Arb.nonBlankString().next()

                listOf("P1M10D", "P0D", "P0Y0M", "P2W", "PT24H", "100", "P", "p1d", "-P1D", "P99999999999D").forEach { repeat ->
                    shouldThrow<AppActionException.InvalidArgument> {
                        action.add(title = title, description = null, color = null, baseDate = null, countsBaseDay = null, repeats = listOf(repeat), endDate = null, overflowPolicy = null, tagIds = emptyList())
                    }.message shouldBe "Each 'repeats' item must be an ISO 8601 period of days only or of years and months only, 1 or greater, like P100D, P3M or P1Y."
                }
                shouldThrow<AppActionException.InvalidArgument> {
                    action.add(title = title, description = null, color = null, baseDate = null, countsBaseDay = null, repeats = emptyList(), endDate = null, overflowPolicy = "clamp", tagIds = emptyList())
                }.message shouldBe "'overflowPolicy' must be one of clamp_to_month_end, skip, next_month_first_day."
                shouldThrow<AppActionException.InvalidArgument> {
                    action.add(title = title, description = null, color = null, baseDate = "2026-5-1", countsBaseDay = null, repeats = emptyList(), endDate = null, overflowPolicy = null, tagIds = emptyList())
                }.message shouldBe "'baseDate' must be a date like 2026-01-31."
                shouldThrow<AppActionException.InvalidArgument> {
                    action.add(title = title, description = null, color = null, baseDate = "2026-05-01", countsBaseDay = null, repeats = emptyList(), endDate = "2026-04-30", overflowPolicy = null, tagIds = emptyList())
                }.message shouldBe "'endDate' must not be before 'baseDate'."
                coVerify(exactly = 0) { addDDayUseCase(parameter = any()) }
            }
        }

        test("수정은 담은 값만 바꾸고, 종료일 지우기가 종료일보다 앞서며, 반복 간격을 담지 않으면 그대로 둔다") {
            runTest {
                val stored =
                    Arb.dday().next().let { dday ->
                        dday.copy(
                            detail =
                                Arb
                                    .ddayDetail(
                                        baseDate = LocalDate(2026, 1, 1),
                                        countsBaseDay = true,
                                        repeats = setOf(DateTimeUnit.DayBased(days = 100)),
                                        endDate = LocalDate(2026, 12, 31),
                                    ).next(),
                        )
                    }
                val changedCount = Arb.int(0..1).next()
                val parameterList = mutableListOf<UpdateDDayUseCase.Parameter>()
                val findDDayUseCase = mockk<FindDDayUseCase>()
                val updateDDayUseCase = mockk<UpdateDDayUseCase>()
                every { findDDayUseCase(parameter = stored.id) } returns flowOf(Result.success(stored))
                coEvery { updateDDayUseCase(parameter = capture(parameterList)) } returns Result.success(changedCount)
                val action = ddayAppActionOf(findDDayUseCase, updateDDayUseCase, clockAt(date = LocalDate(2026, 3, 20)))

                action.update(
                    ddayId = stored.id.toString(),
                    title = null,
                    description = null,
                    color = "#000000",
                    baseDate = null,
                    countsBaseDay = false,
                    repeats = null,
                    endDate = "2027-01-01",
                    clearEndDate = true,
                    overflowPolicy = "next_month_first_day",
                ) shouldBe AppActionChangedCount(changedCount = changedCount)

                parameterList.single().id shouldBe stored.id
                parameterList.single().input.recordedDetail() shouldBe
                    stored.detail.copy(color = 0xFF000000.toInt().toLong(), countsBaseDay = false, endDate = null, overflowPolicy = DDayOverflowPolicy.NEXT_MONTH_FIRST_DAY)
            }
        }

        test("수정할 디데이가 없으면 바뀐 수 0을, 종료일이 기준일보다 앞이 되면 실패를 알리고 앱 기능을 실행하지 않는다") {
            runTest {
                val stored = Arb.dday().next().copy(detail = Arb.ddayDetail(baseDate = LocalDate(2026, 1, 1), endDate = LocalDate(2026, 12, 31)).next())
                val missingId = Arb.kotlinUuid().next()
                val findDDayUseCase = mockk<FindDDayUseCase>()
                val updateDDayUseCase = mockk<UpdateDDayUseCase>()
                every { findDDayUseCase(parameter = stored.id) } returns flowOf(Result.success(stored))
                every { findDDayUseCase(parameter = missingId) } returns flowOf(Result.success(null))
                val action = ddayAppActionOf(findDDayUseCase, updateDDayUseCase, clockAt(date = LocalDate(2026, 3, 20)))

                action.update(
                    ddayId = missingId.toString(),
                    title = Arb.nonBlankString().next(),
                    description = null,
                    color = null,
                    baseDate = null,
                    countsBaseDay = null,
                    repeats = null,
                    endDate = null,
                    clearEndDate = null,
                    overflowPolicy = null,
                ) shouldBe AppActionChangedCount(changedCount = 0)
                shouldThrow<AppActionException.InvalidArgument> {
                    action.update(
                        ddayId = stored.id.toString(),
                        title = null,
                        description = null,
                        color = null,
                        baseDate = "2027-01-01",
                        countsBaseDay = null,
                        repeats = null,
                        endDate = null,
                        clearEndDate = null,
                        overflowPolicy = null,
                    )
                }.message shouldBe "'endDate' must not be before 'baseDate'."
                coVerify(exactly = 0) { updateDDayUseCase(parameter = any()) }
            }
        }

        test("삭제·되돌리기는 바뀐 수를, 태그 연결·해제는 디데이 종류로 넘겨 완료를 돌려준다") {
            runTest {
                val ddayId = Arb.kotlinUuid().next()
                val tagId = Arb.kotlinUuid().next()
                val changedCount = Arb.int(0..1).next()
                val deleteDDayUseCase = mockk<DeleteDDayUseCase>()
                val restoreDDayUseCase = mockk<RestoreDDayUseCase>()
                val addEntityTagUseCase = mockk<AddEntityTagUseCase>()
                val removeEntityTagUseCase = mockk<RemoveEntityTagUseCase>()
                coEvery { deleteDDayUseCase(parameter = ddayId) } returns Result.success(changedCount)
                coEvery { restoreDDayUseCase(parameter = ddayId) } returns Result.success(changedCount)
                coEvery { addEntityTagUseCase(parameter = any()) } returns Result.success(Unit)
                coEvery { removeEntityTagUseCase(parameter = any()) } returns Result.success(Unit)
                val action = ddayAppActionOf(deleteDDayUseCase, restoreDDayUseCase, addEntityTagUseCase, removeEntityTagUseCase, clockAt(date = LocalDate(2026, 3, 20)))

                action.delete(ddayId = ddayId.toString()) shouldBe AppActionChangedCount(changedCount = changedCount)
                action.restore(ddayId = ddayId.toString()) shouldBe AppActionChangedCount(changedCount = changedCount)
                action.tagAdd(ddayId = ddayId.toString(), tagId = tagId.toString()) shouldBe AppActionDone(done = true)
                action.tagRemove(ddayId = ddayId.toString(), tagId = tagId.toString()) shouldBe AppActionDone(done = true)

                coVerify(exactly = 1) { addEntityTagUseCase(parameter = AddEntityTagUseCase.Parameter(kind = EntityTagKind.DDAY, entityId = ddayId, tagId = tagId)) }
                coVerify(exactly = 1) { removeEntityTagUseCase(parameter = RemoveEntityTagUseCase.Parameter(kind = EntityTagKind.DDAY, entityId = ddayId, tagId = tagId)) }
                shouldThrow<AppActionException.InvalidArgument> { action.tagAdd(ddayId = ddayId.toString(), tagId = "not-id") }.message shouldBe "'tagId' must be an id."
            }
        }

        test("바꾸는 앱 기능이 실패하면 MCP와 같은 메시지로 실패를 알린다") {
            runTest {
                val message = Arb.nonBlankString().next()
                val addDDayUseCase = mockk<AddDDayUseCase>()
                coEvery { addDDayUseCase(parameter = any()) } returns Result.failure(IllegalStateException(message))
                val action = ddayAppActionOf(addDDayUseCase, clockAt(date = LocalDate(2026, 3, 20)))

                shouldThrow<AppActionException.Failure> {
                    action.add(title = " ", description = null, color = null, baseDate = null, countsBaseDay = null, repeats = emptyList(), endDate = null, overflowPolicy = null, tagIds = emptyList())
                }.message shouldBe "IllegalStateException: $message"
            }
        }

        test("달력에 없는 날짜 처리는 말일로 당김, 표시하지 않음, 다음 달 1일로 미룸 순서로 알린다") {
            appActionDDayOverflowPolicyList shouldBe listOf(DDayOverflowPolicy.CLAMP_TO_MONTH_END, DDayOverflowPolicy.SKIP, DDayOverflowPolicy.NEXT_MONTH_FIRST_DAY)
        }

        test("달력에 없는 날짜 처리 목록은 모든 처리를 한 번씩 담는다") {
            appActionDDayOverflowPolicyList shouldContainExactlyInAnyOrder DDayOverflowPolicy.entries
        }
    })

private const val RGB = 0xFFFFFFL

private fun Long.expectedHex(): String = "#%06X".format(this and RGB)

private fun DDay.expected(count: AppActionDDayCount): AppActionDDay =
    AppActionDDay(
        id = id.toString(),
        title = detail.title,
        description = detail.description,
        color = detail.color.expectedHex(),
        baseDate = detail.baseDate.toString(),
        countsBaseDay = detail.countsBaseDay,
        repeats = emptyList(),
        endDate = detail.endDate?.toString(),
        overflowPolicy =
            when (detail.overflowPolicy) {
                DDayOverflowPolicy.CLAMP_TO_MONTH_END -> "clamp_to_month_end"
                DDayOverflowPolicy.SKIP -> "skip"
                DDayOverflowPolicy.NEXT_MONTH_FIRST_DAY -> "next_month_first_day"
            },
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
        count = count,
    )

private fun DDayInput.recordedDetail(): DDayDetail =
    DDayDetail(
        title = title,
        description = description,
        color = color,
        baseDate = baseDate,
        countsBaseDay = countsBaseDay,
        repeats = checkNotNull(repeatList.toRepeatSetOrNull()),
        endDate = endDate,
        overflowPolicy = overflowPolicy,
    )
