package io.github.taetae98coding.diary.work.sync.scheduler

import androidx.work.WorkerFactory
import io.github.taetae98coding.diary.work.sync.work.SyncWork
import io.mockk.every
import io.mockk.mockk

internal fun mockSyncWorkerFactory(syncWork: SyncWork): WorkerFactory =
    SyncWorkerFactory(
        syncWorkerFactory =
            mockk<SyncWorker.Factory> {
                every { create(context = any(), parameters = any()) } answers {
                    SyncWorker(
                        context = firstArg(),
                        parameters = secondArg(),
                        syncWork = syncWork,
                    )
                }
            },
    )
