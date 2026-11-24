package io.github.taetae98coding.diary.work.appaction.lunar

import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.domain.lunar.usecase.GetLunarUseCase
import io.github.taetae98coding.diary.work.appaction.core.dateRangeArgument
import io.github.taetae98coding.diary.work.appaction.core.readFirst
import io.github.taetae98coding.diary.work.appaction.core.runAppAction

@Inject
public class LunarAppAction internal constructor(
    private val getLunarUseCase: GetLunarUseCase,
) {
    public suspend fun list(
        startDate: String,
        endDate: String,
    ): List<AppActionLunarDate> =
        runAppAction {
            getLunarUseCase
                .readFirst(parameter = dateRangeArgument(startDate = startDate, endDate = endDate))
                .map { lunarDate -> lunarDate.toAppActionLunarDate() }
        }
}
