package io.github.taetae98coding.diary.core.database.impl.contact.datasource

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import app.cash.turbine.test
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactBirthdayCalendarLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactListItemLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactPhoneNumberLocalEntity
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.contact.transaction.AccountContactTransactionImpl
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountContactLocalDataSourceImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var dataSource: AccountContactLocalDataSourceImpl
        lateinit var transaction: AccountContactTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            dataSource = AccountContactLocalDataSourceImpl(database = database)
            transaction = AccountContactTransactionImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun pageItems(
            accountId: Uuid,
            sort: ListSortLocalEntity,
            hasBirthday: Boolean? = null,
            hasBirthYear: Boolean? = null,
            hasPhoneNumber: Boolean? = null,
        ): List<ContactListItemLocalEntity> {
            val page =
                dataSource.page(accountId = accountId, sort = sort, hasBirthday = hasBirthday, hasBirthYear = hasBirthYear, hasPhoneNumber = hasPhoneNumber).load(
                    PagingSource.LoadParams.Refresh(
                        key = null,
                        loadSize = PAGE_SIZE,
                        placeholdersEnabled = false,
                    ),
                )

            return (page as PagingSource.LoadResult.Page).data
        }

        suspend fun page(
            accountId: Uuid,
            sort: ListSortLocalEntity,
            hasBirthday: Boolean? = null,
            hasBirthYear: Boolean? = null,
            hasPhoneNumber: Boolean? = null,
        ): List<ContactLocalEntity> = pageItems(accountId = accountId, sort = sort, hasBirthday = hasBirthday, hasBirthYear = hasBirthYear, hasPhoneNumber = hasPhoneNumber).map { item -> item.contact }

        test("TC-52472399-3874-4d4b-9299-13fd459db696 즐겨찾기인 연락처가 고른 기준과 무관하게 앞에 놓인다") {
            val accountId = Arb.kotlinUuid().next()
            val favorite = contact(name = LAST_NAME, isFavorite = true, updatedAt = OLD_INSTANT)
            val other = contact(name = FIRST_NAME, isFavorite = false, updatedAt = NEW_INSTANT)
            transaction.upsert(accountId = accountId, contactList = listOf(favorite, other))

            ListSortLocalEntity.entries.forEach { sort ->
                page(accountId = accountId, sort = sort) shouldBe listOf(favorite, other)
            }
        }

        test("TC-917f997e-5d12-4ba0-85d4-e116df60e153 즐겨찾기 묶음 안에서는 고른 기준의 순서가 적용된다") {
            val accountId = Arb.kotlinUuid().next()
            val recentFavorite = contact(name = LAST_NAME, isFavorite = true, updatedAt = NEW_INSTANT)
            val oldFavorite = contact(name = FIRST_NAME, isFavorite = true, updatedAt = OLD_INSTANT)
            val recentOther = contact(name = MIDDLE_NAME, isFavorite = false, updatedAt = NEW_INSTANT)
            val oldOther = contact(name = ANOTHER_NAME, isFavorite = false, updatedAt = OLD_INSTANT)
            transaction.upsert(accountId = accountId, contactList = listOf(recentFavorite, oldFavorite, recentOther, oldOther))

            page(accountId = accountId, sort = ListSortLocalEntity.NAME) shouldBe
                listOf(oldFavorite, recentFavorite, oldOther, recentOther)
            page(accountId = accountId, sort = ListSortLocalEntity.RECENTLY_UPDATED) shouldBe
                listOf(recentFavorite, oldFavorite, recentOther, oldOther)
        }

        test("TC-9fc00544-2fa0-4f81-80aa-77ea44c04e91 현재 계정과 연결되고 삭제되지 않은 연락처만 노출한다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val favorite = contact(name = FIRST_NAME, isFavorite = true)
            val other = contact(name = ANOTHER_NAME)
            val deletedFavorite = contact(name = MIDDLE_NAME, isFavorite = true).copy(isDeleted = true)
            val deletedOther = contact(name = LAST_NAME).copy(isDeleted = true)
            val otherAccountFavorite = contact(name = "EContact", isFavorite = true)
            val otherAccountOther = contact(name = "FContact")
            transaction.upsert(accountId = accountId, contactList = listOf(favorite, other, deletedFavorite, deletedOther))
            transaction.upsert(accountId = otherAccountId, contactList = listOf(otherAccountFavorite, otherAccountOther))

            page(accountId = accountId, sort = ListSortLocalEntity.NAME) shouldBe listOf(favorite, other)
            page(accountId = accountId, sort = ListSortLocalEntity.RECENTLY_UPDATED) shouldBe listOf(favorite, other)
        }

        test("TC-28dfd5bb-5a64-4514-a6f2-536ca2e9f8f8 즐겨찾기 여부와 관계없이 노출 기준을 만족하는 연락처가 모두 노출된다") {
            val accountId = Arb.kotlinUuid().next()
            val favorite = contact(name = LAST_NAME, isFavorite = true)
            val other = contact(name = FIRST_NAME)
            val deletedFavorite = contact(name = MIDDLE_NAME, isFavorite = true).copy(isDeleted = true)
            transaction.upsert(accountId = accountId, contactList = listOf(favorite, other, deletedFavorite))

            ListSortLocalEntity.entries.forEach { sort ->
                page(accountId = accountId, sort = sort).toSet() shouldBe setOf(favorite, other)
            }

            transaction.updateFavorite(accountId = accountId, contactId = other.id, isFavorite = true, updatedAt = NEW_INSTANT)

            ListSortLocalEntity.entries.forEach { sort ->
                page(accountId = accountId, sort = sort).map { item -> item.id }.toSet() shouldBe setOf(favorite.id, other.id)
            }
        }

        test("TC-1c7010ad-28ed-42c8-b291-5ddea4beaeb7 이름순 정렬은 알게 된 경로를 쓰지 않고 이름만으로 정한다") {
            val accountId = Arb.kotlinUuid().next()
            val sameNameFirst = contact(name = FIRST_NAME).withHowWeMet("Z")
            val sameNameSecond = contact(name = FIRST_NAME).withHowWeMet("Z")
            val longerName = contact(name = "${FIRST_NAME}1").withHowWeMet("")
            transaction.upsert(accountId = accountId, contactList = listOf(longerName, sameNameFirst, sameNameSecond))

            val items = pageItems(accountId = accountId, sort = ListSortLocalEntity.NAME)

            items.map { item -> item.contact.detail.name } shouldBe listOf(FIRST_NAME, FIRST_NAME, "${FIRST_NAME}1")
            items.map { item -> item.nameQualifier } shouldBe listOf("Z", "Z", "")
        }

        test("TC-e353695f-5048-4230-b9e7-3413310e9031 실행 취소로 돌아온 연락처는 즐겨찾기 묶음과 정렬 기준에 맞는 위치에 다시 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val shownFavorite = contact(name = ANOTHER_NAME, isFavorite = true)
            val restoredFavorite = contact(name = LAST_NAME, isFavorite = true)
            val shownOther = contact(name = FIRST_NAME)
            val anotherShownOther = contact(name = MIDDLE_NAME)
            val restoredOther = contact(name = "EContact")
            transaction.upsert(
                accountId = accountId,
                contactList = listOf(shownFavorite, restoredFavorite, shownOther, anotherShownOther, restoredOther),
            )
            transaction.updateDeleted(accountId = accountId, contactId = restoredFavorite.id, isDeleted = true, updatedAt = NEW_INSTANT)
            transaction.updateDeleted(accountId = accountId, contactId = restoredOther.id, isDeleted = true, updatedAt = NEW_INSTANT)
            page(accountId = accountId, sort = ListSortLocalEntity.NAME).map { item -> item.id } shouldBe
                listOf(shownFavorite.id, shownOther.id, anotherShownOther.id)

            transaction.updateDeleted(accountId = accountId, contactId = restoredFavorite.id, isDeleted = false, updatedAt = NEWEST_INSTANT)
            transaction.updateDeleted(accountId = accountId, contactId = restoredOther.id, isDeleted = false, updatedAt = NEWEST_INSTANT)

            page(accountId = accountId, sort = ListSortLocalEntity.NAME).map { item -> item.id } shouldBe
                listOf(shownFavorite.id, restoredFavorite.id, shownOther.id, anotherShownOther.id, restoredOther.id)
            page(accountId = accountId, sort = ListSortLocalEntity.RECENTLY_UPDATED).map { item -> item.id } shouldBe
                listOf(restoredFavorite.id, shownFavorite.id, restoredOther.id, shownOther.id, anotherShownOther.id)
        }

        test("TC-132ea7e0-fc17-4feb-a89a-444bce49f3a5 저장 상태가 바뀌면 노출 기준과 정렬 기준으로 목록을 갱신한다") {
            val accountId = Arb.kotlinUuid().next()
            val first = contact(name = FIRST_NAME)
            val last = contact(name = LAST_NAME)
            val added = contact(name = MIDDLE_NAME)
            transaction.upsert(accountId = accountId, contactList = listOf(first, last))
            page(accountId = accountId, sort = ListSortLocalEntity.NAME) shouldBe listOf(first, last)

            transaction.upsert(accountId = accountId, contactList = listOf(added))
            page(accountId = accountId, sort = ListSortLocalEntity.NAME).map { item -> item.id } shouldBe listOf(first.id, added.id, last.id)

            transaction.updateDetail(accountId = accountId, contactId = last.id, detail = last.detail.copy(name = "0Contact"), updatedAt = NEW_INSTANT)
            page(accountId = accountId, sort = ListSortLocalEntity.NAME).map { item -> item.id } shouldBe listOf(last.id, first.id, added.id)

            transaction.updateFavorite(accountId = accountId, contactId = added.id, isFavorite = true, updatedAt = NEW_INSTANT)
            page(accountId = accountId, sort = ListSortLocalEntity.NAME).map { item -> item.id } shouldBe listOf(added.id, last.id, first.id)

            transaction.updateFavorite(accountId = accountId, contactId = added.id, isFavorite = false, updatedAt = NEW_INSTANT)
            page(accountId = accountId, sort = ListSortLocalEntity.NAME).map { item -> item.id } shouldBe listOf(last.id, first.id, added.id)

            transaction.updateDeleted(accountId = accountId, contactId = first.id, isDeleted = true, updatedAt = NEW_INSTANT)
            page(accountId = accountId, sort = ListSortLocalEntity.NAME).map { item -> item.id } shouldBe listOf(last.id, added.id)
        }

        test("TC-7f25844a-3385-4cf1-ba2b-6c31a33c4c8e 생일과 달력 구분, 태어난 연도, 고향, 즐겨찾기 여부가 함께 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val stored =
                contact(name = FIRST_NAME, isFavorite = true).let { contact ->
                    contact.copy(
                        detail =
                            contact.detail.copy(
                                birthday = BIRTHDAY,
                                birthdayCalendar = ContactBirthdayCalendarLocalEntity.LUNAR,
                                birthYear = BIRTHDAY.year,
                                hometown = HOMETOWN,
                            ),
                    )
                }
            transaction.upsert(accountId = accountId, contactList = listOf(stored))

            page(accountId = accountId, sort = ListSortLocalEntity.NAME) shouldBe listOf(stored)
        }

        test("TC-612ffcfb-b3f2-466b-ae99-7d654a7343dc 즐겨찾기 우선을 조회 조건으로 넘겨 뒤쪽 구간의 즐겨찾기도 첫 구간에 조회한다") {
            val accountId = Arb.kotlinUuid().next()
            val otherList = List(PAGE_SIZE) { index -> contact(name = "A-${index.toString().padStart(length = 2, padChar = '0')}") }
            val favorite = contact(name = LAST_NAME, isFavorite = true)
            transaction.upsert(accountId = accountId, contactList = otherList + favorite)

            page(accountId = accountId, sort = ListSortLocalEntity.NAME) shouldBe listOf(favorite) + otherList.dropLast(1)
        }

        test("TC-17413b38-70cc-4699-8582-646d45e93421 즐겨찾기가 없으면 목록은 이름 오름차순으로 정렬한다") {
            val accountId = Arb.kotlinUuid().next()
            val first = contact(name = FIRST_NAME)
            val another = contact(name = ANOTHER_NAME)
            val middle = contact(name = MIDDLE_NAME)
            val last = contact(name = LAST_NAME)
            transaction.upsert(accountId = accountId, contactList = listOf(middle, last, first, another))

            page(accountId = accountId, sort = ListSortLocalEntity.NAME) shouldBe listOf(first, another, middle, last)
        }

        test("TC-816287d1-db3a-4119-a608-379b27ba26da 최근 수정순은 수정 시각 내림차순이고 같으면 이름 오름차순이다") {
            val accountId = Arb.kotlinUuid().next()
            val recent = contact(name = LAST_NAME, updatedAt = NEW_INSTANT)
            val oldFirst = contact(name = FIRST_NAME, updatedAt = OLD_INSTANT)
            val oldAnother = contact(name = ANOTHER_NAME, updatedAt = OLD_INSTANT)
            transaction.upsert(accountId = accountId, contactList = listOf(oldAnother, oldFirst, recent))

            page(accountId = accountId, sort = ListSortLocalEntity.RECENTLY_UPDATED) shouldBe listOf(recent, oldFirst, oldAnother)
        }

        test("TC-c01cf3f6-6ce5-4033-ab44-7afc4fa1f298 즐겨찾기가 하나도 없으면 목록은 고른 기준 하나로만 정렬된다") {
            val accountId = Arb.kotlinUuid().next()
            val nameFirst = contact(name = FIRST_NAME, updatedAt = OLD_INSTANT)
            val nameLast = contact(name = LAST_NAME, updatedAt = NEW_INSTANT)
            transaction.upsert(accountId = accountId, contactList = listOf(nameLast, nameFirst))

            page(accountId = accountId, sort = ListSortLocalEntity.NAME) shouldBe listOf(nameFirst, nameLast)
            page(accountId = accountId, sort = ListSortLocalEntity.RECENTLY_UPDATED) shouldBe listOf(nameLast, nameFirst)
        }

        test("TC-fdd70132-3ae7-46e0-8cb8-bf4fb62a3a23 고른 연락처는 고른 순서와 관계없이 이름 오름차순으로 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val first = contact(name = FIRST_NAME)
            val middle = contact(name = MIDDLE_NAME, isFavorite = true, updatedAt = NEW_INSTANT)
            val last = contact(name = LAST_NAME)
            transaction.upsert(accountId = accountId, contactList = listOf(middle, last, first))

            dataSource
                .get(accountId = accountId, contactIdSet = linkedSetOf(last.id, first.id, middle.id))
                .first()
                .map { item -> item.contact.id } shouldBe listOf(first.id, middle.id, last.id)
        }

        test("TC-c341b505-225c-4c85-99b8-1dc9512ea71a 각 축은 정해진 기준으로 연락처의 유무를 판정한다") {
            val accountId = Arb.kotlinUuid().next()
            val solarBirthday = contact(name = FIRST_NAME, phoneNumberList = emptyList()).withBirthday(calendar = ContactBirthdayCalendarLocalEntity.SOLAR)
            val lunarBirthday = contact(name = ANOTHER_NAME, phoneNumberList = emptyList()).withBirthday(calendar = ContactBirthdayCalendarLocalEntity.LUNAR)
            val birthYearOnly = contact(name = MIDDLE_NAME, phoneNumberList = listOf(phoneNumber())).let { contact -> contact.copy(detail = contact.detail.copy(birthYear = Arb.int().next())) }
            val multiplePhoneNumber = contact(name = LAST_NAME, phoneNumberList = listOf(phoneNumber(), phoneNumber()))
            transaction.upsert(accountId = accountId, contactList = listOf(solarBirthday, lunarBirthday, birthYearOnly, multiplePhoneNumber))

            page(accountId = accountId, sort = ListSortLocalEntity.NAME, hasBirthday = true) shouldBe listOf(solarBirthday, lunarBirthday)
            page(accountId = accountId, sort = ListSortLocalEntity.NAME, hasBirthday = false) shouldBe listOf(birthYearOnly, multiplePhoneNumber)
            page(accountId = accountId, sort = ListSortLocalEntity.NAME, hasPhoneNumber = true) shouldBe listOf(birthYearOnly, multiplePhoneNumber)
            page(accountId = accountId, sort = ListSortLocalEntity.NAME, hasPhoneNumber = false) shouldBe listOf(solarBirthday, lunarBirthday)
            page(accountId = accountId, sort = ListSortLocalEntity.NAME, hasBirthYear = true) shouldBe listOf(solarBirthday, lunarBirthday, birthYearOnly)
            page(accountId = accountId, sort = ListSortLocalEntity.NAME, hasBirthYear = false) shouldBe listOf(multiplePhoneNumber)
        }

        test("TC-5093c0b4-9086-4f20-886b-a0f0470af41f 전체가 아닌 축이 여럿이면 그 축을 모두 함께 만족하는 연락처만 노출한다") {
            val accountId = Arb.kotlinUuid().next()
            val both = contact(name = FIRST_NAME, phoneNumberList = listOf(phoneNumber())).withBirthday()
            val birthdayOnly = contact(name = ANOTHER_NAME, phoneNumberList = emptyList()).withBirthday()
            val birthYearOnly =
                contact(name = MIDDLE_NAME, phoneNumberList = emptyList()).let { contact ->
                    contact.copy(detail = contact.detail.copy(birthYear = Arb.int().next()))
                }
            val phoneNumberOnly = contact(name = LAST_NAME, phoneNumberList = listOf(phoneNumber()))
            val neither = contact(name = FIRST_NAME, phoneNumberList = emptyList())
            transaction.upsert(accountId = accountId, contactList = listOf(both, birthdayOnly, birthYearOnly, phoneNumberOnly, neither))

            page(accountId = accountId, sort = ListSortLocalEntity.NAME, hasBirthday = true, hasPhoneNumber = false) shouldBe listOf(birthdayOnly)
            page(accountId = accountId, sort = ListSortLocalEntity.NAME, hasBirthday = false, hasBirthYear = true) shouldBe listOf(birthYearOnly)
            page(accountId = accountId, sort = ListSortLocalEntity.NAME, hasBirthYear = false, hasPhoneNumber = true) shouldBe listOf(phoneNumberOnly)
        }

        test("TC-4b99af6a-d212-49df-9661-35bdc1a8a39f 유무 필터는 즐겨찾기 우선과 정렬 순서를 바꾸지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val recentFavorite = contact(name = LAST_NAME, isFavorite = true, updatedAt = NEW_INSTANT)
            val oldFavorite = contact(name = FIRST_NAME, isFavorite = true, updatedAt = OLD_INSTANT)
            val recentOther = contact(name = MIDDLE_NAME, updatedAt = NEW_INSTANT)
            val oldOther = contact(name = ANOTHER_NAME, updatedAt = OLD_INSTANT)
            val withoutPhoneNumber = contact(name = FIRST_NAME, isFavorite = true, updatedAt = NEW_INSTANT, phoneNumberList = emptyList())
            transaction.upsert(accountId = accountId, contactList = listOf(recentFavorite, oldFavorite, recentOther, oldOther, withoutPhoneNumber))

            page(accountId = accountId, sort = ListSortLocalEntity.NAME, hasPhoneNumber = true) shouldBe
                listOf(oldFavorite, recentFavorite, oldOther, recentOther)
            page(accountId = accountId, sort = ListSortLocalEntity.RECENTLY_UPDATED, hasPhoneNumber = true) shouldBe
                listOf(recentFavorite, oldFavorite, recentOther, oldOther)
        }

        test("TC-c2c96771-f9fd-4e86-addd-544b202e1ce4 필터를 적용한 동안 전화번호가 바뀌어 판정이 달라지면 목록에서 사라지거나 나타난다") {
            val accountId = Arb.kotlinUuid().next()
            val withPhoneNumber = contact(name = FIRST_NAME)
            val withoutPhoneNumber = contact(name = LAST_NAME, phoneNumberList = emptyList())
            transaction.upsert(accountId = accountId, contactList = listOf(withPhoneNumber, withoutPhoneNumber))
            page(accountId = accountId, sort = ListSortLocalEntity.NAME, hasPhoneNumber = true) shouldBe listOf(withPhoneNumber)

            val cleared = withPhoneNumber.copy(detail = withPhoneNumber.detail.copy(phoneNumberList = emptyList()))
            val added = withoutPhoneNumber.copy(detail = withoutPhoneNumber.detail.copy(phoneNumberList = listOf(phoneNumber())))
            transaction.upsert(accountId = accountId, contactList = listOf(cleared, added))

            page(accountId = accountId, sort = ListSortLocalEntity.NAME, hasPhoneNumber = true) shouldBe listOf(added)
        }

        test("TC-c2c96771-f9fd-4e86-addd-544b202e1ce4 필터를 적용한 동안 생일이 바뀌어 판정이 달라지면 목록에서 사라지거나 나타난다") {
            val accountId = Arb.kotlinUuid().next()
            val withBirthday = contact(name = FIRST_NAME).withBirthday()
            val withoutBirthday = contact(name = LAST_NAME)
            transaction.upsert(accountId = accountId, contactList = listOf(withBirthday, withoutBirthday))
            page(accountId = accountId, sort = ListSortLocalEntity.NAME, hasBirthday = true) shouldBe listOf(withBirthday)

            val cleared = withBirthday.copy(detail = withBirthday.detail.copy(birthday = null, birthdayCalendar = null, birthYear = null))
            val added = withoutBirthday.withBirthday()
            transaction.upsert(accountId = accountId, contactList = listOf(cleared, added))

            page(accountId = accountId, sort = ListSortLocalEntity.NAME, hasBirthday = true) shouldBe listOf(added)
        }

        test("TC-c2c96771-f9fd-4e86-addd-544b202e1ce4 필터를 적용한 동안 태어난 연도가 바뀌어 판정이 달라지면 목록에서 사라지거나 나타난다") {
            val accountId = Arb.kotlinUuid().next()
            val withBirthYear = contact(name = FIRST_NAME).let { contact -> contact.copy(detail = contact.detail.copy(birthYear = Arb.int().next())) }
            val withoutBirthYear = contact(name = LAST_NAME)
            transaction.upsert(accountId = accountId, contactList = listOf(withBirthYear, withoutBirthYear))
            page(accountId = accountId, sort = ListSortLocalEntity.NAME, hasBirthYear = true) shouldBe listOf(withBirthYear)

            val cleared = withBirthYear.copy(detail = withBirthYear.detail.copy(birthYear = null))
            val added = withoutBirthYear.copy(detail = withoutBirthYear.detail.copy(birthYear = Arb.int().next()))
            transaction.upsert(accountId = accountId, contactList = listOf(cleared, added))

            page(accountId = accountId, sort = ListSortLocalEntity.NAME, hasBirthYear = true) shouldBe listOf(added)
        }

        test("TC-2d15ffea-2441-4291-bf5d-4be0e7380d4c 필터를 적용한 동안 조건에 맞지 않는 연락처를 저장하면 목록에 나타나지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val shown = contact(name = FIRST_NAME)
            transaction.upsert(accountId = accountId, contactList = listOf(shown))
            page(accountId = accountId, sort = ListSortLocalEntity.NAME, hasBirthday = false) shouldBe listOf(shown)

            val withBirthday = contact(name = MIDDLE_NAME).withBirthday()
            val withoutBirthday = contact(name = LAST_NAME)
            transaction.upsert(accountId = accountId, contactList = listOf(withBirthday, withoutBirthday))

            page(accountId = accountId, sort = ListSortLocalEntity.NAME, hasBirthday = false) shouldBe listOf(shown, withoutBirthday)
        }

        test("TC-c01f5552-16a5-454a-a625-b79418424739 입력한 고향은 그대로, 즐겨찾기 여부는 즐겨찾기가 아닌 값으로 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val stored =
                contact(name = FIRST_NAME, isFavorite = false).let { contact ->
                    contact.copy(detail = contact.detail.copy(hometown = HOMETOWN))
                }
            transaction.upsert(accountId = accountId, contactList = listOf(stored))

            val loaded = page(accountId = accountId, sort = ListSortLocalEntity.NAME).single()

            loaded.detail.hometown shouldBe HOMETOWN
            loaded.isFavorite shouldBe false
        }

        test("TC-0ab3e2d9-b691-489e-bbf0-07e3daf5db6a 고향을 비우면 비어 있는 값으로 저장한다") {
            val accountId = Arb.kotlinUuid().next()
            val stored =
                contact(name = FIRST_NAME).let { contact ->
                    contact.copy(detail = contact.detail.copy(hometown = ""))
                }
            transaction.upsert(accountId = accountId, contactList = listOf(stored))

            page(accountId = accountId, sort = ListSortLocalEntity.NAME)
                .single()
                .detail.hometown shouldBe ""
        }

        test("TC-0c9d59e7-16ed-466a-94ff-10c48fbd9833 알게 된 경로를 연락처의 값으로 함께 저장한다") {
            val accountId = Arb.kotlinUuid().next()
            val howWeMet = "how-we-met-${Arb.string().next()}"
            val stored =
                contact(name = FIRST_NAME).let { contact ->
                    contact.copy(detail = contact.detail.copy(howWeMet = howWeMet))
                }
            transaction.upsert(accountId = accountId, contactList = listOf(stored))

            page(accountId = accountId, sort = ListSortLocalEntity.NAME) shouldBe listOf(stored)
        }

        test("TC-c5a0e434-17b1-4ced-861d-9a5c2ca7206b 알게 된 경로를 비우면 비어 있는 값으로 저장한다") {
            val accountId = Arb.kotlinUuid().next()
            val stored =
                contact(name = FIRST_NAME).let { contact ->
                    contact.copy(detail = contact.detail.copy(howWeMet = ""))
                }
            transaction.upsert(accountId = accountId, contactList = listOf(stored))

            page(accountId = accountId, sort = ListSortLocalEntity.NAME)
                .single()
                .detail.howWeMet shouldBe ""
        }
        test("TC-fde0222b-1e09-44f7-b6da-08dc845efb07 계정의 삭제되지 않은 다른 연락처와 이름이 글자 그대로 같을 때만 구분 경로를 준다") {
            val sameName = "name-${Arb.nonBlankString().next()}-name"
            val howWeMet = "how-${Arb.nonBlankString().next()}-met"
            // 다른 연락처의 이름, 삭제 여부, 다른 계정에만 있는지와 그때 기대하는 구분 경로
            val cases =
                listOf(
                    OtherContactCase(name = sameName) to howWeMet,
                    OtherContactCase(name = sameName, isDeleted = true) to "",
                    OtherContactCase(name = "other-$sameName") to "",
                    OtherContactCase(name = "$sameName ") to "",
                    OtherContactCase(name = sameName.replaceFirst("-", " -")) to "",
                    OtherContactCase(name = sameName, isOtherAccount = true) to "",
                )

            cases.forEach { (case, expected) ->
                val accountId = Arb.kotlinUuid().next()
                val target = contact(name = sameName).withHowWeMet(howWeMet)
                val other = contact(name = case.name).copy(isDeleted = case.isDeleted)
                transaction.upsert(accountId = accountId, contactList = listOf(target))
                transaction.upsert(accountId = if (case.isOtherAccount) Arb.kotlinUuid().next() else accountId, contactList = listOf(other))

                pageItems(accountId = accountId, sort = ListSortLocalEntity.NAME)
                    .single { item -> item.contact.id == target.id }
                    .nameQualifier shouldBe expected
                dataSource
                    .get(accountId = accountId, contactIdSet = setOf(target.id))
                    .first()
                    .single()
                    .nameQualifier shouldBe expected
            }
        }

        test("TC-31472722-f6d7-4211-aafd-9806fb6c244c 이름이 같은 연락처의 구분 경로는 저장된 알게 된 경로를 그대로 담는다") {
            val sameName = "name-${Arb.nonBlankString().next()}-name"
            val howWeMetList = listOf("", "  ", "  how-${Arb.nonBlankString().next()}-met  ")

            howWeMetList.forEach { howWeMet ->
                val accountId = Arb.kotlinUuid().next()
                val target = contact(name = sameName).withHowWeMet(howWeMet)
                transaction.upsert(accountId = accountId, contactList = listOf(target, contact(name = sameName)))

                dataSource
                    .get(accountId = accountId, contactIdSet = setOf(target.id))
                    .first()
                    .single()
                    .nameQualifier shouldBe howWeMet
            }
        }

        test("TC-41b81344-7aae-477b-b95c-63d125742fed 이름이 같은 연락처인지가 바뀌면 다시 조회하지 않아도 구분 경로가 바뀐다") {
            val accountId = Arb.kotlinUuid().next()
            val sameName = "name-${Arb.nonBlankString().next()}-name"
            val howWeMet = "how-${Arb.nonBlankString().next()}-met"
            val changedHowWeMet = "changed-$howWeMet"
            val target = contact(name = sameName).withHowWeMet(howWeMet)
            val other = contact(name = sameName)
            transaction.upsert(accountId = accountId, contactList = listOf(target))

            dataSource
                .get(accountId = accountId, contactIdSet = setOf(target.id))
                .map { list -> list.single().nameQualifier }
                .distinctUntilChanged()
                .test {
                    awaitItem() shouldBe ""

                    transaction.upsert(accountId = accountId, contactList = listOf(other))
                    awaitItem() shouldBe howWeMet

                    transaction.updateDetail(accountId = accountId, contactId = target.id, detail = target.detail.copy(howWeMet = changedHowWeMet), updatedAt = NEW_INSTANT)
                    awaitItem() shouldBe changedHowWeMet

                    transaction.updateDeleted(accountId = accountId, contactId = other.id, isDeleted = true, updatedAt = NEW_INSTANT)
                    awaitItem() shouldBe ""

                    transaction.updateDeleted(accountId = accountId, contactId = other.id, isDeleted = false, updatedAt = NEW_INSTANT)
                    awaitItem() shouldBe changedHowWeMet

                    transaction.updateDetail(accountId = accountId, contactId = other.id, detail = other.detail.copy(name = "other-$sameName"), updatedAt = NEW_INSTANT)
                    awaitItem() shouldBe ""
                }
        }
    }) {
    public companion object {
        private const val PAGE_SIZE = 20

        private const val FIRST_NAME = "AContact"
        private const val ANOTHER_NAME = "BContact"
        private const val MIDDLE_NAME = "CContact"
        private const val LAST_NAME = "DContact"
        private const val HOMETOWN = "강원도 춘천시"

        private val BIRTHDAY = LocalDate(year = 1990, month = 3, day = 4)
        private val OLD_INSTANT = Instant.fromEpochMilliseconds(1_000_000_000)
        private val NEW_INSTANT = Instant.fromEpochMilliseconds(2_000_000_000)
        private val NEWEST_INSTANT = Instant.fromEpochMilliseconds(3_000_000_000)

        private fun contact(
            name: String,
            isFavorite: Boolean = false,
            updatedAt: Instant = OLD_INSTANT,
            phoneNumberList: List<ContactPhoneNumberLocalEntity> = listOf(phoneNumber()),
        ): ContactLocalEntity =
            ContactLocalEntity(
                id = Arb.kotlinUuid().next(),
                detail =
                    ContactDetailLocalEntity(
                        name = name,
                        description = "description-${Arb.string().next()}",
                        heightCentimeter = null,
                        footSizeMillimeter = null,
                        birthday = null,
                        birthdayCalendar = null,
                        birthYear = null,
                        hometown = "hometown-${Arb.string().next()}",
                        howWeMet = "how-we-met-${Arb.string().next()}",
                        phoneNumberList = phoneNumberList,
                    ),
                isFavorite = isFavorite,
                isDeleted = false,
                updatedAt = updatedAt,
                createdAt = OLD_INSTANT,
            )

        private data class OtherContactCase(
            val name: String,
            val isDeleted: Boolean = false,
            val isOtherAccount: Boolean = false,
        )

        private fun ContactLocalEntity.withHowWeMet(howWeMet: String): ContactLocalEntity = copy(detail = detail.copy(howWeMet = howWeMet))

        private fun phoneNumber(): ContactPhoneNumberLocalEntity = ContactPhoneNumberLocalEntity(number = Arb.nonBlankString().next())

        private fun ContactLocalEntity.withBirthday(calendar: ContactBirthdayCalendarLocalEntity = ContactBirthdayCalendarLocalEntity.SOLAR): ContactLocalEntity = copy(detail = detail.copy(birthday = BIRTHDAY, birthdayCalendar = calendar, birthYear = BIRTHDAY.year))
    }
}
