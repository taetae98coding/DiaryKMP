package io.github.taetae98coding.diary.core.database.impl.web.datasource

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.database.api.webtag.entity.WebTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.web.transaction.AccountWebTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.webtag.transaction.AccountWebTagTransactionImpl
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.time.Instant
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class AccountTagWebLocalDataSourceImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var dataSource: AccountTagWebLocalDataSourceImpl
        lateinit var webTagTransaction: AccountWebTagTransactionImpl
        lateinit var tagTransaction: AccountTagTransactionImpl
        lateinit var webTransaction: AccountWebTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            dataSource = AccountTagWebLocalDataSourceImpl(database = database)
            webTagTransaction = AccountWebTagTransactionImpl(database = database)
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
                webId = webId,
                tagId = tagId,
                isDeleted = isDeleted,
                updatedAt = instant(),
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

        test("TC-TAG-DETAIL-WEB-DATA-001 현재 계정의 대상 태그와 활성 연결을 가진 미삭제 웹 항목만 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
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

        test("TC-TAG-DETAIL-WEB-FEATURE-002 태그별 웹 항목은 제목 오름차순으로 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
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

        test("TC-TAG-DETAIL-WEB-DATA-002 태그별 웹 항목을 페이지 단위로 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val targetTag = tag()
            val webList = List(3) { index -> web(title = "web-$index") }

            insertTag(accountId, targetTag)
            insertWeb(accountId, *webList.toTypedArray())
            webList.forEach { value -> link(accountId = accountId, webId = value.id, tagId = targetTag.id) }

            pagedWebIdList(accountId = accountId, tagId = targetTag.id, loadSize = 2) shouldBe
                webList.take(2).map { value -> value.id }
        }

        test("TC-TAG-DETAIL-WEB-DATA-003 연결을 해제하면 목록에서 사라지고 복구하면 다시 나타난다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
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

        test("TC-TAG-DETAIL-WEB-DATA-003 웹 항목을 삭제하면 목록에서 사라진다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val targetTag = tag()
            val targetWeb = web()

            insertTag(accountId, targetTag)
            insertWeb(accountId, targetWeb)
            link(accountId = accountId, webId = targetWeb.id, tagId = targetTag.id)
            insertWeb(accountId, targetWeb.copy(isDeleted = true))

            pagedWebIdList(accountId = accountId, tagId = targetTag.id).shouldBeEmpty()
        }

        test("TC-TAG-DETAIL-WEB-DATA-003 웹 항목의 제목을 바꾸면 바뀐 제목의 정렬 위치로 이동한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
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

        test("TC-TAG-DETAIL-WEB-DOMAIN-001 태그를 완료하거나 삭제해도 연결된 웹 항목은 계속 조회된다") {
            listOf(
                true to false,
                false to true,
            ).forEach { (isFinished, isDeleted) ->
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val targetTag = tag()
                val targetWeb = web()

                insertTag(accountId, targetTag)
                insertWeb(accountId, targetWeb)
                link(accountId = accountId, webId = targetWeb.id, tagId = targetTag.id)
                insertTag(accountId, targetTag.copy(isFinished = isFinished, isDeleted = isDeleted))

                pagedWebIdList(accountId = accountId, tagId = targetTag.id) shouldBe listOf(targetWeb.id)
            }
        }

        test("TC-TAG-DETAIL-WEB-DATA-004 웹 항목는 저장 시점의 태그 선택으로 저장된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
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
                            WebTagLocalEntity(
                                webId = entity.id,
                                tagId = value.id,
                                isDeleted = false,
                                updatedAt = instant(),
                                createdAt = instant(),
                            )
                        },
                )

                pagedWebIdList(accountId = accountId, tagId = tagA.id).contains(entity.id) shouldBe isShownInTagATab
                selectedTagList.forEach { value ->
                    pagedWebIdList(accountId = accountId, tagId = value.id).contains(entity.id) shouldBe true
                }
            }
        }

        test("TC-TAG-DETAIL-WEB-DOMAIN-002 태그마다 조회되는 웹 항목이 다르다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
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
        private suspend fun PagingSource<Int, WebLocalEntity>.pagedWebIdList(loadSize: Int): List<Uuid> {
            val result =
                load(
                    PagingSource.LoadParams.Refresh(
                        key = null,
                        loadSize = loadSize,
                        placeholdersEnabled = false,
                    ),
                )

            return result.shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, WebLocalEntity>>().data.map { web -> web.id }
        }

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())

        private fun tag(): TagLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<TagLocalEntity>()
                .setExp(TagLocalEntity::updatedAt, instant())
                .setExp(TagLocalEntity::createdAt, instant())
                .sample()
                .copy(isFinished = false, isDeleted = false)

        private fun web(title: String = "title-${fixtureMonkey.giveMeOne<String>()}"): WebLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<WebLocalEntity>()
                .setExp(WebLocalEntity::updatedAt, instant())
                .setExp(WebLocalEntity::createdAt, instant())
                .sample()
                .let { web ->
                    web.copy(
                        detail = web.detail.copy(title = title),
                        isDeleted = false,
                    )
                }
    }
}
