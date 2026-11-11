package io.github.taetae98coding.diary.work.appaction.appfunction.memo

import android.content.Context
import androidx.appfunctions.AppFunctionAppUnknownException
import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.appfunctions.ExecuteAppFunctionResponse
import androidx.appfunctions.testing.AppFunctionTestRule
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.testing.memo.memo
import io.github.taetae98coding.diary.domain.contact.usecase.FindContactUseCase
import io.github.taetae98coding.diary.domain.memo.exception.MemoTitleBlankException
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.CopyMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.DeleteMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FindMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FinishMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoContactUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoPlaceUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoWebUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageContactMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageFinishedContactMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageFinishedMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageFinishedPlaceMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageFinishedWebMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageMemoHomeUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PagePlaceMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageWebMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestartMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestoreMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.UpdateMemoUseCase
import io.github.taetae98coding.diary.domain.place.usecase.FindPlaceUseCase
import io.github.taetae98coding.diary.domain.web.usecase.FindWebUseCase
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionId
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionServiceTester
import io.github.taetae98coding.diary.work.appaction.appfunction.core.returnValueAs
import io.github.taetae98coding.diary.work.appaction.memo.MemoAppAction
import io.github.taetae98coding.diary.work.appaction.memo.MemoListAppAction
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
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

