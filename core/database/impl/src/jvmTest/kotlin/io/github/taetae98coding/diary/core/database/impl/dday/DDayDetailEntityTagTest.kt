package io.github.taetae98coding.diary.core.database.impl.dday

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.dday.datasource.AccountDDayLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.dday.datasource.AccountDDaySyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.dday.datasource.AccountTagDDayLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.dday.transaction.AccountDDaySyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.dday.transaction.AccountDDayTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.ddayfilter.entity.DDayFilterTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entitytag.datasource.AccountEntityTagLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.entitytag.datasource.AccountEntityTagSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.entitytag.transaction.AccountEntityTagTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.core.testing.dday.ddayLocalEntity
import io.github.taetae98coding.diary.core.testing.tag.tagLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.flow.first
import kotlin.uuid.Uuid

class DDayDetailEntityTagTest :
    FunSpec({
        lateinit var database: DiaryDatabase

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
        }

        afterTest {
            database.close()
        }

        suspend fun upsertTag(
            accountId: Uuid,
            vararg tagList: TagLocalEntity,
        ) {
            AccountTagTransactionImpl(database = database).upsert(accountId = accountId, tagList = tagList.toList(), tagLinkList = emptyList())
        }

        // 업로드를 마친 디데이를 저장한다.
        suspend fun insertDDay(
            accountId: Uuid,
            isDeleted: Boolean = false,
            title: String? = null,
        ): DDayLocalEntity {
            val dday =
                Arb.ddayLocalEntity().next().let { value ->
                    value.copy(isDeleted = isDeleted, detail = value.detail.copy(title = title ?: value.detail.title))
                }
            AccountDDayTransactionImpl(database = database).upsert(accountId = accountId, dday = dday, ddayTagList = emptyList())
            val syncDataSource = AccountDDaySyncLocalDataSourceImpl(database = database)
            AccountDDaySyncTransactionImpl(database = database).clearPending(accountId = accountId, ddayList = syncDataSource.readPendingList(accountId = accountId))
            return dday
        }

        suspend fun link(
            accountId: Uuid,
            ddayId: Uuid,
            tagId: Uuid,
            isDeleted: Boolean = false,
        ) {
            AccountEntityTagTransactionImpl(database = database).upsert(
                accountId = accountId,
                kind = EntityTagKindLocalEntity.DDAY,
                entityId = ddayId,
                tagId = tagId,
                isDeleted = isDeleted,
                updatedAt = Arb.secondPrecisionInstant().next(),
            )
        }

        suspend fun find(
            accountId: Uuid,
            ddayId: Uuid,
        ): DDayLocalEntity? = AccountDDayLocalDataSourceImpl(database = database).find(accountId = accountId, ddayId = ddayId).first()

        suspend fun displayedTagIdList(
            accountId: Uuid,
            ddayId: Uuid,
        ): List<Uuid> =
            AccountEntityTagLocalDataSourceImpl(database = database)
                .getTagList(accountId = accountId, kind = EntityTagKindLocalEntity.DDAY, entityId = ddayId)
                .first()
                .map { tag -> tag.id }

        suspend fun PagingSource<Int, DDayLocalEntity>.pagedIdList(): List<Uuid> =
            load(PagingSource.LoadParams.Refresh(key = null, loadSize = 100, placeholdersEnabled = false))
                .shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, DDayLocalEntity>>()
                .data
                .map { dday -> dday.id }

        suspend fun homeIdList(accountId: Uuid): List<Uuid> = AccountDDayLocalDataSourceImpl(database = database).page(accountId = accountId).pagedIdList()

        suspend fun tagTabIdList(
            accountId: Uuid,
            tagId: Uuid,
        ): List<Uuid> = AccountTagDDayLocalDataSourceImpl(database = database).page(accountId = accountId, tagId = tagId, scope = TagScopeLocalEntity.SELF).pagedIdList()

        listOf(
            Triple("미완료·미삭제", { tag: TagLocalEntity -> tag }, true),
            Triple("완료됨", { tag: TagLocalEntity -> tag.copy(isFinished = true) }, true),
            Triple("삭제됨", { tag: TagLocalEntity -> tag.copy(isDeleted = true) }, false),
        ).forEach { (name, change, isDisplayed) ->
            test("TC-7189972b-88ed-4681-a39e-84ebb873497f 연결된 태그가 $name 이면 태그 입력 표시 여부가 $isDisplayed 다") {
                val accountId = Arb.kotlinUuid().next()
                val tag = tag()
                upsertTag(accountId, tag)
                val dday = insertDDay(accountId = accountId)
                link(accountId = accountId, ddayId = dday.id, tagId = tag.id)

                upsertTag(accountId, change(tag))

                displayedTagIdList(accountId = accountId, ddayId = dday.id) shouldBe if (isDisplayed) listOf(tag.id) else emptyList()
            }
        }

        test("TC-f4f71546-e2c5-49bf-8b9d-cc6f380c85eb 태그 연결과 해제는 디데이의 정보, 삭제 여부와 수정 시각을 바꾸지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = tag()
            upsertTag(accountId, tag)
            val dday = insertDDay(accountId = accountId)

            link(accountId = accountId, ddayId = dday.id, tagId = tag.id)
            find(accountId = accountId, ddayId = dday.id) shouldBe dday

            link(accountId = accountId, ddayId = dday.id, tagId = tag.id, isDeleted = true)
            find(accountId = accountId, ddayId = dday.id) shouldBe dday
        }

        test("TC-244a7551-a306-4742-8a65-39ddda6abed6 삭제 상태인 디데이에도 태그를 연결하고 해제하며 디데이는 삭제 상태로 남는다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = tag()
            upsertTag(accountId, tag)
            val dday = insertDDay(accountId = accountId, isDeleted = true)

            link(accountId = accountId, ddayId = dday.id, tagId = tag.id)
            displayedTagIdList(accountId = accountId, ddayId = dday.id) shouldBe listOf(tag.id)
            find(accountId = accountId, ddayId = dday.id)?.isDeleted shouldBe true

            link(accountId = accountId, ddayId = dday.id, tagId = tag.id, isDeleted = true)
            displayedTagIdList(accountId = accountId, ddayId = dday.id).shouldBeEmpty()
            find(accountId = accountId, ddayId = dday.id)?.isDeleted shouldBe true
        }

        test("TC-e983f63a-fd0f-4efa-8339-0a269bc667ec 태그 연결의 저장은 그 연결만 업로드 대기로 만들고 디데이는 대기로 만들지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = tag()
            upsertTag(accountId, tag)
            val dday = insertDDay(accountId = accountId)
            val ddaySyncDataSource = AccountDDaySyncLocalDataSourceImpl(database = database)
            val linkSyncDataSource = AccountEntityTagSyncLocalDataSourceImpl(database = database)

            link(accountId = accountId, ddayId = dday.id, tagId = tag.id)
            ddaySyncDataSource.readPendingList(accountId = accountId).shouldBeEmpty()
            linkSyncDataSource.readPendingList(accountId = accountId, kind = EntityTagKindLocalEntity.DDAY).map { value -> value.tagId to value.isDeleted } shouldBe listOf(tag.id to false)

            link(accountId = accountId, ddayId = dday.id, tagId = tag.id, isDeleted = true)
            ddaySyncDataSource.readPendingList(accountId = accountId).shouldBeEmpty()
            linkSyncDataSource.readPendingList(accountId = accountId, kind = EntityTagKindLocalEntity.DDAY).map { value -> value.tagId to value.isDeleted } shouldBe listOf(tag.id to true)
        }

        test("TC-c47dc0cd-1ff0-41b4-92ed-2230cbab42d2 연결한 동안에는 그 태그의 디데이 목록에 나타나고 해제하면 사라진다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = tag()
            upsertTag(accountId, tag)
            val dday = insertDDay(accountId = accountId)

            link(accountId = accountId, ddayId = dday.id, tagId = tag.id)
            tagTabIdList(accountId = accountId, tagId = tag.id) shouldBe listOf(dday.id)

            link(accountId = accountId, ddayId = dday.id, tagId = tag.id, isDeleted = true)
            tagTabIdList(accountId = accountId, tagId = tag.id).shouldBeEmpty()
        }

        test("TC-c98cd87b-bc3e-41a0-bb33-b9fa8ef1fcca 필터가 없으면 연결 전과 같은 노출과 순서이고 태그 A를 고르면 디데이 X가 표시된다") {
            val accountId = Arb.kotlinUuid().next()
            val tagA = tag()
            upsertTag(accountId, tagA)
            val ddayList = listOf("a-title", "b-title", "c-title").map { title -> insertDDay(accountId = accountId, title = title) }
            val ddayX = ddayList[1]
            val before = homeIdList(accountId)
            before shouldBe ddayList.map { dday -> dday.id }

            link(accountId = accountId, ddayId = ddayX.id, tagId = tagA.id)

            homeIdList(accountId) shouldBe before
            database.ddayFilterTagDao().upsert(entity = DDayFilterTagLocalEntity(accountId = accountId, tagId = tagA.id))
            homeIdList(accountId) shouldBe listOf(ddayX.id)
        }
    }) {
    private companion object {
        fun tag(): TagLocalEntity = Arb.tagLocalEntity().next().copy(isFinished = false, isDeleted = false)
    }
}
