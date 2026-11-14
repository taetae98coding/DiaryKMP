package io.github.taetae98coding.diary.data.routine.repository

import io.github.taetae98coding.diary.core.database.api.routinerecord.transaction.AccountRoutineRecordTransaction
import io.github.taetae98coding.diary.core.testing.account.accountUser
import io.github.taetae98coding.diary.core.testing.routine.routineRecord
import io.github.taetae98coding.diary.data.routine.mapper.toLocal
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

private class RoutineRecordRepositoryTestException : RuntimeException()

class AccountRoutineRecordRepositoryImplTest :
    FunSpec({
        test("TC-4c532680-5a51-4c03-a37d-bee4981c14c8 기록 넣기는 현재 계정으로 새 기록을 한 번 저장하고 저장된 수를 돌려준다") {
            val account = Arb.accountUser().next()
            val record = Arb.routineRecord(routineId = Arb.kotlinUuid().next()).next()
            val transaction = mockk<AccountRoutineRecordTransaction>()
            coEvery { transaction.upsert(accountId = account.id, record = record.toLocal()) } returns 1
            val repository = AccountRoutineRecordRepositoryImpl(accountRoutineRecordLocalDataSource = mockk(), accountRoutineRecordTransaction = transaction)

            repository.upsert(account = account, record = record) shouldBe 1

            coVerify(exactly = 1) { transaction.upsert(accountId = any(), record = any()) }
        }

        test("TC-66ca9007-c763-4a22-8cf6-66a583355e88 기록 빼기는 현재 계정의 그 기록에 삭제 여부와 수정 시각만 넘긴다") {
            val account = Arb.accountUser().next()
            val recordId = Arb.kotlinUuid().next()
            val updatedAt = Arb.secondPrecisionInstant().next()
            val transaction = mockk<AccountRoutineRecordTransaction>()
            coEvery { transaction.updateDeleted(accountId = account.id, recordId = recordId, isDeleted = true, updatedAt = updatedAt) } returns 1
            val repository = AccountRoutineRecordRepositoryImpl(accountRoutineRecordLocalDataSource = mockk(), accountRoutineRecordTransaction = transaction)

            repository.updateDeleted(account = account, recordId = recordId, isDeleted = true, updatedAt = updatedAt) shouldBe 1

            coVerify(exactly = 0) { transaction.upsert(accountId = any(), record = any()) }
        }

        test("TC-34479437-32c2-4d78-84ce-d20748c95e38 기기 저장의 실패를 삼키지 않고 그대로 전달한다") {
            val account = Arb.accountUser().next()
            val transaction = mockk<AccountRoutineRecordTransaction>()
            coEvery { transaction.upsert(accountId = any(), record = any()) } throws RoutineRecordRepositoryTestException()
            coEvery { transaction.updateDeleted(accountId = any(), recordId = any(), isDeleted = any(), updatedAt = any()) } throws RoutineRecordRepositoryTestException()
            val repository = AccountRoutineRecordRepositoryImpl(accountRoutineRecordLocalDataSource = mockk(), accountRoutineRecordTransaction = transaction)

            shouldThrowExactly<RoutineRecordRepositoryTestException> {
                repository.upsert(account = account, record = Arb.routineRecord(routineId = Arb.kotlinUuid().next()).next())
            }
            shouldThrowExactly<RoutineRecordRepositoryTestException> {
                repository.updateDeleted(account = account, recordId = Arb.kotlinUuid().next(), isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next())
            }
        }
    })