// 데스크톱 MCP 도구와 같은 앱 기능을 거쳐 같은 결과를 내는지 보도록, 공통 동작은 실제로 만들고 앱 기능만 mock으로 둔다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoAppFunctionUseCaseTest {
    private val context: Context = RuntimeEnvironment.getApplication()

    @get:Rule
    val appFunctionTestRule = AppFunctionTestRule(context)

    private val pageMemoHomeUseCase = mockk<PageMemoHomeUseCase>()
    private val findMemoUseCase = mockk<FindMemoUseCase>()
    private val getMemoTagUseCase = mockk<GetMemoTagUseCase>()
    private val getMemoPlaceUseCase = mockk<GetMemoPlaceUseCase>()
    private val getMemoWebUseCase = mockk<GetMemoWebUseCase>()
    private val getMemoContactUseCase = mockk<GetMemoContactUseCase>()
    private val addMemoUseCase = mockk<AddMemoUseCase>()
    private val updateMemoUseCase = mockk<UpdateMemoUseCase>()
    private val copyMemoUseCase = mockk<CopyMemoUseCase>()
    private val finishMemoUseCase = mockk<FinishMemoUseCase>()
    private val restartMemoUseCase = mockk<RestartMemoUseCase>()
    private val deleteMemoUseCase = mockk<DeleteMemoUseCase>()
    private val restoreMemoUseCase = mockk<RestoreMemoUseCase>()

    private lateinit var tester: AppFunctionServiceTester

    @Before
    fun setUp() {
        val memoAppAction =
            MemoAppAction(
                findMemoUseCase = findMemoUseCase,
                getMemoTagUseCase = getMemoTagUseCase,
                getMemoPlaceUseCase = getMemoPlaceUseCase,
                getMemoWebUseCase = getMemoWebUseCase,
                getMemoContactUseCase = getMemoContactUseCase,
                addMemoUseCase = addMemoUseCase,
                updateMemoUseCase = updateMemoUseCase,
                copyMemoUseCase = copyMemoUseCase,
                finishMemoUseCase = finishMemoUseCase,
                restartMemoUseCase = restartMemoUseCase,
                deleteMemoUseCase = deleteMemoUseCase,
                restoreMemoUseCase = restoreMemoUseCase,
            )
        val memoListAppAction =
            MemoListAppAction(
                findPlaceUseCase = mockk<FindPlaceUseCase>(),
                findWebUseCase = mockk<FindWebUseCase>(),
                findContactUseCase = mockk<FindContactUseCase>(),
                pageMemoHomeUseCase = pageMemoHomeUseCase,
                pageFinishedMemoUseCase = mockk<PageFinishedMemoUseCase>(),
                pagePlaceMemoUseCase = mockk<PagePlaceMemoUseCase>(),
                pageFinishedPlaceMemoUseCase = mockk<PageFinishedPlaceMemoUseCase>(),
                pageWebMemoUseCase = mockk<PageWebMemoUseCase>(),
                pageFinishedWebMemoUseCase = mockk<PageFinishedWebMemoUseCase>(),
                pageContactMemoUseCase = mockk<PageContactMemoUseCase>(),
                pageFinishedContactMemoUseCase = mockk<PageFinishedContactMemoUseCase>(),
            )
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
    fun `TC-cbe1dece-01a3-48b8-8f17-426e674f8546 설정 없이 진행 중 메모 목록을 받는다`() =
        runBlocking<Unit> {
            val memoList = Arb.list(Arb.memo(), 1..3).next()
            every { pageMemoHomeUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(memoList)))

            val response = tester.execute(name = MEMO_LIST) { }

            response.returnValueAs<AppFunctionMemoPage>() shouldBe AppFunctionMemoPage(items = memoList.map { memo -> memo.expected() }, nextCursor = null)
            verify(exactly = 1) { pageMemoHomeUseCase(parameter = ListSort.DEFAULT) }
        }

    @Test
    fun `TC-98fbb01f-297f-4333-9ef9-e2281361473a 진행 중 메모 목록은 MCP와 같은 항목을 돌려준다`() =
        runBlocking<Unit> {
            val memoList = Arb.list(Arb.memo(), 2..2).next().map { memo -> memo.copy(isFinished = false, isDeleted = false) }
            every { pageMemoHomeUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(memoList)))

            val response = tester.execute(name = MEMO_LIST) { }

            response.returnValueAs<AppFunctionMemoPage>() shouldBe AppFunctionMemoPage(items = memoList.map { memo -> memo.expected() }, nextCursor = null)
        }

    @Test
    fun `TC-98fbb01f-297f-4333-9ef9-e2281361473a 메모 상세는 MCP와 같은 항목을 돌려준다`() =
        runBlocking<Unit> {
            val memo = Arb.memo().next().copy(isDeleted = false)
            every { findMemoUseCase(parameter = memo.id) } returns flowOf(Result.success(memo))
            every { getMemoTagUseCase(parameter = memo.id) } returns flowOf(Result.success(emptyList()))
            every { getMemoPlaceUseCase(parameter = memo.id) } returns flowOf(Result.success(emptyList()))
            every { getMemoWebUseCase(parameter = memo.id) } returns flowOf(Result.success(emptyList()))
            every { getMemoContactUseCase(parameter = memo.id) } returns flowOf(Result.success(emptyList()))

            val response = tester.execute(name = MEMO_GET) { setString(MEMO_ID, memo.id.toString()) }

            response.returnValueAs<AppFunctionMemoDetail>() shouldBe
                AppFunctionMemoDetail(memo = memo.expected(), tags = emptyList(), places = emptyList(), webs = emptyList(), contacts = emptyList())
        }

    @Test
    fun `TC-98fbb01f-297f-4333-9ef9-e2281361473a 메모 추가는 MCP와 같이 추가한 id를 돌려준다`() =
        runBlocking<Unit> {
            val title = Arb.nonBlankString().next()
            val addedId = Arb.kotlinUuid().next()
            coEvery { addMemoUseCase(parameter = any()) } returns Result.success(addedId)

            val response = tester.execute(name = MEMO_ADD) { setString(TITLE, title) }

            response.returnValueAs<AppFunctionId>() shouldBe AppFunctionId(id = addedId.toString())
        }

    @Test
    fun `TC-ad39c2f4-b850-40b0-83f3-c67890a7169e 형식이 아닌 메모 id는 실패하고 조회하지 않는다`() =
        runBlocking<Unit> {
            val response = tester.execute(name = MEMO_GET) { setString(MEMO_ID, NOT_ID) }

            val error = response.shouldBeInstanceOf<ExecuteAppFunctionResponse.Error>().error
            error.shouldBeInstanceOf<AppFunctionInvalidArgumentException>()
            error.errorMessage shouldBe "'memoId' must be an id."
            verify(exactly = 0) { findMemoUseCase(parameter = any()) }
            confirmWriteNotCalled()
        }

    @Test
    fun `TC-ad39c2f4-b850-40b0-83f3-c67890a7169e 고를 수 없는 정렬은 실패하고 조회하지 않는다`() =
        runBlocking<Unit> {
            // Android는 동작 설명에 고를 수 있는 정렬을 담으므로, 고를 수 없는 정렬은 요청을 만드는 단계에서 거부된다.
            shouldThrow<IllegalArgumentException> { tester.execute(name = MEMO_LIST) { setString(SORT, "name") } }

            verify(exactly = 0) { pageMemoHomeUseCase(parameter = any()) }
            confirmWriteNotCalled()
        }

    @Test
    fun `TC-ad39c2f4-b850-40b0-83f3-c67890a7169e 빈 제목으로 추가하면 실패하고 저장 항목은 그대로다`() =
        runBlocking<Unit> {
            coEvery { addMemoUseCase(parameter = any()) } returns Result.failure(MemoTitleBlankException())

            val response = tester.execute(name = MEMO_ADD) { setString(TITLE, " ") }

            val error = response.shouldBeInstanceOf<ExecuteAppFunctionResponse.Error>().error
            error.shouldBeInstanceOf<AppFunctionAppUnknownException>()
            error.errorMessage shouldBe "MemoTitleBlankException"
            coVerify(exactly = 1) { addMemoUseCase(parameter = any()) }
            confirmWriteNotCalled()
        }

    @Test
    fun `TC-4e322266-9215-44f2-bafd-54e1b0b9de20 다음 페이지 위치로 101번째 메모를 받는다`() =
        runBlocking<Unit> {
            val memoList = Arb.list(Arb.memo(), PAGE_SIZE + 1..PAGE_SIZE + 1).next()
            every { pageMemoHomeUseCase(parameter = any()) } answers { flowOf(Result.success(PagingData.from(memoList))) }

            val firstPage = tester.execute(name = MEMO_LIST) { }.returnValueAs<AppFunctionMemoPage>()
            val nextCursor = firstPage.nextCursor
            val secondPage = tester.execute(name = MEMO_LIST) { setString(CURSOR, requireNotNull(nextCursor)) }.returnValueAs<AppFunctionMemoPage>()

            firstPage.items shouldBe memoList.take(PAGE_SIZE).map { memo -> memo.expected() }
            secondPage shouldBe AppFunctionMemoPage(items = listOf(memoList.last().expected()), nextCursor = null)
        }

    private fun confirmWriteNotCalled() {
        confirmVerified(addMemoUseCase, updateMemoUseCase, copyMemoUseCase, finishMemoUseCase, restartMemoUseCase, deleteMemoUseCase, restoreMemoUseCase)
    }
}

