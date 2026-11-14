package io.github.taetae98coding.diary.data.routine.repository

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.datasource.AccountRoutineLocalDataSource
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.transaction.AccountRoutineTransaction
import io.github.taetae98coding.diary.core.testing.account.accountUser
import io.github.taetae98coding.diary.core.testing.routine.routine
import io.github.taetae98coding.diary.core.testing.routine.routineDetail
import io.github.taetae98coding.diary.core.testing.routine.routineLocalEntity
import io.github.taetae98coding.diary.data.core.mapper.toDomain
import io.github.taetae98coding.diary.data.core.mapper.toLocal
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.MutableStateFlow

private class RoutineRepositoryTestException : RuntimeException()

class AccountRoutineRepositoryImplTest :
    FunSpec({
        test("TC-721ea497-efd4-4db1-b751-a7204dc77179 TC-cb549cbe-7bb3-4087-9871-d262ead82419 루틴과 고른 태그와의 연결을 현재 계정으로 한 번에 함께 저장한다") {
            val account = Arb.accountUser().next()
            val routine = Arb.routine(isDeleted = false).next()
            val tagIdSet = setOf(Arb.kotlinUuid().next(), Arb.kotlinUuid().next())
            val transaction = mockk<AccountRoutineTransaction>()
            val routineSlot = slot<RoutineLocalEntity>()
            val linkSlot = slot<List<EntityTagLocalEntity>>()
            coEvery { transaction.upsert(accountId = account.id, routine = capture(routineSlot), routineTagList = capture(linkSlot)) } returns Unit
            val repository = AccountRoutineRepositoryImpl(accountRoutineLocalDataSource = mockk(), accountRoutineTransaction = transaction)

            repository.upsert(account = account, routine = routine, tagIdSet = tagIdSet)

            coVerify(exactly = 1) { transaction.upsert(accountId = any(), routine = any(), routineTagList = any()) }
            routineSlot.captured shouldBe routine.toLocal()
            linkSlot.captured shouldContainExactlyInAnyOrder
                tagIdSet.map { tagId ->
                    EntityTagLocalEntity(entityId = routine.id, tagId = tagId, isDeleted = false, updatedAt = routine.updatedAt, createdAt = routine.createdAt)
                }
        }

        test("TC-e0d1f1aa-a05a-47ba-98f3-b09ea206f9f5 태그를 고르지 않으면 태그 연결 없이 루틴만 저장한다") {
            val account = Arb.accountUser().next()
            val routine = Arb.routine(isDeleted = false).next()
            val transaction = mockk<AccountRoutineTransaction>()
            coEvery { transaction.upsert(accountId = any(), routine = any(), routineTagList = any()) } returns Unit
            val repository = AccountRoutineRepositoryImpl(accountRoutineLocalDataSource = mockk(), accountRoutineTransaction = transaction)

            repository.upsert(account = account, routine = routine, tagIdSet = emptySet())

            coVerify(exactly = 1) { transaction.upsert(accountId = account.id, routine = routine.toLocal(), routineTagList = emptyList()) }
        }

        test("TC-2bb300e0-0d65-4b6a-8660-4ea88218e211 TC-34479437-32c2-4d78-84ce-d20748c95e38 기기 저장의 실패를 삼키지 않고 그대로 전달한다") {
            val account = Arb.accountUser().next()
            val routine = Arb.routine(isDeleted = false).next()
            val transaction = mockk<AccountRoutineTransaction>()
            coEvery { transaction.upsert(accountId = any(), routine = any(), routineTagList = any()) } throws RoutineRepositoryTestException()
            coEvery { transaction.updateDetail(accountId = any(), routineId = any(), detail = any(), updatedAt = any()) } throws RoutineRepositoryTestException()
            coEvery { transaction.updateFinished(accountId = any(), routineId = any(), isFinished = any(), updatedAt = any()) } throws RoutineRepositoryTestException()
            coEvery { transaction.updateDeleted(accountId = any(), routineId = any(), isDeleted = any(), updatedAt = any()) } throws RoutineRepositoryTestException()
            val repository = AccountRoutineRepositoryImpl(accountRoutineLocalDataSource = mockk(), accountRoutineTransaction = transaction)

            shouldThrowExactly<RoutineRepositoryTestException> { repository.upsert(account = account, routine = routine, tagIdSet = setOf(Arb.kotlinUuid().next())) }
            shouldThrowExactly<RoutineRepositoryTestException> {
                repository.updateDetail(account = account, routineId = routine.id, detail = routine.detail, updatedAt = Arb.secondPrecisionInstant().next())
            }
            shouldThrowExactly<RoutineRepositoryTestException> {
                repository.updateDeleted(account = account, routineId = routine.id, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next())
            }
        }

        test("TC-6c70fa47-52f4-4b00-b897-c505b3706104 완료 여부 저장의 실패를 삼키지 않고 그대로 전달한다") {
            val account = Arb.accountUser().next()
            val transaction = mockk<AccountRoutineTransaction>()
            coEvery { transaction.updateFinished(accountId = any(), routineId = any(), isFinished = any(), updatedAt = any()) } throws RoutineRepositoryTestException()
            val repository = AccountRoutineRepositoryImpl(accountRoutineLocalDataSource = mockk(), accountRoutineTransaction = transaction)

            listOf(true, false).forEach { isFinished ->
                shouldThrowExactly<RoutineRepositoryTestException> {
                    repository.updateFinished(account = account, routineId = Arb.kotlinUuid().next(), isFinished = isFinished, updatedAt = Arb.secondPrecisionInstant().next())
                }
            }
        }

        test("TC-2300a4b5-9cd2-499b-b005-33cfb830d42f 완료 여부 변경은 현재 계정의 그 루틴에 넘긴 값 그대로 반영하고 바뀐 수를 돌려준다") {
            val account = Arb.accountUser().next()
            val routineId = Arb.kotlinUuid().next()
            val updatedAt = Arb.secondPrecisionInstant().next()
            val transaction = mockk<AccountRoutineTransaction>()
            coEvery { transaction.updateFinished(accountId = account.id, routineId = routineId, isFinished = true, updatedAt = updatedAt) } returns 1
            coEvery { transaction.updateFinished(accountId = account.id, routineId = routineId, isFinished = false, updatedAt = updatedAt) } returns 0
            val repository = AccountRoutineRepositoryImpl(accountRoutineLocalDataSource = mockk(), accountRoutineTransaction = transaction)

            repository.updateFinished(account = account, routineId = routineId, isFinished = true, updatedAt = updatedAt) shouldBe 1
            repository.updateFinished(account = account, routineId = routineId, isFinished = false, updatedAt = updatedAt) shouldBe 0
            coVerify(exactly = 0) { transaction.updateDetail(accountId = any(), routineId = any(), detail = any(), updatedAt = any()) }
            coVerify(exactly = 0) { transaction.updateDeleted(accountId = any(), routineId = any(), isDeleted = any(), updatedAt = any()) }
        }

        test("수정과 삭제 여부 변경은 현재 계정의 그 루틴에 넘긴 값 그대로 반영하고 바뀐 수를 돌려준다") {
            val account = Arb.accountUser().next()
            val routineId = Arb.kotlinUuid().next()
            val detail = Arb.routineDetail().next()
            val updatedAt = Arb.secondPrecisionInstant().next()
            val isDeleted = Arb.boolean().next()
            val transaction = mockk<AccountRoutineTransaction>()
            coEvery { transaction.updateDetail(accountId = account.id, routineId = routineId, detail = detail.toLocal(), updatedAt = updatedAt) } returns 1
            coEvery { transaction.updateDeleted(accountId = account.id, routineId = routineId, isDeleted = isDeleted, updatedAt = updatedAt) } returns 0
            val repository = AccountRoutineRepositoryImpl(accountRoutineLocalDataSource = mockk(), accountRoutineTransaction = transaction)

            repository.updateDetail(account = account, routineId = routineId, detail = detail, updatedAt = updatedAt) shouldBe 1
            repository.updateDeleted(account = account, routineId = routineId, isDeleted = isDeleted, updatedAt = updatedAt) shouldBe 0
        }

        test("TC-59993333-fe5a-4248-84d9-7a7ca26b660e 상세 대상은 현재 계정으로 기기를 조회하고 없으면 없음을, 바뀌면 바뀐 루틴을 전달한다") {
            val account = Arb.accountUser().next()
            val local = Arb.routineLocalEntity().next()
            val localFlow = MutableStateFlow<RoutineLocalEntity?>(null)
            val dataSource = mockk<AccountRoutineLocalDataSource>()
            every { dataSource.find(accountId = account.id, routineId = local.id) } returns localFlow
            val repository = AccountRoutineRepositoryImpl(accountRoutineLocalDataSource = dataSource, accountRoutineTransaction = mockk())

            repository.find(account = account, routineId = local.id).test {
                awaitItem() shouldBe null

                localFlow.value = local

                awaitItem() shouldBe local.toDomain()
                cancelAndIgnoreRemainingEvents()
            }
        }

        test("완료되지 않은 루틴의 유무는 현재 계정으로 기기를 조회한 값을 그대로 전달한다") {
            val account = Arb.accountUser().next()
            val existsFlow = MutableStateFlow(false)
            val dataSource = mockk<AccountRoutineLocalDataSource>()
            every { dataSource.getUnfinishedExists(accountId = account.id) } returns existsFlow
            val repository = AccountRoutineRepositoryImpl(accountRoutineLocalDataSource = dataSource, accountRoutineTransaction = mockk())

            repository.getUnfinishedExists(account = account).test {
                awaitItem() shouldBe false

                existsFlow.value = true

                awaitItem() shouldBe true
                cancelAndIgnoreRemainingEvents()
            }
        }
    })
