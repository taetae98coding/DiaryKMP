package io.github.taetae98coding.diary.work.sync.scheduler

import io.github.taetae98coding.diary.work.sync.scheduler.PeriodicSyncWorkScheduler
import io.github.taetae98coding.diary.work.sync.work.SyncWork
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import platform.BackgroundTasks.BGTask
import kotlin.time.Duration

internal class BackgroundTaskPeriodicSyncWorkScheduler(
    private val syncWork: SyncWork,
    private val scope: CoroutineScope,
) : PeriodicSyncWorkScheduler {
    override fun schedule(period: Duration) {
        // 시스템이 앱을 깨워 실행할 때는 앱이 종료된 뒤일 수 있으므로 다음 주기를 다시 예약할 간격을 기기에 남긴다.
        savePeriodicSyncPeriod(period = period)

        scope.launch {
            // 이미 남아 있는 예약을 다시 제출하면 다음 실행 시각이 뒤로 밀리므로 없을 때만 제출한다.
            if (!isPeriodicSyncRequestPending()) {
                submitPeriodicSyncRequest(period = period)
            }
        }
    }

    override fun cancel() {
        clearPeriodicSyncPeriod()
        cancelPeriodicSyncRequest()
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