private const val MEMO_LIST = "memoList"
private const val MEMO_GET = "memoGet"
private const val MEMO_ADD = "memoAdd"
private const val MEMO_ID = "memoId"
private const val TITLE = "title"
private const val SORT = "sort"
private const val CURSOR = "cursor"
private const val NOT_ID = "not-id"
private const val PAGE_SIZE = 100
private const val RGB = 0xFFFFFFL

// 데스크톱 MCP 도구가 메모를 JSON으로 담는 규칙: id와 시각은 문자열, 색은 불투명도를 뺀 #RRGGBB, 기간은 종일 여부와 시작·끝.
private fun Memo.expected(): AppFunctionMemo =
    AppFunctionMemo(
        id = id.toString(),
        title = detail.title,
        description = detail.description,
        color = "#%06X".format(detail.color and RGB),
        dateTime =
            when (val dateTime = detail.dateTime) {
                null -> null
                is MemoDateTime.AllDay -> AppFunctionMemoDateTime(allDay = true, start = dateTime.dateRange.start.toString(), endInclusive = dateTime.dateRange.endInclusive.toString())
                is MemoDateTime.DateTime -> AppFunctionMemoDateTime(allDay = false, start = dateTime.start.toString(), endInclusive = dateTime.endInclusive.toString())
            },
        primaryTagId = primaryTagId?.toString(),
        isFinished = isFinished,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )
