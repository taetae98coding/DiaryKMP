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
import io.github.taetae98coding.diary.work.appaction.appfunction.core.expectedTag
import io.github.taetae98coding.diary.work.appaction.appfunction.core.returnValueAs
import io.github.taetae98coding.diary.work.appaction.appfunction.item.AppFunctionWeb
import io.github.taetae98coding.diary.work.appaction.appfunction.memo.AppFunctionMemo
import io.github.taetae98coding.diary.work.appaction.appfunction.memo.AppFunctionMemoPage
import io.github.taetae98coding.diary.work.appaction.appfunction.web.AppFunctionWebDetail
import io.github.taetae98coding.diary.work.appaction.appfunction.web.AppFunctionWebHeader
import io.github.taetae98coding.diary.work.appaction.appfunction.web.AppFunctionWebPage
import io.github.taetae98coding.diary.work.appaction.appfunction.web.AppFunctionWebWithHeaders
import io.github.taetae98coding.diary.work.appaction.core.AppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.AppActionDone
import io.github.taetae98coding.diary.work.appaction.core.AppActionException
import io.github.taetae98coding.diary.work.appaction.core.AppActionId
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.core.AppActionSortValue
import io.github.taetae98coding.diary.work.appaction.core.appActionColor
import io.github.taetae98coding.diary.work.appaction.memo.AppActionMemo
import io.github.taetae98coding.diary.work.appaction.memo.MemoListAppAction
import io.github.taetae98coding.diary.work.appaction.tag.AppActionEntityTagKind
import io.github.taetae98coding.diary.work.appaction.tag.EntityTagAppAction
import io.github.taetae98coding.diary.work.appaction.tag.appActionTag
import io.github.taetae98coding.diary.work.appaction.web.AppActionWeb
import io.github.taetae98coding.diary.work.appaction.web.AppActionWebDetail
import io.github.taetae98coding.diary.work.appaction.web.AppActionWebHeader
import io.github.taetae98coding.diary.work.appaction.web.AppActionWebWithHeaders
import io.github.taetae98coding.diary.work.appaction.web.WebAppAction
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
class WebAppFunctionServiceTest {
    private val context: Context = RuntimeEnvironment.getApplication()

    @get:Rule
    val appFunctionTestRule = AppFunctionTestRule(context)

    private val webAppAction = mockk<WebAppAction>()
    private val entityTagAppAction = mockk<EntityTagAppAction>()
    private val memoListAppAction = mockk<MemoListAppAction>()

    private lateinit var tester: AppFunctionServiceTester

