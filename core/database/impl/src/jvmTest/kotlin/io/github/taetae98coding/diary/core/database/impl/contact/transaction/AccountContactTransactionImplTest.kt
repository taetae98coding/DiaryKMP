package io.github.taetae98coding.diary.core.database.impl.contact.transaction

import androidx.room3.Room
import androidx.room3.useReaderConnection
import androidx.room3.useWriterConnection
import androidx.sqlite.SQLiteStatement
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactBirthdayCalendarLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactPhoneNumberLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.contact.entity.AccountContactLocalEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.spyk
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountContactTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var transaction: AccountContactTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            transaction = AccountContactTransactionImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun findContactList(): List<ContactLocalEntity> =
            database.useReaderConnection { transactor ->
                transactor.usePrepared(
                    """
                    SELECT id, name, description, height_centimeter, foot_size_millimeter, birthday, birthday_calendar,
                        hometown, phone_number_list, is_favorite, is_deleted, updated_at, created_at
                    FROM contact
                    ORDER BY id ASC
                    """,
                ) { statement -> statement.readAll { it.toContact() } }
            }

        suspend fun findAccountContactList(): List<AccountContactLocalEntity> =
            database.useReaderConnection { transactor ->
                transactor.usePrepared(
                    """
                    SELECT account_id, contact_id, is_dirty
                    FROM account_contact
                    ORDER BY contact_id ASC
                    """,
                ) { statement -> statement.readAll { it.toAccountContact() } }
            }

        test("TC-CONTACT-ADD-DATA-001 연락처와 현재 계정의 연결을 함께 저장한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val contact = contact()

            transaction.upsert(accountId = accountId, contactList = listOf(contact))

            findContactList() shouldBe listOf(contact)
            findAccountContactList() shouldBe
                listOf(
                    AccountContactLocalEntity(
                        accountId = accountId,
                        contactId = contact.id,
                        isDirty = true,
                    ),
                )
        }

        test("TC-CONTACT-ADD-DOMAIN-009 이름, 설명과 미삭제 상태, 추가 시각을 그대로 저장한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val now = instant()
            val contact =
                contact().copy(
                    detail = detail().copy(name = "name-${fixtureMonkey.giveMeOne<String>()}", description = ""),
                    isDeleted = false,
                    updatedAt = now,
                    createdAt = now,
                )

            transaction.upsert(accountId = accountId, contactList = listOf(contact))

            findContactList() shouldBe listOf(contact)
        }

        test("TC-CONTACT-ADD-DATA-002 전화번호는 저장한 순서 그대로 함께 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val phoneNumberList =
                listOf(
                    ContactPhoneNumberLocalEntity(number = "010-1234-5678"),
                    ContactPhoneNumberLocalEntity(number = "02-987-6543"),
                    ContactPhoneNumberLocalEntity(number = "010-1234-5678"),
                )
            val contact = contact().let { contact -> contact.copy(detail = contact.detail.copy(phoneNumberList = phoneNumberList)) }

            transaction.upsert(accountId = accountId, contactList = listOf(contact))

            findContactList().single().detail.phoneNumberList shouldBe phoneNumberList
        }

        test("TC-CONTACT-ADD-DOMAIN-004 전화번호가 없는 연락처는 빈 전화번호 목록으로 저장된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val contact = contact().let { contact -> contact.copy(detail = contact.detail.copy(phoneNumberList = emptyList())) }

            transaction.upsert(accountId = accountId, contactList = listOf(contact))

            findContactList().single().detail.phoneNumberList shouldBe emptyList()
        }

        test("TC-CONTACT-ADD-DATA-004 키, 신발 사이즈, 생일을 저장한 값 그대로 함께 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val contact =
                contact().let { contact ->
                    contact.copy(
                        detail =
                            contact.detail.copy(
                                heightCentimeter = 175.5,
                                footSizeMillimeter = 250,
                                birthday = LocalDate(year = 1998, month = 5, day = 12),
                            ),
                    )
                }

            transaction.upsert(accountId = accountId, contactList = listOf(contact))

            findContactList().single().detail shouldBe contact.detail
        }

        test("TC-CONTACT-ADD-DATA-005 생일의 날짜와 달력 구분을 함께 저장한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val birthday = LocalDate(year = 1998, month = 5, day = 12)
            val contact =
                contact().let { contact ->
                    contact.copy(
                        detail =
                            contact.detail.copy(
                                birthday = birthday,
                                birthdayCalendar = ContactBirthdayCalendarLocalEntity.LUNAR,
                            ),
                    )
                }

            transaction.upsert(accountId = accountId, contactList = listOf(contact))

            val stored = findContactList().single().detail
            stored.birthday shouldBe birthday
            stored.birthdayCalendar shouldBe ContactBirthdayCalendarLocalEntity.LUNAR
        }

        test("TC-CONTACT-ADD-DATA-006 생일이 없으면 날짜와 달력 구분을 모두 없는 상태로 저장한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val contact =
                contact().let { contact ->
                    contact.copy(detail = contact.detail.copy(birthday = null, birthdayCalendar = null))
                }

            transaction.upsert(accountId = accountId, contactList = listOf(contact))

            val stored = findContactList().single().detail
            stored.birthday.shouldBeNull()
            stored.birthdayCalendar.shouldBeNull()
        }

        test("TC-CONTACT-ADD-DOMAIN-012 키, 신발 사이즈, 생일이 없으면 없는 값으로 저장한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val contact =
                contact().let { contact ->
                    contact.copy(
                        detail =
                            contact.detail.copy(
                                heightCentimeter = null,
                                footSizeMillimeter = null,
                                birthday = null,
                                birthdayCalendar = null,
                            ),
                    )
                }

            transaction.upsert(accountId = accountId, contactList = listOf(contact))

            val stored = findContactList().single().detail
            stored.heightCentimeter.shouldBeNull()
            stored.footSizeMillimeter.shouldBeNull()
            stored.birthday.shouldBeNull()
            stored.birthdayCalendar.shouldBeNull()
        }

        test("같은 식별자의 연락처를 다시 저장하면 덮어쓴다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val contact = contact()
            val changedContact = contact.copy(detail = detail())

            transaction.upsert(accountId = accountId, contactList = listOf(contact))
            transaction.upsert(accountId = accountId, contactList = listOf(changedContact))

            findContactList() shouldBe listOf(changedContact)
            findAccountContactList() shouldBe
                listOf(
                    AccountContactLocalEntity(
                        accountId = accountId,
                        contactId = contact.id,
                        isDirty = true,
                    ),
                )
        }

        test("TC-CONTACT-ADD-DOMAIN-009 서로 다른 식별자의 연락처는 각각 저장한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val contactList = List(3) { contact() }.sortedBy { contact -> contact.id.toString() }

            contactList.forEach { contact ->
                transaction.upsert(accountId = accountId, contactList = listOf(contact))
            }

            findContactList() shouldBe contactList
        }

        test("TC-DATA-SYNC-DOMAIN-001 TC-CONTACT-ADD-DATA-009 연락처 추가·수정·삭제·실행 취소·즐겨찾기 변경은 업로드 대기 상태가 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val contact = contact().copy(isDeleted = false)
            val pending = listOf(AccountContactLocalEntity(accountId = accountId, contactId = contact.id, isDirty = true))
            val changeList: List<suspend () -> Unit> =
                listOf(
                    { transaction.updateDetail(accountId = accountId, contactId = contact.id, detail = detail(), updatedAt = instant()) },
                    { transaction.updateDeleted(accountId = accountId, contactId = contact.id, isDeleted = true, updatedAt = instant()) },
                    { transaction.updateDeleted(accountId = accountId, contactId = contact.id, isDeleted = false, updatedAt = instant()) },
                    {
                        transaction.updateFavorite(
                            accountId = accountId,
                            contactId = contact.id,
                            isFavorite = !contact.isFavorite,
                            updatedAt = instant(),
                        )
                    },
                )

            transaction.upsert(accountId = accountId, contactList = listOf(contact))
            findAccountContactList() shouldBe pending

            changeList.forEach { change ->
                database.useWriterConnection { transactor ->
                    transactor.usePrepared("UPDATE account_contact SET is_dirty = 0") { statement -> statement.step() }
                }

                change()

                findAccountContactList() shouldBe pending
            }
        }

        test("TC-CONTACT-DETAIL-DOMAIN-018 삭제 상태인 연락처도 즐겨찾기를 바꿀 수 있고 삭제 여부는 그대로 남는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val contact = contact().copy(isFavorite = false, isDeleted = true)
            val updatedAt = instant()
            transaction.upsert(accountId = accountId, contactList = listOf(contact))

            transaction.updateFavorite(accountId = accountId, contactId = contact.id, isFavorite = true, updatedAt = updatedAt)

            findContactList() shouldBe listOf(contact.copy(isFavorite = true, updatedAt = updatedAt))
        }

        test("TC-CONTACT-ADD-DATA-003 저장에 실패하면 연락처와 계정 연결 중 어느 것도 남지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val contact = contact()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val failingDatabase = spyk(database)
            every { failingDatabase.accountContactDao() } throws throwable
            val failingTransaction = AccountContactTransactionImpl(database = failingDatabase)

            shouldThrow<IllegalStateException> {
                failingTransaction.upsert(accountId = accountId, contactList = listOf(contact))
            }.message shouldBe throwable.message

            findContactList().shouldBeEmpty()
            findAccountContactList().shouldBeEmpty()
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun contact(): ContactLocalEntity =
            ContactLocalEntity(
                id = fixtureMonkey.giveMeOne<Uuid>(),
                detail = detail(),
                isFavorite = false,
                isDeleted = false,
                updatedAt = instant(),
                createdAt = instant(),
            )

        private fun detail(): ContactDetailLocalEntity =
            ContactDetailLocalEntity(
                name = "name-${fixtureMonkey.giveMeOne<String>()}",
                description = "description-${fixtureMonkey.giveMeOne<String>()}",
                heightCentimeter = 175.5,
                footSizeMillimeter = 250,
                birthday = LocalDate(year = 1998, month = 5, day = 12),
                birthdayCalendar = ContactBirthdayCalendarLocalEntity.SOLAR,
                hometown = "hometown-${fixtureMonkey.giveMeOne<String>()}",
                phoneNumberList = listOf(ContactPhoneNumberLocalEntity(number = "010-1234-5678")),
            )

        private fun instant(): Instant = fixtureMonkey.giveMeOne<Instant>()

        private fun <T> SQLiteStatement.readAll(read: (SQLiteStatement) -> T): List<T> =
            buildList {
                while (step()) {
                    add(read(this@readAll))
                }
            }

        private fun SQLiteStatement.toContact(): ContactLocalEntity =
            ContactLocalEntity(
                id = Uuid.parse(getText(0)),
                detail =
                    ContactDetailLocalEntity(
                        name = getText(1),
                        description = getText(2),
                        heightCentimeter = if (isNull(3)) null else getDouble(3),
                        footSizeMillimeter = if (isNull(4)) null else getInt(4),
                        birthday = if (isNull(5)) null else LocalDate.parse(getText(5)),
                        birthdayCalendar = if (isNull(6)) null else ContactBirthdayCalendarLocalEntity.fromPersistentValue(getText(6)),
                        hometown = getText(7),
                        phoneNumberList = Json.decodeFromString(getText(8)),
                    ),
                isFavorite = getBoolean(9),
                isDeleted = getBoolean(10),
                updatedAt = Instant.fromEpochMilliseconds(getLong(11)),
                createdAt = Instant.fromEpochMilliseconds(getLong(12)),
            )

        private fun SQLiteStatement.toAccountContact(): AccountContactLocalEntity =
            AccountContactLocalEntity(
                accountId = Uuid.parse(getText(0)),
                contactId = Uuid.parse(getText(1)),
                isDirty = getBoolean(2),
            )
    }
}
