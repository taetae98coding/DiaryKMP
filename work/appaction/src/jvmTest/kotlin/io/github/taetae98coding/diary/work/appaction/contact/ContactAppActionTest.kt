package io.github.taetae98coding.diary.work.appaction.contact

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.core.model.contact.ContactFilter
import io.github.taetae98coding.diary.core.model.contact.ContactListItem
import io.github.taetae98coding.diary.core.model.contact.ContactPhoneNumber
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.measure.Length.Companion.centimeter
import io.github.taetae98coding.diary.core.model.measure.Length.Companion.millimeter
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.testing.contact.contact
import io.github.taetae98coding.diary.core.testing.contact.contactBirthday
import io.github.taetae98coding.diary.core.testing.memo.memo
import io.github.taetae98coding.diary.domain.contact.usecase.AddContactUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.DeleteContactUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.FavoriteContactUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.FindContactUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.PageContactUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.RestoreContactUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.UnfavoriteContactUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.UpdateContactUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageContactMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageFinishedContactMemoUseCase
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.work.appaction.core.AppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.AppActionException
import io.github.taetae98coding.diary.work.appaction.core.AppActionId
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.memo.AppActionMemo
import io.github.taetae98coding.diary.work.appaction.memo.AppActionMemoDateTime
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

class ContactAppActionTest :
    FunSpec({
        test("목록은 정렬을 담지 않으면 이름순으로, 담으면 그 정렬로 필터 없이 조회해 MCP와 같은 항목을 돌려준다") {
            runTest {
                val itemList = Arb.list(Arb.contact(), 1..3).next().map { contact -> ContactListItem(contact = contact, nameQualifier = Arb.string().next()) }
                val pageContactUseCase = mockk<PageContactUseCase>()
                every { pageContactUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(itemList)))
                val action = contactAppActionOf(pageContactUseCase)

                action.list(sort = null, cursor = null) shouldBe AppActionPage(items = itemList.map { item -> item.expected() }, nextCursor = null)
                action.list(sort = "recently_updated", cursor = null).items shouldBe itemList.map { item -> item.expected() }

                verify(exactly = 1) { pageContactUseCase(parameter = PageContactUseCase.Parameter(sort = ListSort.NAME, filter = ContactFilter())) }
                verify(exactly = 1) { pageContactUseCase(parameter = PageContactUseCase.Parameter(sort = ListSort.RECENTLY_UPDATED, filter = ContactFilter())) }
            }
        }

        test("목록이 제공하지 않는 정렬과 잘못된 페이지 위치는 앱 기능을 실행하지 않고 실패를 알린다") {
            runTest {
                val pageContactUseCase = mockk<PageContactUseCase>()
                val action = contactAppActionOf(pageContactUseCase)

                shouldThrow<AppActionException.InvalidArgument> { action.list(sort = "title", cursor = null) }.message shouldBe "'sort' must be one of name, recently_updated."
                shouldThrow<AppActionException.InvalidArgument> { action.list(sort = null, cursor = "next") }.message shouldBe "'cursor' must be a nextCursor value from a previous page."
                verify(exactly = 0) { pageContactUseCase(parameter = any()) }
            }
        }

        test("목록은 100개씩 나눠 다음 페이지 위치를 알리고, 그 위치로 다음 페이지를 받는다") {
            runTest {
                val itemList = Arb.list(Arb.contact(), 101..150).next().map { contact -> ContactListItem(contact = contact, nameQualifier = "") }
                val pageContactUseCase = mockk<PageContactUseCase>()
                every { pageContactUseCase(parameter = any()) } answers { flowOf(Result.success(PagingData.from(itemList))) }
                val action = contactAppActionOf(pageContactUseCase)

                val firstPage = action.list(sort = null, cursor = null)
                val secondPage = action.list(sort = null, cursor = firstPage.nextCursor)

                firstPage shouldBe AppActionPage(items = itemList.take(100).map { item -> item.expected() }, nextCursor = "100")
                secondPage shouldBe AppActionPage(items = itemList.drop(100).map { item -> item.expected() }, nextCursor = null)
            }
        }

        test("연락처 하나를 MCP와 같은 형식의 모든 정보로 돌려준다") {
            runTest {
                val heightMm = Arb.int(0..9999).next()
                val footSizeMm = Arb.int(0..999).next()
                val contact =
                    Arb.contact().next().let { contact ->
                        contact.copy(
                            detail =
                                contact.detail.copy(
                                    height = heightMm.millimeter,
                                    footSize = footSizeMm.millimeter,
                                    birthday = Arb.contactBirthday().next().copy(calendar = ContactBirthdayCalendar.LUNAR),
                                    phoneNumberList = listOf(ContactPhoneNumber(number = Arb.nonBlankString().next())),
                                ),
                        )
                    }
                val findContactUseCase = mockk<FindContactUseCase>()
                every { findContactUseCase(parameter = contact.id) } returns flowOf(Result.success(contact))
                val action = contactAppActionOf(findContactUseCase)

                action.get(contactId = contact.id.toString()) shouldBe
                    AppActionContactDetail(
                        id = contact.id.toString(),
                        name = contact.detail.name,
                        description = contact.detail.description,
                        heightCm = heightMm / 10.0,
                        footSizeMm = footSizeMm,
                        birthday =
                            AppActionContactBirthday(
                                date =
                                    contact.detail.birthday!!
                                        .date
                                        .toString(),
                                calendar = "lunar",
                            ),
                        birthYear = contact.detail.birthYear,
                        hometown = contact.detail.hometown,
                        howWeMet = contact.detail.howWeMet,
                        phoneNumbers = contact.detail.phoneNumberList.map { phoneNumber -> phoneNumber.number },
                        isFavorite = contact.isFavorite,
                        createdAt = contact.createdAt.toString(),
                        updatedAt = contact.updatedAt.toString(),
                    )
            }
        }

        test("정보가 비어 있는 연락처는 빈 값을 null로 돌려준다") {
            runTest {
                val contact = Arb.contact().next().let { contact -> contact.copy(detail = contact.detail.copy(height = null, footSize = null, birthday = null, birthYear = null)) }
                val findContactUseCase = mockk<FindContactUseCase>()
                every { findContactUseCase(parameter = contact.id) } returns flowOf(Result.success(contact))
                val action = contactAppActionOf(findContactUseCase)

                val detail = action.get(contactId = contact.id.toString())

                detail?.heightCm shouldBe null
                detail?.footSizeMm shouldBe null
                detail?.birthday shouldBe null
                detail?.birthYear shouldBe null
            }
        }

        test("없는 연락처를 조회하면 null을, id 형식이 아니면 앱 기능을 실행하지 않고 실패를 알린다") {
            runTest {
                val contactId = Arb.kotlinUuid().next()
                val findContactUseCase = mockk<FindContactUseCase>()
                every { findContactUseCase(parameter = contactId) } returns flowOf(Result.success(null))
                val action = contactAppActionOf(findContactUseCase)

                action.get(contactId = contactId.toString()) shouldBe null
                shouldThrow<AppActionException.InvalidArgument> { action.get(contactId = "not-id") }.message shouldBe "'contactId' must be an id."
                verify(exactly = 1) { findContactUseCase(parameter = any()) }
            }
        }

        test("연락처의 진행 중·완료 메모 목록은 연락처와 정렬로 조회한다") {
            runTest {
                val contactId = Arb.kotlinUuid().next()
                val memoList = Arb.list(Arb.memo(), 1..3).next()
                val pageContactMemoUseCase = mockk<PageContactMemoUseCase>()
                val pageFinishedContactMemoUseCase = mockk<PageFinishedContactMemoUseCase>()
                every { pageContactMemoUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(memoList)))
                every { pageFinishedContactMemoUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(memoList)))
                val action = contactAppActionOf(pageContactMemoUseCase, pageFinishedContactMemoUseCase)
                val expected = AppActionPage(items = memoList.map { memo -> memo.expected() }, nextCursor = null)

                action.memoList(contactId = contactId.toString(), sort = null, cursor = null) shouldBe expected
                action.memoFinishedList(contactId = contactId.toString(), sort = "title", cursor = null) shouldBe expected

                verify(exactly = 1) { pageContactMemoUseCase(parameter = PageContactMemoUseCase.Parameter(contactId = contactId, sort = ListSort.DEFAULT)) }
                verify(exactly = 1) { pageFinishedContactMemoUseCase(parameter = PageFinishedContactMemoUseCase.Parameter(contactId = contactId, sort = ListSort.TITLE)) }
            }
        }

        test("연락처의 메모 목록은 메모 목록의 정렬만 받는다") {
            runTest {
                val pageContactMemoUseCase = mockk<PageContactMemoUseCase>()
                val action = contactAppActionOf(pageContactMemoUseCase)

                shouldThrow<AppActionException.InvalidArgument> {
                    action.memoList(contactId = Arb.kotlinUuid().next().toString(), sort = "name", cursor = null)
                }.message shouldBe "'sort' must be one of default, title, recently_updated."
                verify(exactly = 0) { pageContactMemoUseCase(parameter = any()) }
            }
        }

        test("연락처를 추가하면 받은 값으로 즐겨찾기 없이 추가하고 추가한 연락처의 id를 돌려준다") {
            runTest {
                val name = Arb.nonBlankString().next()
                val description = Arb.string().next()
                val birthday = Arb.kotlinxLocalDate().next()
                val heightCm = Arb.int(0..9999).next().let { heightMm -> "${heightMm / 10}.${heightMm % 10}" }
                val footSizeMm = Arb.int(0..999).next()
                val birthYear = Arb.int(1900..2100).next()
                val hometown = Arb.string().next()
                val howWeMet = Arb.string().next()
                val phoneNumberList = Arb.list(Arb.nonBlankString(), 0..3).next()
                val addedId = Arb.kotlinUuid().next()
                val parameter = slot<AddContactUseCase.Parameter>()
                val addContactUseCase = mockk<AddContactUseCase>()
                coEvery { addContactUseCase(parameter = capture(parameter)) } returns Result.success(addedId)
                val action = contactAppActionOf(addContactUseCase)

                val result =
                    action.add(
                        name = name,
                        description = description,
                        heightCm = heightCm,
                        footSizeMm = footSizeMm,
                        birthday = birthday.toString(),
                        birthdayCalendar = "lunar",
                        birthYear = birthYear,
                        hometown = hometown,
                        howWeMet = howWeMet,
                        phoneNumbers = phoneNumberList,
                    )

                result shouldBe AppActionId(id = addedId.toString())
                parameter.captured shouldBe
                    AddContactUseCase.Parameter(
                        detail =
                            ContactDetail(
                                name = name,
                                description = description,
                                height = heightCm.toDouble().centimeter,
                                footSize = footSizeMm.millimeter,
                                birthday = ContactBirthday(date = birthday, calendar = ContactBirthdayCalendar.LUNAR),
                                birthYear = birthYear,
                                hometown = hometown,
                                howWeMet = howWeMet,
                                phoneNumberList = phoneNumberList.map { number -> ContactPhoneNumber(number = number) },
                            ),
                        tagIdSet = emptySet(),
                    )
            }
        }

        test("담지 않은 값은 빈 값으로, 달력 없이 담은 생일은 양력으로 추가한다") {
            runTest {
                val name = Arb.nonBlankString().next()
                val birthday = Arb.kotlinxLocalDate().next()
                val parameter = slot<AddContactUseCase.Parameter>()
                val addContactUseCase = mockk<AddContactUseCase>()
                coEvery { addContactUseCase(parameter = capture(parameter)) } returns Result.success(Arb.kotlinUuid().next())
                val action = contactAppActionOf(addContactUseCase)

                action.add(
                    name = name,
                    description = null,
                    heightCm = null,
                    footSizeMm = null,
                    birthday = birthday.toString(),
                    birthdayCalendar = null,
                    birthYear = null,
                    hometown = null,
                    howWeMet = null,
                    phoneNumbers = emptyList(),
                )

                parameter.captured.detail shouldBe ContactDetail.EMPTY.copy(name = name, birthday = ContactBirthday(date = birthday, calendar = ContactBirthdayCalendar.SOLAR))
            }
        }

        test("잘못된 키, 발 크기, 생일, 출생 연도는 앱 기능을 실행하지 않고 MCP와 같은 메시지로 실패를 알린다") {
            runTest {
                val date = Arb.kotlinxLocalDate().next().toString()
                val addContactUseCase = mockk<AddContactUseCase>()
                val action = contactAppActionOf(addContactUseCase)
                val caseList =
                    listOf(
                        ContactAddCase(heightCm = "172.55") to "'heightCm' must be centimeters with up to three digits and one decimal place, like \"172.5\".",
                        ContactAddCase(heightCm = "1000") to "'heightCm' must be centimeters with up to three digits and one decimal place, like \"172.5\".",
                        ContactAddCase(footSizeMm = -1) to "'footSizeMm' must be between 0 and 999.",
                        ContactAddCase(footSizeMm = 1000) to "'footSizeMm' must be between 0 and 999.",
                        ContactAddCase(birthYear = 10_000) to "'birthYear' must be between 0 and 9999.",
                        ContactAddCase(birthday = "1990") to "'birthday' must be a date like 2026-01-31.",
                        ContactAddCase(birthday = date, birthdayCalendar = "chinese") to "'birthdayCalendar' must be one of solar, lunar.",
                        ContactAddCase(birthdayCalendar = "lunar") to "'birthdayCalendar' needs 'birthday'.",
                    )

                caseList.forEach { (case, message) ->
                    shouldThrow<AppActionException.InvalidArgument> {
                        action.add(
                            name = Arb.nonBlankString().next(),
                            description = null,
                            heightCm = case.heightCm,
                            footSizeMm = case.footSizeMm,
                            birthday = case.birthday,
                            birthdayCalendar = case.birthdayCalendar,
                            birthYear = case.birthYear,
                            hometown = null,
                            howWeMet = null,
                            phoneNumbers = emptyList(),
                        )
                    }.message shouldBe message
                }
                coVerify(exactly = 0) { addContactUseCase(parameter = any()) }
            }
        }

        test("고칠 값을 담지 않은 항목은 지금 값을 유지하고 고친 수를 돌려준다") {
            runTest {
                val contact = Arb.contact().next()
                val name = Arb.nonBlankString().next()
                val changedCount = Arb.int(0..1).next()
                val findContactUseCase = mockk<FindContactUseCase>()
                val updateContactUseCase = mockk<UpdateContactUseCase>()
                every { findContactUseCase(parameter = contact.id) } returns flowOf(Result.success(contact))
                coEvery { updateContactUseCase(parameter = any()) } returns Result.success(changedCount)
                val action = contactAppActionOf(findContactUseCase, updateContactUseCase)

                action.update(input = AppActionContactUpdateInput(contactId = contact.id.toString(), name = name)) shouldBe AppActionChangedCount(changedCount = changedCount)
                coVerify(exactly = 1) { updateContactUseCase(parameter = UpdateContactUseCase.Parameter(id = contact.id, detail = contact.detail.copy(name = name))) }
            }
        }

        test("지우기를 요청한 값은 지우고, 빈 전화번호 목록은 모든 전화번호를 지운다") {
            runTest {
                val contact = Arb.contact().next().withBirthday()
                val findContactUseCase = mockk<FindContactUseCase>()
                val updateContactUseCase = mockk<UpdateContactUseCase>()
                every { findContactUseCase(parameter = contact.id) } returns flowOf(Result.success(contact))
                coEvery { updateContactUseCase(parameter = any()) } returns Result.success(1)
                val action = contactAppActionOf(findContactUseCase, updateContactUseCase)

                action.update(
                    input =
                        AppActionContactUpdateInput(
                            contactId = contact.id.toString(),
                            clearHeight = true,
                            clearFootSize = true,
                            clearBirthday = true,
                            phoneNumbers = emptyList(),
                        ),
                )

                coVerify(exactly = 1) {
                    updateContactUseCase(
                        parameter =
                            UpdateContactUseCase.Parameter(
                                id = contact.id,
                                detail = contact.detail.copy(height = null, footSize = null, birthday = null, birthYear = null, phoneNumberList = emptyList()),
                            ),
                    )
                }
            }
        }

        test("생일을 지워도 출생 연도를 담으면 그 연도를 남기고, 새 키·발 크기·전화번호는 그 값으로 바꾼다") {
            runTest {
                val contact = Arb.contact().next().withBirthday()
                val heightCm = Arb.int(0..999).next()
                val footSizeMm = Arb.int(0..999).next()
                val birthYear = Arb.int(1900..2100).next()
                val phoneNumberList = Arb.list(Arb.nonBlankString(), 1..3).next()
                val findContactUseCase = mockk<FindContactUseCase>()
                val updateContactUseCase = mockk<UpdateContactUseCase>()
                every { findContactUseCase(parameter = contact.id) } returns flowOf(Result.success(contact))
                coEvery { updateContactUseCase(parameter = any()) } returns Result.success(1)
                val action = contactAppActionOf(findContactUseCase, updateContactUseCase)

                action.update(
                    input =
                        AppActionContactUpdateInput(
                            contactId = contact.id.toString(),
                            heightCm = heightCm.toString(),
                            footSizeMm = footSizeMm,
                            clearBirthday = true,
                            birthYear = birthYear,
                            phoneNumbers = phoneNumberList,
                        ),
                )

                coVerify(exactly = 1) {
                    updateContactUseCase(
                        parameter =
                            UpdateContactUseCase.Parameter(
                                id = contact.id,
                                detail =
                                    contact.detail.copy(
                                        height = heightCm.toDouble().centimeter,
                                        footSize = footSizeMm.millimeter,
                                        birthday = null,
                                        birthYear = birthYear,
                                        phoneNumberList = phoneNumberList.map { number -> ContactPhoneNumber(number = number) },
                                    ),
                            ),
                    )
                }
            }
        }

        test("생일의 날짜만 바꾸면 달력을, 달력만 바꾸면 날짜를 유지한다") {
            runTest {
                val contact = Arb.contact().next().withBirthday()
                val currentBirthday = contact.detail.birthday!!
                val date = Arb.kotlinxLocalDate().next()
                val calendar = if (currentBirthday.calendar == ContactBirthdayCalendar.SOLAR) "lunar" else "solar"
                val findContactUseCase = mockk<FindContactUseCase>()
                val updateContactUseCase = mockk<UpdateContactUseCase>()
                every { findContactUseCase(parameter = contact.id) } returns flowOf(Result.success(contact))
                coEvery { updateContactUseCase(parameter = any()) } returns Result.success(1)
                val action = contactAppActionOf(findContactUseCase, updateContactUseCase)

                action.update(input = AppActionContactUpdateInput(contactId = contact.id.toString(), birthday = date.toString()))
                action.update(input = AppActionContactUpdateInput(contactId = contact.id.toString(), birthdayCalendar = calendar))

                coVerify(exactly = 1) {
                    updateContactUseCase(parameter = UpdateContactUseCase.Parameter(id = contact.id, detail = contact.detail.copy(birthday = currentBirthday.copy(date = date))))
                }
                coVerify(exactly = 1) {
                    updateContactUseCase(
                        parameter =
                            UpdateContactUseCase.Parameter(
                                id = contact.id,
                                detail = contact.detail.copy(birthday = currentBirthday.copy(calendar = ContactBirthdayCalendar.entries.single { entry -> entry != currentBirthday.calendar })),
                            ),
                    )
                }
            }
        }

        test("생일이 없는 연락처에 달력만 담으면 앱 기능을 실행하지 않고 실패를 알린다") {
            runTest {
                val contact = Arb.contact().next().let { contact -> contact.copy(detail = contact.detail.copy(birthday = null)) }
                val findContactUseCase = mockk<FindContactUseCase>()
                val updateContactUseCase = mockk<UpdateContactUseCase>()
                every { findContactUseCase(parameter = contact.id) } returns flowOf(Result.success(contact))
                val action = contactAppActionOf(findContactUseCase, updateContactUseCase)

                shouldThrow<AppActionException.InvalidArgument> {
                    action.update(input = AppActionContactUpdateInput(contactId = contact.id.toString(), birthdayCalendar = "lunar"))
                }.message shouldBe "'birthdayCalendar' needs 'birthday' when the contact has no birthday."
                coVerify(exactly = 0) { updateContactUseCase(parameter = any()) }
            }
        }

        test("없는 연락처를 고치면 고친 수 0을 돌려준다") {
            runTest {
                val contactId = Arb.kotlinUuid().next()
                val findContactUseCase = mockk<FindContactUseCase>()
                val updateContactUseCase = mockk<UpdateContactUseCase>()
                every { findContactUseCase(parameter = contactId) } returns flowOf(Result.success(null))
                val action = contactAppActionOf(findContactUseCase, updateContactUseCase)

                action.update(input = AppActionContactUpdateInput(contactId = contactId.toString(), name = Arb.nonBlankString().next())) shouldBe AppActionChangedCount(changedCount = 0)
                coVerify(exactly = 0) { updateContactUseCase(parameter = any()) }
            }
        }

        test("즐겨찾기·즐겨찾기 해제·삭제·되돌리기는 바뀐 수를 돌려준다") {
            runTest {
                val contactId = Arb.kotlinUuid().next()
                val changedCount = Arb.int(0..1).next()
                val favoriteContactUseCase = mockk<FavoriteContactUseCase>()
                val unfavoriteContactUseCase = mockk<UnfavoriteContactUseCase>()
                val deleteContactUseCase = mockk<DeleteContactUseCase>()
                val restoreContactUseCase = mockk<RestoreContactUseCase>()
                coEvery { favoriteContactUseCase(parameter = contactId) } returns Result.success(changedCount)
                coEvery { unfavoriteContactUseCase(parameter = contactId) } returns Result.success(changedCount)
                coEvery { deleteContactUseCase(parameter = contactId) } returns Result.success(changedCount)
                coEvery { restoreContactUseCase(parameter = contactId) } returns Result.success(changedCount)
                val action = contactAppActionOf(favoriteContactUseCase, unfavoriteContactUseCase, deleteContactUseCase, restoreContactUseCase)
                val expected = AppActionChangedCount(changedCount = changedCount)

                action.favorite(contactId = contactId.toString()) shouldBe expected
                action.unfavorite(contactId = contactId.toString()) shouldBe expected
                action.delete(contactId = contactId.toString()) shouldBe expected
                action.restore(contactId = contactId.toString()) shouldBe expected
            }
        }

        test("바꾸는 앱 기능이 실패하면 MCP와 같은 메시지로 실패를 알린다") {
            runTest {
                val message = Arb.nonBlankString().next()
                val deleteContactUseCase = mockk<DeleteContactUseCase>()
                coEvery { deleteContactUseCase(parameter = any()) } returns Result.failure(IllegalStateException(message))
                val action = contactAppActionOf(deleteContactUseCase)

                shouldThrow<AppActionException.Failure> { action.delete(contactId = Arb.kotlinUuid().next().toString()) }.message shouldBe "IllegalStateException: $message"
            }
        }
    })

