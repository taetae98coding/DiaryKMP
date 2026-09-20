@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.work.sync.scheduler

import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.mp.KoinPlatform
import platform.BackgroundTasks.BGProcessingTaskRequest
import platform.BackgroundTasks.BGTaskRequest
import platform.BackgroundTasks.BGTaskScheduler
import platform.Foundation.NSDate
import platform.Foundation.NSUserDefaults
import platform.Foundation.dateByAddingTimeInterval
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

internal const val PERIODIC_SYNC_TASK_IDENTIFIER: String = "io.github.taetae98coding.diary.sync.periodic"

private const val PERIODIC_SYNC_ACCOUNT_ID_KEY: String = "periodicSyncAccountId"
private const val PERIODIC_SYNC_PERIOD_SECONDS_KEY: String = "periodicSyncPeriodSeconds"

// 시스템은 앱이 실행을 마치기 전에 등록된 식별자만 깨울 수 있으므로, 앱 시작 시점에 한 번 호출한다.
public fun registerPeriodicSyncBackgroundTask() {
    BGTaskScheduler.sharedScheduler.registerForTaskWithIdentifier(
        identifier = PERIODIC_SYNC_TASK_IDENTIFIER,
        usingQueue = null,
    ) { task ->
        // 시스템이 앱을 깨운 시점에야 동기화 수단이 필요하므로 등록 시점에 미리 만들지 않는다.
        KoinPlatform.getKoin().get<BackgroundTaskPeriodicSyncWorkScheduler>().runTask(task = task)
    }
}

internal fun savePeriodicSyncTarget(
    accountId: Uuid,
    period: Duration,
) {
    NSUserDefaults.standardUserDefaults.apply {
        setObject(accountId.toString(), PERIODIC_SYNC_ACCOUNT_ID_KEY)
        setDouble(period.inWholeSeconds.toDouble(), PERIODIC_SYNC_PERIOD_SECONDS_KEY)
    }
}

internal fun clearPeriodicSyncTarget() {
    NSUserDefaults.standardUserDefaults.apply {
        removeObjectForKey(PERIODIC_SYNC_ACCOUNT_ID_KEY)
        removeObjectForKey(PERIODIC_SYNC_PERIOD_SECONDS_KEY)
    }
}

internal fun periodicSyncAccountId(): Uuid? =
    NSUserDefaults.standardUserDefaults
        .stringForKey(PERIODIC_SYNC_ACCOUNT_ID_KEY)
        ?.let { value -> runCatching { Uuid.parse(value) }.getOrNull() }

internal fun periodicSyncPeriod(): Duration? =
    NSUserDefaults.standardUserDefaults
        .doubleForKey(PERIODIC_SYNC_PERIOD_SECONDS_KEY)
        .toLong()
        .takeIf { seconds -> seconds > 0 }
        ?.seconds

internal fun submitPeriodicSyncRequest(period: Duration) {
    val request =
        BGProcessingTaskRequest(PERIODIC_SYNC_TASK_IDENTIFIER).apply {
            requiresNetworkConnectivity = true
            requiresExternalPower = false
            earliestBeginDate = NSDate().dateByAddingTimeInterval(period.inWholeSeconds.toDouble())
        }

    // 시뮬레이터처럼 백그라운드 작업을 허용하지 않는 환경에서는 제출이 거절된다. 이때는 다음 계기에 다시 예약한다.
    BGTaskScheduler.sharedScheduler.submitTaskRequest(request, null)
}

internal fun cancelPeriodicSyncRequest() {
    BGTaskScheduler.sharedScheduler.cancelTaskRequestWithIdentifier(PERIODIC_SYNC_TASK_IDENTIFIER)
}

internal suspend fun isPeriodicSyncRequestPending(): Boolean =
    suspendCoroutine { continuation ->
        BGTaskScheduler.sharedScheduler.getPendingTaskRequestsWithCompletionHandler { requestList ->
            val isPending =
                requestList
                    .orEmpty()
                    .any { request -> (request as? BGTaskRequest)?.identifier == PERIODIC_SYNC_TASK_IDENTIFIER }

            continuation.resume(isPending)
        }
    }
