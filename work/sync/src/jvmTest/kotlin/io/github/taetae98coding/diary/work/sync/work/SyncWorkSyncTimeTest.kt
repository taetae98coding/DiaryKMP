package io.github.taetae98coding.diary.work.sync.work

import io.github.taetae98coding.diary.core.model.account.Account
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

class SyncWorkSyncTimeTest :
    FunSpec({
        test("TC-DATA-SYNC-DOMAIN-078 업로드와 내려받기가 모두 끝나면 마지막 동기화 시각을 갱신한다") {
            runTest {
                val context = context()

                context.subject.doWork()

                coVerify(exactly = 1) {
                    context.accountSyncTimeLocalDataSource.upsert(accountId = context.accountId, syncedAt = context.syncedAt)
                }
            }
        }

        test("TC-DATA-SYNC-DOMAIN-078 업로드가 실패하면 마지막 동기화 시각을 갱신하지 않는다") {
            runTest {
                val context = context()
                coEvery { context.tagSyncLocalDataSource.findPending(accountId = context.accountId) } throws TestException("push")

                shouldThrow<TestException> { context.subject.doWork() }

                coVerify(exactly = 0) {
                    context.accountSyncTimeLocalDataSource.upsert(accountId = any(), syncedAt = any())
                }
            }
        }

        test("TC-DATA-SYNC-DOMAIN-078 내려받기가 실패하면 마지막 동기화 시각을 갱신하지 않는다") {
            runTest {
                val context = context()
                coEvery {
                    context.tagRemoteDataSource.pull(usn = any())
                } throws TestException("pull")

                shouldThrow<TestException> { context.subject.doWork() }

                coVerify(exactly = 0) {
                    context.accountSyncTimeLocalDataSource.upsert(accountId = any(), syncedAt = any())
                }
            }
        }

        test("TC-DATA-SYNC-DOMAIN-075 실행될 때마다 업로드를 시작하기 전에 그 계정의 강제 전체 재동기화를 판단한다") {
            runTest {
                val context = context()

                context.subject.doWork()
                context.subject.doWork()

                coVerify(exactly = 2) { context.prepareSyncUseCase(parameter = context.accountId) }
                coVerifyOrder {
                    context.prepareSyncUseCase(parameter = context.accountId)
                    context.tagSyncLocalDataSource.findPending(accountId = context.accountId)
                }
            }
        }

        test("TC-DATA-SYNC-DOMAIN-074 강제 전체 재동기화를 판단한 뒤 같은 계정으로 동기화를 이어 간다") {
            runTest {
                val context = context()

                context.subject.doWork()

                coVerifyOrder {
                    context.prepareSyncUseCase(parameter = context.accountId)
                    context.tagRemoteDataSource.pull(usn = any())
                    context.accountSyncTimeLocalDataSource.upsert(accountId = context.accountId, syncedAt = context.syncedAt)
                }
            }
        }

        test("강제 전체 재동기화 판단이 실패하면 업로드하지 않고 동기화가 실패한다") {
            runTest {
                val context = context()
                coEvery { context.prepareSyncUseCase(parameter = context.accountId) } returns Result.failure(TestException("prepare"))

                shouldThrow<TestException> { context.subject.doWork() }

                coVerify(exactly = 0) { context.tagSyncLocalDataSource.findPending(accountId = any()) }
                coVerify(exactly = 0) {
                    context.accountSyncTimeLocalDataSource.upsert(accountId = any(), syncedAt = any())
                }
            }
        }

        test("실행 시점에 계정이 게스트면 강제 전체 재동기화를 판단하지 않는다") {
            runTest {
                val context = context(accountFlow = flowOf(Result.success(Account.Guest)))

                context.subject.doWork()

                coVerify(exactly = 0) { context.prepareSyncUseCase(parameter = any()) }
            }
        }
    })
