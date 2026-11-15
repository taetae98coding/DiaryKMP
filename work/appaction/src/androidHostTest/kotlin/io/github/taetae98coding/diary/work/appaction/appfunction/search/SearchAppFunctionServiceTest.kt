package io.github.taetae98coding.diary.work.appaction.appfunction.search

import android.content.Context
import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.appfunctions.ExecuteAppFunctionResponse
import androidx.appfunctions.testing.AppFunctionTestRule
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionServiceTester
import io.github.taetae98coding.diary.work.appaction.appfunction.core.returnValueAs
import io.github.taetae98coding.diary.work.appaction.appfunction.item.AppFunctionPlace
import io.github.taetae98coding.diary.work.appaction.appfunction.item.AppFunctionTag
import io.github.taetae98coding.diary.work.appaction.appfunction.memo.AppFunctionMemo
import io.github.taetae98coding.diary.work.appaction.appfunction.memo.AppFunctionMemoPage
import io.github.taetae98coding.diary.work.appaction.core.AppActionException
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.core.AppActionSortValue
import io.github.taetae98coding.diary.work.appaction.core.appActionColor
import io.github.taetae98coding.diary.work.appaction.memo.AppActionMemo
import io.github.taetae98coding.diary.work.appaction.place.AppActionPlace
import io.github.taetae98coding.diary.work.appaction.search.SearchAppAction
import io.github.taetae98coding.diary.work.appaction.tag.AppActionTag
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.numericDouble
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
class SearchAppFunctionServiceTest {
    private val context: Context = RuntimeEnvironment.getApplication()

    @get:Rule
    val appFunctionTestRule = AppFunctionTestRule(context)

    private val searchAppAction = mockk<SearchAppAction>()

    private lateinit var tester: AppFunctionServiceTester

    @Before
    fun setUp() {
        startKoin { modules(module { single { searchAppAction } }) }
        tester = AppFunctionServiceTester(context = context, appFunctionManager = appFunctionTestRule.getAppFunctionManager())
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `검색 동작을 모두 제공한다`() =
        runBlocking<Unit> {
            tester.functionNameList().filter { name -> name.startsWith("search") } shouldContainExactlyInAnyOrder
                listOf("searchMemo", "searchTag", "searchPlace", "searchWeb", "searchDday", "searchRoutine")
        }

    @Test
    fun `메모 검색은 검색어와 정렬을 공통 동작에 넘기고 페이지를 같은 값으로 돌려준다`() =
        runBlocking<Unit> {
            val query = Arb.string().next()
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
            val nextCursor = Arb.int(1..1000).next().toString()
            coEvery { searchAppAction.memo(query = any(), sort = any(), cursor = any()) } returns AppActionPage(items = listOf(memo), nextCursor = nextCursor)

            val response =
                tester.execute(name = "searchMemo") {
                    setString(QUERY, query)
                    setString(SORT, AppActionSortValue.RECENTLY_UPDATED)
                }

            response.returnValueAs<AppFunctionMemoPage>() shouldBe
                AppFunctionMemoPage(
                    items =
                        listOf(
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
                        ),
                    nextCursor = nextCursor,
                )
            coVerify(exactly = 1) { searchAppAction.memo(query = query, sort = AppActionSortValue.RECENTLY_UPDATED, cursor = null) }
        }

    @Test
    fun `태그 검색은 정렬과 위치를 담지 않으면 빈 값으로 넘기고 태그 페이지를 돌려준다`() =
        runBlocking<Unit> {
            val query = Arb.string().next()
            val tag =
                AppActionTag(
                    id = Arb.kotlinUuid().next().toString(),
                    emoji = Arb.string().next(),
                    title = Arb.string().next(),
                    description = Arb.string().next(),
                    color = Arb.appActionColor().next(),
                    isFinished = Arb.boolean().next(),
                    createdAt = Arb.string().next(),
                    updatedAt = Arb.string().next(),
                )
            coEvery { searchAppAction.tag(query = any(), sort = any(), cursor = any()) } returns AppActionPage(items = listOf(tag), nextCursor = null)

            val response = tester.execute(name = "searchTag") { setString(QUERY, query) }

            response.returnValueAs<AppFunctionSearchTagPage>() shouldBe
                AppFunctionSearchTagPage(
                    items =
                        listOf(
                            AppFunctionTag(
                                id = tag.id,
                                emoji = tag.emoji,
                                title = tag.title,
                                description = tag.description,
                                color = tag.color,
                                isFinished = tag.isFinished,
                                createdAt = tag.createdAt,
                                updatedAt = tag.updatedAt,
                            ),
                        ),
                    nextCursor = null,
                )
            coVerify(exactly = 1) { searchAppAction.tag(query = query, sort = null, cursor = null) }
        }

    @Test
    fun `장소 검색은 다음 페이지 위치를 넘기고 좌표가 없는 장소도 돌려준다`() =
        runBlocking<Unit> {
            val query = Arb.string().next()
            val cursor = Arb.int(1..1000).next().toString()
            val place =
                AppActionPlace(
                    id = Arb.kotlinUuid().next().toString(),
                    title = Arb.string().next(),
                    description = Arb.string().next(),
                    color = Arb.appActionColor().next(),
                    address = Arb.string().next(),
                    latitude = Arb.numericDouble().next(),
                    longitude = null,
                    createdAt = Arb.string().next(),
                    updatedAt = Arb.string().next(),
                )
            coEvery { searchAppAction.place(query = any(), sort = any(), cursor = any()) } returns AppActionPage(items = listOf(place), nextCursor = null)

            val response =
                tester.execute(name = "searchPlace") {
                    setString(QUERY, query)
                    setString(CURSOR, cursor)
                }

            response.returnValueAs<AppFunctionSearchPlacePage>() shouldBe
                AppFunctionSearchPlacePage(
                    items =
                        listOf(
                            AppFunctionPlace(
                                id = place.id,
                                title = place.title,
                                description = place.description,
                                color = place.color,
                                address = place.address,
                                latitude = place.latitude,
                                longitude = null,
                                createdAt = place.createdAt,
                                updatedAt = place.updatedAt,
                            ),
                        ),
                    nextCursor = null,
                )
            coVerify(exactly = 1) { searchAppAction.place(query = query, sort = null, cursor = cursor) }
        }

    @Test
    fun `인자 오류는 잘못된 인자 실패로 같은 메시지를 알린다`() =
        runBlocking<Unit> {
            val message = Arb.nonBlankString().next()
            coEvery { searchAppAction.web(query = any(), sort = any(), cursor = any()) } throws AppActionException.InvalidArgument(message = message)

            val response =
                tester.execute(name = "searchWeb") {
                    setString(QUERY, Arb.string().next())
                    setString(CURSOR, "not-cursor")
                }

            val error = response.shouldBeInstanceOf<ExecuteAppFunctionResponse.Error>().error
            error.shouldBeInstanceOf<AppFunctionInvalidArgumentException>()
            error.errorMessage shouldBe message
        }
}

private const val QUERY = "query"
private const val SORT = "sort"
private const val CURSOR = "cursor"
