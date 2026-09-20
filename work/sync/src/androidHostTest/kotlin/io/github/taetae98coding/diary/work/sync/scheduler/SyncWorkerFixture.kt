package io.github.taetae98coding.diary.work.sync.scheduler

import android.content.Context
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import io.github.taetae98coding.diary.work.sync.work.SyncWork
import io.mockk.every
import io.mockk.mockk

internal fun mockSyncWorkerFactory(syncWork: SyncWork): WorkerFactory =
    mockk {
        every { createWorker(any(), any(), any()) } answers {
            SyncWorker(
                context = firstArg<Context>(),
                parameters = thirdArg<WorkerParameters>(),
                syncWork = syncWork,
            )
        }
        every { createWorkerWithDefaultFallback(any(), any(), any()) } answers {
            SyncWorker(
                context = firstArg<Context>(),
                parameters = thirdArg<WorkerParameters>(),
                syncWork = syncWork,
            )
        }
    }
