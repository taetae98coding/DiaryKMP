package io.github.taetae98coding.diary.work.appaction.appfunction

import android.content.Context
import androidx.appfunctions.AppFunctionAppUnknownException
import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.appfunctions.ExecuteAppFunctionResponse
import androidx.appfunctions.testing.AppFunctionTestRule
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.localDateRange
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionChangedCount
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionId
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionServiceTester
import io.github.taetae98coding.diary.work.appaction.appfunction.core.expectedTag
import io.github.taetae98coding.diary.work.appaction.appfunction.core.returnValueAs
import io.github.taetae98coding.diary.work.appaction.appfunction.memo.AppFunctionMemo
import io.github.taetae98coding.diary.work.appaction.appfunction.memo.AppFunctionMemoDateTime
import io.github.taetae98coding.diary.work.appaction.appfunction.memo.AppFunctionMemoDetail
import io.github.taetae98coding.diary.work.appaction.appfunction.memo.AppFunctionMemoPage
import io.github.taetae98coding.diary.work.appaction.core.AppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.AppActionException
import io.github.taetae98coding.diary.work.appaction.core.AppActionId
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.core.AppActionSortValue
import io.github.taetae98coding.diary.work.appaction.core.appActionColor
import io.github.taetae98coding.diary.work.appaction.memo.AppActionMemo
import io.github.taetae98coding.diary.work.appaction.memo.AppActionMemoDateTime
import io.github.taetae98coding.diary.work.appaction.memo.AppActionMemoDetail
import io.github.taetae98coding.diary.work.appaction.memo.MemoAppAction
import io.github.taetae98coding.diary.work.appaction.memo.MemoListAppAction
import io.github.taetae98coding.diary.work.appaction.tag.appActionTag
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.int
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
class DiaryAppFunctionServiceTest {
    private val context: Context = RuntimeEnvironment.getApplication()

    @get:Rule
    val appFunctionTestRule = AppFunctionTestRule(context)

    private val memoAppAction = mockk<MemoAppAction>()
    private val memoListAppAction = mockk<MemoListAppAction>()

    private lateinit var tester: AppFunctionServiceTester

