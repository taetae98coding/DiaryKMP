package io.github.taetae98coding.diary.work.appaction.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class AppActionEpochTest :
    FunSpec({
        test("ISO-8601 시각을 epoch 밀리초로 바꾼다") {
            appActionEpochMillis(instant = "1970-01-01T00:00:00Z") shouldBe 0L
            appActionEpochMillis(instant = "2026-01-31T09:30:15.250Z") shouldBe 1_769_851_815_250L
        }
    })
