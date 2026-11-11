package io.github.taetae98coding.diary.work.appaction.appfunction

import android.content.Context
import androidx.appfunctions.AppFunctionAppUnknownException
import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.appfunctions.ExecuteAppFunctionResponse
import androidx.appfunctions.testing.AppFunctionTestRule
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.work.appaction.appfunction.checklist.AppFunctionChecklist
import io.github.taetae98coding.diary.work.appaction.appfunction.checklist.AppFunctionChecklistDetail
import io.github.taetae98coding.diary.work.appaction.appfunction.checklist.AppFunctionChecklistItem
import io.github.taetae98coding.diary.work.appaction.appfunction.checklist.AppFunctionChecklistSummary
import io.github.taetae98coding.diary.work.appaction.appfunction.checklist.AppFunctionChecklistSummaryPage
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionChangedCount
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionDone
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionId
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionServiceTester
import io.github.taetae98coding.diary.work.appaction.appfunction.core.expectedTag
import io.github.taetae98coding.diary.work.appaction.appfunction.core.returnValueAs
import io.github.taetae98coding.diary.work.appaction.checklist.AppActionChecklist
import io.github.taetae98coding.diary.work.appaction.checklist.AppActionChecklistDetail
import io.github.taetae98coding.diary.work.appaction.checklist.AppActionChecklistItem
import io.github.taetae98coding.diary.work.appaction.checklist.AppActionChecklistItemSortValue
import io.github.taetae98coding.diary.work.appaction.checklist.AppActionChecklistSummary
import io.github.taetae98coding.diary.work.appaction.checklist.ChecklistAppAction
import io.github.taetae98coding.diary.work.appaction.checklist.ChecklistItemAppAction
import io.github.taetae98coding.diary.work.appaction.core.AppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.AppActionDone
import io.github.taetae98coding.diary.work.appaction.core.AppActionException
import io.github.taetae98coding.diary.work.appaction.core.AppActionId
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.tag.AppActionEntityTagKind
import io.github.taetae98coding.diary.work.appaction.tag.EntityTagAppAction
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
class ChecklistAppFunctionServiceTest {
    private val context: Context = RuntimeEnvironment.getApplication()

    @get:Rule
    val appFunctionTestRule = AppFunctionTestRule(context)

    private val checklistAppAction = mockk<ChecklistAppAction>()
    private val entityTagAppAction = mockk<EntityTagAppAction>()
    private val checklistItemAppAction = mockk<ChecklistItemAppAction>()

    private lateinit var tester: AppFunctionServiceTester

