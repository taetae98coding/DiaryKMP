@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.taetae98coding.diary.compose.core.scene

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.SceneStrategyScope
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class BottomSheetSceneStrategyTest : FunSpec() {
    init {
        test("단일 Entry에 Bottom Sheet metadata가 있으면 BottomSheetScene을 반환한다") {
            val key = fixtureMonkey.giveMeOne<String>()
            val entry =
                NavEntry(
                    key = key,
                    contentKey = key,
                    metadata = BottomSheetSceneStrategy.bottomSheet(),
                    content = {},
                )

            val scene =
                with(BottomSheetSceneStrategy<String>()) {
                    SceneStrategyScope<String>().calculateScene(entries = listOf(entry))
                }

            scene.shouldBeInstanceOf<BottomSheetScene<String>>()
            scene.key shouldBe key
            scene.entries shouldBe listOf(entry)
            scene.previousEntries shouldBe emptyList()
            scene.overlaidEntries shouldBe emptyList()
        }

        test("마지막 Entry에 Bottom Sheet metadata가 없으면 Scene을 반환하지 않는다") {
            val key = fixtureMonkey.giveMeOne<String>()
            val entry = NavEntry(key = key, contentKey = key, content = {})

            val scene =
                with(BottomSheetSceneStrategy<String>()) {
                    SceneStrategyScope<String>().calculateScene(entries = listOf(entry))
                }

            scene shouldBe null
        }
    }

    companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
