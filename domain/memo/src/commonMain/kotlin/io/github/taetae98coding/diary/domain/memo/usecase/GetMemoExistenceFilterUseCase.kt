package io.github.taetae98coding.diary.domain.memo.usecase

import io.github.taetae98coding.diary.core.model.memo.MemoExistenceFilter
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.memo.repository.MemoExistenceFilterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
public class GetMemoExistenceFilterUseCase internal constructor(
    private val memoExistenceFilterRepository: MemoExistenceFilterRepository,
) : FlowUseCase<Unit, MemoExistenceFilter>() {
    override fun execute(parameter: Unit): Flow<Result<MemoExistenceFilter>> = memoExistenceFilterRepository.get().map { existence -> Result.success(existence) }
}
