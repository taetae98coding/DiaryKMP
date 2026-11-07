package io.github.taetae98coding.diary.core.database.impl.web.datasource

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entitytag.transaction.AccountEntityTagTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.web.transaction.AccountWebTransactionImpl
import io.github.taetae98coding.diary.core.testing.tag.tagLocalEntity
import io.github.taetae98coding.diary.core.testing.web.webLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import kotlin.uuid.Uuid

class AccountTagWebLocalDataSourceImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var dataSource: AccountTagWebLocalDataSourceImpl
        lateinit var webTagTransaction: AccountEntityTagTransactionImpl
        lateinit var tagTransaction: AccountTagTransactionImpl
        lateinit var webTransaction: AccountWebTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            dataSource = AccountTagWebLocalDataSourceImpl(database = database)
            webTagTransaction = AccountEntityTagTransactionImpl(database = database)
            tagTransaction = AccountTagTransactionImpl(database = database)
            webTransaction = AccountWebTransactionImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun insertTag(
            accountId: Uuid,
            vararg tagList: TagLocalEntity,
        ) {
            tagTransaction.upsert(accountId = accountId, tagList = tagList.toList(), tagLinkList = emptyList())
        }

        suspend fun insertWeb(
            accountId: Uuid,
            vararg webList: WebLocalEntity,
        ) {
            webTransaction.upsert(accountId = accountId, webList = webList.toList(), webTagList = emptyList())
        }

        suspend fun link(
            accountId: Uuid,
            webId: Uuid,
            tagId: Uuid,
            isDeleted: Boolean = false,
        ) {
            webTagTransaction.upsert(
                accountId = accountId,
                kind = EntityTagKindLocalEntity.WEB,
                entityId = webId,
                tagId = tagId,
                isDeleted = isDeleted,
                updatedAt = Arb.secondPrecisionInstant().next(),
            )
        }

        suspend fun pagedWebIdList(
            accountId: Uuid,
            tagId: Uuid,
            loadSize: Int = 100,
        ): List<Uuid> =
            dataSource
                .page(accountId = accountId, tagId = tagId, scope = TagScopeLocalEntity.SELF, sort = ListSortLocalEntity.DEFAULT)
                .pagedWebIdList(loadSize = loadSize)

        test("TC-dcd46928-6c6c-4815-98ce-dac1679940f3 현재 계정의 대상 태그와 활성 연결을 가진 미삭제 웹 항목만 조회한다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val targetTag = tag()
            val otherTag = tag()
            val linkedWeb = web(title = "a-linked")
            val otherTagWeb = web(title = "b-other-tag")
            val unlinkedWeb = web(title = "c-unlinked")
            val deletedWeb = web(title = "d-deleted").copy(isDeleted = true)
            val otherAccountWeb = web(title = "e-other-account")

            insertTag(accountId, targetTag, otherTag)
            insertTag(otherAccountId, targetTag)
            insertWeb(accountId, linkedWeb, otherTagWeb, unlinkedWeb, deletedWeb)
            insertWeb(otherAccountId, otherAccountWeb)

            link(accountId = accountId, webId = linkedWeb.id, tagId = targetTag.id)
            link(accountId = accountId, webId = otherTagWeb.id, tagId = otherTag.id)
            link(accountId = accountId, webId = unlinkedWeb.id, tagId = targetTag.id, isDeleted = true)
            link(accountId = accountId, webId = deletedWeb.id, tagId = targetTag.id)
            link(accountId = otherAccountId, webId = otherAccountWeb.id, tagId = targetTag.id)

            pagedWebIdList(accountId = accountId, tagId = targetTag.id) shouldBe listOf(linkedWeb.id)
        }

        test("TC-d78d5005-016c-4999-a0db-c050105980ef 태그별 웹 항목은 제목 오름차순으로 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val targetTag = tag()
            val firstWeb = web(title = "a-web")
            val secondWeb = web(title = "b-web")
            val thirdWeb = web(title = "c-web")

            insertTag(accountId, targetTag)
            insertWeb(accountId, thirdWeb, firstWeb, secondWeb)
            listOf(firstWeb, secondWeb, thirdWeb).forEach { value ->
                link(accountId = accountId, webId = value.id, tagId = targetTag.id)
            }

            pagedWebIdList(accountId = accountId, tagId = targetTag.id) shouldBe
                listOf(firstWeb.id, secondWeb.id, thirdWeb.id)
        }

        test("TC-bbe313f4-ac99-4a84-ac55-4a8b97039d5b 태그별 웹 항목을 페이지 단위로 조회한다") {
            val accountId = Arb.kotlinUuid().next()
            val targetTag = tag()
            val webList = List(3) { index -> web(title = "web-$index") }

            insertTag(accountId, targetTag)
            insertWeb(accountId, *webList.toTypedArray())
            webList.forEach { value -> link(accountId = accountId, webId = value.id, tagId = targetTag.id) }

            val firstPage =
                dataSource
                    .page(accountId = accountId, tagId = targetTag.id, scope = TagScopeLocalEntity.SELF, sort = ListSortLocalEntity.DEFAULT)
                    .loadPage(key = null, loadSize = 2)

            firstPage.data.map { value -> value.id } shouldBe webList.take(2).map { value -> value.id }

            val secondPage =
                dataSource
                    .page(accountId = accountId, tagId = targetTag.id, scope = TagScopeLocalEntity.SELF, sort = ListSortLocalEntity.DEFAULT)
                    .loadPage(key = firstPage.nextKey, loadSize = 2)

            secondPage.data.map { value -> value.id } shouldBe webList.drop(2).map { value -> value.id }
        }

        test("TC-5905c263-f462-4474-b5d4-6e4a7a8bf218 연결을 해제하면 목록에서 사라지고 복구하면 다시 나타난다") {
            val accountId = Arb.kotlinUuid().next()
            val targetTag = tag()
            val targetWeb = web()

            insertTag(accountId, targetTag)
            insertWeb(accountId, targetWeb)
            link(accountId = accountId, webId = targetWeb.id, tagId = targetTag.id)

            pagedWebIdList(accountId = accountId, tagId = targetTag.id) shouldBe listOf(targetWeb.id)

            link(accountId = accountId, webId = targetWeb.id, tagId = targetTag.id, isDeleted = true)

            pagedWebIdList(accountId = accountId, tagId = targetTag.id).shouldBeEmpty()

            link(accountId = accountId, webId = targetWeb.id, tagId = targetTag.id)

            pagedWebIdList(accountId = accountId, tagId = targetTag.id) shouldBe listOf(targetWeb.id)
        }

        test("TC-5905c263-f462-4474-b5d4-6e4a7a8bf218 웹 항목을 삭제하면 목록에서 사라진다") {
            val accountId = Arb.kotlinUuid().next()
            val targetTag = tag()
            val targetWeb = web()

            insertTag(accountId, targetTag)
            insertWeb(accountId, targetWeb)
            link(accountId = accountId, webId = targetWeb.id, tagId = targetTag.id)
            insertWeb(accountId, targetWeb.copy(isDeleted = true))

            pagedWebIdList(accountId = accountId, tagId = targetTag.id).shouldBeEmpty()
        }

        test("TC-5905c263-f462-4474-b5d4-6e4a7a8bf218 삭제를 되돌리면 웹 항목이 정렬 위치에 다시 나타난다") {
            val accountId = Arb.kotlinUuid().next()
            val targetTag = tag()
            val firstWeb = web(title = "a-web")
            val deletedWeb = web(title = "b-web")
            val lastWeb = web(title = "c-web")

            insertTag(accountId, targetTag)
            insertWeb(accountId, firstWeb, deletedWeb.copy(isDeleted = true), lastWeb)
            listOf(firstWeb, deletedWeb, lastWeb).forEach { value ->
                link(accountId = accountId, webId = value.id, tagId = targetTag.id)
            }

            pagedWebIdList(accountId = accountId, tagId = targetTag.id) shouldBe listOf(firstWeb.id, lastWeb.id)

            insertWeb(accountId, deletedWeb)

            pagedWebIdList(accountId = accountId, tagId = targetTag.id) shouldBe listOf(firstWeb.id, deletedWeb.id, lastWeb.id)
        }

        test("TC-5905c263-f462-4474-b5d4-6e4a7a8bf218 웹 항목의 제목을 바꾸면 바뀐 제목의 정렬 위치로 이동한다") {
            val accountId = Arb.kotlinUuid().next()
            val targetTag = tag()
            val firstWeb = web(title = "a-web")
            val secondWeb = web(title = "b-web")

            insertTag(accountId, targetTag)
            insertWeb(accountId, firstWeb, secondWeb)
            listOf(firstWeb, secondWeb).forEach { value ->
                link(accountId = accountId, webId = value.id, tagId = targetTag.id)
            }

            pagedWebIdList(accountId = accountId, tagId = targetTag.id) shouldBe listOf(firstWeb.id, secondWeb.id)

            insertWeb(accountId, firstWeb.copy(detail = firstWeb.detail.copy(title = "z-web")))

            pagedWebIdList(accountId = accountId, tagId = targetTag.id) shouldBe listOf(secondWeb.id, firstWeb.id)
        }

        test("TC-af215105-464c-47f4-aa42-1090f176b5d7 태그를 완료하거나 삭제해도 연결된 웹 항목은 계속 조회된다") {
            listOf(
                true to false,
                false to true,
            ).forEach { (isFinished, isDeleted) ->
                val accountId = Arb.kotlinUuid().next()
                val targetTag = tag()
                val targetWeb = web()

                insertTag(accountId, targetTag)
                insertWeb(accountId, targetWeb)
                link(accountId = accountId, webId = targetWeb.id, tagId = targetTag.id)
                insertTag(accountId, targetTag.copy(isFinished = isFinished, isDeleted = isDeleted))

                pagedWebIdList(accountId = accountId, tagId = targetTag.id) shouldBe listOf(targetWeb.id)
            }
        }

        test("TC-a1c70efb-92cb-4be0-9cb4-3514c5abff40 웹 항목는 저장 시점의 태그 선택으로 저장된다") {
            val accountId = Arb.kotlinUuid().next()
            val tagA = tag()
            val tagB = tag()
            insertTag(accountId, tagA, tagB)

            listOf(
                listOf(tagA) to true,
                listOf(tagA, tagB) to true,
                listOf(tagB) to false,
            ).forEach { (selectedTagList, isShownInTagATab) ->
                val entity = web()

                webTransaction.upsert(
                    accountId = accountId,
                    webList = listOf(entity),
                    webTagList =
                        selectedTagList.map { value ->
                            EntityTagLocalEntity(
                                entityId = entity.id,
                                tagId = value.id,
                                isDeleted = false,
                                updatedAt = Arb.secondPrecisionInstant().next(),
                                createdAt = Arb.secondPrecisionInstant().next(),
                            )
                        },
                )

                pagedWebIdList(accountId = accountId, tagId = tagA.id).contains(entity.id) shouldBe isShownInTagATab
                selectedTagList.forEach { value ->
                    pagedWebIdList(accountId = accountId, tagId = value.id).contains(entity.id) shouldBe true
                }
            }
        }

        test("TC-8b82947c-df43-4860-ab2f-b50719cf60c5 태그마다 조회되는 웹 항목이 다르다") {
            val accountId = Arb.kotlinUuid().next()
            val firstTag = tag()
            val secondTag = tag()
            val firstWeb = web()
            val secondWeb = web()

            insertTag(accountId, firstTag, secondTag)
            insertWeb(accountId, firstWeb, secondWeb)
            link(accountId = accountId, webId = firstWeb.id, tagId = firstTag.id)
            link(accountId = accountId, webId = secondWeb.id, tagId = secondTag.id)

            pagedWebIdList(accountId = accountId, tagId = firstTag.id) shouldBe listOf(firstWeb.id)
            pagedWebIdList(accountId = accountId, tagId = secondTag.id) shouldBe listOf(secondWeb.id)
        }
    }) {
    companion object {
        private suspend fun PagingSource<Int, WebLocalEntity>.pagedWebIdList(loadSize: Int): List<Uuid> = loadPage(key = null, loadSize = loadSize).data.map { web -> web.id }

        private suspend fun PagingSource<Int, WebLocalEntity>.loadPage(
            key: Int?,
            loadSize: Int,
        ): PagingSource.LoadResult.Page<Int, WebLocalEntity> {
            val result =
                load(
                    if (key == null) {
                        PagingSource.LoadParams.Refresh(key = null, loadSize = loadSize, placeholdersEnabled = false)
                    } else {
                        PagingSource.LoadParams.Append(key = key, loadSize = loadSize, placeholdersEnabled = false)
                    },
                )

            return result.shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, WebLocalEntity>>()
        }

        private fun tag(): TagLocalEntity =
            Arb
                .tagLocalEntity()
                .next()
                .copy(
                    updatedAt = Arb.secondPrecisionInstant().next(),
                    createdAt = Arb.secondPrecisionInstant().next(),
                ).copy(isFinished = false, isDeleted = false)

        private fun web(title: String = "title-${Arb.string().next()}"): WebLocalEntity =
            Arb
                .webLocalEntity()
                .next()
                .copy(
                    updatedAt = Arb.secondPrecisionInstant().next(),
                    createdAt = Arb.secondPrecisionInstant().next(),
                ).let { web ->
                    web.copy(
                        detail = web.detail.copy(title = title),
                        isDeleted = false,
                    )
                }
    }
}