    @Before
    fun setUp() {
        startKoin {
            modules(
                module {
                    single { checklistAppAction }
                    single { entityTagAppAction }
                    single { checklistItemAppAction }
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
    fun `체크리스트 동작을 모두 제공한다`() =
        runBlocking<Unit> {
            tester.functionNameList().filter { name -> name.startsWith("checklist") } shouldContainExactlyInAnyOrder
                listOf(
                    "checklistList",
                    "checklistGet",
                    "checklistAdd",
                    "checklistUpdate",
                    "checklistReset",
                    "checklistDelete",
                    "checklistRestore",
                    "checklistItemAdd",
                    "checklistItemCheck",
                    "checklistItemUncheck",
                    "checklistItemDelete",
                    CHECKLIST_TAG_ADD,
                    CHECKLIST_TAG_REMOVE,
                )
        }

    @Test
    fun `체크리스트 목록은 공통 동작의 페이지를 같은 값으로 돌려준다`() =
        runBlocking<Unit> {
            val summary =
                AppActionChecklistSummary(
                    id = Arb.kotlinUuid().next().toString(),
                    title = Arb.string().next(),
                    description = Arb.string().next(),
                    checkedCount = Arb.int(0..10).next(),
                    totalCount = Arb.int(10..20).next(),
                    createdAt = Arb.string().next(),
                    updatedAt = Arb.string().next(),
                )
            val cursor = Arb.int(1..1000).next().toString()
            coEvery { checklistAppAction.list(cursor = any()) } returns AppActionPage(items = listOf(summary), nextCursor = null)

            val response = tester.execute(name = "checklistList") { setString("cursor", cursor) }

            response.returnValueAs<AppFunctionChecklistSummaryPage>() shouldBe
                AppFunctionChecklistSummaryPage(
                    items =
                        listOf(
                            AppFunctionChecklistSummary(
                                id = summary.id,
                                title = summary.title,
                                description = summary.description,
                                checkedCount = summary.checkedCount,
                                totalCount = summary.totalCount,
                                createdAt = summary.createdAt,
                                updatedAt = summary.updatedAt,
                            ),
                        ),
                    nextCursor = null,
                )
            coVerify(exactly = 1) { checklistAppAction.list(cursor = cursor) }
        }

    @Test
    fun `TC-4ca7faaf-f740-4b6a-9c4b-33ac9b779292 체크리스트 조회는 항목 정렬을 넘기고 항목, 연결된 태그와 함께 돌려준다`() =
        runBlocking<Unit> {
            val checklist =
                AppActionChecklist(
                    id = Arb.kotlinUuid().next().toString(),
                    title = Arb.string().next(),
                    description = Arb.string().next(),
                    createdAt = Arb.string().next(),
                    updatedAt = Arb.string().next(),
                )
            val item =
                AppActionChecklistItem(
                    id = Arb.kotlinUuid().next().toString(),
                    content = Arb.string().next(),
                    isChecked = Arb.boolean().next(),
                    createdAt = Arb.string().next(),
                    updatedAt = Arb.string().next(),
                )
            val tag = Arb.appActionTag().next()
            coEvery { checklistAppAction.get(checklistId = any(), itemSort = any()) } returns AppActionChecklistDetail(checklist = checklist, items = listOf(item), tags = listOf(tag))

            val response =
                tester.execute(name = "checklistGet") {
                    setString(CHECKLIST_ID, checklist.id)
                    setString("itemSort", AppActionChecklistItemSortValue.UNCHECKED_FIRST)
                }

            response.returnValueAs<AppFunctionChecklistDetail>() shouldBe
                AppFunctionChecklistDetail(
                    checklist =
                        AppFunctionChecklist(
                            id = checklist.id,
                            title = checklist.title,
                            description = checklist.description,
                            createdAt = checklist.createdAt,
                            updatedAt = checklist.updatedAt,
                        ),
                    items =
                        listOf(
                            AppFunctionChecklistItem(
                                id = item.id,
                                content = item.content,
                                isChecked = item.isChecked,
                                createdAt = item.createdAt,
                                updatedAt = item.updatedAt,
                            ),
                        ),
                    tags = listOf(tag.expectedTag()),
                )
            coVerify(exactly = 1) { checklistAppAction.get(checklistId = checklist.id, itemSort = AppActionChecklistItemSortValue.UNCHECKED_FIRST) }
        }

    @Test
    fun `TC-edf2f510-aac0-4a31-afdb-2f9368763906 체크리스트 추가는 받은 값과 태그를 공통 동작에 넘기고 id를 돌려준다`() =
        runBlocking<Unit> {
            val title = Arb.nonBlankString().next()
            val itemList = listOf(Arb.nonBlankString().next(), Arb.nonBlankString().next())
            val tagIdList = listOf(Arb.kotlinUuid().next().toString(), Arb.kotlinUuid().next().toString())
            val addedId = Arb.kotlinUuid().next().toString()
            coEvery { checklistAppAction.add(title = any(), description = any(), items = any(), tagIds = any()) } returns AppActionId(id = addedId)

            val response =
                tester.execute(name = "checklistAdd") {
                    setString("title", title)
                    setStringList("items", itemList)
                    setStringList("tagIds", tagIdList)
                }

            response.returnValueAs<AppFunctionId>() shouldBe AppFunctionId(id = addedId)
            coVerify(exactly = 1) { checklistAppAction.add(title = title, description = null, items = itemList, tagIds = tagIdList) }
        }

    @Test
    fun `항목 추가는 체크리스트와 내용을 넘기고 추가한 수를 돌려준다`() =
        runBlocking<Unit> {
            val checklistId = Arb.kotlinUuid().next().toString()
            val content = Arb.nonBlankString().next()
            val changedCount = Arb.int(0..1).next()
            coEvery { checklistItemAppAction.add(checklistId = checklistId, content = content) } returns AppActionChangedCount(changedCount = changedCount)

            val response =
                tester.execute(name = "checklistItemAdd") {
                    setString(CHECKLIST_ID, checklistId)
                    setString("content", content)
                }

            response.returnValueAs<AppFunctionChangedCount>() shouldBe AppFunctionChangedCount(changedCount = changedCount)
        }

    @Test
    fun `리셋은 해제한 수를 돌려준다`() =
        runBlocking<Unit> {
            val checklistId = Arb.kotlinUuid().next().toString()
            val changedCount = Arb.int(0..10).next()
            coEvery { checklistAppAction.reset(checklistId = checklistId) } returns AppActionChangedCount(changedCount = changedCount)

            tester.execute(name = "checklistReset") { setString(CHECKLIST_ID, checklistId) }.returnValueAs<AppFunctionChangedCount>() shouldBe AppFunctionChangedCount(changedCount = changedCount)
        }

    @Test
    fun `TC-173de82e-308b-45d4-bc9b-14a00eb1c2fc 체크리스트 태그 연결과 해제는 완료를 돌려준다`() =
        runBlocking<Unit> {
            val checklistId = Arb.kotlinUuid().next().toString()
            val tagId = Arb.kotlinUuid().next().toString()
            coEvery { entityTagAppAction.add(kind = AppActionEntityTagKind.CHECKLIST, entityId = any(), tagId = any()) } returns AppActionDone(done = true)
            coEvery { entityTagAppAction.remove(kind = AppActionEntityTagKind.CHECKLIST, entityId = any(), tagId = any()) } returns AppActionDone(done = true)

            val addResponse =
                tester.execute(name = CHECKLIST_TAG_ADD) {
                    setString(CHECKLIST_ID, checklistId)
                    setString("tagId", tagId)
                }
            val removeResponse =
                tester.execute(name = CHECKLIST_TAG_REMOVE) {
                    setString(CHECKLIST_ID, checklistId)
                    setString("tagId", tagId)
                }

            addResponse.returnValueAs<AppFunctionDone>() shouldBe AppFunctionDone(done = true)
            removeResponse.returnValueAs<AppFunctionDone>() shouldBe AppFunctionDone(done = true)
            coVerify(exactly = 1) { entityTagAppAction.add(kind = AppActionEntityTagKind.CHECKLIST, entityId = checklistId, tagId = tagId) }
            coVerify(exactly = 1) { entityTagAppAction.remove(kind = AppActionEntityTagKind.CHECKLIST, entityId = checklistId, tagId = tagId) }
        }

    @Test
    fun `인자 오류는 잘못된 인자 실패로 같은 메시지를 알린다`() =
        runBlocking<Unit> {
            val message = Arb.nonBlankString().next()
            coEvery { checklistItemAppAction.check(itemId = any()) } throws AppActionException.InvalidArgument(message = message)

            val error = tester.execute(name = "checklistItemCheck") { setString("itemId", "not-id") }.shouldBeInstanceOf<ExecuteAppFunctionResponse.Error>().error

            error.shouldBeInstanceOf<AppFunctionInvalidArgumentException>()
            error.errorMessage shouldBe message
        }

    @Test
    fun `앱 기능 실패는 앱 실패로 같은 메시지를 알린다`() =
        runBlocking<Unit> {
            val message = Arb.nonBlankString().next()
            coEvery { checklistAppAction.update(checklistId = any(), title = any(), description = any()) } throws AppActionException.Failure(message = message)

            val response =
                tester.execute(name = "checklistUpdate") {
                    setString(CHECKLIST_ID, Arb.kotlinUuid().next().toString())
                    setString("title", Arb.nonBlankString().next())
                }

            val error = response.shouldBeInstanceOf<ExecuteAppFunctionResponse.Error>().error
            error.shouldBeInstanceOf<AppFunctionAppUnknownException>()
            error.errorMessage shouldBe message
            coVerify(exactly = 1) { checklistAppAction.update(checklistId = any(), title = any(), description = null) }
        }
}

private const val CHECKLIST_TAG_ADD = "checklistTagAdd"
private const val CHECKLIST_TAG_REMOVE = "checklistTagRemove"
private const val CHECKLIST_ID = "checklistId"
