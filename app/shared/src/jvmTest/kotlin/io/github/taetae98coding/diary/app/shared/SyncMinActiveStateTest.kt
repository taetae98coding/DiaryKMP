package io.github.taetae98coding.diary.app.shared

import androidx.lifecycle.Lifecycle
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class SyncMinActiveStateTest :
    FunSpec({
        test("TC-26917c1b-cc78-4490-9ee0-e37320c43233 TC-ef293202-c717-49a9-826f-72d515b1ba10 JVM 데스크톱은 앱 창이 포커스를 가진 동안을 활성 상태로 본다") {
            syncMinActiveState shouldBe Lifecycle.State.RESUMED
        }
    })
