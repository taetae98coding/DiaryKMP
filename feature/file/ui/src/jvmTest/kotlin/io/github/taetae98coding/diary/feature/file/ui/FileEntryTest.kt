@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.file.ui

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.file.api.FileAddNavKey
import io.github.taetae98coding.diary.feature.file.api.FileDetailNavKey
import io.github.taetae98coding.diary.feature.file.api.FileHomeNavKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class FileEntryTest :
    FunSpec({
        test("TC-675a9714-031d-420a-8df3-639645c0a8c6 파일 목록에서 이어 진입한 FileAdd와 FileDetail은 목록·상세 배치의 상세 pane이다") {
            val detail = FileDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
            val caseList =
                listOf(
                    listOf(OtherNavKey, FileHomeNavKey, FileAddNavKey) to FileAddNavKey,
                    listOf(OtherNavKey, FileHomeNavKey, detail) to detail,
                    listOf(OtherNavKey, FileHomeNavKey, detail, FileAddNavKey) to FileAddNavKey,
                )

            caseList.forEach { (backStack, key) ->
                metadataOf(backStack = backStack, key = key).keys shouldBe detailPaneMetadataKeys
            }
        }

        test("TC-93ef7888-533c-419e-ab80-d0ca6304a6f8 파일 목록에서 진입하지 않은 FileAdd와 FileDetail은 목록·상세 배치에 참여하지 않는다") {
            val detail = FileDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
            val caseList =
                listOf(
                    listOf(OtherNavKey, FileAddNavKey) to FileAddNavKey,
                    listOf(FileHomeNavKey, OtherNavKey, FileAddNavKey) to FileAddNavKey,
                    listOf(OtherNavKey, detail) to detail,
                    listOf(FileHomeNavKey, OtherNavKey, detail) to detail,
                )

            caseList.forEach { (backStack, key) ->
                metadataOf(backStack = backStack, key = key).shouldBeEmpty()
            }
        }

        test("FileHome 화면은 목록·상세 배치의 목록 pane이다") {
            val backStack = listOf(OtherNavKey, FileHomeNavKey)

            metadataOf(backStack = backStack, key = FileHomeNavKey).keys shouldBe listPaneMetadataKeys
        }
    }) {
    companion object {
        private val detailPaneMetadataKeys: Set<String> =
            (ListDetailSceneStrategy.detailPane() + ListDetailSceneStrategy.preferredPaneSize(width = 0.5f)).keys

        private val listPaneMetadataKeys: Set<String> =
            (ListDetailSceneStrategy.listPane(detailPlaceholder = {}) + ListDetailSceneStrategy.preferredPaneSize(width = 0.5f)).keys

        private fun metadataOf(
            backStack: List<ScreenNavKey>,
            key: ScreenNavKey,
        ): Map<String, Any> {
            val provider = entryProvider<ScreenNavKey> { fileEntry(backStack = NavBackStack(*backStack.toTypedArray())) }

            return provider(key).metadata
        }
    }
}

// 파일 목록이 아닌 진입 화면을 대신한다. `더보기` 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object OtherNavKey : ScreenNavKey {
    override val screenName: String
        get() = "Other"
}
