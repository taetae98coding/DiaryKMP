package io.github.taetae98coding.diary.domain.sync.usecase

import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.sync.SyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
public class GetProgressReportedUseCase internal constructor(
    private val syncManager: SyncManager,
) : FlowUseCase<Unit, Boolean>() {
    override fun execute(parameter: Unit): Flow<Result<Boolean>> = syncManager.isProgressReported.map { isProgressReported -> Result.success(isProgressReported) }
}
