package io.github.taetae98coding.diary.data.memo.repository

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.memofilter.datasource.MemoExistenceFilterLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memofilter.entity.MemoExistenceFilterLocalEntity
import io.github.taetae98coding.diary.core.model.memo.MemoExistenceFilter
import io.github.taetae98coding.diary.core.model.memo.MemoFilterExistence
import io.github.taetae98coding.diary.data.memo.mapper.toDomain
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

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class MemoExistenceFilterRepositoryImplTest :
    FunSpec({
        test("TC-MEMO-HOME-DATA-011 유지된 유무 필터가 없으면 세 축을 전체로 조회한다") {
            val localDataSource = mockk<MemoExistenceFilterLocalDataSource>()
            every { localDataSource.find() } returns flowOf(null)
            val repository = MemoExistenceFilterRepositoryImpl(memoExistenceFilterLocalDataSource = localDataSource)

            repository.get().first() shouldBe MemoExistenceFilter()
        }

        test("유무 필터 조회는 저장된 상태를 도메인 모델로 변환한다") {
            val local =
                MemoExistenceFilterLocalEntity(
                    hasDate = true,
                    hasTag = null,
                    hasPlace = false,
                )
            val localDataSource = mockk<MemoExistenceFilterLocalDataSource>()
            every { localDataSource.find() } returns flowOf(local)
            val repository = MemoExistenceFilterRepositoryImpl(memoExistenceFilterLocalDataSource = localDataSource)

            repository.get().first() shouldBe local.toDomain()
        }

        test("유무 필터 조회 중 발생한 에러는 그대로 전파한다") {
            val failure = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val localDataSource = mockk<MemoExistenceFilterLocalDataSource>()
            every { localDataSource.find() } returns flow { throw failure }
            val repository = MemoExistenceFilterRepositoryImpl(memoExistenceFilterLocalDataSource = localDataSource)

            shouldThrow<IllegalStateException> {
                repository.get().first()
            } shouldBe failure
        }

        test("TC-MEMO-HOME-DATA-010 축 저장은 그 축의 값만 저장소에 전달한다") {
            val localDataSource = mockk<MemoExistenceFilterLocalDataSource>()
            coEvery { localDataSource.upsertHasDate(hasDate = any()) } just Runs
            coEvery { localDataSource.upsertHasTag(hasTag = any()) } just Runs
            coEvery { localDataSource.upsertHasPlace(hasPlace = any()) } just Runs
            val repository = MemoExistenceFilterRepositoryImpl(memoExistenceFilterLocalDataSource = localDataSource)

            repository.updateDate(existence = MemoFilterExistence.EXIST)
            repository.updateTag(existence = MemoFilterExistence.NOT_EXIST)
            repository.updatePlace(existence = MemoFilterExistence.ALL)

            coVerify(exactly = 1) { localDataSource.upsertHasDate(hasDate = true) }
            coVerify(exactly = 1) { localDataSource.upsertHasTag(hasTag = false) }
            coVerify(exactly = 1) { localDataSource.upsertHasPlace(hasPlace = null) }
        }
    })
