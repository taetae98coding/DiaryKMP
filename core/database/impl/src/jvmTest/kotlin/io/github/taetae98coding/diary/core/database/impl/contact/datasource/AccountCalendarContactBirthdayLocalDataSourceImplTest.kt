package io.github.taetae98coding.diary.core.database.impl.contact.datasource

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.contact.entity.CalendarContactBirthdayLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactBirthdayCalendarLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.LunarContactBirthdayLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.contact.transaction.AccountContactTransactionImpl
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountCalendarContactBirthdayLocalDataSourceImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var dataSource: AccountCalendarContactBirthdayLocalDataSourceImpl
        lateinit var transaction: AccountContactTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            dataSource = AccountCalendarContactBirthdayLocalDataSourceImpl(database = database)
            transaction = AccountContactTransactionImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun upsert(
            accountId: Uuid,
            vararg contactList: ContactLocalEntity,
        ) {
            transaction.upsert(accountId = accountId, contactList = contactList.toList())
        }

        suspend fun birthdayList(
            accountId: Uuid,
            start: LocalDate = RANGE_START,
            endInclusive: LocalDate = RANGE_END_INCLUSIVE,
        ): List<CalendarContactBirthdayLocalEntity> =
            dataSource
                .get(
                    accountId = accountId,
                    dateRange = start..endInclusive,
                ).first()

        suspend fun birthdayContactIdList(
            accountId: Uuid,
            start: LocalDate = RANGE_START,
            endInclusive: LocalDate = RANGE_END_INCLUSIVE,
        ): List<Uuid> =
            birthdayList(accountId = accountId, start = start, endInclusive = endInclusive)
                .map { birthday -> birthday.contactId }

        test("TC-562c7066-a181-42d8-b661-036748dca665 표시 대상 기간에 드는 생일만 표시 대상이 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val contactList =
                listOf(
                    contact(birthday = LocalDate(1990, 7, 4)) to false,
                    contact(birthday = LocalDate(1990, 7, 5)) to true,
                    contact(birthday = LocalDate(1990, 7, 8)) to true,
                    contact(birthday = LocalDate(1990, 7, 11)) to true,
                    contact(birthday = LocalDate(1990, 7, 12)) to false,
                )
            upsert(accountId, *contactList.map { (contact, _) -> contact }.toTypedArray())

            birthdayContactIdList(accountId = accountId) shouldContainExactlyInAnyOrder
                contactList.filter { (_, isIncluded) -> isIncluded }.map { (contact, _) -> contact.id }
        }

        test("TC-21b7fda4-cbaa-4c17-8b8c-22c5b592f60a 생일이 없는 연락처는 태어난 연도가 있어도 표시하지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val noBirthdayContact = contact(birthday = null)
            val birthYearOnlyContact = contact(birthday = null, birthYear = RANGE_START.year)
            val birthdayContact = contact(birthday = LocalDate(1990, 7, 8))

            upsert(accountId, noBirthdayContact, birthYearOnlyContact, birthdayContact)

            birthdayContactIdList(accountId = accountId) shouldBe listOf(birthdayContact.id)
        }

        test("TC-ecc2acc9-313a-431e-a6da-4922f67e605d 삭제된 연락처의 생일은 표시하지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val deletedContact = contact(birthday = LocalDate(1990, 7, 8)).copy(isDeleted = true)
            val contact = contact(birthday = LocalDate(1990, 7, 9))

            upsert(accountId, deletedContact, contact)

            birthdayContactIdList(accountId = accountId) shouldBe listOf(contact.id)
        }

        test("TC-11809f6b-1f91-45a2-9601-642607c666e7 현재 사용자 계정과 연결된 연락처의 생일만 표시한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val contact = contact(birthday = LocalDate(1990, 7, 8))
            val otherContact = contact(birthday = LocalDate(1990, 7, 9))

            upsert(accountId, contact)
            upsert(otherAccountId, otherContact)

            birthdayContactIdList(accountId = accountId) shouldBe listOf(contact.id)
            birthdayContactIdList(accountId = otherAccountId) shouldBe listOf(otherContact.id)
        }

        test("TC-7c916cff-11ea-4f54-aa3c-01cb3bdf4796 태어난 해부터 이후 연도의 같은 월·일을 차지한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val contact = contact(birthday = LocalDate(1990, 7, 8))
            upsert(accountId, contact)

            val expectedDateByYear =
                listOf(
                    1989 to null,
                    1990 to LocalDate(1990, 7, 8),
                    1991 to LocalDate(1991, 7, 8),
                    2026 to LocalDate(2026, 7, 8),
                )

            expectedDateByYear.forEach { (year, expectedDate) ->
                val birthdayList =
                    birthdayList(
                        accountId = accountId,
                        start = LocalDate(year, 7, 5),
                        endInclusive = LocalDate(year, 7, 11),
                    )

                if (expectedDate == null) {
                    birthdayList.shouldBeEmpty()
                } else {
                    birthdayList.map { birthday -> birthday.birthdayDate } shouldBe listOf(expectedDate)
                }
            }
        }

        test("TC-989bfb60-35b7-4ace-afb7-510c1b712b67 표시 대상 연도의 양력 달력에 없는 월·일은 표시하지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val contact = contact(birthday = LocalDate(2020, 2, 29))
            upsert(accountId, contact)

            val expectedDateByYear =
                listOf(
                    2024 to LocalDate(2024, 2, 29),
                    2025 to null,
                    2026 to null,
                    2028 to LocalDate(2028, 2, 29),
                )

            expectedDateByYear.forEach { (year, expectedDate) ->
                val birthdayList =
                    birthdayList(
                        accountId = accountId,
                        start = LocalDate(year, 2, 24),
                        endInclusive = LocalDate(year, 3, 8),
                    )

                if (expectedDate == null) {
                    birthdayList.shouldBeEmpty()
                } else {
                    birthdayList.map { birthday -> birthday.birthdayDate } shouldBe listOf(expectedDate)
                }
            }
        }

        test("TC-ba930941-ab6e-4585-827a-b12ba6732563 날짜와 이름 순으로 정렬해 전달한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val laterDateContact = contact(birthday = LocalDate(1990, 7, 9), name = "가")
            val sameDateSecondNameContact = contact(birthday = LocalDate(1990, 7, 8), name = "나")
            val sameDateFirstNameContact = contact(birthday = LocalDate(1990, 7, 8), name = "가")

            upsert(accountId, laterDateContact, sameDateSecondNameContact, sameDateFirstNameContact)

            birthdayContactIdList(accountId = accountId) shouldContainExactly
                listOf(sameDateFirstNameContact.id, sameDateSecondNameContact.id, laterDateContact.id)
        }

        test("TC-ba930941-ab6e-4585-827a-b12ba6732563 날짜와 이름이 같으면 식별값의 오름차순으로 전달한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val name = fixtureMonkey.giveMeOne<String>()
            val contactList = List(3) { contact(birthday = LocalDate(1990, 7, 8), name = name) }

            upsert(accountId, *contactList.toTypedArray())

            birthdayContactIdList(accountId = accountId) shouldContainExactly
                contactList.map { contact -> contact.id }.sortedBy { id -> id.toString() }
        }

        test("TC-5f13616c-6d29-4f52-8cce-f7ff4b7468f3 즐겨찾기 여부는 생일의 노출과 순서를 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val notFavoriteContact = contact(birthday = LocalDate(1990, 7, 8), name = "가").copy(isFavorite = false)
            val favoriteContact = contact(birthday = LocalDate(1990, 7, 8), name = "나").copy(isFavorite = true)

            upsert(accountId, favoriteContact, notFavoriteContact)

            birthdayContactIdList(accountId = accountId) shouldContainExactly listOf(notFavoriteContact.id, favoriteContact.id)
        }

        test("TC-2df44f71-0a98-463b-a3f2-660a9d421b77 같은 표시 대상 기간을 다시 조회해도 순서가 유지된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val contactList = List(3) { index -> contact(birthday = LocalDate(1990, 7, 6 + index)) }

            upsert(accountId, *contactList.toTypedArray())

            birthdayContactIdList(accountId = accountId) shouldContainExactly birthdayContactIdList(accountId = accountId)
        }

        test("TC-03255112-291c-49e2-a12b-b57929da0e7b 표시 대상 기간이 바뀌면 그 기간을 기준으로 다시 정해진다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val julyContact = contact(birthday = LocalDate(1990, 7, 8))
            val augustContact = contact(birthday = LocalDate(1990, 8, 8))

            upsert(accountId, julyContact, augustContact)

            birthdayContactIdList(accountId = accountId) shouldBe listOf(julyContact.id)
            birthdayContactIdList(
                accountId = accountId,
                start = LocalDate(2026, 8, 5),
                endInclusive = LocalDate(2026, 8, 11),
            ) shouldBe listOf(augustContact.id)
        }

        test("TC-ab1b184d-62ca-4905-b709-67c388bc0a26 연락처 이름이 바뀌면 조작 없이 결과에 반영된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val contact = contact(birthday = LocalDate(1990, 7, 8), name = "기존 이름")
            upsert(accountId, contact)

            dataSource
                .get(accountId = accountId, dateRange = RANGE_START..RANGE_END_INCLUSIVE)
                .test {
                    awaitItem().map { birthday -> birthday.name } shouldBe listOf("기존 이름")

                    upsert(accountId, contact.withName("새 이름"))

                    awaitItem().map { birthday -> birthday.name } shouldBe listOf("새 이름")
                    cancelAndIgnoreRemainingEvents()
                }
        }

        test("TC-0fe37213-820c-413b-8295-0a24165035e2 표시할 생일이 없으면 빈 결과를 전달한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()

            upsert(accountId, contact(birthday = LocalDate(1990, 8, 8)))

            birthdayList(accountId = accountId).shouldBeEmpty()
        }

        test("TC-e152d30e-9dd7-4cb9-ae10-efaa737bdb50 연도 경계를 넘는 표시 대상 기간은 각 연도의 생일을 함께 담는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val yearEndContact = contact(birthday = LocalDate(1990, 12, 30))
            val yearStartContact = contact(birthday = LocalDate(1990, 1, 2))

            upsert(accountId, yearEndContact, yearStartContact)

            birthdayList(
                accountId = accountId,
                start = LocalDate(2025, 12, 28),
                endInclusive = LocalDate(2026, 1, 3),
            ).map { birthday -> birthday.contactId to birthday.birthdayDate } shouldContainExactly
                listOf(
                    yearEndContact.id to LocalDate(2025, 12, 30),
                    yearStartContact.id to LocalDate(2026, 1, 2),
                )
        }

        test("TC-b1731a38-4521-4b1f-bca4-5ab2d0a725bd 여러 연도를 넘는 표시 대상 기간은 사이에 있는 연도의 생일도 함께 담는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val contact = contact(birthday = LocalDate(1990, 7, 10))

            upsert(accountId, contact)

            birthdayList(
                accountId = accountId,
                start = LocalDate(2024, 12, 1),
                endInclusive = LocalDate(2026, 1, 31),
            ).map { birthday -> birthday.contactId to birthday.birthdayDate } shouldContainExactly
                listOf(contact.id to LocalDate(2025, 7, 10))
        }

        test("TC-ae1c2d0e-ecb8-4b0c-9a26-8a83277c8ac6 생일이 지워지면 결과에서 빠진다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val contact = contact(birthday = LocalDate(1990, 7, 8))
            upsert(accountId, contact)

            dataSource
                .get(accountId = accountId, dateRange = RANGE_START..RANGE_END_INCLUSIVE)
                .test {
                    awaitItem().map { birthday -> birthday.contactId } shouldBe listOf(contact.id)

                    upsert(accountId, contact.withBirthday(birthday = null))

                    awaitItem().shouldBeEmpty()
                    cancelAndIgnoreRemainingEvents()
                }
        }

        test("TC-d77b7322-ae31-444b-b9c1-19d533da895f 생일이 채워지면 결과에 들어온다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val contact = contact(birthday = null)
            upsert(accountId, contact)

            dataSource
                .get(accountId = accountId, dateRange = RANGE_START..RANGE_END_INCLUSIVE)
                .test {
                    awaitItem().shouldBeEmpty()

                    upsert(accountId, contact.withBirthday(birthday = LocalDate(1990, 7, 8)))

                    awaitItem().map { birthday -> birthday.contactId } shouldBe listOf(contact.id)
                    cancelAndIgnoreRemainingEvents()
                }
        }

        test("TC-27426d2e-863d-49cf-8657-5c44cc4058e0 연락처가 삭제되면 결과에서 빠진다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val contact = contact(birthday = LocalDate(1990, 7, 8))
            upsert(accountId, contact)

            dataSource
                .get(accountId = accountId, dateRange = RANGE_START..RANGE_END_INCLUSIVE)
                .test {
                    awaitItem().map { birthday -> birthday.contactId } shouldBe listOf(contact.id)

                    upsert(accountId, contact.copy(isDeleted = true))

                    awaitItem().shouldBeEmpty()
                    cancelAndIgnoreRemainingEvents()
                }
        }

        test("TC-4a43bb4f-f132-41c6-98f1-1d2491566655 생일이 기간에 들지 않는 월·일로 바뀌면 결과에서 빠진다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val contact = contact(birthday = LocalDate(1990, 7, 8))
            upsert(accountId, contact)

            dataSource
                .get(accountId = accountId, dateRange = RANGE_START..RANGE_END_INCLUSIVE)
                .test {
                    awaitItem().map { birthday -> birthday.contactId } shouldBe listOf(contact.id)

                    upsert(accountId, contact.withBirthday(birthday = LocalDate(1990, 8, 8)))

                    awaitItem().shouldBeEmpty()
                    cancelAndIgnoreRemainingEvents()
                }
        }

        test("TC-c866baad-5624-4ebc-8611-979fabb71da3 표시 대상 기간에 드는 생일을 가진 연락처가 새로 저장되면 결과에 들어온다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val contact = contact(birthday = LocalDate(1990, 7, 8))

            dataSource
                .get(accountId = accountId, dateRange = RANGE_START..RANGE_END_INCLUSIVE)
                .test {
                    awaitItem().shouldBeEmpty()

                    upsert(accountId, contact)

                    awaitItem().map { birthday -> birthday.contactId } shouldBe listOf(contact.id)
                    cancelAndIgnoreRemainingEvents()
                }
        }

        test("표시 대상 기간의 시작일과 종료일이 같아도 그날의 생일을 담는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val contact = contact(birthday = LocalDate(1990, 7, 8))

            upsert(accountId, contact)

            birthdayContactIdList(
                accountId = accountId,
                start = LocalDate(2026, 7, 8),
                endInclusive = LocalDate(2026, 7, 8),
            ) shouldBe listOf(contact.id)
        }

        test("양력 생일 조회는 음력 생일 연락처를 담지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val solarContact = contact(birthday = LocalDate(1990, 7, 8))
            val lunarContact = contact(birthday = LocalDate(1990, 7, 8), birthdayCalendar = ContactBirthdayCalendarLocalEntity.LUNAR)

            upsert(accountId, solarContact, lunarContact)

            birthdayContactIdList(accountId = accountId) shouldBe listOf(solarContact.id)
        }

        test("달력 구분이 없는 생일은 양력으로 다룬다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val contact = contact(birthday = LocalDate(1990, 7, 8), birthdayCalendar = null)

            upsert(accountId, contact)

            birthdayContactIdList(accountId = accountId) shouldBe listOf(contact.id)
        }

        test("음력 생일 조회는 현재 계정의 삭제되지 않은 음력 생일 연락처만 저장된 날짜 그대로 담는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val lunarContact = contact(birthday = LocalDate(1990, 7, 8), name = "나", birthdayCalendar = ContactBirthdayCalendarLocalEntity.LUNAR)
            val firstByNameLunarContact = contact(birthday = LocalDate(1991, 2, 28), name = "가", birthdayCalendar = ContactBirthdayCalendarLocalEntity.LUNAR)
            val solarContact = contact(birthday = LocalDate(1990, 7, 8), name = "다")
            val deletedLunarContact = contact(birthday = LocalDate(1990, 7, 8), name = "라", birthdayCalendar = ContactBirthdayCalendarLocalEntity.LUNAR).copy(isDeleted = true)
            val noBirthdayContact = contact(birthday = null, name = "마")
            val otherAccountLunarContact = contact(birthday = LocalDate(1990, 7, 8), name = "바", birthdayCalendar = ContactBirthdayCalendarLocalEntity.LUNAR)

            upsert(accountId, lunarContact, firstByNameLunarContact, solarContact, deletedLunarContact, noBirthdayContact)
            upsert(otherAccountId, otherAccountLunarContact)

            dataSource.getLunar(accountId = accountId).first() shouldBe
                listOf(
                    LunarContactBirthdayLocalEntity(contactId = firstByNameLunarContact.id, name = "가", birthday = LocalDate(1991, 2, 28)),
                    LunarContactBirthdayLocalEntity(contactId = lunarContact.id, name = "나", birthday = LocalDate(1990, 7, 8)),
                )
        }

        test("음력 생일 조회 중에 연락처가 바뀌면 새 목록을 제공한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val contact = contact(birthday = LocalDate(1990, 7, 8), birthdayCalendar = ContactBirthdayCalendarLocalEntity.LUNAR)

            dataSource.getLunar(accountId = accountId).test {
                awaitItem().shouldBeEmpty()

                upsert(accountId, contact)

                awaitItem().map { birthday -> birthday.contactId } shouldBe listOf(contact.id)
                cancelAndIgnoreRemainingEvents()
            }
        }

        test("태어난 해가 표시 대상 기간의 두 연도 중 뒤쪽이면 뒤쪽 연도의 생일만 담는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val contact = contact(birthday = LocalDate(2026, 1, 2))

            upsert(accountId, contact)

            birthdayList(
                accountId = accountId,
                start = LocalDate(2025, 12, 28),
                endInclusive = LocalDate(2026, 1, 3),
            ).map { birthday -> birthday.birthdayDate } shouldBe listOf(LocalDate(2026, 1, 2))
        }
    }) {
    private companion object {
        private val RANGE_START: LocalDate = LocalDate(2026, 7, 5)
        private val RANGE_END_INCLUSIVE: LocalDate = LocalDate(2026, 7, 11)

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun instant(): Instant = fixtureMonkey.giveMeOne<Instant>()

        private fun contact(
            birthday: LocalDate?,
            name: String = fixtureMonkey.giveMeOne<String>(),
            birthdayCalendar: ContactBirthdayCalendarLocalEntity? = ContactBirthdayCalendarLocalEntity.SOLAR,
            birthYear: Int? = birthday?.year,
        ): ContactLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<ContactLocalEntity>()
                .setExp(ContactLocalEntity::updatedAt, instant())
                .setExp(ContactLocalEntity::createdAt, instant())
                .sample()
                .let { contact ->
                    contact.copy(
                        isDeleted = false,
                        detail =
                            contact.detail.copy(
                                name = name,
                                birthday = birthday,
                                birthdayCalendar = birthday?.let { birthdayCalendar },
                                birthYear = birthYear,
                            ),
                    )
                }

        private fun ContactLocalEntity.withName(name: String): ContactLocalEntity = copy(detail = detail.copy(name = name))

        private fun ContactLocalEntity.withBirthday(birthday: LocalDate?): ContactLocalEntity =
            copy(
                detail =
                    detail.copy(
                        birthday = birthday,
                        birthdayCalendar = birthday?.let { ContactBirthdayCalendarLocalEntity.SOLAR },
                    ),
            )
    }
}
