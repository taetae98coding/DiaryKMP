package io.github.taetae98coding.diary.core.database.impl.contact.datasource

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactBirthdayCalendarLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactPhoneNumberLocalEntity
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.contact.transaction.AccountContactTransactionImpl
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.library.fixturemonkey.nonBlankString
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
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

        suspend fun page(
            accountId: Uuid,
            sort: ListSortLocalEntity,
            hasBirthday: Boolean? = null,
            hasBirthYear: Boolean? = null,
            hasPhoneNumber: Boolean? = null,
        ): List<ContactLocalEntity> {
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

        test("TC-52472399-3874-4d4b-9299-13fd459db696 즐겨찾기인 연락처가 고른 기준과 무관하게 앞에 놓인다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val favorite = contact(name = LAST_NAME, isFavorite = true, updatedAt = OLD_INSTANT)
            val other = contact(name = FIRST_NAME, isFavorite = false, updatedAt = NEW_INSTANT)
            transaction.upsert(accountId = accountId, contactList = listOf(favorite, other))

            ListSortLocalEntity.entries.forEach { sort ->
                page(accountId = accountId, sort = sort) shouldBe listOf(favorite, other)
            }
        }

        test("TC-917f997e-5d12-4ba0-85d4-e116df60e153 즐겨찾기 묶음 안에서는 고른 기준의 순서가 적용된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
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

        test("즐겨찾기인 연락처도 삭제되었거나 다른 계정과 연결되어 있으면 노출하지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val favorite = contact(name = FIRST_NAME, isFavorite = true)
            val deletedFavorite = contact(name = MIDDLE_NAME, isFavorite = true).copy(isDeleted = true)
            val otherAccountFavorite = contact(name = ANOTHER_NAME, isFavorite = true)
            transaction.upsert(accountId = accountId, contactList = listOf(favorite, deletedFavorite))
            transaction.upsert(accountId = otherAccountId, contactList = listOf(otherAccountFavorite))

            page(accountId = accountId, sort = ListSortLocalEntity.NAME) shouldBe listOf(favorite)
        }

        test("TC-7f25844a-3385-4cf1-ba2b-6c31a33c4c8e TC-c01f5552-16a5-454a-a625-b79418424739 생일과 달력 구분, 태어난 연도, 고향, 즐겨찾기 여부가 함께 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
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
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherList = List(PAGE_SIZE) { index -> contact(name = "A-${index.toString().padStart(length = 2, padChar = '0')}") }
            val favorite = contact(name = LAST_NAME, isFavorite = true)
            transaction.upsert(accountId = accountId, contactList = otherList + favorite)

            page(accountId = accountId, sort = ListSortLocalEntity.NAME) shouldBe listOf(favorite) + otherList.dropLast(1)
        }

        test("TC-17413b38-70cc-4699-8582-646d45e93421 즐겨찾기가 없으면 목록은 이름 오름차순으로 정렬한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val first = contact(name = FIRST_NAME)
            val another = contact(name = ANOTHER_NAME)
            val middle = contact(name = MIDDLE_NAME)
            val last = contact(name = LAST_NAME)
            transaction.upsert(accountId = accountId, contactList = listOf(middle, last, first, another))

            page(accountId = accountId, sort = ListSortLocalEntity.NAME) shouldBe listOf(first, another, middle, last)
        }

        test("TC-816287d1-db3a-4119-a608-379b27ba26da 최근 수정순은 수정 시각 내림차순이고 같으면 이름 오름차순이다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val recent = contact(name = LAST_NAME, updatedAt = NEW_INSTANT)
            val oldFirst = contact(name = FIRST_NAME, updatedAt = OLD_INSTANT)
            val oldAnother = contact(name = ANOTHER_NAME, updatedAt = OLD_INSTANT)
            transaction.upsert(accountId = accountId, contactList = listOf(oldAnother, oldFirst, recent))

            page(accountId = accountId, sort = ListSortLocalEntity.RECENTLY_UPDATED) shouldBe listOf(recent, oldFirst, oldAnother)
        }

        test("TC-c01cf3f6-6ce5-4033-ab44-7afc4fa1f298 즐겨찾기가 없으면 노출 기준의 연락처가 모두 고른 기준 하나로만 정렬된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val nameFirst = contact(name = FIRST_NAME, updatedAt = OLD_INSTANT)
            val nameLast = contact(name = LAST_NAME, updatedAt = NEW_INSTANT)
            transaction.upsert(accountId = accountId, contactList = listOf(nameLast, nameFirst))

            page(accountId = accountId, sort = ListSortLocalEntity.NAME) shouldBe listOf(nameFirst, nameLast)
            page(accountId = accountId, sort = ListSortLocalEntity.RECENTLY_UPDATED) shouldBe listOf(nameLast, nameFirst)
        }

        test("TC-fdd70132-3ae7-46e0-8cb8-bf4fb62a3a23 고른 연락처는 고른 순서와 관계없이 이름 오름차순으로 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val first = contact(name = FIRST_NAME)
            val middle = contact(name = MIDDLE_NAME, isFavorite = true, updatedAt = NEW_INSTANT)
            val last = contact(name = LAST_NAME)
            transaction.upsert(accountId = accountId, contactList = listOf(middle, last, first))

            dataSource
                .get(accountId = accountId, contactIdSet = linkedSetOf(last.id, first.id, middle.id))
                .first()
                .map { contact -> contact.id } shouldBe listOf(first.id, middle.id, last.id)
        }

        test("TC-c341b505-225c-4c85-99b8-1dc9512ea71a 각 축은 정해진 기준으로 연락처의 유무를 판정한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val solarBirthday = contact(name = FIRST_NAME, phoneNumberList = emptyList()).withBirthday(calendar = ContactBirthdayCalendarLocalEntity.SOLAR)
            val lunarBirthday = contact(name = ANOTHER_NAME, phoneNumberList = emptyList()).withBirthday(calendar = ContactBirthdayCalendarLocalEntity.LUNAR)
            val birthYearOnly = contact(name = MIDDLE_NAME, phoneNumberList = listOf(phoneNumber())).let { contact -> contact.copy(detail = contact.detail.copy(birthYear = fixtureMonkey.giveMeOne<Int>())) }
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
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val both = contact(name = FIRST_NAME, phoneNumberList = listOf(phoneNumber())).withBirthday()
            val birthdayOnly = contact(name = ANOTHER_NAME, phoneNumberList = emptyList()).withBirthday()
            val birthYearOnly =
                contact(name = MIDDLE_NAME, phoneNumberList = emptyList()).let { contact ->
                    contact.copy(detail = contact.detail.copy(birthYear = fixtureMonkey.giveMeOne<Int>()))
                }
            val phoneNumberOnly = contact(name = LAST_NAME, phoneNumberList = listOf(phoneNumber()))
            val neither = contact(name = FIRST_NAME, phoneNumberList = emptyList())
            transaction.upsert(accountId = accountId, contactList = listOf(both, birthdayOnly, birthYearOnly, phoneNumberOnly, neither))

            page(accountId = accountId, sort = ListSortLocalEntity.NAME, hasBirthday = true, hasPhoneNumber = false) shouldBe listOf(birthdayOnly)
            page(accountId = accountId, sort = ListSortLocalEntity.NAME, hasBirthday = false, hasBirthYear = true) shouldBe listOf(birthYearOnly)
            page(accountId = accountId, sort = ListSortLocalEntity.NAME, hasBirthYear = false, hasPhoneNumber = true) shouldBe listOf(phoneNumberOnly)
        }

        test("TC-4b99af6a-d212-49df-9661-35bdc1a8a39f 유무 필터는 즐겨찾기 우선과 정렬 순서를 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
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
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
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
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
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
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val withBirthYear = contact(name = FIRST_NAME).let { contact -> contact.copy(detail = contact.detail.copy(birthYear = fixtureMonkey.giveMeOne<Int>())) }
            val withoutBirthYear = contact(name = LAST_NAME)
            transaction.upsert(accountId = accountId, contactList = listOf(withBirthYear, withoutBirthYear))
            page(accountId = accountId, sort = ListSortLocalEntity.NAME, hasBirthYear = true) shouldBe listOf(withBirthYear)

            val cleared = withBirthYear.copy(detail = withBirthYear.detail.copy(birthYear = null))
            val added = withoutBirthYear.copy(detail = withoutBirthYear.detail.copy(birthYear = fixtureMonkey.giveMeOne<Int>()))
            transaction.upsert(accountId = accountId, contactList = listOf(cleared, added))

            page(accountId = accountId, sort = ListSortLocalEntity.NAME, hasBirthYear = true) shouldBe listOf(added)
        }

        test("TC-2d15ffea-2441-4291-bf5d-4be0e7380d4c 필터를 적용한 동안 조건에 맞지 않는 연락처를 저장하면 목록에 나타나지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val shown = contact(name = FIRST_NAME)
            transaction.upsert(accountId = accountId, contactList = listOf(shown))
            page(accountId = accountId, sort = ListSortLocalEntity.NAME, hasBirthday = false) shouldBe listOf(shown)

            val withBirthday = contact(name = MIDDLE_NAME).withBirthday()
            val withoutBirthday = contact(name = LAST_NAME)
            transaction.upsert(accountId = accountId, contactList = listOf(withBirthday, withoutBirthday))

            page(accountId = accountId, sort = ListSortLocalEntity.NAME, hasBirthday = false) shouldBe listOf(shown, withoutBirthday)
        }

        test("TC-0ab3e2d9-b691-489e-bbf0-07e3daf5db6a 고향을 비우면 비어 있는 값으로 저장한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val stored =
                contact(name = FIRST_NAME).let { contact ->
                    contact.copy(detail = contact.detail.copy(hometown = ""))
                }
            transaction.upsert(accountId = accountId, contactList = listOf(stored))

            page(accountId = accountId, sort = ListSortLocalEntity.NAME)
                .single()
                .detail.hometown shouldBe ""
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

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun contact(
            name: String,
            isFavorite: Boolean = false,
            updatedAt: Instant = OLD_INSTANT,
            phoneNumberList: List<ContactPhoneNumberLocalEntity> = listOf(phoneNumber()),
        ): ContactLocalEntity =
            ContactLocalEntity(
                id = fixtureMonkey.giveMeOne<Uuid>(),
                detail =
                    ContactDetailLocalEntity(
                        name = name,
                        description = "description-${fixtureMonkey.giveMeOne<String>()}",
                        heightCentimeter = null,
                        footSizeMillimeter = null,
                        birthday = null,
                        birthdayCalendar = null,
                        birthYear = null,
                        hometown = "hometown-${fixtureMonkey.giveMeOne<String>()}",
                        phoneNumberList = phoneNumberList,
                    ),
                isFavorite = isFavorite,
                isDeleted = false,
                updatedAt = updatedAt,
                createdAt = OLD_INSTANT,
            )

        private fun phoneNumber(): ContactPhoneNumberLocalEntity = ContactPhoneNumberLocalEntity(number = fixtureMonkey.nonBlankString())

        private fun ContactLocalEntity.withBirthday(calendar: ContactBirthdayCalendarLocalEntity = ContactBirthdayCalendarLocalEntity.SOLAR): ContactLocalEntity = copy(detail = detail.copy(birthday = BIRTHDAY, birthdayCalendar = calendar, birthYear = BIRTHDAY.year))
    }
}