private const val RGB = 0xFFFFFFL

private data class ContactAddCase(
    val heightCm: String? = null,
    val footSizeMm: Int? = null,
    val birthday: String? = null,
    val birthdayCalendar: String? = null,
    val birthYear: Int? = null,
)

private fun Contact.withBirthday(): Contact = copy(detail = detail.copy(birthday = Arb.contactBirthday().next(), birthYear = Arb.int(0..9999).next()))

private fun ContactListItem.expected(): AppActionContact =
    AppActionContact(
        id = contact.id.toString(),
        name = contact.detail.name,
        nameQualifier = nameQualifier,
        description = contact.detail.description,
        isFavorite = contact.isFavorite,
        createdAt = contact.createdAt.toString(),
        updatedAt = contact.updatedAt.toString(),
    )

private fun Memo.expected(): AppActionMemo =
    AppActionMemo(
        id = id.toString(),
        title = detail.title,
        description = detail.description,
        color = "#%06X".format(detail.color and RGB),
        dateTime =
            detail.dateTime?.let { dateTime ->
                when (dateTime) {
                    is MemoDateTime.AllDay ->
                        AppActionMemoDateTime(allDay = true, start = dateTime.dateRange.start.toString(), endInclusive = dateTime.dateRange.endInclusive.toString())

                    is MemoDateTime.DateTime ->
                        AppActionMemoDateTime(allDay = false, start = dateTime.start.toString(), endInclusive = dateTime.endInclusive.toString())
                }
            },
        primaryTagId = primaryTagId?.toString(),
        isFinished = isFinished,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )
