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
import io.github.taetae98coding.diary.core.testing.tag.tag
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
import io.github.taetae98coding.diary.domain.tag.EntityTagKind
import io.github.taetae98coding.diary.domain.tag.usecase.AddEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.GetEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RemoveEntityTagUseCase
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.work.appaction.core.AppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.AppActionDone
import io.github.taetae98coding.diary.work.appaction.core.AppActionException
import io.github.taetae98coding.diary.work.appaction.core.AppActionId
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.memo.AppActionMemo
import io.github.taetae98coding.diary.work.appaction.memo.AppActionMemoDateTime
import io.github.taetae98coding.diary.work.appaction.memo.memoListAppActionOf
import io.github.taetae98coding.diary.work.appaction.tag.AppActionEntityTagKind
import io.github.taetae98coding.diary.work.appaction.tag.entityTagAppActionOf
import io.github.taetae98coding.diary.work.appaction.tag.expected
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

        test("TC-4ca7faaf-f740-4b6a-9c4b-33ac9b779292 연락처 하나를 연결된 태그, 모든 정보와 함께 돌려준다") {
            runTest {
                val heightMm = Arb.int(0..9999).next()
                val footSizeMm = Arb.int(0..999).next()
                val contact =
                    Arb.contact().next().copy(isDeleted = false).let { contact ->
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
                val tagList = Arb.list(Arb.tag(), 1..3).next()
                val findContactUseCase = mockk<FindContactUseCase>()
                val getEntityTagUseCase = mockk<GetEntityTagUseCase>()
                every { findContactUseCase(parameter = contact.id) } returns flowOf(Result.success(contact))
                every { getEntityTagUseCase(parameter = GetEntityTagUseCase.Parameter(kind = EntityTagKind.CONTACT, entityId = contact.id)) } returns flowOf(Result.success(tagList))
                val action = contactAppActionOf(findContactUseCase, getEntityTagUseCase)
                val detail = action.get(contactId = contact.id.toString())

                detail?.tags shouldBe tagList.map { tag -> tag.expected() }
                detail?.contact shouldBe
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
                val contact =
                    Arb
                        .contact()
                        .next()
                        .copy(isDeleted = false)
                        .let { contact -> contact.copy(detail = contact.detail.copy(height = null, footSize = null, birthday = null, birthYear = null)) }
                val findContactUseCase = mockk<FindContactUseCase>()
                val getEntityTagUseCase = mockk<GetEntityTagUseCase>()
                every { findContactUseCase(parameter = contact.id) } returns flowOf(Result.success(contact))
                every { getEntityTagUseCase(parameter = any()) } returns flowOf(Result.success(emptyList()))
                val action = contactAppActionOf(findContactUseCase, getEntityTagUseCase)

                val detail = action.get(contactId = contact.id.toString())?.contact

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
                val action = memoListAppActionOf(pageContactMemoUseCase, pageFinishedContactMemoUseCase)
                val expected = AppActionPage(items = memoList.map { memo -> memo.expected() }, nextCursor = null)

                action.contactList(contactId = contactId.toString(), sort = null, cursor = null) shouldBe expected
                action.contactFinishedList(contactId = contactId.toString(), sort = "title", cursor = null) shouldBe expected

                verify(exactly = 1) { pageContactMemoUseCase(parameter = PageContactMemoUseCase.Parameter(contactId = contactId, sort = ListSort.DEFAULT)) }
                verify(exactly = 1) { pageFinishedContactMemoUseCase(parameter = PageFinishedContactMemoUseCase.Parameter(contactId = contactId, sort = ListSort.TITLE)) }
            }
        }

        test("연락처의 메모 목록은 메모 목록의 정렬만 받는다") {
            runTest {
                val pageContactMemoUseCase = mockk<PageContactMemoUseCase>()
                val action = memoListAppActionOf(pageContactMemoUseCase)

                shouldThrow<AppActionException.InvalidArgument> {
                    action.contactList(contactId = Arb.kotlinUuid().next().toString(), sort = "name", cursor = null)
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
                        tagIds = emptyList(),
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
                    tagIds = emptyList(),
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
                            tagIds = emptyList(),
                        )
                    }.message shouldBe message
                }
                coVerify(exactly = 0) { addContactUseCase(parameter = any()) }
            }
        }

        test("고칠 값을 담지 않은 항목은 지금 값을 유지하고 고친 수를 돌려준다") {
            runTest {
                val contact = Arb.contact().next().copy(isDeleted = false)
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
                val contact =
                    Arb
                        .contact()
                        .next()
                        .copy(isDeleted = false)
                        .withBirthday()
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
                val contact =
                    Arb
                        .contact()
                        .next()
                        .copy(isDeleted = false)
                        .withBirthday()
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
                val contact =
                    Arb
                        .contact()
                        .next()
                        .copy(isDeleted = false)
                        .withBirthday()
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
                val contact =
                    Arb
                        .contact()
                        .next()
                        .copy(isDeleted = false)
                        .let { contact -> contact.copy(detail = contact.detail.copy(birthday = null)) }
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

        test("TC-e17787d4-7a35-4675-8dbd-0b61b2795a73 삭제된 연락처의 상세는 없는 연락처와 같이 null을 돌려준다") {
            runTest {
                val contact = Arb.contact().next().copy(isDeleted = true)
                val findContactUseCase = mockk<FindContactUseCase>()
                val getEntityTagUseCase = mockk<GetEntityTagUseCase>()
                every { findContactUseCase(parameter = contact.id) } returns flowOf(Result.success(contact))
                val action = contactAppActionOf(findContactUseCase, getEntityTagUseCase)

                action.get(contactId = contact.id.toString()) shouldBe null
                verify(exactly = 0) { getEntityTagUseCase(parameter = any()) }
            }
        }

        test("TC-edf2f510-aac0-4a31-afdb-2f9368763906 추가 요청에 담은 태그를 함께 연결한다") {
            runTest {
                val tagIdSet = setOf(Arb.kotlinUuid().next(), Arb.kotlinUuid().next())
                val addedId = Arb.kotlinUuid().next()
                val parameter = slot<AddContactUseCase.Parameter>()
                val addUseCase = mockk<AddContactUseCase>()
                coEvery { addUseCase(parameter = capture(parameter)) } returns Result.success(addedId)
                val action = contactAppActionOf(addUseCase)

                action.add(
                    name = Arb.nonBlankString().next(),
                    description = null,
                    heightCm = null,
                    footSizeMm = null,
                    birthday = null,
                    birthdayCalendar = null,
                    birthYear = null,
                    hometown = null,
                    howWeMet = null,
                    phoneNumbers = emptyList(),
                    tagIds = tagIdSet.map { tagId -> tagId.toString() },
                ) shouldBe
                    AppActionId(id = addedId.toString())
                parameter.captured.tagIdSet shouldBe tagIdSet
            }
        }

        test("추가 요청의 태그 id가 id 형식이 아니면 추가하지 않고 MCP와 같은 메시지로 실패한다") {
            runTest {
                val addUseCase = mockk<AddContactUseCase>()
                val action = contactAppActionOf(addUseCase)

                shouldThrow<AppActionException.InvalidArgument> {
                    action.add(name = Arb.nonBlankString().next(), description = null, heightCm = null, footSizeMm = null, birthday = null, birthdayCalendar = null, birthYear = null, hometown = null, howWeMet = null, phoneNumbers = emptyList(), tagIds = listOf("tag"))
                }.message shouldBe
                    "'tagIds' must be an id."
                coVerify(exactly = 0) { addUseCase(parameter = any()) }
            }
        }

        test("TC-173de82e-308b-45d4-bc9b-14a00eb1c2fc 태그 연결과 해제는 연락처 종류로 요청하고 완료를 돌려준다") {
            runTest {
                val contactId = Arb.kotlinUuid().next()
                val tagId = Arb.kotlinUuid().next()
                val addEntityTagUseCase = mockk<AddEntityTagUseCase>()
                val removeEntityTagUseCase = mockk<RemoveEntityTagUseCase>()
                coEvery { addEntityTagUseCase(parameter = any()) } returns Result.success(Unit)
                coEvery { removeEntityTagUseCase(parameter = any()) } returns Result.success(Unit)
                val action = entityTagAppActionOf(addEntityTagUseCase, removeEntityTagUseCase)

                action.add(kind = AppActionEntityTagKind.CONTACT, entityId = contactId.toString(), tagId = tagId.toString()) shouldBe AppActionDone(done = true)
                action.remove(kind = AppActionEntityTagKind.CONTACT, entityId = contactId.toString(), tagId = tagId.toString()) shouldBe AppActionDone(done = true)

                coVerify(exactly = 1) { addEntityTagUseCase(parameter = AddEntityTagUseCase.Parameter(kind = EntityTagKind.CONTACT, entityId = contactId, tagId = tagId)) }
                coVerify(exactly = 1) { removeEntityTagUseCase(parameter = RemoveEntityTagUseCase.Parameter(kind = EntityTagKind.CONTACT, entityId = contactId, tagId = tagId)) }
            }
        }

        test("태그 연결의 id가 id 형식이 아니면 연결하지 않고, 연결이 실패하면 MCP와 같은 메시지로 실패한다") {
            runTest {
                val message = Arb.nonBlankString().next()
                val addEntityTagUseCase = mockk<AddEntityTagUseCase>()
                val removeEntityTagUseCase = mockk<RemoveEntityTagUseCase>()
                coEvery { removeEntityTagUseCase(parameter = any()) } returns Result.failure(IllegalStateException(message))
                val action = entityTagAppActionOf(addEntityTagUseCase, removeEntityTagUseCase)

                shouldThrow<AppActionException.InvalidArgument> { action.add(kind = AppActionEntityTagKind.CONTACT, entityId = "not-id", tagId = Arb.kotlinUuid().next().toString()) }.message shouldBe "'contactId' must be an id."
                shouldThrow<AppActionException.InvalidArgument> { action.add(kind = AppActionEntityTagKind.CONTACT, entityId = Arb.kotlinUuid().next().toString(), tagId = "tag") }.message shouldBe "'tagId' must be an id."
                shouldThrow<AppActionException.Failure> {
                    action.remove(kind = AppActionEntityTagKind.CONTACT, entityId = Arb.kotlinUuid().next().toString(), tagId = Arb.kotlinUuid().next().toString())
                }.message shouldBe "IllegalStateException: $message"
                coVerify(exactly = 0) { addEntityTagUseCase(parameter = any()) }
            }
        }

        test("삭제된 연락처도 포함하는 조회는 삭제된 연락처를 돌려준다") {
            runTest {
                val contact = Arb.contact().next().copy(isDeleted = true)
                val findContactUseCase = mockk<FindContactUseCase>()
                val getEntityTagUseCase = mockk<GetEntityTagUseCase>()
                every { findContactUseCase(parameter = contact.id) } returns flowOf(Result.success(contact))
                every { getEntityTagUseCase(parameter = GetEntityTagUseCase.Parameter(kind = EntityTagKind.CONTACT, entityId = contact.id)) } returns flowOf(Result.success(emptyList()))
                val action = contactAppActionOf(findContactUseCase, getEntityTagUseCase)

                action.getIncludingDeleted(contactId = contact.id.toString())?.contact?.id shouldBe contact.id.toString()
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
