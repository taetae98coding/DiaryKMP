package io.github.taetae98coding.diary.feature.memo.ui

import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

internal fun randomText(prefix: String): String = prefix + Arb.nonBlankString().next()
