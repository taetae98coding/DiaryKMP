package io.github.taetae98coding.diary.domain.memo.usecase

import io.github.taetae98coding.diary.core.model.memo.MemoFilterExistence
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.memo.repository.MemoExistenceFilterRepository
import org.koin.core.annotation.Factory

@Factory
public class SetMemoTagExistenceFilterUseCase internal constructor(
    private val memoExistenceFilterRepository: MemoExistenceFilterRepository,
) : UseCase<MemoFilterExistence, Unit>() {
    override suspend fun execute(parameter: MemoFilterExistence) {
        memoExistenceFilterRepository.updateTag(existence = parameter)
    }
}
