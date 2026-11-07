package io.github.taetae98coding.diary.app.shared

import io.github.taetae98coding.diary.compose.web.DiaryWebSession
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean

internal fun Arb.Companion.diaryWebSession(): Arb<DiaryWebSession> =
    arbitrary {
        DiaryWebSession(
            isRestoring = Arb.boolean().bind(),
        )
    }
