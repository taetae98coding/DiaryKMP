package io.github.taetae98coding.diary.work.appaction.appfunction

import android.content.Context
import androidx.appfunctions.AppFunctionAppUnknownException
import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.appfunctions.ExecuteAppFunctionResponse
import androidx.appfunctions.testing.AppFunctionTestRule
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.work.appaction.appfunction.contact.AppFunctionContactBirthday
import io.github.taetae98coding.diary.work.appaction.appfunction.contact.AppFunctionContactDetail
import io.github.taetae98coding.diary.work.appaction.appfunction.contact.AppFunctionContactDetailWithTags
import io.github.taetae98coding.diary.work.appaction.appfunction.contact.AppFunctionContactPage
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionChangedCount
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionDone
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionId
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionServiceTester
import io.github.taetae98coding.diary.work.appaction.appfunction.core.expectedTag
import io.github.taetae98coding.diary.work.appaction.appfunction.core.returnValueAs
import io.github.taetae98coding.diary.work.appaction.appfunction.item.AppFunctionContact
import io.github.taetae98coding.diary.work.appaction.appfunction.memo.AppFunctionMemoPage
import io.github.taetae98coding.diary.work.appaction.contact.AppActionContact
import io.github.taetae98coding.diary.work.appaction.contact.AppActionContactBirthday
import io.github.taetae98coding.diary.work.appaction.contact.AppActionContactDetail
import io.github.taetae98coding.diary.work.appaction.contact.AppActionContactDetailWithTags
import io.github.taetae98coding.diary.work.appaction.contact.AppActionContactUpdateInput
import io.github.taetae98coding.diary.work.appaction.contact.ContactAppAction
import io.github.taetae98coding.diary.work.appaction.core.AppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.AppActionDone
import io.github.taetae98coding.diary.work.appaction.core.AppActionException
import io.github.taetae98coding.diary.work.appaction.core.AppActionId
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.core.AppActionSortValue
import io.github.taetae98coding.diary.work.appaction.memo.MemoListAppAction
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
class ContactAppFunctionServiceTest {
    private val context: Context = RuntimeEnvironment.getApplication()

    @get:Rule
    val appFunctionTestRule = AppFunctionTestRule(context)

    private val contactAppAction = mockk<ContactAppAction>()
    private val entityTagAppAction = mockk<EntityTagAppAction>()
    private val memoListAppAction = mockk<MemoListAppAction>()

    private lateinit var tester: AppFunctionServiceTester

