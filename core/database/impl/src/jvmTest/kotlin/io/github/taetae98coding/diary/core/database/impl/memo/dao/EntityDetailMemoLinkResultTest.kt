package io.github.taetae98coding.diary.core.database.impl.memo.dao

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.memocontact.entity.MemoContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.memoplace.entity.MemoPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.memoweb.entity.MemoWebLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.contact.entity.AccountContactLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memo.transaction.AccountMemoTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.place.entity.AccountPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.web.entity.AccountWebLocalEntity
import io.github.taetae98coding.diary.core.testing.contact.localContact
import io.github.taetae98coding.diary.core.testing.memo.localMemo
import io.github.taetae98coding.diary.core.testing.place.localPlace
import io.github.taetae98coding.diary.core.testing.web.localWeb
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class EntityDetailMemoLinkResultTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var transaction: AccountMemoTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            transaction = AccountMemoTransactionImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun PagingSource<Int, MemoLocalEntity>.pagedIds(): List<Uuid> {
            val result =
                load(
                    PagingSource.LoadParams.Refresh(
                        key = null,
                        loadSize = 100,
                        placeholdersEnabled = false,
                    ),
                )

            return result.shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, MemoLocalEntity>>().data.map { memo -> memo.id }
        }

        suspend fun insertPlace(
            accountId: Uuid,
            place: PlaceLocalEntity,
        ) {
            database.withWriteTransaction {
                database.placeDao().upsert(listOf(place))
                database.accountPlaceDao().upsert(AccountPlaceLocalEntity(accountId = accountId, placeId = place.id, isDirty = true))
            }
        }

        suspend fun insertWeb(
            accountId: Uuid,
            web: WebLocalEntity,
        ) {
            database.withWriteTransaction {
                database.webDao().upsert(listOf(web))
                database.accountWebDao().upsert(AccountWebLocalEntity(accountId = accountId, webId = web.id, isDirty = true))
            }
        }

        suspend fun insertContact(
            accountId: Uuid,
            contact: ContactLocalEntity,
        ) {
            database.withWriteTransaction {
                database.contactDao().upsert(listOf(contact))
                database.accountContactDao().upsert(AccountContactLocalEntity(accountId = accountId, contactId = contact.id, isDirty = true))
            }
        }

        suspend fun placeMemoIds(
            accountId: Uuid,
            placeId: Uuid,
        ): List<Uuid> = database.accountPlaceMemoDao().page(accountId = accountId, placeId = placeId, sort = ListSortLocalEntity.DEFAULT.queryValue).pagedIds()

        suspend fun webMemoIds(
            accountId: Uuid,
            webId: Uuid,
        ): List<Uuid> = database.accountWebMemoDao().page(accountId = accountId, webId = webId, sort = ListSortLocalEntity.DEFAULT.queryValue).pagedIds()

        suspend fun contactMemoIds(
            accountId: Uuid,
            contactId: Uuid,
        ): List<Uuid> = database.accountContactMemoDao().page(accountId = accountId, contactId = contactId, sort = ListSortLocalEntity.DEFAULT.queryValue).pagedIds()

        test("TC-PLACE-DETAIL-MEMO-DATA-005 새 메모는 저장 시점의 장소 선택으로 연결되고 장소 A의 메모 탭은 그 연결을 따른다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val placeA = fixtureMonkey.localPlace(isDeleted = false)
            val placeB = fixtureMonkey.localPlace(isDeleted = false)
            insertPlace(accountId = accountId, place = placeA)
            insertPlace(accountId = accountId, place = placeB)

            selectionCaseList(a = placeA.id, b = placeB.id).forEach { selectionCase ->
                val memo = fixtureMonkey.localMemo(isFinished = false, isDeleted = false, primaryTagId = null)
                transaction.upsert(
                    accountId = accountId,
                    memoList = listOf(memo),
                    memoTagList = emptyList(),
                    memoPlaceList = selectionCase.selectedIdSet.map { placeId -> memoPlace(memo = memo, placeId = placeId) },
                    memoWebList = emptyList(),
                    memoContactList = emptyList(),
                )

                withClue(selectionCase) {
                    database
                        .memoPlaceDao()
                        .findByMemoIdList(listOf(memo.id))
                        .filterNot { memoPlace -> memoPlace.isDeleted }
                        .map { memoPlace -> memoPlace.placeId } shouldContainExactlyInAnyOrder selectionCase.selectedIdSet
                    (memo.id in placeMemoIds(accountId = accountId, placeId = placeA.id)) shouldBe selectionCase.isShownInA
                }
            }
        }

        test("TC-WEB-DETAIL-MEMO-DATA-005 새 메모는 저장 시점의 웹 항목 선택으로 연결되고 웹 항목 A의 메모 탭은 그 연결을 따른다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val webA = fixtureMonkey.localWeb(isDeleted = false)
            val webB = fixtureMonkey.localWeb(isDeleted = false)
            insertWeb(accountId = accountId, web = webA)
            insertWeb(accountId = accountId, web = webB)

            selectionCaseList(a = webA.id, b = webB.id).forEach { selectionCase ->
                val memo = fixtureMonkey.localMemo(isFinished = false, isDeleted = false, primaryTagId = null)
                transaction.upsert(
                    accountId = accountId,
                    memoList = listOf(memo),
                    memoTagList = emptyList(),
                    memoPlaceList = emptyList(),
                    memoWebList = selectionCase.selectedIdSet.map { webId -> memoWeb(memo = memo, webId = webId) },
                    memoContactList = emptyList(),
                )

                withClue(selectionCase) {
                    database
                        .memoWebDao()
                        .findByMemoIdList(listOf(memo.id))
                        .filterNot { memoWeb -> memoWeb.isDeleted }
                        .map { memoWeb -> memoWeb.webId } shouldContainExactlyInAnyOrder selectionCase.selectedIdSet
                    (memo.id in webMemoIds(accountId = accountId, webId = webA.id)) shouldBe selectionCase.isShownInA
                }
            }
        }

        test("TC-CONTACT-DETAIL-MEMO-DATA-005 새 메모는 저장 시점의 연락처 선택으로 연결되고 연락처 A의 메모 탭은 그 연결을 따른다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val contactA = fixtureMonkey.localContact(numberList = emptyList(), hasMeasure = false, isFavorite = fixtureMonkey.giveMeOne<Boolean>(), isDeleted = false)
            val contactB = fixtureMonkey.localContact(numberList = emptyList(), hasMeasure = false, isFavorite = fixtureMonkey.giveMeOne<Boolean>(), isDeleted = false)
            insertContact(accountId = accountId, contact = contactA)
            insertContact(accountId = accountId, contact = contactB)

            selectionCaseList(a = contactA.id, b = contactB.id).forEach { selectionCase ->
                val memo = fixtureMonkey.localMemo(isFinished = false, isDeleted = false, primaryTagId = null)
                transaction.upsert(
                    accountId = accountId,
                    memoList = listOf(memo),
                    memoTagList = emptyList(),
                    memoPlaceList = emptyList(),
                    memoWebList = emptyList(),
                    memoContactList = selectionCase.selectedIdSet.map { contactId -> memoContact(memo = memo, contactId = contactId) },
                )

                withClue(selectionCase) {
                    database
                        .memoContactDao()
                        .findByMemoIdList(listOf(memo.id))
                        .filterNot { memoContact -> memoContact.isDeleted }
                        .map { memoContact -> memoContact.contactId } shouldContainExactlyInAnyOrder selectionCase.selectedIdSet
                    (memo.id in contactMemoIds(accountId = accountId, contactId = contactA.id)) shouldBe selectionCase.isShownInA
                }
            }
        }

        test("TC-CONTACT-DETAIL-MEMO-DOMAIN-003 상세 대상이 다른 연락처로 바뀌면 목록은 새 연락처의 메모로 바뀐다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstContact = fixtureMonkey.localContact(numberList = emptyList(), hasMeasure = false, isFavorite = fixtureMonkey.giveMeOne<Boolean>(), isDeleted = false)
            val secondContact = fixtureMonkey.localContact(numberList = emptyList(), hasMeasure = false, isFavorite = fixtureMonkey.giveMeOne<Boolean>(), isDeleted = false)
            insertContact(accountId = accountId, contact = firstContact)
            insertContact(accountId = accountId, contact = secondContact)
            val firstMemo = fixtureMonkey.localMemo(isFinished = false, isDeleted = false, primaryTagId = null)
            val secondMemo = fixtureMonkey.localMemo(isFinished = false, isDeleted = false, primaryTagId = null)
            transaction.upsert(
                accountId = accountId,
                memoList = listOf(firstMemo, secondMemo),
                memoTagList = emptyList(),
                memoPlaceList = emptyList(),
                memoWebList = emptyList(),
                memoContactList =
                    listOf(
                        memoContact(memo = firstMemo, contactId = firstContact.id),
                        memoContact(memo = secondMemo, contactId = secondContact.id),
                    ),
            )

            contactMemoIds(accountId = accountId, contactId = firstContact.id) shouldBe listOf(firstMemo.id)
            contactMemoIds(accountId = accountId, contactId = secondContact.id) shouldBe listOf(secondMemo.id)
        }
    }) {
    private data class SelectionCase(
        val selectedIdSet: Set<Uuid>,
        val isShownInA: Boolean,
    )

    private companion object {
        fun selectionCaseList(
            a: Uuid,
            b: Uuid,
        ): List<SelectionCase> =
            listOf(
                SelectionCase(selectedIdSet = setOf(a), isShownInA = true),
                SelectionCase(selectedIdSet = setOf(a, b), isShownInA = true),
                SelectionCase(selectedIdSet = setOf(b), isShownInA = false),
                SelectionCase(selectedIdSet = emptySet(), isShownInA = false),
            )

        fun memoPlace(
            memo: MemoLocalEntity,
            placeId: Uuid,
        ): MemoPlaceLocalEntity =
            MemoPlaceLocalEntity(
                memoId = memo.id,
                placeId = placeId,
                isDeleted = false,
                updatedAt = memo.updatedAt,
                createdAt = memo.createdAt,
            )

        fun memoWeb(
            memo: MemoLocalEntity,
            webId: Uuid,
        ): MemoWebLocalEntity =
            MemoWebLocalEntity(
                memoId = memo.id,
                webId = webId,
                isDeleted = false,
                updatedAt = memo.updatedAt,
                createdAt = memo.createdAt,
            )

        fun memoContact(
            memo: MemoLocalEntity,
            contactId: Uuid,
        ): MemoContactLocalEntity =
            MemoContactLocalEntity(
                memoId = memo.id,
                contactId = contactId,
                isDeleted = false,
                updatedAt = memo.updatedAt,
                createdAt = memo.createdAt,
            )
    }
}
