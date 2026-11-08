package io.github.taetae98coding.diary.work.appaction.appfunction.memolink

import android.content.Context
import androidx.appfunctions.AppFunctionAppUnknownException
import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.appfunctions.ExecuteAppFunctionResponse
import androidx.appfunctions.testing.AppFunctionTestRule
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionDone
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionServiceTester
import io.github.taetae98coding.diary.work.appaction.appfunction.core.returnValueAs
import io.github.taetae98coding.diary.work.appaction.core.AppActionDone
import io.github.taetae98coding.diary.work.appaction.core.AppActionException
import io.github.taetae98coding.diary.work.appaction.memolink.MemoLinkAppAction
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
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
class MemoLinkAppFunctionServiceTest {
    private val context: Context = RuntimeEnvironment.getApplication()

    @get:Rule
    val appFunctionTestRule = AppFunctionTestRule(context)

    private val memoLinkAppAction = mockk<MemoLinkAppAction>()

    private lateinit var tester: AppFunctionServiceTester

    @Before
    fun setUp() {
        startKoin { modules(module { single { memoLinkAppAction } }) }
        tester = AppFunctionServiceTester(context = context, appFunctionManager = appFunctionTestRule.getAppFunctionManager())
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `메모 연결 동작을 모두 제공한다`() =
        runBlocking<Unit> {
            tester.functionNameList() shouldContainAll
                listOf(
                    "memoTagAdd",
                    "memoTagRemove",
                    "memoPrimaryTagSet",
                    "memoPrimaryTagUnset",
                    "memoPlaceAdd",
                    "memoPlaceRemove",
                    "memoWebAdd",
                    "memoWebRemove",
                    "memoContactAdd",
                    "memoContactRemove",
                )
        }

    @Test
    fun `메모 태그 연결은 받은 id를 공통 동작에 넘기고 완료 결과를 돌려준다`() =
        runBlocking<Unit> {
            val memoId = Arb.kotlinUuid().next().toString()
            val tagId = Arb.kotlinUuid().next().toString()
            coEvery { memoLinkAppAction.tagAdd(memoId = any(), tagId = any()) } returns AppActionDone(done = true)

            val response =
                tester.execute(name = "memoTagAdd") {
                    setString(MEMO_ID, memoId)
                    setString("tagId", tagId)
                }

            response.returnValueAs<AppFunctionDone>() shouldBe AppFunctionDone(done = true)
            coVerify(exactly = 1) { memoLinkAppAction.tagAdd(memoId = memoId, tagId = tagId) }
        }

    @Test
    fun `대표 태그 해제는 메모 id만 받아 완료 결과를 돌려준다`() =
        runBlocking<Unit> {
            val memoId = Arb.kotlinUuid().next().toString()
            coEvery { memoLinkAppAction.primaryTagUnset(memoId = any()) } returns AppActionDone(done = true)

            val response = tester.execute(name = "memoPrimaryTagUnset") { setString(MEMO_ID, memoId) }

            response.returnValueAs<AppFunctionDone>() shouldBe AppFunctionDone(done = true)
            coVerify(exactly = 1) { memoLinkAppAction.primaryTagUnset(memoId = memoId) }
        }

    @Test
    fun `메모 연락처 해제는 받은 id를 공통 동작에 넘긴다`() =
        runBlocking<Unit> {
            val memoId = Arb.kotlinUuid().next().toString()
            val contactId = Arb.kotlinUuid().next().toString()
            coEvery { memoLinkAppAction.contactRemove(memoId = any(), contactId = any()) } returns AppActionDone(done = true)

            val response =
                tester.execute(name = "memoContactRemove") {
                    setString(MEMO_ID, memoId)
                    setString("contactId", contactId)
                }

            response.returnValueAs<AppFunctionDone>() shouldBe AppFunctionDone(done = true)
            coVerify(exactly = 1) { memoLinkAppAction.contactRemove(memoId = memoId, contactId = contactId) }
        }

    @Test
    fun `인자 오류는 잘못된 인자 실패로 같은 메시지를 알린다`() =
        runBlocking<Unit> {
            val message = Arb.nonBlankString().next()
            coEvery { memoLinkAppAction.placeAdd(memoId = any(), placeId = any()) } throws AppActionException.InvalidArgument(message = message)

            val response =
                tester.execute(name = "memoPlaceAdd") {
                    setString(MEMO_ID, "not-id")
                    setString("placeId", "not-id")
                }

            val error = response.shouldBeInstanceOf<ExecuteAppFunctionResponse.Error>().error
            error.shouldBeInstanceOf<AppFunctionInvalidArgumentException>()
            error.errorMessage shouldBe message
        }

    @Test
    fun `앱 기능 실패는 앱 실패로 같은 메시지를 알린다`() =
        runBlocking<Unit> {
            val message = Arb.nonBlankString().next()
            coEvery { memoLinkAppAction.webRemove(memoId = any(), webId = any()) } throws AppActionException.Failure(message = message)

            val response =
                tester.execute(name = "memoWebRemove") {
                    setString(MEMO_ID, Arb.kotlinUuid().next().toString())
                    setString("webId", Arb.kotlinUuid().next().toString())
                }

            val error = response.shouldBeInstanceOf<ExecuteAppFunctionResponse.Error>().error
            error.shouldBeInstanceOf<AppFunctionAppUnknownException>()
            error.errorMessage shouldBe message
        }
}

private const val MEMO_ID = "memoId"
