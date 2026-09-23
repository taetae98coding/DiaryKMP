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
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
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
        ): List<ContactLocalEntity> {
            val page =
                dataSource.page(accountId = accountId, sort = sort).load(
                    PagingSource.LoadParams.Refresh(
                        key = null,
                        loadSize = PAGE_SIZE,
                        placeholdersEnabled = false,
                    ),
                )

            return (page as PagingSource.LoadResult.Page).data
        }

        test("TC-CONTACT-HOME-DOMAIN-007 즐겨찾기인 연락처가 고른 기준과 무관하게 앞에 놓인다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val favorite = contact(name = LAST_NAME, isFavorite = true, updatedAt = OLD_INSTANT)
            val other = contact(name = FIRST_NAME, isFavorite = false, updatedAt = NEW_INSTANT)
            transaction.upsert(accountId = accountId, contactList = listOf(favorite, other))

            ListSortLocalEntity.entries.forEach { sort ->
                page(accountId = accountId, sort = sort) shouldBe listOf(favorite, other)
            }
        }

        test("TC-CONTACT-HOME-DOMAIN-008 즐겨찾기 묶음 안에서는 고른 기준의 순서가 적용된다") {
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

        test("TC-CONTACT-HOME-DOMAIN-009 즐겨찾기 우선은 목록에 노출하는 연락처를 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val favorite = contact(name = FIRST_NAME, isFavorite = true)
            val deletedFavorite = contact(name = MIDDLE_NAME, isFavorite = true).copy(isDeleted = true)
            val otherAccountFavorite = contact(name = ANOTHER_NAME, isFavorite = true)
            transaction.upsert(accountId = accountId, contactList = listOf(favorite, deletedFavorite))
            transaction.upsert(accountId = otherAccountId, contactList = listOf(otherAccountFavorite))

            page(accountId = accountId, sort = ListSortLocalEntity.NAME) shouldBe listOf(favorite)
        }

        test("TC-CONTACT-HOME-DATA-006 TC-CONTACT-ADD-DATA-007 생일과 달력 구분, 고향, 즐겨찾기 여부가 함께 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val stored =
                contact(name = FIRST_NAME, isFavorite = true).let { contact ->
                    contact.copy(
                        detail =
                            contact.detail.copy(
                                birthday = BIRTHDAY,
                                birthdayCalendar = ContactBirthdayCalendarLocalEntity.LUNAR,
                                hometown = HOMETOWN,
                            ),
                    )
                }
            transaction.upsert(accountId = accountId, contactList = listOf(stored))

            page(accountId = accountId, sort = ListSortLocalEntity.NAME) shouldBe listOf(stored)
        }

        test("TC-CONTACT-HOME-DATA-007 즐겨찾기 우선을 조회 조건으로 넘겨 정렬된 순서로 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val favorite = contact(name = LAST_NAME, isFavorite = true)
            val other = contact(name = FIRST_NAME, isFavorite = false)
            transaction.upsert(accountId = accountId, contactList = listOf(other, favorite))

            page(accountId = accountId, sort = ListSortLocalEntity.NAME) shouldBe listOf(favorite, other)
        }

        test("TC-CONTACT-ADD-DATA-008 고향을 비우면 비어 있는 값으로 저장한다") {
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
                        hometown = "hometown-${fixtureMonkey.giveMeOne<String>()}",
                        phoneNumberList = listOf(ContactPhoneNumberLocalEntity(number = "010-1234-5678")),
                    ),
                isFavorite = isFavorite,
                isDeleted = false,
                updatedAt = updatedAt,
                createdAt = OLD_INSTANT,
            )
    }
}
