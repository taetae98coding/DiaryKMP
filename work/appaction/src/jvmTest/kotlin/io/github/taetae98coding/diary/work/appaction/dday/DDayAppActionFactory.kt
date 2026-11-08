package io.github.taetae98coding.diary.work.appaction.dday

import io.github.taetae98coding.diary.work.appaction.core.useCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Clock

internal fun ddayAppActionOf(vararg useCaseArray: Any): DDayAppAction {
    val useCaseList = useCaseArray.toList()

    return DDayAppAction(
        pageDDayUseCase = useCaseList.useCase(),
        findDDayUseCase = useCaseList.useCase(),
        getEntityTagUseCase = useCaseList.useCase(),
        addDDayUseCase = useCaseList.useCase(),
        updateDDayUseCase = useCaseList.useCase(),
        deleteDDayUseCase = useCaseList.useCase(),
        restoreDDayUseCase = useCaseList.useCase(),
        addEntityTagUseCase = useCaseList.useCase(),
        removeEntityTagUseCase = useCaseList.useCase(),
        clock = useCaseList.useCase(),
    )
}

// 기기 시간대에서 그 날짜의 한낮을 돌려줘, 시간대와 관계없이 오늘이 그 날짜가 되게 한다.
internal fun clockAt(date: LocalDate): Clock {
    val clock = mockk<Clock>()
    every { clock.now() } returns LocalDateTime(date, LocalTime(hour = 12, minute = 0)).toInstant(TimeZone.currentSystemDefault())

    return clock
}
