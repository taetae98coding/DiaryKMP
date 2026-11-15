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
import io.github.taetae98coding.diary.work.appaction.appfunction.qr.AppFunctionQr
import io.github.taetae98coding.diary.work.appaction.appfunction.qr.AppFunctionQrDetail
import io.github.taetae98coding.diary.work.appaction.appfunction.qr.AppFunctionQrPage
import io.github.taetae98coding.diary.work.appaction.core.AppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.AppActionDone
import io.github.taetae98coding.diary.work.appaction.core.AppActionException
import io.github.taetae98coding.diary.work.appaction.core.AppActionId
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.qr.AppActionQr
import io.github.taetae98coding.diary.work.appaction.qr.AppActionQrDetail
import io.github.taetae98coding.diary.work.appaction.qr.QrAppAction
import io.github.taetae98coding.diary.work.appaction.tag.AppActionEntityTagKind
import io.github.taetae98coding.diary.work.appaction.tag.EntityTagAppAction
import io.github.taetae98coding.diary.work.appaction.tag.appActionTag
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
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
class QrAppFunctionServiceTest {
    private val context: Context = RuntimeEnvironment.getApplication()

    @get:Rule
    val appFunctionTestRule = AppFunctionTestRule(context)

    private val qrAppAction = mockk<QrAppAction>()
    private val entityTagAppAction = mockk<EntityTagAppAction>()

    private lateinit var tester: AppFunctionServiceTester

