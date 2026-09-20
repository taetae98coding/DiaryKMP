package io.github.taetae98coding.diary.app

import androidx.lifecycle.Lifecycle
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class SyncMinActiveStateTest :
    FunSpec({
        test("TC-DATA-SYNC-DOMAIN-068 TC-DATA-SYNC-DOMAIN-069 JVM 데스크톱은 앱 창이 포커스를 가진 동안을 활성 상태로 본다") {
            syncMinActiveState shouldBe Lifecycle.State.RESUMED
        }
    })
