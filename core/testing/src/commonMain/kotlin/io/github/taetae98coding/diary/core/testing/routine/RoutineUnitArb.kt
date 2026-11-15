package io.github.taetae98coding.diary.core.testing.routine

import io.kotest.property.Arb
import io.kotest.property.arbitrary.of

public fun Arb.Companion.routineUnit(): Arb<String> = Arb.of("", "회", "ml", "m", "쪽", "times")
