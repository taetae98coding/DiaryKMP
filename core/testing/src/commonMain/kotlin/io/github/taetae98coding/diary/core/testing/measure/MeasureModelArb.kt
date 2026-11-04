package io.github.taetae98coding.diary.core.testing.measure

import io.github.taetae98coding.diary.core.model.measure.Length
import io.github.taetae98coding.diary.core.model.measure.Length.Companion.millimeter
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map

private const val MAX_MILLIMETER = 3_000

// Length는 생성자가 private이라 단위 확장 프로퍼티로 만든다.
public fun Arb.Companion.length(): Arb<Length> = Arb.int(0..MAX_MILLIMETER).map { value -> value.millimeter }
