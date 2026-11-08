package io.github.taetae98coding.diary.work.appaction.appfunction

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
import io.github.taetae98coding.diary.work.appaction.appfunction.core.returnValueAs
import io.github.taetae98coding.diary.work.appaction.appfunction.item.AppFunctionPlace
import io.github.taetae98coding.diary.work.appaction.appfunction.item.AppFunctionTag
import io.github.taetae98coding.diary.work.appaction.appfunction.place.AppFunctionPlaceDetail
import io.github.taetae98coding.diary.work.appaction.appfunction.place.AppFunctionPlaceList
import io.github.taetae98coding.diary.work.appaction.appfunction.place.AppFunctionPlacePage
import io.github.taetae98coding.diary.work.appaction.core.AppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.AppActionDone
import io.github.taetae98coding.diary.work.appaction.core.AppActionException
import io.github.taetae98coding.diary.work.appaction.core.AppActionId
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.core.AppActionSortValue
import io.github.taetae98coding.diary.work.appaction.core.appActionColor
import io.github.taetae98coding.diary.work.appaction.place.AppActionPlace
import io.github.taetae98coding.diary.work.appaction.place.AppActionPlaceDetail
import io.github.taetae98coding.diary.work.appaction.place.PlaceAppAction
import io.github.taetae98coding.diary.work.appaction.tag.AppActionTag
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
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
class PlaceAppFunctionServiceTest {
    private val context: Context = RuntimeEnvironment.getApplication()

    @get:Rule
    val appFunctionTestRule = AppFunctionTestRule(context)

    private val placeAppAction = mockk<PlaceAppAction>()

    private lateinit var tester: AppFunctionServiceTester

