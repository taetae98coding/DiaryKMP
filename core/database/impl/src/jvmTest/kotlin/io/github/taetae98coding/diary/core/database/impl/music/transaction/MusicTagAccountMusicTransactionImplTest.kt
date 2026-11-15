package io.github.taetae98coding.diary.core.database.impl.music.transaction

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entitytag.datasource.AccountEntityTagLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.AccountTagLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.core.testing.music.musicLocalEntity
import io.github.taetae98coding.diary.core.testing.tag.tagLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.flow.first
import kotlin.uuid.Uuid

class MusicTagAccountMusicTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var transaction: AccountMusicTransactionImpl
        lateinit var tagTransaction: AccountTagTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            transaction = AccountMusicTransactionImpl(database = database)
            tagTransaction = AccountTagTransactionImpl(database = database)
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

        suspend fun selectedTagIdList(
            accountId: Uuid,
            tagIdSet: Set<Uuid>,
        ): List<Uuid> =
            AccountTagLocalDataSourceImpl(database = database)
                .get(accountId = accountId, tagIdSet = tagIdSet)
                .first()
                .map { tag -> tag.id }

        suspend fun linkedTagIdList(
            accountId: Uuid,
            musicId: Uuid,
        ): List<Uuid> =
            AccountEntityTagLocalDataSourceImpl(database = database)
                .getTagList(accountId = accountId, kind = EntityTagKindLocalEntity.MUSIC, entityId = musicId)
                .first()
                .map { tag -> tag.id }

        test("TC-a3d83b22-b123-44fd-b130-9b77672f92fb 고른 태그가 완료되면 고른 태그 조회에서 빠진다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = tag()
            val keptTag = tag()
            insertTag(accountId, tag, keptTag)
            val selectedTagIdSet = setOf(tag.id, keptTag.id)
            selectedTagIdList(accountId = accountId, tagIdSet = selectedTagIdSet) shouldContainExactlyInAnyOrder listOf(tag.id, keptTag.id)

            tagTransaction.updateFinished(accountId = accountId, tagId = tag.id, isFinished = true, updatedAt = Arb.secondPrecisionInstant().next())

            selectedTagIdList(accountId = accountId, tagIdSet = selectedTagIdSet) shouldBe listOf(keptTag.id)
        }

        test("TC-a3d83b22-b123-44fd-b130-9b77672f92fb 고른 태그가 삭제되면 고른 태그 조회에서 빠진다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = tag()
            val keptTag = tag()
            insertTag(accountId, tag, keptTag)
            val selectedTagIdSet = setOf(tag.id, keptTag.id)
            selectedTagIdList(accountId = accountId, tagIdSet = selectedTagIdSet) shouldContainExactlyInAnyOrder listOf(tag.id, keptTag.id)

            tagTransaction.updateDeleted(accountId = accountId, tagId = tag.id, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next())

            selectedTagIdList(accountId = accountId, tagIdSet = selectedTagIdSet) shouldBe listOf(keptTag.id)
        }

        test("TC-22b0c5f3-6b9d-402d-a599-8140be1226e2 고른 두 태그만 새 곡의 연결된 태그로 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val tagList = List(3) { tag() }
            insertTag(accountId, *tagList.toTypedArray())
            val music = newMusic()

            transaction.upsert(accountId = accountId, musicList = listOf(music), musicTagList = linkList(music = music, tagList = tagList.take(2)))

            linkedTagIdList(accountId = accountId, musicId = music.id) shouldContainExactlyInAnyOrder tagList.take(2).map { tag -> tag.id }
        }

        test("TC-fa73e7c6-6ec0-4a20-b9dc-701f280a3794 고른 뒤 완료된 태그도 새 곡과 연결되어 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = tag()
            insertTag(accountId, tag)
            tagTransaction.updateFinished(accountId = accountId, tagId = tag.id, isFinished = true, updatedAt = Arb.secondPrecisionInstant().next())
            val music = newMusic()

            transaction.upsert(accountId = accountId, musicList = listOf(music), musicTagList = linkList(music = music, tagList = listOf(tag)))

            linkedTagIdList(accountId = accountId, musicId = music.id) shouldBe listOf(tag.id)
        }

        test("TC-fa73e7c6-6ec0-4a20-b9dc-701f280a3794 고른 뒤 삭제된 태그도 연결되어 삭제를 되돌리면 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = tag()
            insertTag(accountId, tag)
            tagTransaction.updateDeleted(accountId = accountId, tagId = tag.id, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next())
            val music = newMusic()

            transaction.upsert(accountId = accountId, musicList = listOf(music), musicTagList = linkList(music = music, tagList = listOf(tag)))

            linkedTagIdList(accountId = accountId, musicId = music.id).shouldBeEmpty()
            tagTransaction.updateDeleted(accountId = accountId, tagId = tag.id, isDeleted = false, updatedAt = Arb.secondPrecisionInstant().next())
            linkedTagIdList(accountId = accountId, musicId = music.id) shouldBe listOf(tag.id)
        }

        test("TC-05538b0f-b0c7-4a66-872a-6dd2e9927158 연결된 태그는 완료되어도 조회되고 삭제되면 조회되지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = tag()
            insertTag(accountId, tag)
            val music = newMusic()
            transaction.upsert(accountId = accountId, musicList = listOf(music), musicTagList = linkList(music = music, tagList = listOf(tag)))
            linkedTagIdList(accountId = accountId, musicId = music.id) shouldBe listOf(tag.id)

            tagTransaction.updateFinished(accountId = accountId, tagId = tag.id, isFinished = true, updatedAt = Arb.secondPrecisionInstant().next())
            linkedTagIdList(accountId = accountId, musicId = music.id) shouldBe listOf(tag.id)

            tagTransaction.updateFinished(accountId = accountId, tagId = tag.id, isFinished = false, updatedAt = Arb.secondPrecisionInstant().next())
            tagTransaction.updateDeleted(accountId = accountId, tagId = tag.id, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next())
            linkedTagIdList(accountId = accountId, musicId = music.id).shouldBeEmpty()
        }
    }) {
    private companion object {
        fun newMusic(): MusicLocalEntity = Arb.musicLocalEntity().next().copy(isDeleted = false)

        fun tag(): TagLocalEntity = Arb.tagLocalEntity().next().copy(isFinished = false, isDeleted = false)

        fun linkList(
            music: MusicLocalEntity,
            tagList: List<TagLocalEntity>,
        ): List<EntityTagLocalEntity> =
            tagList.map { tag ->
                EntityTagLocalEntity(entityId = music.id, tagId = tag.id, isDeleted = false, updatedAt = music.updatedAt, createdAt = music.createdAt)
            }
    }
}
