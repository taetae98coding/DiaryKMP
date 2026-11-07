package io.github.taetae98coding.diary.core.database.impl.contact.transaction

import androidx.room3.Room
import androidx.room3.useReaderConnection
import androidx.room3.useWriterConnection
import androidx.sqlite.SQLiteStatement
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactBirthdayCalendarLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactPhoneNumberLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.contact.entity.AccountContactLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
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
                    SELECT id, name, description, height_centimeter, foot_size_millimeter, birthday, birthday_calendar, birth_year,
                        hometown, how_we_met, phone_number_list, is_favorite, is_deleted, updated_at, created_at
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

        test("TC-7e927777-fb91-4305-91d6-2edf09cea62d 연락처와 현재 계정의 연결을 함께 저장한다") {
            val accountId = Arb.kotlinUuid().next()
            val contact = contact()

            transaction.upsert(accountId = accountId, contactList = listOf(contact), contactTagList = emptyList())

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

        test("TC-94d69dd9-1da3-4891-bfdc-ec5a148ab86b 이름, 설명과 미삭제 상태, 추가 시각을 그대로 저장한다") {
            val accountId = Arb.kotlinUuid().next()
            val now = Arb.secondPrecisionInstant().next()
            val contact =
                contact().copy(
                    detail = detail().copy(name = "name-${Arb.string().next()}", description = ""),
                    isDeleted = false,
                    updatedAt = now,
                    createdAt = now,
                )

            transaction.upsert(accountId = accountId, contactList = listOf(contact), contactTagList = emptyList())

            findContactList() shouldBe listOf(contact)
        }

        test("TC-2395f8c2-2d07-437e-883f-9baa369e0049 전화번호는 저장한 순서 그대로 함께 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val phoneNumberList =
                listOf(
                    ContactPhoneNumberLocalEntity(number = "010-1234-5678"),
                    ContactPhoneNumberLocalEntity(number = "02-987-6543"),
                    ContactPhoneNumberLocalEntity(number = "010-1234-5678"),
                )
            val contact = contact().let { contact -> contact.copy(detail = contact.detail.copy(phoneNumberList = phoneNumberList)) }

            transaction.upsert(accountId = accountId, contactList = listOf(contact), contactTagList = emptyList())

            findContactList().single().detail.phoneNumberList shouldBe phoneNumberList
        }

        test("TC-54d47d79-d19d-4b62-9477-2f87590574a5 전화번호가 없는 연락처는 빈 전화번호 목록으로 저장된다") {
            val accountId = Arb.kotlinUuid().next()
            val contact = contact().let { contact -> contact.copy(detail = contact.detail.copy(phoneNumberList = emptyList())) }

            transaction.upsert(accountId = accountId, contactList = listOf(contact), contactTagList = emptyList())

            findContactList().single().detail.phoneNumberList shouldBe emptyList()
        }

        test("TC-d5481b57-845d-420d-900e-ec7f8527757b 생일 없이 저장한 태어난 연도를 그대로 함께 조회한다") {
            val accountId = Arb.kotlinUuid().next()
            val contact =
                contact().let { contact ->
                    contact.copy(detail = contact.detail.copy(birthday = null, birthdayCalendar = null, birthYear = Arb.kotlinxLocalDate().next().year))
                }

            transaction.upsert(accountId = accountId, contactList = listOf(contact), contactTagList = emptyList())

            findContactList().single().detail shouldBe contact.detail
        }

        test("TC-d455eef9-c217-4560-8686-15c4fff6eeaf 키, 신발 사이즈, 생일을 저장한 값 그대로 함께 조회한다") {
            val accountId = Arb.kotlinUuid().next()
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

            transaction.upsert(accountId = accountId, contactList = listOf(contact), contactTagList = emptyList())

            findContactList().single().detail shouldBe contact.detail
        }

        test("TC-60665eac-ca6d-4c82-b7a9-2e98d0d3d154 생일의 날짜와 달력 구분을 함께 저장한다") {
            val accountId = Arb.kotlinUuid().next()
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

            transaction.upsert(accountId = accountId, contactList = listOf(contact), contactTagList = emptyList())

            val stored = findContactList().single().detail
            stored.birthday shouldBe birthday
            stored.birthdayCalendar shouldBe ContactBirthdayCalendarLocalEntity.LUNAR
        }

        test("TC-e25bcfee-46d1-4d01-a055-c534dfff8889 생일이 없으면 날짜와 달력 구분을 모두 없는 상태로 저장한다") {
            val accountId = Arb.kotlinUuid().next()
            val contact =
                contact().let { contact ->
                    contact.copy(detail = contact.detail.copy(birthday = null, birthdayCalendar = null))
                }

            transaction.upsert(accountId = accountId, contactList = listOf(contact), contactTagList = emptyList())

            val stored = findContactList().single().detail
            stored.birthday.shouldBeNull()
            stored.birthdayCalendar.shouldBeNull()
        }

        test("TC-8df70bbc-e460-4a06-916f-a72e1bfd665d 키, 신발 사이즈, 생일이 없으면 없는 값으로 저장한다") {
            val accountId = Arb.kotlinUuid().next()
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

            transaction.upsert(accountId = accountId, contactList = listOf(contact), contactTagList = emptyList())

            val stored = findContactList().single().detail
            stored.heightCentimeter.shouldBeNull()
            stored.footSizeMillimeter.shouldBeNull()
            stored.birthday.shouldBeNull()
            stored.birthdayCalendar.shouldBeNull()
        }

        test("같은 식별자의 연락처를 다시 저장하면 덮어쓴다") {
            val accountId = Arb.kotlinUuid().next()
            val contact = contact()
            val changedContact = contact.copy(detail = detail())

            transaction.upsert(accountId = accountId, contactList = listOf(contact), contactTagList = emptyList())
            transaction.upsert(accountId = accountId, contactList = listOf(changedContact), contactTagList = emptyList())

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

        test("TC-94d69dd9-1da3-4891-bfdc-ec5a148ab86b 서로 다른 식별자의 연락처는 각각 저장한다") {
            val accountId = Arb.kotlinUuid().next()
            val contactList = List(3) { contact() }.sortedBy { contact -> contact.id.toString() }

            contactList.forEach { contact ->
                transaction.upsert(accountId = accountId, contactList = listOf(contact), contactTagList = emptyList())
            }

            findContactList() shouldBe contactList
        }

        test("TC-11e51a31-7a79-4ddc-8eac-ef6b45a9caa5 TC-6dd12398-c5b0-4e7d-b36c-c8a61031b697 연락처 추가·수정·삭제·실행 취소·즐겨찾기 변경은 업로드 대기 상태가 된다") {
            val accountId = Arb.kotlinUuid().next()
            val contact = contact().copy(isDeleted = false)
            val pending = listOf(AccountContactLocalEntity(accountId = accountId, contactId = contact.id, isDirty = true))
            val changeList: List<suspend () -> Unit> =
                listOf(
                    { transaction.updateDetail(accountId = accountId, contactId = contact.id, detail = detail(), updatedAt = Arb.secondPrecisionInstant().next()) },
                    { transaction.updateDeleted(accountId = accountId, contactId = contact.id, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next()) },
                    { transaction.updateDeleted(accountId = accountId, contactId = contact.id, isDeleted = false, updatedAt = Arb.secondPrecisionInstant().next()) },
                    {
                        transaction.updateFavorite(
                            accountId = accountId,
                            contactId = contact.id,
                            isFavorite = !contact.isFavorite,
                            updatedAt = Arb.secondPrecisionInstant().next(),
                        )
                    },
                )

            transaction.upsert(accountId = accountId, contactList = listOf(contact), contactTagList = emptyList())
            findAccountContactList() shouldBe pending

            changeList.forEach { change ->
                database.useWriterConnection { transactor ->
                    transactor.usePrepared("UPDATE account_contact SET is_dirty = 0") { statement -> statement.step() }
                }

                change()

                findAccountContactList() shouldBe pending
            }
        }

        test("TC-8549cc6d-5e8c-452f-948a-11916bc70255 수정한 태어난 연도를 기기에 저장한다") {
            val accountId = Arb.kotlinUuid().next()
            val birthYear = Arb.kotlinxLocalDate().next().year
            val contact =
                contact().let { contact ->
                    contact.copy(detail = contact.detail.copy(birthday = null, birthdayCalendar = null, birthYear = birthYear))
                }
            val updatedDetail = contact.detail.copy(birthYear = birthYear + 1)
            val updatedAt = Arb.secondPrecisionInstant().next()
            transaction.upsert(accountId = accountId, contactList = listOf(contact), contactTagList = emptyList())

            transaction.updateDetail(accountId = accountId, contactId = contact.id, detail = updatedDetail, updatedAt = updatedAt)

            findContactList().single().detail.birthYear shouldBe birthYear + 1
        }

        test("TC-f0aabb78-2348-41f2-8203-5ea73829103c 삭제 상태인 연락처도 즐겨찾기를 바꿀 수 있고 삭제 여부는 그대로 남는다") {
            val accountId = Arb.kotlinUuid().next()
            val contact = contact().copy(isFavorite = false, isDeleted = true)
            val updatedAt = Arb.secondPrecisionInstant().next()
            transaction.upsert(accountId = accountId, contactList = listOf(contact), contactTagList = emptyList())

            transaction.updateFavorite(accountId = accountId, contactId = contact.id, isFavorite = true, updatedAt = updatedAt)

            findContactList() shouldBe listOf(contact.copy(isFavorite = true, updatedAt = updatedAt))
        }

        test("TC-6ec3f675-b8e4-4edc-8e7f-9af3c7691cfa 수정은 전화번호를 반영한 순서 그대로 대체해 저장한다") {
            val accountId = Arb.kotlinUuid().next()
            val contact = contact()
            val phoneNumberList =
                listOf(
                    ContactPhoneNumberLocalEntity(number = "02-333-4444"),
                    ContactPhoneNumberLocalEntity(number = "010-1234-5678"),
                    ContactPhoneNumberLocalEntity(number = "031-555-6666"),
                )
            transaction.upsert(accountId = accountId, contactList = listOf(contact), contactTagList = emptyList())

            transaction.updateDetail(
                accountId = accountId,
                contactId = contact.id,
                detail = contact.detail.copy(phoneNumberList = phoneNumberList),
                updatedAt = Arb.secondPrecisionInstant().next(),
            ) shouldBe 1

            findContactList().single().detail.phoneNumberList shouldBe phoneNumberList
        }

        test("TC-1c371ce2-5734-4028-b428-301bc197ab5a 전화번호를 모두 지운 수정을 반영하면 전화번호가 남지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val contact = contact()
            transaction.upsert(accountId = accountId, contactList = listOf(contact), contactTagList = emptyList())

            transaction.updateDetail(
                accountId = accountId,
                contactId = contact.id,
                detail = contact.detail.copy(phoneNumberList = emptyList()),
                updatedAt = Arb.secondPrecisionInstant().next(),
            ) shouldBe 1

            findContactList().single().detail.phoneNumberList shouldBe emptyList()
        }

        test("TC-b55bd3d9-08dc-4afb-b09e-d24205aca28e 수정은 즐겨찾기 여부와 삭제 여부와 생성 시각과 계정 연결을 바꾸지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val contact = contact().copy(isFavorite = true, isDeleted = true)
            val updatedDetail = detail().copy(name = "updated-${Arb.string().next()}")
            val updatedAt = Arb.secondPrecisionInstant().next()
            transaction.upsert(accountId = accountId, contactList = listOf(contact), contactTagList = emptyList())

            transaction.updateDetail(accountId = accountId, contactId = contact.id, detail = updatedDetail, updatedAt = updatedAt) shouldBe 1

            findContactList() shouldBe listOf(contact.copy(detail = updatedDetail, updatedAt = updatedAt))
            findAccountContactList().map { accountContact -> accountContact.accountId to accountContact.contactId } shouldBe
                listOf(accountId to contact.id)
        }

        test("TC-21d1c705-308e-4ddc-bb00-1dcffba5d805 즐겨찾기 변경은 즐겨찾기 여부와 수정 시각만 저장한다") {
            val accountId = Arb.kotlinUuid().next()
            val contact = contact().copy(isFavorite = false)
            val updatedAt = Arb.secondPrecisionInstant().next()
            transaction.upsert(accountId = accountId, contactList = listOf(contact), contactTagList = emptyList())

            transaction.updateFavorite(accountId = accountId, contactId = contact.id, isFavorite = true, updatedAt = updatedAt) shouldBe 1

            findContactList() shouldBe listOf(contact.copy(isFavorite = true, updatedAt = updatedAt))
        }

        test("TC-9aaa0ba4-69e2-4de4-a225-3e64e1f24b30 삭제는 연락처와 계정 연결과 전화번호를 지우지 않고 삭제 여부와 수정 시각만 바꾼다") {
            val accountId = Arb.kotlinUuid().next()
            val contact = contact().copy(isFavorite = true, isDeleted = false)
            val updatedAt = Arb.secondPrecisionInstant().next()
            transaction.upsert(accountId = accountId, contactList = listOf(contact), contactTagList = emptyList())

            transaction.updateDeleted(accountId = accountId, contactId = contact.id, isDeleted = true, updatedAt = updatedAt) shouldBe 1

            findContactList() shouldBe listOf(contact.copy(isDeleted = true, updatedAt = updatedAt))
            findAccountContactList().map { accountContact -> accountContact.accountId to accountContact.contactId } shouldBe
                listOf(accountId to contact.id)
        }

        test("TC-92a80131-b9a8-457a-93e9-5aef7d57aea0 TC-7ff50880-b5cd-4f40-bde3-44fe777b0439 TC-11b8fded-b993-4a0c-8334-0da88e444e17 현재 계정과 연결되지 않은 연락처는 수정과 삭제와 즐겨찾기 변경이 바꾸지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val contact = contact()
            transaction.upsert(accountId = accountId, contactList = listOf(contact), contactTagList = emptyList())
            val before = findContactList()

            transaction.updateDetail(
                accountId = otherAccountId,
                contactId = contact.id,
                detail = detail().copy(name = "updated-${Arb.string().next()}"),
                updatedAt = Arb.secondPrecisionInstant().next(),
            ) shouldBe 0
            transaction.updateDeleted(accountId = otherAccountId, contactId = contact.id, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 0
            transaction.updateFavorite(accountId = otherAccountId, contactId = contact.id, isFavorite = true, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 0
            transaction.updateDetail(accountId = accountId, contactId = Arb.kotlinUuid().next(), detail = detail(), updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 0

            findContactList() shouldBe before
        }

        test("TC-1baea3d4-f655-46b8-8d62-64d922ba1d95 TC-e5d727a2-80a2-4f39-bc0f-a1295f84684d 저장에 실패하면 실패를 전달하고 저장된 연락처는 바뀌지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val contact = contact()
            val throwable = IllegalStateException(Arb.string().next())
            transaction.upsert(accountId = accountId, contactList = listOf(contact), contactTagList = emptyList())
            val failingDatabase = spyk(database)
            every { failingDatabase.accountContactDao() } throws throwable
            val failingTransaction = AccountContactTransactionImpl(database = failingDatabase)

            shouldThrow<IllegalStateException> {
                failingTransaction.updateDetail(accountId = accountId, contactId = contact.id, detail = detail(), updatedAt = Arb.secondPrecisionInstant().next())
            }.message shouldBe throwable.message
            shouldThrow<IllegalStateException> {
                failingTransaction.updateDeleted(accountId = accountId, contactId = contact.id, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next())
            }.message shouldBe throwable.message
            shouldThrow<IllegalStateException> {
                failingTransaction.updateFavorite(accountId = accountId, contactId = contact.id, isFavorite = true, updatedAt = Arb.secondPrecisionInstant().next())
            }.message shouldBe throwable.message

            findContactList() shouldBe listOf(contact)
        }

        test("TC-05a1f42a-bfb1-4a3d-b30b-7594415bdedf 저장에 실패하면 연락처와 계정 연결 중 어느 것도 남지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val contact = contact()
            val throwable = IllegalStateException(Arb.string().next())
            val failingDatabase = spyk(database)
            every { failingDatabase.accountContactDao() } throws throwable
            val failingTransaction = AccountContactTransactionImpl(database = failingDatabase)

            shouldThrow<IllegalStateException> {
                failingTransaction.upsert(accountId = accountId, contactList = listOf(contact), contactTagList = emptyList())
            }.message shouldBe throwable.message

            findContactList().shouldBeEmpty()
            findAccountContactList().shouldBeEmpty()
        }
    }) {
    public companion object {
        private fun contact(): ContactLocalEntity =
            ContactLocalEntity(
                id = Arb.kotlinUuid().next(),
                detail = detail(),
                isFavorite = false,
                isDeleted = false,
                updatedAt = Arb.secondPrecisionInstant().next(),
                createdAt = Arb.secondPrecisionInstant().next(),
            )

        private fun detail(): ContactDetailLocalEntity =
            ContactDetailLocalEntity(
                name = "name-${Arb.string().next()}",
                description = "description-${Arb.string().next()}",
                heightCentimeter = 175.5,
                footSizeMillimeter = 250,
                birthday = LocalDate(year = 1998, month = 5, day = 12),
                birthdayCalendar = ContactBirthdayCalendarLocalEntity.SOLAR,
                birthYear = 1998,
                hometown = "hometown-${Arb.string().next()}",
                howWeMet = "how-we-met-${Arb.string().next()}",
                phoneNumberList = listOf(ContactPhoneNumberLocalEntity(number = "010-1234-5678")),
            )

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
                        birthYear = if (isNull(7)) null else getInt(7),
                        hometown = getText(8),
                        howWeMet = getText(9),
                        phoneNumberList = Json.decodeFromString(getText(10)),
                    ),
                isFavorite = getBoolean(11),
                isDeleted = getBoolean(12),
                updatedAt = Instant.fromEpochMilliseconds(getLong(13)),
                createdAt = Instant.fromEpochMilliseconds(getLong(14)),
            )

        private fun SQLiteStatement.toAccountContact(): AccountContactLocalEntity =
            AccountContactLocalEntity(
                accountId = Uuid.parse(getText(0)),
                contactId = Uuid.parse(getText(1)),
                isDirty = getBoolean(2),
            )
    }
}