    @Before
    fun setUp() {
        startKoin {
            modules(
                module {
                    single { memoAppAction }
                    single { memoListAppAction }
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
    fun `메모 동작을 모두 제공한다`() =
        runBlocking<Unit> {
            tester.functionNameList().filter { name -> name.startsWith("memo") && memoLinkPrefixList.none { prefix -> name.startsWith(prefix) } } shouldContainExactlyInAnyOrder
                listOf("memoList", "memoFinishedList", MEMO_GET, "memoAdd", "memoUpdate", "memoCopy", "memoFinish", "memoRestart", "memoDelete", "memoRestore")
        }

    @Test
    fun `메모 목록은 공통 동작의 페이지를 같은 값으로 돌려준다`() =
        runBlocking<Unit> {
            val allDayRange = Arb.localDateRange().next()
            val memo =
                AppActionMemo(
                    id = Arb.kotlinUuid().next().toString(),
                    title = Arb.string().next(),
                    description = Arb.string().next(),
                    color = Arb.appActionColor().next(),
                    dateTime = AppActionMemoDateTime(allDay = true, start = allDayRange.start.toString(), endInclusive = allDayRange.endInclusive.toString()),
                    primaryTagId = null,
                    isFinished = Arb.boolean().next(),
                    createdAt = Arb.string().next(),
                    updatedAt = Arb.string().next(),
                )
            val nextCursor = Arb.int(1..1000).next().toString()
            coEvery { memoListAppAction.list(sort = any(), cursor = any()) } returns AppActionPage(items = listOf(memo), nextCursor = nextCursor)

            val response = tester.execute(name = "memoList") { setString("sort", AppActionSortValue.TITLE) }

            response.returnValueAs<AppFunctionMemoPage>() shouldBe
                AppFunctionMemoPage(
                    items =
                        listOf(
                            AppFunctionMemo(
                                id = memo.id,
                                title = memo.title,
                                description = memo.description,
                                color = memo.color,
                                dateTime = AppFunctionMemoDateTime(allDay = true, start = allDayRange.start.toString(), endInclusive = allDayRange.endInclusive.toString()),
                                primaryTagId = null,
                                isFinished = memo.isFinished,
                                createdAt = memo.createdAt,
                                updatedAt = memo.updatedAt,
                            ),
                        ),
                    nextCursor = nextCursor,
                )
            coVerify(exactly = 1) { memoListAppAction.list(sort = AppActionSortValue.TITLE, cursor = null) }
        }

    @Test
    fun `TC-edf2f510-aac0-4a31-afdb-2f9368763906 메모 추가는 받은 값과 태그를 공통 동작에 넘기고 id를 돌려준다`() =
        runBlocking<Unit> {
            val title = Arb.nonBlankString().next()
            val tagIdList = listOf(Arb.kotlinUuid().next().toString(), Arb.kotlinUuid().next().toString())
            val addedId = Arb.kotlinUuid().next().toString()
            coEvery { memoAppAction.add(any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns AppActionId(id = addedId)

            val response =
                tester.execute(name = "memoAdd") {
                    setString("title", title)
                    setStringList("tagIds", tagIdList)
                }

            response.returnValueAs<AppFunctionId>() shouldBe AppFunctionId(id = addedId)
            coVerify(exactly = 1) {
                memoAppAction.add(
                    title = title,
                    description = null,
                    color = null,
                    dateTime = null,
                    primaryTagId = null,
                    tagIds = tagIdList,
                    placeIds = emptyList(),
                    webIds = emptyList(),
                    contactIds = emptyList(),
                )
            }
        }

    @Test
    fun `메모 삭제는 바뀐 수를 돌려준다`() =
        runBlocking<Unit> {
            val memoId = Arb.kotlinUuid().next().toString()
            val changedCount = Arb.int(0..1).next()
            coEvery { memoAppAction.delete(memoId = memoId) } returns AppActionChangedCount(changedCount = changedCount)

            val response = tester.execute(name = "memoDelete") { setString(MEMO_ID, memoId) }

            response.returnValueAs<AppFunctionChangedCount>() shouldBe AppFunctionChangedCount(changedCount = changedCount)
        }

    @Test
    fun `TC-4ca7faaf-f740-4b6a-9c4b-33ac9b779292 메모 조회는 연결된 태그와 함께 돌려준다`() =
        runBlocking<Unit> {
            val memo =
                AppActionMemo(
                    id = Arb.kotlinUuid().next().toString(),
                    title = Arb.string().next(),
                    description = Arb.string().next(),
                    color = Arb.appActionColor().next(),
                    dateTime = null,
                    primaryTagId = null,
                    isFinished = Arb.boolean().next(),
                    createdAt = Arb.string().next(),
                    updatedAt = Arb.string().next(),
                )
            val tag = Arb.appActionTag().next()
            coEvery { memoAppAction.get(memoId = memo.id) } returns AppActionMemoDetail(memo = memo, tags = listOf(tag), places = emptyList(), webs = emptyList(), contacts = emptyList())

            val response = tester.execute(name = MEMO_GET) { setString(MEMO_ID, memo.id) }

            response.returnValueAs<AppFunctionMemoDetail>() shouldBe
                AppFunctionMemoDetail(
                    memo =
                        AppFunctionMemo(
                            id = memo.id,
                            title = memo.title,
                            description = memo.description,
                            color = memo.color,
                            dateTime = null,
                            primaryTagId = null,
                            isFinished = memo.isFinished,
                            createdAt = memo.createdAt,
                            updatedAt = memo.updatedAt,
                        ),
                    tags = listOf(tag.expectedTag()),
                    places = emptyList(),
                    webs = emptyList(),
                    contacts = emptyList(),
                )
        }

    @Test
    fun `없는 메모를 조회하면 값이 없는 결과를 돌려준다`() =
        runBlocking<Unit> {
            val memoId = Arb.kotlinUuid().next().toString()
            coEvery { memoAppAction.get(memoId = memoId) } returns null

            val response = tester.execute(name = MEMO_GET) { setString(MEMO_ID, memoId) }

            response.shouldBeInstanceOf<ExecuteAppFunctionResponse.Success>().returnValue.containsKey(ExecuteAppFunctionResponse.Success.PROPERTY_RETURN_VALUE) shouldBe false
        }

    @Test
    fun `인자 오류는 잘못된 인자 실패로 같은 메시지를 알린다`() =
        runBlocking<Unit> {
            val message = Arb.nonBlankString().next()
            coEvery { memoAppAction.finish(memoId = any()) } throws AppActionException.InvalidArgument(message = message)

            val response = tester.execute(name = "memoFinish") { setString(MEMO_ID, "not-id") }

            val error = response.shouldBeInstanceOf<ExecuteAppFunctionResponse.Error>().error
            error.shouldBeInstanceOf<AppFunctionInvalidArgumentException>()
            error.errorMessage shouldBe message
        }

    @Test
    fun `앱 기능 실패는 앱 실패로 같은 메시지를 알린다`() =
        runBlocking<Unit> {
            val message = Arb.nonBlankString().next()
            coEvery { memoAppAction.update(any(), any(), any(), any(), any(), any()) } throws AppActionException.Failure(message = message)

            val response =
                tester.execute(name = "memoUpdate") {
                    setString(MEMO_ID, Arb.kotlinUuid().next().toString())
                    setBoolean("clearDateTime", true)
                }

            val error = response.shouldBeInstanceOf<ExecuteAppFunctionResponse.Error>().error
            error.shouldBeInstanceOf<AppFunctionAppUnknownException>()
            error.errorMessage shouldBe message
            coVerify(exactly = 1) { memoAppAction.update(memoId = any(), title = null, description = null, color = null, dateTime = null, clearDateTime = true) }
        }
}

private const val MEMO_ID = "memoId"
private const val MEMO_GET = "memoGet"
private val memoLinkPrefixList = listOf("memoTag", "memoPrimaryTag", "memoPlace", "memoWeb", "memoContact")