    @Before
    fun setUp() {
        startKoin {
            modules(
                module {
                    single { webAppAction }
                    single { entityTagAppAction }
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
    fun `웹 동작을 모두 제공한다`() =
        runBlocking<Unit> {
            tester.functionNameList().filter { name -> name.startsWith("web") } shouldContainExactlyInAnyOrder
                listOf(WEB_LIST, WEB_GET, WEB_MEMO_LIST, "webMemoFinishedList", WEB_ADD, WEB_UPDATE, "webDelete", WEB_RESTORE, WEB_TAG_ADD, WEB_TAG_REMOVE)
        }

    @Test
    fun `웹 목록은 공통 동작의 페이지를 같은 값으로 돌려준다`() =
        runBlocking<Unit> {
            val cursor = Arb.int(1..1000).next().toString()
            val web = appActionWeb()
            coEvery { webAppAction.list(sort = any(), cursor = any()) } returns AppActionPage(items = listOf(web), nextCursor = null)

            val response = tester.execute(name = WEB_LIST) { setString("cursor", cursor) }

            response.returnValueAs<AppFunctionWebPage>() shouldBe
                AppFunctionWebPage(
                    items = listOf(AppFunctionWeb(id = web.id, title = web.title, description = web.description, url = web.url, createdAt = web.createdAt, updatedAt = web.updatedAt)),
                    nextCursor = null,
                )
            coVerify(exactly = 1) { webAppAction.list(sort = null, cursor = cursor) }
        }

    @Test
    fun `TC-4ca7faaf-f740-4b6a-9c4b-33ac9b779292 웹 조회는 요청 헤더와 연결된 태그를 함께 돌려준다`() =
        runBlocking<Unit> {
            val web = appActionWeb()
            val tag = Arb.appActionTag().next()
            val header = AppActionWebHeader(name = Arb.string().next(), value = Arb.string().next())
            val detail =
                AppActionWebDetail(
                    web =
                        AppActionWebWithHeaders(
                            id = web.id,
                            title = web.title,
                            description = web.description,
                            url = web.url,
                            createdAt = web.createdAt,
                            updatedAt = web.updatedAt,
                            headers = listOf(header),
                        ),
                    tags = listOf(tag),
                )
            coEvery { webAppAction.get(webId = web.id) } returns detail

            val response = tester.execute(name = WEB_GET) { setString(WEB_ID, web.id) }

            response.returnValueAs<AppFunctionWebDetail>() shouldBe
                AppFunctionWebDetail(
                    web =
                        AppFunctionWebWithHeaders(
                            id = web.id,
                            title = web.title,
                            description = web.description,
                            url = web.url,
                            createdAt = web.createdAt,
                            updatedAt = web.updatedAt,
                            headers = listOf(AppFunctionWebHeader(name = header.name, value = header.value)),
                        ),
                    tags = listOf(tag.expectedTag()),
                )
        }

    @Test
    fun `웹의 메모 목록은 메모 페이지로 돌려준다`() =
        runBlocking<Unit> {
            val cursor = Arb.int(1..1000).next().toString()
            val webId = Arb.kotlinUuid().next().toString()
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
            coEvery { memoListAppAction.webList(webId = any(), sort = any(), cursor = any()) } returns AppActionPage(items = listOf(memo), nextCursor = cursor)

            val response =
                tester.execute(name = WEB_MEMO_LIST) {
                    setString(WEB_ID, webId)
                    setString("sort", AppActionSortValue.DEFAULT)
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
                    nextCursor = cursor,
                )
            coVerify(exactly = 1) { memoListAppAction.webList(webId = webId, sort = AppActionSortValue.DEFAULT, cursor = null) }
        }

    @Test
    fun `웹 추가는 받은 값을 공통 동작에 넘기고 담지 않은 헤더와 태그는 빈 목록으로 넘긴다`() =
        runBlocking<Unit> {
            val title = Arb.nonBlankString().next()
            val url = Arb.nonBlankString().next()
            val addedId = Arb.kotlinUuid().next().toString()
            coEvery { webAppAction.add(any(), any(), any(), any(), any()) } returns AppActionId(id = addedId)

            val response =
                tester.execute(name = WEB_ADD) {
                    setString(TITLE, title)
                    setString("url", url)
                }

            response.returnValueAs<AppFunctionId>() shouldBe AppFunctionId(id = addedId)
            coVerify(exactly = 1) { webAppAction.add(title = title, url = url, description = null, headers = emptyList(), tagIds = emptyList()) }
        }

    @Test
    fun `TC-edf2f510-aac0-4a31-afdb-2f9368763906 웹 추가는 받은 값과 태그를 공통 동작에 넘기고 id를 돌려준다`() =
        runBlocking<Unit> {
            val title = Arb.nonBlankString().next()
            val url = Arb.nonBlankString().next()
            val tagIdList = listOf(Arb.kotlinUuid().next().toString(), Arb.kotlinUuid().next().toString())
            val addedId = Arb.kotlinUuid().next().toString()
            coEvery { webAppAction.add(any(), any(), any(), any(), any()) } returns AppActionId(id = addedId)

            val response =
                tester.execute(name = WEB_ADD) {
                    setString(TITLE, title)
                    setString("url", url)
                    setStringList("tagIds", tagIdList)
                }

            response.returnValueAs<AppFunctionId>() shouldBe AppFunctionId(id = addedId)
            coVerify(exactly = 1) { webAppAction.add(title = title, url = url, description = null, headers = emptyList(), tagIds = tagIdList) }
        }

    @Test
    fun `웹 수정은 헤더를 담지 않음과 빈 목록을 구분해 넘긴다`() =
        runBlocking<Unit> {
            val webId = Arb.kotlinUuid().next().toString()
            val changedCount = Arb.int(0..1).next()
            coEvery { webAppAction.update(any(), any(), any(), any(), any()) } returns AppActionChangedCount(changedCount = changedCount)

            tester.execute(name = WEB_UPDATE) { setString(WEB_ID, webId) }.returnValueAs<AppFunctionChangedCount>() shouldBe AppFunctionChangedCount(changedCount = changedCount)
            tester.execute(name = WEB_UPDATE) {
                setString(WEB_ID, webId)
                setStringList("headers", emptyList())
            }

            coVerify(exactly = 1) { webAppAction.update(webId = webId, title = null, description = null, url = null, headers = null) }
            coVerify(exactly = 1) { webAppAction.update(webId = webId, title = null, description = null, url = null, headers = emptyList()) }
        }

    @Test
    fun `TC-173de82e-308b-45d4-bc9b-14a00eb1c2fc 웹 태그 연결과 해제는 완료를 돌려준다`() =
        runBlocking<Unit> {
            val webId = Arb.kotlinUuid().next().toString()
            val tagId = Arb.kotlinUuid().next().toString()
            coEvery { entityTagAppAction.add(kind = AppActionEntityTagKind.WEB, entityId = any(), tagId = any()) } returns AppActionDone(done = true)
            coEvery { entityTagAppAction.remove(kind = AppActionEntityTagKind.WEB, entityId = any(), tagId = any()) } returns AppActionDone(done = true)

            val addResponse =
                tester.execute(name = WEB_TAG_ADD) {
                    setString(WEB_ID, webId)
                    setString("tagId", tagId)
                }
            val removeResponse =
                tester.execute(name = WEB_TAG_REMOVE) {
                    setString(WEB_ID, webId)
                    setString("tagId", tagId)
                }

            addResponse.returnValueAs<AppFunctionDone>() shouldBe AppFunctionDone(done = true)
            removeResponse.returnValueAs<AppFunctionDone>() shouldBe AppFunctionDone(done = true)
            coVerify(exactly = 1) { entityTagAppAction.add(kind = AppActionEntityTagKind.WEB, entityId = webId, tagId = tagId) }
            coVerify(exactly = 1) { entityTagAppAction.remove(kind = AppActionEntityTagKind.WEB, entityId = webId, tagId = tagId) }
        }

    @Test
    fun `인자 오류는 잘못된 인자 실패로, 앱 기능 실패는 앱 실패로 같은 메시지를 알린다`() =
        runBlocking<Unit> {
            val invalidMessage = Arb.nonBlankString().next()
            val failureMessage = Arb.nonBlankString().next()
            coEvery { webAppAction.add(any(), any(), any(), any(), any()) } throws AppActionException.InvalidArgument(message = invalidMessage)
            coEvery { webAppAction.restore(webId = any()) } throws AppActionException.Failure(message = failureMessage)

            val invalid =
                tester
                    .execute(name = WEB_ADD) {
                        setString(TITLE, Arb.nonBlankString().next())
                        setString("url", Arb.nonBlankString().next())
                        setStringList("headers", listOf("NoColon"))
                    }.shouldBeInstanceOf<ExecuteAppFunctionResponse.Error>()
                    .error
            val failure = tester.execute(name = WEB_RESTORE) { setString(WEB_ID, Arb.kotlinUuid().next().toString()) }.shouldBeInstanceOf<ExecuteAppFunctionResponse.Error>().error

            invalid.shouldBeInstanceOf<AppFunctionInvalidArgumentException>()
            invalid.errorMessage shouldBe invalidMessage
            failure.shouldBeInstanceOf<AppFunctionAppUnknownException>()
            failure.errorMessage shouldBe failureMessage
        }

    private fun appActionWeb(): AppActionWeb =
        AppActionWeb(
            id = Arb.kotlinUuid().next().toString(),
            title = Arb.string().next(),
            description = Arb.string().next(),
            url = Arb.string().next(),
            createdAt = Arb.string().next(),
            updatedAt = Arb.string().next(),
        )
}

private const val WEB_ID = "webId"
private const val WEB_ADD = "webAdd"
private const val WEB_GET = "webGet"
private const val WEB_LIST = "webList"
private const val WEB_MEMO_LIST = "webMemoList"
private const val WEB_RESTORE = "webRestore"
private const val WEB_TAG_ADD = "webTagAdd"
private const val WEB_TAG_REMOVE = "webTagRemove"
private const val WEB_UPDATE = "webUpdate"
private const val TITLE = "title"
