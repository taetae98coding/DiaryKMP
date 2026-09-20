package io.github.taetae98coding.diary.data.memo.repository

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.memofilter.datasource.AccountMemoFilterLocalDataSource
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.data.tag.mapper.toDomain
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Instant
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class AccountMemoFilterRepositoryImplTest :
    FunSpec({
        test("선택 태그 조회는 로컬 태그를 도메인 모델로 변환한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val localTagList = listOf(tag(), tag())
            val localDataSource = mockk<AccountMemoFilterLocalDataSource>()
            every { localDataSource.getTagList(accountId = account.id) } returns flowOf(localTagList)
            val repository = AccountMemoFilterRepositoryImpl(accountMemoFilterLocalDataSource = localDataSource)

            val tagList = repository.getTagList(account = account).first()

            tagList shouldBe localTagList.map { tag -> tag.toDomain() }
        }

        test("선택 태그 조회 중 발생한 에러는 그대로 전파한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val failure = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val localDataSource = mockk<AccountMemoFilterLocalDataSource>()
            every { localDataSource.getTagList(accountId = account.id) } returns flow { throw failure }
            val repository = AccountMemoFilterRepositoryImpl(accountMemoFilterLocalDataSource = localDataSource)

            shouldThrow<IllegalStateException> {
                repository.getTagList(account = account).first()
            } shouldBe failure
        }

        test("태그 선택은 현재 계정의 선택으로 저장한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val localDataSource = mockk<AccountMemoFilterLocalDataSource>()
            coEvery { localDataSource.upsert(accountId = account.id, tagId = tagId) } just Runs
            val repository = AccountMemoFilterRepositoryImpl(accountMemoFilterLocalDataSource = localDataSource)

            repository.upsert(account = account, tagId = tagId)

            coVerify(exactly = 1) { localDataSource.upsert(accountId = account.id, tagId = tagId) }
        }

        test("태그 선택 해제는 현재 계정의 선택에서 제거한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val localDataSource = mockk<AccountMemoFilterLocalDataSource>()
            coEvery { localDataSource.delete(accountId = account.id, tagId = tagId) } just Runs
            val repository = AccountMemoFilterRepositoryImpl(accountMemoFilterLocalDataSource = localDataSource)

            repository.delete(account = account, tagId = tagId)

            coVerify(exactly = 1) { localDataSource.delete(accountId = account.id, tagId = tagId) }
        }

        test("태그 선택 전체 해제는 현재 계정의 필터 선택을 모두 제거한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val localDataSource = mockk<AccountMemoFilterLocalDataSource>()
            coEvery { localDataSource.deleteAll(accountId = account.id) } just Runs
            val repository = AccountMemoFilterRepositoryImpl(accountMemoFilterLocalDataSource = localDataSource)

            repository.deleteAll(account = account)

            coVerify(exactly = 1) { localDataSource.deleteAll(accountId = account.id) }
        }
    }) {
    public companion object {
        private fun tag(): TagLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<TagLocalEntity>()
                .setExp(TagLocalEntity::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(TagLocalEntity::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()
    }
}
