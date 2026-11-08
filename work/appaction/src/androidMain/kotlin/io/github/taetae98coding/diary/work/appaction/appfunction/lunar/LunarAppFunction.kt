package io.github.taetae98coding.diary.work.appaction.appfunction.lunar

import io.github.taetae98coding.diary.work.appaction.appfunction.core.appFunctionCall
import io.github.taetae98coding.diary.work.appaction.lunar.LunarAppAction

internal class LunarAppFunction(
    private val lunarAppAction: LunarAppAction,
) {
    suspend fun list(
        startDate: String,
        endDate: String,
    ): List<AppFunctionLunarDate> = appFunctionCall { lunarAppAction.list(startDate = startDate, endDate = endDate).map { lunarDate -> lunarDate.toAppFunctionLunarDate() } }
}
