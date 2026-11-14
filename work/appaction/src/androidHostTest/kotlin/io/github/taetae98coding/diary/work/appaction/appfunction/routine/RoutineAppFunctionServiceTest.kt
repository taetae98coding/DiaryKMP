package io.github.taetae98coding.diary.work.appaction.appfunction.routine

import android.content.Context
import androidx.appfunctions.AppFunctionAppUnknownException
import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.appfunctions.ExecuteAppFunctionResponse
import androidx.appfunctions.testing.AppFunctionTestRule
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionChangedCount
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionDone
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionId
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionServiceTester
import io.github.taetae98coding.diary.work.appaction.appfunction.core.expectedTag
import io.github.taetae98coding.diary.work.appaction.appfunction.core.returnValueAs
import io.github.taetae98coding.diary.work.appaction.appfunction.search.AppFunctionSearchRoutinePage
import io.github.taetae98coding.diary.work.appaction.core.AppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.AppActionDone
import io.github.taetae98coding.diary.work.appaction.core.AppActionException
import io.github.taetae98coding.diary.work.appaction.core.AppActionId
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.core.appActionColor
import io.github.taetae98coding.diary.work.appaction.routine.AppActionRoutine
import io.github.taetae98coding.diary.work.appaction.routine.AppActionRoutineCycleValue
import io.github.taetae98coding.diary.work.appaction.routine.AppActionRoutineDetail
import io.github.taetae98coding.diary.work.appaction.routine.AppActionRoutineProgress
import io.github.taetae98coding.diary.work.appaction.routine.AppActionRoutineRecord
import io.github.taetae98coding.diary.work.appaction.routine.RoutineAppAction
import io.github.taetae98coding.diary.work.appaction.routine.RoutineListAppAction
import io.github.taetae98coding.diary.work.appaction.routine.RoutineRecordAppAction
import io.github.taetae98coding.diary.work.appaction.search.SearchAppAction
import io.github.taetae98coding.diary.work.appaction.tag.AppActionEntityTagKind
import io.github.taetae98coding.diary.work.appaction.tag.EntityTagAppAction
import io.github.taetae98coding.diary.work.appaction.tag.TagItemAppAction
import io.github.taetae98coding.diary.work.appaction.tag.appActionTag
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RoutineAppFunctionServiceTest {
    private val context: Context = RuntimeEnvironment.getApplication()

    @get:Rule
    val appFunctionTestRule = AppFunctionTestRule(context)

    private val routineAppAction = mockk<RoutineAppAction>()
    private val routineListAppAction = mockk<RoutineListAppAction>()
    private val routineRecordAppAction = mockk<RoutineRecordAppAction>()
    private val entityTagAppAction = mockk<EntityTagAppAction>()
    private val searchAppAction = mockk<SearchAppAction>()
    private val tagItemAppAction = mockk<TagItemAppAction>()

    private lateinit var tester: AppFunctionServiceTester

    @Before
    fun setUp() {
        startKoin {
            modules(
                module {
                    single { routineAppAction }
                    single { routineListAppAction }
                    single { routineRecordAppAction }
                    single { entityTagAppAction }
                    single { searchAppAction }
                    single { tagItemAppAction }
                },
            )
        }
        tester = AppFunctionServiceTester(context = context, appFunctionManager = appFunctionTestRule.getAppFunctionManager())
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `루틴 동작을 모두 제공한다`() =
        runBlocking<Unit> {
            tester.functionNameList().filter { name -> name.startsWith("routine") } shouldContainExactlyInAnyOrder
                listOf(
                    "routineList",
                    "routineFinishedList",
                    ROUTINE_GET,
                    ROUTINE_ADD,
                    ROUTINE_UPDATE,
                    "routineFinish",
                    "routineRestart",
                    "routineDelete",
                    ROUTINE_RESTORE,
                    ROUTINE_TAG_ADD,
                    ROUTINE_TAG_REMOVE,
                    "routineRecordList",
                    ROUTINE_RECORD_ADD,
                    "routineRecordRemove",
                )
            tester.functionNameList() shouldContainAll listOf("searchRoutine", "tagRoutineList", "tagRoutineFinishedList")
        }

    @Test
    fun `TC-68c32f9f-68c5-4b5c-8ac5-74e6e6bff138 루틴 목록, 검색과 태그의 루틴은 진행 상태, 합계와 달성률을 같은 값으로 돌려준다`() =
        runBlocking<Unit> {
            val inProgress = appActionRoutine(progress = AppActionRoutineProgress(status = "in_progress", cycleAmount = Arb.long(0L..9_999_999L).next(), achievementRate = Arb.int(0..100).next()))
            val notStarted = appActionRoutine(progress = AppActionRoutineProgress(status = "not_started", cycleAmount = null, achievementRate = null))
            val cursor = Arb.int(1..1000).next().toString()
            val query = Arb.string().next()
            val tagId = Arb.kotlinUuid().next().toString()
            val page = AppActionPage(items = listOf(inProgress, notStarted), nextCursor = null)
            coEvery { routineListAppAction.list(cursor = any()) } returns page
            coEvery { searchAppAction.routine(query = any(), cursor = any()) } returns page
            coEvery { tagItemAppAction.routineList(tagId = any(), scope = any(), cursor = any()) } returns page

            val listResponse = tester.execute(name = "routineList") { setString("cursor", cursor) }
            val searchResponse = tester.execute(name = "searchRoutine") { setString("query", query) }
            val tagResponse = tester.execute(name = "tagRoutineList") { setString(TAG_ID, tagId) }

            val expectedList = listOf(inProgress.expected(), notStarted.expected())
            listResponse.returnValueAs<AppFunctionRoutinePage>() shouldBe AppFunctionRoutinePage(items = expectedList, nextCursor = null)
            searchResponse.returnValueAs<AppFunctionSearchRoutinePage>() shouldBe AppFunctionSearchRoutinePage(items = expectedList, nextCursor = null)
            tagResponse.returnValueAs<AppFunctionRoutinePage>() shouldBe AppFunctionRoutinePage(items = expectedList, nextCursor = null)
            coVerify(exactly = 1) { routineListAppAction.list(cursor = cursor) }
            coVerify(exactly = 1) { searchAppAction.routine(query = query, cursor = null) }
            coVerify(exactly = 1) { tagItemAppAction.routineList(tagId = tagId, scope = null, cursor = null) }
        }

    @Test
    fun `TC-55b4db15-225b-4ee1-862f-dcdfa753279a TC-e408a0ff-1873-49ab-8f51-d7ca57065fee 완료된 루틴 목록은 완료 여부를 같은 값으로 돌려준다`() =
        runBlocking<Unit> {
            val finished = appActionRoutine(progress = AppActionRoutineProgress(status = "in_progress", cycleAmount = null, achievementRate = null), isFinished = true)
            val cursor = Arb.int(1..1000).next().toString()
            val tagId = Arb.kotlinUuid().next().toString()
            val page = AppActionPage(items = listOf(finished), nextCursor = null)
            coEvery { routineListAppAction.finishedList(cursor = any()) } returns page
            coEvery { tagItemAppAction.routineFinishedList(tagId = any(), cursor = any()) } returns page

            val listResponse = tester.execute(name = "routineFinishedList") { setString("cursor", cursor) }
            val tagResponse = tester.execute(name = "tagRoutineFinishedList") { setString(TAG_ID, tagId) }

            val expected = AppFunctionRoutinePage(items = listOf(finished.expected()), nextCursor = null)
            expected.items.single().isFinished shouldBe true
            listResponse.returnValueAs<AppFunctionRoutinePage>() shouldBe expected
            tagResponse.returnValueAs<AppFunctionRoutinePage>() shouldBe expected
            coVerify(exactly = 1) { routineListAppAction.finishedList(cursor = cursor) }
            coVerify(exactly = 1) { tagItemAppAction.routineFinishedList(tagId = tagId, cursor = null) }
        }

    @Test
    fun `TC-9c80fb54-58d6-42ab-8d3f-15f6c838b996 루틴 완료와 다시 시작은 바뀐 수를 돌려준다`() =
        runBlocking<Unit> {
            val routineId = Arb.kotlinUuid().next().toString()
            val changedCount = Arb.int(0..1).next()
            coEvery { routineAppAction.finish(routineId = routineId) } returns AppActionChangedCount(changedCount = changedCount)
            coEvery { routineAppAction.restart(routineId = routineId) } returns AppActionChangedCount(changedCount = changedCount)

            tester.execute(name = "routineFinish") { setString(ROUTINE_ID, routineId) }.returnValueAs<AppFunctionChangedCount>() shouldBe AppFunctionChangedCount(changedCount = changedCount)
            tester.execute(name = "routineRestart") { setString(ROUTINE_ID, routineId) }.returnValueAs<AppFunctionChangedCount>() shouldBe AppFunctionChangedCount(changedCount = changedCount)
            coVerify(exactly = 1) { routineAppAction.finish(routineId = routineId) }
            coVerify(exactly = 1) { routineAppAction.restart(routineId = routineId) }
        }

    @Test
    fun `TC-4ca7faaf-f740-4b6a-9c4b-33ac9b779292 루틴 상세는 연결된 태그와 함께 돌려준다`() =
        runBlocking<Unit> {
            val routine = appActionRoutine(progress = AppActionRoutineProgress(status = "not_started", cycleAmount = null, achievementRate = null))
            val tag = Arb.appActionTag().next()
            coEvery { routineAppAction.get(routineId = routine.id) } returns AppActionRoutineDetail(routine = routine, tags = listOf(tag))

            val response = tester.execute(name = ROUTINE_GET) { setString(ROUTINE_ID, routine.id) }

            response.returnValueAs<AppFunctionRoutineDetail>() shouldBe AppFunctionRoutineDetail(routine = routine.expected(), tags = listOf(tag.expectedTag()))
        }

    @Test
    fun `TC-edf2f510-aac0-4a31-afdb-2f9368763906 루틴 추가는 받은 값과 태그를 공통 동작에 넘기고 id를 돌려준다`() =
        runBlocking<Unit> {
            val title = Arb.nonBlankString().next()
            val tagIdList = listOf(Arb.kotlinUuid().next().toString(), Arb.kotlinUuid().next().toString())
            val addedId = Arb.kotlinUuid().next().toString()
            coEvery {
                routineAppAction.add(
                    title = any(),
                    description = any(),
                    startDate = any(),
                    cycle = any(),
                    unit = any(),
                    goalAmount = any(),
                    amountPerRecord = any(),
                    tagIds = any(),
                )
            } returns AppActionId(id = addedId)

            val filledResponse =
                tester.execute(name = ROUTINE_ADD) {
                    setString("title", title)
                    setString("startDate", START_DATE)
                    setString("cycle", AppActionRoutineCycleValue.WEEKLY)
                    setString("unit", "ml")
                    setInt("goalAmount", 2000)
                    setInt("amountPerRecord", 250)
                    setStringList("tagIds", tagIdList)
                }
            val titleOnlyResponse = tester.execute(name = ROUTINE_ADD) { setString("title", title) }

            filledResponse.returnValueAs<AppFunctionId>() shouldBe AppFunctionId(id = addedId)
            titleOnlyResponse.returnValueAs<AppFunctionId>() shouldBe AppFunctionId(id = addedId)
            coVerify(exactly = 1) {
                routineAppAction.add(
                    title = title,
                    description = null,
                    startDate = START_DATE,
                    cycle = AppActionRoutineCycleValue.WEEKLY,
                    unit = "ml",
                    goalAmount = 2000,
                    amountPerRecord = 250,
                    tagIds = tagIdList,
                )
            }
            coVerify(exactly = 1) {
                routineAppAction.add(
                    title = title,
                    description = null,
                    startDate = null,
                    cycle = null,
                    unit = null,
                    goalAmount = null,
                    amountPerRecord = null,
                    tagIds = emptyList(),
                )
            }
        }

    @Test
    fun `TC-f3d2b737-4227-4136-a021-1cc493cd40ac 루틴 수정은 담지 않은 값을 비워 두고 목표량 지우기를 그대로 넘긴다`() =
        runBlocking<Unit> {
            val routineId = Arb.kotlinUuid().next().toString()
            val changedCount = Arb.int(0..1).next()
            coEvery {
                routineAppAction.update(
                    routineId = any(),
                    title = any(),
                    description = any(),
                    color = any(),
                    startDate = any(),
                    cycle = any(),
                    unit = any(),
                    goalAmount = any(),
                    clearGoalAmount = any(),
                    amountPerRecord = any(),
                )
            } returns AppActionChangedCount(changedCount = changedCount)

            val response =
                tester.execute(name = ROUTINE_UPDATE) {
                    setString(ROUTINE_ID, routineId)
                    setBoolean("clearGoalAmount", true)
                }

            response.returnValueAs<AppFunctionChangedCount>() shouldBe AppFunctionChangedCount(changedCount = changedCount)
            coVerify(exactly = 1) {
                routineAppAction.update(
                    routineId = routineId,
                    title = null,
                    description = null,
                    color = null,
                    startDate = null,
                    cycle = null,
                    unit = null,
                    goalAmount = null,
                    clearGoalAmount = true,
                    amountPerRecord = null,
                )
            }
        }

    @Test
    fun `TC-f3d2b737-4227-4136-a021-1cc493cd40ac 루틴 수정은 빈 글자로 담은 단위를 담지 않은 것과 구분해 넘긴다`() =
        runBlocking<Unit> {
            val routineId = Arb.kotlinUuid().next().toString()
            coEvery {
                routineAppAction.update(
                    routineId = any(),
                    title = any(),
                    description = any(),
                    color = any(),
                    startDate = any(),
                    cycle = any(),
                    unit = any(),
                    goalAmount = any(),
                    clearGoalAmount = any(),
                    amountPerRecord = any(),
                )
            } returns AppActionChangedCount(changedCount = 1)

            tester.execute(name = ROUTINE_UPDATE) {
                setString(ROUTINE_ID, routineId)
                setString("unit", "")
            }

            coVerify(exactly = 1) {
                routineAppAction.update(
                    routineId = routineId,
                    title = null,
                    description = null,
                    color = null,
                    startDate = null,
                    cycle = null,
                    unit = "",
                    goalAmount = null,
                    clearGoalAmount = null,
                    amountPerRecord = null,
                )
            }
        }

    @Test
    fun `TC-77c32c1d-a54a-480f-9654-066a1f73fa1f 삭제와 되돌리기는 바뀐 수를 돌려준다`() =
        runBlocking<Unit> {
            val routineId = Arb.kotlinUuid().next().toString()
            val changedCount = Arb.int(0..1).next()
            coEvery { routineAppAction.delete(routineId = routineId) } returns AppActionChangedCount(changedCount = changedCount)
            coEvery { routineAppAction.restore(routineId = routineId) } returns AppActionChangedCount(changedCount = changedCount)

            tester.execute(name = "routineDelete") { setString(ROUTINE_ID, routineId) }.returnValueAs<AppFunctionChangedCount>() shouldBe AppFunctionChangedCount(changedCount = changedCount)
            tester.execute(name = ROUTINE_RESTORE) { setString(ROUTINE_ID, routineId) }.returnValueAs<AppFunctionChangedCount>() shouldBe AppFunctionChangedCount(changedCount = changedCount)
        }

    @Test
    fun `TC-173de82e-308b-45d4-bc9b-14a00eb1c2fc 루틴 태그 연결과 해제는 완료를 돌려준다`() =
        runBlocking<Unit> {
            val routineId = Arb.kotlinUuid().next().toString()
            val tagId = Arb.kotlinUuid().next().toString()
            coEvery { entityTagAppAction.add(kind = AppActionEntityTagKind.ROUTINE, entityId = any(), tagId = any()) } returns AppActionDone(done = true)
            coEvery { entityTagAppAction.remove(kind = AppActionEntityTagKind.ROUTINE, entityId = any(), tagId = any()) } returns AppActionDone(done = true)

            val addResponse =
                tester.execute(name = ROUTINE_TAG_ADD) {
                    setString(ROUTINE_ID, routineId)
                    setString(TAG_ID, tagId)
                }
            val removeResponse =
                tester.execute(name = ROUTINE_TAG_REMOVE) {
                    setString(ROUTINE_ID, routineId)
                    setString(TAG_ID, tagId)
                }

            addResponse.returnValueAs<AppFunctionDone>() shouldBe AppFunctionDone(done = true)
            removeResponse.returnValueAs<AppFunctionDone>() shouldBe AppFunctionDone(done = true)
            coVerify(exactly = 1) { entityTagAppAction.add(kind = AppActionEntityTagKind.ROUTINE, entityId = routineId, tagId = tagId) }
            coVerify(exactly = 1) { entityTagAppAction.remove(kind = AppActionEntityTagKind.ROUTINE, entityId = routineId, tagId = tagId) }
        }

    @Test
    fun `TC-3ac1d111-84b2-4444-aeec-c2464eaf5912 루틴 기록 조회는 기록을 받은 순서대로 날짜와 양과 함께 돌려준다`() =
        runBlocking<Unit> {
            val routineId = Arb.kotlinUuid().next().toString()
            val recordList = listOf("2026-03-20" to 500, "2026-03-20" to 250, "2026-03-19" to 300).map { (date, amount) -> appActionRoutineRecord(routineId = routineId, date = date, amount = amount) }
            val nextCursor = Arb.int(1..1000).next().toString()
            coEvery { routineRecordAppAction.list(routineId = any(), cursor = any()) } returns AppActionPage(items = recordList, nextCursor = nextCursor)

            val response = tester.execute(name = "routineRecordList") { setString(ROUTINE_ID, routineId) }

            response.returnValueAs<AppFunctionRoutineRecordPage>() shouldBe AppFunctionRoutineRecordPage(items = recordList.map { record -> record.expected() }, nextCursor = nextCursor)
            coVerify(exactly = 1) { routineRecordAppAction.list(routineId = routineId, cursor = null) }
        }

    @Test
    fun `TC-ac3ef6dd-ea4d-4fd3-80f2-489adacf3019 루틴 기록 넣기는 담지 않은 날짜와 양을 비워 넘기고 넣은 수를 돌려준다`() =
        runBlocking<Unit> {
            val routineId = Arb.kotlinUuid().next().toString()
            coEvery { routineRecordAppAction.add(routineId = any(), date = any(), amount = any()) } returns AppActionChangedCount(changedCount = 1)

            val omittedResponse = tester.execute(name = ROUTINE_RECORD_ADD) { setString(ROUTINE_ID, routineId) }
            val filledResponse =
                tester.execute(name = ROUTINE_RECORD_ADD) {
                    setString(ROUTINE_ID, routineId)
                    setString("date", "2026-03-10")
                    setInt("amount", 500)
                }

            omittedResponse.returnValueAs<AppFunctionChangedCount>() shouldBe AppFunctionChangedCount(changedCount = 1)
            filledResponse.returnValueAs<AppFunctionChangedCount>() shouldBe AppFunctionChangedCount(changedCount = 1)
            coVerify(exactly = 1) { routineRecordAppAction.add(routineId = routineId, date = null, amount = null) }
            coVerify(exactly = 1) { routineRecordAppAction.add(routineId = routineId, date = "2026-03-10", amount = 500) }
        }

    @Test
    fun `TC-31b4b63f-09f6-48e4-9f73-6d2c095a695f 루틴 기록 빼기는 바뀐 수를 돌려준다`() =
        runBlocking<Unit> {
            val recordId = Arb.kotlinUuid().next().toString()
            coEvery { routineRecordAppAction.remove(recordId = recordId) } returns AppActionChangedCount(changedCount = 0)

            tester.execute(name = "routineRecordRemove") { setString("recordId", recordId) }.returnValueAs<AppFunctionChangedCount>() shouldBe AppFunctionChangedCount(changedCount = 0)
        }

    @Test
    fun `TC-78e9deb8-0990-4954-9903-4337415a5943 인자 오류는 잘못된 인자 실패로, 앱 기능 실패는 앱 실패로 같은 메시지를 알린다`() =
        runBlocking<Unit> {
            val invalidMessage = Arb.nonBlankString().next()
            val failureMessage = Arb.nonBlankString().next()
            coEvery { routineRecordAppAction.add(routineId = any(), date = any(), amount = any()) } throws AppActionException.InvalidArgument(message = invalidMessage)
            coEvery { routineAppAction.restore(routineId = any()) } throws AppActionException.Failure(message = failureMessage)

            val invalidError =
                tester
                    .execute(name = ROUTINE_RECORD_ADD) {
                        setString(ROUTINE_ID, Arb.kotlinUuid().next().toString())
                        setInt("amount", 0)
                    }.shouldBeInstanceOf<ExecuteAppFunctionResponse.Error>()
                    .error
            val failureError =
                tester
                    .execute(name = ROUTINE_RESTORE) { setString(ROUTINE_ID, Arb.kotlinUuid().next().toString()) }
                    .shouldBeInstanceOf<ExecuteAppFunctionResponse.Error>()
                    .error

            invalidError.shouldBeInstanceOf<AppFunctionInvalidArgumentException>()
            invalidError.errorMessage shouldBe invalidMessage
            failureError.shouldBeInstanceOf<AppFunctionAppUnknownException>()
            failureError.errorMessage shouldBe failureMessage
        }
}

private const val ROUTINE_ID = "routineId"
private const val TAG_ID = "tagId"
private const val ROUTINE_GET = "routineGet"
private const val ROUTINE_ADD = "routineAdd"
private const val ROUTINE_UPDATE = "routineUpdate"
private const val ROUTINE_TAG_ADD = "routineTagAdd"
private const val ROUTINE_TAG_REMOVE = "routineTagRemove"
private const val ROUTINE_RECORD_ADD = "routineRecordAdd"
private const val ROUTINE_RESTORE = "routineRestore"
private const val START_DATE = "2026-01-31"

private fun appActionRoutine(
    progress: AppActionRoutineProgress,
    isFinished: Boolean = false,
): AppActionRoutine =
    AppActionRoutine(
        id = Arb.kotlinUuid().next().toString(),
        title = Arb.string().next(),
        description = Arb.string().next(),
        color = Arb.appActionColor().next(),
        startDate = START_DATE,
        cycle = AppActionRoutineCycleValue.DAILY,
        unit = "ml",
        goalAmount = Arb.int(1..9_999_999).next(),
        amountPerRecord = Arb.int(1..9_999_999).next(),
        isFinished = isFinished,
        createdAt = Arb.string().next(),
        updatedAt = Arb.string().next(),
        progress = progress,
    )

private fun appActionRoutineRecord(
    routineId: String,
    date: String,
    amount: Int,
): AppActionRoutineRecord =
    AppActionRoutineRecord(
        id = Arb.kotlinUuid().next().toString(),
        routineId = routineId,
        date = date,
        amount = amount,
        createdAt = Arb.string().next(),
        updatedAt = Arb.string().next(),
    )

private fun AppActionRoutine.expected(): AppFunctionRoutine =
    AppFunctionRoutine(
        id = id,
        title = title,
        description = description,
        color = color,
        startDate = startDate,
        cycle = cycle,
        unit = unit,
        goalAmount = goalAmount,
        amountPerRecord = amountPerRecord,
        isFinished = isFinished,
        createdAt = createdAt,
        updatedAt = updatedAt,
        progress =
            AppFunctionRoutineProgress(
                status = progress.status,
                cycleAmount = progress.cycleAmount,
                achievementRate = progress.achievementRate,
            ),
    )

private fun AppActionRoutineRecord.expected(): AppFunctionRoutineRecord =
    AppFunctionRoutineRecord(
        id = id,
        routineId = routineId,
        date = date,
        amount = amount,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