    @Before
    fun setUp() {
        startKoin {
            modules(
                module {
                    single { contactAppAction }
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
    fun `연락처 동작을 모두 제공한다`() =
        runBlocking<Unit> {
            tester.functionNameList().filter { name -> name.startsWith("contact") } shouldContainExactlyInAnyOrder
                listOf(
                    "contactList",
                    CONTACT_GET,
                    "contactMemoList",
                    "contactMemoFinishedList",
                    "contactAdd",
                    CONTACT_UPDATE,
                    "contactFavorite",
                    "contactUnfavorite",
                    "contactDelete",
                    "contactRestore",
                    CONTACT_TAG_ADD,
                    CONTACT_TAG_REMOVE,
                )
        }

    @Test
    fun `연락처 목록은 공통 동작의 페이지를 같은 값으로 돌려준다`() =
        runBlocking<Unit> {
            val contact =
                AppActionContact(
                    id = Arb.kotlinUuid().next().toString(),
                    name = Arb.string().next(),
                    nameQualifier = Arb.string().next(),
                    description = Arb.string().next(),
                    isFavorite = Arb.boolean().next(),
                    createdAt = Arb.string().next(),
                    updatedAt = Arb.string().next(),
                )
            val nextCursor = Arb.int(1..1000).next().toString()
            coEvery { contactAppAction.list(sort = any(), cursor = any()) } returns AppActionPage(items = listOf(contact), nextCursor = nextCursor)

            val response = tester.execute(name = "contactList") { setString("sort", AppActionSortValue.RECENTLY_UPDATED) }

            response.returnValueAs<AppFunctionContactPage>() shouldBe
                AppFunctionContactPage(
                    items =
                        listOf(
                            AppFunctionContact(
                                id = contact.id,
                                name = contact.name,
                                nameQualifier = contact.nameQualifier,
                                description = contact.description,
                                isFavorite = contact.isFavorite,
                                createdAt = contact.createdAt,
                                updatedAt = contact.updatedAt,
                            ),
                        ),
                    nextCursor = nextCursor,
                )
            coVerify(exactly = 1) { contactAppAction.list(sort = AppActionSortValue.RECENTLY_UPDATED, cursor = null) }
        }

    @Test
    fun `TC-4ca7faaf-f740-4b6a-9c4b-33ac9b779292 연락처 조회는 모든 정보를 연결된 태그와 함께 돌려주고 없으면 값이 없는 결과를 돌려준다`() =
        runBlocking<Unit> {
            val birthday = Arb.kotlinxLocalDate().next().toString()
            val detail =
                AppActionContactDetail(
                    id = Arb.kotlinUuid().next().toString(),
                    name = Arb.string().next(),
                    description = Arb.string().next(),
                    heightCm = Arb.numericDouble(min = 0.0, max = 999.9).next(),
                    footSizeMm = Arb.int(1..999).next(),
                    birthday = AppActionContactBirthday(date = birthday, calendar = LUNAR),
                    birthYear = Arb.int(1900..2100).next(),
                    hometown = Arb.string().next(),
                    howWeMet = Arb.string().next(),
                    phoneNumbers = listOf(Arb.nonBlankString().next()),
                    isFavorite = Arb.boolean().next(),
                    createdAt = Arb.string().next(),
                    updatedAt = Arb.string().next(),
                )
            val tag = Arb.appActionTag().next()
            val missingId = Arb.kotlinUuid().next().toString()
            coEvery { contactAppAction.get(contactId = detail.id) } returns AppActionContactDetailWithTags(contact = detail, tags = listOf(tag))
            coEvery { contactAppAction.get(contactId = missingId) } returns null

            tester.execute(name = CONTACT_GET) { setString(CONTACT_ID, detail.id) }.returnValueAs<AppFunctionContactDetailWithTags>() shouldBe
                AppFunctionContactDetailWithTags(
                    contact =
                        AppFunctionContactDetail(
                            id = detail.id,
                            name = detail.name,
                            description = detail.description,
                            heightCm = detail.heightCm,
                            footSizeMm = detail.footSizeMm,
                            birthday = AppFunctionContactBirthday(date = birthday, calendar = LUNAR),
                            birthYear = detail.birthYear,
                            hometown = detail.hometown,
                            howWeMet = detail.howWeMet,
                            phoneNumbers = detail.phoneNumbers,
                            isFavorite = detail.isFavorite,
                            createdAt = detail.createdAt,
                            updatedAt = detail.updatedAt,
                        ),
                    tags = listOf(tag.expectedTag()),
                )
            tester
                .execute(name = CONTACT_GET) { setString(CONTACT_ID, missingId) }
                .shouldBeInstanceOf<ExecuteAppFunctionResponse.Success>()
                .returnValue
                .containsKey(ExecuteAppFunctionResponse.Success.PROPERTY_RETURN_VALUE) shouldBe false
        }

    @Test
    fun `연락처의 메모 목록은 연락처와 정렬을 공통 동작에 넘긴다`() =
        runBlocking<Unit> {
            val contactId = Arb.kotlinUuid().next().toString()
            coEvery { memoListAppAction.contactFinishedList(contactId = any(), sort = any(), cursor = any()) } returns AppActionPage(items = emptyList(), nextCursor = null)

            val response =
                tester.execute(name = "contactMemoFinishedList") {
                    setString(CONTACT_ID, contactId)
                    setString("sort", AppActionSortValue.TITLE)
                }

            response.returnValueAs<AppFunctionMemoPage>() shouldBe AppFunctionMemoPage(items = emptyList(), nextCursor = null)
            coVerify(exactly = 1) { memoListAppAction.contactFinishedList(contactId = contactId, sort = AppActionSortValue.TITLE, cursor = null) }
        }

    @Test
    fun `TC-edf2f510-aac0-4a31-afdb-2f9368763906 연락처 추가는 받은 값과 태그를 공통 동작에 넘기고 id를 돌려준다`() =
        runBlocking<Unit> {
            val birthday = Arb.kotlinxLocalDate().next().toString()
            val heightCm = Arb.int(0..9999).next().let { tenth -> "${tenth / 10}.${tenth % 10}" }
            val footSizeMm = Arb.int(1..999).next()
            val name = Arb.nonBlankString().next()
            val phoneNumber = Arb.nonBlankString().next()
            val tagIdList = listOf(Arb.kotlinUuid().next().toString(), Arb.kotlinUuid().next().toString())
            val addedId = Arb.kotlinUuid().next().toString()
            coEvery { contactAppAction.add(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns AppActionId(id = addedId)

            val response =
                tester.execute(name = "contactAdd") {
                    setString("name", name)
                    setString("heightCm", heightCm)
                    setInt("footSizeMm", footSizeMm)
                    setString("birthday", birthday)
                    setString("birthdayCalendar", LUNAR)
                    setStringList("phoneNumbers", listOf(phoneNumber))
                    setStringList("tagIds", tagIdList)
                }

            response.returnValueAs<AppFunctionId>() shouldBe AppFunctionId(id = addedId)
            coVerify(exactly = 1) {
                contactAppAction.add(
                    name = name,
                    description = null,
                    heightCm = heightCm,
                    footSizeMm = footSizeMm,
                    birthday = birthday,
                    birthdayCalendar = LUNAR,
                    birthYear = null,
                    hometown = null,
                    howWeMet = null,
                    phoneNumbers = listOf(phoneNumber),
                    tagIds = tagIdList,
                )
            }
        }

    @Test
    fun `연락처 수정은 담지 않은 전화번호와 빈 전화번호 목록을 구분해 넘긴다`() =
        runBlocking<Unit> {
            val contactId = Arb.kotlinUuid().next().toString()
            val changedCount = Arb.int(0..1).next()
            coEvery { contactAppAction.update(input = any()) } returns AppActionChangedCount(changedCount = changedCount)

            val omitted =
                tester.execute(name = CONTACT_UPDATE) {
                    setString(CONTACT_ID, contactId)
                    setBoolean("clearBirthday", true)
                }
            tester.execute(name = CONTACT_UPDATE) {
                setString(CONTACT_ID, contactId)
                setStringList("phoneNumbers", emptyList())
            }

            omitted.returnValueAs<AppFunctionChangedCount>() shouldBe AppFunctionChangedCount(changedCount = changedCount)
            coVerify(exactly = 1) { contactAppAction.update(input = AppActionContactUpdateInput(contactId = contactId, clearBirthday = true)) }
            coVerify(exactly = 1) { contactAppAction.update(input = AppActionContactUpdateInput(contactId = contactId, phoneNumbers = emptyList())) }
        }

    @Test
    fun `즐겨찾기는 바뀐 수를 돌려준다`() =
        runBlocking<Unit> {
            val contactId = Arb.kotlinUuid().next().toString()
            val changedCount = Arb.int(0..1).next()
            coEvery { contactAppAction.favorite(contactId = contactId) } returns AppActionChangedCount(changedCount = changedCount)

            tester.execute(name = "contactFavorite") { setString(CONTACT_ID, contactId) }.returnValueAs<AppFunctionChangedCount>() shouldBe AppFunctionChangedCount(changedCount = changedCount)
        }

    @Test
    fun `TC-173de82e-308b-45d4-bc9b-14a00eb1c2fc 연락처 태그 연결과 해제는 완료를 돌려준다`() =
        runBlocking<Unit> {
            val contactId = Arb.kotlinUuid().next().toString()
            val tagId = Arb.kotlinUuid().next().toString()
            coEvery { entityTagAppAction.add(kind = AppActionEntityTagKind.CONTACT, entityId = any(), tagId = any()) } returns AppActionDone(done = true)
            coEvery { entityTagAppAction.remove(kind = AppActionEntityTagKind.CONTACT, entityId = any(), tagId = any()) } returns AppActionDone(done = true)

            val addResponse =
                tester.execute(name = CONTACT_TAG_ADD) {
                    setString(CONTACT_ID, contactId)
                    setString("tagId", tagId)
                }
            val removeResponse =
                tester.execute(name = CONTACT_TAG_REMOVE) {
                    setString(CONTACT_ID, contactId)
                    setString("tagId", tagId)
                }

            addResponse.returnValueAs<AppFunctionDone>() shouldBe AppFunctionDone(done = true)
            removeResponse.returnValueAs<AppFunctionDone>() shouldBe AppFunctionDone(done = true)
            coVerify(exactly = 1) { entityTagAppAction.add(kind = AppActionEntityTagKind.CONTACT, entityId = contactId, tagId = tagId) }
            coVerify(exactly = 1) { entityTagAppAction.remove(kind = AppActionEntityTagKind.CONTACT, entityId = contactId, tagId = tagId) }
        }

    @Test
    fun `인자 오류는 잘못된 인자 실패로 같은 메시지를 알린다`() =
        runBlocking<Unit> {
            val message = Arb.nonBlankString().next()
            coEvery { contactAppAction.delete(contactId = any()) } throws AppActionException.InvalidArgument(message = message)

            val error = tester.execute(name = "contactDelete") { setString(CONTACT_ID, "not-id") }.shouldBeInstanceOf<ExecuteAppFunctionResponse.Error>().error

            error.shouldBeInstanceOf<AppFunctionInvalidArgumentException>()
            error.errorMessage shouldBe message
        }

    @Test
    fun `앱 기능 실패는 앱 실패로 같은 메시지를 알린다`() =
        runBlocking<Unit> {
            val message = Arb.nonBlankString().next()
            coEvery { contactAppAction.restore(contactId = any()) } throws AppActionException.Failure(message = message)

            val error = tester.execute(name = "contactRestore") { setString(CONTACT_ID, Arb.kotlinUuid().next().toString()) }.shouldBeInstanceOf<ExecuteAppFunctionResponse.Error>().error

            error.shouldBeInstanceOf<AppFunctionAppUnknownException>()
            error.errorMessage shouldBe message
        }
}

private const val CONTACT_TAG_ADD = "contactTagAdd"
private const val CONTACT_TAG_REMOVE = "contactTagRemove"
private const val CONTACT_ID = "contactId"
private const val CONTACT_GET = "contactGet"
private const val LUNAR = "lunar"
private const val CONTACT_UPDATE = "contactUpdate"
