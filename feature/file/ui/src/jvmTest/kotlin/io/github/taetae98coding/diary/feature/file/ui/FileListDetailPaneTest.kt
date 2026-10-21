package io.github.taetae98coding.diary.feature.file.ui

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.file.api.FileAddNavKey
import io.github.taetae98coding.diary.feature.file.api.FileDetailNavKey
import io.github.taetae98coding.diary.feature.file.api.FileHomeNavKey
import io.github.taetae98coding.diary.feature.file.api.isFileAddOnDetailPane
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class FileListDetailPaneTest :
    FunSpec({
        test("TC-FILE-LIST-DETAIL-FEATURE-003 선택한 파일이 없거나 FileAdd로 이동했으면 상세 영역에 FileAdd가 놓인다") {
            val backStackCases =
                listOf(
                    listOf(FileHomeNavKey),
                    listOf(OtherScreenNavKey, FileHomeNavKey),
                    listOf(OtherScreenNavKey, FileHomeNavKey, FileAddNavKey),
                    listOf(OtherScreenNavKey, FileHomeNavKey, FileDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()), FileAddNavKey),
                )

            backStackCases.forEach { backStack ->
                backStack.isFileAddOnDetailPane() shouldBe true
            }
        }

        test("TC-FILE-LIST-DETAIL-FEATURE-004 FileDetail로 이동했으면 상세 영역에 FileAdd가 놓이지 않는다") {
            val backStackCases =
                listOf(
                    listOf(OtherScreenNavKey, FileHomeNavKey, FileDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())),
                    listOf(OtherScreenNavKey, FileHomeNavKey, FileAddNavKey, FileDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())),
                )

            backStackCases.forEach { backStack ->
                backStack.isFileAddOnDetailPane() shouldBe false
            }
        }

        test("파일 목록에서 진입하지 않은 FileAdd는 상세 영역에 놓인 것으로 보지 않는다") {
            val backStackCases =
                listOf(
                    emptyList(),
                    listOf(OtherScreenNavKey),
                    listOf(OtherScreenNavKey, FileAddNavKey),
                    listOf(FileHomeNavKey, OtherScreenNavKey, FileAddNavKey),
                )

            backStackCases.forEach { backStack ->
                backStack.isFileAddOnDetailPane() shouldBe false
            }
        }
    })

// 파일 목록이 아닌 진입 화면을 대신한다. `더보기` 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object OtherScreenNavKey : ScreenNavKey {
    override val screenName: String
        get() = "Other"
}