    @Before
    fun setUp() {
        startKoin { modules(module { single { placeAppAction } }) }
        tester = AppFunctionServiceTester(context = context, appFunctionManager = appFunctionTestRule.getAppFunctionManager())
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `장소 동작을 모두 제공한다`() =
        runBlocking<Unit> {
            tester.functionNameList().filter { name -> name.startsWith("place") } shouldContainExactlyInAnyOrder
                listOf(
                    PLACE_LIST,
                    PLACE_LIST_IN_AREA,
                    PLACE_GET,
                    "placeMemoList",
                    "placeMemoFinishedList",
                    PLACE_ADD,
                    PLACE_UPDATE,
                    PLACE_DELETE,
                    "placeRestore",
                    PLACE_TAG_ADD,
                    "placeTagRemove",
                )
        }

    @Test
    fun `장소 목록은 공통 동작의 페이지를 같은 값으로 돌려준다`() =
        runBlocking<Unit> {
            val place = appActionPlace()
            val nextCursor = Arb.int(1..1000).next().toString()
            coEvery { placeAppAction.list(sort = any(), cursor = any()) } returns AppActionPage(items = listOf(place), nextCursor = nextCursor)

            val response = tester.execute(name = PLACE_LIST) { setString("sort", AppActionSortValue.RECENTLY_UPDATED) }

            response.returnValueAs<AppFunctionPlacePage>() shouldBe AppFunctionPlacePage(items = listOf(place.expected()), nextCursor = nextCursor)
            coVerify(exactly = 1) { placeAppAction.list(sort = AppActionSortValue.RECENTLY_UPDATED, cursor = null) }
        }

    @Test
    fun `지역 안 목록은 받은 영역을 공통 동작에 넘기고 모든 항목을 돌려준다`() =
        runBlocking<Unit> {
            val (south, north) =
                Arb
                    .list(Arb.numericDouble(min = -90.0, max = 90.0), 2..2)
                    .next()
                    .sorted()
                    .map(Double::toString)
            val (west, east) =
                Arb
                    .list(Arb.numericDouble(min = -180.0, max = 180.0), 2..2)
                    .next()
                    .sorted()
                    .map(Double::toString)
            val place = appActionPlace().copy(latitude = null, longitude = null)
            coEvery { placeAppAction.listInArea(any(), any(), any(), any(), any()) } returns listOf(place)

            val response =
                tester.execute(name = PLACE_LIST_IN_AREA) {
                    setString("south", south)
                    setString("north", north)
                    setString("west", west)
                    setString("east", east)
                }

            response.returnValueAs<AppFunctionPlaceList>() shouldBe AppFunctionPlaceList(items = listOf(place.expected()))
            coVerify(exactly = 1) { placeAppAction.listInArea(south = south, north = north, west = west, east = east, sort = null) }
        }

    @Test
    fun `장소 조회는 연결된 태그와 함께 돌려주고 없으면 값이 없는 결과를 돌려준다`() =
        runBlocking<Unit> {
            val place = appActionPlace()
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
            val missingId = Arb.kotlinUuid().next().toString()
            coEvery { placeAppAction.get(placeId = place.id) } returns AppActionPlaceDetail(place = place, tags = listOf(tag))
            coEvery { placeAppAction.get(placeId = missingId) } returns null

            tester.execute(name = PLACE_GET) { setString(PLACE_ID, place.id) }.returnValueAs<AppFunctionPlaceDetail>() shouldBe
                AppFunctionPlaceDetail(
                    place = place.expected(),
                    tags =
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
                )
            tester
                .execute(name = PLACE_GET) { setString(PLACE_ID, missingId) }
                .shouldBeInstanceOf<ExecuteAppFunctionResponse.Success>()
                .returnValue
                .containsKey(ExecuteAppFunctionResponse.Success.PROPERTY_RETURN_VALUE) shouldBe false
        }

    @Test
    fun `장소 추가는 받은 값을 공통 동작에 넘기고 담지 않은 값은 비워 넘긴다`() =
        runBlocking<Unit> {
            val latitude = Arb.numericDouble(min = -90.0, max = 90.0).next().toString()
            val longitude = Arb.numericDouble(min = -180.0, max = 180.0).next().toString()
            val title = Arb.nonBlankString().next()
            val addedId = Arb.kotlinUuid().next().toString()
            coEvery { placeAppAction.add(any(), any(), any(), any(), any(), any(), any()) } returns AppActionId(id = addedId)

            val response =
                tester.execute(name = PLACE_ADD) {
                    setString("title", title)
                    setString("latitude", latitude)
                    setString("longitude", longitude)
                }

            response.returnValueAs<AppFunctionId>() shouldBe AppFunctionId(id = addedId)
            coVerify(exactly = 1) {
                placeAppAction.add(title = title, latitude = latitude, longitude = longitude, address = null, description = null, color = null, tagIds = emptyList())
            }
        }

    @Test
    fun `장소 수정은 담은 값만 공통 동작에 넘기고 바뀐 수를 돌려준다`() =
        runBlocking<Unit> {
            val longitude = Arb.numericDouble(min = -180.0, max = 180.0).next().toString()
            val placeId = Arb.kotlinUuid().next().toString()
            val changedCount = Arb.int(0..1).next()
            coEvery { placeAppAction.update(any(), any(), any(), any(), any(), any(), any()) } returns AppActionChangedCount(changedCount = changedCount)

            val response =
                tester.execute(name = PLACE_UPDATE) {
                    setString(PLACE_ID, placeId)
                    setString("longitude", longitude)
                }

            response.returnValueAs<AppFunctionChangedCount>() shouldBe AppFunctionChangedCount(changedCount = changedCount)
            coVerify(exactly = 1) {
                placeAppAction.update(placeId = placeId, title = null, description = null, color = null, address = null, latitude = null, longitude = longitude)
            }
        }

    @Test
    fun `태그 연결은 완료를 돌려준다`() =
        runBlocking<Unit> {
            val placeId = Arb.kotlinUuid().next().toString()
            val tagId = Arb.kotlinUuid().next().toString()
            coEvery { placeAppAction.tagAdd(placeId = placeId, tagId = tagId) } returns AppActionDone(done = true)

            val response =
                tester.execute(name = PLACE_TAG_ADD) {
                    setString(PLACE_ID, placeId)
                    setString("tagId", tagId)
                }

            response.returnValueAs<AppFunctionDone>() shouldBe AppFunctionDone(done = true)
        }

    @Test
    fun `인자 오류는 잘못된 인자 실패로, 앱 기능 실패는 앱 실패로 같은 메시지를 알린다`() =
        runBlocking<Unit> {
            val (south, north) =
                Arb
                    .list(Arb.numericDouble(min = -90.0, max = 90.0), 2..2)
                    .next()
                    .sorted()
                    .map(Double::toString)
            val (west, east) =
                Arb
                    .list(Arb.numericDouble(min = -180.0, max = 180.0), 2..2)
                    .next()
                    .sorted()
                    .map(Double::toString)
            val invalidMessage = Arb.nonBlankString().next()
            val failureMessage = Arb.nonBlankString().next()
            coEvery { placeAppAction.listInArea(any(), any(), any(), any(), any()) } throws AppActionException.InvalidArgument(message = invalidMessage)
            coEvery { placeAppAction.delete(placeId = any()) } throws AppActionException.Failure(message = failureMessage)

            val invalid =
                tester
                    .execute(name = PLACE_LIST_IN_AREA) {
                        setString("south", north)
                        setString("north", south)
                        setString("west", west)
                        setString("east", east)
                    }.shouldBeInstanceOf<ExecuteAppFunctionResponse.Error>()
                    .error
            val failure = tester.execute(name = PLACE_DELETE) { setString(PLACE_ID, Arb.kotlinUuid().next().toString()) }.shouldBeInstanceOf<ExecuteAppFunctionResponse.Error>().error

            invalid.shouldBeInstanceOf<AppFunctionInvalidArgumentException>()
            invalid.errorMessage shouldBe invalidMessage
            failure.shouldBeInstanceOf<AppFunctionAppUnknownException>()
            failure.errorMessage shouldBe failureMessage
        }

    private fun appActionPlace(): AppActionPlace =
        AppActionPlace(
            id = Arb.kotlinUuid().next().toString(),
            title = Arb.string().next(),
            description = Arb.string().next(),
            color = Arb.appActionColor().next(),
            address = Arb.string().next(),
            latitude = Arb.numericDouble(min = -90.0, max = 90.0).next(),
            longitude = Arb.numericDouble(min = -180.0, max = 180.0).next(),
            createdAt = Arb.string().next(),
            updatedAt = Arb.string().next(),
        )

    private fun AppActionPlace.expected(): AppFunctionPlace =
        AppFunctionPlace(
            id = id,
            title = title,
            description = description,
            color = color,
            address = address,
            latitude = latitude,
            longitude = longitude,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}

private const val PLACE_ID = "placeId"
private const val PLACE_ADD = "placeAdd"
private const val PLACE_DELETE = "placeDelete"
private const val PLACE_GET = "placeGet"
private const val PLACE_LIST = "placeList"
private const val PLACE_LIST_IN_AREA = "placeListInArea"
private const val PLACE_TAG_ADD = "placeTagAdd"
private const val PLACE_UPDATE = "placeUpdate"
