package io.github.taetae98coding.diary.work.sync.work

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest

class SyncWorkSyncTimeTest :
    FunSpec({
        test("TC-DATA-SYNC-DOMAIN-072 업로드와 내려받기가 모두 끝나면 마지막 동기화 시각을 갱신한다") {
            runTest {
                val context = context()

                context.subject.doWork()

                coVerify(exactly = 1) {
                    context.accountSyncTimeLocalDataSource.upsert(accountId = context.accountId, syncedAt = context.syncedAt)
                }
            }
        }

        test("TC-DATA-SYNC-DOMAIN-072 업로드가 실패하면 마지막 동기화 시각을 갱신하지 않는다") {
            runTest {
                val context = context()
                coEvery { context.tagSyncLocalDataSource.findPending(accountId = context.accountId) } throws TestException("push")

                shouldThrow<TestException> { context.subject.doWork() }

                coVerify(exactly = 0) {
                    context.accountSyncTimeLocalDataSource.upsert(accountId = any(), syncedAt = any())
                }
            }
        }

        test("TC-DATA-SYNC-DOMAIN-072 내려받기가 실패하면 마지막 동기화 시각을 갱신하지 않는다") {
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
    })
