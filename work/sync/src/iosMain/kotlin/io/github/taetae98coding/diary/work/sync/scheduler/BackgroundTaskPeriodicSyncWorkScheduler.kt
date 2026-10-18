package io.github.taetae98coding.diary.work.sync.scheduler

import io.github.taetae98coding.diary.work.sync.scheduler.PeriodicSyncWorkScheduler
import io.github.taetae98coding.diary.work.sync.work.SyncWork
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import platform.BackgroundTasks.BGTask
import kotlin.time.Duration

internal class BackgroundTaskPeriodicSyncWorkScheduler(
    private val syncWork: SyncWork,
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
) : PeriodicSyncWorkScheduler {
    // 제출과 취소는 시스템 예약 관리자의 응답을 기다리므로 메인 스레드 밖에서 한다. 한 줄로 실행되는 dispatcher와 잠금으로 요청한 순서를 지켜,
    // 예약 직후 취소해도 취소 뒤에 제출되지 않게 한다.
    private val mutex = Mutex()

    override fun schedule(period: Duration) {
        // 시스템이 앱을 깨워 실행할 때는 앱이 종료된 뒤일 수 있으므로 다음 주기를 다시 예약할 간격을 기기에 남긴다.
        savePeriodicSyncPeriod(period = period)

        scope.launch(dispatcher) {
            mutex.withLock {
                // 이미 남아 있는 예약을 다시 제출하면 다음 실행 시각이 뒤로 밀리므로 없을 때만 제출한다.
                if (!isPeriodicSyncRequestPending()) {
                    submitPeriodicSyncRequest(period = period)
                }
            }
        }
    }

    override fun cancel() {
        clearPeriodicSyncPeriod()

        scope.launch(dispatcher) {
            mutex.withLock { cancelPeriodicSyncRequest() }
        }
    }

    fun runTask(task: BGTask?) {
        if (task == null) return

        val period = periodicSyncPeriod()

        if (period == null) {
            task.setTaskCompletedWithSuccess(false)
            return
        }

        // 한 번의 예약은 한 번만 실행되므로, 이번 실행의 성패와 무관하게 다음 주기를 먼저 예약한다.
        submitPeriodicSyncRequest(period = period)

        val job =
            scope.launch {
                val isSuccess =
                    try {
                        syncWork.doWork()
                        true
                    } catch (exception: CancellationException) {
                        throw exception
                    } catch (_: Throwable) {
                        false
                    }

                task.setTaskCompletedWithSuccess(isSuccess)
            }

        task.expirationHandler = {
            job.cancel()
            task.setTaskCompletedWithSuccess(false)
        }
    }
}
