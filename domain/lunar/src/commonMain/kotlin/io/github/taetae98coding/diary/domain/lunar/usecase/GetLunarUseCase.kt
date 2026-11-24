package io.github.taetae98coding.diary.domain.lunar.usecase

import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.model.lunar.LunarDate
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.lunar.repository.LunarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateRange

@Inject
public class GetLunarUseCase internal constructor(
    private val lunarRepository: LunarRepository,
) : FlowUseCase<LocalDateRange, List<LunarDate>>() {
    override fun execute(parameter: LocalDateRange): Flow<Result<List<LunarDate>>> =
        lunarRepository
            .get(dateRange = parameter)
            .map { lunarDateList -> Result.success(lunarDateList) }
}