    @Before
    fun setUp() {
        startKoin {
            modules(
                module {
                    single { qrAppAction }
                    single { entityTagAppAction }
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
    fun `QR 동작을 모두 제공한다`() =
        runBlocking<Unit> {
            tester.functionNameList().filter { name -> name.startsWith("qr") } shouldContainExactlyInAnyOrder
                listOf(QR_LIST, QR_GET, QR_ADD, QR_UPDATE, QR_DELETE, QR_RESTORE, QR_TAG_ADD, QR_TAG_REMOVE)
        }

    @Test
    fun `QR 목록은 공통 동작의 페이지를 같은 값으로 돌려준다`() =
        runBlocking<Unit> {
            val qr = arbAppActionQr()
            val cursor = Arb.int(1..1000).next().toString()
            val nextCursor = Arb.int(1..1000).next().toString()
            coEvery { qrAppAction.list(cursor = any()) } returns AppActionPage(items = listOf(qr), nextCursor = nextCursor)

            val response = tester.execute(name = QR_LIST) { setString("cursor", cursor) }

            response.returnValueAs<AppFunctionQrPage>() shouldBe AppFunctionQrPage(items = listOf(qr.expected()), nextCursor = nextCursor)
            coVerify(exactly = 1) { qrAppAction.list(cursor = cursor) }
        }

    @Test
    fun `TC-4ca7faaf-f740-4b6a-9c4b-33ac9b779292 QR 조회는 연결된 태그와 함께 돌려주고 없으면 값이 없는 결과를 돌려준다`() =
        runBlocking<Unit> {
            val qr = arbAppActionQr()
            val tag = Arb.appActionTag().next()
            val missingId = Arb.kotlinUuid().next().toString()
            coEvery { qrAppAction.get(qrId = qr.id) } returns AppActionQrDetail(qr = qr, tags = listOf(tag))
            coEvery { qrAppAction.get(qrId = missingId) } returns null

            tester.execute(name = QR_GET) { setString(QR_ID, qr.id) }.returnValueAs<AppFunctionQrDetail>() shouldBe AppFunctionQrDetail(qr = qr.expected(), tags = listOf(tag.expectedTag()))
            tester
                .execute(name = QR_GET) { setString(QR_ID, missingId) }
                .shouldBeInstanceOf<ExecuteAppFunctionResponse.Success>()
                .returnValue
                .containsKey(ExecuteAppFunctionResponse.Success.PROPERTY_RETURN_VALUE) shouldBe false
        }

    @Test
    fun `TC-edf2f510-aac0-4a31-afdb-2f9368763906 QR 추가는 받은 값과 태그를 공통 동작에 넘기고 id를 돌려준다`() =
        runBlocking<Unit> {
            val title = Arb.nonBlankString().next()
            val value = Arb.nonBlankString().next()
            val tagIdList = listOf(Arb.kotlinUuid().next().toString(), Arb.kotlinUuid().next().toString())
            val addedId = Arb.kotlinUuid().next().toString()
            coEvery { qrAppAction.add(title = any(), description = any(), value = any(), tagIds = any()) } returns AppActionId(id = addedId)

            val response =
                tester.execute(name = QR_ADD) {
                    setString("title", title)
                    setString("value", value)
                    setStringList("tagIds", tagIdList)
                }

            response.returnValueAs<AppFunctionId>() shouldBe AppFunctionId(id = addedId)
            coVerify(exactly = 1) { qrAppAction.add(title = title, description = null, value = value, tagIds = tagIdList) }
        }

    @Test
    fun `QR 수정, 삭제, 되돌리기는 바뀐 수를 돌려준다`() =
        runBlocking<Unit> {
            val qrId = Arb.kotlinUuid().next().toString()
            val description = Arb.string().next()
            val changedCount = Arb.int(0..1).next()
            coEvery { qrAppAction.update(qrId = any(), title = any(), description = any(), value = any()) } returns AppActionChangedCount(changedCount = changedCount)
            coEvery { qrAppAction.delete(qrId = qrId) } returns AppActionChangedCount(changedCount = changedCount)
            coEvery { qrAppAction.restore(qrId = qrId) } returns AppActionChangedCount(changedCount = changedCount)
            val expected = AppFunctionChangedCount(changedCount = changedCount)

            tester
                .execute(name = QR_UPDATE) {
                    setString(QR_ID, qrId)
                    setString("description", description)
                }.returnValueAs<AppFunctionChangedCount>() shouldBe expected
            tester.execute(name = QR_DELETE) { setString(QR_ID, qrId) }.returnValueAs<AppFunctionChangedCount>() shouldBe expected
            tester.execute(name = QR_RESTORE) { setString(QR_ID, qrId) }.returnValueAs<AppFunctionChangedCount>() shouldBe expected
            coVerify(exactly = 1) { qrAppAction.update(qrId = qrId, title = null, description = description, value = null) }
        }

    @Test
    fun `TC-173de82e-308b-45d4-bc9b-14a00eb1c2fc QR 태그 연결과 해제는 완료를 돌려준다`() =
        runBlocking<Unit> {
            val qrId = Arb.kotlinUuid().next().toString()
            val tagId = Arb.kotlinUuid().next().toString()
            coEvery { entityTagAppAction.add(kind = AppActionEntityTagKind.QR, entityId = any(), tagId = any()) } returns AppActionDone(done = true)
            coEvery { entityTagAppAction.remove(kind = AppActionEntityTagKind.QR, entityId = any(), tagId = any()) } returns AppActionDone(done = true)

            val addResponse =
                tester.execute(name = QR_TAG_ADD) {
                    setString(QR_ID, qrId)
                    setString("tagId", tagId)
                }
            val removeResponse =
                tester.execute(name = QR_TAG_REMOVE) {
                    setString(QR_ID, qrId)
                    setString("tagId", tagId)
                }

            addResponse.returnValueAs<AppFunctionDone>() shouldBe AppFunctionDone(done = true)
            removeResponse.returnValueAs<AppFunctionDone>() shouldBe AppFunctionDone(done = true)
            coVerify(exactly = 1) { entityTagAppAction.add(kind = AppActionEntityTagKind.QR, entityId = qrId, tagId = tagId) }
            coVerify(exactly = 1) { entityTagAppAction.remove(kind = AppActionEntityTagKind.QR, entityId = qrId, tagId = tagId) }
        }

    @Test
    fun `인자 오류는 잘못된 인자 실패로, 앱 기능 실패는 앱 실패로 같은 메시지를 알린다`() =
        runBlocking<Unit> {
            val invalidMessage = Arb.nonBlankString().next()
            val failureMessage = Arb.nonBlankString().next()
            coEvery { qrAppAction.delete(qrId = any()) } throws AppActionException.InvalidArgument(message = invalidMessage)
            coEvery { qrAppAction.restore(qrId = any()) } throws AppActionException.Failure(message = failureMessage)

            val invalidError = tester.execute(name = QR_DELETE) { setString(QR_ID, "not-id") }.shouldBeInstanceOf<ExecuteAppFunctionResponse.Error>().error
            val failureError = tester.execute(name = QR_RESTORE) { setString(QR_ID, Arb.kotlinUuid().next().toString()) }.shouldBeInstanceOf<ExecuteAppFunctionResponse.Error>().error

            invalidError.shouldBeInstanceOf<AppFunctionInvalidArgumentException>()
            invalidError.errorMessage shouldBe invalidMessage
            failureError.shouldBeInstanceOf<AppFunctionAppUnknownException>()
            failureError.errorMessage shouldBe failureMessage
        }
}

private const val QR_LIST = "qrList"
private const val QR_GET = "qrGet"
private const val QR_ADD = "qrAdd"
private const val QR_UPDATE = "qrUpdate"
private const val QR_DELETE = "qrDelete"
private const val QR_RESTORE = "qrRestore"
private const val QR_TAG_ADD = "qrTagAdd"
private const val QR_TAG_REMOVE = "qrTagRemove"
private const val QR_ID = "qrId"

private fun arbAppActionQr(): AppActionQr =
    AppActionQr(
        id = Arb.kotlinUuid().next().toString(),
        title = Arb.nonBlankString().next(),
        description = Arb.string().next(),
        value = Arb.nonBlankString().next(),
        createdAt = Arb.string().next(),
        updatedAt = Arb.string().next(),
    )

private fun AppActionQr.expected(): AppFunctionQr =
    AppFunctionQr(
        id = id,
        title = title,
        description = description,
        value = value,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
