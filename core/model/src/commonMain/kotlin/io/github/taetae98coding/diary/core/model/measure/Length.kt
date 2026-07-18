package io.github.taetae98coding.diary.core.model.measure

import kotlin.jvm.JvmInline
import kotlin.math.roundToInt

@JvmInline
public value class Length private constructor(
    private val millimeter: Double,
) : Comparable<Length> {
    public val inMillimeter: Double
        get() = millimeter

    public val inWholeMillimeter: Int
        get() = millimeter.roundToInt()

    public val inCentimeter: Double
        get() = millimeter / MILLIMETER_PER_CENTIMETER

    override fun compareTo(other: Length): Int = millimeter.compareTo(other.millimeter)

    public companion object {
        public val Double.centimeter: Length
            get() = Length(millimeter = this * MILLIMETER_PER_CENTIMETER)

        public val Int.millimeter: Length
            get() = Length(millimeter = toDouble())
    }
}

private const val MILLIMETER_PER_CENTIMETER = 10.0
