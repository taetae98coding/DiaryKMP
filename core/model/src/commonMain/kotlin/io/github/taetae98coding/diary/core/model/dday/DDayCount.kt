package io.github.taetae98coding.diary.core.model.dday

public sealed interface DDayCount {
    public data class Upcoming(
        val remainingDays: Int,
        val period: DDayPeriod,
    ) : DDayCount

    public data object Today : DDayCount

    public data class Passed(
        val passedDays: Int,
        val period: DDayPeriod,
    ) : DDayCount
}
